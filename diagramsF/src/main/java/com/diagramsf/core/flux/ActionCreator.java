package com.diagramsf.core.flux;

/**
 * http://www.jianshu.com/p/896ce1a8e4ed
 *
 * =======================知乎这个回答觉着很好，对Flux理解又进一步
 * https://www.zhihu.com/question/33864532
 * 1. 视图组件变得很薄，只包含了渲染逻辑和触发 action 这两个职责，即所谓 "dumb components"。
 *
 * 2. 要理解一个 store 可能发生的状态变化，只需要看它所注册的 actions 回调就可以。
 *
 * 3. 任何状态的变化都必须通过 action 触发，而 action 又必须通过 dispatcher 走，
 * 所以整个应用的每一次状态变化都会从同一个地方流过。其实 Flux 和传统 MVC最不一样的就在这里了。
 * React 在宣传的时候一直强调的一点就是 “理解你的应用的状态变化是很困难的
 * (managing state changing over time is hard)”，Flux 的意义就在于强制让所有的状态变化都必须留下一笔记录，
 * 这样就可以利用这个来做各种 debug 工具、历史回滚等等。
 *
 * 市面上各种各样的 Flux 实现那么多，归根结底是因为 1. Flux 这个概念本来就定义松散，具体怎么实现大家各有各的看法；
 * 2. 官方实现又臭又长，不好用。
 *
 * ====================================
 * 一个app中不一定只有一个ActionCreator，可以有多个，其实这个ActionCreator并不是Flux必须有的，
 * 而是作为dispatcher的辅助函数，通常可以认为是Flux中的第四部分
 *
 * Created by Diagrams on 2016/8/12 17:00
 */
public class ActionCreator {
  private static volatile ActionCreator singleton = null;

  private Dispatcher dispatcher;

  private ActionCreator(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public static ActionCreator get(Dispatcher dispatcher) {
    if (null == singleton) {
      synchronized (ActionCreator.class) {
        if (null == singleton) {
          singleton = new ActionCreator(dispatcher);
        }
      }
    }
    return singleton;
  }

  public void sendAction(Action.Type actionType) {
    dispatcher.dispatchAction(new Action(actionType));
  }
}
