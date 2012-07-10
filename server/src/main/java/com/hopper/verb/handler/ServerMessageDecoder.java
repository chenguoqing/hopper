package com.hopper.verb.handler;

import com.hopper.session.Message;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class ServerMessageDecoder extends FrameDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

        // Read the message size and body
        if (buffer.readableBytes() < 4 || buffer.readableBytes() < 4 + buffer.getInt(0)) {
            return null;
        }

        // Deserialize the message instance
        Message message = new Message();

        message.deserialize(buffer);

        return message;
    }
}
