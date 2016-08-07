package com.diagramsf.executor;

/**
 * 这里就是Domain层连接Presentation Layer层(就是专注于处理UI、动画逻辑的地方，这里可以使用MVP
 * ，MVVM，MVC等，在这层一般就是处理View，不能够直接更改下层的数据，更改数据需要通过Domain层来实现)的接触点。
 * 也就是use case
 *
 * Created by Diagrams on 2016/8/5 18:09
 */
public interface Interactor {
  void run();
}
