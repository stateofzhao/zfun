package com.zfun.lib.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import com.zfun.lib.permission.core.Callback;
import com.zfun.lib.permission.core.IamUI;
import com.zfun.lib.permission.core.ManagerPermissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Android M(6.0) 以下版本使用。
 * <p/>
 * Created by lizhaofei on 2018/3/19 17:14
 */
public class BMAdapter implements ManagerPermissions.PlatformAdapter {
    private ManagerPermissions managerPermissions;

    public BMAdapter(ManagerPermissions managerPermissions) {
        this.managerPermissions = managerPermissions;
    }

    @Override
    public boolean requestPermission(Object obj, int requestCode, String[] permissions) {
        Activity activity = getActivity(obj);
        if (null == activity) {
            return false;
        }
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
        return true;
    }

    @Override
    public List<String> checkSelfPermission(Object obj, String[] permissions) {
        final List<String> denied = new ArrayList<>();
        Activity activity = getActivity(obj);
        if (null != activity) {
            PackageManager packageManager = activity.getPackageManager();
            String packageName = activity.getPackageName();
            for (String permission : permissions) {
                final int permissionResult =
                        packageManager.checkPermission(permission, packageName);
                if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                    denied.add(permission);
                }
            }
        }
        return denied;
    }

    @Override
    public void postCallback(final Object obj, @Nullable final Callback callback, final IamUI iamUI,
                             final int requestCode, final String[] permissions, final int[] grantResults) {
        if (null == grantResults || grantResults.length == 0) {
            if (null != callback) {
                callback.onCancel(requestCode);
            }
        } else {
            final List<String> denied = new ArrayList<>();//被拒绝的权限（包括 不再提示的）

            final int requestLength = grantResults.length;
            for (int i = 0; i < requestLength; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {//拒绝
                    final String permissionStr = permissions[i];
                    denied.add(permissionStr);
                }
            }
            if (denied.size() == 0) {
                if (null != callback) {
                    callback.onSuccess(requestCode);
                }
            } else {//只要有一项拒绝，那么直接去 设置页 即可，因为这个版本下没有让android系统打开权限的方法
                iamUI.showGoSettingsTip(permissions, new IamUI.OnClickCancel() {
                    @Override
                    public void onClick() {//取消
                        if (null != callback) {
                            callback.onFail(requestCode, permissions, grantResults);
                        }
                    }
                }, new IamUI.OnClickOk() {//去设置页
                    @Override
                    public void onClick() {
                        managerPermissions.startSettingsForResult(obj, requestCode,
                                permissions, callback, iamUI);
                    }
                });
            }
        }
    }

    private Activity getActivity(Object obj) {
        if (obj instanceof Fragment) {
            return ((Fragment) obj).getActivity();
        } else if (obj instanceof Activity) {
            return (Activity) obj;
        }
        return null;
    }
}
