package com.diagramsf.executor;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 能够被{@link Executor}执行的任务，domain层的所有 interactor(use case)都需要实现此接口，
 * 以便让{@link Executor}来执行。
 *
 * //这个接口也可以命名为UseCase，看个人喜好了
 *
 * Created by Diagrams on 2016/8/5 18:09
 */
public interface Interactor {
  /** 最高优先级 */
  int HIGH = 3;
  /** 普通（默认）优先级 */
  int NORMAL = 2;
  /** 最低优先级 */
  int LOW = 1;

  @Retention(RetentionPolicy.SOURCE) @IntDef({ HIGH, NORMAL, LOW }) @interface Priority {
  }

  /** 添加到{@link Executor} 中了-->正在执行 */
  int NEW = 1;
  /** 被取消了 */
  int CANCEL = 2;
  /** 执行完毕 */
  int FINISHED = 3;
  /** 完全从{@link Executor}中移除掉了 */
  int DIE = 4;

  @Retention(RetentionPolicy.SOURCE) @IntDef({ NEW, CANCEL, FINISHED, DIE }) @interface State {
  }

  /** 执行任务 */
  void run();

  /** 此方法被调用，表示此任务被取消了，如果在外部自己手动调用此方法，只是会标记该任务为取消状态 */
  void cancel();

  /**
   * 此任务是否被取消了
   *
   * @return true表示任务被取消了，false表示没有被取消；注意，如果任务正常执行完，这个应该返回 false
   */
  boolean isCancel();

  /** 获取此任务的优先级 */
  @Priority int getPriority();

  /** 设置此任务的优先级 */
  void priority(@Priority int priority);

  /** 状态变化的回调 */
  void onStateChange(@State int state);
}
