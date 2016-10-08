package com.diagramsf.util;

import android.graphics.Matrix;
import android.graphics.Rect;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.DraweeView;

/**
 * Fresco图片库常用工具
 */
public class FrescoUtil {

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
        if(-1 != basisOfWidth){
          childWidth = basisOfWidth;
        }
        if(-1 != basisOfHeight){
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
}
