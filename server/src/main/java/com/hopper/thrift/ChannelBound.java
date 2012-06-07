package com.hopper.thrift;

import org.jboss.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Records the client's channel while processing the client message.
 */
public class ChannelBound {
    /**
     * ThreadLocal
     */
    private static final ThreadLocal<ChannelWithLatch> channelBound = new ThreadLocal<ChannelWithLatch>();

    /**
     * Bound channel to current thread, if channel has bound, increments the counter.
     */
    public static void bound(Channel channel) {
        if (channelBound.get() == null) {
            channelBound.set(new ChannelWithLatch(channel));
        } else {
            ChannelWithLatch latch = channelBound.get();
            latch.counter.incrementAndGet();
        }
    }

    /**
     * Unbound channel from current thread, decrements the counter, if counter=0, remove the channel
     */
    public static void unbound() {
        ChannelWithLatch latch = channelBound.get();
        if (latch != null) {
            latch.counter.decrementAndGet();
            if (latch.counter.get() == 0) {
                channelBound.remove();
            }
        }
    }

    public static Channel get() {
        ChannelWithLatch latch = channelBound.get();
        return latch == null ? null : latch.channel;
    }


    private static class ChannelWithLatch {
        final AtomicInteger counter = new AtomicInteger(0);
        final Channel channel;

        ChannelWithLatch(Channel channel) {
            this.channel = channel;
        }
    }
}
