package com.zfun.sharelib.init;


import androidx.annotation.NonNull;

import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;

/**
 * Created by lzf on 2021/12/21 3:18 下午
 */
public class InitParams {
    public static final int UN_MINIPTOGRAM_TYPE = -10086;

    private String QQ_APP_ID;
    private String QQ_SECRET;
    private String QZONE_REDIRECT_URL = "http://i.kuwo.cn/US/2013/mobile/login_ar_qq.htm";

    private String WX_APP_ID;

    private String SINA_APP_KEY;
    private String SINA_REDIRECT_URL;
    private String SINA_SCOPE;

    private String fileProviderAuthorities;//清单中注册的FileProvider的authorities属性

    //可选
    private int MINIPTOGRAM_TYPE_RELEASE = UN_MINIPTOGRAM_TYPE;

    public String SHARE_DEFAULT_COPY_URL;
    public String SHARE_DEFAULT_IMAGE_URL;


    public void setQZONE_REDIRECT_URL(String QZONE_REDIRECT_URL) {
        this.QZONE_REDIRECT_URL = QZONE_REDIRECT_URL;
    }

    public String getQZONE_REDIRECT_URL() {
        return QZONE_REDIRECT_URL;
    }

    public void setWX_APP_ID(String WX_APP_ID) {
        this.WX_APP_ID = WX_APP_ID;
    }

    public String getWX_APP_ID() {
        return WX_APP_ID;
    }

    /**
     * {@link WXMiniProgramObject#MINIPTOGRAM_TYPE_RELEASE}
     * */
    public void setMINIPTOGRAM_TYPE_RELEASE(int MINIPTOGRAM_TYPE_RELEASE) {
        this.MINIPTOGRAM_TYPE_RELEASE = MINIPTOGRAM_TYPE_RELEASE;
    }

    public int getMINIPTOGRAM_TYPE_RELEASE() {
        return MINIPTOGRAM_TYPE_RELEASE;
    }

    public void setQQ_SECRET(String QQ_SECRET) {
        this.QQ_SECRET = QQ_SECRET;
    }

    public String getQQ_SECRET() {
        return QQ_SECRET;
    }

    public String getQQ_APP_ID() {
        return QQ_APP_ID;
    }

    public void setQQ_APP_ID(String QQ_APP_ID) {
        this.QQ_APP_ID = QQ_APP_ID;
    }

    public String getSINA_APP_KEY() {
        return SINA_APP_KEY;
    }

    public void setSINA_APP_KEY(String SINA_APP_KEY) {
        this.SINA_APP_KEY = SINA_APP_KEY;
    }

    public String getSINA_REDIRECT_URL() {
        return SINA_REDIRECT_URL;
    }

    public void setSINA_REDIRECT_URL(String SINA_REDIRECT_URL) {
        this.SINA_REDIRECT_URL = SINA_REDIRECT_URL;
    }

    public String getSINA_SCOPE() {
        return SINA_SCOPE;
    }

    public void setSINA_SCOPE(String SINA_SCOPE) {
        this.SINA_SCOPE = SINA_SCOPE;
    }

    public String getFileProviderAuthorities() {
        return fileProviderAuthorities;
    }

    public void setFileProviderAuthorities(String fileProviderAuthorities) {
        this.fileProviderAuthorities = fileProviderAuthorities;
    }

    public String getSHARE_DEFAULT_COPY_URL() {
        return SHARE_DEFAULT_COPY_URL;
    }

    public void setSHARE_DEFAULT_COPY_URL(String SHARE_DEFAULT_COPY_URL) {
        this.SHARE_DEFAULT_COPY_URL = SHARE_DEFAULT_COPY_URL;
    }

    public String getSHARE_DEFAULT_IMAGE_URL() {
        return SHARE_DEFAULT_IMAGE_URL;
    }

    public void setSHARE_DEFAULT_IMAGE_URL(String SHARE_DEFAULT_IMAGE_URL) {
        this.SHARE_DEFAULT_IMAGE_URL = SHARE_DEFAULT_IMAGE_URL;
    }

    public InitParams copy(){
        InitParams initParams = new InitParams();
        initParams.QQ_APP_ID = QQ_APP_ID;
        initParams.QQ_SECRET = QQ_SECRET;
        initParams.QZONE_REDIRECT_URL = QZONE_REDIRECT_URL;

        initParams.WX_APP_ID = WX_APP_ID;
        initParams.MINIPTOGRAM_TYPE_RELEASE = MINIPTOGRAM_TYPE_RELEASE;

        initParams.SINA_APP_KEY = SINA_APP_KEY;
        initParams.SINA_REDIRECT_URL = SINA_REDIRECT_URL;
        initParams.SINA_SCOPE = SINA_SCOPE;

        initParams.fileProviderAuthorities = fileProviderAuthorities;
        initParams.SHARE_DEFAULT_COPY_URL = SHARE_DEFAULT_COPY_URL;
        initParams.SHARE_DEFAULT_IMAGE_URL = SHARE_DEFAULT_IMAGE_URL;
        return initParams;
    }

    public void from(@NonNull InitParams initParams){
        QQ_APP_ID = initParams.QQ_APP_ID;
        QQ_SECRET = initParams.QQ_SECRET;
        QZONE_REDIRECT_URL = initParams.QZONE_REDIRECT_URL;

        WX_APP_ID = initParams.WX_APP_ID;
        MINIPTOGRAM_TYPE_RELEASE = initParams.MINIPTOGRAM_TYPE_RELEASE;

        SINA_APP_KEY = initParams.SINA_APP_KEY;
        SINA_REDIRECT_URL = initParams.SINA_REDIRECT_URL;
        SINA_SCOPE = initParams.SINA_SCOPE;

        fileProviderAuthorities = initParams.fileProviderAuthorities;
        SHARE_DEFAULT_COPY_URL = initParams.SHARE_DEFAULT_COPY_URL;
        SHARE_DEFAULT_IMAGE_URL = initParams.SHARE_DEFAULT_IMAGE_URL;
    }

    public InitParams(){
    }
}
