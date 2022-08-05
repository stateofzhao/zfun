package com.zfun.sharelib;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.zfun.sharelib.core.ShareConstant;
import com.zfun.sharelib.init.IHttpPicDownloader;
import com.zfun.sharelib.init.InternalShareInitBridge;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by lzf on 2021/12/22 2:13 下午
 */
public class ShareUtils {

    public static boolean isSupportSmallAppShare(Context context) {
        IWXAPI api = SdkApiProvider.getWXAPI(context);
        return api.getWXAppSupportAPI() >= Build.MINIPROGRAM_SUPPORTED_SDK_INT;
    }

    public static boolean isSupportMusicVideoShare(Context context) {
        IWXAPI api = SdkApiProvider.getWXAPI(context);
        return api.getWXAppSupportAPI() >= 0x28000000;
    }


    public static String getShareFilePath(Context context, File file, String packageName) {
        Uri uri = getUriForFile(context, file);
        context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return uri.toString();
    }

    public static Uri getUriForFile(Context context, File file) {
        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                uri = FileProvider.getUriForFile(context.getApplicationContext(), InternalShareInitBridge.getInstance().getInitParams().getFileProviderAuthorities(), file);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                uri = null;
            }
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    @NonNull
    public static ArrayList<String> checkQQZoneImageUrls(ArrayList<String> imageUrls) {
        if (null != imageUrls) {
            for (int i = imageUrls.size() - 1; i >= 0; --i) {
                String str = imageUrls.get(i);
                if (TextUtils.isEmpty(str) || str.equals("NO_PIC")) {
                    imageUrls.remove(i);
                }
            }
        }
        if (null == imageUrls || imageUrls.size() == 0) {
            imageUrls = new ArrayList<>();
            imageUrls.add(ShareConstant.SHARE_DEFAULT_IMAGE);
        }
        return imageUrls;
    }

    @Nullable
    public static ArrayList<String> getQQZoneImageUrls(String imageUrl) {
        ArrayList<String> imageUrls = null;
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("NO_PIC")) {
            imageUrls = new ArrayList<>();
            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    public static Bitmap decodeSizeBitmapFromFile(String path, int size) {
        File file = new File(path);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) {
            return null;
        }
        int bmpSize = 0;
        FileInputStream fis = null;
        try {
            if (file.exists()) {
                fis = new FileInputStream(file);
                bmpSize = fis.available();
            }
        } catch (Exception ignore) {
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException ignore) {

                }
            }
        }

        if (bmpSize > size * 1024) {
            int multiple = bmpSize / size / 1024;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = (int) Math.sqrt(multiple);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(path, opts);
        }
        return bitmap;
    }

    /**
     * 获取网络图片的本地路径
     */
    @Nullable
    public static Cancelable getNetPicLocalPath(final String picUrl, final OnCallback callback) {
        if (null == callback) {
            return null;
        }
        if (TextUtils.isEmpty(picUrl)) {
            callback.onResult("");
            return null;
        }
        if (!picUrl.startsWith("http")) {//直接认为是本地文件
            callback.onResult(picUrl);
        }
        final IHttpPicDownloader picDownloader = InternalShareInitBridge.getInstance().getPicDownloader();
        if (null == picDownloader) {
            callback.onResult("");
            return null;
        }
        final Cancelable cancelable = new Cancelable();
        InternalShareInitBridge.getInstance().getMessageHandler().runInOtherThread(new Runnable() {
            @Override
            public void run() {
                if (cancelable.isCancel) {
                    return;
                }
                String picPath = picDownloader.downPic(picUrl);
                if (cancelable.isCancel) {
                    return;
                }
                if (TextUtils.isEmpty(picPath)) {
                    callback.onResult("");
                } else {
                    callback.onResult(picPath);
                }
            }
        });
        return cancelable;
    }

    /*@Nullable
    public static byte[] imgThumbFromBimapForWx(@NonNull Bitmap originalBitmap){
        return imgThumbFromBitmap(originalBitmap,ShareConstant.WX_IMAGE_THUMB_SIZE, ShareConstant.WX_IMAGE_THUMB_SIZE,ShareConstant.MAX_WX_THUMBDATA_SIZE);
    }*/

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,
                width,
                height,
                true);
    }

    public static byte[] compressBitmap(Bitmap image,int byteMaxLength){
        int qulity = 100;
        byte[] result = bmpToByteArray(image, qulity, false);
        // 不超过32k
        while (result.length >= byteMaxLength) {
            qulity = qulity-2;
            result = bmpToByteArray(image, qulity, false);
        }
        return result;
    }

    /*@Nullable
    public static byte[] imgThumbFromBitmap(@NonNull Bitmap originalBitmap, int desWidth, int desHeight,int byteMaxLength) {
        Bitmap thumbBmp = null;
        byte[] result = null;
        try {
            thumbBmp = Bitmap.createScaledBitmap(originalBitmap, desWidth, desHeight, true);// 缩放
            int qulity = 100;
            result = bmpToByteArray(thumbBmp, qulity, false);
            // 不超过32k
            while (result.length >= byteMaxLength) {
                qulity--;
                result = bmpToByteArray(thumbBmp, qulity, false);
            }
        } catch (Throwable ignore) {
            //
        } finally {
            try {
                if (thumbBmp != null && !thumbBmp.isRecycled()) thumbBmp.recycle();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }*/

    public static byte[] bmpToByteArray(final Bitmap bmp, final int qulity, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, qulity, output);// 尽量减少压缩大小
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public interface OnCallback {
        void onResult(String path);
    }//OnCallback end

    public static class Cancelable {
        public boolean isCancel = false;
    }//

    /*@Nullable
    public static byte[] imgThumbFromByte(Context context, byte[] original, int desWidth, int desHeight, int maxThumbSize) {
        Bitmap thumb = null;
        Bitmap thumbBmp = null;
        byte[] result = null;
        try {
            if (original == null || TextUtils.isEmpty(new String(original))) {
                return null;
            } else {
                thumb = BitmapFactory.decodeByteArray(original, 0, original.length);
            }
            thumbBmp = Bitmap.createScaledBitmap(thumb, desWidth, desHeight, true);// 缩放

            int qulity = 100;

            result = bmpToByteArray(thumbBmp, qulity, false); // 不超过32k

            while (result.length >= maxThumbSize) {
                qulity--;
                result = bmpToByteArray(thumbBmp, qulity, false);
            }
        } catch (Throwable e) {

        } finally {
            try {
                if (thumb != null && !thumb.isRecycled()) thumb.recycle();
                if (thumbBmp != null && !thumbBmp.isRecycled()) thumbBmp.recycle();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }*/
}
