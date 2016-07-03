package com.diagramsf;

import android.content.Context;
import com.diagramsf.netvolley.NetRequestManager;

/**
 * Created by Diagrams on 2016/7/3 11:47
 */
public class RemoteDataSource implements EntitiesDataSource {
    private NetRequestManager netRequestManager;

    private volatile static RemoteDataSource single;

    private RemoteDataSource(NetRequestManager requestManager){
        this.netRequestManager = requestManager;
    }

    public static RemoteDataSource with(Context context){
        if(null == single){
            synchronized (RemoteDataSource.class){
                if(null == single){
                    single = new RemoteDataSource(NetRequestManager.with(context));
                }
            }
        }

        return single;
    }

    public NetRequestManager.RequestCreator load(String url){
        return netRequestManager.load(url);
    }

    /**
     * @param url    请求的网址
     * @param method {@link com.android.volley.Request.Method}中的一种
     */
    public NetRequestManager.RequestCreator load(String url, int method) {
        return netRequestManager.load(url,method);
    }

}
