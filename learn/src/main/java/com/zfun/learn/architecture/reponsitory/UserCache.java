package com.zfun.learn.architecture.reponsitory;

import com.zfun.learn.architecture.bean.User;

public class UserCache {

    //todo zfun 从本地读取User
    public User getUser(String userId){
        return new User();
    }

    //todo zfun 判断本地缓存是否有User
    public boolean isExists(String userId){
        return true;
    }

    public void saveUser(User user){
        //todo zfun 保存User到本地
    }
}
