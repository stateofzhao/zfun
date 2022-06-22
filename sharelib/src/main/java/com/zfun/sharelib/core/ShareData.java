package com.zfun.sharelib.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.zfun.sharelib.ShareMgrImpl;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.zfun.sharelib.SsoFactory;
import com.zfun.sharelib.init.InternalShareInitBridge;

/**
 * 分享数据
 * <p/>
 * Created by lizhaofei on 2017/8/6 8:57
 */
public class ShareData {

    public interface OnShareListener {
        void onSuccess();

        void onFail();

        void onCancel();
    }//OnShareResult end

    private QQ mQQShareData;
    private QQZone mQQZShareData;
    private Wx mWxShareData;
    private SinaWeibo mSinaShareData;

    //复制链接，复制下载链接 用到的
    private String mUrl;

    private WeakReference<Activity> mCompelContext;
    private WeakReference<IWBAPI> mWBAPI;

    public OnShareListener mShareListener;

    /**
     * 构建分享到QQ好友的 纯图片数据
     */
    public QQImageBuilder buildQQImage() {
        return new QQImageBuilder();
    }

    /**
     * 构建分享到QQ好友的 图文分享数据
     */
    public QQTextImageBuilder buildQQTImage() {
        return new QQTextImageBuilder();
    }

    /** 构建分享到QQ好友的 音乐分享数据 */
    public QQAudioBuilder buildQQAudio() {
        return new QQAudioBuilder();
    }

    /** 构建QQ空间 图文分享数据 */
    public QZoneImageTextBuilder buildQZoneTImage() {
        return new QZoneImageTextBuilder();
    }

    /** 构建微信分项数据 */
    public WXBuilder buildWx() {
        return new WXBuilder();
    }

    /** 构建新浪微博蚊香数据 */
    public SinaWbBuilder buildSinaWb() {
        return new SinaWbBuilder();
    }

    /** 分享数据是否为空 */
    public boolean isEmpty() {
        return null == mQQShareData
                && null == mQQZShareData
                && null == mWxShareData
                && null == mSinaShareData
                && TextUtils.isEmpty(mUrl);
    }

    public void setCopyUrl(String url) {
        mUrl = url;
    }

    /** 设置让{@link IShareHandler} 强制使用的Activity，这样能够让IShareHandler强制使用此处设置的Activity进行分享操作 */
    public void setCompelContext(Activity activity) {
        mCompelContext = new WeakReference<>(activity);
    }

    /**
     * 设置新浪微博授权使用的{@link IWBAPI}，
     * 如果不设置的话会默认使用 {@link SsoFactory#getWBAPI(Context)} 来获取{@link IWBAPI}，
     * 这样如果发起分享的话，结果回调方法不在{@link InternalShareInitBridge#getHostActivity()}中，
     * 需要你自己来处理分享回调。
     */
    public void setWBAPI(IWBAPI wbapi) {
        mWBAPI = new WeakReference<>(wbapi);
    }

    public void setOnShareListener(OnShareListener listener) {
        mShareListener = listener;
    }

    public String getCopyUrl() {
        return mUrl;
    }

    /** 获取 qq空间 分享数据 */
    public QQZone getQqZShareData() {
        return mQQZShareData;
    }

    /** 获取 QQ好友 分项数据 */
    public QQ getQqFrendData() {
        return mQQShareData;
    }

    public Wx getWxShareData() {
        return mWxShareData;
    }

    public SinaWeibo getSinaShareData() {
        return mSinaShareData;
    }

    @Nullable
    public Activity getCompelContext() {
        return null != mCompelContext ? mCompelContext.get() : null;
    }

    /** 参见 {@link #setWBAPI(IWBAPI)} */
    @Nullable
    public IWBAPI getWBAPI() {
        return null != mWBAPI ? mWBAPI.get() : null;
    }

    //分享到QQ好友的数据
    public class QQ {
        /**
         * 分享类型，包括图文分享，纯图片分享，音乐分享等 参见{@link QQShare#SHARE_TO_QQ_TYPE_IMAGE}
         * ，{@link QQShare#SHARE_TO_QQ_TYPE_AUDIO}，{@link
         * QQShare#SHARE_TO_QQ_TYPE_DEFAULT}
         */
        public final int shareType;
        public final String title;
        public final String summary;
        public final String targetUrl;
        public final String imageURLorFilePath;
        public final String appName;
        public final String site;
        public final String audioUrl;
        public final int ext;

        private QQ(int shareType, String title, String targetUrl, String summary, String imageURLorFilePath,
                String appName, String site, String audioUrl, int ext) {
            this.shareType = shareType;
            this.title = title;
            this.targetUrl = targetUrl;
            this.summary = summary;
            this.imageURLorFilePath = imageURLorFilePath;
            this.appName = appName;
            this.site = site;
            this.audioUrl = audioUrl;
            this.ext = ext;
        }

        public void share(){
            ShareMgrImpl.getInstance().share(ShareConstant.SHARE_TYPE_QQ_FRIEND, ShareData.this);
        }
    }//QQ end

    //分享到QQ空间的数据
    public class QQZone {
        /** 目前仅支持图文分享 */
        public final int shareType;
        public final String title;
        public final String summary;
        public final String site;
        public final String targetUrl;
        public final ArrayList<String> imageUrlOrFilePath;

        /**
         * @param imageUrlOrFilePath 不支持混合传递，要么全是URL，要么全是文件路径
         * */
        private QQZone(int shareType, String title, String summary, String site,
                       ArrayList<String> imageUrlOrFilePath, String targetUrl) {
            this.shareType = shareType;
            this.title = title;
            this.summary = summary;
            this.site = site;
            this.imageUrlOrFilePath = imageUrlOrFilePath;
            this.targetUrl = targetUrl;
        }

        private QQZone(String title, String summary, String site, ArrayList<String> imageUrlOrFilePath, String targetUrl) {
            this(QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT, title, summary, site, imageUrlOrFilePath, targetUrl);
        }

        public void share(){
            ShareMgrImpl.getInstance().share(ShareConstant.SHARE_TYPE_QQ_ZONE, ShareData.this);
        }
    }//QQZone end

    public class Wx {
        public final static int TYPE_TEXT = 1;
        public final static int TYPE_IMAGE = 2;
        public final static int TYPE_MUSIC = 3;
        public final static int TYPE_VIDEO = 4;
        public final static int TYPE_WEB = 5;//网页分享
        public final static int TYPE_SMALL_APP = 6;//小程序类型分享，暂未实现
        public final static int TYPE_MUSIC_VIDEO = 7;//音乐视频类型分享

        //通用
        public int type = -1;

        //文本
        public String text;

        //图片
        public Bitmap bmp;//要分享的图片

        //图片/网页/音乐
        public byte[] thumbData;//缩略图

        //音乐/视频/网页
        public String title;
        public String description;

        //音乐
        public String musicUrl;
        public String musicDataUrl;

        //视频
        public String videoUrl;

        //音乐视频类型
        public String singerName;//不能为空
        public int duration;//时长，单位毫秒，必有
        public String songLyric;//歌词，标准的歌词格式字符串，可以为空
        public String hdAlbumThumbFilePath;//高清专辑图本地路径，文件限制长度不超过1MB
        public String albumName;//音乐专辑名
        public String musicGenre;//音乐流派
        public long issueDate;//发行时间，Unix时间戳，单位秒
        public String identification;//音乐标识符，用户在微信音乐播放器跳回应用时会携带该参数，可用于唯一标识一首歌，微信侧不理解

        //网页
        public String webpageUrl;

        //小程序
        public String userName; // 小程序原始id,获取方法：登录小程序管理后台-设置-基本设置-帐号信息
        public String path; // 小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"

        private Wx(){}

        //1 微信好友；2微信朋友圈；3微信小程序
        private void share(int where){
            ShareMgrImpl.getInstance().share(where, ShareData.this);
        }

        public void share2Friend(){
            share(ShareConstant.SHARE_TYPE_WX_FRIEND);
        }

        public void share2Timeline(){
            share(ShareConstant.SHARE_TYPE_WX_CYCLE);
        }

        public void share2MiniProgram(){
            share(ShareConstant.SHARE_TYPE_WX_MINI_PROGRAM);
        }
    }//Wx end

    public class SinaWeibo {
        public final String msg;
        public final String imagePath;
        public final Bitmap bitmap;

        private SinaWeibo(String msg, String imagePath, Bitmap bitmap) {
            this.msg = msg;
            this.imagePath = imagePath;
            this.bitmap = bitmap;
        }

        public void share(){
            ShareMgrImpl.getInstance().share(ShareConstant.SHARE_TYPE_SINA_WEIBO, ShareData.this);
        }
    }//SinaWeibo end

    //构建 QQ图文分享 的数据
    public class QQTextImageBuilder {
        private String title;
        private String targetUrl;
        private String summary;
        private String imageURLorFilePath;
        private String appName;
        private String site;
        private int ext = -1;

        private QQTextImageBuilder() {
        }

        public QQTextImageBuilder title(String title) {
            this.title = title;
            return this;
        }

        public QQTextImageBuilder targetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public QQTextImageBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }

        /**
         * @param imageURLorFilePath 本地or网络
         */
        public QQTextImageBuilder imageURLorFilePath(String imageURLorFilePath) {
            this.imageURLorFilePath = imageURLorFilePath;
            return this;
        }

        public QQTextImageBuilder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public QQTextImageBuilder site(String site) {
            this.site = site;
            return this;
        }

        public QQTextImageBuilder ext(int ext) {
            this.ext = ext;
            return this;
        }

        public QQ build() {
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(targetUrl)) {
                throw new IllegalArgumentException("title or targetUrl must not null!");
            }
            mQQShareData =  new QQ(QQShare.SHARE_TO_QQ_TYPE_DEFAULT, title, targetUrl, summary, imageURLorFilePath,
                            appName, site, null, ext);
            return mQQShareData;
        }
    }//QQTextImageBuilder end

    //构建 QQ纯图片分享 的数据
    public class QQImageBuilder {
        private String appName;
        private String imageLocalUri;//只能是本地图片路径
        private int ext;

        private QQImageBuilder() {
        }

        public QQImageBuilder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public QQImageBuilder imageLocalUri(String imageLocalUri) {
            this.imageLocalUri = imageLocalUri;
            return this;
        }

        public QQImageBuilder ext(int ext) {
            this.ext = ext;
            return this;
        }

        public QQ build() {
            if (TextUtils.isEmpty(imageLocalUri)) {
                throw new IllegalArgumentException("imageLocalUri must not null!");
            }
            mQQShareData = new QQ(QQShare.SHARE_TO_QQ_TYPE_IMAGE, null, null, null, imageLocalUri, appName,
                            null, null, ext);
            return mQQShareData;
        }
    }//QQImageBuilder end

    //构建 QQ分享音乐 的数据
    public class QQAudioBuilder {
        private String title;
        private String targetUrl;
        private String summary;
        private String imageURLorFilePath;
        private String appName;
        private String site;
        private int ext = -1;

        private String audioUrl;

        private QQAudioBuilder() {
        }

        public QQAudioBuilder title(String title) {
            this.title = title;
            return this;
        }

        public QQAudioBuilder targetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public QQAudioBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }

        /**
         * @param imageURLorFilePath 本地or网络
         */
        public QQAudioBuilder imageURLorFilePath(String imageURLorFilePath) {
            this.imageURLorFilePath = imageURLorFilePath;
            return this;
        }

        public QQAudioBuilder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public QQAudioBuilder site(String site) {
            this.site = site;
            return this;
        }

        public QQAudioBuilder ext(int ext) {
            this.ext = ext;
            return this;
        }

        public QQAudioBuilder audioUrl(String audioUrl) {
            this.audioUrl = audioUrl;
            return this;
        }

        public QQ build() {
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(targetUrl) || TextUtils.isEmpty(audioUrl)) {
                throw new IllegalArgumentException("title or targetUrl or audioUrl must not null!");
            }
            mQQShareData = new QQ(QQShare.SHARE_TO_QQ_TYPE_AUDIO, title, targetUrl, summary, imageURLorFilePath,
                    appName, site, audioUrl, ext);
            return mQQShareData;
        }
    }//QQAudioBuilder end

    //构建 QQ空间图文分享 的数据
    public class QZoneImageTextBuilder {
        private String title;
        private String summary;
        private String targetUrl;
        private String site;
        private ArrayList<String> netImageUrls;
        private ArrayList<String> localImagePaths;

        private QZoneImageTextBuilder() {
        }

        public QZoneImageTextBuilder title(String title) {
            this.title = title;
            return this;
        }

        public QZoneImageTextBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public QZoneImageTextBuilder targetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public QZoneImageTextBuilder site(String site) {
            this.site = site;
            return this;
        }

        /**
         * @param imageNetUrls 不能本地和网络图片混着来，只能是一种类型（要么全是本地，要么全是网络）
         */
        public QZoneImageTextBuilder imageNetUrls(ArrayList<String> imageNetUrls) {
            this.netImageUrls = imageNetUrls;
            return this;
        }

        /**
         * @param imageLocalPaths 不能本地和网络图片混着来，只能是一种类型（要么全是本地，要么全是网络）
         */
        public QZoneImageTextBuilder imageLocalPaths(ArrayList<String> imageLocalPaths) {
            this.localImagePaths = imageLocalPaths;
            return this;
        }

        public ShareData.QQZone build() {
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(targetUrl)) {
                throw new IllegalArgumentException("title or targetUrl must not null!");
            }
            final ArrayList<String> images;
            if(null != netImageUrls && !netImageUrls.isEmpty()){
                images = netImageUrls;
            } else {
                images = localImagePaths;
            }
            mQQZShareData = new QQZone(title, summary, site, images, targetUrl);
            return mQQZShareData;
        }
    }//QZoneImageTextBuilder end

    //构建 微信 分享数据
    public class WXBuilder {
        Wx wx;

        private WXBuilder() {
            wx = new Wx();
        }

        public TextBuilder typeText() {
            checkType();
            wx.type = Wx.TYPE_TEXT;
            return new TextBuilder();
        }

        public ImageBuilder typeImage() {
            checkType();
            wx.type = Wx.TYPE_IMAGE;
            return new ImageBuilder();
        }

        public MusicBuilder typeMusic() {
            checkType();
            wx.type = Wx.TYPE_MUSIC;
            return new MusicBuilder();
        }

        public VideoBuilder typeVideo() {
            checkType();
            wx.type = Wx.TYPE_VIDEO;
            return new VideoBuilder();
        }

        public WebBuilder typeWeb() {
            checkType();
            wx.type = Wx.TYPE_WEB;
            return new WebBuilder();
        }

        public SmallAppBuilder typeSmallApp() {
            checkType();
            wx.type = Wx.TYPE_SMALL_APP;
            return new SmallAppBuilder();
        }

        public MusicVideoBuilder typeMusicVideo(){
            checkType();
            wx.type = Wx.TYPE_MUSIC_VIDEO;
            return new MusicVideoBuilder();
        }

        private void checkType() {
            if (wx.type != -1) {//证明已经设置了类型
                throw new IllegalArgumentException("weixin share type already has set!");
            }
        }

        public class TextBuilder {

            private TextBuilder() {
            }

            public TextBuilder text(String text) {
                wx.text = text;
                return this;
            }

            public ShareData.Wx build() {
                if (TextUtils.isEmpty(wx.text)) {
                    throw new IllegalArgumentException("weixin text share text must not null!");
                }
                mWxShareData = wx;
                return mWxShareData;
            }
        }

        public class ImageBuilder {

            private ImageBuilder() {
            }

            public ImageBuilder bitmap(Bitmap bitmap) {
                wx.bmp = bitmap;
                return this;
            }

            public ImageBuilder thumb(byte[] data) {
                wx.thumbData = data;
                return this;
            }

            public ShareData.Wx build() {
                if (null == wx.bmp || null == wx.thumbData) {
                    throw new IllegalArgumentException(
                            "weixin image share bitmap and thumb must not null!");
                }
                mWxShareData = wx;
                return mWxShareData;
            }
        }

        public class MusicBuilder {

            private MusicBuilder() {
            }

            public MusicBuilder title(String title) {
                wx.title = title;
                return this;
            }

            public MusicBuilder description(String description) {
                wx.description = description;
                return this;
            }

            public MusicBuilder musicUrl(String musicUrl) {
                wx.musicUrl = musicUrl;
                return this;
            }

            public MusicBuilder musicDataUrl(String musicDataUrl) {
                wx.musicDataUrl = musicDataUrl;
                return this;
            }

            public MusicBuilder thumb(byte[] thumb) {
                wx.thumbData = thumb;
                return this;
            }

            public ShareData.Wx build() {
                if (TextUtils.isEmpty(wx.musicUrl)) {
                    throw new IllegalArgumentException(
                            "weixin music share musicUrl must not null!");
                }
                mWxShareData = wx;
                return mWxShareData;
            }
        }//MusicBuilder end

        public class VideoBuilder {

            private VideoBuilder() {
            }

            public VideoBuilder title(String title) {
                wx.title = title;
                return this;
            }

            public VideoBuilder description(String description) {
                wx.description = description;
                return this;
            }

            public VideoBuilder videoUrl(String videoUrl) {
                wx.videoUrl = videoUrl;
                return this;
            }

            public VideoBuilder thumb(byte[] thumb) {
                wx.thumbData = thumb;
                return this;
            }

            public ShareData.Wx build() {
                if (TextUtils.isEmpty(wx.videoUrl)) {
                    throw new IllegalArgumentException(
                            "weixin video share videoUrl must not null!");
                }
                mWxShareData = wx;
                return mWxShareData;
            }
        }//VideoBuilder end

        public class WebBuilder {

            private WebBuilder() {
            }

            public WebBuilder title(String title) {
                wx.title = title;
                return this;
            }

            public WebBuilder description(String description) {
                wx.description = description;
                return this;
            }

            public WebBuilder webpageUrl(String webpageUrl) {
                wx.webpageUrl = webpageUrl;
                return this;
            }

            public WebBuilder thumb(byte[] bytes) {
                wx.thumbData = bytes;
                return this;
            }

            public ShareData.Wx build() {
                if (TextUtils.isEmpty(wx.webpageUrl)) {
                    throw new IllegalArgumentException(
                            "weixin webpage share webpageUrl must not null!");
                }
                if (TextUtils.isEmpty(wx.title)) {
                    throw new IllegalArgumentException("weixin webpage share title must not null!");
                }
                mWxShareData = wx;
                return mWxShareData;
            }
        }//WebBuilder end

        public class SmallAppBuilder {
            private SmallAppBuilder() {
            }

            public SmallAppBuilder webpageUrl(String webpageUrl) {
                wx.webpageUrl = webpageUrl;
                return this;
            }

            public SmallAppBuilder userName(String userName) {
                wx.userName = userName;
                return this;
            }

            public SmallAppBuilder path(String path) {
                wx.path = path;
                return this;
            }

            public SmallAppBuilder title(String title) {
                wx.title = title;
                return this;
            }

            public SmallAppBuilder description(String description) {
                wx.description = description;
                return this;
            }

            public SmallAppBuilder thumb(byte[] bytes) {
                wx.thumbData = bytes;
                return this;
            }

            public ShareData.Wx build() {
                mWxShareData = wx;
                return mWxShareData;
            }
        }//SmallAppBuilder end

        //音乐视频类型
        public class MusicVideoBuilder {

            private MusicVideoBuilder() {
            }

            public MusicVideoBuilder title(String title) {
                wx.title = title;
                return this;
            }

            public MusicVideoBuilder description(String description) {
                wx.description = description;
                return this;
            }

            public MusicVideoBuilder thumb(byte[] thumb) {
                wx.thumbData = thumb;
                return this;
            }

            public MusicVideoBuilder musicUrl(String musicUrl) {
                wx.musicUrl = musicUrl;
                return this;
            }

            public MusicVideoBuilder musicDataUrl(String musicDataUrl) {
                wx.musicDataUrl = musicDataUrl;
                return this;
            }

            public MusicVideoBuilder singerName(String singerName){
                wx.singerName = singerName;
                return this;
            }

            //单位毫秒
            public MusicVideoBuilder duration(int musicDuration){
                wx.duration = musicDuration;
                return this;
            }

            public MusicVideoBuilder songLyric(String lyric){
                wx.songLyric = lyric;
                return this;
            }

            public MusicVideoBuilder hdAlbumThumbFilePath(String hdAlbumThumbFilePath){
                wx.hdAlbumThumbFilePath = hdAlbumThumbFilePath;
                return this;
            }

            public MusicVideoBuilder albumName(String albumName){
                wx.albumName = albumName;
                return this;
            }

            public MusicVideoBuilder musicGenre(String musicGenre){
                wx.musicGenre = musicGenre;
                return this;
            }

            //单位秒
            public MusicVideoBuilder issueDate(long issueDate){
                wx.issueDate = issueDate;
                return this;
            }

            public MusicVideoBuilder identification(String identification){
                wx.identification = identification;
                return this;
            }

            public ShareData.Wx build() {
                if (TextUtils.isEmpty(wx.musicUrl)) {
                    throw new IllegalArgumentException("weixin musicVideo share musicUrl must not null!");
                }
                if (TextUtils.isEmpty(wx.musicDataUrl)) {
                    throw new IllegalArgumentException("weixin musicVideo share musicDataUrl must not null!");
                }
                if (TextUtils.isEmpty(wx.singerName)) {
                    throw new IllegalArgumentException("weixin musicVideo share singerName must not null!");
                }
                if (wx.duration <=0) {
                    throw new IllegalArgumentException("weixin musicVideo share duration must not null!");
                }
                mWxShareData = wx;
                return mWxShareData;
            }
        }//MusicVideoBuilder end
    }//WXBuilder end

    //构建 微博 分享数据
    public class SinaWbBuilder {
        private String msg;
        private String imagePath;
        private Bitmap bitmap;

        private SinaWbBuilder() {
        }

        public SinaWbBuilder msg(String msg) {
            this.msg = msg;
            return this;
        }

        /**
         * @param imagePath 本地
         */
        public SinaWbBuilder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public SinaWbBuilder bitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public SinaWeibo build() {
            /*if (TextUtils.isEmpty(msg)) {
                throw new IllegalArgumentException("sina weibo msg must not null!");
            }*/
            if (TextUtils.isEmpty(imagePath) && null == bitmap) {
                throw new IllegalArgumentException("sina weibo both imagePath and bitmap is null!");
            }
            mSinaShareData = new SinaWeibo(msg, imagePath, bitmap);
            return mSinaShareData;
        }
    }//SinaWbBuilder end
}
