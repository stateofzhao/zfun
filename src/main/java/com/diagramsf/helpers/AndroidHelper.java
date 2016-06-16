package com.diagramsf.helpers;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.UserManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.widget.TextView;

import static android.provider.Settings.System.AIRPLANE_MODE_ON;

/**
 * 与Android平台有关系的 工具方法
 * <p>
 * Created by Diagrams on 2016/2/16 18:17
 */
public class AndroidHelper {

    private static final String TAG = "AndroidHelper";

    /**
     * 获取当前登录用户的字符串表示
     */
    // 由于targetSdkVersion低于17，只能通过反射获取
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getUserSerial(Context context) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        if (userManager == null) {
            AppDebugLog.e(TAG, "userManager not exsit !!!");
            return null;
        }
        return userManager.getSerialNumberForUser(android.os.Process.myUserHandle()) + "";
    }

    /**
     * 检测是否有系统权限
     *
     * @param context    context
     * @param permission 权限字符串
     *
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
    @SuppressWarnings("unchecked")
    public static <T> T getService(Context context, String service) {
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
     * @param textView     要缩放TextSize的 {@link TextView}
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
     * @param typeface     字体类型
     * @param text         要显示的文字
     * @param maxTextSize  文字的最大尺寸
     * @param desiredWidth 文字占据空间的最大宽度
     *
     * @return 文字大小，单位是像素
     */
    public static float computeTextSizeByWidth(Typeface typeface, String text, float maxTextSize, int desiredWidth) {
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
     *
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

}
