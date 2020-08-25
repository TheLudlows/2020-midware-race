package io.four.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.four.utils.Longutils.high;
import static io.four.utils.Longutils.low;

public class ByteArrayCache<K> {
    private int cacheSize;
    private Map<K, List<Long>> map;
    private byte[] cache;
    private int writeIndex;
    public ByteArrayCache(int cacheSize, int mapSize){
        this.cacheSize = cacheSize;
        this.cache = new byte[cacheSize];
        this.map = new LinkedHashMap<>(mapSize);
        this.writeIndex = 0;
    }

    public void putCache(byte[] bytes, int start,int end) {
        if(writeIndex + end - start > cacheSize) {
            writeIndex = 0;
        }
        System.arraycopy(bytes,start,cache,writeIndex,end-start);
        writeIndex += end-start;
    }

    public void putCache(byte[] bytes, int len) {
        if(writeIndex + len > cacheSize) {
            writeIndex = 0;
        }
        System.arraycopy(bytes,0,cache,writeIndex,len);
        writeIndex+=len;
    }

    public int getWriteIndex() {
        return writeIndex;
    }

    public List<byte[]> get(K k) {
        List<Long> list = map.get(k);
        if (list != null && list.size() > 0) {
            List r = new ArrayList();
            for (long  l : list) {
                byte[] b = new byte[low(l)];
                System.arraycopy(cache,high(l),b,0,low(l));
                r.add(b);
            }
            return r;
        } else {
            return null;
        }
    }

}
