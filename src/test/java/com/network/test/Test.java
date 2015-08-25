package com.network.test;

import com.db.connect.NeoConnect;
import com.neo4j.pretreat.DegreeCount;
import com.neo4j.pretreat.GraphDelete;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

/**
 * Created by Administrator on 2015/8/18.
 */
public class Test {

    public static void main(String[] args){
        /*
        one commit
         */
//        System.out.println("Hello World!");
        GraphDatabaseService g = NeoConnect.getInstance("F:\\amzData\\amzData.db");
        System.out.println("nodeCount: " + DegreeCount.countNode(g));
        System.out.println("edgeCount: " + DegreeCount.countEdge(g));
//        Map<Long, Long> map = DegreeCount.countDegree(g);
//        for(Map.Entry entry : map.entrySet()){
//            System.out.println("degree:" + entry.getKey() + "  count: " + entry.getValue());
//        }
//        GraphDelete.deleteEdge(g, 3L);
//        System.out.println("nodeCount: " + DegreeCount.countNode(g));
//        System.out.println("edgeCount: " + DegreeCount.countEdge(g));
    }
}
