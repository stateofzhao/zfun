package com.diagramsf.volleybox;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.diagramsf.helpers.Base64;
import com.diagramsf.net.NetResult;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LoginBasicRequest extends NetResultRequest {

    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";

    private String loginInfo;

    public LoginBasicRequest(int method, String url, String strRequest,
                             NetResultFactory jsonParse,
                             Response.Listener<NetResult> listener, ErrorListener errorListener) {
        super(method, url, strRequest, jsonParse, listener, errorListener);
        setShouldCache(false);
    }

    public void setLoginInfo(String uName, String pwd) {
        loginInfo = uName + ":" + pwd;
    }

    /** BASIC 加密 */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        try {
            Map<String,String> superHead = super.getHeaders();
            if (null == superHead) {
                superHead = new HashMap<>();
                superHead = Collections.synchronizedMap(superHead);
            }

            byte[] encodedPassword = loginInfo.getBytes(PROTOCOL_CHARSET);
            String encodeStr = Base64.encodeToString(encodedPassword);

            superHead.put("Authorization", "Basic " + encodeStr);
            superHead.put("User-Agent", Volley.userAgent);

            return superHead;

        } catch (UnsupportedEncodingException uee) {
            VolleyLog
                    .wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            loginInfo, PROTOCOL_CHARSET);
            return super.getHeaders();
        }
    }

    public void setRegistSanfangInfo(String openid, String pwd) {
        loginInfo = openid + ":" + pwd;
    }

    public void setRegistUserNameSanfangInfo(String openid, String userName) {
        loginInfo = openid + ":" + userName;
    }
}
