package com.hopper.client.thrift;

import com.hopper.thrift.HopperService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessageReceiverThread supports the thrift biddirection communication for receving the callback message
 */
public class MessageReceiverThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MessageReceiverThread.class);

    private final TProtocol protocol;
    private final HopperService.Processor<HopperServiceCallback> processor;
    private volatile boolean shutdown = false;

    /**
     * Constructor
     */
    public MessageReceiverThread(TProtocol protocol, HopperServiceCallback clientMessageHandler) {
        this.protocol = protocol;
        this.processor = new HopperService.Processor<HopperServiceCallback>(clientMessageHandler);
    }

    public void shutdown() {
        this.shutdown = true;
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {

        while (!shutdown) {
            try {
                processor.process(protocol, protocol);
            } catch (TException e) {
                logger.error("Failed to process received message.", e);
            }
        }
    }
}
