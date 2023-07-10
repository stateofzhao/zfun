package com.zfun.initapi;

import com.zfun.initapi.internal.IInitProvider;
import com.zfun.initapi.utils.PrintMsg;
import com.zfun.initapi.utils.SortUtil;

import java.util.ArrayList;
import java.util.List;

public class InitMgr {
    public static boolean DEBUG = true;
    //
    private static boolean isSorted = false;
    private static final List<IInitProvider> iInitList = new ArrayList<>();//原始初始化数据
    // dependentInitList + noDependentInitList == iInitList
    private static final List<IInitProvider> dependentInitList = new ArrayList<>();//按照依赖关系排序好的初始化数据
    private static final List<IInitProvider> noDependentInitList = new ArrayList<>();//无依赖关系的初始化数据，可以单独初始化

    static {
        PrintMsg.println("InitMgr static -- ");
    }

    public static void init(IInitLifecycle initLifecycle, InitCallback callback) {
        PrintMsg.println("原始 initList 大小：" + iInitList.size());
        sortIfNeeded();
        PrintMsg.println("有依赖关系的初始化List大小：" + dependentInitList.size());
        PrintMsg.println("【无】有依赖关系的初始化List大小：" + noDependentInitList.size());

        //开两个线成初始化，后续其实可以根据 noDependentInitList 大小，来多开几个线程初始化，
        // 但是 dependentInitList 只能在一个线程中初始化
        /*new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();*/

        final int size = iInitList.size();
        for (int i = size - 1; i >= 0; i--) {
            final IInitProvider iInitProvider = iInitList.get(i);
            final Object initObj = iInitProvider.get();
            if (initObj instanceof IInit) {
                ((IInit) initObj).init(initLifecycle, null);
            }
        }
    }

    public static void sortIfNeeded() {
        if (isSorted) {
            return;
        }
        isSorted = true;
        PrintMsg.println("InitMgr 排序 start-- ");
        final List<IInitProvider> sortList = SortUtil.sortDependencies(iInitList);
        dependentInitList.addAll(sortList);
        if (sortList.size() > 0 && sortList.size() < iInitList.size()) {
            for (IInitProvider srcItem : iInitList) {
                boolean isNotInDependentList = true;
                for (IInitProvider sortItem : sortList) {
                    if (srcItem == sortItem) {
                        isNotInDependentList = false;
                        break;
                    }
                }
                if (isNotInDependentList) {
                    noDependentInitList.add(srcItem);
                }
            }
        }
        PrintMsg.println("InitMgr 排序 end-- ");
    }

    public static List<IInitProvider> getDependentInitList(){
        return new ArrayList<>(dependentInitList);
    }

    public static List<IInitProvider> getNoDependentInitList(){
        return new ArrayList<>(noDependentInitList);
    }

    interface InitCallback{
        void end();
    }//

    //
    private InitMgr() {
    }
}
