package com.algorithm.test;

import com.db.connect.NeoConnect;
import com.neo4j.algorithm.PageRank;
import com.neo4j.engine.GrapAlgoEngine;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Created by Yangyi on 2015/8/19.
 */
public class PageRankTest {
    public static void main(String[] args){
        GraphDatabaseService g = NeoConnect.getInstance("F:\\neo4jTest\\201101m.db");
        GrapAlgoEngine engine = new GrapAlgoEngine(g);
        PageRank pageRank = new PageRank(g);
        engine.execute(pageRank);
    }
}
