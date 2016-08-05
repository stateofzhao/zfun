package com.diagramsf.net;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;
import org.apache.http.HttpException;

/**
 * 用于处理网络连接异常，便于排查原因（Exception的装饰类）
 */
public class ExceptionWrapper extends Exception {
  /** 定义异常类型 */
  public final static byte TYPE_NETWORK = 0x01;//网络连接失败，请检查网络设置
  public final static byte TYPE_SOCKET = 0x02;//网络异常，读取数据超时
  public final static byte TYPE_HTTP_CODE = 0x03;//服务器错误，响应码：%d
  public final static byte TYPE_HTTP_ERROR = 0x04;//网络异常，请求超时
  public final static byte TYPE_JSON = 0x05;//数据解析异常
  public final static byte TYPE_IO = 0x06;//文件流异常
  public final static byte TYPE_RUN = 0x07;//应用程序运行时异常
  public final static byte TYPE_AUTH = 0X08;//服务器验证身份失败

  private byte type; // 异常类型
  private int serverResponseErrorCode; // HTTP 服务器返回结果异常，此值代表服务器返回的异常值

  private Exception exception;

  private ExceptionWrapper(byte type, int code, @NonNull Exception exception) {
    super(exception);
    this.type = type;
    this.serverResponseErrorCode = code;
    this.exception = exception;
  }

  @Override public Throwable getCause() {
    return exception;
  }

  @Override public String getLocalizedMessage() {
    return convertExceptionToText();
  }

  @Override public String getMessage() {
    return convertExceptionToText();
  }

  @Override public Throwable fillInStackTrace() {
    return exception.fillInStackTrace();
  }

  @Override public StackTraceElement[] getStackTrace() {
    return exception.getStackTrace();
  }

  @Override public Throwable initCause(Throwable throwable) {
    return exception.initCause(throwable);
  }

  @Override public void printStackTrace() {
    exception.printStackTrace();
  }

  @Override public void printStackTrace(PrintStream err) {
    exception.printStackTrace(err);
  }

  @Override public void printStackTrace(PrintWriter err) {
    exception.printStackTrace(err);
  }

  @Override public void setStackTrace(StackTraceElement[] trace) {
    exception.setStackTrace(trace);
  }

  @Override public String toString() {
    return exception.toString();
  }

  public int getCode() {
    return serverResponseErrorCode;
  }

  public int getType() {
    return this.type;
  }

  /**
   * 服务器应答错误
   *
   * @param code 服务器异常码
   */
  public static ExceptionWrapper server(int code) {
    return new ExceptionWrapper(TYPE_HTTP_CODE, code, null);
  }

  /** 表示网络连接超时 */
  public static ExceptionWrapper http(Exception e) {
    return new ExceptionWrapper(TYPE_HTTP_ERROR, 0, e);
  }

  /** 读取数据超时 */
  public static ExceptionWrapper socket(Exception e) {
    return new ExceptionWrapper(TYPE_SOCKET, 0, e);
  }

  /** 文件流异常 */
  public static ExceptionWrapper io(Exception e) {
    if (e instanceof UnknownHostException || e instanceof ConnectException) {
      return new ExceptionWrapper(TYPE_NETWORK, 0, e);
    } else if (e instanceof IOException) {
      return new ExceptionWrapper(TYPE_IO, 0, e);
    }
    return run(e);
  }

  /** 解析异常 */
  public static ExceptionWrapper json(Exception e) {
    return new ExceptionWrapper(TYPE_JSON, 0, e);
  }

  /** 无网络连接 */
  public static ExceptionWrapper noNet(Exception e) {
    return new ExceptionWrapper(TYPE_NETWORK, 0, e);
  }

  /** 当无法解析主机，或者没有联网时，生成 “检测是否联网”异常 */
  public static ExceptionWrapper network(Exception e) {
    if (e instanceof UnknownHostException || e instanceof ConnectException) {
      return new ExceptionWrapper(TYPE_NETWORK, 0, e);
    } else if (e instanceof HttpException) {
      return http(e);
    } else if (e instanceof SocketException) {
      return socket(e);
    }
    return http(e);
  }

  /** 应用程序运行时异常 */
  public static ExceptionWrapper run(Exception e) {
    return new ExceptionWrapper(TYPE_RUN, 0, e);
  }

  /** 身份验证错误 */
  public static ExceptionWrapper auth(Exception e) {
    return new ExceptionWrapper(TYPE_AUTH, 0, e);
  }

  private String convertExceptionToText() {
    String text;
    switch (this.getType()) {
      case TYPE_HTTP_CODE:
        text = String.format(Locale.getDefault(), "服务器错误，响应码：%d", getCode());
        break;
      case TYPE_HTTP_ERROR:
        text = "网络异常，请求超时";
        break;
      case TYPE_SOCKET:
        text = "网络异常，读取数据超时";
        break;
      case TYPE_NETWORK:
        text = "网络连接失败，请检查网络设置";
        break;
      case TYPE_JSON:
        text = "数据解析异常";
        break;
      case TYPE_IO:
        text = "文件流异常";
        break;
      case TYPE_RUN:
        text = "应用程序运行时异常";
        break;
      case TYPE_AUTH:
        text = "服务器验证身份失败";
        break;
      default:
        text = "";
        break;
    }
    return text;
  }
}
