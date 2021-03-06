package com.neo4j.prepare;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yangyi on 2015/8/21.
 */
public class DataImportor {
    private String dbPath;
    private BatchInserter inserter;
    private long nodeNum = 0L;
    private long edgeNum = 0L;

    public DataImportor(String dbPath){
        this.dbPath = dbPath;
        this.inserter = BatchInserters.inserter(dbPath);
    }

    public void getDataFromFile(String filePath){
        Path path = Paths.get(filePath);
        if(Files.notExists(path)){
            return;
        }
        Map<String, Object> weightMap = new HashMap<>();
        weightMap.put("weight", 1L);
        Label label = DynamicLabel.label("karate");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            String line = null;
            while((line = reader.readLine()) != null){
                if(line.startsWith("#")){
                    continue;
                }
                String[] nodeIds = line.split(" ");
                if(nodeIds.length != 2){
                    continue;
                }
                long nodeA = Long.parseLong(nodeIds[0]);
                long nodeB = Long.parseLong(nodeIds[1]);
                if(!inserter.nodeExists(nodeA)){
                    Map<String, Object> map = new HashMap<>();
                    map.put("community", nodeA);
                    inserter.createNode(nodeA, map, label);
                    nodeNum ++;
                }
                if(!inserter.nodeExists(nodeB)){
                    Map<String, Object> map = new HashMap<>();
                    map.put("community", nodeB);
                    inserter.createNode(nodeB, map, label);
                    nodeNum ++;
                }
                inserter.createRelationship(nodeA, nodeB, RelTypes.CONNECT, weightMap);
                edgeNum ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            inserter.shutdown();
        }

        System.out.println("Network contains " + nodeNum + " nodes, " + edgeNum + " edges.");
    }

    public String getDbPath(){
        return dbPath;
    }

    private static enum RelTypes implements RelationshipType
    {
        CONNECT
    }
}
