package com.zfun.sharelib.init;


import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;

/**
 * Created by lzf on 2021/12/21 3:18 下午
 */
public class InitParams {
    private String QQ_APP_ID;
    private String QQ_SECRET;
    private String QZONE_REDIRECT_URL = "http://i.kuwo.cn/US/2013/mobile/login_ar_qq.htm";

    private String WX_APP_ID;

    private String SINA_APP_KEY;
    private String SINA_REDIRECT_URL;
    private String SINA_SCOPE;

    private String fileProviderAuthorities;//清单中注册的FileProvider的authorities属性

    //可选
    private int MINIPTOGRAM_TYPE_RELEASE = WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;

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

    public InitParams copy(){
        InitParams initParams = new InitParams();
        initParams.QQ_APP_ID = QQ_APP_ID;
        initParams.SINA_APP_KEY = SINA_APP_KEY;
        initParams.SINA_REDIRECT_URL = SINA_REDIRECT_URL;
        initParams.SINA_SCOPE = SINA_SCOPE;
        initParams.fileProviderAuthorities = fileProviderAuthorities;
        return initParams;
    }

    public InitParams(){
    }
}
