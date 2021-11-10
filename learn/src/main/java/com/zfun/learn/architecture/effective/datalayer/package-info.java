/**
 * 数据层，负责提供业务所需要的数据，一般数据有三个来源：
 * 1.内存缓存中的数据。
 * 2.磁盘文件缓存中的数据（包括数据库）。
 * 3.网络来源的数据。
 * <P>
 * 这一层一般通过实现domain层中的'repository'接口来暴露数据给domain层。
 * <P>
 * 该层可以有与平台相关的代码依赖，比如 android中的MySQL相关类。
 */
package com.zfun.learn.architecture.effective.datalayer;
