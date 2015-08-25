package com.neo4j.pretreat;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Created by Yangyi on 2015/8/21.
 */
public class GraphDelete {

    public static void deleteEdge(GraphDatabaseService g ,long weight){
        try(Transaction tx = g.beginTx()){
            for(Relationship relationship : GlobalGraphOperations.at(g).getAllRelationships()){
                if(relationship.hasProperty("weight")){
                    if((long)((int)relationship.getProperty("weight")) <= weight){
                        for(Node node : relationship.getNodes()){
                            if(node.getDegree() == 1){
                                node.delete();
                            }
                        }
                        relationship.delete();
                    }
                }
            }
            tx.success();
        }
    }

    public static  void deleteNode(GraphDatabaseService g, long degree){
        try(Transaction tx = g.beginTx()){
            for(Node node : GlobalGraphOperations.at(g).getAllNodes()){
                if(node.getDegree() <= degree){
                    node.delete();
                }
            }
            tx.success();
        }
    }
}
