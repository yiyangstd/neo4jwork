package com.neo4j.algorithm;

import com.neo4j.engine.algointerface.VertexAlgorithm;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.*;

/**
 * Created by Yangyi on 2015/8/19.
 */
public class LabelPropagation implements VertexAlgorithm{
    protected final String attName = "community";
    protected Map<Long, Long> result = new HashMap<>();
    protected long statblePart = 0L;

    @Override
    public void init(Node node) {
        node.setProperty(attName, node.getId());
    }

    @Override
    public void apply(Node node) {
        long label = getMostFrequentLabel(node);
        if(node.hasProperty(attName)){
            if((long)node.getProperty(attName) == label){
                statblePart ++;
            }
        }
        node.setProperty(attName, label);
    }

    protected long getMostFrequentLabel(Node node){
        long temp = 0L;
        List<Long> labelList = new ArrayList<>();
        Random random = new Random();
        Map<Long,Long> nextLabel = new HashMap<>();
        for(Relationship relationship : node.getRelationships()){
            Node otherNode = relationship.getOtherNode(node);
            long otherNodeLabel = (long)otherNode.getProperty(attName);
            if(nextLabel.containsKey(otherNodeLabel)){
                nextLabel.put(otherNodeLabel, nextLabel.get(otherNodeLabel) + 1L);
            }else {
                nextLabel.put(otherNodeLabel, 1L);
            }
        }
        for(Map.Entry<Long, Long> entry : nextLabel.entrySet()){
            if(entry.getValue() > temp){
                temp = entry.getValue();
                labelList.clear();
                labelList.add(entry.getKey());
//                label = entry.getKey();
            }else if(entry.getValue() == temp){
                labelList.add(entry.getKey());
            }
        }
        int index = random.nextInt(labelList.size());
        return labelList.get(index);
    }

    @Override
    public void collectResult(Node node) {
        result.put(node.getId(), (long)node.getProperty(attName));
    }

    @Override
    public int getMaxIterations() {
        return 5;
    }

    @Override
    public long getStablePart() {
        return statblePart;
    }

    @Override
    public void reSetStablePart() {
        this.statblePart = 0L;
    }

    @Override
    public String getAttributeName() {
        return attName;
    }

    @Override
    public String getName() {
        return "LabelPropagation";
    }

    @Override
    public Object getResult() {
        return result;
    }
}
