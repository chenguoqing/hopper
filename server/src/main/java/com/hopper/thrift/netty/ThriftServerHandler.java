package com.hopper.thrift.netty;

import com.hopper.thrift.ChannelBound;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * Handler for Thrift RPC processors
 * <p/>
 * Requires properly decoded ChannelBuffers of raw Thrift data as input
 * Will use normal Netty ChannelBuffers for performance.
 * If an HttpRequest message is found, it will automagically extract the
 * content and use that as input for protocol decoding.
 * <p/>
 * For optimal performance, please tweak the default response size by
 * setting {@link #setResponseSize(int)}
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public class ThriftServerHandler extends SimpleChannelUpstreamHandler {
    private TProcessor processor;
    private TProtocolFactory protocolFactory;
    private int responseSize = 1024;

    /**
     * Creates a Thrift processor handler with the default binary protocol
     *
     * @param processor Processor to handle incoming calls
     */
    public ThriftServerHandler(TProcessor processor) {
        this.processor = processor;
        this.protocolFactory = new TBinaryProtocol.Factory();
    }

    /**
     * Creates a Thrift processor handler
     *
     * @param processor       Processor to handle incoming calls
     * @param protocolFactory Protocol factory to use when encoding/decoding incoming calls.
     */
    public ThriftServerHandler(TProcessor processor, TProtocolFactory protocolFactory) {
        this.processor = processor;
        this.protocolFactory = protocolFactory;
    }

    @Override
    public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
        super.childChannelClosed(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        // bound channel to thread local
        ChannelBound.bound(ctx.getChannel());

        try {
            ChannelBuffer input = (ChannelBuffer) e.getMessage();

            ChannelBuffer output = ChannelBuffers.dynamicBuffer(responseSize);
            TProtocol protocol = protocolFactory.getProtocol(new TNettyServerTransport(input, output));

            processor.process(protocol, protocol);

            e.getChannel().write(output);
        } finally {
            ChannelBound.unbound();
        }
    }

    /**
     * @return Default size for response buffer
     */
    public int getResponseSize() {
        return responseSize;
    }

    /**
     * Sets the default size for response buffer
     *
     * @param responseSize New default size
     */
    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }
}
