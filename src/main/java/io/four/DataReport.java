package io.four;


import io.four.codec.ClientDecoder;
import io.four.codec.ClientEncoder;
import io.four.utils.NewTraceId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.List;

import static io.four.Constants.NO_DELAY;
import static io.four.Filter.addError;

/**
 * 错误日志上报至数据中心，接收错误的traceID，同步至本机的过滤器中。
 * 上报数据同步？
 */
public class DataReport {

    private static ReportTransportClient client;

    public static void init(int port) {
        client = new ReportTransportClient(port);
    }

    public static void sendData(String s) {
        client.sendData(s);
    }

    public static void sendData(byte[] s) {
        client.sendData(s);
    }

    public static void writeData(List<byte[]> list) {
        //client.writeData(list);
        for(byte[] b : list) {
            writeData(b);
        }
    }
    public static void flush() {
        client.channel.flush();
    }

    public static void writeData(byte[] body) {
        client.writeData(body);
    }

    public static void writeData(String body) {
        client.writeData(body.getBytes());
    }

    /*public static void writeData(List<String> list) {
        for (String str : list) {
            client.writeData(str.getBytes());
        }
    }*/

    public static void sendFinished() {
        client.sendFinish("f");
    }

    public static void start() {
        client.start();
    }

    public static void sync() {
        try {
            client.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

class ReportTransportClient {

    private final String host = "localhost";
    private int port;
    public Channel channel;
    private Bootstrap bootstrap;

    public ReportTransportClient(int port) {
        this.port = port;
    }

    public void sendData(String body) {
        channel.writeAndFlush(body);
    }

    public void sendData(byte[] body) {
        channel.writeAndFlush(body);
    }

    public void writeData(byte[] body) {
        channel.write(body);
    }

    public void writeData(List<byte[]> body) {
        channel.writeAndFlush(body);
    }

    public void sendFinish(String f) {
        channel.flush();
        channel.writeAndFlush(f);
    }

    public void sync() throws InterruptedException {
        channel.closeFuture().sync();
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        //创建Bootstrap
        bootstrap = new Bootstrap();

        try {
            bootstrap.group(group)
                    .option(ChannelOption.TCP_NODELAY, NO_DELAY)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ClientEncoder());
                            ch.pipeline().addLast(new ClientDecoder());
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });
            connect();
        } catch (Exception e) {
            System.out.println("start report client error");
            System.exit(1);
        }
    }

    private void connect() throws InterruptedException {
        boolean connected = false;
        while (!connected) {
            try {
                //发送client id
                ChannelFuture future = bootstrap.connect().sync();
                channel = future.channel();
                connected = true;
            } catch (Exception e) {
                System.out.println("retry to connect to " + port);
                Thread.sleep(200);
            }
        }
        channel.writeAndFlush(String.valueOf(BootStarter.local_port));
        System.out.println("start client over, curr client is:" + BootStarter.local_port);
    }

    public void close() {
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeList(List<String> cache) {
        channel.write(cache);
    }
}

class ClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    //当被通知Channel是活跃的时候，发送一条消息
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    //接收消息时会调用该方法
    protected void channelRead0(ChannelHandlerContext ctx, String str) throws Exception {
        byte[] b = str.getBytes();
        addError(b);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

