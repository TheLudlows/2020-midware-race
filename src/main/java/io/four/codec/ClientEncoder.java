package io.four.codec;

import io.four.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;


/**
 * Server REPORT DATA: length + type + byte[] batch？
 * Client BOARD: length + type + traceId
 */
public class ClientEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if(o instanceof byte[]) {
            Codec.encodeBytes((byte[]) o, byteBuf);
        } else if(o instanceof List) {
            for(byte[] s : (List<byte[]>)o) {
                Codec.encodeBytes(s, byteBuf);
            }
        }else if (o instanceof String) {
            Codec.encodeClientId((String) o, byteBuf);
            System.out.println(o);
        }
    }

}
