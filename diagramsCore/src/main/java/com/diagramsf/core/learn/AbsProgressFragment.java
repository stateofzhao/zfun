package com.diagramsf.core.learn;


import android.support.v4.app.Fragment;

/**
 * 能够显示进度条的Fragment
 *
 * @author Diagrams
 *         2015/6/6
 *         18:49
 */
public abstract class AbsProgressFragment extends Fragment {

    /** 显示进度条 */
    public abstract void showProgressBar();

    /** 隐藏进度条 */
    public abstract void hideProgressBar();

    /**
     * 是否可以调用它的父Activity
     *
     * @return true 可以，false不可以
     */
    public final boolean isCanWorkWithActivity() {
        return null != getActivity();
    }

}
