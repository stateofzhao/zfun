package com.zfun.learn;


import android.support.v4.app.Fragment;

//不再推荐像这样采用继承结构来处理UI层逻辑了，继承结构处理UI层逻辑太恶心了，经常使用者找不到View在哪里。
//现在推荐使用组合形式（采用装饰着模式来实现）来装配需要的封装好的UI逻辑。
/**
 * 能够显示进度条的Fragment
 *
 * @author Diagrams
 *         2015/6/6
 *         18:49
 */
@Deprecated
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
