package com.hopper.avro.netty;

import com.hopper.GlobalConfiguration;
import com.hopper.avro.ClientService;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import org.apache.avro.ipc.NettyTransportCodec;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-30
 * Time: 下午6:16
 * To change this template use File | Settings | File Templates.
 */
public class AvroPiplelineFactory implements ChannelPipelineFactory {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        Responder responder = new SpecificResponder(ClientService.class, null);
        ChannelPipeline p = Channels.pipeline();
        p.addLast("frameDecoder", new NettyTransportCodec.NettyFrameDecoder());
        p.addLast("frameEncoder", new NettyTransportCodec.NettyFrameEncoder());
        p.addLast("handler", new AvroServerHandler(responder));
        return p;
    }
}
