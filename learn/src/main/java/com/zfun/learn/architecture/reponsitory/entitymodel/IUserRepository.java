package com.zfun.learn.architecture.reponsitory.entitymodel;

public interface IUserRepository {

    UserType loadFromLocal();

    void login(OnCallback callback);

    interface OnCallback{
        void onSuc(UserType userType);
        void onFail(int code,String msg);
    }
}
