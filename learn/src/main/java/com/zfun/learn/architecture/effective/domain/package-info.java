/**
 *
 * https://www.jianshu.com/p/bdb845401405
 *
 * domain层，是一个承上启下的层：
 * 与上层（Presentation Layer[就是专注于处理UI、动画逻辑的地方，可以使用MVP
 * ，MVVM，MVC等，在这层一般就是处理View，不能够直接更改下层的数据，更改数据需要通过Domain层来实现]）
 * 交互的接口放在包interactor中（use case 又叫 Task；定义在本层）；
 *
 * 与下层（Data Layer）交互的接口放在包repository也定义在本层；
 *
 * 本层实现所有业务逻辑；
 *
 * 本层是核心，用纯java代码实现，可以随时拿走；
 */
package com.zfun.learn.architecture.effective.domain;
