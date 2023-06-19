package com.zfun.initapi;

import java.util.ArrayList;
import java.util.List;

public class InitMgr {
    public static final List<Object> iInitList = new ArrayList<>();

    /*static {
        final List<String> initClassNames = new ArrayList<>();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final String generateClassName = "com.zfun.processor.init.InitMgr$" + i;
            try {
                final Class<?> initClass = InitMgr.class.getClassLoader().loadClass(generateClassName);
                final Object obj = initClass.getConstructor().newInstance();
                final Method getInitClassNameMethod = initClass.getDeclaredMethod("getInitClassName");
                final String initClassName = (String) getInitClassNameMethod.invoke(obj);
                initClassNames.add(initClassName);
            } catch (ClassNotFoundException
                     | NoSuchMethodException
                     | InvocationTargetException
                     | IllegalAccessException
                     | InstantiationException e) {
                break;
            }
        }
        for (int i = 0; i < initClassNames.size(); i++) {
            try {
                final String initClassName = initClassNames.get(i);
                final Class<?> initClass = InitMgr.class.getClassLoader().loadClass(initClassName);
                final Object obj = initClass.getConstructor().newInstance();
                if (obj instanceof IInit){
                    iInitList.add((IInit) obj);
                }
            } catch (ClassNotFoundException
                     | InvocationTargetException
                     | InstantiationException
                     | IllegalAccessException
                     | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

       *//* Collections.sort(iInitList, (o1, o2) -> {
            return o1.level() - o2.level();
        });*//*
    }*/

    static {
        System.out.println("initList static -- ");
    }

    public static void init(InitLifecycle initLifecycle) {
        System.out.println("initList 大小："+ iInitList.size());
        for (Object aInit : iInitList) {
            if (aInit instanceof IInit) {
                ((IInit) aInit).init(initLifecycle);
            }
        }
    }

}
