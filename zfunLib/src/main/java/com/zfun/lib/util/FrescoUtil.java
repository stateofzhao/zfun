package com.zfun.lib.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.greenrobot.eventbus.EventBus;

/**
 * Fresco图片库常用工具
 */
public class FrescoUtil {
  private static final String IMAGE_PATH = "zfun";

  /**
   * 把原始图片缩放到指定宽高后再根据 焦点进行裁剪，焦点需要调用{@link DraweeView#getHierarchy()#setActualImageScaleType()}来设置
   *
   * @param basisOfWidth 会把原始图片缩放到此宽度后再进行裁剪，传递-1 表示根据原始图片尺寸进行裁剪
   * @param basisOfHeight 会把原始图片缩放到此高度后在进行裁剪，传递-1 表示根据原始图片尺寸进行裁剪
   */
  public static ScalingUtils.ScaleType superFocusCrop(final int basisOfWidth,
      final int basisOfHeight) {
    return new ScalingUtils.AbstractScaleType() {
      @Override public void getTransformImpl(Matrix outTransform, Rect parentRect, int childWidth,
          int childHeight, float focusX, float focusY, float scaleX, float scaleY) {
        float dx, dy;
        if (-1 != basisOfWidth && -1 != basisOfHeight) {
          scaleX = (basisOfWidth * 1f) / childWidth;
          scaleY = (basisOfHeight * 1f) / childHeight;
          outTransform.setScale(scaleX, scaleY);

          childWidth = basisOfWidth;
          childHeight = basisOfHeight;
        }

        //计算平移
        dx = parentRect.width() * 0.5f - childWidth * focusX;
        dx = parentRect.left + Math.max(Math.min(dx, 0), parentRect.width() - childWidth);
        dy = parentRect.height() * 0.5f - childHeight * focusY;
        dy = parentRect.top + Math.max(Math.min(dy, 0), parentRect.height() - childHeight);
        outTransform.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
      }
    };
  }

  /** 获取水平翻转的后处理器 */
  public static Postprocessor mirrorPostProcessor() {
    return new BasePostprocessor() {
      @Override public String getName() {
        return "mirrorPostprocessor";
      }

      @Override public void process(Bitmap destBitmap, Bitmap sourceBitmap) {
        //水平翻转图片
        for (int x = 0; x < destBitmap.getWidth(); x++) {
          for (int y = 0; y < destBitmap.getHeight(); y++) {
            destBitmap.setPixel(destBitmap.getWidth() - x, y, sourceBitmap.getPixel(x, y));
          }
        }
      }
    };
  }

  /** 保存图片到系统图库 */
  public static void downloadImageToPic(Context context, @NonNull final String url) {
    //分两步进行，首先检测本地磁盘中是否已经有这张图片了，如果有直接复制到系统图库；
    // 本地磁盘没有，此时直接调用Fresco来加载图片（此时图片可能来自网络，也可能来自内存缓存）
    final Object callerContext = new Object();
    final String imageName = url.substring(url.lastIndexOf("/") + 1, url.length());
    File pubPicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    final File systemPic = new File(pubPicDir.getAbsolutePath() + "/" + IMAGE_PATH + "/");

    final String successMessage = "保存成功";
    final String failMessage = "出现异常保存失败";
    final String sizeFailMessage = "存储空间不足出现异常";

    Intent notifyIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
        Uri.fromFile(new File(systemPic.getPath() + "/" + imageName)));
    final PendingIntent pendingIntent =
        PendingIntent.getBroadcast(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    new AsyncTask<Void, Void, Void>() {
      @Override protected Void doInBackground(Void... params) {
        if (systemPic.getUsableSpace() < 1024 * 1024 * 20) {//小于20M，提示控件不足
          EventBus.getDefault().post(new DownloadImageEvent(false, sizeFailMessage));
          return null;
        }
        if (!systemPic.exists() && !systemPic.mkdirs()) { //创建图片目录失败
          EventBus.getDefault().post(new DownloadImageEvent(false, failMessage));
          return null;
        }

        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
            .getEncodedCacheKey(ImageRequest.fromUri(Uri.parse(url)), callerContext);
        final File localFile = getCacheImageOnDisk(cacheKey);
        if (null != localFile) {//本地有图片缓存
          if (systemPic.mkdirs() || systemPic.exists()) {
            boolean result =
                FileUtil.copyFile(localFile.getPath(), systemPic.getPath() + "/" + imageName);
            if (result) {
              try {
                pendingIntent.send();
              } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
              }
              EventBus.getDefault().post(new DownloadImageEvent(true, successMessage));
            } else {
              EventBus.getDefault().post(new DownloadImageEvent(false, failMessage));
            }
          }
        } else {//本地没有图片缓存
          ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
              .setProgressiveRenderingEnabled(true)
              .build();
          ImagePipeline imagePipeline = Fresco.getImagePipeline();
          DataSource<CloseableReference<CloseableImage>> dataSource =
              imagePipeline.fetchDecodedImage(imageRequest, callerContext);
          dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override protected void onNewResultImpl(Bitmap bitmap) {
              File file = new File(systemPic, imageName);
              try {
                FileOutputStream fos = new FileOutputStream(file);
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                try {
                  pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                  e.printStackTrace();
                }
                EventBus.getDefault().post(new DownloadImageEvent(true, successMessage));
              } catch (IOException e) {
                e.printStackTrace();
              }
            }

            @Override protected void onFailureImpl(
                DataSource<CloseableReference<CloseableImage>> dataSource) {
              //TODO 下载图片失败
              EventBus.getDefault().post(new DownloadImageEvent(false, failMessage));
            }
          }, CallerThreadExecutor.getInstance());
        }
        return null;
      }
    }.execute();
  }

  /**
   * @return null本地磁盘没有文件
   */
  private static File getCacheImageOnDisk(CacheKey cacheKey) {
    File localFile = null;
    if (cacheKey != null) {
      if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
        BinaryResource resource =
            ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
        localFile = ((FileBinaryResource) resource).getFile();
      } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
        BinaryResource resource =
            ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
        localFile = ((FileBinaryResource) resource).getFile();
      }
    }
    return localFile;
  }

  /** 下载图片通知事件 */
  public static class DownloadImageEvent {
    public static void register(Object subscriber) {
      EventBus.getDefault().register(subscriber);
    }

    public static void unRegister(Object subscriber) {
      EventBus.getDefault().unregister(subscriber);
    }

    public final boolean success;//是否下载成功
    public final String msg;//消息

    private DownloadImageEvent(boolean success, String msg) {
      this.success = success;
      this.msg = msg;
    }
  }
}
