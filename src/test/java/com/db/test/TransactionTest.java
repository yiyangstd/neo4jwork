package com.db.test;

import com.db.connect.NeoConnect;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Created by Yangyi on 2015/8/22.
 */
public class TransactionTest {

    public static void main(String[] args){
        GraphDatabaseService graph = NeoConnect.getInstance("F:\\neo4jTest\\neotest.db");
        Node node = null;
        try(Transaction tx = graph.beginTx()){
            node = graph.getNodeById(123);
            try(Transaction tx2 = graph.beginTx()) {
                System.out.println(node.getProperty("name"));
                tx2.success();
            }
            tx.success();
        }
//        try(Transaction tx = graph.beginTx()) {
//            System.out.println(node.getProperty("name"));
//            tx.success();
//        }
    }
}
