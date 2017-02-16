package com.diagramsf.util;

import android.util.Log;
import com.diagramsf.BuildConfig;

/**
 * 打印Log的工具
 * <p>
 * create by Diagrams
 */
public class AppLog {

  public static void logSystemOut(String log) {
    if (BuildConfig.DEBUG) {
      System.out.println(log);
    }
  }

  public static void logError(String tag, String msg) {
    if (BuildConfig.DEBUG) {
      Log.e(tag, msg);
    }
  }

  public static void e(String tag, String msg) {
    if (BuildConfig.DEBUG) {
      Log.e(tag, msg);
    }
  }

  public static void d(String tag, String msg) {
    if (BuildConfig.DEBUG) {
      Log.d(tag, msg);
    }
  }

  public static void i(String tag, String msg) {
    if (BuildConfig.DEBUG) {
      Log.i(tag, msg);
    }
  }

  public static void v(String tag, String msg) {
    if (BuildConfig.DEBUG) {
      Log.v(tag, msg);
    }
  }

  public static void w(String tag, String msg) {
    if (BuildConfig.DEBUG) {
      Log.w(tag, msg);
    }
  }

  public static void wtf(String tag, String msg) {
    if (BuildConfig.DEBUG) {
      Log.wtf(tag, msg);
    }
  }
}
