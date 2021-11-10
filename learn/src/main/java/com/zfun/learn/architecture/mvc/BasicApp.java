package com.zfun.learn.architecture.mvc;

import android.app.Application;
import com.zfun.learn.architecture.reponsitory.UserCache;
import com.zfun.learn.architecture.reponsitory.UserRepository;
import com.zfun.learn.architecture.reponsitory.WebServer;

public class BasicApp extends Application {
    private static BasicApp mInstance;

    private UserCache mUserCache;
    private WebServer mWebServer;

    @Override
    public void onCreate() {
        super.onCreate();

        mUserCache = new UserCache();
        mWebServer = new WebServer();

        mInstance = this;
    }

    public UserRepository getUserRepository(){
        return UserRepository.getInstance(mUserCache,mWebServer);
    }

    public static BasicApp getInstance(){
        return mInstance;
    }
}
