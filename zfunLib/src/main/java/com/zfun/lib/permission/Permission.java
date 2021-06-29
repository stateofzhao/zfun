package com.zfun.lib.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.Fragment;
import com.zfun.lib.permission.core.Callback;
import com.zfun.lib.permission.core.IamUI;
import com.zfun.lib.permission.core.ManagerPermissions;

import java.util.List;

/**
 * android系统权限管理工具。
 *
 * 下列权限使用必须动态申请，但是目前来看，只需要获取一组中的一个权限，那么android系统会给你整个组的权限（无需申请）：
 * <ul>
 * <li><strong>group:android.permission-group.CONTACTS</strong><br/>
 * permission:android.permission.WRITE_CONTACTS<br/>
 * permission:android.permission.GET_ACCOUNTS<br/>
 * permission:android.permission.READ_CONTACTS<br/>
 *
 * <li><strong>group:android.permission-group.PHONE</strong><br/>
 * permission:android.permission.READ_CALL_LOG<br/>
 * permission:android.permission.READ_PHONE_STATE<br/>
 * permission:android.permission.CALL_PHONE<br/>
 * permission:android.permission.WRITE_CALL_LOG<br/>
 * permission:android.permission.USE_SIP<br/>
 * permission:android.permission.PROCESS_OUTGOING_CALLS<br/>
 * permission:com.android.voicemail.permission.ADD_VOICEMAIL
 *
 * <li><strong>group:android.permission-group.CALENDAR</strong><br/>
 * permission:android.permission.READ_CALENDAR<br/>
 * permission:android.permission.WRITE_CALENDAR
 *
 * <li><strong>group:android.permission-group.CAMERA</strong><br/>
 * permission:android.permission.CAMERA
 *
 * <li><strong>group:android.permission-group.SENSORS</strong><br/>
 * permission:android.permission.BODY_SENSORS
 *
 * <li><strong>group:android.permission-group.LOCATION</strong><br/>
 * permission:android.permission.ACCESS_FINE_LOCATION<br/>
 * permission:android.permission.ACCESS_COARSE_LOCATION
 *
 * <li><strong>group:android.permission-group.STORAGE</strong><br/>
 * permission:android.permission.READ_EXTERNAL_STORAGE<br/>
 * permission:android.permission.WRITE_EXTERNAL_STORAGE
 *
 * <li><strong>group:android.permission-group.MICROPHONE</strong><br/>
 * permission:android.permission.RECORD_AUDIO
 *
 * <li><strong>group:android.permission-group.SMS</strong><br/>
 * permission:android.permission.READ_SMS<br/>
 * permission:android.permission.RECEIVE_WAP_PUSH<br/>
 * permission:android.permission.RECEIVE_MMS<br/>
 * permission:android.permission.RECEIVE_SMS<br/>
 * permission:android.permission.SEND_SMS<br/>
 * permission:android.permission.READ_CELL_BROADCASTS
 * </ul>
 * <p/>
 * Created by lizhaofei on 2018/3/12 15:14
 */
public class Permission {
    public static final int REQUEST_PERMISSION_SETTING = 65535;//打开设置页必须用这个 作为 RequestCode （不能大于65536，也不能为负数）

    /** {@link ManagerPermissions#requestPermissions(Object, int, String[], Callback, IamUI)} */
    public static void requestPermissions(Activity activity, int requestCode, String[] permissions,
            Callback callback, IamUI iamUI) {
        sRequestPermissions.requestPermissions(activity, requestCode, permissions, callback, iamUI);
    }

    /** {@link ManagerPermissions#requestPermissions(Object, int, String[], Callback, IamUI)} */
    public static void requestPermissions(Fragment fragment, int requestCode, String[] permissions,
            Callback callback, IamUI iamUI) {
        sRequestPermissions.requestPermissions(fragment, requestCode, permissions, callback, iamUI);
    }

    /** {@link ManagerPermissions#requestPermissions(Object, int, String[], Callback, IamUI)} */
    public static void requestPermissions(Activity activity, int requestCode, String[] permissions,
            Callback callback) {
        sRequestPermissions.requestPermissions(activity, requestCode, permissions, callback, null);
    }

    /** {@link ManagerPermissions#requestPermissions(Object, int, String[], Callback, IamUI)} */
    public static void requestPermissions(Fragment fragment, int requestCode, String[] permissions,
            Callback callback) {
        sRequestPermissions.requestPermissions(fragment, requestCode, permissions, callback, null);
    }

    /** 检测是否已经拥有了权限，只要有一项没有拥有就返回false */
    public static boolean checkSelfPermission(Activity activity, String[] permissions) {
        List<String> denied = sRequestPermissions.checkSelfPermission(activity, permissions);
        return null == denied || denied.size() == 0;
    }

    /** 检测是否已经拥有了权限，只要有一项没有拥有就返回false */
    public static boolean checkSelfPermission(Fragment fragment, String[] permissions) {
        List<String> denied = sRequestPermissions.checkSelfPermission(fragment, permissions);
        return null == denied || denied.size() == 0;
    }

    public static void onRequestPermissionResult(Fragment fragment, int requestCode,
            String[] permissions, int[] grantResults) {
        sRequestPermissions.onRequestPermissionsResult(fragment, requestCode, permissions,
                grantResults);
    }

    public static void onRequestPermissionResult(Activity activity, int requestCode,
            String[] permissions, int[] grantResults) {
        sRequestPermissions.onRequestPermissionsResult(activity, requestCode, permissions,
                grantResults);
    }

    public static void onActivityDestroy(Activity activity) {
        sRequestPermissions.onActivityOrFragmentDestroy(activity);
    }

    public static void onFragmentDestroy(Fragment fragment) {
        sRequestPermissions.onActivityOrFragmentDestroy(fragment);
    }

    public static void onActivityResult(Activity activity, int requestCode, int resultCode,
            Intent data) {
        sRequestPermissions.onActivityResult(activity, requestCode, resultCode, data);
    }

    public static void onActivityResult(Fragment fragment, int requestCode, int resultCode,
            Intent data) {
        sRequestPermissions.onActivityResult(fragment, requestCode, resultCode, data);
    }

    private static final ManagerPermissions sRequestPermissions = findRequestPermission();

    private static ManagerPermissions findRequestPermission() {
        ManagerPermissions result = new ManagerPermissions();
        ManagerPermissions.PlatformAdapter adapter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            adapter = new MAdapter(result);
        } else {
            adapter = new BMAdapter(result);
        }
        result.setPlatformAdapter(adapter);
        return result;
    }
}
