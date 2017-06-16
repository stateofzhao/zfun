package com.diagramsf.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 用来获取设备方面的信息
 * <p/>
 * Created by Diagrams on 2015/12/15 10:39
 */
public class DeviceUtil {
  private final static String TAG = "DeviceUtil";

  /**
   * 网络类型
   */
  public enum NetType {
    /** 未知类型 */
    UNKNOWN,
    /** wifi */
    WIFI,
    /** 2G */
    G2,
    /** 3G */
    G3,
    /** 4G */
    G4,
    /** 暂未处理的类型 */
    NO_DEAL,
    /** 断网了 */
    NO_NET
  }//class end

  /**
   * 运营商类型
   */
  public enum MNCType {
    /** 其他 */
    OTHER,
    /** 中国移动 */
    CMCC,
    /** 中国联通 */
    CUCC,
    /** 中国电信 */
    CTCC
  }//class end

  /**
   * 获取当前IPv4的整型ip地址
   *
   * @param canLocalHost 如果没有获取到任何网络是否返回本地主机地址（IPv4 127/8）
   * @return 获取失败会返回0
   */
  public static int getIPv4IntegerAdress(Context context, boolean canLocalHost) {
    if (isWifiEnable(context)) {
      return getWIFILocalIpAdress(context);
    } else {
      InetAddress inetAddress = getInetAddress(canLocalHost);
      if (inetAddress instanceof Inet4Address) {
        return inetAddressToInt(inetAddress);
      }
    }
    return 0;
  }

  /**
   * 获取当前IP地址的字符串表示，可以是IPv4也可以是IPv6
   *
   * @return 如果沒有获取到任何网络的话，会返回 本地主机地址（IPv4 127/8;IPv6 ::1）；如果
   * 都获取失败返回 null
   */
  public static String getIPStrAddress(Context context) {
    InetAddress inetAddress = getInetAddress(true);
    if (null != inetAddress) {
      return inetAddress.getHostAddress();
    }
    return null;
  }

  /**
   * 根据{@link Context}来获取网络类型
   *
   * 需要 android.permission.ACCESS_NETWORK_STATE 权限
   *
   * @return {@link NetType}
   */
  public static NetType getNetType(Context context) {
    if (isWifi(context)) {
      return NetType.WIFI;
    }

    if (!isNetworkAvailable(context)) {
      return NetType.NO_NET;
    }

    //下面类型的判断不包含WIFI，如果是wifi类型会返回 UNKNOWN
    TelephonyManager teleMan = (TelephonyManager) context.
        getSystemService(Context.TELEPHONY_SERVICE);
    int networkType = teleMan.getNetworkType();
    switch (networkType) {
      case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
      case TelephonyManager.NETWORK_TYPE_HSPAP:
      case TelephonyManager.NETWORK_TYPE_EHRPD:
        return NetType.G4;
      case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
      case TelephonyManager.NETWORK_TYPE_CDMA:
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
      case TelephonyManager.NETWORK_TYPE_EVDO_B:
      case 17://隐藏API
        return NetType.G3;
      case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
      case TelephonyManager.NETWORK_TYPE_EDGE:
      case 16:
        return NetType.G2;
      case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        return NetType.UNKNOWN;
      default:
        return NetType.NO_DEAL;
    }
  }

  /**
   * 当前是否连接网络
   *
   * 需要 android.permission.ACCESS_NETWORK_STATE 权限
   *
   * @return true联网，false没有联网
   */
  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivityManager =
        AndroidUtil.getService(context, Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
  }

  /**
   * 判断当前网络是否是wifi网络
   * if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE)  //判断3G网
   *
   * 需要 android.permission.ACCESS_NETWORK_STATE 权限
   *
   * @return boolean
   */
  public static boolean isWifi(Context context) {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
    if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
      return true;
    }
    return false;
  }

  /**
   * 判断当前网络是否是移动网络
   *
   * @return boolean
   */
  public static boolean isMobileNet(Context context) {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE;
  }

  /**
   * 获取设备品牌
   */
  public static String getDeviceBrand() {
    return Build.BRAND;
  }

  /** 获取设备制造商 */
  public static String getDeviceManufacturer() {
    return Build.MANUFACTURER;
  }

  /** 获取手机的型号 设备名称 */
  public static String getDeviceName() {
    return Build.MODEL;
  }

  /** 用户是否开启了wifi */
  public static boolean isWifiEnable(Context context) {
    WifiManager wifiManager = AndroidUtil.getService(context, Context.WIFI_SERVICE);
    return wifiManager.isWifiEnabled();
  }

  /**
   * 获取 WIFI的 IP地址，此处是获取的ipv4的整型ip，如果用户没有开启wifi返回0，或者此时是IPv6那么也会返回0
   *
   * 需要 android.permission.ACCESS_WIFI_STATE
   *
   * @return 0获取失败
   */
  public static int getWIFILocalIpAdress(Context context) {
    //获取wifi服务
    WifiManager wifiManager = AndroidUtil.getService(context, Context.WIFI_SERVICE);
    //==========下面的给注释掉了，直接开启用户wifi不好
    //判断wifi是否开启
    //        if (!wifiManager.isWifiEnabled()) {
    //            wifiManager.setWifiEnabled(true);
    //        }
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    if (null == wifiInfo) {
      return 0;
    }
    return wifiInfo.getIpAddress();
  }

  /**
   * 获取的网络地址{@link InetAddress}
   *
   * @param canLocalHost 如果没有获取到任何网络是否返回本地主机地址（IPv4 127/8;IPv6 ::1）
   * @return 获取失败返回null
   */
  public static InetAddress getInetAddress(boolean canLocalHost) {
    InetAddress candidateAddress = null;
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface intf : interfaces) {
        List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
        for (InetAddress inetAddr : addrs) {
          if (!inetAddr.isLoopbackAddress()) {
            if (inetAddr.isSiteLocalAddress()) {
              // Found non-loopback site-local address. Return it immediately...
              return inetAddr;
            } else if (candidateAddress == null) {
              // Found non-loopback address, but not necessarily site-local.
              // StoreImpl it as a candidate to be returned if site-local address is not subsequently found...
              candidateAddress = inetAddr;
              // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
              // only the first. For subsequent iterations, candidate will be non-null.
            }
          }
        }
      }

      if (null != candidateAddress) {
        return candidateAddress;
      }

      if (canLocalHost) {
        // At this point, we did not find a non-loopback address.
        // Fall back to returning whatever InetAddress.getLocalHost() returns...
        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
        if (jdkSuppliedAddress == null) {
          throw new UnknownHostException(
              "The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
        }
        return jdkSuppliedAddress;
      }
    } catch (Exception ex) {
      AppLog.e(TAG, "get ip address error:" + ex.toString());
    } // for now eat exceptions
    return null;
  }

  /**
   * Get IP address from first non-localhost interface
   * <p/>
   * 获取首个非本地主机(IPv4 127/8;IPv6 ::1) 的ip地址。
   * 摘自<a href="http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device#answers">stack
   * overflow</a>
   *
   * @param useIPv4 true=return ipv4, false=return ipv6
   * @return address or empty string
   */
  public static String getIPAddress(boolean useIPv4) {
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface intf : interfaces) {
        List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
        for (InetAddress addr : addrs) {
          if (!addr.isLoopbackAddress()) {
            String sAddr = addr.getHostAddress();
            //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
            boolean isIPv4 = sAddr.indexOf(':') < 0;
            if (useIPv4) {
              if (isIPv4) {
                return sAddr;
              }
            } else {
              if (!isIPv4) {
                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
              }
            }
          }
        }
      }
    } catch (Exception ex) {
      AppLog.e(TAG, "get ip address error:" + ex.toString());
    } // for now eat exceptions
    return "";
  }

  /**
   * Returns MAC address of the given interface name.
   * <p/>
   * 摘自<a href="http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device#answers">stack
   * overflow</a>
   *
   * @param interfaceName eth0(第一块有线网卡), wlan0(第一块无线网卡) or NULL=use first interface
   * @return mac address or empty string
   */
  public static String getMACAddress(String interfaceName) {
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface intf : interfaces) {
        if (interfaceName != null && !intf.getName().equalsIgnoreCase(interfaceName)) {
          continue;
        }
        byte[] mac = intf.getHardwareAddress();
        if (mac == null) {
          return "";
        }
        StringBuilder buf = new StringBuilder();
        for (byte aMac : mac) {
          buf.append(String.format("%02X:", aMac));
        }
        if (buf.length() > 0) {
          buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } // for now eat exceptions
    return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
  }

  /**
   * Convert a IPv4 address from an InetAddress to an integer
   * <p/>
   * 将 {@link Inet4Address} 转换成ip地址的整形数，摘自android.net.NetworkUtils 源码，
   * <a href="http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.0.1_r1/android/net/NetworkUtils.java#line-126">GrepCode</a>
   *
   * @param inetAddr is an InetAddress corresponding to the IPv4 address
   * @return the IP address as an integer in network byte order
   */
  public static int inetAddressToInt(InetAddress inetAddr) throws IllegalArgumentException {
    byte[] addr = inetAddr.getAddress();
    if (addr.length != 4) {
      throw new IllegalArgumentException("Not an IPv4 address");
    }
    return ((addr[3] & 0xff) << 24) | ((addr[2] & 0xff) << 16) |
        ((addr[1] & 0xff) << 8) | (addr[0] & 0xff);
  }

  /**
   * 格式化ip地址，将int型的ip地址转化成字符串型的.
   * <p/>
   * 重要！！注意： 这个方法并不是完全正确的，在有些情况下会造成错误参见：
   * <a href="http://stackoverflow.com/questions/1957637/java-convert-int-to-inetaddress#answer-1957670">stack
   * overflow</a>
   * <p/>
   * 摘自
   * <a href="http://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android#answer-18638588">stack
   * overflow</a>
   *
   * @param ipAdress 数值型ip地址
   */
  public static String formatIpAddress(int ipAdress) {
    // Convert little-endian to big-endianif needed
    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
      ipAdress = Integer.reverseBytes(ipAdress);
    }

    byte[] ipByteArray = BigInteger.valueOf(ipAdress).toByteArray();

    String ipAddressString;
    try {
      ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
    } catch (UnknownHostException ex) {
      AppLog.e("WIFIIP", "Unable to get host address.");
      ipAddressString = null;
    }
    return ipAddressString;
  }

  /**
   * 获取设备的UUID,该UUID由设备号(Device Id),SIM卡编号(Sim SerialNumber),以及Android Id编码组成
   *
   * @param context 上下文对象
   * @return 获取到的UUID
   */
  public static String getDeviceUUID(Context context) {
    final TelephonyManager tm =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    String deviceId, serialNumber, androidId;
    deviceId = tm.getDeviceId() + "";
    serialNumber = tm.getSimSerialNumber() + "";
    androidId = android.provider.Settings.Secure.getString(context.getContentResolver(),
        android.provider.Settings.Secure.ANDROID_ID) + "";
    UUID deviceUuid = new UUID(androidId.hashCode(),
        ((long) deviceId.hashCode() << 32) | serialNumber.hashCode());
    return deviceUuid.toString();
  }

  /**
   * 获取设备Id
   *
   * @param context 上下文对象
   * @return 设备Id
   */
  public static String getDeviceId(Context context) {
    final TelephonyManager tm =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String deviceId = tm.getDeviceId();
    if (deviceId == null) {
      // 如果获取不到设备号(平板电脑等没有电话服务的设备会出现该情况),则获取android id
      deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
          android.provider.Settings.Secure.ANDROID_ID) + "";
    }
    return deviceId;
  }

  /**
   * 返回 IMSI
   *
   * 需要 permission.READ_PHONE_STATE
   */
  public static String getIMSI(Context context) {
    TelephonyManager telemamanger = AndroidUtil.getService(context, Context.TELEPHONY_SERVICE);
    //telemamanger.getSimSerialNumber();
    // returns the ICCID (simcard serial number, unique and printed in the simcard).
    // If you need the IMSI (which is stored in the simcard and can be changed), you will want telemamanger.getSubscriberId();
    // Beware though, some manufacturer implementation only returns the non-personal digits of the IMSI (6 digits instead of 14-15)
    return telemamanger.getSubscriberId();
  }

  /**
   * 获取手机卡基站编号
   *
   * @return 如果获取失败，返回-1
   */
  public static int getCID(Context context) {
    TelephonyManager telemamanger = AndroidUtil.getService(context, Context.TELEPHONY_SERVICE);
    CellLocation location = telemamanger.getCellLocation();
    if (location instanceof GsmCellLocation) {
      GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
      return gsmCellLocation.getCid() & 0xffff;//基站编号
    }
    return -1;
  }

  /**
   * 获取手机基站的 位置区域码
   *
   * @return 如果获取失败，返回-1
   */
  public static int getLAC(Context context) {
    TelephonyManager telemamanger = AndroidUtil.getService(context, Context.TELEPHONY_SERVICE);
    CellLocation location = telemamanger.getCellLocation();
    if (null == location) {
      return -1;
    }
    if (location instanceof GsmCellLocation) {
      GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
      return gsmCellLocation.getLac() & 0xffff;//位置区域码;
    }
    return -1;
  }

  /**
   * 获取移动网络运营商
   *
   * @return 如果获取不到返回null
   */
  public static MNCType getMobileType(Context context) {
    TelephonyManager telemamanger = AndroidUtil.getService(context, Context.TELEPHONY_SERVICE);
    String operator = telemamanger.getNetworkOperator();
    //        int mcc = Integer.parseInt(operator.substring(0, 3));//移动国家代码（中国的为460）；
    try {//防止启动就崩溃，这里加上异常处理
      int mnc = Integer.parseInt(operator.substring(3));//，移动网络号码（中国移动为0,2，中国联通为1，中国电信为3）；
      switch (mnc) {
        case 0:
          return MNCType.CMCC;
        case 1:
          return MNCType.CUCC;
        case 2: //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
          return MNCType.CMCC;
        case 3:
        case 11://电信4G返回的是 46011
          return MNCType.CTCC;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return MNCType.OTHER;
  }
}
