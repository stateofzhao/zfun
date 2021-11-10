/**
 *
 * 学习架构模式，MVC，MVP，MVVM，Flux等。
 * <P/>
 * MVC,MVP,MVVM中的Model和View都是一样的概念：<br/>
 * Model：模型，业务需求对应的计算机模型，可以理解为仅仅是javaBean；<br/>
 * 也可以理解为包含数据获取，包含将获取的数据组合成业务需求的JavaBean的过程。<br/>
 *
 * View：这个没有争议，就是用户界面。
 *
 * Controller：在Android中就是 Activity，Fragment；为了给Activity，Fragment减负，
 * 一般不把Controller的逻辑写到 Activity，Fragment里面，此时就单独写一个类出来来处理 Activity，Fragment与Model的交互，
 * 为了解藕一般把Activity，Fragment抽象成接口，然后传递给抽象出来的类，为了与Controller区别，这个类就重新起名叫Presenter了。
 * */
package com.zfun.learn.architecture;