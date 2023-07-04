package com.zfun.initapi.utils;

import com.zfun.initapi.InitMgr;

public class PrintMsg {
    public static void println(String msg){
        if (InitMgr.DEBUG){
            System.out.println(msg);
        }
    }
}
