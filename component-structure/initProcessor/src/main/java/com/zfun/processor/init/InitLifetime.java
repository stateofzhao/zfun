package com.zfun.processor.init;


public enum InitLifetime {
    APP_ONCREATE("ApplicationOnCreate()"),
    ENTRYACTIVITY_ONCREATE("EntryActivityOnCreate()"),//启动页的onCreate()方法
    ENTRYACTIVITY_ONCREATE_AFTER_PROTOCOL("EntryActivityOnCreateAfterProtocol()"),//启动页用户同意隐私政策后调用
    MAINACTIVITY_ONCREATE("MainActivityOnCreate()");//主页面Activity的onCreate方法

    private String str;
    InitLifetime(String str){
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
