package io.four;

import com.alibaba.fastjson.JSON;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 模拟评测程序
 */
public class FakeEvaluation {
    private final static OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(50L, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .build();

    public static void main(String[] args) throws IOException {
        Request request1 = new Request.Builder().url("http://localhost:8000/setParameter?port=9000").build();
        Request request2 = new Request.Builder().url("http://localhost:8001/setParameter?port=9000").build();
        Request request3 = new Request.Builder().url("http://localhost:8002/setParameter?port=9000").build();

        Response response1 = OK_HTTP_CLIENT.newCall(request1).execute();
        Response response2 = OK_HTTP_CLIENT.newCall(request2).execute();
        Response response3 = OK_HTTP_CLIENT.newCall(request3).execute();

        System.out.println(response1.body().toString());
        System.out.println(response2.body().toString());
        System.out.println(response3.body().toString());

    }

    public static void checkSum(Map<String, String> result) {
        try {
            RandomAccessFile file = new RandomAccessFile("/tmp/checkSum.data", "r");
            //RandomAccessFile file = new RandomAccessFile("D:/data/checkSum.data", "r");

            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = file.readLine()) != null) {
                buffer.append(line);
            }
            int right = 0;
            int wrong = 0;
            Map<String, String> map = (Map<String, String>) JSON.parse(buffer.toString());
            for (Map.Entry<String, String> e : result.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();
                String v = map.get(key);
                if (value.equals(v)) {
                    right++;
                } else {
                    wrong++;
                }
            }
            System.out.println("right:" + right + " wrong:" + wrong);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static Map buildRealData() {
        Map<String, List<String>> data = new HashMap<>();
        Map<String, Object> map = new HashMap();
        Object o = new Object();
        File[] files = new File[2];
        try {
            files[0] = new File("/Users/liuchao56/data/trace1.data");
            files[1] = new File("/Users/liuchao56/data/trace2.data");
            String line;
            for (File file : files) {
                FileReader reader = new FileReader(file);
                BufferedReader bf = new BufferedReader(reader);
                while ((line = bf.readLine()) != null) {
                    String[] cols = line.split("\\|");
                    if (cols == null || cols.length < 8) {
                        continue;
                    }
                    String traceID = cols[0];
                    String tags = cols[8];
                    if (tags.contains("error=1") || (tags.contains("http.status_code=") && tags.indexOf("http.status_code=200") < 0)) {
                        map.put(traceID, o);
                    }
                }
            }
            System.out.println(map.size());
            int totalSpan = 0;
            //全搜集
            for (File file : files) {
                FileReader reader = new FileReader(file);
                BufferedReader bf = new BufferedReader(reader);
                while ((line = bf.readLine()) != null) {
                    String[] cols = line.split("\\|");
                    if (cols == null || cols.length < 8) {
                        continue;
                    }
                    String traceID = cols[0];
                    if (map.get(traceID) != null) {
                        totalSpan++;
                        String finalLine = line;
                        data.compute(traceID, (k, v) -> {
                            List<String> values = v;
                            if (values == null) {
                                values = new ArrayList<>();
                            }
                            values.add(finalLine);
                            return values;
                        });
                    }
                }
            }
            System.out.println("total span:" + totalSpan);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
    public static void checkData( Map<String,List<String>> result) {
        Map<String,List<String>> map = buildRealData();
        int count = 0;
        for (Map.Entry<String,List<String>> e : map.entrySet()) {
            String key = e.getKey();
            List<String> value = e.getValue();
            List r = result.get(key);
            if(r.size() != value.size()) {
                System.out.println(value.size() + " " + r.size());
                count++;
            }

        }
        System.out.println("check error:" + count);
    }
}
