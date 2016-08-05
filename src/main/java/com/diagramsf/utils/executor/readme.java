package com.diagramsf.utils.executor;

import android.os.AsyncTask;

/**
 * 
 * 这个包下的类是一个线程池，用来执行异步任务。由于android 的 {@link AsyncTask}是所有app共享的 最大可执行任务数是
 * CPU_COUNT * 2 + 1 ，所以它不适合做长时间的后台任务，只适合做只有几秒的后台操作。
 * <P>
 * 
 * 本包下的类，是一个自定义的线程池，最大线程数是 {@link MyExecutor#MAX_THREAD}，核心线程数是
 * {@link MyExecutor#CORE_THREAD}，可以根据需要使用。
 * 
 * */
public class readme {

}
