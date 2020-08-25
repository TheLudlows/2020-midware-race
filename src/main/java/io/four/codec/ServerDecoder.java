package io.four.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static io.four.Codec.decodeBytes;
import static io.four.Codec.decodeClientId;
import static io.four.Constants.MESSAGE_TYPE_CLIENT_ID;
import static io.four.Constants.MESSAGE_TYPE_REPORT_DATA;


/**
 * Server REPORT ERROR: length  + type + from + traceId
 * Server REPORT DATA: length + type + TraceDomain batch？
 * Client BOARD: length + type + traceId
 */
public class ServerDecoder extends LengthFieldBasedFrameDecoder {

    public ServerDecoder() {
        super(Integer.MAX_VALUE, 0x0, 0x2);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buf = (ByteBuf) super.decode(ctx, in);
        if (buf != null) {
            try {
                byte type = buf.getByte(2);
                if (type == MESSAGE_TYPE_REPORT_DATA) {
                    return decodeBytes(buf);
                } else if (type == MESSAGE_TYPE_CLIENT_ID) {
                    return decodeClientId(buf);
                }
            } finally {
                buf.release();
            }
        }
        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
