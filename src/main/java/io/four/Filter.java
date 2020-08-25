package io.four;

import io.four.utils.Bytes;
import io.four.utils.HeapCache;
import io.four.utils.NewTraceId;
import org.jctools.queues.MpscLinkedQueue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.four.Collector.THREAD_COUNT;
import static io.four.Constants.*;
import static io.four.DataReport.flush;
import static io.four.DataReport.writeData;

/**
 * 数据过滤处理
 */
public class Filter {
    private static final Object object = new Object();
    public static final Map<NewTraceId, Object> errors = new ConcurrentHashMap<>(1024 * 16);
    private static final HeapCache[] caches = new HeapCache[THREAD_COUNT];
    private final HeapCache cache;
    private static int n = 0;
    public static MpscLinkedQueue<byte[]> queue = new MpscLinkedQueue();
    public static Thread sendDataThread = new Thread(() -> dataJob());

    public Filter(int size, int mapSize) {
        //printMem();
        cache = new HeapCache(size, mapSize);
        caches[n] = cache;
        n++;
    }

    public void writeCache(byte[] b, int start, int end) {
        int index = Bytes.indexOf(b,spilt, start, end);
        if(index < 0) {
            return;
        }
        NewTraceId id = new NewTraceId(Arrays.copyOfRange(b, start, index));
        if (errors.get(id) != null) {
            //writeData(Arrays.copyOfRange(b, start, end));
            queue.offer(Arrays.copyOfRange(b, start, end));
        } else if (errorEq(b,start,end)) {
            //System.out.println(hashcode);
            errors.put(id, object);
            //sendData(Arrays.copyOfRange(b, start, end));
            queue.offer(Arrays.copyOfRange(b, start, end));
            onError(id);
        } else {
            cache.write(b, start, end, id);
        }
    }

    public static final void addError(byte[] b) {
        NewTraceId id = new NewTraceId(b);
        if (!errors.containsKey(id)) {
            errors.put(id, object);
            onError(id);
        }
    }

    private static void onError(NewTraceId traceId) {
        for (HeapCache cache : caches) {
            List<byte[]> l = cache.get(traceId);
            if (l != null && l.size() > 0) {
                l.forEach(e -> queue.offer(e));
            }
        }
    }

    public static void dataJob() {
        while (!Collector.finished || queue.size() > 0) {
            byte[] b;
            int i = 0;
            while ((b = queue.poll()) != null) {
                writeData(b);
                i++;
            }
            if (i > 0) {
                flush();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
