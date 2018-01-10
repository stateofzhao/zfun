package com.diagramsf.lib.util;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * 常用java方面的方法，不会关联Android提供类，关联Android提供的类的帮助类是{@link AndroidUtil}
 * <p/>
 * Created by Diagrams on 2015/10/10 14:51
 */
public class JavaUtil {
  /**
   * 合并两个数组；
   * 来自google的guava类库中的com.google.common.collect.ObjectArrays 类；
   */
  public static <T> T[] concat(T[] first, T[] second, Class<T> type) {
    T[] result = newArray(type, first.length + second.length);
    System.arraycopy(first, 0, result, 0, first.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /**
   * 把一个数组 插入到 一个数组指定的位置
   *
   * @param position 如果是2 那么结果数组中 第二项就是 要插入数组的起始位置
   */
  public static <T> T[] insertToArray(T[] desInserted, T[] data, int position, Class<T> type) {
    if (position >= desInserted.length) {
      throw new IllegalArgumentException("position must < forInserted.length !!!!");
    }
    T[] result = newArray(type, desInserted.length + data.length);
    System.arraycopy(desInserted, 0, result, 0, position);//复制时不包含position对应的元素
    System.arraycopy(data, 0, result, position, data.length);//把 要插入的数据 复制到 结果数组中
    System.arraycopy(desInserted, position, result, position + data.length,
        desInserted.length - position);//把目标数组 剩下的数据 复制到 结果数组中
    return result;
  }

  /**
   * 来自google的guava类库中的com.google.common.collect.ObjectArrays 类
   * <p/>
   * Returns a new array of the given length with the specified component type.
   *
   * @param type the component type
   * @param length the length of the new array
   */
  @SuppressWarnings("unchecked") public static <T> T[] newArray(Class<T> type, int length) {
    return (T[]) Array.newInstance(type, length);
  }

  /**
   * 把{@link Collection}转换成数组
   */
  public static <T> T[] toArray(Collection<T> c, Class<T> type) {
    return c.toArray(newArray(type, c.size()));
  }

  /**
   * 计算缩放尺寸的 高度，
   *
   * @param orangeWith 原始 宽度
   * @param orangeHeight 原始 高度
   * @param desWith 目标宽度
   * @return 目标高度
   */
  public static int calculationWithAndHeight(int orangeWith, int orangeHeight, int desWith) {
    return (orangeHeight * desWith) / orangeWith;
  }

  /**
   * 简单的比较两个字符串是否相似
   *
   * @return true相似，false不相似
   */
  public static boolean strSample(String str1, String str2) {
    return str1.equals(str2) || str1.contains(str2) || str2.contains(str1);
  }
}
