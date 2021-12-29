package com.zfun.sharelib;

import androidx.annotation.NonNull;

import java.util.Observable;

/**
 * Created by lzf on 2021/12/21 5:46 下午
 */
public class LiveState {

    @NonNull
    public Observable observable(){
        return observable;
    }

    public void setNetAvailable(boolean available){
        isNetAvailable = available;
        observable.notifyObservers();
    }

    public boolean isNetAvailable(){
        return isNetAvailable;
    }

    private boolean isNetAvailable;
    private final Observable observable;

    public static LiveState getInstance(){
        return LiveStateHolder.liveState;
    }
    private LiveState(){
        observable = new Observable();
    }

    public static class LiveStateHolder{
        private final static LiveState liveState = new LiveState();
    }//
}
