package com.diagramsf;

/**
 * {@link UseCaseHandler}执行{@link UseCase}的调度器
 * <p>
 * Created by Diagrams on 2016/6/27 11:55
 */
public interface UseCaseScheduler {
    void execute(UseCase useCase, Object tag);

    void cancel(Object tag);

    <T extends UseCase.ResponseValue> void notifyResult(T response, UseCase.Listener<T> listener);

    <E extends UseCase.ErrorValue> void error(E error, UseCase.ErrorListener<E> errorListener);
}
