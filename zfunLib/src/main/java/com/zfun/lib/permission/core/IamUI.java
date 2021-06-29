package com.zfun.lib.permission.core;

/**
 * 提供每次权限请求的自定义显示。
 * <p/>
 * Created by lizhaofei on 2018/3/22 14:44
 */
public interface IamUI {

    /** 点击了提示中的 “去获取权限” */
    public interface OnClickOk {
        void onClick();
    }

    /** 点击了提示中的 “取消” */
    public interface OnClickCancel {
        void onClick();
    }

    /**
     * 显示授权弹框
     *
     * @param permissions 要申请的权限
     * @param onClickCancel 用户点击弹框中【取消】时的回调
     * @param onClickOk 用户点击弹窗中【去授权】的回调
     */
    void showRequestPermissionTip(String[] permissions, OnClickCancel onClickCancel,
            OnClickOk onClickOk);

    /**
     * 显示让用户去设置页打开权限的弹窗
     *
     * @param permissions 需要在设置页打开的权限
     * @param onClickCancel 用户点击弹框中【取消】时的回调
     * @param onClickOk 用户点击弹窗中【去授权】的回调
     */
    void showGoSettingsTip(String[] permissions, OnClickCancel onClickCancel, OnClickOk onClickOk);


}
