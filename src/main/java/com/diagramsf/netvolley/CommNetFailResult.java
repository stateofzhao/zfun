package com.diagramsf.netvolley;

import android.content.Context;
import android.widget.Toast;
import com.diagramsf.net.base.NetFailResultBase;

/**
 * Created by Diagrams on 2016/6/26 18:27
 */
public class CommNetFailResult extends NetFailResultBase {

    public Exception e;

    @Override
    public void setException(Exception e) {
        this.e = e;
    }

    @Override
    public void toastFailStr(Context context) {
        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void logFailInfo(String tag) {

    }

    @Override
    public String getInfoText(Context context) {
        return null;
    }
}
