package com.hopper.server.thrift;

import com.hopper.server.thrift.netty.TNettyClientTransport;
import com.hopper.session.ClientSession;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

/**
 * BidDirectNotify represents a bidirectional communication between client and netty server(thrift),
 * the server use it to notify the client for status changes.
 */
public class BidDirectNotify {
    /**
     * Protocol factory
     */
    private final TProtocolFactory protocolFactory;
    /**
     * Client proxy
     */
    private final HopperService.Client client;

    public BidDirectNotify(ClientSession session) {
        this(new TBinaryProtocol.Factory(), new TNettyClientTransport(session));
    }

    public BidDirectNotify(TProtocolFactory protocolFactory, TTransport transport) {
        this.protocolFactory = protocolFactory;
        this.client = new HopperService.Client(protocolFactory.getProtocol(transport));
    }

    public void statusChange(int oldStatus, int newStatus) throws TException {
        client.statusChange(oldStatus, newStatus);
    }
}
