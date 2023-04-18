package com.outzone.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;


public class IdGeneratorUtil {
    //4位
    private static Integer index = 0;
    private static Integer machineCode = 12;

    //十三位

    static {
        machineCode = new Random().nextInt(8)+1;
    }

    public static Long generateId(){
        if(index > 9999) index = 0;
        Date date = new Date();
        Long timemills = date.getTime() % 10000000000L * 10000;

        return timemills += machineCode * 10000000000L + (index++);


    }


}
