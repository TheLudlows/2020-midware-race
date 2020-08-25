package io.four;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static io.four.BootStarter.RecError;
import static io.four.DataReport.sendFinished;
import static io.four.Filter.*;


/**
 * 通过http接口拉取数据
 */
public class Collector {
    public static volatile boolean start = false;
    public static final int THREAD_COUNT = 1;
    public static DownloadThread[] ts;
    public static long startTime;
    public static volatile boolean finished = false;

    public static void init() {
        ts = new DownloadThread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            Filter f = new Filter(1024 * 1024 * (1024), 1024 * (48));
            ts[i] = new DownloadThread(f);
            //ts[i].setPriority(Thread.MAX_PRIORITY);
        }
        sendDataThread.start();
    }

    public static void warm() {
        for(int i=0;i<1000000;i++) {
            byte[] s = ("aaaaa|aaaaaaaa"+i).getBytes();
            for(int j=0;j<THREAD_COUNT;j++) {
                ts[j].f.writeCache(s, 0, s.length);
            }
        }
        System.gc();
    }

    public static void sync() {
        try {
            while (!start) {
                Thread.sleep(20);
            }
            for (Thread t : ts) {
                t.join();
            }
            Thread.sleep(1000);
            finished = true;
            sendDataThread.join();
            sendFinished();
            System.out.println("total rec error :" + RecError.get());
            System.out.println("pull data cost:" + (System.currentTimeMillis() - startTime));
            System.out.println("error size:" + errors.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void download(String path) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == 200) {
                long size = conn.getContentLengthLong();
                long blockSize = size / THREAD_COUNT;
                //开启指定数目的线程同时下载
                for (int i = 0; i < THREAD_COUNT; i++) {
                    long startIndex = i * blockSize;
                    long endIndex = (i + 1) * blockSize - 1;
                    if (i == blockSize - 1) {
                        endIndex = size - 1;
                    }
                    ts[i].setUrlAndStart(startIndex, endIndex, url);
                }
            } else {
                System.out.println("connect failed！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class DownloadThread extends Thread {
        private long start;
        private long end;
        private URL url;
        private Filter f;

        public DownloadThread(Filter f) {
            this.f = f;
        }

        public void setUrlAndStart(long start, long end, URL url) {
            this.start = start;
            this.end = end;
            this.url = url;
            this.start();
        }

        public void run() {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //设置请求数据的范围
                System.out.println("start:" + start + " end:" + end);
                conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
                if (conn.getResponseCode() == 206) {
                    InputStream inStream = conn.getInputStream();
                    byte[] bytes = new byte[1024 * 256];
                    int n;
                    byte[] tmp = new byte[1024*8];
                    int tempLen = 0;
                    int lineEnd;
                    int index;
                    while ((n = inStream.read(bytes)) > 0) {
                        index= 0;
                        while (true) {
                            lineEnd = -1;
                            for (int i = index; i < n; i++) {
                                if (bytes[i] == Constants.r) {
                                    lineEnd = i;
                                    break;
                                }
                            }
                            if (lineEnd == -1) {
                                if (index != n) {
                                    System.arraycopy(bytes, index, tmp, 0, n - index);
                                    tempLen = n - index;
                                }
                                break;
                            }
                            lineEnd++;
                            if (tempLen != 0) {
                                System.arraycopy(bytes, index, tmp, tempLen, lineEnd - index);
                                int end = tempLen + lineEnd - index;
                                f.writeCache(tmp, 0, end);
                                tempLen = 0;
                            } else {
                                f.writeCache(bytes, index, lineEnd);
                            }
                            index = lineEnd;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void pullData(int port) {
        //port = 80;
        String curr = System.getProperty("server.port", "8080");
        String url;
        if ("8000".equals(curr)) {
            url = "http://localhost:" + port + "/trace1.data";
        } else {
            url = "http://localhost:" + port + "/trace2.data";
        }
        startTime = System.currentTimeMillis();
        download(url);
        start = true;
    }

}

