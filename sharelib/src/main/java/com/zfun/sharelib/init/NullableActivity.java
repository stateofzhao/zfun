package com.zfun.sharelib.init;

import android.app.Activity;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Created by lzf on 2021/12/21 2:25 下午
 */
public class NullableActivity extends Activity {
    private static WeakReference<Activity> activityRef;

    @NonNull
    public static synchronized Activity get(){
        if(null == activityRef || null == activityRef.get()){
            activityRef = new WeakReference<>(new NullableActivity());
        }
        return activityRef.get();
    }
}
