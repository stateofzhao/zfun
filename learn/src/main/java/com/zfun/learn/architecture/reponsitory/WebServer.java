package com.zfun.learn.architecture.reponsitory;

import com.zfun.learn.architecture.bean.User;

public class WebServer {
    //todo zfun 从网络获取User
    public User getUser(String userId){
        return new User();
    }

    //todo zfun 更新服务器上UserName
    public User updateUserName(String userId,String userName){
        User user = new User();
        user.name = userName;
        return user;
    }
}
