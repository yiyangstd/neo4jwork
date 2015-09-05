package com.neo4j.algorithm;

import com.util.Timer;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.logging.Logger;

/**
 * Created by Yangyi on 2015/8/31.
 */
public class GN {
    private GraphDatabaseService graph;
    private String edgeDeleteTag = "deleted";
    private String attName = "betweeness";
    private Logger logger = Logger.getLogger(GN.class.getName());
    private Betweeness betweeness = null;
    private int nodeNum = 0;
    private int edgeNum = 0;

    public GN(GraphDatabaseService graph){
        this.graph = graph;
        Timer.timer().start();
        try(Transaction tx = graph.beginTx()){
            nodeNum = IteratorUtil.count(GlobalGraphOperations.at(graph).getAllNodes());
            edgeNum = IteratorUtil.count(GlobalGraphOperations.at(graph).getAllRelationships());
            for(Relationship relationship : GlobalGraphOperations.at(graph).getAllRelationships()){
                relationship.setProperty(edgeDeleteTag, false);
                relationship.setProperty(attName, 0L);
            }
            tx.success();
        }
        betweeness = new Betweeness(this.graph, attName, edgeDeleteTag);
        Timer.timer().stop();
        logger.info("Init " + Timer.timer().totalTime());
        logger.info("The network contains " + nodeNum + " nodes, " + edgeNum + " edges.");
    }

    public void execute(){
        Timer.timer().start();
        try(Transaction tx = graph.beginTx()){
            betweeness.computeBetweeness();
            tx.failure();
        }
        Timer.timer().stop();
        logger.info("Execute " + Timer.timer().totalTime());
    }

    public void clean(){
        Timer.timer().start();
        try(Transaction tx = graph.beginTx()){
            for(Relationship relationship : GlobalGraphOperations.at(graph).getAllRelationships()){
                relationship.removeProperty(edgeDeleteTag);
                relationship.removeProperty(attName);
                tx.success();
            }
        }
        Timer.timer().stop();
        logger.info("Clean " + Timer.timer().totalTime());
    }
}

class Betweeness{
    private GraphDatabaseService graph;
    private String attName;
    private String tagName;
    private PathFinder<Path> pathFinder;

    public Betweeness(GraphDatabaseService graph, String attName, String tagName){
        this.graph = graph;
        this.attName = attName;
        this.tagName = tagName;
//        this.pathFinder = GraphAlgoFactory.dijkstra(PathExpanders.allTypesAndDirections(), new CostEvaluator<Double>() {
//            @Override
//            public Double getCost(Relationship relationship, Direction direction) {
//                if((boolean)relationship.getProperty(tagName)){
//                    return Double.MAX_VALUE;
//                }
//
//                return 1.0;
//            }
//        });
        this.pathFinder = GraphAlgoFactory.shortestPath(PathExpanders.allTypesAndDirections(), 6);
    }

    public void computeBetweeness(){
        while(IteratorUtil.count(GlobalGraphOperations.at(graph).getAllRelationships()) > 0) {
            resetBetweeness();
            for (Node startNode : GlobalGraphOperations.at(graph).getAllNodes()) {
                for (Node endNode : GlobalGraphOperations.at(graph).getAllNodes()) {
                    if (startNode.equals(endNode) || startNode.getDegree() == 0 || endNode.getDegree() == 0) {
                        continue;
                    }
                    shortestPath(startNode, endNode);
                }
            }
            compute().delete();
        }
    }

    public Relationship compute(){
        Relationship result = null;
        long betweeness = 0L;
        for(Relationship relationship : GlobalGraphOperations.at(graph).getAllRelationships()){
            if((long)relationship.getProperty(attName) > betweeness){
                betweeness = (long) relationship.getProperty(attName);
                result = relationship;
            }
        }
        System.out.println("Edge " + result.getStartNode().getId() + "->" + result.getEndNode().getId() + " deleted");
        return  result;
    }

    private void shortestPath(Node startNode, Node endNode){
        Path path = pathFinder.findSinglePath(startNode, endNode);
        if(path == null || path.length() == 0){
            return;
        }
        for(Relationship relationship : path.relationships()){
            relationship.setProperty(attName, (long)relationship.getProperty(attName) + 1L);
        }
    }

    private void resetBetweeness(){
        for(Relationship relationship : GlobalGraphOperations.at(graph).getAllRelationships()){
            relationship.setProperty(attName, 0L);
        }
    }
}
