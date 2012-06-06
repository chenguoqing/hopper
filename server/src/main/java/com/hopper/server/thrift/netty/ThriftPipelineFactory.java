package com.hopper.server.thrift.netty;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Pipeline factory for Thrift servers and clients
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public class ThriftPipelineFactory implements ChannelPipelineFactory {
    private ChannelHandler handler;
    private int maxFrameSize = 512 * 1024;

    public ThriftPipelineFactory(ChannelHandler handler) {
        this.handler = handler;
    }

    public ThriftPipelineFactory(ChannelHandler handler, int maxFrameSize) {
        this(handler);
        this.maxFrameSize = maxFrameSize;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(maxFrameSize, 0, 4, 0, 4));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast("thriftHandler", handler);
        return pipeline;
    }
}
