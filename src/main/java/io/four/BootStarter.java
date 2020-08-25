package io.four;

import java.util.concurrent.atomic.AtomicInteger;

import static io.four.Constants.*;
import static java.lang.System.exit;

/**
 * 启动类
 */
public class BootStarter {
    public static final AtomicInteger RecError = new AtomicInteger();
    public static HttpServer httpServer;
    // set param 设置的port
    public static int port = 0;
    // 当前应用暴露的port
    public static int local_port = 0;

    public static void main(String[] args) {
        local_port = getPort();
        try {
            if (isDataCenter()) {
                httpServer = new HttpServer(getPort());
                httpServer.start();
                DataCenter.start();
            } else if (isCollector()) {
                try {
                    port = getPort();
                    DataReport.init(DATA_CENTER_PORT);
                    DataReport.start();
                    Collector.init();
                    httpServer = new HttpServer(port);
                    httpServer.start();
                    Collector.sync();
                    DataReport.sync();
                    //httpServer.close();
                    //exit(1);
                }catch (Exception e) {
                    DataReport.sendFinished();
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public static boolean isDataCenter() {
      return DATA_CENTER_HTTP_PORT == local_port;
    }

    public static boolean isCollector() {
        return COLLECTOR_PORT1 == local_port || COLLECTOR_PORT2 == local_port;
    }

    public static int getPort() {
        return Integer.parseInt(System.getProperty("server.port", "8080"));
    }
}
