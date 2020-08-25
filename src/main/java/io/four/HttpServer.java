package io.four;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.four.BootStarter.isCollector;
import static io.four.Collector.pullData;
import static io.four.Collector.warm;
import static io.four.Constants.*;

public class HttpServer {
    private int port;
    private ServerBootstrap b;
    private EventLoopGroup bossGroup;
    private Channel channel;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        // 创建EventLoopGroup
        bossGroup = new NioEventLoopGroup(1);
        try {
            b = new ServerBootstrap();
            b.group(bossGroup)
                    // 设置并绑定服务端Channel
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline ph = ch.pipeline();
                            ph.addLast("codec", new HttpServerCodec());
                            ph.addLast("aggregator", new HttpObjectAggregator(1024));
                            ph.addLast("handler", new HttpServerHandler());// 服务端业务逻辑
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture future = b.bind(port).sync();
            channel = future.channel();
            System.out.println("Http server start at port:" + port);
            // 等待服务端监听端口关闭
            //future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            channel.close().sync();
            bossGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class HttpServerHandler extends ChannelInboundHandlerAdapter {
    FullHttpResponse response;

    public HttpServerHandler() {
        super();
        response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(OK, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof FullHttpRequest)) {
            System.out.println("未知请求!");
            return;
        }
        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        try {
            String path = httpRequest.uri();          //获取路径
            HttpMethod method = httpRequest.method();//获取请求方法
            if (HttpMethod.GET.equals(method)) {
                process(path);
                ctx.writeAndFlush(response);
            }
            httpRequest.release();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }


    /**
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    private void process(String path) {
        String pathArr[] = path.split("\\?");
        if (SET_PARAM_PATH.equals(pathArr[0])) {
            String[] paramArr = pathArr[1].split("=");
            if (paramArr.length < 2) {
                return;
            }
            int port = Integer.parseInt(paramArr[1]);
            if (isCollector()) {
                pullData(port);
            } else {
                //DataCenter.conn(port);
                BootStarter.port = port;
            }
        } else if(READY_PATH.equals(pathArr[0])) {
            if(isCollector()) {
                warm();
            }
        }
    }
}

