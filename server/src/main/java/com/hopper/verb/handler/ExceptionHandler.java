package com.hopper.verb.handler;

import com.hopper.session.MessageDecodeException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The handler processes all exceptions which occurs on channel reading/writing.
 *
 * @author chenguoqing
 */
public class ExceptionHandler extends SimpleChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Throwable t = e.getCause();

        if (t instanceof AccessDeniedException) {
            e.getChannel().close();
            logger.warn("Reject the illegal access from " + ((AccessDeniedException) e).address);
        } else if (t instanceof MessageDecodeException) {

        }
    }

}
