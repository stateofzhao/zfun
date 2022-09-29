package com.zfun.learn.architecture.uiarchitecture.mvc;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;

//仅仅当作View的生命周期管理类
public class UserFragment extends Fragment {
    private UserController controller;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = new UserController(view);
        controller.start();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        controller.destroyView();
    }
}
