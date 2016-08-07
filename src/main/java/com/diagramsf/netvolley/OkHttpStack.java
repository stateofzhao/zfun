package com.diagramsf.netvolley;

import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * OkHttp 封装的{@link HttpStack}
 * <p>
 * Created by Diagrams on 2016/6/27 9:58
 */
public class OkHttpStack extends HurlStack {
  private final OkUrlFactory okUrlFactory;

  public OkHttpStack() {
    this(new OkUrlFactory(new OkHttpClient()));
  }

  public OkHttpStack(OkUrlFactory okUrlFactory) {
    if (okUrlFactory == null) {
      throw new IllegalArgumentException("Client must not be null.");
    }
    this.okUrlFactory = okUrlFactory;
  }

  @Override protected HttpURLConnection createConnection(URL url) throws IOException {
    return okUrlFactory.open(url);
  }
}
