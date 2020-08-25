package io.four.utils;

import static io.four.Constants.spilt;

public class NewTraceId {
    private byte[] body;
    private int hash;

    public NewTraceId(byte[] body) {
        this.body = body;
    }

    public int hashCode() {
        int h = hash;
        if (h == 0 && body.length > 0) {
            h = 1;
            byte val[] = body;

            for (int i = 0; i < body.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }

    public static int hashCode(byte[] body, int start, int end) {
        int h = 0;
        for (int i = start; i < end; i++) {
            byte b = body[i];
            if (b == spilt) {
                return h;
            }
            h = 31 * h + body[i];
        }
        return h;
    }


    @Override
    public boolean equals(Object obj) {
        NewTraceId other = (NewTraceId) obj;
        if (body.length != other.body.length) {
            return false;
        }
        for (int i = 0; i < body.length; i++)
            if (body[i] != other.body[i])
                return false;
        return true;
    }

    @Override
    public String toString() {
        return new String(body);
    }
}
