package com.zfun.sharelib;

import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_CHORUS_URL;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_COPY_URL;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_QQ_FRIEND;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_QQ_ZONE;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_SINA_WEIBO;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_WX_CYCLE;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_WX_FRIEND;
import static com.zfun.sharelib.core.ShareConstant.SHARE_TYPE_WX_MINI_PROGRAM;

import android.content.Context;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zfun.sharelib.core.CopyUrlHandler;
import com.zfun.sharelib.core.IShareHandler;
import com.zfun.sharelib.core.IShareMgr;
import com.zfun.sharelib.core.ISharePlug;
import com.zfun.sharelib.core.KwFriendHandler;
import com.zfun.sharelib.core.QQFrendShareHandler;
import com.zfun.sharelib.core.QQZoneShareHandler;
import com.zfun.sharelib.core.ShareConstant;
import com.zfun.sharelib.core.ShareData;
import com.zfun.sharelib.core.SinaWeiboHandler;
import com.zfun.sharelib.core.WeixinCircleHandler;
import com.zfun.sharelib.core.WeixinFriendHandler;
import com.zfun.sharelib.core.WeixinMiniProgramHandler;
import com.zfun.sharelib.init.InitContext;
import com.zfun.sharelib.init.NullableActivity;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;

/**
 * 分享管理器
 * <p/>
 * Created by zfun on 2017/8/4 16:04
 */
public class ShareMgrImpl implements IShareMgr {
    private final SparseArray<IShareHandler> mShareHandlers = new SparseArray<>();
    private volatile boolean hasInit = false;
    private volatile boolean hasInitSdk = false;

    private Tencent mTencent;
    private IWXAPI mWxAPI;
//    private IWBAPI mWBAPI;
    private IShareHandler mCurShareHandler;

    private static class SingletonHolder {
        private static final ShareMgrImpl INSTANCE = new ShareMgrImpl();
    }

    public static ShareMgrImpl getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private  void initSdk(Context applicationContext) {
        if (applicationContext == null || hasInitSdk) {
            return;
        }
        hasInitSdk = true;
//         mWBAPI = WBAPIFactory.createWBAPI(applicationContext);
//         mWBAPI.registerApp(App.getInstance(), new AuthInfo(applicationContext, ShareConstants.SINA_APP_KEY,
//                 ShareConstants.SINA_REDIRECT_URL, ShareConstants.SINA_SCOPE));//新浪微博初始化
        if(applicationContext.getApplicationContext() != null){
            mTencent = Tencent.createInstance(
                    ShareConstant.QQ_APP_ID,
                    applicationContext.getApplicationContext(),
                    // 第三个参数是清单中注册的FileProvider的authorities属性
                    InitContext.getInstance().getInitParams().getFileProviderAuthorities());
        }
        mWxAPI = WXAPIFactory.createWXAPI(applicationContext, ShareConstant.WX_APP_ID,true, ConstantsAPI.LaunchApplication.LAUNCH_MODE_USING_START_ACTIVITY);
        mWxAPI.registerApp(ShareConstant.WX_APP_ID);
    }

    @Nullable
    public Tencent getTencent(){
        return mTencent;
    }

    @Nullable
    public IWXAPI getWxApi(){
        return mWxAPI;
    }

    //单例
    private ShareMgrImpl() {
        // 挂载所有分享类型对应的 IShareHandler
        mShareHandlers.put(SHARE_TYPE_QQ_ZONE, new QQZoneShareHandler());
        mShareHandlers.put(SHARE_TYPE_QQ_FRIEND, new QQFrendShareHandler());
        mShareHandlers.put(SHARE_TYPE_WX_CYCLE, new WeixinCircleHandler());
        mShareHandlers.put(SHARE_TYPE_WX_FRIEND, new WeixinFriendHandler());
        mShareHandlers.put(SHARE_TYPE_SINA_WEIBO, new SinaWeiboHandler());
        mShareHandlers.put(SHARE_TYPE_COPY_URL, new CopyUrlHandler());
        mShareHandlers.put(SHARE_TYPE_CHORUS_URL, new KwFriendHandler());
        mShareHandlers.put(SHARE_TYPE_WX_MINI_PROGRAM,new WeixinMiniProgramHandler());
        //只会在主进程进行调用
        initSdk(InitContext.getInstance().getHostActivity());
    }

    //暂时先不用调用
    private void release() {
        hasInit = false;
        hasInitSdk = false;
        mTencent = null;
        mWxAPI = null;
        final int size = mShareHandlers.size();
        for (int index = 0; index < size; index++) {
            IShareHandler handler = mShareHandlers.valueAt(index);
            handler.release();
        }
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
            throw new IllegalArgumentException(
                    "share type :" + "[" + shareType + "] is not support!");
        }
        mCurShareHandler = handler;
        return handler;
    }

    //所有分享Handler默认持有cn.kuwo.player.activities.MainActivity，方便主进程使用
    private boolean initIfNeed() {
        if (hasInit) {
            return true;
        }
        if (!(InitContext.getInstance().getHostActivity() instanceof NullableActivity)) {//这里有可能为null
            synchronized (this) {
                if (!hasInit) {
                    final int size = mShareHandlers.size();
                    for (int index = 0; index < size; index++) {
                        IShareHandler handler = mShareHandlers.valueAt(index);
                        handler.init();
                    }
                    hasInit = true;
                }
            }
            return true;
        }
        return false;
    }
}
