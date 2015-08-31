package com.db.test;

import com.validation.CommunityValidation;

/**
 * Created by Yangyi on 2015/8/30.
 */
public class CommunityValidationTest {

    public static void main(String[] args){
        try {
            CommunityValidation test = new CommunityValidation("F:\\amzData\\outPut.txt","F:\\amzData\\com-amazon.all.dedup.cmty.txt");
            System.out.println(test.getAccuracy(0.8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
