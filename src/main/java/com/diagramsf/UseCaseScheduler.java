package com.diagramsf;

/**
 * {@link UseCaseHandler}执行{@link UseCase}的调度器
 * <p>
 * Created by Diagrams on 2016/6/27 11:55
 */
public interface UseCaseScheduler {
    /**
     * 运行任务
     *
     * @param task 要执行的任务
     * @param tag  任务标志，用来取消任务用的
     */
    void execute(Runnable task, Object tag);

    /**
     * 取消任务运行
     *
     * @param tag 对应于{@link UseCaseScheduler#execute(Runnable, Object)}第二个参数传递的tag
     */
    void cancel(Object tag);

    <T extends UseCase.ResponseValue> void notifyResult(T response, UseCase.Listener<T> listener);

    <E extends UseCase.ErrorValue> void error(E error, UseCase.ErrorListener<E> errorListener);
}
