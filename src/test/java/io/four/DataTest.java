package io.four;

public class DataTest {
    public static void main(String[] args) {
        for(int i=0;i<10;i++) {
            System.out.println((i+"").charAt(0)%16);
        }
        for(int i = 'a';i<='f';i++) {
            System.out.println(i%16);
        }
        for(int i = 'A';i<='F';i++) {
            System.out.println(i%16);
        }

    }
}
