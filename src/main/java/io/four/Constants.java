package io.four;

import io.four.utils.Bytes;
import io.netty.util.AttributeKey;

public interface Constants {
    int DATA_CENTER_PORT = 8003;
    int DATA_CENTER_HTTP_PORT = 8002;
    int COLLECTOR_PORT1 = 8000;
    int COLLECTOR_PORT2 = 8001;

    String OK = "SUC";
    String READY_PATH = "/ready";
    String SET_PARAM_PATH = "/setParameter";

    byte MESSAGE_TYPE_REPORT_DATA = 0;
    byte MESSAGE_TYPE_BOARD = 1;
    byte MESSAGE_TYPE_REPORT_ERROR = 2;
    byte MESSAGE_TYPE_CLIENT_ID = 3;

    AttributeKey key = AttributeKey.valueOf("client name");

    boolean NO_DELAY = true;

    byte r = '\n';
    byte[] e1 = "error=1".getBytes();
    byte[] e2 = "s_code=".getBytes();
    byte spilt = '|';

    static boolean error(String s) {
        if (s.contains("error=1")) {
            return true;
        } else if (s.contains("_code=")) {
            return !s.contains("_code=200");
        }
        return false;
    }

    static boolean error(byte[] bytes, int start, int end) {
        for (int i = end - 8; i > start; i--) {
            byte b = bytes[i];
            if (b == e1[0]) {
                if (bytes[i + 1] == e1[1]
                        && bytes[i + 2] == e1[2] && bytes[i + 3] == e1[3]
                        && bytes[i + 4] == e1[4] && bytes[i + 5] == e1[5]
                        && bytes[i + 6] == e1[6]) {
                    return true;
                } else {
                    i -= 4;
                }
            } else if (b == e2[0]) {
                if (bytes[i + 1] == e2[1]
                        && bytes[i + 2] == e2[2] && bytes[i + 3] == e2[3]
                        && bytes[i + 4] == e2[4] && bytes[i + 5] == e2[5]
                        && bytes[i + 6] == e2[6]) {
                    return !(bytes[i + 7] == '2');
                } else {
                    i -= 7;
                }
            } else if (b == spilt) {
                return false;
            }
        }
        return false;
    }

    static boolean errorEq(byte[] b, int start, int end) {
        for (int i = end - 1; i > start + 6; i--) {
            if (b[i] == '=') {
                if (b[i - 1] == 'r' && b[i - 2] == 'o' && b[i - 3] == 'r' && b[i - 4] == 'r' && b[i - 5] == 'e' && b[i + 1] == '1') {
                    return true;
                } else if (b[i - 1] == 'e' && b[i - 2] == 'd' && b[i - 3] == 'o'
                        && b[i - 4] == 'c' && b[i - 5] == '_' && b[i - 6] == 's') {
                    if (b[i + 1] == '2' && b[i + 2] == '0' && b[i + 3] == '0') {
                        continue;
                    } else {
                        return true;
                    }
                }
            } else if (b[i] == '|') {
                return false;
            }
        }
        return false;
    }

    static boolean errorSkip(byte[] b, int start, int end) {
        int last = end - 1;
        int i = last;

        while (i > start) {
            if (b[i] == '&') {
                int tagLen = last - i;
                if (tagLen == 8) {
                    if (b[i + 1] == 'e') {
                        return true;
                    }
                } else if (tagLen == 21) {
                    if (b[i + 1] == 'h') {
                        return (!(b[i + 18] == '2'));
                    }
                }
                last = i;
            } else if (b[i] == '|') {
                int tagLen = last - i;
                if (tagLen == 8) {
                    if (b[i + 1] == 'e') {
                        return true;
                    }
                } else if (tagLen == 21) {
                    return b[i + 1] == 'h' && !(b[i + 18] == '2');

                }
            }
            i--;
        }
        return false;
    }

    static boolean errorSlow(byte[] b, int start, int end) {
        int n;
        if (Bytes.indexOf(b, start, end - start, e1, 0, e1.length, 0) > 0) {
            return true;
        } else if ((n = Bytes.indexOf(b, start, end - start, e2, 0, e2.length, 0)) > 0) {
            return !(b[start + n + 8] == '2' && b[start + n + 9] == '0' && b[start + n + 10] == '0');
        }
        return false;
    }

    static boolean errorLast(byte[] b, int start, int end) {
        int i;
        if (Bytes.lastIndexOf(b, start, end - start, e1, end) > -1) {
            return true;
        } else if ((i = Bytes.lastIndexOf(b, start, end - start, e2, end)) > -1) {
            //return !(b[i + 6] == '2');
            return !(b[start + i + 7] == '2' && b[start + i + 8] == '0' && b[start + i + 9] == '0');
        }
        return false;
    }
}
