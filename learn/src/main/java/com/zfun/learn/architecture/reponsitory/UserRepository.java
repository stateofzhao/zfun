package com.zfun.learn.architecture.reponsitory;

import com.zfun.learn.architecture.bean.User;

//针对单一可信源解释下：
//为了保证数据的一致性，我们需要从同一个来源读取数据，比如，本地要展示的数据有两个来源：
//本地缓存CacheData，和网络数据NetData（获取后需要保存到CacheData中），
// 那么我们在读取数据时为了保证数据一致性，就需要始终以本地为准！
// 过程是先读取网络然后保存到本地之后再从本地读取，而不能是读取到网络后就显示网络数据（同时进行本地保存），
// 这样一旦保存出问题就不能做到数据一致性了（本地数据和显示的数据不一样）；
// 如果只有一个来源，那么就无所谓了，直接使用即可。
public class UserRepository {
    private static UserRepository sInstance;

    private UserCache cache;
    private WebServer webServer;

    //参数注入，便于解藕和测试
    private UserRepository(UserCache cache, WebServer webServer) {
        this.cache = cache;
        this.webServer = webServer;
    }


    public static UserRepository getInstance(UserCache cache, WebServer webServer) {
        if (sInstance == null) {
            synchronized (UserRepository.class) {
                if (sInstance == null) {
                    sInstance = new UserRepository(cache,webServer);
                }
            }
        }
        return sInstance;
    }

    public User getUser(String userId) {
        refreshUser(userId);
        return cache.getUser(userId);//单一可信来源，始终显示本地数据
    }

    public User updateUserName(String userId,String name){
        User updatedUser = webServer.updateUserName(userId,name);
        cache.saveUser(updatedUser);
        return updatedUser;
    }

    private void refreshUser(String userId) {
        boolean isExists = cache.isExists(userId);
        if(!isExists){
            User user = webServer.getUser(userId);
            cache.saveUser(user);
        }
    }
}
