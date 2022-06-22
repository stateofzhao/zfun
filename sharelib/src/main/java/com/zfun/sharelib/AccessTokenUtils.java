package com.zfun.sharelib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.zfun.sharelib.core.ShareConstant;
import com.zfun.sharelib.type.QzoneOAuthV2;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * Seriously, you should say something about your code
 * Author: hongze
 * Date: 13-9-18
 * Time: 下午5:19
 */
public class AccessTokenUtils {
    private static final String TAG = "AccessTokenUtils";

    public static final String SOURCE_SINA = "sina";

    public static final String SOURCE_TWEIBO = "tweibo";

    public static final String SOURCE_QZONE = "qzone";

    public static final String SINA_WEIBO_PREFERENCES = "com_weibo_sdk_android";

    public static final String TENCENT_WEIBO_PREFERENCES = "tencent_weibo";

    public static final String TENCENT_QZONE_PREFERENCES = "tencent_qzone";

    public static final String CB_STATUS_MARK = "cb_status_mark";


    /**
     * 保存新浪accesstoken到SharedPreferences
     *
     * @param context Activity 上下文环境
     * @param token   Oauth2AccessToken
     */
    public static void keepAccessToken(Context context, Oauth2AccessToken token) {
        if (context == null) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(SINA_WEIBO_PREFERENCES, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token.getAccessToken());
        editor.putLong("expiresTime", token.getExpiresTime());
        editor.apply();

        SharedPreferences sp = context.getSharedPreferences(CB_STATUS_MARK, Context.MODE_PRIVATE);
        SharedPreferences.Editor cbEditor = sp.edit();
        cbEditor.putBoolean("sina_weibo", true);
        cbEditor.putLong("sina_weibo_expires", token.getExpiresTime());
        cbEditor.apply();
    }

    /**
     * 保存新浪微博的UID
     * @param context Activity 上下文环境
     * @param wbUid 新浪微博的UID
     */
    public static void keepAccessUid(Context context, String wbUid) {
        SharedPreferences pref = context.getSharedPreferences(SINA_WEIBO_PREFERENCES, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("friend_rec_weibo_uid", wbUid);
        editor.apply();

    }

    /**
     * 获取新浪微博的UID
     * @param context Activity 上下文环境
     */
    public static String readAccessUid(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SINA_WEIBO_PREFERENCES, Context.MODE_APPEND);
        return pref.getString("friend_rec_weibo_uid", "");
    }

    /**
     * 从SharedPreferences读取新浪accessstoken
     *
     * @param context
     * @return Oauth2AccessToken
     */
    public static Oauth2AccessToken readAccessToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SINA_WEIBO_PREFERENCES, Context.MODE_APPEND);
        Bundle bundle = new Bundle();
        bundle.putString("access_token", pref.getString("token", ""));
        bundle.putString("expires_in", String.valueOf(pref.getLong("expiresTime", 0) / 1000));
        return Oauth2AccessToken.parseAccessToken(bundle);
    }


    /**
     * 公共方法，清空sharepreference
     *
     * @param context
     * @param preference
     */
    public static void clear(Context context, String preference) {
        SharedPreferences pref = context.getSharedPreferences(preference, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        SharedPreferences sp = context.getSharedPreferences(CB_STATUS_MARK, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sp.edit();
        if (SINA_WEIBO_PREFERENCES.equals(preference)){
            prefEditor.putBoolean("sina_weibo", false);
            prefEditor.putString(SOURCE_SINA+"uid","");
        }
        else if (TENCENT_WEIBO_PREFERENCES.equals(preference)){
            prefEditor.putBoolean("tencent_weibo", false);
            prefEditor.putString(SOURCE_TWEIBO+"uid","");
        }
        else if (TENCENT_QZONE_PREFERENCES.equals(preference)){
            prefEditor.putBoolean("tencent_qzone", false);
            prefEditor.putString(SOURCE_QZONE+"uid","");
        }
        prefEditor.apply();

    }

    /**
     * 从SharedPreferences读取QQ空间授权信息
     *
     * @param context
     * @return OAuthV2
     */
    public static QzoneOAuthV2 doReadTencentQzoneToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(TENCENT_QZONE_PREFERENCES, Context.MODE_APPEND);
        QzoneOAuthV2 oAuth = new QzoneOAuthV2();
        oAuth.redirectUri = ShareConstant.QZONE_REDIRECT_URL;
        oAuth.clientId =  ShareConstant.QQ_APP_ID;
        oAuth.clientSecret = ShareConstant.QQ_SECRET;
        oAuth.accessToken = pref.getString("access_token", "");
        oAuth.openId = pref.getString("openid", "");
        oAuth.expiresIn = pref.getString("expiresin", "");
        return oAuth;
    }

    /**
     * 保存QQ空间授权信息到SharedPreferences
     *
     * @param context
     * @param oAuth
     */
    public static void doSaveTencentQzoneToken(Context context, QzoneOAuthV2 oAuth) {
        SharedPreferences pref = context.getSharedPreferences(TENCENT_QZONE_PREFERENCES, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("access_token", oAuth.accessToken);
        editor.putString("expiresin", oAuth.expiresIn);
        editor.putString("openid", oAuth.openId);
        editor.apply();

        SharedPreferences sp = context.getSharedPreferences(CB_STATUS_MARK, Context.MODE_PRIVATE);
        SharedPreferences.Editor cbEditor = sp.edit();
        cbEditor.putBoolean("tencent_qzone", true);
        cbEditor.putString("tencent_qzone_expires", oAuth.expiresIn);
        cbEditor.apply();
    }

    public static void doSaveUserInfoByType(Context context,String uid,String name ,String type){
        SharedPreferences sp = context.getSharedPreferences(CB_STATUS_MARK, Context.MODE_PRIVATE);
        SharedPreferences.Editor cbEditor = sp.edit();
        if(!TextUtils.isEmpty(uid)){
            cbEditor.putString(type+"uid",uid);
        }
        cbEditor.putString(type+"name",name);
        cbEditor.apply();
    }

    public static String getUidByType(Context context,String type){
        SharedPreferences sp = context.getSharedPreferences(CB_STATUS_MARK, Context.MODE_PRIVATE);
        return sp.getString(type+"uid","");
    }

    public static boolean isSessionValid(String expiresIn) {
        if ("".equals(expiresIn) || expiresIn == null) {
            return false;
        }
        Long expireMillis = System.currentTimeMillis() + Long.parseLong(expiresIn) * 1000;
        return expireMillis > System.currentTimeMillis();
    }
}
