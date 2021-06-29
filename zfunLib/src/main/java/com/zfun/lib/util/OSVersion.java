package com.zfun.lib.util;

import android.os.Build;

/**
 * Class containing some static utility methods.
 */
public class OSVersion {

  private OSVersion() {
  }

  public static void enableStrictMode() {
  }

  /** 获得操作系统版本 */
  public static String getOs_Version() {
    if (null != Build.VERSION.RELEASE) {
      return Build.VERSION.RELEASE;
    }
    return "";
  }

  /**
   * 1.0 API 1.0
   */
  public static boolean has1() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed
    // behavior.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE;
  }

  /**
   * 1.1 API 2
   */
  public static boolean has2() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed
    // behavior.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE_1_1;
  }

  /**
   * 1.5 API 3 ，NDK 1
   */
  public static boolean hasCupcake() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed
    // behavior.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE;
  }

  /**
   * 1.6 API 4 ，NDK 2
   */
  public static boolean hasDonut() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed
    // behavior.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT;
  }

  /**
   * 2.0 API 5
   */
  public static boolean hasEclair() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed
    // behavior.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR;
  }


  /**
   * 2.0.1 API 6
   */
  public static boolean hasEclair01() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed
    // behavior.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_0_1;
  }


  /**
   * 2.1 API 7 , NDK 3
   */
  public static boolean hasEclairMR1() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed
    // behavior.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1;
  }

  /**
   * 2.2 API 8 ，NDK 4
   */
  public static boolean hasFroyo() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed
    // behavior.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
  }

  /**
   *  2.3–2.3.2 API 9， NDK 5
   */
  public static boolean hasGingerbread() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
  }

  /**
   *  2.3.3–2.3.7 API 10， NDK 5
   */
  public static boolean hasGingerbreadMR1(){
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1;
  }

  /**
   * 3.0 API 11
   */
  public static boolean hasHoneycomb() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
  }

  /**
   * 3.1 API 12 ,NDK 6
   */
  public static boolean hasHoneycombMR1() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
  }

  /**
   * 3.2.x API 13
   */
  public static boolean hasHoneycombMR2() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
  }

  /**
   * 4.0-4.0.2 API 14 ,NDK 7
   */
  public static boolean hasIceCreamSandwich() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
  }

  /**
   *4.0.3-4.0.4 API 15 , NDK 8
   */
  public static boolean hasIceCreamSandwichMR1() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
  }

  /**
   * 4.1.x API 16
   */
  public static boolean hasJellyBean() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
  }

  /**
   * 4.2.x API 17
   */
  public static boolean hasJellyBeanMR1() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
  }

  /**
   * 4.3.x API 18
   */
  public static boolean hasJellyBeanMR2() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
  }


  /**
   * 4.4 API19
   */
  public static boolean hasKitKat() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
  }

  /**
   * 5.0.1 API 21
   */
  public static boolean hasL() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  /**
   * 5.1.1 API 22
   */
  public static boolean hasLMR1() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
  }

  /**
   * 6.0 API 23
   */
  public static boolean hasM() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  }

  /**
   * 7.0 API 24
   */
  public static boolean hasN() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
  }
}
