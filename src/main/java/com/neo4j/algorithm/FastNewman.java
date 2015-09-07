package com.neo4j.algorithm;

import com.util.Timer;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Yangyi on 2015/8/26.
 */
public class FastNewman {
    protected Logger logger = Logger.getLogger(FastNewman.class.getName());
    protected double Q = 0.0;
    protected double deltaQ = 0.0;
    protected final String attName = "community";
    protected GraphDatabaseService graph;
    private Label communityLabel;
    private IndexDefinition communityIndex;
    private long communityA = 0L, communityB = 0L;
    private long edgeNum = 0L;
    private long communityNum = 0L;
    private boolean tag = false;

    public FastNewman(GraphDatabaseService graph){
        Timer.timer().start();
        this.graph = graph;
        communityLabel = DynamicLabel.label("communityLabel");
        try(Transaction tx = graph.beginTx()){
            for(IndexDefinition index : graph.schema().getIndexes(communityLabel)){
                for(String key : index.getPropertyKeys()){
                    if(key.equals(attName)){
                        tag = true;
                    }
                }
            }
            tx.success();
        }
        if(!tag) {
            try (Transaction tx = graph.beginTx()) {
                communityIndex = graph.schema().indexFor(communityLabel).on(attName).create();
                tx.success();
            }
            try (Transaction tx = graph.beginTx()) {
                graph.schema().awaitIndexOnline(communityIndex, 10, TimeUnit.SECONDS);
                tx.success();
            }
        }
        try(Transaction tx = graph.beginTx()){
            for(Relationship relationship : GlobalGraphOperations.at(graph).getAllRelationships()){
                edgeNum ++;
            }
            tx.success();
        }
        try(Transaction tx = graph.beginTx()){
            for(Node node : GlobalGraphOperations.at(graph).getAllNodes()){
                node.addLabel(communityLabel);
                node.setProperty(attName, node.getId());
                communityNum ++;
            }
            tx.success();
        }
        Timer.timer().stop();
        logger.info("Init: " + Timer.timer().totalTime());
        logger.info("Network contains " + communityNum + " nodes, " + edgeNum + " edges");
    }

    public void execute(){
        Timer.timer().start();
        while(true){
            try(Transaction tx = graph.beginTx()){
                for(Relationship relationship : GlobalGraphOperations.at(graph).getAllRelationships()){
                    Node srcNode = relationship.getStartNode();
                    Node dstNode = relationship.getEndNode();
                    if(srcNode.getProperty(attName) == dstNode.getProperty(attName)){
                        continue;
                    }else{
                        computeDelta((long)srcNode.getProperty(attName), (long)dstNode.getProperty(attName));
                    }
                }
                tx.success();
            }
            if(deltaQ > 0){
                System.out.println("Selected " + deltaQ + " " + communityA + " " + communityB);
                try(Transaction tx = graph.beginTx()){
                    mergeCommunity(communityA, communityB);
                    communityNum --;
                    tx.success();
                }
                Q += deltaQ;
                deltaQ = 0.0;
            }else{
                break;
            }
        }
        Timer.timer().stop();
        logger.info("Execute: " + Timer.timer().totalTime());
        logger.info("Q is " + Q);
    }

    private void computeDelta(long srcCommunity, long dstCommuntiy){
        ResourceIterator<Node> srcIterator = graph.findNodes(communityLabel, attName, srcCommunity);
        ResourceIterator<Node> dstIterator = graph.findNodes(communityLabel, attName, dstCommuntiy);
        //计算srcCommunity和dstCommunity的度
        long srcDegree = 0L, dstDegree = 0L, edgeCount = 0L;//srcDegree源群组的度 dstDegree目标群组的度 edgeCount群组之间的边数
        Set<Node> srcSet = IteratorUtil.asSet(srcIterator);
        Set<Node> dstSet = IteratorUtil.asSet(dstIterator);
        for(Node node : srcSet){
            for(Relationship relationship : node.getRelationships()){
                Node otherNode = relationship.getOtherNode(node);
                if(dstSet.contains(otherNode)){
                    edgeCount ++;
                }
                if(!srcSet.contains(otherNode)){
                    srcDegree ++;
                }
            }
        }
        for(Node node : dstSet){
            for(Relationship relationship : node.getRelationships()){
                Node otherNode = relationship.getOtherNode(node);
                if(!dstSet.contains(otherNode)){
                    dstDegree ++;
//                    dstNebSet.add((long)otherNode.getProperty(attName));
                }
            }
        }
        double tempDelta = (double)(2 * edgeNum * edgeCount - dstDegree * srcDegree) / (double)(2 * edgeNum * edgeNum);
//        double tempDelta = 2 * (double)(edgeNum * edgeCount - dstDegree * srcDegree) / (double)(edgeNum * edgeNum);
//        System.out.println("tempDelta " + srcCommunity + " " + dstCommuntiy + " " + tempDelta);
        if(tempDelta > deltaQ){
            deltaQ = tempDelta;
            communityA = srcCommunity;
            communityB = dstCommuntiy;
        }
    }

    private void mergeCommunity(long srcCommunity, long targetCommunity){
       ResourceIterator<Node> srcIterator = graph.findNodes(communityLabel, attName, srcCommunity);
       while(srcIterator.hasNext()){
           Node node = srcIterator.next();
           node.setProperty(attName, targetCommunity);
       }
    }

    public String getAttName(){
        return attName;
    }
}
