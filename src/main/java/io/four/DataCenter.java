package io.four;

import com.alibaba.fastjson.JSON;
import io.four.codec.ServerDecoder;
import io.four.codec.ServerEncoder;
import io.four.utils.Tuple3;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jctools.queues.MpscLinkedQueue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.four.Codec.decodeTrace;
import static io.four.Constants.DATA_CENTER_PORT;
import static io.four.Constants.NO_DELAY;
import static io.four.utils.MD5.MD5;
import static io.netty.channel.ChannelOption.TCP_NODELAY;

public class DataCenter {
    // key port+traceID  某client 是否有该err traceId
    private static final Object o = new Object();
    // port channel
    public static volatile Channel ch1;
    public static volatile Channel ch2;
    public static int clientCount = 2;
    static int spansCount = 0;
    static int _8000 = 0;
    static int _8001 = 0;
    private static Map<String, Object> c1Map = new ConcurrentHashMap();
    private static Map<String, Object> c2Map = new ConcurrentHashMap();
    private static Map<String, List<Tuple3<String, Long, String>>> data;
    private static DataServer dataServer = new DataServer(DATA_CENTER_PORT);
    private static MpscLinkedQueue<Tuple3<String, Long, String>> queue;
    public static volatile boolean finished = false;

    public static void start() {
        data = new HashMap<>(1024 * 16);
        System.out.println("start server at:" + DATA_CENTER_PORT);
        queue = new MpscLinkedQueue();
        new Thread(() -> processData()).start();
        dataServer.start();
    }
    public static void processData() {
        System.out.println("start process");
        while (!finished) {
            if (queue.size() == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Tuple3 t;
            while ((t= queue.poll()) != null){
                Tuple3<String, Long, String> tuple3 = t;
                data.compute(tuple3.first, (k, v) -> {
                    List<Tuple3<String, Long, String>> vals = v;
                    if (v == null) vals = new ArrayList<>(64);
                    vals.add(tuple3);
                    return vals;
                });
            }
        }
        reportData();
    }

    public static void addData(byte[] body, int clientId) {
        Tuple3<String, Long, String> tuple3 = decodeTrace(body);
        board(tuple3.first, clientId);
        queue.offer(tuple3);
      /*  data.compute(tuple3.first, (k, v) -> {
            List<Tuple3<String, Long, String>> vals = v;
            if (v == null) vals = new ArrayList<>(50);
            vals.add(tuple3);
            return vals;
        });*/
    }

    public static void board(String traceId, int clientId) {
        // id存入error map 中
        if (clientId == 1) {
            c1Map.putIfAbsent(traceId, o);
            if (!c2Map.containsKey(traceId)) {
                c2Map.put(traceId, o);
                ch2.writeAndFlush(traceId);
                _8001++;
            }
        } else {
            c2Map.putIfAbsent(traceId, o);
            if (!c1Map.containsKey(traceId)) {
                c1Map.put(traceId, o);
                ch1.writeAndFlush(traceId);
                _8000++;
            }
        }
    }

    static Map<String, String> CHECKSUM_MAP = new HashMap<>(1024 * 16);

    public static void reportData() {
        long t = System.currentTimeMillis();
        StringBuffer buf = new StringBuffer();
        for (Map.Entry<String, List<Tuple3<String, Long, String>>> entry : data.entrySet()) {
            String traceId = entry.getKey();
            List<Tuple3<String, Long, String>> list = entry.getValue();
            if (list.size() == 1) {
                continue;
            }
            buf.setLength(0);
            list.sort((e1, e2) -> (int) (e1.second - e2.second));
            for (Tuple3 tuple3 : list) {
                buf.append(tuple3.third);
            }
            CHECKSUM_MAP.put(traceId, MD5(buf.toString()));
            spansCount += list.size();
        }
        try {
            String result = JSON.toJSONString(CHECKSUM_MAP);
            send("result=" + result);
            System.out.println("finished total error count:" + data.size() + " span size:" + spansCount);
            System.out.println("8000:" + _8000 + " 8001:" + _8001 + " cost:" + (System.currentTimeMillis() - t));
            //checkData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkData() {
        Map map = new HashMap();
        for(Map.Entry<String,List<Tuple3<String, Long, String>>> e: data.entrySet()) {
            String key = e.getKey();
            List<Tuple3<String, Long, String>> v = e.getValue();
            List<String> list = new ArrayList<>();
            v.forEach(ee -> list.add(ee.third));
            map.put(key,list);
        }
        FakeEvaluation.checkData(map);
    }

    public static void send(String str) throws Exception {
        conn(BootStarter.port);
        con.getOutputStream().write(str.getBytes());
        con.getOutputStream().flush();
        int code = con.getResponseCode();
        if (code != 200) {
            System.out.println("http send error");
        }
    }

    static HttpURLConnection con;

    public static void conn(int port) {
        String u = "http://localhost:" + port + "/api/finished";
        try {
            URL url = new URL(u);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(1000 * 60);
            con.setDoOutput(true);
            con.setDoInput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class DataServer {
    private int port;
    private ServerBootstrap b;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public DataServer(int port) {
        this.port = port;
    }

    public void start() {
        // 创建EventLoopGroup
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(2);
        try {
            b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .childOption(TCP_NODELAY, NO_DELAY)
                    // 设置并绑定服务端Channel
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline ph = ch.pipeline();
                            ph.addLast(new ServerEncoder());
                            ph.addLast(new ServerDecoder());
                            ph.addLast(new ServerHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture future = b.bind(port).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}


class ServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Object lock = new Object();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object o) {
        if (o instanceof byte[]) { // data
            DataCenter.addData((byte[]) o, (int) ctx.channel().attr(Constants.key).get());
        } else if (o instanceof String) { //client id || f
            String str = (String) o;
            if ("f".equals(str)) {
                System.out.println("Client " + ctx.channel().attr(Constants.key).get() + " finished at:" + System.currentTimeMillis() );
                DataCenter.clientCount--;
                if (DataCenter.clientCount == 0) {
                    //DataCenter.finished = true;
                    //DataCenter.reportData();
                    //DataCenter.reportData();
                    DataCenter.finished = true;
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        synchronized (lock) {
            if (DataCenter.ch1 == null) {
                DataCenter.ch1 = ctx.channel();
                ctx.channel().attr(Constants.key).getAndSet(1);
            } else {
                DataCenter.ch2 = ctx.channel();
                ctx.channel().attr(Constants.key).getAndSet(2);
            }
        }
        System.out.println("set attr " + ctx.channel().attr(Constants.key).get());
        System.out.println("Server address:" + ctx.channel().localAddress() + " client:" + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}

