package com.diagramsf.lib.executor;

/**
 * 执行所有实现了{@link Task}接口的任务。
 * <p>
 * Created by Diagrams on 2016/8/5 18:29
 */
interface Executor {
  /**
   * 执行 {@link Task}
   *
   * @param tag 取消执行用的标志
   */
  void execute(final Task task, Object tag);

  /**
   * 取消{@link Task} 的执行
   *
   * @param tag {@link #execute(Task, Object)}的第二个参数
   */
  void cancel(Object tag);
}
