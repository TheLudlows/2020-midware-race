package io.four.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.four.utils.Longutils.*;

public class HeapCache {
    private int size;
    private int maxSize;
    private Map<NewTraceId, List<Long>> index;
    private ByteBuf buffer;
    private NewTraceId[] ids;
    private List[] lists;

    private int i;

    public HeapCache(int size, int maxSize) {
        this.size = size;
        this.maxSize = maxSize;
        buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(size);
        // 先用这个试试
        index = new HashMap((int) (maxSize/0.75));
        ids = new NewTraceId[this.maxSize];
        lists = new FastList[this.maxSize];
        for (int i = 0; i < this.maxSize; i++) {
            lists[i] = new FastList(64);
        }
    }

    // write a line bytes
    public void write(byte[] body, int start, int end, NewTraceId id) {
        int len = end - start;
        if (len + buffer.writerIndex() > size) {
            buffer.writerIndex(0);
        }
        int wIndex = buffer.writerIndex();
        buffer.writeBytes(body, start, len);
        putIndex(id, wIndex, len);
    }

    private void putIndex(NewTraceId id, int start, int len) {
        List<Long> list = index.get(id);
        if (list == null) {
            NewTraceId oldId = ids[i % maxSize];
            if (oldId != null) {
                list = index.remove(oldId);
                list.clear();
            } else {
                list = lists[i % maxSize];
            }
            ids[i % maxSize] = id;
            i++;
            index.put(id, list);
        }
        list.add(combine(start, len));
    }

    public List<byte[]> get(NewTraceId id) {
        List<Long> list = index.get(id);
        if (list != null && list.size() > 0) {
            List r = new ArrayList();
            int size = list.size();
            for (int i=0;i<size;i++) {
                long l = list.get(i);
                int len = low(l);
                int start = high(l);
                byte[] b = new byte[len];
                buffer.getBytes(start, b);
                r.add(b);
            }
            return r;
        } else {
            return null;
        }
    }
}
