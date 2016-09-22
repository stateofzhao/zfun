package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/15 19:19
 */
public class Utils {
  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }
}
