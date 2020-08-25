package io.four;

import io.four.utils.Bytes;
import io.netty.util.internal.StringUtil;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.four.Filter.*;

public class StringTest {

    public static void main(String[] args) {
        String a = "aaaa|error=1";
        String b = "bbbb|bbbbbb";
        String c = "ccccc|cccchttpstatus_code=300";
        String d = "ccccccccc|http.status_code=200";

        System.out.println('a');
        List<String> list = new ArrayList<>();
        list.add("aaaa");
        list.add("zzzz");
        for(String s : list) {
            System.out.println(s);
        }
        System.out.println( a.substring(0,a.indexOf('|')));
        String s = "aa||bbb|cc";
        int first = s.indexOf('|');
        System.out.println(first);
        System.out.println(s.indexOf('|',first+1));
        System.out.println(2=='2');
        String sss =  "aaa\nbbbb";
        byte[] bytes =  sss.getBytes();
        System.out.println(bytes);
        System.out.println(bytes[3] == '\n');

        byte[] b1 = s.getBytes();
        System.out.println(Bytes.indexOf(b1, (byte) '|'));
        System.out.println(Arrays.toString(Arrays.copyOfRange(b1, 0,2)));
        String s1 = "aaaa|bbbb";
        byte[] b2 = s1.getBytes();
        System.out.println(new String(b2,0,Bytes.indexOf(b2, (byte) '|')));
    }



}
