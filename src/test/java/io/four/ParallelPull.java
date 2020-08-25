package io.four;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelPull {

    public static void main(String[] args) throws Exception {
        long t = System.currentTimeMillis();
        String p = "http://localhost:9000/trace1.data";
        download(p, 2, 1024 * 1024 *32 );
        System.out.println(System.currentTimeMillis() - t);
        System.out.println(totalSpan);
    }

    private static final AtomicInteger current_id = new AtomicInteger();
    private static long block_count;

    public static void download(String path, int threads, int blockSize) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        Thread[] ts = new Thread[threads];
        if (conn.getResponseCode() == 200) {
            long size = conn.getContentLengthLong();
            System.out.println("size:" + size);
            block_count = size % blockSize == 0 ? size / blockSize : size / blockSize + 1;
            //开启指定数目的线程同时下载
            for (int i = 0; i < threads; i++) {
                ts[i] = new DownloadThread(i, blockSize, url);
                ts[i].start();
            }
        } else {
            System.out.println("下载失败！");
        }
        for (Thread t : ts) {
            t.join();
        }
    }

    static AtomicInteger totalSpan = new AtomicInteger();

    static class DownloadThread extends Thread {
        private int id;
        private long block;
        private URL url;
        private ArrayList<String> list;

        public DownloadThread(int id, int block, URL url) {
            this.id = id;
            this.block = block;
            this.url = url;
            list = new ArrayList<>();
        }

        public void run() {
            try {
                for (; id <= block_count; id += 2) {
                    int curId = current_id.get();
                    long start = id * block;
                    long end = (id + 1) * block - 1;
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    //设置请求数据的范围
                    conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
                    System.out.println(Thread.currentThread().getName() + "Range bytes=" + start + "-" + end);
                    if (conn.getResponseCode() == 206) {
                        InputStream inStream = conn.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));

                        String line;
                        while ((line = br.readLine()) != null) {
                            if (curId == id) {

                            } else {
                                list.add(line);
                            }
                            totalSpan.getAndIncrement();
                        }
                    }
                    while (id!=current_id.get()) {
                        Thread.sleep(1);
                    }
                    if (!list.isEmpty()) {
                        list.clear();
                        // 入队,并清空
                    }
                    current_id.getAndIncrement();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}



