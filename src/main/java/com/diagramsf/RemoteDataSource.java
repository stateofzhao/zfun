package com.diagramsf;

import android.content.Context;
import com.diagramsf.netvolley.RequestManager;

/**
 * Created by Diagrams on 2016/7/3 11:47
 */
public class RemoteDataSource implements EntitiesDataSource {
  private RequestManager netRequestManager;

  private volatile static RemoteDataSource single;

  private RemoteDataSource(RequestManager requestManager) {
    this.netRequestManager = requestManager;
  }

  public static RemoteDataSource with(Context context) {
    if (null == single) {
      synchronized (RemoteDataSource.class) {
        if (null == single) {
          single = new RemoteDataSource(RequestManager.with(context));
        }
      }
    }
    return single;
  }

  public RequestManager.RequestCreator load(String url) {
    return netRequestManager.load(url);
  }

  /**
   * @param url 请求的网址
   * @param method {@link com.android.volley.Request.Method}中的一种
   */
  public RequestManager.RequestCreator load(String url, int method) {
    return netRequestManager.load(url, method);
  }
}
