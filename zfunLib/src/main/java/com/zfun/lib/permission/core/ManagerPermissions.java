package com.zfun.lib.permission.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import android.util.Log;
import com.zfun.lib.permission.BMAdapter;
import com.zfun.lib.permission.Permission;
import com.zfun.lib.permission.Util;
import java.util.ArrayList;
import java.util.List;

import static com.zfun.lib.permission.Permission.REQUEST_PERMISSION_SETTING;

//fixme lzf 考虑性能问题的话，这里可以去掉同步，改为必需在主线程中调用。

/**
 * 管理权限请求。
 *
 * 注意事项：
 * 1.只能有一个实例。
 * 2.必需在Activity/Fragment的销毁方法中调用{@link #onActivityOrFragmentDestroy(Object)}方法，否则可能造成Activity/Fragment泄露。
 *
 * <p/>
 * Created by lizhaofei on 2018/3/19 16:23
 */
public class ManagerPermissions {

    public interface PlatformAdapter {
        //如果可以执行请求返回true，不可以执行返回false
        boolean requestPermission(Object obj, int requestCode, String[] permissions);

        /**
         * 检测给定的权限是否有被拒绝的。
         *
         * @return 返回被拒绝的权限
         */
        List<String> checkSelfPermission(Object obj, String[] permissions);

        void postCallback(Object obj, Callback callback, IamUI iamUI, int requestCode,
                String[] permissions, int[] grantResults);
    }// PlatformAdapter end

    private static int count = 0;

    private final List<PermissionRequest> mRequests = new ArrayList<>();

    private PlatformAdapter mPlatAdapter;

    public ManagerPermissions() {
        count++;
        Util.check(count == 1);//有且只能有一个实例
    }

    public void setPlatformAdapter(PlatformAdapter adapter) {
        mPlatAdapter = adapter;
    }

    /**
     * 请求权限，会弹窗提示。
     *
     * @param obj 只接受两种类型 {@link Activity} 和 {@link Fragment}
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(final Object obj, final int requestCode,
            final String[] permissions, final Callback callback, final IamUI iamUI) {
        if (null == mPlatAdapter) {
            mPlatAdapter = new BMAdapter(this);
        }
        Log.e("permission", "当前请求权限的obj数：" + mRequests.size());
        final List<String> denyPermissions = checkSelfPermission(getActivity(obj), permissions);
        final boolean isCallingOnObj = isCallingPermissions(obj, permissions);
        if (isCallingOnObj) {//已经存在了请求，不应该这样
            if (null != callback) {
                callback.onCancel(requestCode);
            }
            return;
        }

        if (null != denyPermissions && denyPermissions.size() > 0) {
            final IamUI ui = null == iamUI ? Util.createDefaultIamUI(getContext(obj)) : iamUI;
            synchronized (mRequests) {
                addCallback(obj, callback, requestCode, permissions, ui);
            }
            ui.showRequestPermissionTip(denyPermissions.toArray(new String[denyPermissions.size()]),
                    new IamUI.OnClickCancel() {
                        @Override
                        public void onClick() {
                            synchronized (mRequests) {
                                PermissionRequest request = fetchRequestByObj(obj);
                                clearCallback(request);
                            }
                            if (null != callback) {
                                callback.onFail(requestCode, permissions,
                                        Util.createResultCode(permissions, denyPermissions));
                            }
                        }
                    }, new IamUI.OnClickOk() {
                        @Override
                        public void onClick() {
                            final boolean success =
                                    mPlatAdapter.requestPermission(obj, requestCode, permissions);
                            if (!success) {
                                synchronized (mRequests) {
                                    PermissionRequest request = fetchRequestByObj(obj);
                                    clearCallback(request);
                                }
                                if (null != callback) {
                                    callback.onCancel(requestCode);
                                }
                            }
                        }
                    });
        } else {
            if (null != callback) {
                callback.onSuccess(requestCode);
            }
        }
    }

    public void onRequestPermissionsResult(Object obj, int requestCode, String[] permissions,
            int[] grantResults) {
        synchronized (mRequests) {
            postCallback(obj, requestCode, permissions, grantResults);
        }
    }

    public void onActivityOrFragmentDestroy(Object obj) {
        PermissionRequest request = fetchRequestByObj(obj);
        if (null == request) {
            return;
        }
        synchronized (mRequests) {
            clearCallback(request);
        }
    }

    /** 仅仅检测是否授权，不会弹窗 */
    public List<String> checkSelfPermission(Object obj, String[] permissions) {
        return mPlatAdapter.checkSelfPermission(obj, permissions);
    }

    /** 接收设置页回来的结果，如果仍然有没有打开的权限会弹窗提示 */
    public void onActivityResult(final Object obj, int startActivityRequestCode,
            final int resultCode, Intent data) {
        if (startActivityRequestCode != REQUEST_PERMISSION_SETTING) {
            return;
        }
        PermissionRequest request = fetchRequestByObj(obj);
        if (null == request) {
            return;
        }
        final PermissionRequest requestNew = request.buildNew();
        synchronized (mRequests) {
            clearCallback(request);
        }
        final String[] requestPermissions = requestNew.permissions;
        final int permissionRequestCode = requestNew.requestCode;
        final Callback callback = requestNew.callback;
        final IamUI iamUI = requestNew.iamUI;

        final List<String> denied = mPlatAdapter.checkSelfPermission(obj, requestPermissions);
        if (null == denied || denied.size() == 0) {//全部授权通过
            if (null != callback) {
                callback.onSuccess(permissionRequestCode);
            }
        } else {//仍然有没有授权的权限
            requestPermissions(obj, permissionRequestCode, requestPermissions, callback, iamUI);//重新去授权
            //iamUI.showRequestPermissionTip(denied.toArray(new String[denied.size()]),
            //        new IamUI.OnClickCancel() {
            //            @Override
            //            public void onClick() {
            //                if (null != callback) {
            //                    callback.onFail(permissionRequestCode, requestPermissions,
            //                            Util.createResultCode(requestPermissions, denied));
            //                }
            //            }
            //        }, new IamUI.OnClickOk() {
            //            @Override
            //            public void onClick() {//重新去授权
            //                requestPermissions(obj, permissionRequestCode, requestPermissions,
            //                        callback, iamUI);
            //            }
            //        });
        }
    }

    /**
     * 打开设置页去让用户手动开启权限，不会弹窗，直接跳转到设置页。
     *
     * @param requestPermissionCode 请求权限的请求码，注意：不是 {@link Activity#startActivityForResult(Intent, int)}中的那个请求码！！
     * @param permissions 要到设置页让用户手动打开的权限
     * @param callback 接收结果的回调接口
     */
    public void startSettingsForResult(Object obj, int requestPermissionCode, String[] permissions,
            Callback callback, IamUI iamUI) {
        final Context context;
        if (obj instanceof Fragment) {
            context = ((Fragment) obj).getContext();
        } else {
            context = ((Activity) obj);
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        if (obj instanceof Fragment) {
            ((Fragment) obj).startActivityForResult(intent, Permission.REQUEST_PERMISSION_SETTING);
        } else {
            ((Activity) obj).startActivityForResult(intent, Permission.REQUEST_PERMISSION_SETTING);
        }
        synchronized (mRequests) {
            addCallback(obj, callback, requestPermissionCode, permissions, iamUI);
        }
    }

    //是否正在执行权限请求
    private boolean isCallingPermissions(Object obj, String[] permissions) {
        if (null == obj) {
            return false;
        }
        synchronized (mRequests) {
            final int size = mRequests.size();
            for (int i = size - 1; i >= 0; i--) {
                PermissionRequest request = mRequests.get(i);
                if (request.objRef == obj) {//已经存在了请求
                    return true;
                }
            }
        }
        return false;
    }

    //param obj 之所以要存储起来这个obj，是因为发起权限是调用的obj的方法，然后结果也会传递给这个obj，所以就可以通过这个obj来建立一个callback对应关系
    private void addCallback(Object obj, Callback callback, int requestCode, String[] permissions,
            IamUI iamUI) {
        Util.check(Thread.holdsLock(mRequests));
        evictRef();
        PermissionRequest request =
                new PermissionRequest(obj, callback, requestCode, permissions, iamUI);
        mRequests.add(request);
    }

    //param obj 根据obj来拿到回调接口
    private void postCallback(Object obj, int requestCode, String[] permissions,
            int[] grantResults) {
        Util.check(Thread.holdsLock(mRequests));

        PermissionRequest request = fetchRequestByObj(obj);
        if (null == request) {
            return;
        }
        final PermissionRequest newRequest = request.buildNew();
        clearCallback(request);

        //If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            mPlatAdapter.postCallback(obj, newRequest.callback, newRequest.iamUI, requestCode,
                    permissions, grantResults);
        } else {//empty 这里是系统取消
            if (null != newRequest.callback) {
                newRequest.callback.onCancel(requestCode);
            }
        }
    }

    //结果有可能为null
    private PermissionRequest fetchRequestByObj(Object obj) {
        PermissionRequest request = null;
        synchronized (mRequests) {
            final int size = mRequests.size();
            for (int i = 0; i < size; i++) {
                request = mRequests.get(i);
                if (null != request && request.objRef == obj) {
                    break;
                }
                request = null;
            }
        }
        return request;
    }

    //结果有可能为null
    private PermissionRequest fetchRequestByCallback(Callback callback) {
        PermissionRequest request = null;
        synchronized (mRequests) {
            final int size = mRequests.size();
            for (int i = 0; i < size; i++) {
                request = mRequests.get(i);
                if (null != request && request.callback == callback) {
                    break;
                }
                request = null;
            }
        }
        return request;
    }

    private void clearCallback(PermissionRequest request) {
        if (null == request) {
            return;
        }
        Util.check(Thread.holdsLock(mRequests));
        request.callback = null;
        request.objRef = null;
        evictRef();
    }

    private void evictRef() {
        Util.check(Thread.holdsLock(mRequests));
        final int size = mRequests.size();
        for (int i = size - 1; i >= 0; i--) {
            PermissionRequest request = mRequests.get(i);
            if (null == request) {
                mRequests.remove(i);
                continue;
            }
            if (null == request.callback) {
                mRequests.remove(i);
                continue;
            }
            if (null == request.objRef) {
                mRequests.remove(i);
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

    private Context getContext(Object obj) {
        if (obj instanceof Activity) {
            return (Activity) obj;
        } else if (obj instanceof Fragment) {
            return ((Fragment) obj).getContext();
        }
        return null;
    }

    private class PermissionRequest {
        private Object objRef;
        private Callback callback;
        private final int requestCode;
        private final String[] permissions;
        private IamUI iamUI;

        private PermissionRequest(Object obj, Callback callback, int requestCode,
                String[] permissions, IamUI iamUI) {
            this.objRef = obj;
            this.callback = callback;
            this.requestCode = requestCode;
            this.permissions = permissions;
            this.iamUI = iamUI;
            if (null == this.iamUI) {
                this.iamUI = Util.createDefaultIamUI(getContext(obj));
            }
        }

        private PermissionRequest buildNew() {
            return new PermissionRequest(objRef, callback, requestCode, permissions, iamUI);
        }
    }//PermissionRequest end
}
