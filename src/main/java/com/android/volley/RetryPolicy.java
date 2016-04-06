/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.volley;

/**
 * request 的重试策略。就是定义请求失败后何时再发起请求
 * <p>
 * 
 * Retry policy for a request.
 */
public interface RetryPolicy {

    /**
     * 返回当前超时时间。（调试用的），根据回退乘数 计算得到当前重试的超时时间。
     * <P>
     * 
     * Returns the current timeout (used for logging).
     */
    public int getCurrentTimeout();

    /**
     * 返回当前重试次数。
     * <P>
     * 
     * Returns the current retry count (used for logging).
     */
    public int getCurrentRetryCount();

    /**
     * 根据超时回扣，准备进行重试
     * <p>
     * 
     * Prepares for the next retry by applying a backoff to the timeout.
     * @param error The error code of the last attempt.
     * @throws VolleyError In the event that the retry could not be performed (for example if we
     * ran out of attempts), the passed in error is thrown.
     */
    public void retry(VolleyError error) throws VolleyError;
}
