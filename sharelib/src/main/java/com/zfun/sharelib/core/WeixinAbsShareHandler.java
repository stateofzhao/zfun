package com.zfun.sharelib.core;

import android.content.Context;
import androidx.annotation.Nullable;

import com.zfun.sharelib.init.InitContext;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 微信分享基类，注意微信分享回调在{@link com.zfun.sharelib.WxCallbackActivity}中实现。
 * <p/>
 * Created by zfun on 2017/8/8 14:23
 */
public abstract class WeixinAbsShareHandler implements IShareHandler {
    private static final String TAG = "WeixinAbsShareHandler";

    static final int SESSION_SUPPORTED_VERSION = 0x21010001; // 4.0以上版本
    static final int TIMELINE_SUPPORTED_VERSION = 0x21020001; // 4.2以上版本

    protected Context mContext;
    protected boolean isRelease = true;
    protected boolean isSharing = false;

    @Nullable
    private ShareData mNowShareData;//注意内存泄漏，这个handler是单例

    /**
     * 分享类型{@link SendMessageToWX.Req#WXSceneFavorite},{@link SendMessageToWX.Req#WXSceneSession},{@link
     * SendMessageToWX.Req#WXSceneTimeline}
     */
    abstract int scene();

    public boolean isSharing() {
        return isSharing;
    }

    public void setSharing(boolean sharing) {
        isSharing = sharing;
    }

    public void postShareSuccess() {
        if (null != mNowShareData && null != mNowShareData.mShareListener) {
            InitContext.getInstance().getMessageHandler().asyncRun(new Runnable() {
                @Override
                public void run() {
                    mNowShareData.mShareListener.onSuccess();
                    mNowShareData.mShareListener = null;
                    mNowShareData = null;
                }
            });
        } else {
            mNowShareData = null;
        }
    }

    public void postShareError() {
        if (null != mNowShareData && null != mNowShareData.mShareListener) {
            InitContext.getInstance().getMessageHandler().asyncRun(new Runnable() {
                @Override
                public void run() {
                    mNowShareData.mShareListener.onFail();
                    mNowShareData.mShareListener = null;
                    mNowShareData = null;
                }
            });
        } else {
            mNowShareData = null;
        }
    }

    public void postShareCancel() {
        if (null != mNowShareData && null != mNowShareData.mShareListener) {
            InitContext.getInstance().getMessageHandler().asyncRun(new Runnable() {
                @Override
                public void run() {
                    mNowShareData.mShareListener.onCancel();
                    mNowShareData.mShareListener = null;
                    mNowShareData = null;
                }
            });
        } else {
            mNowShareData = null;
        }
    }

    void doShare(ShareData shareData, IWXAPI api) {
        if (isRelease || null == shareData) {
            return;
        }
        mNowShareData = shareData;
        ShareData.Wx wx = shareData.getWxShareData();
        int type = wx.type;
        boolean result = false;
        if (type == ShareData.Wx.TYPE_IMAGE) {
            WXImageObject imgObj = new WXImageObject(wx.bmp);
            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = imgObj;

            //设置缩略图
            msg.thumbData = wx.thumbData;

            //构造一个Req
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("image");
            req.message = msg;
            req.scene = scene();

            //发送数据到微信
            result = api.sendReq(req);
        } else if (type == ShareData.Wx.TYPE_MUSIC) {
            WXMusicObject musicObj = new WXMusicObject();
            musicObj.musicUrl = wx.musicUrl;
            musicObj.musicDataUrl = wx.musicDataUrl;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = musicObj;
            msg.title = wx.title;
            msg.description = wx.description;
            msg.thumbData = wx.thumbData;

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("music");
            req.message = msg;
            req.scene = scene();

            result = api.sendReq(req);
        } else if (type == ShareData.Wx.TYPE_VIDEO) {
            WXVideoObject videoObj = new WXVideoObject();
            videoObj.videoUrl = wx.videoUrl;

            WXMediaMessage msg = new WXMediaMessage(videoObj);
            msg.title = wx.title;
            msg.description = wx.description;
            msg.thumbData = wx.thumbData;

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("video");
            req.message = msg;
            req.scene = scene();

            result = api.sendReq(req);
        } else if (type == ShareData.Wx.TYPE_TEXT) {
            WXTextObject textObj = new WXTextObject();
            textObj.text = wx.text;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = textObj;
            msg.description = wx.text;

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("text");
            req.message = msg;
            req.scene = scene();

            result = api.sendReq(req);
        } else if (type == ShareData.Wx.TYPE_WEB) {
            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = wx.webpageUrl;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = webpage;
            msg.title = wx.title;
            msg.description = wx.description;
            msg.thumbData = wx.thumbData;

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("webpage");
            req.message = msg;
            req.scene = scene();

            result = api.sendReq(req);
        } else if (type == ShareData.Wx.TYPE_SMALL_APP) {
            WXMiniProgramObject miniProgramObj = new WXMiniProgramObject();
            miniProgramObj.webpageUrl = wx.webpageUrl; // 兼容低版本的网页链接
            int miniProgramType = InitContext.getInstance().getInitParams().getMINIPTOGRAM_TYPE_RELEASE();
            miniProgramObj.miniprogramType = miniProgramType;// 正式版:0，测试版:1，体验版:2
            miniProgramObj.userName = wx.userName;     // 小程序原始id
            miniProgramObj.path = wx.path;            //小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
            WXMediaMessage msg = new WXMediaMessage(miniProgramObj);
            msg.title = wx.title;                    // 小程序消息title
            msg.description = wx.description;               // 小程序消息desc
            msg.thumbData = wx.thumbData;                      // 小程序消息封面图片，小于128k

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("miniProgram");
            req.message = msg;
            req.scene =  scene();
            result = api.sendReq(req);
        } else if(type == ShareData.Wx.TYPE_MUSIC_VIDEO){//
            WXMusicVideoObject musicVideoObject = new WXMusicVideoObject();
            musicVideoObject.musicUrl = wx.musicUrl;
            musicVideoObject.musicDataUrl = wx.musicDataUrl;
            musicVideoObject.songLyric = wx.songLyric;
            musicVideoObject.hdAlbumThumbFilePath = wx.hdAlbumThumbFilePath;
            musicVideoObject.singerName = wx.singerName;
            musicVideoObject.albumName = wx.albumName;
            musicVideoObject.musicGenre = wx.musicGenre;
            if(wx.issueDate>0){
                musicVideoObject.issueDate = wx.issueDate;
            }
            try {
                musicVideoObject.identification =  URLEncoder.encode(wx.identification,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                //
            }
            musicVideoObject.duration = wx.duration;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = musicVideoObject;
            msg.title = wx.title;
            msg.description = wx.description;
            msg.messageExt = musicVideoObject.identification;
            msg.thumbData = wx.thumbData;

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("musicVideo");
            req.message = msg;
            req.scene= scene();
            result = api.sendReq(req);
        }
        setSharing(result);
    }

    private static String buildTransaction(final String type) {
        String transPre =  (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();

        return transPre + ShareConstant.SHARE_TAG_STR;
    }

    @Override
    public void init() {
        isRelease = false;
        mContext = InitContext.getInstance().getHostActivity();
    }

    @Override
    public void release() {
        isRelease = true;
        mContext = null;
    }
}
