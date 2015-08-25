package com.network.test;

import com.db.connect.NeoConnect;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;

/**
 * Created by Yangyi on 2015/8/22.
 */
public class ShortestPathTest {

    public static void main(String[] args){
        GraphDatabaseService graph = NeoConnect.getInstance("F:\\neo4jTest\\201101m.db");
        PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.allTypesAndDirections(), new CostEvaluator<Double>() {
            @Override
            public Double getCost(Relationship relationship, Direction direction) {
                return 1.0;
            }
        });
        Node startNode = null, endNode = null;
        try(Transaction tx = graph.beginTx()) {
            startNode = graph.getNodeById(112);
            endNode = graph.getNodeById(223);
            for(Path path : finder.findAllPaths(startNode, endNode)){
                System.out.println("1");
            }
            tx.success();
        }


    }
}
