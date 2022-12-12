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

    public static final int MAX_WX_MUSIC_VIDEO_THUMBDATA_SIZE = 1024 * 1024 * 1024;//大小限制1M

    // 分享菜单的配置
    public static final int SONG_LIST_CARD = -1;//歌单卡片分享不显示QQ空间
    public static final int TEMPLATE_AREA_CARD = -2;//歌单卡片分享不显示QQ空间
    public static final int KSING_CHORUS_SHARE = -3;//邀请好友来合唱
    public static final int GAME_SHARE = -4;//游戏分享不显示QQ空间 和复制链接
    public static final int OTHER_SHARE = 0;
    public static final int KSING_STORY_SHARE = -5;//分享音乐故事类型，不显示复制链接
    public static final int FEED_RECOMMEND = -6;  //视频Feed流列表页中显示的分享，不包括视听、下载、酷我三个
    public static final int AUDIO_STREAM_TOPIC = -7;  //音乐片段
    public static final int PLAY_PAGE = -8;  //播放页不显示复制视听，显示音乐片段，歌词海报俩入口
    public static final int LIBRARY_MUSIC = -9; //歌单 歌手的分享，添加音乐片段入口，不显示复制试听
    public static final int AD_SHARE = -10;//网页广告分享
    public static final int WX_MUSIC_THUMB_SIZE = 100;//分享音乐，缩略图尺寸

    //几个包名
    public final static String PACKAGE_TYPE_QQ = "com.tencent.mobileqq";
    public final static String PACKAGE_TYPE_WB = "com.sina.weibo";
    public final static String PACKAGE_TYPE_WX = "com.tencent.mm";
    public static final int WX_IMAGE_THUMB_SIZE = 150;//分享图片，缩略图尺寸
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
            , SHARE_TYPE_QQ_FRIEND, SHARE_TYPE_COPY_URL, SHARE_TYPE_CHORUS_URL, SHARE_TYPE_WX_MINI_PROGRAM
    })
    public @interface ShareType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SONG_LIST_CARD, TEMPLATE_AREA_CARD, KSING_CHORUS_SHARE, GAME_SHARE, OTHER_SHARE,
            KSING_STORY_SHARE, FEED_RECOMMEND, AUDIO_STREAM_TOPIC, PLAY_PAGE, AD_SHARE})
    public @interface MenuType {
    }

    //
    public static final int MUSIC_THUMB_SIZE = 100;//分享音乐，缩略图尺寸
    public static final int IMAGE_THUMB_SIZE = 150;//分享图片，缩略图尺寸
}
