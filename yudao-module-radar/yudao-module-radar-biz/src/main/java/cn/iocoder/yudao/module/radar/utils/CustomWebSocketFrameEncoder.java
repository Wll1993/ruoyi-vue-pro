package cn.iocoder.yudao.module.radar.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.websocketx.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Created by l09655 on 2022/8/9.
 */
public class CustomWebSocketFrameEncoder extends MessageToMessageEncoder<WebSocketFrame> implements WebSocketFrameEncoder {

    private static final byte OPCODE_CONT = 0x0;
    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;
    private static final byte OPCODE_CLOSE = 0x8;
    private static final byte OPCODE_PING = 0x9;
    private static final byte OPCODE_PONG = 0xA;

    /**
     * The size threshold for gathering writes. Non-Masked messages bigger than this size will be be sent fragmented as
     * a header and a content ByteBuf whereas messages smaller than the size will be merged into a single buffer and
     * sent at once.<br>
     * Masked messages will always be sent at once.
     */
    private static final int GATHERING_WRITE_THRESHOLD = 1024;

    private final boolean maskPayload;

    /**
     * Constructor
     *
     * @param maskPayload Web socket clients must set this to true to mask payload. Server implementations must set this to
     *                    false.
     */
    public CustomWebSocketFrameEncoder(boolean maskPayload) {
        this.maskPayload = maskPayload;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {

        final ByteBuf data = msg.content();
        byte[] mask;
        boolean ifRequest = true;

        byte opcode;
        if (msg instanceof TextWebSocketFrame) {
            opcode = OPCODE_TEXT;
        } else if (msg instanceof PingWebSocketFrame) {
            opcode = OPCODE_PING;
        } else if (msg instanceof PongWebSocketFrame) {
            opcode = OPCODE_PONG;
        } else if (msg instanceof CloseWebSocketFrame) {
            opcode = OPCODE_CLOSE;
        } else if (msg instanceof BinaryWebSocketFrame) {
            opcode = OPCODE_BINARY;
        } else if (msg instanceof ContinuationWebSocketFrame) {
            opcode = OPCODE_CONT;
        } else {
            throw new UnsupportedOperationException("Cannot encode frame of type: " + msg.getClass().getName());
        }

        int length = data.readableBytes();

        int b0 = 0;
        if (msg.isFinalFragment()) {
            b0 |= 1 << 7;
        }
        b0 |= msg.rsv() % 8 << 4;
        b0 |= opcode % 128;

        if (opcode == OPCODE_PING && length > 125) {
            throw new TooLongFrameException("invalid payload for PING (payload length must be <= 125, was "
                    + length);
        }

        String requestStr = ((TextWebSocketFrame) msg).text();
        /*if (requestStr.contains("RequestURL")) {
            //发送请求，需要加掩码
            ifRequest = maskPayload;
        } else if (requestStr.contains("ResponseURL")) {
            //响应请求，不需要加掩码
            ifRequest = false;
        }*/
        /**
         * Modified by l09655 on 2022/12/01.
         * 规则修改为客户端发送的带掩码，服务端发送的不带掩码，demo作为服务端所有请求和响应均不带掩码
         */
        ifRequest = false;


        boolean release = true;
        ByteBuf buf = null;
        try {
            int maskLength = ifRequest ? 4 : 0;
            if (length <= 125) {
                int size = 2 + maskLength;
                if (ifRequest || length <= GATHERING_WRITE_THRESHOLD) {
                    size += length;
                }
                buf = ctx.alloc().buffer(size);
                buf.writeByte(b0);
                byte b = (byte) (ifRequest ? 0x80 | (byte) length : (byte) length);
                buf.writeByte(b);
            } else if (length <= 0xFFFF) {
                int size = 4 + maskLength;
                if (ifRequest || length <= GATHERING_WRITE_THRESHOLD) {
                    size += length;
                }
                buf = ctx.alloc().buffer(size);
                buf.writeByte(b0);
                buf.writeByte(ifRequest ? 0xFE : 126);
                buf.writeByte(length >>> 8 & 0xFF);
                buf.writeByte(length & 0xFF);
            } else {
                int size = 10 + maskLength;
                if (ifRequest || length <= GATHERING_WRITE_THRESHOLD) {
                    size += length;
                }
                buf = ctx.alloc().buffer(size);
                buf.writeByte(b0);
                buf.writeByte(ifRequest ? 0xFF : 127);
                buf.writeLong(length);
            }

            // Write payload
            if (ifRequest) {
                int random = (int) (Math.random() * Integer.MAX_VALUE);
                mask = ByteBuffer.allocate(4).putInt(random).array();
                buf.writeBytes(mask);

                ByteOrder srcOrder = data.order();
                ByteOrder dstOrder = buf.order();

                int counter = 0;
                int i = data.readerIndex();
                int end = data.writerIndex();

                if (srcOrder == dstOrder) {
                    // Use the optimized path only when byte orders match
                    // Remark: & 0xFF is necessary because Java will do signed expansion from
                    // byte to int which we don't want.
                    int intMask = ((mask[0] & 0xFF) << 24)
                            | ((mask[1] & 0xFF) << 16)
                            | ((mask[2] & 0xFF) << 8)
                            | (mask[3] & 0xFF);

                    // If the byte order of our buffers it little endian we have to bring our mask
                    // into the same format, because getInt() and writeInt() will use a reversed byte order
                    if (srcOrder == ByteOrder.LITTLE_ENDIAN) {
                        intMask = Integer.reverseBytes(intMask);
                    }

                    for (; i + 3 < end; i += 4) {
                        int intData = data.getInt(i);
                        buf.writeInt(intData ^ intMask);
                    }
                }
                for (; i < end; i++) {
                    byte byteData = data.getByte(i);
                    buf.writeByte(byteData ^ mask[counter++ % 4]);
                }
            } else {
                if (buf.writableBytes() >= data.readableBytes()) {
                    // merge buffers as this is cheaper then a gathering write if the payload is small enough
                    buf.writeBytes(data);
                }
            }
            release = false;
            ctx.writeAndFlush(buf);
        } finally {
            if (release && buf != null) {
                buf.release();
            }
        }
    }
}