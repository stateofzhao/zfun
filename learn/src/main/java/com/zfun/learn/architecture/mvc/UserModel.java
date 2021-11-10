package com.zfun.learn.architecture.mvc;

import com.zfun.learn.architecture.bean.User;
import com.zfun.learn.architecture.reponsitory.UserRepository;

//这一层对数据源层添加了一个数据改变回调，如果数据源层直接就有回调，那么这个类就完全没必要存在了，直接使用数据源类即可～
public class UserModel {
    private OnChangedListener mListener;
    private UserRepository mUserRepository;

    public UserModel(UserRepository repository){
        this.mUserRepository = repository;
    }

    public User getUser(String userId){
        return mUserRepository.getUser(userId);
    }

    public void updateUserName(String userId,String userName){
        User user = mUserRepository.updateUserName(userId,userName);
        if(null != mListener){
            mListener.onChange(user);
        }
    }

    public void setOnChangedListener(OnChangedListener listener){
        mListener = listener;
    }

    public interface OnChangedListener{
        void onChange(User user);
    }
}
