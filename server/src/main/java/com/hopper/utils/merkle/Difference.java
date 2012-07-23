package com.hopper.utils.merkle;

import com.hopper.session.Serializer;
import com.hopper.storage.StateNode;
import com.hopper.storage.StateNodeSnapshot;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * {@link Difference} represents a comparison result of two merkle-trees..
 */
public class Difference implements Serializer {

    /**
     * Holden the state nodes that should be added
     */
    public final List<StateNodeSnapshot> addedList = new ArrayList<StateNodeSnapshot>();
    /**
     * Holden the state nodes that should be removed
     */
    public final List<StateNodeSnapshot> removedList = new ArrayList<StateNodeSnapshot>();
    /**
     * Holden the state nodes that should be updated
     */
    public final List<StateNodeSnapshot> updatedList = new ArrayList<StateNodeSnapshot>();

    public void added(MerkleNode node) {
        addedList.addAll(getStateNodeSnapshots(node));
    }

    /**
     * All state nodes associated with <code>node</code> should be removed.
     */
    public void removed(MerkleNode node) {
        removedList.addAll(getStateNodeSnapshots(node));
    }


    public void updated(Leaf node1, Leaf node2) {
        List<StateNode> stateNodes1 = node1.getObjectRefs();
        List<StateNode> stateNodes2 = node2.getObjectRefs();

        Collections.sort(stateNodes1, new Comparator<StateNode>() {
            @Override
            public int compare(StateNode o1, StateNode o2) {
                return o1.key.compareTo(o2.key);
            }
        });

        Collections.sort(stateNodes2, new Comparator<StateNode>() {
            @Override
            public int compare(StateNode o1, StateNode o2) {
                return o1.key.compareTo(o2.key);
            }
        });

        int i = 0;
        int j = 0;

        while (i < stateNodes1.size() && j < stateNodes2.size()) {

            StateNode n1 = stateNodes1.get(i);
            StateNode n2 = stateNodes2.get(j);

            int r = compareStateNode(n1, n2);

            if (r < 0) {
                addedList.add(new StateNodeSnapshot(n1));
                i++;
            } else if (r == 0) {
                if (n1.getVersion() > n2.getVersion()) {
                    updatedList.add(new StateNodeSnapshot(n1));
                    i++;
                    j++;
                }
            } else {
                removedList.add(new StateNodeSnapshot(n2));
                j++;
            }
        }

        if (i < stateNodes1.size()) {
            addedList.addAll(wrapStateNodeList(stateNodes1, i));
        } else if (j < stateNodes2.size()) {
            removedList.addAll(wrapStateNodeList(stateNodes2, j));
        }
    }

    private int compareStateNode(StateNode node1, StateNode node2) {
        return node1.key.compareTo(node2.key);
    }

    public boolean hasDifferences() {
        return !addedList.isEmpty() || !updatedList.isEmpty() || !removedList.isEmpty();
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        serializeList(addedList, out);
        serializeList(updatedList, out);
        serializeList(removedList, out);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        int size = in.readInt();
        readList(addedList, size, in);
        size = in.readInt();
        readList(updatedList, size, in);
        size = in.readInt();
        readList(removedList, size, in);
    }

    private void serializeList(List<StateNodeSnapshot> stateNodeList, DataOutput out) throws IOException {
        out.writeInt(stateNodeList.size());
        for (StateNodeSnapshot snapshot : stateNodeList) {
            snapshot.serialize(out);
        }
    }

    private void readList(List<StateNodeSnapshot> snapshots, int size, DataInput in) throws IOException {
        for (int i = 0; i < size; i++) {
            StateNodeSnapshot snapshot = new StateNodeSnapshot();
            snapshot.deserialize(in);
            snapshots.add(snapshot);
        }
    }

    private List<StateNodeSnapshot> getStateNodeSnapshots(MerkleNode node) {

        List<StateNode> nodes = node.getObjectRefs();

        List<StateNodeSnapshot> snapshots = new ArrayList<StateNodeSnapshot>(nodes.size());
        for (StateNode state : nodes) {
            snapshots.add(new StateNodeSnapshot(state));
        }

        return snapshots;
    }

    private List<StateNodeSnapshot> wrapStateNodeList(List<StateNode> stateNodeList, int offset) {
        List<StateNodeSnapshot> snapshots = new ArrayList<StateNodeSnapshot>();

        for (int i = offset; i < stateNodeList.size(); i++) {
            snapshots.add(new StateNodeSnapshot(stateNodeList.get(i)));
        }

        return snapshots;
    }
}
