package com.zfun.learn;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LearnFragment extends Fragment {

    // ----------------------------对应与Activity 的 Created 状态

    /** 与Activity关联起来了 */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /** Activity的 onCreate()方法执行完毕后，才调用这个方法 */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // ----------------------与Activity的生命周期方法相仿

    /**
     * 这几个与Activity生命周期相仿的方法，需要注意一点，就是只有Activity相应的方法调用或者Fragment首次add后
     * 才会分发给Fragment的这几个方法。
     * <p/>
     * 像调用FragmentTranslation的show()和hide()方法，并不会执行Fragment的这几个方法 （已验证）
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 在{@link Activity#onSaveInstanceState(Bundle)} 中调用此方法。（目前所学只能看出来会在{@link #onDestroyView()}之前调用,不知道
     * 何种情况下，会在 {@link #onDestroyView()} 方法之后调用，所以最好不要在此方法中调用View）
     * <p/>
     * 注意：无论如何会在 {@link #onDestroy()}之前调用此方法。
     * Fragment中没有 onRestoreInstanceState(Bundle) 方法！
     * <p/>
     * 注意：调用{@link android.support.v4.app.FragmentTransaction#detach(Fragment)}方法把{@link Fragment}detach掉后，
     * 不会触发这个回调！
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);// 必须调用，因为android系统会自动保存一些View堆栈信息，例如哪个View当前获取焦点了等
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // ---------------------------以 下的几个方法对应与 Activity的 Destroyed状态
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /** Fragment 正与Activity接触关联（执行此方法时，Fragment仍然与Activity是关联的） */
    @Override
    public void onDetach() {
        super.onDetach();
    }

}
