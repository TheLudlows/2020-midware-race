package io.four.codec;

import io.four.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ServerEncoder extends MessageToByteEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String o, ByteBuf byteBuf) throws Exception {
        Codec.encodeTraceId(o, byteBuf);
    }
}
