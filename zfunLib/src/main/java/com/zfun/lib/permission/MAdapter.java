package com.zfun.lib.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.zfun.lib.permission.core.Callback;
import com.zfun.lib.permission.core.IamUI;
import com.zfun.lib.permission.core.ManagerPermissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Android M(6.0)及以上版本使用。
 * <p/>
 * Created by lizhaofei on 2018/3/19 17:13
 */
public class MAdapter implements ManagerPermissions.PlatformAdapter {
    private ManagerPermissions managerPermissions;

    MAdapter(ManagerPermissions managerPermissions) {
        this.managerPermissions = managerPermissions;
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public boolean requestPermission(Object obj, int requestCode, String[] permissions) {
        if (obj instanceof Activity) {
            ((Activity) obj).requestPermissions(permissions, requestCode);
        } else if (obj instanceof Fragment) {
            ((Fragment) obj).requestPermissions(permissions, requestCode);
        } else {
            throw new IllegalArgumentException(
                    null != obj ? obj.getClass().getName() : "null" + " is not supported");
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public List<String> checkSelfPermission(Object obj, String[] permissions) {
        final List<String> denied = new ArrayList<>();
        final Activity activity = getActivity(obj);
        if (null == activity) {
            return denied;
        }
        for (String permission : permissions) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                denied.add(permission);
            }
        }
        return denied;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void postCallback(final Object obj, @Nullable final Callback callback, final IamUI iamUI, final int requestCode,
                             final String[] permissions, final int[] grantResults) {
        if (grantResults.length > 0) {
            final List<String> deniedAndNoShow = new ArrayList<>();//被拒绝并且不再提示
            final List<String> denied = new ArrayList<>();//被拒绝的权限（包括 不再提示的）

            final Activity activity = getActivity(obj);
            final int requestLength = grantResults.length;
            for (int i = 0; i < requestLength; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {//拒绝
                    final String permissionStr = permissions[i];
                    denied.add(permissionStr);
                    if (null != activity && !activity.shouldShowRequestPermissionRationale(
                            permissionStr)) {//不再提示
                        deniedAndNoShow.add(permissionStr);
                    }
                }
            }
            final int deniedAndNoShowSize = deniedAndNoShow.size();

            if (denied.size() == 0) {//全部授权
                if (null != callback) {
                    callback.onSuccess(requestCode);
                }
            } else {//没有全部授权
                if (deniedAndNoShowSize > 0) {//有【拒绝不再提示】状态 提示用户去设置页打开【所有拒绝的权限】的弹窗
                    iamUI.showGoSettingsTip(denied.toArray(new String[denied.size()]), new IamUI.OnClickCancel() {
                        @Override
                        public void onClick() {
                            if(null != callback){
                                callback.onFail(requestCode, permissions, grantResults);
                            }
                        }
                    }, new IamUI.OnClickOk() {
                        @Override
                        public void onClick() {
                            managerPermissions.startSettingsForResult(obj, requestCode, permissions,
                                    callback, iamUI);
                        }
                    });
                } else {
                    if(null != callback){
                        callback.onFail(requestCode, permissions, grantResults);
                    }
                }
            }
        } else {//empty 这里是系统取消
            if(null != callback) {
                callback.onCancel(requestCode);
            }
        }
    }

    private Activity getActivity(Object obj) {
        if (obj instanceof Activity) {
            return (Activity) obj;
        } else if (obj instanceof Fragment) {
            return ((Fragment) obj).getActivity();
        }
        return null;
    }
}
