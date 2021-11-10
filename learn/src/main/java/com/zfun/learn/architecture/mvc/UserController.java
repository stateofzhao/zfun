package com.zfun.learn.architecture.mvc;

import android.view.View;
import com.zfun.learn.architecture.bean.User;

public class UserController {
    private UserModel mUserModel;
    private View mView;
    private String mUserId;

    public UserController(UserModel userModel, View view){
        mUserModel = userModel;
        mView = view;

        //在View中找到"按钮"，给其注册点击事件，在点击回调中调用Model相关方法来更新数据
        mUserModel.setOnChangedListener(new UserModel.OnChangedListener() {
            @Override
            public void onChange(User user) {
                showUserName(user);
            }
        });
    }

    public void start(String userId){
        mUserId = userId;
        User user = mUserModel.getUser(userId);
        showUserName(user);
    }

    public void destroyView(){
        //
    }

    private void showUserName(User user){
        //mView中找到显示Name的TextView来显示user.getName();
    }
}
