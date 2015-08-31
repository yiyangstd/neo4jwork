package com.validation;

import com.util.Timer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Yangyi on 2015/8/30.
 */
public class CommunityValidation {
    private Logger logger = Logger.getLogger(CommunityValidation.class.getName());
    private File resultFile;
    private File trueFile;
    private List<List<Long>> result = new ArrayList<>();
    private List<List<Long>> validation = new ArrayList<>();
    private long resultIdNum = 0L;

    public CommunityValidation(String resultFileString, String trueFileString) throws Exception{
        Timer.timer().start();
        Path resultFilePath = Paths.get(resultFileString);
        Path trueFilePath = Paths.get(trueFileString);
        BufferedReader resultReader = null;
        BufferedReader validationReader = null;
        if(!Files.exists(resultFilePath)){
            throw new Exception("result file not exist!");
        }else{
            this.resultFile = resultFilePath.toFile();
        }
        if(!Files.exists(trueFilePath)){
            throw new Exception("true file not exist!");
        }else{
            this.trueFile = trueFilePath.toFile();
        }
        resultReader = new BufferedReader(new FileReader(this.resultFile));
        validationReader = new BufferedReader(new FileReader(this.trueFile));
        String resultLine = null;
        String validationLine = null;
        while((resultLine = resultReader.readLine()) != null && resultLine != ""){

            List<Long> community = new ArrayList<>();
            for(String id : resultLine.split(" ")){
                resultIdNum ++;
                community.add(Long.parseLong(id));
            }
            this.result.add(community);
        }
        while((validationLine = validationReader.readLine()) != null && validationLine != ""){
            List<Long> community = new ArrayList<>();
            for(String id : validationLine.split("\t")){
                community.add(Long.parseLong(id));
            }
            this.validation.add(community);
        }
        resultReader.close();
        validationReader.close();
        Timer.timer().stop();
        logger.info("Init " + Timer.timer().totalTime() + " Result community num : " + this.result.size() + ", Validation community num : " + this.validation.size());
    }

    public double getAccuracy(double p){
        Timer.timer().start();
        double accuracy = 0.0;
        long matchNum = 0L;
        for(List<Long> resultLine : this.result){
            long lineMatch = 0L;
            for(List<Long> validationLine : this.validation){
                long match = matchNum(resultLine, validationLine);
                if(match > lineMatch){
                    lineMatch = match;
                }
            }
            if((double)lineMatch / resultLine.size() >= p) {
                matchNum += lineMatch;
            }
        }
        accuracy = (double)matchNum / resultIdNum;
        Timer.timer().stop();
        logger.info("Compute acuracy : " + Timer.timer().totalTime());
        return accuracy;
    }

    private long matchNum(List<Long> srcList, List<Long> targetList){
        long num = 0L;
        for(long id : srcList){
            if(targetList.contains(id)){
                num ++;
            }
        }
        return num;
    }
}
