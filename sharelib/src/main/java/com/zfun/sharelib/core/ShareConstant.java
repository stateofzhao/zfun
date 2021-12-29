package com.zfun.sharelib.core;

import androidx.annotation.IntDef;

import com.zfun.sharelib.init.InitContext;
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

    //URL
    //todo me QZONE_REDIRECT_URL 是啥
    public static final String QZONE_REDIRECT_URL;

    //
    public final static int SHARE_TYPE_WX_FRIEND = 1;
    public final static int SHARE_TYPE_WX_CYCLE = 2;
    public final static int SHARE_TYPE_SINA_WEIBO = 3;
    public final static int SHARE_TYPE_QQ_ZONE = 5;
    public final static int SHARE_TYPE_QQ_FRIEND = 6;
    public final static int SHARE_TYPE_COPY_URL = 7;
    public final static int SHARE_TYPE_CHORUS_URL = 8;
    public final static int SHARE_TYPE_WX_MINI_PROGRAM = 12;//微信小程序分享

    public final static String SHARE_TAG_STR = "==share";

    static {
        final InitParams initParams = InitContext.getInstance().getInitParams();
        QQ_APP_ID = initParams.getQQ_APP_ID();
        QQ_SECRET = initParams.getQQ_SECRET();
        QZONE_REDIRECT_URL = initParams.getQZONE_REDIRECT_URL();
        QZONE_SCOPE = "all";
        WX_APP_ID = initParams.getWX_APP_ID();
        SINA_APP_KEY = initParams.getSINA_APP_KEY();
        SINA_REDIRECT_URL = initParams.getSINA_REDIRECT_URL();
        SINA_SCOPE = initParams.getSINA_SCOPE();
        fileProviderAuthorities = initParams.getFileProviderAuthorities();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SHARE_TYPE_WX_FRIEND, SHARE_TYPE_WX_CYCLE, SHARE_TYPE_SINA_WEIBO,SHARE_TYPE_QQ_ZONE
            , SHARE_TYPE_QQ_FRIEND, SHARE_TYPE_COPY_URL, SHARE_TYPE_CHORUS_URL,SHARE_TYPE_WX_MINI_PROGRAM
    })
    public @interface ShareType {
    }

    //--------分享用到的URL
    /** 复制链接/复制下载链接 的默认地址 */
    public final static String SHARE_DEFAULT_COPY_URL = "https://www.google.com";
    /** 分享默认图片URL */
    public final static String SHARE_DEFAULT_IMAGE = "http://image.kuwo.cn/mac/2013/default-play.jpg";

    //几个包名
    public final static String PACKAGE_TYPE_QQ = "com.tencent.mobileqq";
    public final static String PACKAGE_TYPE_WB = "com.sina.weibo";
    public final static String PACKAGE_TYPE_WX = "com.tencent.mm";

    //微信分享缩略图必须小于32k。。。坑。。。否则返回false
    public static final int MAX_THUMBDATA_SIZE = 0x8000;//单位byte
    public static final int MAX_MUSIC_VIDEO_THUMBDATA_SIZE = 1024*1024*1024;//大小限制1M

    /** 联网获取分享信息超时时间 */
    public static final int HTTP_TIME_OUT = 10000;
}
