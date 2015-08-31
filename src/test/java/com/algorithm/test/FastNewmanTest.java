package com.algorithm.test;

import com.db.connect.NeoConnect;
import com.neo4j.algorithm.FastNewman;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Created by Yangyi on 2015/8/26.
 */
public class FastNewmanTest {

    public static void main(String[] args){
        GraphDatabaseService graph = NeoConnect.getInstance("F:\\neo4jTest\\sim.db");
        FastNewman fastNewman = new FastNewman(graph);
        fastNewman.execute();
        try(Transaction tx = graph.beginTx()){
            for(Node node : GlobalGraphOperations.at(graph).getAllNodes()){
                System.out.println(node.getId() + "  " + node.getProperty(fastNewman.getAttName()));
            }
            tx.success();
        }
    }
}
