

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class DiscardServerHandler extends SimpleChannelHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		ChannelBuffer buf = (ChannelBuffer) e.getMessage();
//		while (buf.readable()) {
//			System.out.print((char) buf.readByte());
//		}
		ChannelFuture future = e.getChannel().write(e.getMessage());
		System.out.println(e.getMessage());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();

		Channel ch = e.getChannel();
		ch.close();
	}
}
