/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zfun.learn.architecture.mvp;

/**
 * Presenter 是Model和View的中间衔接层，一个标准的Presenter中应该至少包含一个Model和一个View。<br/>
 * 在MVP模式中，View是不允许与Model交互的，View只是负责展示数据，是彻底的哑View。当View需要更新数据时，
 * 需要通过Presenter来操作Model，Model获取到数据后再通过Presenter来更新View。<br/>
 * 所以数据流通是这样的:
 * ->           ->           Remote data source
 * View    Presenter    Model ->
 * <-            <-          Local data source
 * <br/>
 * 需要注意：Presenter不直接操作数据（更新数据状态），需要调用Model操作数据（更新数据状态）<br/>
 * <p/>
 * 如果结合Architecture思想使用的话，这里的Model都是一个个不同功能的UseCase，Presenter通过组合
 * 各个UseCase来获取需要的数据，然后通知给View。
 */
public interface BasePresenter {
}
