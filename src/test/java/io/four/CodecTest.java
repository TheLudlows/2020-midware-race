package io.four;

import io.four.utils.Tuple2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import static io.four.Codec.*;

public class CodecTest {

    public static void main(String[] args) {
        String traceId = "add23213dsads";
        int port = 8080;
        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.directBuffer(128);
        encodePortTraceId(new Tuple2<>(port, traceId), buf);
        System.out.println(buf.toString());
        System.out.println(decodePortTraceId(buf));
        buf.clear();
        encodeTraceId(traceId, buf);
        System.out.println(decodeTraceId(buf));

    }
}
