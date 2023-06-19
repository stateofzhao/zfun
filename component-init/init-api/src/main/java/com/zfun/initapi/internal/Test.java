package com.zfun.initapi.internal;

import com.zfun.initapi.InitMgr;

import java.util.ArrayList;
import java.util.List;

public class Test {
    static final List<Object> iInitList = new ArrayList<>();

    static {

        InitMgr object1 = new InitMgr();
        iInitList.add(object1);
        Object object2 = new Object();
        iInitList.add(object2);
        Object object3 = new Object();
        iInitList.add(object3);
        Object object4 = new Object();
        iInitList.add(object4);

        System.out.println("静态块初始化执行完毕");
    }

}
