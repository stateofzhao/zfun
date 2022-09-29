package com.zfun.learn.architecture.uiarchitecture.mvc;

import android.view.View;
import com.zfun.learn.architecture.reponsitory.entitymodel.IUserRepository;
import com.zfun.learn.architecture.reponsitory.entitymodel.UserEntity;
import com.zfun.learn.architecture.reponsitory.entitymodel.UserType;

public class UserController {
    private final UserEntity mUserModel;
    private final View mRootView;
    private final View mShowUserV;
    private final View mShowFailV;
    private final View mShowLoadingV;

    public UserController(View view){
        mUserModel = new UserEntity(new IUserRepository() {
            @Override
            public UserType loadFromLocal() {
                return null;
            }

            @Override
            public void login(OnCallback callback) {

            }
        });
        mUserModel.init();
        mRootView = view;
        mShowUserV = mRootView.findViewWithTag(1);
        mShowFailV = mRootView.findViewWithTag(2);
        mShowLoadingV = mRootView.findViewWithTag(3);
    }

    public void start(){
        final UserType user = mUserModel.getLoginUser();
        if(null != user){
            showUserName(user);
        }else {
            showLoading();
            mUserModel.login(new UserEntity.OnListener() {
                @Override
                public void onLoginSuc() {
                    final UserType user = mUserModel.getLoginUser();
                    showUserName(user);
                }

                @Override
                public void onFail(int code, String msg) {

                }
            });
        }
    }

    public void destroyView(){
        //释放资源
    }

    private void showUserName(UserType user){
        //mView中找到显示Name的TextView来显示user.getName();
    }

    private void showLoading(){

    }

    private void showLoginFail(int code,String msg){

    }
}
