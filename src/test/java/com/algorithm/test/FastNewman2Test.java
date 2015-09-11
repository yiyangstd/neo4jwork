package com.algorithm.test;

import com.db.connect.NeoConnect;
import com.neo4j.algorithm.FastNewman2;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Created by Yangyi on 2015/9/5.
 */
public class FastNewman2Test {
    public static void main(String[] args){
        GraphDatabaseService graph = NeoConnect.getInstance("F:\\neo4jTest\\karate2.db");
        FastNewman2 test = new FastNewman2(graph);
        test.execute();
    }
}
