package io.four;

import org.jctools.queues.SpscArrayQueue;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static io.four.Constants.*;


public class PullDataTest {

    @Test
    public void testQueue() {
        SpscArrayQueue<Integer> queue = new SpscArrayQueue<>(1);
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        queue.offer(4);
        queue.offer(5);
        System.out.println(queue.offer(6));
        System.out.println(Arrays.toString(queue.toArray()));
        System.out.println(queue.size());
    }


    /**
     * 1.3s ~ 1.4s 获取完所300m数据
     */
    public static void main(String[] args) throws InterruptedException {
        String p1 = "http://localhost:80/trace2.data";
        parallelPull(p1, 1);
        //singlePull(p1);
        //single(p1);
    }

    public static void singlePull(String url) {
        long start = System.currentTimeMillis();
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection httpConnection = (HttpURLConnection) httpUrl.openConnection(Proxy.NO_PROXY);
            InputStream input = httpConnection.getInputStream();
            byte[] buf = new byte[1024 * 16];
            int n;
            byte[] lastBytes = null;
            int spans = 0;
            int error = 0;
            while ((n = input.read(buf)) > 0) {
                int last = 0;
                byte[] body = Arrays.copyOf(buf, n);
                for (int i = 0; i < n; i++) {
                    if (body[i] == '\n') {
                        byte[] b = Arrays.copyOfRange(body, last, i);
                        if (i == 0 && lastBytes != null) {
                            b = addTwo(lastBytes, b);
                        }
                        spans++;
                        last = i + 1;
                    }
                }
                if (last != body.length) {
                    lastBytes = (Arrays.copyOfRange(body, last, body.length));
                } else if (last == 0) {
                    lastBytes = addTwo(lastBytes, body);
                }
            }
            //15159142
            System.out.println(spans);
            System.out.println(error);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }

    public static void single(String url) {
        long start = System.currentTimeMillis();
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection httpConnection = (HttpURLConnection) httpUrl.openConnection(Proxy.NO_PROXY);
            InputStream input = httpConnection.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(input));
            String line;
            HashSet set = new HashSet();
            int spans = 0;
            int error = 0;
            while ((line = bf.readLine()) != null) {
                spans++;
            }
            //
            System.out.println(spans);
            System.out.println(error);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }

    public static byte[] addTwo(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    public static void parallelPull(String url, int thread) {
        long t = System.currentTimeMillis();
        download(url, thread);
        System.out.println("pull data cost:" + (System.currentTimeMillis() - t));
    }

    public static void download(String path, int threads) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Thread[] ts = new Thread[threads];
            if (conn.getResponseCode() == 200) {
                long size = conn.getContentLengthLong();
                long blockSize = size / threads;
                //开启指定数目的线程同时下载
                for (int i = 1; i <= threads; i++) {
                    long startIndex = (i - 1) * blockSize;
                    long endIndex = i * blockSize - 1;
                    if (i == blockSize) {
                        endIndex = size - 1;
                    }
                    Thread t = new DT(startIndex, endIndex, url);
                    ts[i - 1] = t;
                    t.start();
                }
            } else {
                System.out.println("connect failed！");
            }
            for (Thread t : ts) {
                t.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class DT extends Thread {
        private long start;
        private long end;
        private URL url;

        public DT(long start, long end, URL url) {
            this.start = start;
            this.end = end;
            this.url = url;
        }

        public void run() {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //设置请求数据的范围
                conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
                if (conn.getResponseCode() == 206) {
                    InputStream inStream = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
                    String line;
                    HashSet set = new HashSet();
                    int count = 0;
                    int strcount = 0;
                    int min = Integer.MAX_VALUE;
                    int max = Integer.MIN_VALUE;
                    long maxId = Long.MIN_VALUE;
                    long minId = Long.MAX_VALUE;
                    while ((line = br.readLine()) != null) {
                        line = line+"sql=select*from * join ,exception=NullPointException, stack=com.adf.\n";
                        if (min > line.length()) {
                            min = line.length();
                        }
                        if (max < line.length()) {
                            max = line.length();
                        }
                        if(error(line) != errorEq(line.getBytes(), 0,line.length())){
                            System.out.println(line);
                        }

                    }
                    System.out.println("error count:" + count);
                    System.out.println("error count:" + strcount);
                    System.out.println("min:" + min);
                    System.out.println("max:" + max);
                    System.out.println("minId:" + minId);
                    System.out.println("maxId:" + maxId);
                    System.out.println(set.size());

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
