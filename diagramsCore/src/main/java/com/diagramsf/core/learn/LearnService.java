package com.diagramsf.core.learn;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * 官方简体文档：<br>
 * http://developer.android.com/intl/zh-cn/guide/components/services.html
 * <p>
 * 学习{@link Service}。关于Service的Manifest声明参见：http://developer.android.com/intl/zh-cn/guide/topics/manifest/service-element.html
 * <p>
 * 注意以下几点知识点：<br>
 * 1.通过调用 {@link android.content.ContextWrapper#startService(Intent)}启动服务（会导致对{@link #onStartCommand(Intent, int, int)}的调用），
 * 则服务将一直运行（即使启动服务的组件已被销毁也不受影响），直到服务使用{@link #stopSelf()}自行停止运行，
 * 或由其他组件通过调用{@link android.content.ContextWrapper#stopService(Intent)}停止它为止。
 * <p>
 * 2.如果组件是通过调用 {@link android.content.ContextWrapper#bindService(Intent, ServiceConnection, int)}来创建服务（且未调用onStartCommand()），
 * 则服务只会在该组件与其绑定时运行（组件destroy后会自动取消与服务的绑定）。一旦该服务与所有客户端之间的绑定全部取消，系统便会销毁它。
 * <p>
 * 3.{@link #onCreate()}在Service的一次生命周期中只执行一次，{@link #onStartCommand(Intent, int, int)}会在每次请求启动服务（调用
 * {@link android.content.ContextWrapper#startService(Intent)}）时调用，但是要停止服务，只需一个服务停止请求（使用{@link #stopSelf()}
 * 或{@link android.content.ContextWrapper#stopService(Intent)}）。
 * <p>
 * 4.由于调用{@link #stopSelf()}或{@link android.content.ContextWrapper#stopService(Intent)}会停止服务，但是如果服务同时处理
 * 多个{@link #onStartCommand(Intent, int, int)}请求，则您不应该在处理完一个启动请求之后停止服务，此时应该使用
 * {@link #stopSelf(int)}方法，来把 onStartCommand(Intent,int,int) 方法的第三个参数传递进来，这样系统会根据最近的
 * onStartCommand(Intent,int,int)中的第三个参数是否与 stopSelf(int) 参数匹配，如果匹配就销毁服务，否则不销毁服务。
 * <p>
 * 5.一旦请求使用 stopSelf() 或 stopService() 停止服务，系统就会尽快销毁服务。并不是马上销毁服务。
 * <p>
 * 6.停止服务需要注意，对于 绑定到服务时，当所有绑定都解除绑定时服务自己销毁；对于 startService()开启服务时，需要自己显示调用
 * 停止服务（stopService()或stopSelf()）；当两者都存在时，要停止服务就需要 这两者兼顾了，stopService()或stopSelf() 只有在
 * 绑定到服务的都解除绑定时 才起作用，所有绑定到服务的都解除绑定时 也不会销毁服务，当调用了 stopService()或stopSelf()才会销毁服务。
 * <p>
 * <b>在Manifest中一旦声明了服务的名称android:name 就不要更改了，具体Android项目中哪些内容不能更改参见网址：<br>
 * http://android-developers.blogspot.com/2011/06/things-that-cannot-change.html
 * </b>
 * <p>
 * Created by Diagrams on 2016/3/16 14:35
 */
public class LearnService extends Service {

    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。
     * 如果服务已在运行，则不会调用此方法。
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 当另一个组件（如 Activity）通过调用 startService() 请求启动服务时，系统将调用此方法。一旦执行此方法，
     * 服务即会启动并可在后台无限期运行。 如果您实现此方法，则在服务工作完成后，需要由您通过调用 stopSelf() 或
     * stopService() 来停止服务。（如果您只想提供绑定，则无需实现此方法。）
     *
     * @param intent 其实就是调用{@link #startService(Intent)}传递过来的intent
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 当另一个组件想通过调用 bindService() 与服务绑定（例如执行 RPC）时，系统将调用此方法。在此方法的实现中，
     * 您必须通过返回 IBinder 提供一个接口，供客户端用来与服务进行通信。请务必实现此方法，但如果您并不希望允许绑定，
     * 则应返回 null。
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 当调用 {@link android.content.ContextWrapper#unbindService(ServiceConnection)}时，服务回调
     * {@link #onUnbind(Intent)}生命周期方法，返回true时，再次绑定到服务时 才会回调此生命周期方法。
     */
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * 当服务不再使用且将被销毁时，系统将调用此方法。服务应该实现此方法来清理所有资源，如线程、注册的侦听器、接收器等。
     * 这是服务接收的最后一个调用。
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
