package com.zfun.sharelib;

import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_QQ_FRIEND;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_QQ_ZONE;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_SINA_WEIBO;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_WX_CYCLE;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_WX_FRIEND;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_WX_MINI_PROGRAM;

import androidx.annotation.NonNull;

import com.zfun.sharelib.core.IShareHandler;
import com.zfun.sharelib.core.IShareMgr;
import com.zfun.sharelib.core.ISharePlug;
import com.zfun.sharelib.core.QQFriendShareHandler;
import com.zfun.sharelib.core.QQZoneShareHandler;
import com.zfun.sharelib.core.ShareConstant;
import com.zfun.sharelib.core.WeixinMiniProgramHandler;
import com.zfun.sharelib.core.ShareData;
import com.zfun.sharelib.core.SinaWeiboHandler;
import com.zfun.sharelib.core.WeixinCircleHandler;
import com.zfun.sharelib.core.WeixinFriendHandler;
import com.zfun.sharelib.init.InternalShareInitBridge;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 分享管理器
 * <p/>
 * Created by zfun on 2017/8/4 16:04
 */
public class ShareMgrImpl implements IShareMgr {
    private final Map<Integer, IShareHandler> mShareHandlers = Collections.synchronizedMap(new HashMap<>());
    private volatile boolean shareHandlerInited = false;

    //    private IWBAPI mWBAPI;//SsoFactory类处理了
    private IShareHandler mCurShareHandler;

    private static class SingletonHolder {
        private static final ShareMgrImpl INSTANCE = new ShareMgrImpl();
    }

    public static ShareMgrImpl getInstance() {
        return SingletonHolder.INSTANCE;
    }

    //单例
    private ShareMgrImpl() {
        final ShareTypeBuilder shareTypeBuilder = InternalShareInitBridge.getInstance().getSupportShareTypeBuilder();
        if(null != shareTypeBuilder){
            configShareHandler(shareTypeBuilder);
        }
    }

    private void configShareHandler(@NonNull ShareTypeBuilder shareTypeBuilder) {
        mShareHandlers.putAll(shareTypeBuilder.shareHandlers);
        if (shareTypeBuilder.useQQFriend) {
            mShareHandlers.put(SHARE_TYPE_QQ_FRIEND, new QQFriendShareHandler());
        }
        if (shareTypeBuilder.useQQZone) {
            mShareHandlers.put(SHARE_TYPE_QQ_ZONE, new QQZoneShareHandler());
        }
        if (shareTypeBuilder.useWXFriend) {
            mShareHandlers.put(SHARE_TYPE_WX_FRIEND, new WeixinFriendHandler());
        }
        if (shareTypeBuilder.useWXTimeline) {
            mShareHandlers.put(SHARE_TYPE_WX_CYCLE, new WeixinCircleHandler());
        }
        if (shareTypeBuilder.useWXMineProgram) {
            mShareHandlers.put(SHARE_TYPE_WX_MINI_PROGRAM, new WeixinMiniProgramHandler());
        }
        if (shareTypeBuilder.useSinaWeibo) {
            mShareHandlers.put(SHARE_TYPE_SINA_WEIBO, new SinaWeiboHandler());
        }
    }

    //暂时先不用调用
    private void release() {
        shareHandlerInited = false;
        Set<Integer> keySet = mShareHandlers.keySet();
        for (Integer integer : keySet) {
            IShareHandler handler = mShareHandlers.get(integer);
            if (null != handler) {
                handler.release();
            }
        }
        mShareHandlers.clear();
    }

    /*public boolean isSharing() {
        if (mCurShareHandler != null) {
            if (mCurShareHandler instanceof WeixinFriendHandler) {
                return ((WeixinFriendHandler)mCurShareHandler).isSharing();
            }else if (mCurShareHandler instanceof WeixinCircleHandler) {
                return ((WeixinCircleHandler)mCurShareHandler).isSharing();
            }
        }
        return false;
    }

    public void setSharing(boolean bl) {
        if (mCurShareHandler != null) {
            if (mCurShareHandler instanceof WeixinFriendHandler) {
                ((WeixinFriendHandler)mCurShareHandler).setSharing(bl);
            }else if (mCurShareHandler instanceof WeixinCircleHandler) {
                ((WeixinCircleHandler)mCurShareHandler).setSharing(bl);
            }
        }
    }*/

    @Override
    public void share(@ShareConstant.ShareType int type, @NonNull ShareData shareData) {
        initIfNeed();
        IShareHandler handler = checkShareType(type);
        handler.share(shareData);
    }

    public IShareHandler getCurShareHandler() {
        return mCurShareHandler;
    }

    @Override
    public void share(@ShareConstant.ShareType int type, @NonNull ISharePlug sharePlug) {
        initIfNeed();
        final IShareHandler handler = checkShareType(type);
        sharePlug.share(type, handler);//网络提示，后置到sharePlug中
    }

    /*@Override
    public void shareWithMenu(@NonNull final IShareMenu shareMenu,
            @NonNull final ShareData shareData) {
        //这种在外部设置好了分享数据，所以这里不用再加网络提示
        shareMenu.setOnItemClickListener(new IShareMenu.OnItemClickListener() {
            @Override
            public void onItemClick(int type) {
                share(type, shareData);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onDismiss() {
            }
        });
        shareMenu.show();
    }*/

    /*@Override
    public void shareWithMenu(@NonNull final IShareMenu shareMenu,
            @NonNull final ISharePlug sharePlug) {
        shareMenu.setOnItemClickListener(new IShareMenu.OnItemClickListener() {
            @Override
            public void onItemClick(int type) {
                share(type, sharePlug);
            }

            @Override
            public void onCancel() {
                sharePlug.cancel();
            }

            @Override
            public void onDismiss() {
            }
        });
        shareMenu.show();
    }*/

    private IShareHandler checkShareType(int shareType) {
        IShareHandler handler = mShareHandlers.get(shareType);
        if (null == handler) {//不支持的分享类型
            throw new IllegalArgumentException("share type :" + "[" + shareType + "] is not support!");
        }
        mCurShareHandler = handler;
        return handler;
    }

    private boolean initIfNeed() {
        if (shareHandlerInited) {
            return true;
        }
        synchronized (this) {
            if (!shareHandlerInited) {
                Set<Integer> keySet = mShareHandlers.keySet();
                for (Integer integer : keySet) {
                    IShareHandler handler = mShareHandlers.get(integer);
                    if (null != handler) {
                        handler.init();
                    }
                }
                shareHandlerInited = true;
            }
        }
        return true;
    }

    public static class ShareTypeBuilder {
        private final Map<Integer, IShareHandler> shareHandlers = new LinkedHashMap<>();
        private boolean useQQFriend = false;
        private boolean useQQZone = false;
        private boolean useWXFriend = false;
        private boolean useWXTimeline = false;
        private boolean useWXMineProgram = false;
        private boolean useSinaWeibo = false;

        public ShareTypeBuilder addExtraHandler(int shareType, @NonNull IShareHandler shareHandler) {
            assert shareType > 100;
            shareHandlers.put(shareType, shareHandler);
            return this;
        }

        public ShareTypeBuilder useQQFriend() {
            useQQFriend = true;
            return this;
        }

        public ShareTypeBuilder useQQZone() {
            useQQZone = true;
            return this;
        }

        public ShareTypeBuilder useWxFriend() {
            useWXFriend = true;
            return this;
        }

        public ShareTypeBuilder useWxTimeline() {
            useWXTimeline = true;
            return this;
        }

        public ShareTypeBuilder useWxMinProgram() {
            useWXMineProgram = true;
            return this;
        }

        public ShareTypeBuilder useSinaWeibo() {
            useSinaWeibo = true;
            return this;
        }
    }//
}
