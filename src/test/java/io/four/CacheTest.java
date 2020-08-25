package io.four;

import io.four.utils.ByteArrayCache;

public class CacheTest {

    public static void main(String[] args) {
        ByteArrayCache cache = new ByteArrayCache(1024*1024*256,0);
        System.out.println(System.currentTimeMillis());
        for(int i=0;i<10000000;i++) {
            cache.putCache(new byte[300],300);
        }
        System.out.println(System.currentTimeMillis());
        System.out.println(cache.getWriteIndex());
    }
}
