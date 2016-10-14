package com.diagramsf.core;

/**
 * Action 是一个抽象概念，一个应用中可以有多个 Action。一个常见的 action 包含两个部分：type 和 payload。
 * type 用于指明当前的 action 是什么action（如 BUTTON_CLICK，或 DATA_LOADED） 等，
 * 通常是定义成常量的字符串；而 payload 则是这次 action 中包含的有效信息。
 *
 * Created by Diagrams on 2016/8/12 14:12
 */
class Action<T> implements Utils.Supplier<T> {
  private FluxLog log;
  private Type type;
  private T holder;

  /** {@link Action}的类型接口 */
  public interface Type {

  }

  Action(Type type) {
    this.type = type;
    this.log = new FluxLog(this);
    mark("===================");
    mark("Action created");
  }

  @Override public T get() {
    return holder;
  }

  public void supplier(T holder){
    this.holder = holder;
  }

  public Type type() {
    return type;
  }

  void mark(String text) {
    log.mark(text);
    log.mark("\n");
  }

  void finish() {
    mark("===================");
  }
}
