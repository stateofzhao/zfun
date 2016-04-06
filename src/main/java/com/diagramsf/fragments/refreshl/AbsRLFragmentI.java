package com.diagramsf.fragments.refreshl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 下拉刷新和上拉加载更多Fragment接口
 * <p>
 * Created by Diagrams on 2016/3/15 11:15
 */
public interface AbsRLFragmentI {

    void onCreate(Bundle savedInstanceState);

    View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    void onViewCreated(View view, Bundle savedInstanceState);

    void onSaveInstanceState(Bundle outState);

    void onDestroyView();
}
