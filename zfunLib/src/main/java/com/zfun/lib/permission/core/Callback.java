package com.zfun.lib.permission.core;


/**
 * 权限请求结果回调
 * <p/>
 * Created by zfun on 2018/3/14 11:42
 */
public interface Callback {
    //申请的全部权限都通过
    void onSuccess(int requestCode);

    //申请的部分/全部权限 没有通过
    void onFail(int requestCode, String[] permissions, int[] grantResults);

    ///**
    // * 此时用户，选择了拒绝，并且永不提示，那么你就应该弹出一个Dialog来说明理由，
    // * 并且需要跳转到设置页来让用户打开权限。
    // *
    // * @param permissions 用户拒绝的权限（包括 非不再提示的权限）
    // */
    //void onShowRequestPermissionRationale(Object activityOrFragment, int requestCode,
    //        String[] permissions);

    //取消了，这个是Android系统自动调用的（例如，Activity被回收了）
    void onCancel(int requestCode);
}
