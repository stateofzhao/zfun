package com.diagramsf.simple.usecase;

/**
 * Created by Diagrams on 2016/8/12 11:39
 */
public interface Callback<T,E> {
  void onResponse(T response);
  void onError(E error);
}
