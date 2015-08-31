package com.algorithm.test;

import com.db.connect.NeoConnect;
import com.neo4j.algorithm.PageRank;
import com.neo4j.engine.GrapAlgoEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

/**
 * Created by Yangyi on 2015/8/19.
 */
public class PageRankTest {
    public static void main(String[] args) throws IOException {
        GraphDatabaseService g = NeoConnect.getInstance("F:\\neo4jTest\\sim.db");
        GrapAlgoEngine engine = new GrapAlgoEngine(g);
        PageRank pageRank = new PageRank(g);
        engine.execute(pageRank, 0.98);

        Path resultPath = Paths.get("F:\\neo4jTest\\201101mPageRankResult.txt");
        if(Files.exists(resultPath)){
            Files.delete(resultPath);
        }
        Files.createFile(resultPath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultPath.toFile()));
        writer.write("id\t degree\t pagerank");
        writer.newLine();
        DecimalFormat df=new DecimalFormat("#.000000000");
        try(Transaction tx = g.beginTx()){
            for(Node node : GlobalGraphOperations.at(g).getAllNodes()){
                writer.write(node.getId() + "\t" + node.getDegree() + "\t " + df.format((double) node.getProperty("PageRank")));
                writer.newLine();
            }
            writer.close();
            tx.success();
        }
    }
}
