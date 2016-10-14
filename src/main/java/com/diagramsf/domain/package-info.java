/**
 *
 * 关于领域模型的解释参见这个链接：
 * https://www.oschina.net/question/12_21641
 *
 * domain层，是一个承上启下的层，与上层（Presentation Layer[就是专注于处理UI、动画逻辑的地方，这里可以使用MVP
 * ，MVVM，MVC等，在这层一般就是处理View，不能够直接更改下层的数据，更改数据需要通过Domain层来实现]）
 * 交互的接口（use case 又叫 Interactor）定义在本层，与下层（Data Layer）交互的接口也定义在本层。
 */
package com.diagramsf.domain;
