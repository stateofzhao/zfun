package com.diagramsf.netvolley;

import android.content.Context;
import android.widget.Toast;
import com.diagramsf.net.base.BaseFail;

/**
 * Created by Diagrams on 2016/6/26 18:27
 */
public class CommFail extends BaseFail {

  @Override public void toastFailStr(Context context) {
    Toast.makeText(context, getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
  }

  @Override public String getInfoText(Context context) {
    return null;
  }
}
