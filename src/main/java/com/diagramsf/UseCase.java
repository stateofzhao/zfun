package com.diagramsf;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import com.diagramsf.net.NetContract;
import com.diagramsf.netvolley.ResultFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

/**
 * domain layer (domain层)，所有的业务逻辑都是在这层处理的。考虑到Android工程，
 * 你会看到所有的 interactors (use cases) 也是在这里实现的
 * <p>
 * 这层是一个纯Java的模块，不包含任何Android依赖。
 *
 * Created by Diagrams on 2016/6/27 11:31
 */
public abstract class UseCase<T extends UseCase.RequestValue
    , R extends UseCase.ResponseValue
    , E extends UseCase.ErrorValue> {
  public static final int HIGH = 3;
  public static final int NORMAL = 2;
  public static final int LOW = 1;

  @Retention(RetentionPolicy.SOURCE) @IntDef({ HIGH, NORMAL, LOW }) @interface Type {
  }

  private T requestValue;
  private Listener<R> listener;
  private ErrorListener<E> errorListener;
  private int priority = 1;
  private boolean isCancel;
  private boolean justRun;//是否只是用来修饰Runnable

  private Object tag;//取消请求用的

  public void setPriority(@Type int priority) {
    this.priority = priority;
  }

  public int getPriority() {
    return priority;
  }

  public void setRequestValue(@Nullable T value) {
    this.requestValue = value;
  }

  public T getRequestValue() {
    return requestValue;
  }

  public void setListener(Listener<R> listener) {
    this.listener = listener;
  }

  public Listener<R> getListener() {
    return listener;
  }

  public void setErrorListener(ErrorListener<E> errorListener) {
    this.errorListener = errorListener;
  }

  public ErrorListener<E> getErrorListener() {
    return errorListener;
  }

  public void setTag(Object tag) {
    this.tag = tag;
  }

  public Object getTag() {
    return tag;
  }

  public void cancel() {
    isCancel = true;
  }

  public boolean isCancel() {
    return isCancel;
  }

  public void justRun() {
    justRun = true;
  }

  public boolean isJustRun() {
    return justRun;
  }

  public void run() {
    execute(requestValue);
  }

  public abstract void execute(T requestValue);

  /** 执行请求的参数 */
  public interface RequestValue {

  }

  /** 结果数据 */
  public interface ResponseValue {

  }

  /** 错误结果 */
  public interface ErrorValue {
    void setException(Exception e);
  }

  /** 结果回调接口 */
  public interface Listener<R extends ResponseValue> {
    void onSucceed(R response);
  }//class Callback end

  /** 错误结果回调 */
  public interface ErrorListener<E extends ErrorValue> {
    void onError(E error);
  }

  //===================================下面是一个执行远端请求的domain层实现
  public static class RemoteUseCaseTest extends UseCase<UrlRequestValue, NetResultValue, NetError> {

    private Context context;

    public RemoteUseCaseTest(Context context) {
      if (null == context) {
        throw new IllegalArgumentException("context must not be null!");
      }
      this.context = context.getApplicationContext();
    }

    @Override public void execute(final UrlRequestValue requestValue) {
      RemoteDataSource.with(context)
          .load(requestValue.url)
          .postData(requestValue.postData)
          .listener(new NetContract.Listener() {
            @Override public void onSucceed(NetContract.Result result) {
              NetResultValue resultValue = (NetResultValue) result;
              getListener().onSucceed(resultValue);
            }
          })
          .errorListener(new NetContract.ErrorListener() {
            @Override public void onFailed(NetContract.Fail fail) {
              NetError error = new NetError();
              error.setException(fail.getException());
              getErrorListener().onError(error);
            }
          })
          .into(requestValue.factory);
    }
  }

  public static class UrlRequestValue<T extends NetResultValue> implements RequestValue {
    public String url;
    public Map<String, String> postData;
    public ResultFactory<T> factory;
  }//class end

  public static class NetResultValue implements NetContract.Result, UseCase.ResponseValue {

    @Override public void setResultType(ResultType resultType) {

    }

    @Override public void setRequestTag(Object tag) {

    }

    @Override public ResultType getResultType() {
      return null;
    }

    @Override public Object getRequestTag() {
      return null;
    }

    @Override public boolean checkResultLegitimacy() {
      return false;
    }
  }//class end

  public static class NetError implements ErrorValue {
    public Exception e;

    @Override public void setException(Exception e) {
      this.e = e;
    }
  }//class end

  public static <T extends NetResultValue, E extends NetError, S extends NetResultValue> void testDomain(
      Context context, UrlRequestValue<T> requestValue, ErrorListener<E> errorListener,
      Listener<S> listener) {
    UseCaseHandler.instance()
        .request(requestValue)
        .error(errorListener)
        .listener(listener)
        .execute(new RemoteUseCaseTest(context));
  }
}//class UseCase end
