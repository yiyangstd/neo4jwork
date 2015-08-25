package com.neo4j.engine.algointerface;

import org.neo4j.graphdb.Node;

/**
 * Created by Yangyi on 2015/8/19.
 */
public interface VertexAlgorithm extends Algorithm {
    void init(Node node);

    void apply(Node node);

    void collectResult(Node node);

    int getMaxIterations();

    long getStablePart();

    void reSetStablePart();

    String getAttributeName();
}
