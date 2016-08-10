package com.diagramsf.executor;

/**
 * 将Runnable发送到指定线程。
 *
 * Created by Diagrams on 2016/8/9 11:18
 */
public interface FireThread {

  void post(Runnable runnable);
}
