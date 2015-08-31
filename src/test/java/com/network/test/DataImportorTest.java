package com.network.test;

import com.neo4j.prepare.DataImportor;

/**
 * Created by Yangyi on 2015/8/21.
 */
public class DataImportorTest {

    public static void main(String[] args){
        DataImportor importor = new DataImportor("F:\\neo4jTest\\karate.db");
        importor.getDataFromFile("F:\\neo4jTest\\karate.txt");
    }
}
