package com.algorithm.test;

import com.db.connect.NeoConnect;
import com.neo4j.algorithm.FastNewman3;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Created by Yangyi on 2015/9/10.
 */
public class FastNewman3Test {

    public static void main(String[] args){
        GraphDatabaseService graph = NeoConnect.getInstance("F:\\neo4jTest\\201101m.db");
        FastNewman3 test = new FastNewman3(graph);
        test.execute();
    }
}
