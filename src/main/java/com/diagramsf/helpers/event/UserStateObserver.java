package com.diagramsf.helpers.event;

/** 用户状态订阅者 */
public interface UserStateObserver {

    void userLoginSucceed();

    void userLoginOut();
}
