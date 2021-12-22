package com.zfun.lib.util;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.zfun.funmodule.outhelper.ChannelReaderHelper;

/**
 * 多渠道信息读取。
 * <br/>
 * Created by zfun on 2021/12/15 5:52 PM
 */
public class ChannelUtil {

    public static String readChannel(@NonNull Context context){
        String apkPath = context.getPackageCodePath();
        Log.d("ChannelUtil - apkPath：", apkPath);
        return ChannelReaderHelper.readChannel(apkPath);
    }


}
