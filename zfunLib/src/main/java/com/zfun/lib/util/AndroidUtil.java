package com.zfun.lib.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.UserManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.zfun.lib.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import static android.provider.Settings.System.AIRPLANE_MODE_ON;

/**
 * 与Android平台有关系的 工具方法
 * <p>
 * Created by zfun on 2016/2/16 18:17
 */
public class AndroidUtil {

  private static final String TAG = "AndroidUtil";

  public interface CheckTextEllipsizedCallback {
    void onEllipsized();

    void onNoEllipsized();
  }//class end

  /**
   * 在Android马上要绘制targetView时，post一个主线程操作
   *
   * @return 可以使用{@link #removePreDrawListener(View,ViewTreeObserver.OnPreDrawListener)}来取消执行，防止内存泄露
   */
  public static ViewTreeObserver.OnPreDrawListener deferredActionPreDraw(final View targetView,
      final Runnable action) {
    final ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {

      @Override public boolean onPreDraw() {
        if (null == targetView || null == action) {
          return true;
        }

        ViewTreeObserver vto = targetView.getViewTreeObserver();
        if (!vto.isAlive()) {
          return true;
        }
        vto.removeOnPreDrawListener(this);
        action.run();
        return true;
      }
    };
    //添加监听
    targetView.getViewTreeObserver().addOnPreDrawListener(listener);
    return listener;
  }

  /** 针对targetView来移除在它上面注册的{@link ViewTreeObserver.OnPreDrawListener} */
  public static void removePreDrawListener(View targetView,
      ViewTreeObserver.OnPreDrawListener listener) {
    if (null == targetView || null == listener) {
      return;
    }
    ViewTreeObserver vto = targetView.getViewTreeObserver();
    if (!vto.isAlive()) {
      return;
    }
    vto.removeOnPreDrawListener(listener);
  }

  /**
   * 获取当前登录用户的字符串表示
   */
  // 由于targetSdkVersion低于17，只能通过反射获取
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) public static String getUserSerial(
      Context context) {
    UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
    if (userManager == null) {
      AppLog.e(TAG, "userManager not exsit !!!");
      return null;
    }
    return userManager.getSerialNumberForUser(android.os.Process.myUserHandle()) + "";
  }

  /**
   * 检测是否有系统权限
   *
   * @param context context
   * @param permission 权限字符串
   * @return true有；false 没有
   */
  public static boolean hasPermission(Context context, String permission) {
    return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * 根据服务名来获取 android系统服务
   *
   * @param service 例如{@link Context#WIFI_SERVICE}
   */
  @SuppressWarnings("unchecked") public static <T> T getService(Context context, String service) {
    return (T) context.getSystemService(service);
  }

  /**
   * 是否是飞行模式
   *
   * @return true是飞行模式；false不是飞行模式
   */
  public static boolean isAirplaneModeOn(Context context) {
    ContentResolver contentResolver = context.getContentResolver();
    try {
      return Settings.System.getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0;
    } catch (NullPointerException e) {
      // https://github.com/square/picasso/issues/761, some devices might crash here, assume that
      // airplane mode is off.
      return false;
    }
  }

  /**
   * 克隆实现了 {@link Parcelable}接口的类
   *
   * @deprecated 经测试，此方法并不是深度克隆
   */
  public static Parcelable cloneParcelbleClass(@NonNull Parcelable orange) {
    Bundle bundle = new Bundle();
    bundle.putParcelable("clone", orange);
    return bundle.getParcelable("clone");
  }

  /**
   * 根据指定的宽度 自动缩放TextView中文字的大小（TextSize）;<br/>
   * 来自[stackoverflow](http://stackoverflow.com/questions/4794484/calculate-text-size-according-to-width-of-text-area)
   *
   * @param textView 要缩放TextSize的 {@link TextView}
   * @param desiredWidth 文字显示的最大宽度
   */
  public static void correctTextWidth(TextView textView, int desiredWidth) {
    Paint paint = new Paint();
    Rect bounds = new Rect();

    paint.setTypeface(textView.getTypeface());
    float textSize = textView.getTextSize();
    paint.setTextSize(textSize);
    String text = textView.getText().toString();
    paint.getTextBounds(text, 0, text.length(), bounds);

    while (bounds.width() > desiredWidth) {
      textSize--;
      paint.setTextSize(textSize);
      paint.getTextBounds(text, 0, text.length(), bounds);
    }

    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
  }

  /**
   * 根据指定的宽度计算文字尺寸
   *
   * @param typeface 字体类型
   * @param text 要显示的文字
   * @param maxTextSize 文字的最大尺寸
   * @param desiredWidth 文字占据空间的最大宽度
   * @return 文字大小，单位是像素
   */
  public static float computeTextSizeByWidth(Typeface typeface, String text, float maxTextSize,
      int desiredWidth) {
    if (null == typeface) {
      typeface = Typeface.DEFAULT;
    }
    float textSize = maxTextSize;

    Paint paint = new Paint();
    Rect bounds = new Rect();

    paint.setTypeface(typeface);
    paint.setTextSize(textSize);
    paint.getTextBounds(text, 0, text.length(), bounds);

    while (bounds.width() > desiredWidth) {
      textSize--;
      paint.setTextSize(textSize);
      paint.getTextBounds(text, 0, text.length(), bounds);
    }
    return textSize;
  }

  /**
   * 获取字体的高度
   *
   * @param textSize 像素值
   * @return 单位是像素，第一个元素是字符的真实高度；第二个元素是字符显示区域的高度
   */
  public static float[] getTextHeight(Typeface typeface, float textSize) {
    if (null == typeface) {
      typeface = Typeface.DEFAULT;
    }
    Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
    textPaint.setTypeface(typeface);
    textPaint.setTextSize(textSize);//像素值

    Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
    float topY = fontMetrics.top;//指的是字符到baseLine的最高值，即ascent的最大值
    float bomY = fontMetrics.bottom;//指的是字符到baseLine的最底值，即descent的最大值
    float ascentY = fontMetrics.ascent;//这个是负值，baseLine的值是0，baseLine向上是负值，向下是正值
    float descentY = fontMetrics.descent;//这个是正直
    //        float leading = fontMetrics.leading;//行间距

    float[] result = new float[2];
    result[0] = descentY - ascentY;//字符的真实高度
    result[1] = bomY - topY;//字符显示区域的高度(字符的最大高度)

    return result;
  }

  /**
   * 获取字符串的宽度
   *
   * @return 字符串的宽度 第一个元素是 getTextWidths()；第二个元素是 字符串的显示区域宽度；第三个元素是字符串真实宽度
   */
  public static float[] getTextWidth(Typeface typeface, float textSize, String text) {
    if (null == typeface) {
      typeface = Typeface.DEFAULT;
    }
    Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
    textPaint.setTypeface(typeface);
    textPaint.setTextSize(textSize);//像素值

    float[] widths = new float[1];//这个暂时不知道 是哪个宽度
    textPaint.getTextWidths(text, widths);

    Rect bounds = new Rect();
    textPaint.getTextBounds(text, 0, text.length(), bounds);//字符串的显示区域宽度

    float mt = textPaint.measureText(text, 0, text.length());//字符串的真实宽度

    float[] result = new float[3];
    result[0] = widths[0];
    result[1] = bounds.width();
    result[2] = mt;

    return result;
  }

  /** 隐藏键盘 */
  public static void hideSoftInput(Context context, View paramEditText) {
    InputMethodManager inputManager =
        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    inputManager.hideSoftInputFromWindow(paramEditText.getWindowToken(),0);
  }

  /** 隐藏键盘 */
  public static void hideSoftInput(Activity activity) {
    final View nowServiceView = activity.getCurrentFocus();
    if(null != nowServiceView){
      InputMethodManager inputManager =
              (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(nowServiceView.getWindowToken(),0);
    }
  }

  /** 显示键盘 */
  public static void showKeyBoard(Context context, final View paramEditText) {
    final Context applicationContext = context.getApplicationContext();
    paramEditText.requestFocus();
    paramEditText.post(new Runnable() {
      @Override public void run() {
        ((InputMethodManager) applicationContext.getSystemService(
            Context.INPUT_METHOD_SERVICE)).showSoftInput(paramEditText, 0);
      }
    });
  }

  /** 将 dp 转换成px */
  public static float convertDpToPixel(Context context, float dimen) {
    float dpi = getScreenDPI(context);
    return dimen * (dpi / 160);

    // ---------这个方法和上面的返回值一样
    // return TypedValue.applyDimension(
    // TypedValue.COMPLEX_UNIT_DIP, dimen,
    // context.getResources().getDisplayMetrics());
    //

    // ---------这个方法不对，始终返回0
    // TypedValue tv = new TypedValue();
    // return TypedValue.complexToDimensionPixelSize(tv.data,
    // context.getResources().getDisplayMetrics());
  }

  /** 获取屏幕的 dpi值 */
  public static float getScreenDPI(Context context) {
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    wm.getDefaultDisplay().getMetrics(displayMetrics);

    return (float) displayMetrics.densityDpi;
  }

  /**
   * 取得屏幕的宽度
   *
   * @return 单位px
   */
  public static int getScreenWidth(Context act) {
    final DisplayMetrics displayMetrics = new DisplayMetrics();

    WindowManager wm = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);

    wm.getDefaultDisplay().getMetrics(displayMetrics);
    return displayMetrics.widthPixels;
  }

  /**
   * 取得屏幕高度
   *
   * @return 单位px
   */
  public static int getScreenHeight(Context act) {
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    WindowManager wm = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
    wm.getDefaultDisplay().getMetrics(displayMetrics);
    return displayMetrics.heightPixels;
  }

  /**
   * 取得屏幕 宽度的 dp值
   *
   * @return 单位dp
   */
  public static float getScreenWidthDp(Activity act) {
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    float midu = (float) displayMetrics.densityDpi;

    return displayMetrics.widthPixels / (midu / 160);
  }

  /**
   * 用于测量指定View的宽高参数
   *
   * @param child 要测量的View
   */
  public static void measureView(View child) {
    ViewGroup.LayoutParams p = child.getLayoutParams();
    if (p == null) {
      p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
    int lpHeight = p.height;
    int childHeightSpec;
    if (lpHeight > 0) {
      childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY);
    } else {
      childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }
    child.measure(childWidthSpec, childHeightSpec);
  }

  /** 对TextView设置不同状态时其文字颜色。 */
  public static ColorStateList createColorStateList(int normal, int pressed, int focused,
      int unable) {
    int[] colors = new int[] {
        pressed, focused, normal, focused, unable, normal
    };
    int[][] states = new int[6][];
    states[0] = new int[] {
        android.R.attr.state_pressed, android.R.attr.state_enabled
    };
    states[1] = new int[] {
        android.R.attr.state_enabled, android.R.attr.state_focused
    };
    states[2] = new int[] { android.R.attr.state_enabled };
    states[3] = new int[] { android.R.attr.state_focused };
    states[4] = new int[] { android.R.attr.state_window_focused };
    states[5] = new int[] {};
    return new ColorStateList(states, colors);
  }

  /**
   * Decode and sample down a bitmap from resources to the requested width and
   * height.
   *
   * @param res The resources object containing the image data
   * @param resId The resource id of the image data
   * @param reqWidth The requested width of the resulting bitmap
   * @param reqHeight The requested height of the resulting bitmap
   * @return A bitmap sampled down from the original with the same aspect
   * ratio and dimensions that are equal to or greater than the
   * requested width and height
   */
  public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth,
      int reqHeight) {

    // BEGIN_INCLUDE (read_bitmap_dimensions)
    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, resId, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
    // END_INCLUDE (read_bitmap_dimensions)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, resId, options);
  }

  /**
   * Decode and sample down a bitmap from a file to the requested width and
   * height.
   *
   * @param filename The full path of the file to decode
   * @param reqWidth The requested width of the resulting bitmap
   * @param reqHeight The requested height of the resulting bitmap
   * @return A bitmap sampled down from the original with the same aspect
   * ratio and dimensions that are equal to or greater than the
   * requested width and height
   */
  public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(filename, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeFile(filename, options);
  }

  /**
   * Decode and sample down a bitmap from a file input stream to the requested
   * width and height.
   *
   * @param fileDescriptor The file descriptor to read from
   * @param reqWidth The requested width of the resulting bitmap
   * @param reqHeight The requested height of the resulting bitmap
   * @return A bitmap sampled down from the original with the same aspect
   * ratio and dimensions that are equal to or greater than the
   * requested width and height
   */
  public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor,
      int reqWidth, int reqHeight) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;

    return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
  }

  /**
   * Calculate an inSampleSize for use in a
   * {@link BitmapFactory.Options} object when decoding
   * bitmaps using the decode* methods from
   * {@link BitmapFactory}. This implementation calculates
   * the closest inSampleSize that is a power of 2 and will result in the
   * final decoded bitmap having a width and height equal to or larger than
   * the requested width and height.
   *
   * @param options An options object with out* params already populated (run
   * through a decode* method with inJustDecodeBounds==true
   * @param reqWidth The requested width of the resulting bitmap
   * @param reqHeight The requested height of the resulting bitmap
   * @return The value to be used for inSampleSize
   */
  public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
      int reqHeight) {
    // BEGIN_INCLUDE (calculate_sample_size)
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      final int heightRatio;
      final int widthRatio;
      if (reqHeight == 0) {
        inSampleSize = (int) Math.floor((float) width / (float) reqWidth);
      } else if (reqWidth == 0) {
        inSampleSize = (int) Math.floor((float) height / (float) reqHeight);
      } else {
        heightRatio = (int) Math.floor((float) height / (float) reqHeight);
        widthRatio = (int) Math.floor((float) width / (float) reqWidth);
        inSampleSize = Math.min(heightRatio, widthRatio);
        // request.centerInside
        // ? Math.max(heightRatio, widthRatio)
        // : Math.min(heightRatio, widthRatio);
      }
    }
    //
    // if (height > reqHeight || width > reqWidth) {
    //
    // final int halfHeight = height / 2;
    // final int halfWidth = width / 2;
    //
    // // Calculate the largest inSampleSize value that is a power of 2 and
    // // keeps both
    // // height and width larger than the requested height and width.
    // while ((halfHeight / inSampleSize) > reqHeight
    // && (halfWidth / inSampleSize) > reqWidth) {
    // inSampleSize *= 2;
    // }
    //
    // // This offers some additional logic in case the image has a strange
    // // aspect ratio. For example, a panorama may have a much larger
    // // width than height. In these cases the total pixels might still
    // // end up being too large to fit comfortably in memory, so we should
    // // be more aggressive with sample down the image (=larger
    // // inSampleSize).
    //
    // long totalPixels = width * height / inSampleSize;
    //
    // // Anything more than 2x the requested pixels we'll sample down
    // // further
    // final long totalReqPixelsCap = reqWidth * reqHeight * 2;
    //
    // while (totalPixels > totalReqPixelsCap) {
    // inSampleSize *= 2;
    // totalPixels /= 2;
    // }
    // }
    return inSampleSize;
    // END_INCLUDE (calculate_sample_size)
  }

  /**
   * 生成系统默认样式的通知
   *
   * @param context {@link Context}
   * @param title 通知标题
   * @param text 通知内容
   * @param smallIcon 状态栏上显示的icon的id，如果是android5.0 及以上版本，这个也会同时显示在通知中的大图上
   * @param largeIcon 通知上的左边大图的 资源id
   * @param notificationId 通知的id
   * @param intent 点击通知的行为
   */
  public static void makeDefaultNotification(Context context, String title, String text,
      int smallIcon, int largeIcon, int notificationId, PendingIntent intent) {
    NotificationManager notificationManager = (NotificationManager) context.getApplicationContext()
        .getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder defaultBuilder = new NotificationCompat.Builder(context);
    defaultBuilder.setAutoCancel(true);
    defaultBuilder.setSmallIcon(smallIcon);
    defaultBuilder.setContentText(text);
    defaultBuilder.setContentTitle(title);
    Bitmap largeBitmap = BitmapFactory.decodeResource(context.getResources(), largeIcon);
    defaultBuilder.setLargeIcon(largeBitmap);
    if (null != intent) {
      defaultBuilder.setContentIntent(intent);
    }
    Notification defaultNotification = defaultBuilder.build();
    notificationManager.notify(notificationId, defaultNotification);
  }

  /**
   * 是否是 HTC SenseDevice
   * <p/>
   * 参照：<a href="https://github.com/appcelerator/titanium_mobile/blob/master/android/modules/ui/src/java/ti/modules/titanium/ui/widget/webview/TiUIWebView.java">Github</a>
   * 来区分的
   */
  public static boolean isHTCSenseDevice(Context context) {
    boolean isHTC = false;

    FeatureInfo[] features =
        context.getApplicationContext().getPackageManager().getSystemAvailableFeatures();
    if (features == null) {
      return isHTC;
    }
    for (FeatureInfo f : features) {
      String fName = f.name;
      if (fName != null) {
        isHTC = fName.contains("com.htc.software.Sense");
        if (isHTC) {
          Log.i(TAG, "Detected com.htc.software.Sense feature " + fName);
          break;
        }
      }
    }

    return isHTC;
  }

  /**
   * 是否是横屏
   *
   * @return true横屏；false非横屏
   */
  public static boolean isLand(Configuration config) {
    return config.orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  /**
   * 设置内容Fragment，一个tag只能存在一个Fragment，会自动排重
   *
   * @param containerViewId 如果是-1的话，表示不向指定的布局中添加
   */
  public static Fragment setContainerFragmentOnly(FragmentActivity container, int containerViewId,
      Fragment f, String tag) {
    FragmentManager fragmentManager = container.getSupportFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag(tag);
    if (null != fragment) {
      return fragment;
    }
    fragment = f;

    if (-1 == containerViewId) {
      container.getSupportFragmentManager().beginTransaction().add(fragment, tag).commit();
    } else {
      container.getSupportFragmentManager()
          .beginTransaction()
          .replace(containerViewId, fragment, tag)
          .commit();
    }

    return fragment;
  }

  /** 设置内容Fragment，一个tag只能存在一个Fragment，会自动排重 */
  public static Fragment setContainerFragmentOnly(Fragment container, int containerViewId,
      Fragment f, String tag) {
    FragmentManager fragmentManager = container.getChildFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag(tag);
    if (null != fragment) {
      return fragment;
    }
    fragment = f;
    container.getChildFragmentManager()
        .beginTransaction()
        .replace(containerViewId, fragment, tag)
        .commit();

    return fragment;
  }

  /**
   * 监听{@link TextView}是否省略了其中的内容，如果省略在主线程中执行task
   *
   * @param textView 检测是否省略了其中内容的TextView
   * @param callback 检测到TextView省略其中内容后在主线程中调用
   */
  public static void checkTextViewEllipsized(@NonNull final TextView textView,
      final CheckTextEllipsizedCallback callback) {
    ViewTreeObserver vto = textView.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        if (textView.getViewTreeObserver().isAlive()) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          } else {
            textView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
          }
        }
        Layout l = textView.getLayout();
        if (l != null) {
          int lines = l.getLineCount();
          if (lines > 0) {
            if (l.getEllipsisCount(lines - 1) > 0) {
              AppLog.d(TAG, "Text is ellipsized");
              callback.onEllipsized();
            } else {
              callback.onNoEllipsized();
            }
          } else {
            callback.onNoEllipsized();
          }
        } else {
          callback.onNoEllipsized();
        }
      }
    });
  }

  /**
   * 锁定竖屏
   * <p/>
   * 在{@link Activity#onConfigurationChanged(Configuration)}或者{@link Activity#onConfigurationChanged(Configuration)}
   * 中调用
   */
  public static void lockScreenOrientation(Configuration newConfig, Activity activityForLocked) {
    // Checks the orientation of the screen for landscape and portrait and set portrait mode always
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      activityForLocked.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
      activityForLocked.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
  }

  /**
   * 保存异常日志
   *
   * @param excp 异常信息
   */
  public static void saveErrorLog(Exception excp, String sdDirName) {
    String errorlog = "errorlog.txt";
    String savePath;
    String logFilePath = "";
    FileWriter fw = null;
    PrintWriter pw = null;
    try {
      // 判断是否挂载了SD卡
      String storageState = Environment.getExternalStorageState();
      if (storageState.equals(Environment.MEDIA_MOUNTED)) {
        savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + sdDirName;
        File file = new File(savePath);
        if (!file.exists()) {
          file.mkdirs();
        }
        logFilePath = savePath + errorlog;
      }
      // 没有挂载SD卡，无法写文件
      if (logFilePath.equals("")) {
        return;
      }
      File logFile = new File(logFilePath);
      if (!logFile.exists()) {
        logFile.createNewFile();
      }
      fw = new FileWriter(logFile, true);
      pw = new PrintWriter(fw);
      pw.println("--------------------" + (new Date().toLocaleString()) + "---------------------");
      excp.printStackTrace(pw);
      pw.close();
      fw.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (pw != null) {
        pw.close();
      }
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 获取APP崩溃异常报告
   */
  public static String createCrashReport(Context context, Throwable ex) {
    PackageInfo info = null;
    try {
      info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace(System.err);
    }
    if (info == null) {
      info = new PackageInfo();
    }

    StringBuilder exceptionStr = new StringBuilder();
    exceptionStr.append("Version: ")
        .append(info.versionName)
        .append("(")
        .append(info.versionCode)
        .append(")\n");
    exceptionStr.append("Android: ")
        .append(android.os.Build.VERSION.RELEASE)
        .append("(")
        .append(android.os.Build.MODEL)
        .append(")\n");
    exceptionStr.append("Exception: ").append(ex.getMessage()).append("\n");
    StackTraceElement[] elements = ex.getStackTrace();
    for (StackTraceElement element : elements) {
      exceptionStr.append(element.toString()).append("\n");
    }
    return exceptionStr.toString();
  }

  /**
   * 发送App异常崩溃报告
   */
  public static void sendAppCrashReport(final Context cont, final String crashReport,
      final String email, final String subject, final String title) {
    AlertDialog.Builder builder = new AlertDialog.Builder(cont);
    builder.setIcon(android.R.drawable.ic_dialog_info);
    builder.setTitle(R.string.com_zfun_app_error);
    builder.setMessage(R.string.com_zfun_app_error_message);
    builder.setPositiveButton(R.string.com_zfun_submit_report,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            // 发送异常报告
            Intent i = new Intent(Intent.ACTION_SEND);
            // i.setType("text/plain"); //模拟器
            i.setType("message/rfc822"); // 真机
            i.putExtra(Intent.EXTRA_EMAIL, new String[] { email });// 输入邮箱地址，格式是：用户名@xxx.com
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, crashReport);
            cont.startActivity(Intent.createChooser(i, title));
            // 退出
            AppManager.getAppManager().AppExit(cont);
          }
        });
    builder.setNegativeButton(R.string.com_zfun_sure, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        // 退出
        AppManager.getAppManager().AppExit(cont);
      }
    });
    builder.show();
  }
}
