package com.algorithm.test;

import com.db.connect.NeoConnect;
import com.neo4j.algorithm.GN;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Created by Yangyi on 2015/9/5.
 */
public class GNTest {
    public static void main(String[] args){
        GraphDatabaseService graph = NeoConnect.getInstance("F:\\amzData\\amzData.db");
        GN gn = new GN(graph);
        gn.execute();
    }
}
