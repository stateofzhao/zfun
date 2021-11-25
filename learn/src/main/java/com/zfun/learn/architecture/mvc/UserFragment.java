package com.zfun.learn.architecture.mvc;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;

public class UserFragment extends Fragment {
    private UserController controller;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = new UserController(new UserModel(BasicApp.getInstance().getUserRepository()),view);
        controller.start("1");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        controller.destroyView();
    }
}
