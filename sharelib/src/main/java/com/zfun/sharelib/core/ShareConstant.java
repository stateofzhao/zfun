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

    //URL
    //todo lzf QZONE_REDIRECT_URL 是啥
    public static final String QZONE_REDIRECT_URL;

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
    public final static int LOGIN_WX = 21;//微信登录

    public final static String SHARE_TAG_STR = "==share";
    //--------分享用到的URL
    public final static String SHARE_MUSIC_URL = "https://m.kuwo.cn/yinyue/%1s?f=arphone&t=platform&isstar=";
    //请求歌曲播放地址的url，此链接后面拼接一个歌曲id即可
    public final static String GET_MUSIC_PLAY_URL = "http://antiserver.kuwo.cn/anti.s?type=convert_url2&format=aac%7Cmp3&response=url&needanti=0&rid=MUSIC_";
    //微信分享缩略图必须小于32k。。。坑。。。否则返回false
    public static final int MAX_WX_THUMBDATA_SIZE = 0x8000;//单位byte
    public final static String SHARE_MUSIC_DOWN_URL = "https://m.kuwo.cn/down/single/";
    public final static String SHARE_QZONE_MUSIC_URL =
            "http://player.kuwo.cn/webmusic/play?mid=MUSIC_";
    public final static String SHARE_MUSIC_DEFAULT_URL = "http://shouji.kuwo.cn";
    public final static String SHARE_MUSIC_WX_DEFAULT_URL = "http://shouji.kuwo.cn";
    public final static String SHARE_SONGLIST_URL =
            "https://m.kuwo.cn/?pid=songlistid&from=ar&t=plantform";
    public final static String SHARE_SONGLIST_DOWN_URL =
            "https://m.kuwo.cn/down/playlist/songlistid";
    public final static String SHARE_CD_SHOW_URL = "http://m.kuwo.cn/down/cdpack/cdid";
    public final static String SHARE_CD_CODE_URL = "http://m.kuwo.cn/newh5/cd/albumdetail?id=cdid";
    public final static String SHARE_CD_URL = "http://mobile.kuwo.cn/hzdown";
    public final static String SHARE_ALBUM_URL =
            "https://m.kuwo.cn/?albumid=album_id&from=ar&t=plantform";
    public final static String SHARE_ALBUM_DOWN_URL = "https://m.kuwo.cn/down/album/album_id";
    public final static String SHARE_MV_URL = "http://m.kuwo.cn/newh5app/mvplay/";
    public final static String SHARE_BILLBOARD_URL =
            "https://h5app.kuwo.cn/2000003/rank.html?id=";
    public final static String SHARE_KSING_URL =
            "http://kwsing.kuwo.cn/ksingnew/match/matchworks.htm?wid=";
    public final static String SHARE_OMNIBUS_URL =
            "http://kwsing.kuwo.cn/ksingnew/match/choiceness.htm?wlid=";
    //合唱分享url
    public final static String SHARE_CHORUS_PRUDUCT_URL =
            "http://kwsing.kuwo.cn/ksingnew/match/matchworks.htm?wid=";
    //半成品url
    public final static String SHARE_CHORUS_HALF_PRUDUCT_URL =
            "http://kwsing.kuwo.cn/ksingnew/match/chorus.htm?hid=";
    // 哔哔社区
    public final static String SHARE_TEMPLATE_AREA =
            "http://mobile.kuwo.cn/mpage/fspage/zhuanqu/index.html?mainid=";
    public final static String IMAGE_URL =
            "http://tingshu.kuwo.cn/tingshu/images/weixin_share1.jpg";
    public final static String SA_IMAGE_URL =
            "http://tingshu.kuwo.cn/tingshu/images/short_audio_share.png";
    public final static String SHORT_AUDIO_SHARE_URL = "http://tingshu.kuwo.cn/api/shareVoice.jsp?";
    public final static String SA_TITLE = "「嘘，他们正在聊...」";

    public final static String AUDIO_STREAM_URL = "https://h5app.kuwo.cn/3000021/fragment.html?id=";
    public final static String AUDIO_STREAM_TOPIC_URL = "https://h5app.kuwo.cn/8000001/audioTopic.html?id=";
    public static final int MAX_WX_MUSIC_VIDEO_THUMBDATA_SIZE = 1024 * 1024 * 1024;//大小限制1M
    //test 地址
//    public final static String GET_MUSIC_PLAY_URL =  "http://103.235.253.171:8089/anti.s?type=convert_url2&format=aac%7Cmp3&response=url&needanti=0&rid=MUSIC_";
    // 大众榜单分享
    public final static String SHARE_USER_BILLBOARD_URL = "http://m.kuwo.cn/h5app/sharerank/index?";
    public final static int SHARE_KSING_OMNIBUS_LIST = 10001;
    public final static int SHARE_KSING_FAMILY_OMNIBUS_LIST = 10002;
    public final static int SHARE_KSING_FAMILY = 10003;
    //音乐人分享
    public final static String SHARE_MUSICIAN_URL = "http://m.kuwo.cn/newh5/artist/artistDetail?id=";

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
        QZONE_REDIRECT_URL = initParams.getQZONE_REDIRECT_URL();
        QZONE_SCOPE = "all";
        WX_APP_ID = initParams.getWX_APP_ID();
        SINA_APP_KEY = initParams.getSINA_APP_KEY();
        SINA_REDIRECT_URL = initParams.getSINA_REDIRECT_URL();
        SINA_SCOPE = initParams.getSINA_SCOPE();
        fileProviderAuthorities = initParams.getFileProviderAuthorities();
        SHARE_DEFAULT_COPY_URL = TextUtils.isEmpty(initParams.getSHARE_DEFAULT_COPY_URL())?"https://m.kuwo.cn":initParams.getSHARE_DEFAULT_COPY_URL();
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

    public static final String WEIBO_KUWO = " (来自@酷我音乐)";
    public static final String WEIBO_MUSIC_MSG = "分享歌曲：《%s》(@酷我音乐)%s";
    public static final String SITE = "酷我音乐";

    // 微信小程序分享
    public static final String WX_SMALL_APP_USER_NAME = "gh_d418a168f8bd";
    public static final String WX_SMALL_APP_SONGLIST_PATH = "pages/songDetail/main?id=";
    public static final String WX_SMALL_APP_MUSIC_PATH = "pages/playMusic/main?id=";
    public static final int WX_SMALL_APP_THUMB_IMAGE_SIZE = 500; // 分享小程序封面图宽高
    public static final int WX_SMALL_APP_THUMB_MAX_STORAGE_SIZE = 0x20000; // 分享小程序封面图限制最大132k
    public static final String WX_SMALL_APP_SHARE_TYPE = "miniapp";
    //
    public static final int MUSIC_THUMB_SIZE = 100;//分享音乐，缩略图尺寸
    public static final int IMAGE_THUMB_SIZE = 150;//分享图片，缩略图尺寸
}
