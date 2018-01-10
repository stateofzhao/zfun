package com.diagramsf.core.executor;

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
public interface Task {
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

  /** 获取此任务的优先级 */
  @Priority int getPriority();

  /** 状态变化的回调 ，在state==FINISH时，证明run()方法执行完了 */
  void onStateChange(@State int state);
}
