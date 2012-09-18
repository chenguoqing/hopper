package com.hopper;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * All server
 */
public class NodeRing {
    /**
     * All node address
     */
    private final List<InetSocketAddress> nodes = new ArrayList<>();

    private final AtomicInteger ringIndex = new AtomicInteger();
    private final int ringSize;

    public NodeRing(List<InetSocketAddress> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new NullPointerException();
        }

        this.nodes.addAll(nodes);
        this.ringSize = nodes.size();
    }

    public InetSocketAddress getCurrentNode() {
        return nodes.get(ringIndex.get() % ringSize);
    }

    public InetSocketAddress getNextNode() {
        return nodes.get(ringIndex.incrementAndGet() % ringSize);
    }
}
