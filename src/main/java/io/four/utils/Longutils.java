package io.four.utils;

public class Longutils {

    public static final long combine(int high, int low) {
        return ((long) low & 0xFFFFFFFFl) | (((long)high << 32) & 0xFFFFFFFF00000000l);
    }

    public static final int low(long l) {
        return (int) (0xFFFFFFFFl & l);
    }

    public static final int high(long l) {
        return (int) ((0xFFFFFFFF00000000l & l) >> 32);
    }

    public static void main(String[] args) {
        long c = combine(10,20);
        System.out.println(high(c));
        System.out.println(low(c));
    }

    public static long parseLong(String s, int radix)
            throws NumberFormatException{
        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);

                if (digit < 0) {
                    throw forInputString(s);
                }
                if (result < multmin) {
                    throw forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw forInputString(s);
                }
                result -= digit;
            }
        } else {
            throw forInputString(s);
        }
        return negative ? result : -result;
    }
    static NumberFormatException forInputString(String s) {
        return new NumberFormatException("For input string: \"" + s + "\"");
    }

}
