package com.zfun.processor.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class InitMgr {
    private final ConcurrentHashMap<IInit,Object> forInit = new ConcurrentHashMap<>();
    private final List<IInit> iInitList = new ArrayList<>();

    private final AtomicBoolean isCopy = new AtomicBoolean(false);

    public static InitMgr getInstance(){
        return Holder.initMgr;
    }

    public void registerInitInAndroid(){
        //todo lzf 将 InitInAndroid 注解的类自动注册进来

    }

    public void addInit(IInit init){
        forInit.put(init,new Object());
    }

    public void remove(IInit init){
        forInit.remove(init);
    }

    public void init(InitLifetime initLifetime){
        if(isCopy.compareAndSet(false,true)){
            Enumeration<IInit> keys = forInit.keys();
            while (keys.hasMoreElements()){
                iInitList.add(keys.nextElement());

            }        }
        for (IInit aInit:iInitList){
            aInit.init(initLifetime);
        }
    }

    private void sort(List<IInit> iInitList){
        Collections.sort(iInitList,(o1,o2)->{
             return o1.level()-o2.level();
        });
    }

    private static class Holder {
        private static final InitMgr initMgr = new InitMgr();//loadClass的过程jvm保证同步，类的静态字段会在loadClass时赋值
    }//

    private InitMgr(){}
}
