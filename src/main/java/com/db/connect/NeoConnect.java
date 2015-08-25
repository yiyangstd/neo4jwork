package com.db.connect;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Created by Yangyi on 2015/8/18.
 */
public class NeoConnect {
    private static GraphDatabaseService graphDb = null;

    public static GraphDatabaseService getInstance(String dbPath){
        if(graphDb != null){
            return graphDb;
        }else{
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
            registerShutdownHook(graphDb);
            return graphDb;
        }
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                graphDb.shutdown();
            }
        });
    }
}
