package io.four;

import io.four.utils.Tuple2;
import io.four.utils.Tuple3;
import io.netty.buffer.ByteBuf;

import static io.four.Constants.*;

public class Codec {
    public static void encodeBytes(byte[] body, ByteBuf buf) {
        int index = buf.writerIndex();
        buf.writerIndex(index + 2);
        buf.writeByte(MESSAGE_TYPE_REPORT_DATA);
        buf.writeBytes(body);
        buf.setShort(index, buf.writerIndex() - index - 2);
    }
    public static void encodeBytes(String s, ByteBuf buf) {
        int index = buf.writerIndex();
        buf.writerIndex(index + 2);
        buf.writeByte(MESSAGE_TYPE_REPORT_DATA);
        buf.writeBytes(s.getBytes());
        buf.setShort(index, buf.writerIndex() - index - 2);
    }

    public static byte[] decodeBytes(ByteBuf buf) {
        short len = buf.readShort();
        byte[] body = new byte[len - 1];
        buf.readerIndex(buf.readerIndex() + 1);
        buf.readBytes(body);
        return body;
    }

    public static void encodePortTraceId(Tuple2<Integer, String> tuple2, ByteBuf buf) {
        int index = buf.writerIndex();
        buf.writerIndex(index + 2);
        buf.writeByte(MESSAGE_TYPE_REPORT_ERROR);
        buf.writeInt(tuple2.first);
        buf.writeBytes(tuple2.second.getBytes());
        buf.setShort(index, buf.writerIndex() - index - 2);
    }

    public static Tuple2<Integer, String> decodePortTraceId(ByteBuf buf) {
        short len = buf.readShort();
        buf.readerIndex(buf.readerIndex() + 1);
        int port = buf.readInt();
        byte[] bytes = new byte[len - 5];
        buf.readBytes(bytes);
        return new Tuple2(port, new String(bytes));
    }

    public static void encodeTraceId(String traceId, ByteBuf buf) {
        int index = buf.writerIndex();
        buf.writerIndex(index + 2);
        buf.writeByte(MESSAGE_TYPE_BOARD);
        buf.writeBytes(traceId.getBytes());
        buf.setShort(index, buf.writerIndex() - index - 2);
    }

    public static String decodeTraceId(ByteBuf buf) {
        short len = buf.readShort();
        buf.readerIndex(buf.readerIndex() + 1);
        byte[] bytes = new byte[len - 1];
        buf.readBytes(bytes);
        return new String(bytes);
    }

    public static void encodeClientId(String traceId, ByteBuf buf) {
        int index = buf.writerIndex();
        buf.writerIndex(index + 2);
        buf.writeByte(MESSAGE_TYPE_CLIENT_ID);
        buf.writeBytes(traceId.getBytes());
        buf.setShort(index, buf.writerIndex() - index - 2);
    }

    public static String decodeClientId(ByteBuf buf) {
        short len = buf.readShort();
        buf.readerIndex(buf.readerIndex() + 1);
        byte[] bytes = new byte[len - 1];
        buf.readBytes(bytes);
        return new String(bytes);
    }

    public static Tuple3<String, Long, String> decodeTrace(byte[] body) {
        String str = new String(body);
        int first = str.indexOf('|');
        int second = str.indexOf('|', first+1);
        return new Tuple3<>(str.substring(0,first), Long.parseLong(str.substring(first+1,second)), str);
    }

}
