package com.neo4j.engine;

import com.neo4j.algorithm.PageRank;
import com.neo4j.engine.algointerface.VertexAlgorithm;
import com.util.Timer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.logging.Logger;

/**
 * Created by Yangyi on 2015/8/19.
 */
public class GrapAlgoEngine {
    protected GraphDatabaseService graph;
    protected Logger logger = Logger.getLogger(GrapAlgoEngine.class.getName());
    protected long nodeCount;
    protected long edgeCount;

    public GrapAlgoEngine(GraphDatabaseService g){
        this.graph = g;
        try(Transaction tx = graph.beginTx()){
            nodeCount = IteratorUtil.count(GlobalGraphOperations.at(graph).getAllNodes());
            edgeCount = IteratorUtil.count(GlobalGraphOperations.at(graph).getAllRelationships());
            tx.success();
        }
        logger.info("The network contains " + nodeCount + " nodes, " + edgeCount + " edges");
    }

    public void execute(VertexAlgorithm algorithm){
        Timer timer = Timer.newTimer();
        timer.start();
        try(Transaction tx = graph.beginTx()){
            this.initPhase(algorithm);
            tx.success();
        }
//        try(Transaction tx = graph.beginTx()){
            this.main(algorithm);
//            tx.success();
//        }
        try(Transaction tx = graph.beginTx()){
            this.collectResult(algorithm);
            tx.success();
        }
        timer.stop();
        logger.info("Execute: " + timer.totalTime());
    }

    public void execute(VertexAlgorithm algorithm, double stablePercent){
        Timer timer = Timer.newTimer();
        timer.start();
        try(Transaction tx = graph.beginTx()){
            this.initPhase(algorithm);
            tx.success();
        }

        this.main(algorithm, stablePercent);

        try(Transaction tx = graph.beginTx()) {
            this.collectResult(algorithm);
            tx.success();
        }
        timer.stop();
        logger.info("Execute: " + timer.totalTime());
    }

    protected void initPhase(VertexAlgorithm algorithm){
        Timer.timer().start();
        for (Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
            try(Transaction tx = graph.beginTx()) {
                algorithm.init(n);
                tx.success();
            }
        }
        Timer.timer().stop();
        logger.info("Init: " + Timer.timer().totalTime());
    }

    protected void main(VertexAlgorithm algorithm){
        Timer.timer().start();
        for (int i = 0; i < algorithm.getMaxIterations(); i++) {
            algorithm.reSetStablePart();
            try(Transaction tx = graph.beginTx()) {
                for (Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
                    try(Transaction tx2 = graph.beginTx()) {
                        algorithm.apply(n);
                        tx2.success();
                    }
                }
                tx.success();
            }
        }
        Timer.timer().stop();
        logger.info("Main: " + Timer.timer().totalTime());
    }

    protected void main(VertexAlgorithm algorithm, double stablePercent){
        Timer.timer().start();
        int count = 0;
        do{
            algorithm.reSetStablePart();
            try(Transaction tx = graph.beginTx()) {
                for (Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
                    algorithm.apply(n);
                }
                tx.success();
            }
            count ++;
            System.out.println(algorithm.getStablePart() + "  " + (double)algorithm.getStablePart() / nodeCount);
        }while ((double)algorithm.getStablePart() / nodeCount <= stablePercent);
        Timer.timer().stop();
        logger.info("Main: " + Timer.timer().totalTime() + "  count: " + count);
    }

    public void collectResult(VertexAlgorithm algorithm){
        Timer.timer().start();
        for (Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
            try(Transaction tx = graph.beginTx()) {
                algorithm.collectResult(n);
                tx.success();
            }
        }
        Timer.timer().stop();
        logger.info("CollectResult: " + Timer.timer().totalTime());
    }

    public void clean(VertexAlgorithm algo) {
        try (Transaction tx = graph.beginTx()) {
            for (Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
                String attrName = algo.getAttributeName();
                if (n.hasProperty(attrName))
                    n.removeProperty(attrName);
            }
            tx.success();
        }
    }
}
