package com.zfun.sharelib.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.zfun.sharelib.R;


public abstract class BottomRoundDialog extends Dialog {

    public BottomRoundDialog(Context context) {
        this(context, R.style.DialogBottomRound);
    }

    public BottomRoundDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window != null){
            window.setWindowAnimations(R.style.PopupAnimation);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM | Gravity.START;
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setAttributes(lp);
        }
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_bottom_round, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(view, params);
        onCreateContentView(getLayoutInflater(), view);
    }

    protected abstract View onCreateContentView(LayoutInflater inflater, ViewGroup rootView);

    @Override
    public void show() {
        super.show();
        //修改系统menu菜单不能全屏显示问题
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setAttributes(layoutParams);
        }
    }
}
