package com.hopper.util.merkle;

import com.hopper.session.BufferDataInput;
import com.hopper.session.BufferDataOutput;
import com.hopper.util.MurmurHash;
import junit.framework.Assert;
import org.junit.Test;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: chenguoqing Date: 12-7-19 Time: 下午3:11 To
 * change this template use File | Settings | File Templates.
 */
public class TestMerkleTree {

    @Test
    public void testSerializer() throws Exception {

        // same
        TestObject node1 = new TestObject("/a/b/c1", 11);
        TestObject node2 = new TestObject("/a/b/c2", 12);
        TestObject node3 = new TestObject("/a/b/c3", 13);
        TestObject node4 = new TestObject("/a/b/c4", 13);
        TestObject node5 = new TestObject("/a/b/c5", 13);
        TestObject node6 = new TestObject("/a/b/c6", 13);
        TestObject node7 = new TestObject("/a/b/c7", 13);
        TestObject node8 = new TestObject("/a/b/c8", 13);

        MerkleTree<TestObject> tree = new MerkleTree<TestObject>((byte) 15, TestObject.class);
        TestObjectFactory factory = new TestObjectFactory();
        tree.setObjectFactory(factory);
        factory.nodes.put(node1.key, node1);
        factory.nodes.put(node2.key, node2);
        factory.nodes.put(node3.key, node3);
        factory.nodes.put(node4.key, node4);
        factory.nodes.put(node5.key, node5);
        factory.nodes.put(node6.key, node6);
        factory.nodes.put(node7.key, node7);
        factory.nodes.put(node8.key, node8);

        tree.put(node1);
        tree.put(node2);
        tree.put(node3);
        tree.put(node4);
        tree.put(node5);
        tree.put(node6);
        tree.put(node7);
        tree.put(node8);

        tree.loadHash();

        BufferDataOutput out = new BufferDataOutput();
        tree.serialize(out);

        DataInput in = new BufferDataInput(out.buffer());

        MerkleTree<TestObject> tree2 = new MerkleTree<TestObject>((byte) 15, TestObject.class);
        tree2.deserialize(in);

        assertTreeEquals(tree, tree2);
    }

    @Test
    public void testWideHash() throws Exception {
        String key1 = "Solr is the popular, blazing fast open source enterprise search platform from the Apache Lucene project. Its major features include powerful full-text search, hit highlighting, faceted search, dynamic clustering, database integration, rich document (e.g., Word, PDF) handling, and geospatial search. Solr is highly scalable, providing distributed search and index replication, and it powers the search and navigation features of many of the world's largest internet sites";
        String key2 = "Solr is written in Java and runs as a standalone full-text search server within a servlet container such as Tomcat. Solr uses the Lucene Java search library at its core for full-text indexing and search, and has REST-like HTTP/XML and JSON APIs that make it easy to use from virtually any programming language. Solr's powerful external configuration allows it to be tailored to almost any type of application without Java coding, and it has an extensive plugin architecture when more advanced customization is required";

        int hash1 = MurmurHash.hash(key1);
        int hash2 = MurmurHash.hash(key2);

        MerkleTree<TestObject> tree = new MerkleTree<TestObject>((byte) 15, TestObject.class);
        TestObjectFactory factory = new TestObjectFactory();
        tree.setObjectFactory(factory);

        Method m = MerkleTree.class.getDeclaredMethod("findAndCreateLeaf", new Class[]{int.class});
        m.setAccessible(true);

        Leaf<TestObject> leaf1 = (Leaf<TestObject>) m.invoke(tree, hash1);
        Leaf<TestObject> leaf2 = (Leaf<TestObject>) m.invoke(tree, hash2);

        Assert.assertNotNull(leaf1);
        Assert.assertNotNull(leaf2);
        Assert.assertFalse(leaf1 == leaf2);
    }

    @Test
    public void testDiff() {
        String key1 = "Solr is the popular, blazing fast open source enterprise search platform from the Apache Lucene project. Its major features include powerful full-text search, hit highlighting, faceted search, dynamic clustering, database integration, rich document (e.g., Word, PDF) handling, and geospatial search. Solr is highly scalable, providing distributed search and index replication, and it powers the search and navigation features of many of the world's largest internet sites";
        String key2 = "Solr is written in Java and runs as a standalone full-text search server within a servlet container such as Tomcat. Solr uses the Lucene Java search library at its core for full-text indexing and search, and has REST-like HTTP/XML and JSON APIs that make it easy to use from virtually any programming language. Solr's powerful external configuration allows it to be tailored to almost any type of application without Java coding, and it has an extensive plugin architecture when more advanced customization is required";
        String key3 = "ts always a good idea to separate the searching from the indexing. The searcher should accept a query string, and return a list of hits";
        String key4 = "Implement additional search functionality";
        String key5 = "/cloudsearch/lock/a";
        String key6 = "/cloudsearch/lock/b";
        String key7 = "/cloudsearch/lock/c";
        String key8 = "/cloudsearch/lock/d";
        String key9 = "org.apache.lucene.search.Query";
        String key10 = "org.apache.lucene.search.Query3";

        // same
        TestObject node1 = new TestObject(key1, 11);
        TestObject node2 = new TestObject(key2, 12);
        TestObject node3 = new TestObject(key3, 13);

        // tree1
        TestObject node4 = new TestObject(key4, 13);
        TestObject node5 = new TestObject(key5, 13);
        TestObject node6 = new TestObject(key6, 13);

        // tree2
        TestObject node7 = new TestObject(key7, 13);
        TestObject node8 = new TestObject(key8, 13);
        TestObject node9 = new TestObject(key9, 13);

        // update
        TestObject node10 = new TestObject(key10, 13);
        TestObject node11 = new TestObject(key10, 12);

        MerkleTree<TestObject> tree1 = new MerkleTree<TestObject>((byte) 15, TestObject.class);
        TestObjectFactory factory1 = new TestObjectFactory();
        tree1.setObjectFactory(factory1);
        factory1.nodes.put(node1.key, node1);
        factory1.nodes.put(node2.key, node2);
        factory1.nodes.put(node3.key, node3);

        factory1.nodes.put(node4.key, node4);
        factory1.nodes.put(node5.key, node5);
        factory1.nodes.put(node6.key, node6);

        factory1.nodes.put(node10.key, node10);

        TestObjectFactory factory2 = factory1.copy();
        factory2.nodes.put(node1.key, node1);
        factory2.nodes.put(node2.key, node2);
        factory2.nodes.put(node3.key, node3);

        factory2.nodes.put(node7.key, node7);
        factory2.nodes.put(node8.key, node8);
        factory2.nodes.put(node9.key, node9);

        factory2.nodes.put(node11.key, node11);

        MerkleTree<TestObject> tree2 = new MerkleTree<TestObject>((byte) 15, TestObject.class);
        tree2.setObjectFactory(factory2);

        // same
        tree1.put(node1);
        tree1.put(node2);
        tree1.put(node3);

        tree2.put(node1);
        tree2.put(node2);
        tree2.put(node3);

        // tree1 owen
        tree1.put(node4);
        tree1.put(node5);
        tree1.put(node6);

        // tree2 own
        tree2.put(node7);
        tree2.put(node8);
        tree2.put(node9);

        tree1.put(node10);
        tree2.put(node11);

        tree1.loadHash();
        tree2.loadHash();

        Difference<TestObject> diff = tree1.difference(tree2);

        Assert.assertNotNull(diff);
        Assert.assertEquals(diff.addedList.size(), 3);
        Assert.assertEquals(diff.removedList.size(), 3);
        Assert.assertEquals(diff.updatedList.size(), 1);
    }

    private void assertTreeEquals(MerkleTree<TestObject> tree1, MerkleTree<TestObject> tree2) {
        assertMerkleNodeEquals(tree1.getRoot(), tree2.getRoot());
    }

    private void assertMerkleNodeEquals(MerkleNode<TestObject> node1, MerkleNode<TestObject> node2) {
        if (node1 == null) {
            Assert.assertNull(node2);
            return;
        }

        if (node2 == null) {
            Assert.assertNull(node1);
            return;
        }

        MerkleNode<TestObject> node1Left = node1.getLeft();
        MerkleNode<TestObject> node1Right = node1.getRight();

        MerkleNode<TestObject> node2Left = node2.getLeft();
        MerkleNode<TestObject> node2Right = node2.getRight();

        if (node1Left == null) {
            Assert.assertNull(node2Left);
        }

        if (node2Left == null) {
            Assert.assertNull(node1Left);
        }

        if (node1Right == null) {
            Assert.assertNull(node2Right);
        }

        if (node2Right == null) {
            Assert.assertNull(node1Right);
        }

        if (node1 instanceof Leaf) {
            Assert.assertTrue(node2 instanceof Leaf);
        }

        if (node2 instanceof Leaf) {
            Assert.assertTrue(node1 instanceof Leaf);
        }

        if (node1 instanceof InnerNode) {
            Range range1 = node1.getRange();
            Range range2 = node1.getRange();
            assertRangeEquals(range1, range2);

            if (node1 instanceof Leaf) {
                assertLeafEquals((Leaf<TestObject>) node1, (Leaf<TestObject>) node2);
            } else {
                assertMerkleNodeEquals(node1.getLeft(), node2.getLeft());
                assertMerkleNodeEquals(node1.getRight(), node2.getRight());
            }
        }
    }

    private void assertLeafEquals(Leaf<TestObject> leaf1, Leaf<TestObject> leaf2) {
        Assert.assertEquals(leaf1.getKeyHash(), leaf2.getKeyHash());
        Assert.assertEquals(leaf1.getVersionHash(), leaf2.getVersionHash());
        Assert.assertEquals(leaf1.getObjectRefs().size(), leaf2.getObjectRefs().size());

        for (int index = 0; index < leaf1.getObjectRefs().size(); index++) {
            TestObject o1 = leaf1.getObjectRefs().get(index);
            TestObject o2 = leaf2.getObjectRefs().get(index);

            Assert.assertEquals(o1.getKey(), o2.getKey());
            Assert.assertEquals(o1.getVersion(), o2.getVersion());
        }
    }

    private void assertRangeEquals(Range range1, Range range2) {
        Assert.assertEquals(range1.getLeft(), range2.getLeft());
        Assert.assertEquals(range1.getRight(), range2.getRight());
    }

    static class TestObject implements MerkleObjectRef {

        private String key;
        private long version;
        private String desc;
        private int status;

        public TestObject() {
        }

        public TestObject(String key, long version) {
            this.key = key;
            this.version = version;
        }

        public TestObject(String key, long version, String desc) {
            this.key = key;
            this.version = version;
            this.desc = desc;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setVersion(long version) {
            this.version = version;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void serialize(DataOutput out) throws IOException {
            out.writeUTF(key);
            out.writeLong(version);
            out.writeUTF(desc);
        }

        @Override
        public void deserialize(DataInput in) throws IOException {
            this.key = in.readUTF();
            this.version = in.readLong();
            this.desc = in.readUTF();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public long getVersion() {
            return version;
        }
    }

    static class TestObjectFactory implements MerkleObjectFactory<TestObject> {

        final Map<String, TestObject> nodes = new HashMap<String, TestObject>();

        @Override
        public TestObject find(String key) {
            return nodes.get(key);
        }

        TestObjectFactory copy() {
            TestObjectFactory factory = new TestObjectFactory();
            factory.nodes.putAll(nodes);
            return factory;
        }
    }
}
