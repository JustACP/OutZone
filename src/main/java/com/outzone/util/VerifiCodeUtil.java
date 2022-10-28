package com.outzone.util;

import java.util.Random;


public class VerifiCodeUtil {
    public static String generateVerifiCode(){
        Random random =  new Random();
        Integer code = random.nextInt(9999999 - 1000000 + 1) + 1000000;
        return String.valueOf(code);
    }
}
