package com.db.test;


import com.db.connect.NeoConnect;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Created by Yangyi on 2015/8/18.
 */
public class NeoConnectTest {
    public static void main(String[] args){
        GraphDatabaseService grapdb = NeoConnect.getInstance("F:\\neo4jTest\\karate.db");
        int nodeCount = 0;
        int relsCount = 0;
        try (Transaction tx = grapdb.beginTx()) {
            nodeCount = IteratorUtil.count(GlobalGraphOperations.at(grapdb).getAllNodes());
            relsCount = IteratorUtil.count( GlobalGraphOperations.at(grapdb).getAllRelationships() );
            tx.success();
        }
        System.out.println("nodeCount: " + nodeCount);
        System.out.println("relsCount: " + relsCount);
    }
}
