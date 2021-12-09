package com.zfun.lib.util;

import androidx.core.util.Preconditions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 处理时间的工具类
 * <p>
 * 格式化时间的常用格式：
 * yyyy-MM-dd 1969-12-31<br/>
 * yyyy-MM-dd 1970-01-01<br/>
 * yyyy-MM-dd HH:mm 1969-12-31 16:00<br/>
 * yyyy-MM-dd HH:mm 1970-01-01 00:00<br/>
 * yyyy-MM-dd HH:mmZ 1969-12-31 16:00-0800<br/>
 * yyyy-MM-dd HH:mmZ 1970-01-01 00:00+0000<br/>
 * yyyy-MM-dd HH:mm:ss.SSSZ 1969-12-31 16:00:00.000-0800<br/>
 * yyyy-MM-dd HH:mm:ss.SSSZ 1970-01-01 00:00:00.000+0000<br/>
 * yyyy-MM-dd'T'HH:mm:ss.SSSZ 1969-12-31T16:00:00.000-0800<br/>
 * yyyy-MM-dd'T'HH:mm:ss.SSSZ 1970-01-01T00:00:00.000+0000<br/>
 * <p>
 * Created by zfun on 2016/1/15 14:56
 */
public class DateUtil {
  //ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本
  private final static ThreadLocal<SimpleDateFormat> dateFormat =
      new ThreadLocal<SimpleDateFormat>() {
        //如果对应线程中没有调用 set()方法，那么会使用这个默认值
        @Override protected SimpleDateFormat initialValue() {
          return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
      };

  private final static ThreadLocal<SimpleDateFormat> dateFormat2 =
      new ThreadLocal<SimpleDateFormat>() {
        @Override protected SimpleDateFormat initialValue() {
          return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
      };

  /**
   * 将字符串转位日期类型
   *
   * @param dateStr 字符串日期
   * @param pattern 日期格式化规则
   * @return {@link Date}
   */
  public static Date toDate(String dateStr, String pattern) {
    try {
      dateFormat.set(new SimpleDateFormat(pattern, Locale.getDefault()));
      return dateFormat.get().parse(dateStr);
    } catch (ParseException e) {
      return null;
    }
  }

  /**
   * 以友好的方式显示时间
   *
   * @param dateStr 字符串日期
   * @param pattern 日期格式化规则
   * @return 返回友好的日期
   */
  public static String friendly_time(String dateStr, String pattern) {
    Date time = toDate(dateStr, pattern);
    if (time == null) {
      return "Unknown";
    }
    String ftime = "";
    Calendar cal = Calendar.getInstance();

    // 判断是否是同一天
    String curDate = dateFormat2.get().format(cal.getTime());
    String paramDate = dateFormat2.get().format(time);
    if (curDate.equals(paramDate)) {
      int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
      if (hour == 0) {
        ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
      } else {
        ftime = hour + "小时前";
      }
      return ftime;
    }

    long lt = time.getTime() / 86400000;
    long ct = cal.getTimeInMillis() / 86400000;
    int days = (int) (ct - lt);
    if (days == 0) {
      int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
      if (hour == 0) {
        ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
      } else {
        ftime = hour + "小时前";
      }
    } else if (days == 1) {
      ftime = "昨天";
    } else if (days == 2) {
      ftime = "前天";
    } else if (days > 2 && days <= 10) {
      ftime = days + "天前";
    } else if (days > 10) {
      ftime = dateFormat2.get().format(time);
    }
    return ftime;
  }

  /**
   * 以友好的方式显示时间
   *
   * @param data 日期的毫秒数
   * @return 友好时间字符串
   */
  public static String friendly_time(long data) {
    Date time = new Date(data);
    String ftime = "";
    Calendar cal = Calendar.getInstance();

    // 判断是否是同一天
    String curDate = dateFormat2.get().format(cal.getTime());
    String paramDate = dateFormat2.get().format(time);
    if (curDate.equals(paramDate)) {
      int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
      if (hour == 0) {
        ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
      } else {
        ftime = hour + "小时前";
      }
      return ftime;
    }

    long lt = time.getTime() / 86400000;
    long ct = cal.getTimeInMillis() / 86400000;
    int days = (int) (ct - lt);
    if (days == 0) {
      int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
      if (hour == 0) {
        ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
      } else {
        ftime = hour + "小时前";
      }
    } else if (days == 1) {
      ftime = "昨天";
    } else if (days == 2) {
      ftime = "前天";
    } else if (days > 2 && days <= 10) {
      ftime = days + "天前";
    } else if (days > 10) {
      ftime = dateFormat2.get().format(time);
    }
    return ftime;
  }

  /**
   * 以友好的方式显示倒计时 时间
   *
   * @param data 毫秒
   * @return 友好时间字符串
   */
  public static String friendly_end_time(long data) {
    Date time = new Date(data);
    String ftime;
    Calendar cal = Calendar.getInstance();

    // 判断是否是同一天
    String curDate = dateFormat2.get().format(cal.getTime());
    String paramDate = dateFormat2.get().format(time);
    if (curDate.equals(paramDate)) {
      int hour = (int) ((time.getTime() - cal.getTimeInMillis()) / 3600000);
      if (hour == 0) {
        ftime = Math.max((time.getTime() - cal.getTimeInMillis()) / 60000, 1) + "分钟";
      } else {
        ftime = hour + "小时";
      }
      return ftime;
    }

    long lt = time.getTime() / 86400000;
    long ct = cal.getTimeInMillis() / 86400000;
    int days = (int) (lt - ct);
    if (days == 0) {
      int hour = (int) ((time.getTime() - cal.getTimeInMillis()) / 3600000);
      if (hour == 0) {
        ftime = Math.max((time.getTime() - cal.getTimeInMillis()) / 60000, 1) + "分钟";
      } else {
        ftime = hour + "小时";
      }
    } else {
      ftime = days + "天";
    }
    // if (days == 1) {
    // ftime = "1天";
    // } else if (days == 2) {
    // ftime = "2天";
    // } else if (days > 2 && days <= 10) {
    // ftime = days + "天前";
    // } else if (days > 10) {
    // ftime = dateFormat2.get().format(time);
    // }
    return ftime;
  }

  /**
   * 判断给定字符串时间是否为今日
   *
   * @param dateStr 日期字符串形式
   * @param pattern 日期格式化规则
   * @return boolean
   */
  public static boolean isToday(String dateStr, String pattern) {
    boolean b = false;
    Date time = toDate(dateStr, pattern);
    Date today = new Date();
    if (time != null) {
      String nowDate = dateFormat2.get().format(today);
      String timeDate = dateFormat2.get().format(time);
      if (nowDate.equals(timeDate)) {
        b = true;
      }
    }
    return b;
  }

  /** 判断给定时间是否为今日 */
  public static boolean isToday(Date date) {
    boolean b = false;
    Date today = new Date();
    if (date != null) {
      String nowDate = dateFormat2.get().format(today);
      String timeDate = dateFormat2.get().format(date);
      if (nowDate.equals(timeDate)) {
        b = true;
      }
    }
    return b;
  }

  /**
   * @return 返回long类型的今天的日期
   */
  public static long getToday() {
    Calendar cal = Calendar.getInstance();
    String curDate = dateFormat2.get().format(cal.getTime());
    curDate = curDate.replace("-", "");
    return Long.parseLong(curDate);
  }

  /**
   * 返回指定样式的时间
   *
   * @param timeMillis 毫秒值
   * @param pattern 时间样式,例如： yyyy-MM-dd H:m:s
   * @return 指定样式的时间
   */
  public static String getDateStringByMill(long timeMillis, String pattern) {
    // Calendar c = Calendar.getInstance();
    // c.set(1970, Calendar.JANUARY, 0, 0, 0, 0);
    // long time = c.getTimeInMillis() + timeMillis*1000l;
    // c.setTimeInMillis(time);
    Date data = new Date(timeMillis);
    return getDateString(data, pattern);
  }

  /**
   * 获得日期的格式化字符串
   *
   * @param date 日期对象
   * @param pattern 日期格式
   * @return 日期字符串
   */
  public static String getDateString(Date date, String pattern) {
    SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
    return df.format(date.getTime());
  }

  /**
   * 获取指定时间对应的天 的开始时间 和 结束时间
   *
   * @param timeMillis 指定的时间毫秒数
   * @return 返回 给定时间对应的天的 开始时间（毫秒数）和 结束时间（毫秒数）
   */
  public static long[] getAppointDayStartMillis(long timeMillis) {
    Calendar c1 = new GregorianCalendar();
    c1.setTime(new Date(timeMillis));
    c1.set(Calendar.HOUR_OF_DAY, 0);
    c1.set(Calendar.MINUTE, 0);
    c1.set(Calendar.SECOND, 0);
    c1.set(Calendar.MINUTE, 0);
    c1.set(Calendar.MILLISECOND, 0);

    Calendar c2 = new GregorianCalendar();
    c2.setTime(new Date(timeMillis));
    c2.set(Calendar.HOUR_OF_DAY, 23);
    c2.set(Calendar.MINUTE, 59);
    c2.set(Calendar.SECOND, 59);

    Date start = c1.getTime();
    Date end = c2.getTime();
    return new long[] { start.getTime(), end.getTime() };
  }

  /**
   * 计算 指定时间对应的 周 开始时间和结束时间
   *
   * @param timeMillis 时间毫秒数
   */
  public static long[] getAppointWeekStartMillis(long timeMillis) {
    Calendar c1 = new GregorianCalendar();
    c1.setTime(new Date(timeMillis));
    c1.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    c1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

    Calendar c2 = new GregorianCalendar();
    c2.setTime(c1.getTime());
    c2.add(Calendar.DAY_OF_WEEK, 7);

    Date start = c1.getTime();
    Date end = c2.getTime();
    return new long[] { start.getTime(), end.getTime() };
  }

  /**
   * 计算 指定时间对应的 月 开始时间和结束时间
   *
   * @param timeMillis 时间毫秒数
   */
  public static long[] getAppointMonthStartMillis(long timeMillis) {
    Calendar c1 = new GregorianCalendar();
    c1.setTime(new Date(timeMillis));
    c1.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    c1.set(Calendar.DAY_OF_MONTH, c1.getActualMinimum(Calendar.DAY_OF_MONTH));

    Calendar c2 = new GregorianCalendar();
    c2.setTime(new Date(timeMillis));
    c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    c2.set(Calendar.DAY_OF_MONTH, c2.getActualMaximum(Calendar.DAY_OF_MONTH));
    c2.set(Calendar.HOUR_OF_DAY, 24);

    Date start = c1.getTime();
    Date end = c2.getTime();
    return new long[] { start.getTime(), end.getTime() };
  }

  /**
   * 获取给定日期是周几
   *
   * @return 1表示周一，2表示周二...7表示周日
   */
  public static int getWeekOfDate(Date date) {
    Preconditions.checkNotNull(date);
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.setFirstDayOfWeek(Calendar.MONDAY);
    return cal.get(Calendar.DAY_OF_WEEK);
  }
}
