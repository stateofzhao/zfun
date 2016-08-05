package com.diagramsf.netvolley.simple;

import android.content.Context;
import android.support.annotation.NonNull;
import com.diagramsf.net.NetContract;
import com.diagramsf.net.base.BaseNetSuccessResult;
import com.diagramsf.netvolley.NetResultFactory;
import com.diagramsf.netvolley.NetRequestManager;

import java.util.Map;

/**
 * Created by Diagrams on 2015/10/15 18:24
 */
public class SimpleRequestPresenter implements SimpleContract.Presenter {
  private SimpleContract.View mView;
  private NetRequestManager mNetRequestManager;

  public SimpleRequestPresenter(@NonNull SimpleContract.View view, @NonNull Context context) {
    mView = view;
    mNetRequestManager = NetRequestManager.with(context);
  }

  @Override public void start() {
  }

  @Override public void destroy() {
  }

  @Override public void requestCache(String url, Map<String, String> postData, String cancelTag,
      NetResultFactory factory) {
    mView.onShowCacheLoadProgress();
    requestData(url, postData, cancelTag, factory, false, NetContract.ONLY_CACHE);
  }

  @Override public void requestNet(String url, Map<String, String> postData, String cancelTag,
      NetResultFactory factory, boolean saveCache) {
    mView.onShowNetProgress();

    if (saveCache) {
      requestData(url, postData, cancelTag, factory, true, NetContract.ONLY_NET_THEN_CACHE);
    } else {
      requestData(url, postData, cancelTag, factory, true, NetContract.ONLY_NET_NO_CACHE);
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

  public void onResultFromCache(NetContract.Result result) {
    mView.onHideCacheLoadProgress();
    mView.onShowCacheResult(result);
  }

  public void onNoResultFromCache() {
    mView.onHideCacheLoadProgress();
    mView.onShowNoCache();
  }

  public void onFailFromCache(NetContract.Fail result) {
    mView.onHideCacheLoadProgress();
    mView.onShowCacheFail(result);
  }

  public void onResultFromNet(NetContract.Result result) {
    mView.onHideNetProgress();
    mView.onShowNetResult(result);
  }

  public void onFailFromNet(NetContract.Fail result) {
    mView.onHideNetProgress();
    mView.onShowNetFail(result);
  }

  private void requestData(String url, Map<String, String> postData, String cancelTag,
      NetResultFactory<BaseNetSuccessResult> factory, final boolean fromNet,
      @NetContract.Type int type) {
    mNetRequestManager.<BaseNetSuccessResult>load(url).postData(postData)
        .cancelTag(cancelTag)
        .type(type)
        .errorListener(new NetContract.ErrorListener() {
          @Override public void onFailed(NetContract.Fail fail) {
            if (fromNet) {
              onFailFromNet(fail);
            } else {
              onFailFromCache(fail);
            }
          }
        })
        .listener(new NetContract.Listener<BaseNetSuccessResult>() {
          @Override public void onSucceed(BaseNetSuccessResult result) {
            if (fromNet) {
              onResultFromNet(result);
            } else {
              if (null == result) {
                onNoResultFromCache();
              } else {
                onResultFromCache(result);
              }
            }
          }
        })
        .into(factory);
  }
}
