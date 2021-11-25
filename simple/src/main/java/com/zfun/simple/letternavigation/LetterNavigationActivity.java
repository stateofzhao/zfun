package com.zfun.simple.letternavigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.zfun.lib.widget.LetterNavigation;
import com.zfun.simple.R;

public class LetterNavigationActivity extends AppCompatActivity implements LetterNavigation.OnItemSelected {

    LetterNavigation mVNavigation;

    public static void open(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, LetterNavigationActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_navigation);

        mVNavigation = (LetterNavigation) findViewById(R.id.ln);
        mVNavigation.setOnItemSelected(this);
    }

    @Override
    public void onSelected(int position, Object positionItem, Object positionValue) {
    }
}
