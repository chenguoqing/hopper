package com.hopper.server.handler;

import com.hopper.GlobalConfiguration;
import com.hopper.server.Endpoint;
import com.hopper.server.Verb;
import com.hopper.server.thrift.ChannelBound;
import com.hopper.session.*;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class ServerMessageHandler extends SimpleChannelHandler {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);

    /**
     * The unique {@link SessionManager} instance
     */
    private static final GlobalConfiguration config = GlobalConfiguration.getInstance();

    @Override
    public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
        final Channel channel = ctx.getChannel();
        SocketAddress address = channel.getRemoteAddress();
        Endpoint endpoint = config.getEndpoint(address);

        // Only the allowed endpoints can connect to this port
        if (endpoint == null) {
            channel.close();
            logger.error("Reject the invalidate connection from :" + address);
            return;
        }

        IncomingServerSession incommingSession = config.getSessionManager().getLocalIncommingSession(endpoint);

        // The session has created for other channel from endpoint
        if (incommingSession.getConnection().getChannel() != channel) {
            logger.error("The session for {0} has created, only one connection can be allowed.", address);
            channel.close();
            return;
        }

        // create a outgoing session for the endpoint
        config.getSessionManager().createLocalOutgoingSession(endpoint);

        // forward to others handlers
        super.childChannelOpen(ctx, e);
    }

    @Override
    public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
        final Channel channel = ctx.getChannel();
        Endpoint endpoint = config.getEndpoint(channel.getRemoteAddress());
        IncomingServerSession incommingSession = config.getSessionManager().getLocalIncommingSession(endpoint);

        if (incommingSession != null) {
            incommingSession.close();
        }

        OutgoingServerSession outgoingSession = config.getSessionManager().getOutgoingSession(incommingSession);
        if (outgoingSession != null) {
            outgoingSession.close();
        }

        super.childChannelClosed(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        try {
            // bound channel to thread local
            ChannelBound.bound(ctx.getChannel());

            if (e.getMessage() instanceof Message) {
                Message messge = (Message) e.getMessage();

                // mutation the command
                processReceivedMessage(messge, e.getChannel());

            } else {
                ctx.sendUpstream(e);
            }
        } finally {
            ChannelBound.unbound();
        }
    }

    private void processReceivedMessage(Message message, Channel channel) {

        Verb verb = message.getVerb();

        // register multiplexer session
        if (verb == Verb.BOUND_MULTIPLEXER_SESSION) {
            IncomingServerSession session = config.getSessionManager().getLocalIncommingSession(channel);

            BatchSessionCreator batchCreator = (BatchSessionCreator) message.getBody();

            Message reply = new Message();
            reply.setId(message.getId());
            reply.setVerb(Verb.RES_BOUND_MULTIPLEXER_SESSION);
            reply.setBody(new byte[]{0});

            for (String multiplexerSessionId : batchCreator.getSessions()) {
                session.boundMultiplexerSession(multiplexerSessionId);
            }

            // send reply
            session.sendOneway(reply);
        } else {
            IncomingServerSession session = config.getSessionManager().getLocalIncommingSession(ChannelBound.get());
            // delegates the message processing to bound IncomingServerSession
            session.receive(message);
        }
    }

    private boolean checkSession(Message message, Channel channel) {

        IncomingServerSession incomingServersession = config.getSessionManager().getLocalIncommingSession(channel);

        // validate whether the channel has been bound with one
        // IncomingServerSession
        if (incomingServersession == null) {
            throw new NotAuthException();
        }

        String sessionId = message.getSessionId();

        // if session id is null, it indicates that the message target is the
        // master session
        if (sessionId == null || sessionId.equals(incomingServersession.getId())) {
            return true;
        }

        // otherwise, the session may be a multiplexer session, it must be bound
        // on the outgoing channel,and, must have a connected ClientSession
        OutgoingServerSession outgoingServerSession = config.getSessionManager().getOutgoingServerSession(sessionId);
        if (outgoingServerSession == null) {
            throw new NotBoundSessionException(sessionId, GlobalConfiguration.getInstance().getEndpoint(channel
                    .getRemoteAddress()));
        }

        ClientSession clientSession = config.getSessionManager().getClientSession(sessionId);
        if (clientSession == null) {
            throw new NotFoundClientSessionException(sessionId);
        }

        return true;
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        if (e.getMessage() instanceof Message) {
            Message message = (Message) e.getMessage();
            e.getChannel().write(message.serialize());
        } else {
            ctx.sendDownstream(e);
        }
    }

}
