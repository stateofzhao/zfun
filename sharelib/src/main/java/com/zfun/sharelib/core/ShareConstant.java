package com.zfun.sharelib.core;

import android.text.TextUtils;

import androidx.annotation.IntDef;

import com.zfun.sharelib.init.InternalShareInitBridge;
import com.zfun.sharelib.init.InitParams;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 分享的一些配置信息
 * <p/>
 * Created by zfun on 2017/8/6 13:42
 */
public class ShareConstant {
    //SDK
    public final static String QQ_APP_ID;
    public final static String QQ_SECRET;
    public final static String QZONE_SCOPE;

    public final static String WX_APP_ID;

    public final static String SINA_APP_KEY;
    public final static String SINA_REDIRECT_URL;
    public final static String SINA_SCOPE;

    public final static String fileProviderAuthorities;//清单中注册的FileProvider的authorities属性

    public static final String WX_LOGIN_STATE_PRE = "zfun_wx_login";

    /**
     * 复制链接/复制下载链接 的默认地址
     */
    public static final String SHARE_DEFAULT_COPY_URL;
    /**
     * 分享默认图片URL
     */
    public static final String SHARE_DEFAULT_IMAGE;

    //
    public final static int SHARE_TYPE_WX_FRIEND = 1;
    public final static int SHARE_TYPE_WX_CYCLE = 2;
    public final static int SHARE_TYPE_SINA_WEIBO = 3;
    public final static int SHARE_TYPE_QQ_ZONE = 5;
    public final static int SHARE_TYPE_QQ_FRIEND = 6;
    public final static int SHARE_TYPE_COPY_URL = 7;
    public final static int SHARE_TYPE_CHORUS_URL = 8;

    public final static int SHARE_TYPE_WX_MINI_PROGRAM = 12;//微信小程序分享
    public final static int SHARE_TYPE_LOGIN_WX = 21;//微信登录
    public final static int SHARE_TYPE_LOGIN_QQ = 22;

    public final static String SHARE_TAG_STR = "==share";

    //几个包名
    public final static String PACKAGE_TYPE_QQ = "com.tencent.mobileqq";
    public final static String PACKAGE_TYPE_WB = "com.sina.weibo";
    public final static String PACKAGE_TYPE_WX = "com.tencent.mm";

    /**
     * 联网获取分享信息超时时间
     */
    public static final int HTTP_TIME_OUT = 10000;

    static {
        final InitParams initParams = InternalShareInitBridge.getInstance().getInitParams();
        QQ_APP_ID = initParams.getQQ_APP_ID();
        QQ_SECRET = initParams.getQQ_SECRET();
        QZONE_SCOPE = "all";
        WX_APP_ID = initParams.getWX_APP_ID();
        SINA_APP_KEY = initParams.getSINA_APP_KEY();
        SINA_REDIRECT_URL = initParams.getSINA_REDIRECT_URL();
        SINA_SCOPE = initParams.getSINA_SCOPE();
        fileProviderAuthorities = initParams.getFileProviderAuthorities();
        SHARE_DEFAULT_COPY_URL = TextUtils.isEmpty(initParams.getSHARE_DEFAULT_COPY_URL())?"https://www.baidu.com":initParams.getSHARE_DEFAULT_COPY_URL();
        SHARE_DEFAULT_IMAGE = TextUtils.isEmpty(initParams.getSHARE_DEFAULT_IMAGE_URL())?"http://image.kuwo.cn/mac/2013/default-play.jpg": initParams.SHARE_DEFAULT_IMAGE_URL;
    }
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SHARE_TYPE_WX_FRIEND, SHARE_TYPE_WX_CYCLE, SHARE_TYPE_SINA_WEIBO, SHARE_TYPE_QQ_ZONE
            , SHARE_TYPE_QQ_FRIEND, SHARE_TYPE_COPY_URL, SHARE_TYPE_CHORUS_URL, SHARE_TYPE_WX_MINI_PROGRAM,
            SHARE_TYPE_LOGIN_QQ
    })
    public @interface ShareType {
    }

    //
    public static final int WX_MUSIC_THUMB_SIZE = 100;//分享音乐，缩略图尺寸
    public static final int WX_MUSIC_VIDEO_THUMBDATA_MAX_SIZE = 1024 * 1024 * 1024;//大小限制1M

    public static final int WX_IMAGE_THUMB_SIZE = 150;//分享图片，缩略图尺寸
    public static final int WX_IMAGE_THUMBDATA_MAX_SIZE = 0x8000; //单位byte，微信分享缩略图必须小于32k。。。坑。。。否则返回false
}
