/**
 * 简化Entity调用。<br/>
 * 【注意】这个只能单向依赖Entity，它位于Entity的外层，千万不要让Entity调用到它。这个包整体删除也不影响Entity。
 * <p/>
 * 实现：<br/>
 * 1，单例，方便使用。<br/>
 * 2，对Entity进行依赖注入，后续可以在这里更换Entity对于三方工具的依赖以及数仓的替换。<br/>
 *
 */
package com.zfun.learn.architecture.clean.album.entities.album.provider;