package com.diagramsf.core.netvolley.simple;

import android.content.Context;
import android.support.annotation.NonNull;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.diagramsf.core.netvolley.JsonToClassRequest;
import com.diagramsf.core.netvolley.RequestManager;
import java.util.Map;

/**
 * Created by Diagrams on 2015/10/15 18:24
 */
public class SimpleRequestPresenter implements SimpleContract.Presenter {
  private SimpleContract.View mView;
  private RequestManager mNetRequestManager;

  public SimpleRequestPresenter(@NonNull SimpleContract.View view, @NonNull Context context) {
    mView = view;
    mNetRequestManager = RequestManager.with(context);
  }

  @Override public void requestCache(String url, Map<String, String> postData, String cancelTag,
      JsonToClassRequest.ResultFactory factory) {
    mView.onShowCacheLoadProgress();
    requestData(url, postData, cancelTag, factory, false, JsonToClassRequest.ONLY_CACHE);
  }

  @Override public void requestNet(String url, Map<String, String> postData, String cancelTag,
      JsonToClassRequest.ResultFactory factory, boolean saveCache) {
    mView.onShowNetProgress();

    if (saveCache) {
      requestData(url, postData, cancelTag, factory, true, JsonToClassRequest.ONLY_NET_THEN_CACHE);
    } else {
      requestData(url, postData, cancelTag, factory, true, JsonToClassRequest.ONLY_NET_NO_CACHE);
    }
  }

  @Override public void cancelCacheRequest(String cancelTag) {
    mNetRequestManager.cancel(cancelTag);
    mView.onHideCacheLoadProgress();
  }

  @Override public void cancelNetRequest(String cancelTag) {
    mNetRequestManager.cancel(cancelTag);
    mView.onHideNetProgress();
  }

  public void onResultFromCache(SimpleContract.ResultBean result) {
    mView.onHideCacheLoadProgress();
    mView.onShowCacheResult(result);
  }

  public void onNoResultFromCache() {
    mView.onHideCacheLoadProgress();
    mView.onShowNoCache();
  }

  public void onFailFromCache(VolleyError result) {
    mView.onHideCacheLoadProgress();
    mView.onShowCacheFail(result);
  }

  public void onResultFromNet(SimpleContract.ResultBean result) {
    mView.onHideNetProgress();
    mView.onShowNetResult(result);
  }

  public void onFailFromNet(VolleyError result) {
    mView.onHideNetProgress();
    mView.onShowNetFail(result);
  }

  private void requestData(String url, Map<String, String> postData, String cancelTag,
      JsonToClassRequest.ResultFactory<SimpleContract.ResultBean> factory, final boolean fromNet,
      int type) {
    mNetRequestManager.<SimpleContract.ResultBean>load(url).postData(postData)
        .cancelTag(cancelTag)
        .type(type)
        .errorListener(new Response.ErrorListener() {
          @Override public void onErrorResponse(VolleyError error) {
            if (fromNet) {
              onFailFromNet(error);
            } else {
              onFailFromCache(error);
            }
          }
        })
        .listener(new Response.Listener<SimpleContract.ResultBean>() {
          @Override public void onResponse(SimpleContract.ResultBean response) {
            if (fromNet) {
              onResultFromNet(response);
            } else {
              if (null == response) {
                onNoResultFromCache();
              } else {
                onResultFromCache(response);
              }
            }
          }
        })
        .into(factory);
  }
}
