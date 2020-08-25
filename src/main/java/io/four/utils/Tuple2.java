package io.four.utils;


public class Tuple2<A, B> {

    public final A first;

    public final B second;

    public Tuple2(A a, B b) {
        first = a;
        second = b;
    }

    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
