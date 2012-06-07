package com.hopper.verb.handler;

import com.hopper.session.Message;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class ServerMessageDecoder extends FrameDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

        // Read the message size
        if (buffer.readableBytes() < 4) {
            return null;
        }

        int size = buffer.getInt(0);

        // Read the message body
        if (buffer.readableBytes() < 4 + size) {
            return null;
        }

        // skip the message size
        buffer.skipBytes(4);

        byte[] b = new byte[size];
        buffer.readBytes(b);

        // Deserialize the message instance
        Message message = new Message();

        message.deserialize(b);

        return message;
    }
}
