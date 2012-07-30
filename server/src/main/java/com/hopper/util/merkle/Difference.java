package com.hopper.util.merkle;

import com.hopper.session.Serializer;

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
public class Difference<T extends MerkleObjectRef> implements Serializer {

    /**
     * Holden the state nodes that should be added
     */
    public final List<T> addedList = new ArrayList<T>();
    /**
     * Holden the state nodes that should be removed
     */
    public final List<T> removedList = new ArrayList<T>();
    /**
     * Holden the state nodes that should be updated
     */
    public final List<T> updatedList = new ArrayList<T>();

    private Class<T> clazz;

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void added(MerkleNode<T> node) {
        addedList.addAll(node.getObjectRefs());
    }

    /**
     * All state nodes associated with <code>node</code> should be removed.
     */
    public void removed(MerkleNode<T> node) {
        removedList.addAll(node.getObjectRefs());
    }

    public void updated(Leaf<T> node1, Leaf<T> node2) {
        List<T> refs1 = node1.getObjectRefs();
        List<T> refs2 = node2.getObjectRefs();

        Collections.sort(refs1, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return compareStateNode(o1, o2);
            }
        });

        Collections.sort(refs2, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return compareStateNode(o1, o2);
            }
        });

        int i = 0;
        int j = 0;

        while (i < refs1.size() && j < refs2.size()) {

            T n1 = refs1.get(i);
            T n2 = refs2.get(j);

            int r = compareStateNode(n1, n2);

            if (r < 0) {
                addedList.add(n1);
                i++;
            } else if (r == 0) {
                if (n1.getVersion() > n2.getVersion()) {
                    updatedList.add(n1);
                    i++;
                    j++;
                }
            } else {
                removedList.add(n2);
                j++;
            }
        }

        if (i < refs1.size()) {
            addedList.addAll(refs1.subList(i, refs1.size()));
        } else if (j < refs2.size()) {
            removedList.addAll(refs2.subList(j, refs2.size()));
        }
    }

    private int compareStateNode(T node1, T node2) {
        return node1.getKey().compareTo(node2.getKey());
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

    private void serializeList(List<T> stateNodeList, DataOutput out) throws IOException {
        out.writeInt(stateNodeList.size());
        for (T snapshot : stateNodeList) {
            snapshot.serialize(out);
        }
    }

    private void readList(final List<T> nodeList, int size, DataInput in) throws IOException {
        for (int i = 0; i < size; i++) {
            try {
                T instance = clazz.newInstance();
                instance.deserialize(in);
                nodeList.add(instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
