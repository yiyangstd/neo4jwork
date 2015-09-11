package com.neo4j.algorithm;

import com.util.Timer;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Yangyi on 2015/9/10.
 */
public class FastNewman3 {
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

    private BufferedWriter fileWriter = null;

    public FastNewman3(GraphDatabaseService graph){
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

        java.nio.file.Path filePath = Paths.get("F:\\neo4jTest\\shangchang01.txt");
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            Files.createFile(filePath);
            fileWriter = new BufferedWriter(new FileWriter(filePath.toFile()));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void execute(){
        Timer.timer().start();

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
        while(true) {
            deltaQ = 0.0;
            findMaxDeltaQ();
            if(deltaQ <= 0){
                System.out.println(eValueMap.size() + " " + deltaQ);
                break;
            }else{
                System.out.println("Selected " + deltaQ + " " + communityA + " " + communityB);
                try {
                    fileWriter.write("Selected " + deltaQ + " " + communityA + " " + communityB);
                    fileWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try(Transaction tx = graph.beginTx()){
                    mergeCommunity(communityA, communityB);
                    tx.success();
                }
            }
        }
        Timer.timer().stop();
        logger.info("Execute " + Timer.timer().totalTime());
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findMaxDeltaQ(){
        for(Map.Entry<EValue, Long> entry : eValueMap.entrySet()){
            EValue eValue = entry.getKey();
            long eij = entry.getValue();
            long ai = aValueMap.get(eValue.getI());
            long aj = aValueMap.get(eValue.getJ());
            double temp = 2 * (double)(eij * edgeNum - ai * aj) / (double)(edgeNum * edgeNum);
            try {
                fileWriter.write("   " + "i:" + eValue.getI() + "  j:" + eValue.getJ()  + "  ai:" + ai + "  j:" + eValue.getJ() + "  aj:" + aj + " eij:" + eij + "  deltaQ:" + temp);
                fileWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        EValue eValue = new EValue(srcCommunity, targetCommunity);
        long oldEij = eValueMap.get(eValue);
        long oldAi = aValueMap.get(srcCommunity);
        long oldAj = aValueMap.get(targetCommunity);
        long newAj = oldAi + oldAj - 2 * oldEij;
        List<EValue> eValueRemoveList = new ArrayList<>();
        Map<EValue, Long> eValueAddMap = new HashMap<>();
        eValueMap.remove(new EValue(srcCommunity, targetCommunity));
        for(Map.Entry<EValue, Long> entry : eValueMap.entrySet()){
            EValue tempEvalue = entry.getKey();
            if(tempEvalue.getI() == srcCommunity){
                //eik
                EValue ekj = new EValue(tempEvalue.getJ(), targetCommunity);
                if(eValueMap.containsKey(ekj)){
                    eValueAddMap.put(ekj, eValueMap.get(ekj) + entry.getValue());
                }else{
                    eValueAddMap.put(ekj, entry.getValue());
                }
//                eValueMap.remove(eValue1);
                eValueRemoveList.add(tempEvalue);
            }else if(tempEvalue.getJ() == srcCommunity){
                //eki
                EValue ekj = new EValue(tempEvalue.getI(), targetCommunity);
                if(eValueMap.containsKey(ekj)){
                    eValueAddMap.put(ekj, eValueMap.get(ekj) + entry.getValue());
                }else{
                    eValueAddMap.put(ekj, entry.getValue());
                }
//                eValueMap.remove(eValue1);
                eValueRemoveList.add(tempEvalue);
            }
        }
        aValueMap.remove(srcCommunity);
        aValueMap.put(targetCommunity, newAj);
        if(!eValueRemoveList.isEmpty()) {
            for (EValue removeE : eValueRemoveList) {
                eValueMap.remove(removeE);
            }
        }
        eValueMap.putAll(eValueAddMap);
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
