package com.neo4j.pretreat;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yangyi on 2015/8/21.
 */
public class DegreeCount {

    public static long countNode(GraphDatabaseService g){
        long count = -1L;
        try(Transaction tx = g.beginTx()) {
            count = IteratorUtil.count(GlobalGraphOperations.at(g).getAllNodes());
            tx.success();
        }
        return count;
    }

    public static long countEdge(GraphDatabaseService g){
        long count = -1L;
        try(Transaction tx = g.beginTx()) {
            count = IteratorUtil.count(GlobalGraphOperations.at(g).getAllRelationships());
            tx.success();
        }
        return count;
    }

    public static Map<Long, Long> countDegree(GraphDatabaseService g){
        Map<Long, Long> degreeMap = new HashMap<>();
        try(Transaction tx = g.beginTx()) {
            for (Node node : GlobalGraphOperations.at(g).getAllNodes()) {
                long degree = node.getDegree();
                if (degreeMap.containsKey(degree)) {
                    degreeMap.put(degree, degreeMap.get(degree) + 1L);
                } else {
                    degreeMap.put(degree, 1L);
                }
            }
            tx.success();
        }
        return degreeMap;
    }
}
