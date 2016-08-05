package com.diagramsf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import com.diagramsf.util.AndroidUtil;
import com.diagramsf.util.DeviceUtil;

/**
 * 监听网络状态变化的{@link BroadcastReceiver}，参考
 * Picasso中的监听网络变化的接收器
 * <p>
 * Created by Diagrams on 2016/1/21 15:28
 */
public abstract class NetStateReceiver extends BroadcastReceiver {

  static final String EXTRA_AIRPLANE_STATE = "state";

  /**
   * 注册此广播接收器
   */
  public void register(Context context) {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    context.registerReceiver(this, filter);
  }

  /**
   * 取消注册此广播接收器
   */
  public void unregister(Context context) {
    context.unregisterReceiver(this);
  }

  @Override public void onReceive(Context context, Intent intent) {
    // On some versions of Android this may be called with a null Intent,
    // also without extras (getExtras() == null), in such case we use defaults.
    if (intent == null) {
      onDefault();
      return;
    }

    final String action = intent.getAction();
    if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
      if (!intent.hasExtra(EXTRA_AIRPLANE_STATE)) {
        if (AndroidUtil.isAirplaneModeOn(context)) {
          onAirMode();
        }
        return; // No airplane state, ignore it. Should we query RequestManager.isAirplaneModeOn?
      }
      if (intent.getBooleanExtra(EXTRA_AIRPLANE_STATE, false)) {
        onAirMode();
      }
    } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
      onNetStatChange(DeviceUtil.getNetType(context));
    }
  }

  /**
   * 开启了飞行模式
   */
  public abstract void onAirMode();

  /**
   * 网络状态变化
   */
  public abstract void onNetStatChange(DeviceUtil.NetType type);

  /**
   * 收到Android系统发送的网路状态变化，但是只是收到通知没有收到数据；
   */
  public abstract void onDefault();
}
