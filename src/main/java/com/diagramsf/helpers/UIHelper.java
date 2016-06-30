package com.diagramsf.helpers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.FeatureInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.*;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.diagramsf.StaticBuildConfig;

import java.io.FileDescriptor;
import java.lang.reflect.Field;

public class UIHelper {

    private static final String TAG = "UIHelper";

    /**
     * 应用内提示信息
     */
    public static void showAppToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    /**
     * 根据服务器返回的错误码 弹出错误提示
     */
    public static void showEEErorr(Context context, String ee) {

        showAppToast(context, StaticBuildConfig.SERVICE_ERROR_CODE.get(ee));
    }


    /** 隐藏键盘 */
    public static void hideSoftInput(Context context, View paramEditText) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(paramEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /** 显示键盘 */
    public static void showKeyBoard(Context context, final View paramEditText) {
        final Context applicationContext = context.getApplicationContext();
        paramEditText.requestFocus();
        paramEditText.post(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) applicationContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(
                        paramEditText, 0);
            }
        });
    }


    /** 测量字符的宽度 */
    public static float measureTextWith(float textSize, String text) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        return paint.measureText(text);
    }

    /** 测量字符的宽度 */
    public static float measureTextWith(float textSize, String text, Paint paint) {
        float textSize_ = paint.getTextSize();
        paint.setTextSize(textSize);
        float with = paint.measureText(text);
        paint.setTextSize(textSize_);
        return with;
    }

    /** 测量并获取View的宽度 */
    public static int measureCellWidth(Context context, View cell) {

        // We need a fake parent
        FrameLayout buffer = new FrameLayout(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        buffer.addView(cell, layoutParams);

        cell.forceLayout();
        cell.measure(1000, 1000);

        int width = cell.getMeasuredWidth();

        buffer.removeAllViews();

        return width;
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

    /** 取得屏幕的宽度 */
    public static int getScreenWidth(Context act) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();

        WindowManager wm = (WindowManager) act
                .getSystemService(Context.WINDOW_SERVICE);

        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;

    }

    /** 取得屏幕高度 */
    public static int getScreenHeight(Context act) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) act
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /** 取得屏幕 宽度的 dp值 */
    public static float getScreenWidthDp(Activity act) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float midu = (float) displayMetrics.densityDpi;

        return displayMetrics.widthPixels / (midu / 160);
    }

    /** 获取屏幕的 dpi值 */
    public static float getScreenDPI(Context context) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        return (float) displayMetrics.densityDpi;
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
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight,
                    View.MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /** 对TextView设置不同状态时其文字颜色。 */
    public static ColorStateList createColorStateList(int normal, int pressed,
                                                      int focused, int unable) {
        int[] colors = new int[]{pressed, focused, normal, focused, unable,
                normal};
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_pressed,
                android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled,
                android.R.attr.state_focused};
        states[2] = new int[]{android.R.attr.state_enabled};
        states[3] = new int[]{android.R.attr.state_focused};
        states[4] = new int[]{android.R.attr.state_window_focused};
        states[5] = new int[]{};
        return new ColorStateList(states, colors);
    }

    /**
     * 计算缩放尺寸的 高度，
     *
     * @param orangeWith   原始 宽度
     * @param orangeHeight 原始 高度
     * @param desWith      目标宽度
     *
     * @return 目标高度
     */
    public static int calculationWithAndHeight(int orangeWith,
                                               int orangeHeight, int desWith) {

        return (orangeHeight * desWith) / orangeWith;

    }


    /**
     * Decode and sample down a bitmap from resources to the requested width and
     * height.
     *
     * @param res       The resources object containing the image data
     * @param resId     The resource id of the image data
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     *
     * @return A bitmap sampled down from the original with the same aspect
     * ratio and dimensions that are equal to or greater than the
     * requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight) {

        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // END_INCLUDE (read_bitmap_dimensions)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and
     * height.
     *
     * @param filename  The full path of the file to decode
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     *
     * @return A bitmap sampled down from the original with the same aspect
     * ratio and dimensions that are equal to or greater than the
     * requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested
     * width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth       The requested width of the resulting bitmap
     * @param reqHeight      The requested height of the resulting bitmap
     *
     * @return A bitmap sampled down from the original with the same aspect
     * ratio and dimensions that are equal to or greater than the
     * requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory
                .decodeFileDescriptor(fileDescriptor, null, options);
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
     * @param options   An options object with out* params already populated (run
     *                  through a decode* method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     *
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                inSampleSize = (int) Math.floor((float) width
                        / (float) reqWidth);
            } else if (reqWidth == 0) {
                inSampleSize = (int) Math.floor((float) height
                        / (float) reqHeight);
            } else {
                heightRatio = (int) Math.floor((float) height
                        / (float) reqHeight);
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

    // --------------------------------处理选择本地图片返回的uri--start

    /**
     * 根据相册返回的 uri来获得 选择的图片路径</p> 4.4 以下使用
     *
     * @param context    context
     * @param contentUri 在相册选择完图片后，返回的uri
     *
     * @deprecated
     */
    public static String getRealPathFromMediaUriOld(Context context,
                                                    Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            if (null == cursor)
                return "";
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据相册返回的 uri来获得 选择的图片路径</p> 所有版本andriod系统都可以用
     *
     * @param context context
     * @param uri     在相册选择完图片后，返回的uri
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getRealPathFromMediaUri(final Context context,
                                                 final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     *
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     *
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     *
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     *
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     *
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }// --------------------------------处理选择本地图片返回的uri--end

    public static void canCloseDialog(DialogInterface dialogInterface,
                                      boolean close) {
        try {
            Field field = dialogInterface.getClass().getSuperclass()
                    .getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialogInterface, close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 带下划线的导航，点击时需要让下划线左右滚动.
     *
     * @param currentView      当前下划线所在的View
     * @param desView          要把下划线移动到的目的View
     * @param lineView         下划线View
     * @param lineViewInitLeft 下划线最开始所在位置的 getLeft()
     */
    public static void doLineAnimator(View currentView, View desView,
                                      View lineView, int lineViewInitLeft) {
        // ViewPropertyAnimator.animate(view).translationXBy(transDistance)
        // .start();

        AnimationSet as = null;
        int currentViewWidth = currentView.getWidth();
        int desViewWidth = desView.getWidth();
        int lineViewWidth = lineView.getWidth();

        float scale = (float) desViewWidth / lineViewWidth;
        float privotXValue;
        if (scale > 1.0f) {
            privotXValue = 0.0f;
        } else {
            privotXValue = 0.5f;
        }

        if (currentViewWidth != desViewWidth) {
            as = new AnimationSet(true);
            ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, scale, 1.0f,
                    1.0f, Animation.RELATIVE_TO_SELF, privotXValue,
                    Animation.RELATIVE_TO_SELF, 0.0f);
            as.setFillAfter(true);
            as.addAnimation(scaleAnim);
        }

        TranslateAnimation translateIn = new TranslateAnimation(
                currentView.getLeft() - lineViewInitLeft, desView.getLeft()
                - lineViewInitLeft, 0, 0);
        translateIn.setDuration(500);
        translateIn.setFillAfter(true);
        translateIn.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }
        });

        if (null != as) {
            as.addAnimation(translateIn);
            lineView.startAnimation(as);
        } else {
            lineView.startAnimation(translateIn);
        }

    }

    /**
     * 生成系统默认样式的通知
     *
     * @param context        {@link Context}
     * @param title          通知标题
     * @param text           通知内容
     * @param smallIcon      状态栏上显示的icon的id，如果是android5.0 及以上版本，这个也会同时显示在通知中的大图上
     * @param largeIcon      通知上的左边大图的 资源id
     * @param notificationId 通知的id
     * @param intent         点击通知的行为
     */
    public static void makeDefaultNotification(Context context, String title,
                                               String text, int smallIcon, int largeIcon, int notificationId, PendingIntent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder defaultBuilder = new NotificationCompat.Builder(context);
        defaultBuilder.setAutoCancel(true);
        defaultBuilder.setSmallIcon(smallIcon);
        defaultBuilder.setContentText(text);
        defaultBuilder.setContentTitle(title);
        Bitmap largeBitmap = BitmapFactory.decodeResource(context.getResources(), largeIcon);
        defaultBuilder.setLargeIcon(largeBitmap);
        if (null != intent)
            defaultBuilder.setContentIntent(intent);
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

        FeatureInfo[] features = context.getApplicationContext().getPackageManager().getSystemAvailableFeatures();
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
    public static boolean isLand(Activity context) {
        Display getOrient = context.getWindowManager().getDefaultDisplay();
        return getOrient.getWidth() > getOrient.getHeight();
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
    public static Fragment setContainerFragmentOnly(FragmentActivity container, int containerViewId, Fragment f, String tag) {
        FragmentManager fragmentManager = container.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (null != fragment) {
            return fragment;
        }
        fragment = f;

        if (-1 == containerViewId) {
            container.getSupportFragmentManager()
                    .beginTransaction().add(fragment, tag).commit();
        } else {
            container.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(containerViewId, fragment, tag)
                    .commit();
        }

        return fragment;
    }

    /** 设置内容Fragment，一个tag只能存在一个Fragment，会自动排重 */
    public static Fragment setContainerFragmentOnly(Fragment container, int containerViewId, Fragment f, String tag) {
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
    public static void checkTextViewEllipsized(@NonNull final TextView textView, final CheckTextEllipsizedCallback callback) {
        ViewTreeObserver vto = textView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
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
                            AppDebugLog.d(TAG, "Text is ellipsized");
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

    public interface CheckTextEllipsizedCallback {
        void onEllipsized();

        void onNoEllipsized();
    }

    /** 帮助实现一个页面多个标签切换时Fragment的显示和隐藏 */
    public abstract static class FragmentAttachHelper {

        private static final String TAG = "FragmentAttachHelper";
        private static final boolean DEBUG = false;

        FragmentManager fragmentManager;

        public FragmentAttachHelper(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
        }

        private static String makeFragmentName(int viewId, long id) {
            return "UIHelper:switcher:" + viewId + ":" + id;
        }

        public abstract Fragment getFragment(int position);

        //        private HashMap<Integer, Fragment.SavedState> mSavedState = new HashMap<>();

        /**
         * 在指定位置 attach一个Fragment，如果Fragment已经 attach了，直接返回 Fragment
         *
         * @param show attach fragment后 是否显示出来
         */
        public Fragment attach(ViewGroup container, int position, boolean show) {
            FragmentTransaction curTransaction = fragmentManager.beginTransaction();

            String name = makeFragmentName(container.getId(), position);
            Fragment fragment = fragmentManager.findFragmentByTag(name);

            if (fragment != null) {
                if (fragment.isDetached()) {
                    if (DEBUG) Log.v(TAG, "Attaching item #" + position + ": f=" + fragment);

                    //恢复数据
                    //                    Fragment.SavedState fss = mSavedState.get(position);
                    //                    if (fss != null) {
                    //                        fragment.setInitialSavedState(fss);
                    //                    }

                    curTransaction.attach(fragment);
                }
            } else {
                fragment = getFragment(position);
                if (DEBUG) Log.v(TAG, "Adding item #" + position + ": f=" + fragment);

                //恢复数据
                //                Fragment.SavedState fss = mSavedState.get(position);
                //                if (fss != null) {
                //                    fragment.setInitialSavedState(fss);
                //                }

                curTransaction.add(container.getId(), fragment,
                        makeFragmentName(container.getId(), position));
            }

            if (show) {
                curTransaction.show(fragment);
            } else {
                curTransaction.hide(fragment);
            }

            curTransaction.commitAllowingStateLoss();
            if (!curTransaction.isEmpty()) {
                fragmentManager.executePendingTransactions();
            }
            return fragment;
        }

        public void detach(ViewGroup container, int position) {
            String name = makeFragmentName(container.getId(), position);
            Fragment fragment = fragmentManager.findFragmentByTag(name);

            if (null == fragment) {
                return;
            }

            //保存Fragment的状态
            //            Fragment.SavedState fss = fragmentManager.saveFragmentInstanceState(fragment);
            //            mSavedState.put(position, fss);

            if (fragment.isDetached()) {
                return;
            }

            FragmentTransaction curTransaction = fragmentManager.beginTransaction();
            curTransaction.detach(fragment);
            curTransaction.commitAllowingStateLoss();
            if (!curTransaction.isEmpty()) {
                fragmentManager.executePendingTransactions();
            }
        }
    }

}
