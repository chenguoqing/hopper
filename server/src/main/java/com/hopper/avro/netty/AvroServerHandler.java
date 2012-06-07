package com.hopper.avro.netty;

import com.hopper.thrift.ChannelBound;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.NettyTransportCodec;
import org.apache.avro.ipc.Responder;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * AvroServerHandler processes all request from avro-client, it transfers the netty request to avro request.
 */
public class AvroServerHandler extends SimpleChannelUpstreamHandler {
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AvroServerHandler.class);
    /**
     * Netty-based transceiver
     */
    private NettyTransceiver connection;
    /**
     * Responder instance for avro request processing
     */
    private final Responder responder;

    public AvroServerHandler(Responder responder) {
        this.responder = responder;
        try {
            this.connection = new NettyTransceiver(null);
        } catch (IOException e) {
            //nothing
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

        // Bound channel
        ChannelBound.bound(ctx.getChannel());
        try {
            NettyTransportCodec.NettyDataPack dataPack = (NettyTransportCodec.NettyDataPack) e.getMessage();
            List<ByteBuffer> req = dataPack.getDatas();
            List<ByteBuffer> res = responder.respond(req, connection);
            // response will be null for oneway messages.
            if (res != null) {
                dataPack.setDatas(res);
                e.getChannel().write(dataPack);
            }
        } catch (IOException ex) {
            LOG.warn("unexpect error");
        } finally {
            // unbound bound channel
            ChannelBound.unbound();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        LOG.warn("Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }

}
