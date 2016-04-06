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
 * requests的默认重试策略。
 * <p>
 * <p>
 * Default retry policy for requests.
 */
public class DefaultRetryPolicy implements RetryPolicy {
    /** 当前超时时间，毫秒为单位。<p> The current timeout in milliseconds. */
    private int mCurrentTimeoutMs;

    /** 当前尝试的次数。<p> The current retry count. */
    private int mCurrentRetryCount;

    /** The maximum number of attempts. */
    private final int mMaxNumRetries;

    /** 回扣乘数的策略。<p> The backoff multiplier for for the policy. */
    private final float mBackoffMultiplier;

    /** 默认的套接字超时时间以毫秒为单位。<p> The default socket timeout in milliseconds */
    public static final int DEFAULT_TIMEOUT_MS = 2500;
    //    public static final int DEFAULT_TIMEOUT_MS = 10*1000;

    /** 默认重试次数。<p> The default number of retries */
    public static final int DEFAULT_MAX_RETRIES = 1;

    /** 默认回扣乘数。<p> The default backoff multiplier */
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    /**
     * 使用默认的参数构造一个 重试策略。
     * <p>
     * Constructs a new retry policy using the default timeouts.
     */
    public DefaultRetryPolicy() {
        this(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT);
    }

    /**
     * 构造一个新的 重试策略。
     * <p>
     * <p>
     * Constructs a new retry policy.
     *
     * @param initialTimeoutMs  The initial timeout for the policy.最初超时时间
     * @param maxNumRetries     The maximum number of retries.最大尝试次数
     * @param backoffMultiplier Backoff multiplier for the policy.回扣乘数
     */
    public DefaultRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier) {
        mCurrentTimeoutMs = initialTimeoutMs;
        mMaxNumRetries = maxNumRetries;
        mBackoffMultiplier = backoffMultiplier;
    }

    /**
     * Returns the current timeout.
     */
    @Override
    public int getCurrentTimeout() {
        return mCurrentTimeoutMs;
    }

    /**
     * Returns the current retry count.
     */
    @Override
    public int getCurrentRetryCount() {
        return mCurrentRetryCount;
    }

    /**
     * 根据超时回扣，准备进行重试。
     * <p>
     * <p>
     * Prepares for the next retry by applying a backoff to the timeout.
     *
     * @param error The error code of the last attempt.
     */
    @Override
    public void retry(VolleyError error) throws VolleyError {
        mCurrentRetryCount++;
        mCurrentTimeoutMs += (mCurrentTimeoutMs * mBackoffMultiplier);
        if (!hasAttemptRemaining()) {
            throw error;
        }
    }

    /**
     * 如果重试策略仍在尝试，返回true；否则返回false。
     * <p>
     * <p>
     * Returns true if this policy has attempts remaining, false otherwise.
     */
    protected boolean hasAttemptRemaining() {
        return mCurrentRetryCount <= mMaxNumRetries;
    }
}
