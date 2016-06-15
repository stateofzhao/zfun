package com.diagramsf.netvolley.netrepository;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.diagramsf.exceptions.AppException;
import com.diagramsf.helpers.AppDebugLog;
import com.diagramsf.helpers.StringUtils;
import com.diagramsf.net.NetRequest;
import com.diagramsf.net.comm.NetFailResultComm;
import com.diagramsf.netvolley.NetResultFactory;
import com.diagramsf.netvolley.Utils;
import com.diagramsf.netvolley.VolleyErrorHelper;
import com.diagramsf.netvolley.volleyrequest.VolleyNetRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * {@link NetRequest}的实现类
 * <p/>
 * Created by Diagrams on 2015/12/7 17:54
 */
public class NetRequestImpl implements NetRequest {

    private NetRequest.NetResultCallback mCallback;
    private Object mDeliverToResultTag;
    private Object mCancelRequestTag;

    private VolleyNetRequest mVolleyRequest;

    private Response.Listener<NetSuccessResult> mSuccessListener = new Response.Listener<NetSuccessResult>() {

        @Override
        public void onResponse(NetSuccessResult response) {
            //在这里做进一步封装，将 setDeliverToResultTag()方法设置的值传递回去
            if (null != mCallback) {
                //这里要注意response的null情况
                if (null != mDeliverToResultTag && null != response) {
                    response.setRequestDeliveredTag(mDeliverToResultTag);
                }
                mCallback.onSucceed(response);
            }
        }
    };
    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

            //在这里统一封装处理一下
            if (null != mCallback) {
                AppException e = VolleyErrorHelper.formatVolleyError(error);
                NetFailResultComm fr = new NetFailResultComm(e, error);
                //将 setDeliverToResultTag()方法设置的值传递回去
                if (null != mDeliverToResultTag) {
                    fr.setDeliverToResultTag(mDeliverToResultTag);
                }
                mCallback.onFailed(fr);
            }
        }
    };

    /**
     * @param method   {@link Request.Method}
     * @param url      接口连接
     * @param postData post传递的参数
     * @param factory  数据结果生成的工厂
     */
    public NetRequestImpl(int method, String url, String postData,
                          NetResultFactory factory) {
        init(method, url, null, postData, factory);
    }

    /**
     * @param url      接口连接
     * @param postData post传递的参数
     * @param factory  数据结果生成的工厂
     */
    public NetRequestImpl(String url, String postData,
                          NetResultFactory factory) {
        init(Request.Method.DEPRECATED_GET_OR_POST, url, null, postData, factory);
    }

    /**
     * @param method  {@link Request.Method}
     * @param url     接口连接
     * @param params  post传递的参数
     * @param factory 数据结果生成的工厂
     */
    public NetRequestImpl(int method, String url, Map<String, String> params,
                          NetResultFactory factory) {
        init(method, url, params, null, factory);
    }

    /**
     * @param url     接口连接
     * @param params  post传递的参数
     * @param factory 数据结果生成的工厂
     */
    public NetRequestImpl(String url, Map<String, String> params,
                          NetResultFactory factory) {
        init(Request.Method.DEPRECATED_GET_OR_POST, url, params, null, factory);
    }

    //由于是只在构造函数中调用，所以 params和paramsStr必定有一个为空
    private void init(int method, String url, Map<String, String> params, String paramsStr,
                      NetResultFactory factory) {
        Class<? extends VolleyNetRequest> clazz = null;
        if (null != factory) {
            clazz = factory.whichRequest();
        }
        if (null != clazz) {
            if (!StringUtils.isEmpty(paramsStr)) {
                mVolleyRequest = createByClass(method, url, paramsStr, factory, clazz);
            } else {
                mVolleyRequest = createByClass(method, url, params, factory, clazz);
            }
        } else {
            //初始化Volley的请求
            mVolleyRequest = new VolleyNetRequest(Request.Method.DEPRECATED_GET_OR_POST, url,
                    params, factory, mSuccessListener, mErrorListener);
        }
    }

    /**
     * @param cancelTag 用来取消请求用的tag，
     */
    @Override
    public void doRequest(int type, Object cancelTag) {
        switch (type) {
            case ONLY_CACHE:
                mVolleyRequest.setJustReadCache();
                break;
            case ONLY_NET_NO_CACHE:
                mVolleyRequest.setShouldCache(false);
                break;
            case ONLY_NET_THEN_CACHE:
                mVolleyRequest.skipCache();
                break;
            case HTTP_HEADER_CACHE:
                //Volley默认就是此种方式请求的网络数据
                break;
            case PRIORITY_CACHE:
                mVolleyRequest.setReadCacheWithoutTimeLimit();
                break;
        }
        mVolleyRequest.setTag(cancelTag);
        try {
            Utils.getInstance().addRequest(mVolleyRequest);
        } catch (AppException e) {
            e.printStackTrace();
            String TAG = "NetRequestImpl";
            AppDebugLog.e(TAG, "request netData add to queue error : " + e.toString());
        }

    }

    @Override
    public void setDeliverToResultTag(Object tag) {
        mDeliverToResultTag = tag;
    }

    @Override
    public void setResultCallBack(NetResultCallback callback) {
        mCallback = callback;
    }

    @Override
    public void setCacheKey(String cacheKey) {
        mVolleyRequest.setCacheKey(cacheKey);
    }

    @Override
    public void cancelRequest(Object cancelTag) {
        Utils.getInstance().cancelRequest(cancelTag);
    }

    public static void cancelAllRequest(Object cancelTag){
        Utils.getInstance().cancelRequest(cancelTag);
    }

    private VolleyNetRequest createByClass(int method, String url, String postData,
                                           NetResultFactory factory, Class<? extends VolleyNetRequest> clazz) {
        //要反射的类的构造函数 参数的类型
        Class[] paramTypes = {int.class, String.class, String.class, NetResultFactory.class,
                Response.Listener.class, Response.ErrorListener.class};
        //要反射的类的构造函数 参数的值
        Object[] params = {method, url, postData, factory, mSuccessListener, mErrorListener};
        return createNetResultRequestInstance(paramTypes, params, clazz);
    }

    private VolleyNetRequest createByClass(int method, String url, Map<String, String> postParams,
                                           NetResultFactory factory, Class<? extends VolleyNetRequest> clazz) {
        //要反射的类的构造函数 参数的类型
        Class[] paramTypes = {int.class, String.class, Map.class, NetResultFactory.class,
                Response.Listener.class, Response.ErrorListener.class};
        //要反射的类的构造函数 参数的值
        Object[] params = {method, url, postParams, factory, mSuccessListener, mErrorListener};
        return createNetResultRequestInstance(paramTypes, params, clazz);
    }

    private VolleyNetRequest createNetResultRequestInstance(Class[] paramTypes, Object[] params,
                                                            Class<? extends VolleyNetRequest> clazz) {
        try {
            Constructor constructor = clazz.getConstructor(paramTypes);
            return (VolleyNetRequest) constructor.newInstance(params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("create NetRequestImpl Fail!!! info : " + e.toString()
                    + "\n" + "Not find " + clazz.getName() + " Constructor method!!!"
                    + "\n" + "Note Constructor method must is one of VolleyNetRequest`s Constructors!!!");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("create NetRequestImpl Fail!!! info : " + e.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("create NetRequestImpl Fail IllegalAccessException!!! info : " + e.toString());
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException("create NetRequestImpl Fail InstantiationException!!! info : " + e.toString());
        }
    }

}