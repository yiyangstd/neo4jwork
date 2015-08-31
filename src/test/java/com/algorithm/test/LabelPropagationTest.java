package com.algorithm.test;

import com.db.connect.NeoConnect;
import com.neo4j.algorithm.LabelPropagation;
import com.neo4j.engine.GrapAlgoEngine;
import com.neo4j.pretreat.DegreeCount;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yangyi on 2015/8/19.
 */
public class LabelPropagationTest {
    public static void main(String[] args){
        GraphDatabaseService g = NeoConnect.getInstance("F:\\amzData\\amzData.db");
        GrapAlgoEngine engine = new GrapAlgoEngine(g);
        LabelPropagation labelPropagation = new LabelPropagation();
        engine.execute(labelPropagation, 0.99);
        Map<Long, Long> result = (Map<Long, Long>)labelPropagation.getResult();
        Map<Long, Long> count = new HashMap<>();
        for(Map.Entry<Long, Long> entry : result.entrySet()){
            Long id = entry.getKey();
            Long label = entry.getValue();
            if(count.containsKey(label)){
                count.put(label, count.get(label) + 1);
            }else{
                count.put(label, 1L);
            }
        }
        System.out.println("Size: " + count.size());
        for(Map.Entry<Long, Long> entry : count.entrySet()){
            System.out.println("Label:" + entry.getKey() + "  Num: " + entry.getValue());
        }

        Path outPutPath = Paths.get("F:\\amzData\\amzDataout.txt");
        BufferedWriter writer = null;
        try {
            if (Files.exists(outPutPath)) {
                Files.delete(outPutPath);
            }
            Files.createFile(outPutPath);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            writer = new BufferedWriter(new FileWriter(outPutPath.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<Long, List<Long>> communities = new HashMap<>();
        for(Map.Entry<Long, Long> entry : result.entrySet()){
            Long id = entry.getKey();
            Long label = entry.getValue();
            if(communities.containsKey(label)){
                communities.get(label).add(id);
            }else {
                List<Long> list = new ArrayList<>();
                list.add(id);
                communities.put(label, list);
            }
        }
        for(Map.Entry<Long, List<Long>> entry : communities.entrySet()){
//            System.out.print("Comminity " + entry.getKey() + " :");
            try {
                for (Long id : entry.getValue()) {
//                System.out.print(id + " ");
                    writer.write(id + " ");
                }
//            System.out.println();
                writer.newLine();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
