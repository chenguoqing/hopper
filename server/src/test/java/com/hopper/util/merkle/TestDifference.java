package com.hopper.util.merkle;

import com.hopper.util.merkle.TestMerkleTree.TestObject;
import com.hopper.util.merkle.TestMerkleTree.TestObjectFactory;
import junit.framework.Assert;
import org.junit.Test;

public class TestDifference {

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

        // leaf1 own
        TestObject node1 = new TestObject(key1, 11, "node1");
        TestObject node2 = new TestObject(key2, 12, "node2");
        TestObject node3 = new TestObject(key3, 13, "node3");

        // leaf2 own
        TestObject node4 = new TestObject(key4, 13, "node4");
        TestObject node5 = new TestObject(key5, 13, "node5");
        TestObject node6 = new TestObject(key6, 13, "node6");

        TestObject node71 = new TestObject(key7, 14, "node7");
        TestObject node81 = new TestObject(key8, 15, "node8");
        TestObject node91 = new TestObject(key9, 16, "node9");

        node71.setStatus(2);
        node81.setStatus(2);
        node91.setStatus(2);

        TestObject node72 = new TestObject(key7, 13, "node7");
        TestObject node82 = new TestObject(key8, 13, "node8");
        TestObject node92 = new TestObject(key9, 13, "node9");

        node72.setStatus(3);
        node82.setStatus(3);
        node92.setStatus(3);

        // same
        TestObject node10 = new TestObject(key10, 13);
        TestObject node11 = new TestObject(key10, 13);

        Leaf<TestObject> leaf1 = new Leaf<TestObject>(new Range(-100, 100));
        Leaf<TestObject> leaf2 = new Leaf<TestObject>(new Range(-100, 100));

        TestObjectFactory factory1 = new TestObjectFactory();
        TestObjectFactory factory2 = new TestObjectFactory();

        factory1.nodes.put(node1.getKey(), node1);
        factory1.nodes.put(node2.getKey(), node2);
        factory1.nodes.put(node3.getKey(), node3);
        factory1.nodes.put(node71.getKey(), node71);
        factory1.nodes.put(node81.getKey(), node81);
        factory1.nodes.put(node91.getKey(), node91);
        factory1.nodes.put(node10.getKey(), node10);

        factory2.nodes.put(node4.getKey(), node4);
        factory2.nodes.put(node5.getKey(), node5);
        factory2.nodes.put(node6.getKey(), node6);
        factory2.nodes.put(node72.getKey(), node72);
        factory2.nodes.put(node82.getKey(), node82);
        factory2.nodes.put(node92.getKey(), node92);
        factory2.nodes.put(node11.getKey(), node11);

        leaf1.put(node1);
        leaf1.put(node2);
        leaf1.put(node3);
        leaf1.put(node71);
        leaf1.put(node81);
        leaf1.put(node91);
        leaf1.put(node10);

        leaf2.put(node4);
        leaf2.put(node5);
        leaf2.put(node6);

        leaf2.put(node72);
        leaf2.put(node82);
        leaf2.put(node92);
        leaf2.put(node11);

        leaf1.setObjectFactory(factory1);
        leaf2.setObjectFactory(factory2);

        Difference<TestObject> diff = new Difference<>();

        leaf1.hash();
        leaf2.hash();
        diff.updated(leaf1, leaf2);

        System.out.println("Added list........");
        for (TestObject to : diff.addedList) {
            System.out.println(to.getDesc());
        }

        System.out.println("Removed list........");
        for (TestObject to : diff.removedList) {
            System.out.println(to.getDesc());
        }
        System.out.println("Updated list........");
        for (TestObject to : diff.updatedList) {
            System.out.println(to.getDesc());
        }

        Assert.assertEquals(diff.addedList.size(), 3);
        Assert.assertEquals(diff.removedList.size(), 3);
        Assert.assertEquals(diff.updatedList.size(), 3);
    }
}
