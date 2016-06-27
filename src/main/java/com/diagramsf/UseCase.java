package com.diagramsf;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * domain layer 入口，所有对数据层进行操作的行为都需要通过这个来进入
 * <p>
 * Created by Diagrams on 2016/6/27 11:31
 */
public abstract class UseCase<T extends UseCase.RequestValue
        , R extends UseCase.ResponseValue
        , E extends UseCase.ErrorValue> {
    public static final int HIGH = 3;
    public static final int NORMAL = 2;
    public static final int LOW = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HIGH, NORMAL, LOW})
    @interface Type {}

    private T requestValue;
    private Listener<R> listener;
    private ErrorListener<E> errorListener;
    private int priority = 1;

    private Object tag;//取消请求用的

    public void setPriority(@Type int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setRequestValue(T value) {
        this.requestValue = value;
    }

    public T getRequestValue() {
        return requestValue;
    }

    public void setListener(Listener<R> listener) {
        this.listener = listener;
    }

    public Listener<R> getCallback() {
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

    public void run() {
        execute(requestValue);
    }

    abstract void execute(T requestValue);

    /** 执行请求的参数 */
    public interface RequestValue {

    }

    /** 结果数据 */
    public interface ResponseValue {

    }

    /** 错误结果 */
    public interface ErrorValue {

    }

    /** 结果回调接口 */
    public interface Listener<R extends ResponseValue> {
        void onSucceed(R response);
    }//class Callback end

    /** 错误结果回调 */
    public interface ErrorListener<E extends ErrorValue> {
        void onError(E error);
    }

}//class UseCase end
