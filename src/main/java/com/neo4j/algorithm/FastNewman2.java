package com.neo4j.algorithm;

import com.util.Timer;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Yangyi on 2015/9/5.
 */
public class FastNewman2 {
    private Logger logger = Logger.getLogger(FastNewman2.class.toString());
    private GraphDatabaseService graph = null;
    private Label communityLabel;
    private IndexDefinition communityIndex;
    private Map<EValue, Long> eValueMap = new HashMap<>();//eij
    private Map<Long, Long> aValueMap = new HashMap<>();//ai, key is community id, value is community degree
    protected double Q = 0.0;
    protected double deltaQ = 0.0;
    private long communityA = 0L, communityB = 0L;
    protected final String attName = "community";
    private long edgeNum = 0L;
    private long communityNum = 0L;
    private boolean tag = false;

    public FastNewman2(GraphDatabaseService graph){
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
        while(true) {
            this.aValueMap.clear();
            this.eValueMap.clear();
            try (Transaction tx = graph.beginTx()) {
                for (Relationship relationship : GlobalGraphOperations.at(graph).getAllRelationships()) {
                    Node srcNode = relationship.getStartNode();
                    Node dstNode = relationship.getEndNode();
                    long srcCommunity = (long) srcNode.getProperty(attName);
                    long dstCommunity = (long) dstNode.getProperty(attName);
                    if (srcCommunity == dstCommunity) {
                        continue;
                    } else {
                        if(aValueMap.containsKey(srcCommunity)){
                            aValueMap.put(srcCommunity, aValueMap.get(srcCommunity) + 1);
                        }else{
                            aValueMap.put(srcCommunity, 1L);
                        }
                        if(aValueMap.containsKey(dstCommunity)){
                            aValueMap.put(dstCommunity, aValueMap.get(dstCommunity) + 1);
                        }else{
                            aValueMap.put(dstCommunity, 1L);
                        }
                        EValue eValue = new EValue(srcCommunity, dstCommunity);
                        if(eValueMap.containsKey(eValue)){
                            eValueMap.put(eValue, eValueMap.get(eValue) + 1L);
                        }else{
                            eValueMap.put(eValue, 1L);
                        }
                    }
                }
                tx.success();
            }
            findMaxDeltaQ();
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
        logger.info("Execute " + Timer.timer().totalTime());
    }

    private void findMaxDeltaQ(){
        for(Map.Entry<EValue, Long> entry : eValueMap.entrySet()){
            EValue eValue = entry.getKey();
            long eij = entry.getValue();
            long ai = aValueMap.get(eValue.getI());
            long aj = aValueMap.get(eValue.getJ());
            double temp = 2 * (double)(eij * edgeNum - ai * aj) / (double)(edgeNum * edgeNum);
//            System.out.println("i:" + eValue.getI() + "  j:" + eValue.getJ() + " eij:" + eij + "  deltaQ:" + temp);
            if(temp > deltaQ){
                deltaQ = temp;
                communityA = eValue.getI();
                communityB = eValue.getJ();
            }
        }
    }

    private void mergeCommunity(long srcCommunity, long targetCommunity){
        ResourceIterator<Node> srcIterator = graph.findNodes(communityLabel, attName, srcCommunity);
        while(srcIterator.hasNext()){
            Node node = srcIterator.next();
            node.setProperty(attName, targetCommunity);
        }
    }

    class EValue{
        private long i = 0L;
        private long j = 0L;

        public EValue(long i, long j){
            this.i = i;
            this.j = j;
        }

        public long getI(){
            return i;
        }

        public long getJ(){
            return j;
        }

        @Override
        public int hashCode(){
            int hashcode = 17;
            hashcode = 31 * hashcode + (int)(i ^ (i >>> 32))  + (int)(j ^ (j >>> 32));
//            hashcode = 31 * hashcode + (int)(j ^ (j >>> 32));
            return hashcode;
        }

        @Override
        public boolean equals(Object o){
            if(o == this){
                return true;
            }
            if(!(o instanceof  EValue)){
                return false;
            }
            EValue eValue = (EValue) o;
            return ((i == eValue.i) && (j == eValue.j)) || ((j == eValue.i) && (i == eValue.j));
        }
    }
}
