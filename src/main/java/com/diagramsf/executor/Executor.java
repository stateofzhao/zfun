package com.diagramsf.executor;

/**
 * 执行所有实现了{@link Interactor}接口的任务(use case / interactor)。
 *
 * //也可以命名为UseCaseScheduler，看个人喜好。
 *
 * Created by Diagrams on 2016/8/5 18:29
 */
public interface Executor {
  /**
   * 执行 {@link Interactor}
   *
   * @param tag 取消执行用的标志
   */
  void execute(final Interactor interactor, Object tag);

  /**
   * 取消{@link Interactor} 的执行
   *
   * @param tag {@link #execute(Interactor, Object)}的第二个参数
   */
  void cancel(Object tag);
}
