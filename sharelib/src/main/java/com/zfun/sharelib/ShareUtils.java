package com.zfun.sharelib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.zfun.sharelib.init.InitContext;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.io.File;

/**
 * Created by lzf on 2021/12/22 2:13 下午
 */
public class ShareUtils {

    public static boolean isSupportSmallAppShare() {
        IWXAPI api = ShareMgrImpl.getInstance().getWxApi();
        return api != null && api.getWXAppSupportAPI() >= Build.MINIPROGRAM_SUPPORTED_SDK_INT;
    }

    public static boolean isSupportMusicVideoShare(){
        IWXAPI api = ShareMgrImpl.getInstance().getWxApi();
        return api != null && api.getWXAppSupportAPI() >= 0x28000000;
    }

    public static String getShareFilePath(Context context, File file, String packageName){
        Uri uri = getUriForFile(context,file);
        context.grantUriPermission(packageName,uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return uri.toString();
    }

    public static Uri getUriForFile(Context context, File file) {
        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                uri = FileProvider.getUriForFile(context.getApplicationContext(), InitContext.getInstance().getInitParams().getFileProviderAuthorities(), file);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                uri = null;
            }
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}
