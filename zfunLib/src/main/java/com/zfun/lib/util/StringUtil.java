package com.zfun.lib.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 字符串操作工具包
 */
@SuppressLint("SimpleDateFormat") public class StringUtil {
  private static final String SCHEME_HTTP = "http";
  private static final String SCHEME_HTTPS = "https";
  private static final String PROTOCOL_CHARSET = "utf-8";

  private final static Pattern email =
      Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

  /**
   * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
   *
   * @return true 是空字符串；false others
   */
  public static boolean isEmpty(String input) {
    if (input == null || "".equals(input) || "null".equalsIgnoreCase(input)) {
      return true;
    }
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
        return false;
      }
    }
    return true;
  }

  /**
   * 判断是不是一个合法的电子邮件地址
   *
   * @param email 要判断的字符串
   * @return true是；false不是
   */
  public static boolean isEmail(String email) {
    return !(email == null || email.trim().length() == 0) && StringUtil.email.matcher(email)
        .matches();
  }

  /**
   * 字符串转整数
   */
  public static int toInt(String str, int defValue) {
    try {
      return Integer.parseInt(str);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return defValue;
  }

  /**
   * 对象转整数
   *
   * @return 转换异常返回 0
   */
  public static int toInt(Object obj) {
    if (obj == null) {
      return 0;
    }
    return toInt(obj.toString(), 0);
  }

  /**
   * 对象转整数
   *
   * @return 转换异常返回 0
   */
  public static long toLong(String obj) {
    try {
      return Long.parseLong(obj);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * 字符串转布尔值
   *
   * @return 转换异常返回 false
   */
  public static boolean toBool(String b) {
    try {
      return Boolean.parseBoolean(b);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 将一个InputStream流转换成字符串
   *
   * @return 出现异常时，返回空
   */
  public static String toConvertString(@NonNull InputStream is) {
    StringBuilder res = new StringBuilder();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader read = new BufferedReader(isr);
    try {
      String line;
      line = read.readLine();
      while (line != null) {
        res.append(line);
        line = read.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        isr.close();
        read.close();
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return res.toString();
  }

  /**
   * 字符串编码转换的实现方法
   *
   * @param str 待转换编码的字符串
   * @param oldCharset 原编码
   * @param newCharset 目标编码
   * @throws UnsupportedEncodingException
   */
  public static String changeCharset(String str, String oldCharset, String newCharset)
      throws UnsupportedEncodingException {
    if (str != null) {
      // 用旧的字符编码解码字符串。解码可能会出现异常。
      byte[] bs = str.getBytes(oldCharset);
      // 用新的字符编码生成字符串
      return new String(bs, newCharset);
    }
    return null;
  }

  /**
   * 字符串编码转换的实现方法
   *
   * @param str 待转换编码的字符串
   * @param newCharset 目标编码
   * @throws UnsupportedEncodingException
   */
  public static String changeCharset(String str, String newCharset)
      throws UnsupportedEncodingException {
    if (str != null) {
      byte[] bs = str.getBytes();
      // 用新的字符编码生成字符串
      return new String(bs, newCharset);
    }
    return null;
  }

  /** unicode 转换成汉字 */
  public static String convert(String utfString) {
    StringBuilder sb = new StringBuilder();
    int i;
    int pos = 0;

    while ((i = utfString.indexOf("\\u", pos)) != -1) {
      sb.append(utfString.substring(pos, i));
      if (i + 5 < utfString.length()) {
        pos = i + 6;
        sb.append((char) Integer.parseInt(utfString.substring(i + 2, i + 6), 16));
      }
    }
    return sb.toString();
  }

  /**
   * 判断 url 是否指向一长图片
   *
   * @param url url地址
   */
  public static boolean urlIsPointImage(String url) {
    int lastIndexOf = url.lastIndexOf('.');
    if (-1 == lastIndexOf) {
      return false;
    }
    String lastName = url.substring(lastIndexOf, url.length());// url后缀
    return lastName.equalsIgnoreCase(".jpg")
        || lastName.equalsIgnoreCase(".jpeg")
        || lastName.equalsIgnoreCase(".jepg")
        || lastName.equalsIgnoreCase(".png")
        || lastName.equalsIgnoreCase(".webp");
  }

  /**
   * A hashing method that changes a string (like a URL) into a hash suitable
   * for using as a disk filename.
   */
  public static String convertStrToMD5(String key) {
    String cacheKey;
    try {
      final MessageDigest mDigest = MessageDigest.getInstance("MD5");
      mDigest.update(key.getBytes());
      cacheKey = bytesToHexString(mDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      cacheKey = String.valueOf(key.hashCode());
    }
    return cacheKey;
  }

  private static String bytesToHexString(byte[] bytes) {
    // http://stackoverflow.com/questions/332079
    StringBuilder sb = new StringBuilder();
    for (byte aByte : bytes) {
      String hex = Integer.toHexString(0xFF & aByte);
      if (hex.length() == 1) {
        sb.append('0');
      }
      sb.append(hex);
    }
    return sb.toString();
  }

  /** 截取字符串 ,结尾加上 三个点（...） */
  public static String subString(String str, int subLength) {
    if (str.trim().length() < subLength) {
      return str;
    }
    String tmp = str.substring(0, subLength);
    return tmp + "...";
  }

  /** 转移字符串中的 HTML 特殊字符 */
  public static String converHtmlStr(String html) {
    html = html.replace("&nbsp;&nbsp;", "\t");// 替换跳格
    html = html.replace("&nbsp;", " ");// 替换空格
    html = html.replace("&lt;", "<");
    html = html.replaceAll("&gt;", ">");
    return html;
  }

  /** 高亮显示字符串中的 关键词 */
  @SuppressLint("DefaultLocale") public static SpannableString getHighlightShow(String[] keywords,
      String sourceStr, int color) {
    if (keywords == null || keywords.length == 0) {
      return new SpannableString(sourceStr);
    }
    String copySourceStrLower = sourceStr.toLowerCase();
    List<StringChangeColorHolder> groupChangeList =
        new ArrayList<StringChangeColorHolder>(keywords.length);
    for (String keyword : keywords) {
      String oneKeyLower = keyword.toLowerCase();
      Integer[] position = checkPointStrPosition(copySourceStrLower, oneKeyLower);
      if (null == position) {
        continue;
      }
      StringChangeColorHolder oneHolder =
          new StringChangeColorHolder(position, oneKeyLower.length());
      groupChangeList.add(oneHolder);
    }
    return changeStrPositionColor(sourceStr, groupChangeList, color);
  }

  /**
   * 在字符串中匹配指定的 字符串，并返回匹配到的位置
   *
   * @param sourceStr 源字符串
   * @param pointStr 要匹配的指定字符串
   */
  private static Integer[] checkPointStrPosition(String sourceStr, String pointStr) {
    List<Integer> result_list = new ArrayList<>();
    String copyStr = sourceStr;

    int position = copyStr.indexOf(pointStr);
    int hasSubStrLength = 0;// 已经截取掉的字符串的长度
    while (-1 != position) {
      result_list.add(hasSubStrLength + position);
      copyStr = copyStr.substring(position + pointStr.length());
      hasSubStrLength = hasSubStrLength + position + pointStr.length();
      position = copyStr.indexOf(pointStr);
    }
    return result_list.toArray(new Integer[result_list.size()]);
  }

  /**
   * 改变字符串 中部分 字符的 颜色
   *
   * @param sourceStr 要修改的字符串
   * @param groupChangeList 改变颜色的开始位置，以及要改变的字符串长度
   */
  private static SpannableString changeStrPositionColor(String sourceStr,
      List<StringChangeColorHolder> groupChangeList, int color) {
    SpannableString sp = new SpannableString(sourceStr);
    for (StringChangeColorHolder one : groupChangeList) {
      Integer[] startPositions = one.startPosition;
      int length = one.positionStrLength;
      for (Integer startPosition : startPositions) {
        sp.setSpan(new ForegroundColorSpan(color), startPosition, startPosition + length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
      }
    }
    return sp;
  }

  /** 给字符串标记高亮用到的数据类 */
  private static class StringChangeColorHolder {
    final Integer[] startPosition;// 字符串中匹配上的字符开始位置
    final int positionStrLength;

    StringChangeColorHolder(Integer[] startPosition, int positionStrLength) {
      this.startPosition = startPosition;
      this.positionStrLength = positionStrLength;
    }
  }// class end

  /** 判断url是否符合 图片加载工具的协议 */
  public static boolean canLoadImageFromURL(String url) {
    Uri uri = Uri.parse(url);
    String scheme = uri.getScheme();
    return (SCHEME_HTTP.equals(scheme) || SCHEME_HTTPS.equals(scheme));
  }

  /** 判断给定的字符串是否是URL */
  public static boolean isURL(String str) {
    Uri uri = Uri.parse(str);
    String scheme = uri.getScheme();
    return (SCHEME_HTTP.equals(scheme) || SCHEME_HTTPS.equals(scheme));
  }

  /** 四舍五入小数点保留一位 */
  public static float saveOnePoint(float orange) {
    return (float) (Math.round(orange * 10)) / 10;
  }

  /**
   * 会四舍五入
   *
   * @param d 要四舍五入的数
   * @param wei 要保留的位数
   */
  public static String baoliuwei(double d, int wei) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < wei; i++) {
      sb.append("0");
    }
    if (d == 0) {
      return "0." + sb.toString();
    }

    DecimalFormat df = new DecimalFormat("#." + sb.toString());
    String result_ = df.format(d);
    String[] result_str = result_.split("[.]");
    if (result_str.length == 1 || result_str[0].equals("")) {
      result_ = "0" + result_;
    }
    return result_;
  }

  /**
   * 解析URL中的参数，来生成参数的键值对
   *
   * @return url中的参数值
   */
  public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    String query = url.getQuery();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf('=');
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
          URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    return query_pairs;
  }

  /**
   * 获取Manifest中 meta-data 中的值
   *
   * @param metaKey meta-data的 name
   */
  public static int getMetaDataFromXML(Context context, String metaKey) {
    ApplicationInfo appInfo = null;
    try {
      appInfo = context.getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    if (null == appInfo || null == appInfo.metaData) {
      return -1;
    }
    return appInfo.metaData.getInt(metaKey);
  }

  /** 将 Map 转换成字符串 ，会使用{@link URLEncoder}来编码Map中的key和value */
  public static String changeMapParamToStr(Map<String, String> params, String paramsEncoding) {
    StringBuilder encodedParams = new StringBuilder();
    try {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
        encodedParams.append('=');
        encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
        encodedParams.append('&');
      }
      return encodedParams.toString();
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
    }
  }

  /** 将PostData 转换成Map */
  public static Map<String, String> changePostDataToMap(String postData) {
    if (StringUtil.isEmpty(postData)) {
      return null;
    }
    Map<String, String> result = new HashMap<>();
    String[] params = postData.split("\\&");
    for (String str : params) {
      // 这里要明白一点： ss="" ,这个形式返回的oneParam的length也是1
      String[] oneParam = str.split("\\=");
      result.put(oneParam[0], oneParam[1]);
    }
    return result;
  }

  /**
   * 去掉PostData中的版本控制号
   *
   * @param orangePostData 原始postData
   * @param versionName 中的版本对应的名称
   * @return 去除版本号后的postData
   */
  public static String deletVersionParam(String orangePostData, String versionName) {
    Map<String, String> postMap = changePostDataToMap(orangePostData);
    if (null == postMap) {
      return null;
    }
    postMap.remove(versionName);
    return changeMapParamToStr(postMap, PROTOCOL_CHARSET);
  }

  /**
   * 将有符号的十进制整数转换成有符号的二进制（正常二进制，非计算机中存储的补码）
   *
   * @param i 有符号整数字符串
   */
  public static String intToBinaryStr(String i) {
    BigInteger src = new BigInteger(i, 10);
    return src.toString(2);
  }

  /** 将有符号的二进制（正常二进制，非计算机中存储的补码）字符串转换成有符号的十进制整数 */
  public static String binaryToIntStr(String binary) {
    //第一个参数是数源，第二个参数是数源进制
    BigInteger src = new BigInteger(binary, 2);
    return src.toString(10);
  }

  /**
   * 将有符号十进制整数转换成二进制字符串（这个转换的二进制不是数学上的二进制，是计算机中存储的二进制（补码））
   *
   * @param i 有符号整数字符串
   */
  public static String intToBinaryStrComplement(long i) {
    return Long.toBinaryString(i);//这个就是取得i在计算机中的二进制表示，所以返回的就是补码
  }

  /** 将二进制（计算机中存储的补码）字符串转换成有符号的十进制整数 */
  public static String binaryToIntStrComplement(String binary) {
    BigInteger src = new BigInteger(binary, 2);
    return src.intValue() + "";
  }
}
