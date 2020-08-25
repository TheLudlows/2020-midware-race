package io.four;

import java.util.Arrays;

public class CopyTest {

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        byte[] b = new byte[100];
        for(int i=0;i<10000;i++) {
            Arrays.copyOf(b,b.length);
        }
        System.out.println(System.currentTimeMillis());


        System.out.println(System.currentTimeMillis());
        byte[] bs = new byte[10000];
        for(int i=0;i<100;i++) {
            Arrays.copyOf(bs,bs.length);
        }
        System.out.println(System.currentTimeMillis());

    }
}
