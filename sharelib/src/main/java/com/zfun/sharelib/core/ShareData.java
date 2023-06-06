package com.zfun.sharelib.core;

import android.app.Activity;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.zfun.sharelib.ShareMgrImpl;
import com.tencent.connect.share.QQShare;
import com.zfun.sharelib.type.QzoneOAuthV2;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 分享数据
 * <p/>
 * Created by zfun on 2017/8/6 8:57
 */
public class ShareData {

    public interface OnShareListener {
        void onSuccess(String msg);

        void onFail(String msg);

        void onCancel(String msg);
    }//OnShareResult end

    public interface OnWXLoginListener{
        void onFail(String msg);
        void onCancel(String msg);
        void onSuc(String code,String state,String msg);
    }//

    public interface OnQQLoginListener{
        void onFail(String msg);
        void onCancel(String msg);
        void onSuc(QzoneOAuthV2 authV2,String msg);
    }

    private QQ mQQShareData;
    private QQZone mQQZShareData;
    private QQLogin mQQLogin;
    private Wx mWxShareData;
    private SinaWeibo mSinaShareData;

    //复制链接，复制下载链接 用到的
    private String mUrl;

    public OnShareListener mShareListener;
    public OnWXLoginListener mWXLoginListener;
    public OnQQLoginListener mQQLoginListener;

    /**
     * 构建分享到QQ好友的 纯图片数据
     */
    public QQImageBuilder buildQQImage(@NonNull Activity activity) {
        return new QQImageBuilder(activity);
    }

    /**
     * 构建分享到QQ好友的 图文分享数据
     */
    public QQTextImageBuilder buildQQTImage(@NonNull Activity activity) {
        return new QQTextImageBuilder(activity);
    }

    /** 构建分享到QQ好友的 音乐分享数据 */
    public QQAudioBuilder buildQQAudio(@NonNull Activity activity) {
        return new QQAudioBuilder(activity);
    }

    /** 构建QQ空间 图文分享数据 */
    public QZoneImageTextBuilder buildQZoneTImage(@NonNull Activity activity) {
        return new QZoneImageTextBuilder(activity);
    }

    public QZoneMoodBuilder buildMood(@NonNull Activity activity){
        return new QZoneMoodBuilder(activity);
    }

    public QQLoginBuilder buildQQLogin(@NonNull Activity activity){
        return new QQLoginBuilder(activity);
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

    public void setOnShareListener(OnShareListener listener) {
        mShareListener = listener;
    }

    public void setOnWXLoginListener(OnWXLoginListener listener){
        mWXLoginListener = listener;
    }

    public void setQQLoginListener(OnQQLoginListener qqLoginListener) {
        this.mQQLoginListener = qqLoginListener;
    }

    public String getCopyUrl() {
        return mUrl;
    }

    /** 获取 qq空间 分享数据 */
    public QQZone getQQZoneShareData() {
        return mQQZShareData;
    }

    /** 获取 QQ好友 分项数据 */
    public QQ getQQFriendData() {
        return mQQShareData;
    }

    public QQLogin getQQLoginData(){
        return mQQLogin;
    }

    public Wx getWxShareData() {
        return mWxShareData;
    }

    public SinaWeibo getSinaShareData() {
        return mSinaShareData;
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

        public final WeakReference<Activity> activityRef;

        private QQ(WeakReference<Activity> activityRef,int shareType, String title, String targetUrl, String summary, String imageURLorFilePath,
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

            this.activityRef = activityRef;
        }

        public void share(){
            ShareMgrImpl.getInstance().share(ShareConstant.SHARE_TYPE_QQ_FRIEND, ShareData.this);
        }


    }//QQ end

    //分享到QQ空间的数据
    public class QQZone {
        public static final int TYPE_IMAGE_TEXT = 1;//图文，只支持一张，传递多张的话也只会取第一张
        public static final int TYPE_MINI_PROGRAM = 2;//小程序 todo zfun 暂不支持

        public static final int TYPE_MOOD = 10; //纯图
        public static final int TYPE_PUBLISH_VIDEO = 11;//视频

        public final int shareType;

        public final String title;
        public final String summary;
        public final String site;
        public final String targetUrl;
        public final ArrayList<String> imageUrlOrFilePath;

        public final String hulianCallBack;
        public final String hulianScene;
        public final String videoLocalPath;

        public final WeakReference<Activity> activityRef;

        /**
         * @param imageUrlOrFilePath 不支持混合传递，要么全是URL，要么全是文件路径
         * */
        private QQZone(WeakReference<Activity> activityRef,int shareType, String title, String summary, String site,
                       ArrayList<String> imageUrlOrFilePath, String targetUrl,String hulianCallBack,String hulianScene,String videoLocalPath) {
            this.shareType = shareType;
            this.title = title;
            this.summary = summary;
            this.site = site;
            this.imageUrlOrFilePath = imageUrlOrFilePath;
            this.targetUrl = targetUrl;
            this.hulianCallBack = hulianCallBack;
            this.hulianScene = hulianScene;
            this.videoLocalPath = videoLocalPath;

            this.activityRef = activityRef;
        }

        public void share(){
            ShareMgrImpl.getInstance().share(ShareConstant.SHARE_TYPE_QQ_ZONE, ShareData.this);
        }
    }//QQZone end

    public class QQLogin{
        protected final WeakReference<Activity> activityRef;

        public QQLogin(WeakReference<Activity> activityRef){
            this.activityRef = activityRef;
        }

        public void login(){
            ShareMgrImpl.getInstance().share(ShareConstant.SHARE_TYPE_LOGIN_QQ,ShareData.this);
        }
    }//

    public class Wx {
        public final static int TYPE_TEXT = 1;
        public final static int TYPE_IMAGE = 2;
        public final static int TYPE_MUSIC = 3;
        public final static int TYPE_VIDEO = 4;
        public final static int TYPE_WEB = 5;//网页分享
        public final static int TYPE_SMALL_APP = 6;//小程序类型分享，暂未实现
        public final static int TYPE_MUSIC_VIDEO = 7;//音乐视频类型分享
        public final static int TYPE_LOGIN = 10;//微信登录

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

        public void login(){
            share(ShareConstant.SHARE_TYPE_LOGIN_WX);
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

        private final WeakReference<Activity> activityRef;

        private QQTextImageBuilder(@NonNull Activity activity) {
            activityRef = new WeakReference<>(activity);
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
            mQQShareData =  new QQ(activityRef,QQShare.SHARE_TO_QQ_TYPE_DEFAULT, title, targetUrl, summary, imageURLorFilePath,
                            appName, site, null, ext);
            return mQQShareData;
        }
    }//QQTextImageBuilder end

    //构建 QQ纯图片分享 的数据
    public class QQImageBuilder {
        private String appName;
        private String imageLocalUri;//只能是本地图片路径
        private int ext;

        private final WeakReference<Activity> activityRef;

        private QQImageBuilder(@NonNull Activity activity) {
            activityRef = new WeakReference<>(activity);
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
            mQQShareData = new QQ(activityRef,QQShare.SHARE_TO_QQ_TYPE_IMAGE, null, null, null, imageLocalUri, appName,
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

        private final WeakReference<Activity> activityRef;

        private QQAudioBuilder(@NonNull Activity activity) {
            activityRef = new WeakReference<>(activity);
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
            mQQShareData = new QQ(activityRef,QQShare.SHARE_TO_QQ_TYPE_AUDIO, title, targetUrl, summary, imageURLorFilePath,
                    appName, site, audioUrl, ext);
            return mQQShareData;
        }
    }//QQAudioBuilder end

    //构建 QQ空间图文 分享的数据
    public class QZoneImageTextBuilder{
        private ArrayList<String> netImageUrls;
        private ArrayList<String> localImagePaths;

        protected String title;
        protected String summary;
        protected String targetUrl;
        protected String site;

        protected final WeakReference<Activity> activityRef;

        private QZoneImageTextBuilder(@NonNull Activity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        /**
         * @param title 最多200字
         * */
        public QZoneImageTextBuilder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * @param summary 最多600字
         * */
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
         *  QZone接口暂不支持发送多张图片的能力，若传入多张图片，则会自动选入第一张图片作为预览图。多图的能力将会在以后支持。
         *
         * @param imageNetUrls 不能本地和网络图片混着来，只能是一种类型（要么全是本地，要么全是网络），最多9张
         */
        public QZoneImageTextBuilder imageNetUrls(ArrayList<String> imageNetUrls) {
            this.netImageUrls = imageNetUrls;
            return this;
        }

        /**
         *  QZone接口暂不支持发送多张图片的能力，若传入多张图片，则会自动选入第一张图片作为预览图。多图的能力将会在以后支持。
         *
         * @param imageLocalPaths 不能本地和网络图片混着来，只能是一种类型（要么全是本地，要么全是网络），最多9张
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
            mQQZShareData = new QQZone(activityRef,QQZone.TYPE_IMAGE_TEXT,title, summary, site, images, targetUrl,"","","");
            return mQQZShareData;
        }
    }//QZoneImageTextBuilder end

    //构建 【QQ空间说说】 分享的数据
    public class QZoneMoodBuilder{
        private ArrayList<String> localImagePaths;
        private String hulianCallBack;
        private String summary;
        private String hulianScene;


        protected final WeakReference<Activity> activityRef;

        private QZoneMoodBuilder(@NonNull Activity activity) {
            activityRef = new WeakReference<>(activity);
        }

        /**
         *
         * @param imageLocalPaths 只支持本地图片，<=9张时为发表说说，>9张时为上传到相册
         */
        public QZoneMoodBuilder imageLocalPaths(ArrayList<String> imageLocalPaths) {
            this.localImagePaths = imageLocalPaths;
            return this;
        }

        public QZoneMoodBuilder summary(String summary){
            this.summary = summary;
            return this;
        }

        public QZoneMoodBuilder hulianCallBack(String hulianCallBack){
            this.hulianCallBack = hulianCallBack;
            return this;
        }

        public QZoneMoodBuilder hulianScene(String scene){
            this.hulianScene = scene;
            return this;
        }

        QQZone build() {
            mQQZShareData = new QQZone(activityRef, QQZone.TYPE_MOOD,"", summary, "", localImagePaths, "",hulianCallBack,hulianScene,"");
            return mQQZShareData;
        }
    }//

    //构建 【QQ空间说说】 分享的数据
    public class QZonePublishVideoBuilder{
        private String videoLocalPaths;
        private String hulianCallBack;
        private String summary;
        private String hulianScene;

        protected final WeakReference<Activity> activityRef;

        private QZonePublishVideoBuilder(@NonNull Activity activity) {
            activityRef = new WeakReference<>(activity);
        }

        /**
         * @param videoLocalPaths 只支持本地视频，：上传视频的大 小最好控別在100N以内（因为QQ普通用户上传视频必须在100M
         *                        以內，黄站用户可上传1G以内视频，大于1G会直接报错。)
         */
        public QZonePublishVideoBuilder videoLocalPaths(String videoLocalPaths) {
            this.videoLocalPaths = videoLocalPaths;
            return this;
        }

        public QZonePublishVideoBuilder summary(String summary){
            this.summary = summary;
            return this;
        }

        public QZonePublishVideoBuilder hulianScene(String scene){
            this.hulianScene = scene;
            return this;
        }

        public QZonePublishVideoBuilder hulianCallBack(String hulianCallBack){
            this.hulianCallBack = hulianCallBack;
            return this;
        }

        QQZone build() {
            if (TextUtils.isEmpty(videoLocalPaths)) {
                throw new IllegalArgumentException("videoLocalPaths must not null!");
            }
            mQQZShareData = new QQZone(activityRef, QQZone.TYPE_PUBLISH_VIDEO,"", summary, "", new ArrayList<String>(0), "",hulianCallBack,hulianScene,videoLocalPaths);
            return mQQZShareData;
        }
    }//

    public class QQLoginBuilder {
        protected final WeakReference<Activity> activityRef;

        private QQLoginBuilder(@NonNull Activity activity) {
            activityRef = new WeakReference<>(activity);
        }

        QQLogin build() {
            mQQLogin = new QQLogin(activityRef);
            return mQQLogin;
        }
    }//

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

        public LoginBuilder typeLogin(){
            checkType();
            wx.type = Wx.TYPE_LOGIN;
            return new LoginBuilder();
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
        }//

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
        }//

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

        //微信登录
        public class LoginBuilder{
            public ShareData.Wx build(){
                mWxShareData = wx;
                return mWxShareData;
            }
        }//
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
