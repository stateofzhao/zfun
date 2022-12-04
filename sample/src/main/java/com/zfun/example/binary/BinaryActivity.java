package com.zfun.example.binary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zfun.learn.binary.BinaryUtils;
import com.zfun.lib.util.AndroidUtil;
import com.zfun.example.R;

/**
 * {@link BinaryUtils}
 * */
public class BinaryActivity extends AppCompatActivity {
  LinearLayout ll;
  TextView tv1, tv2, tv3, tv4;
  TextView tv5, tv6, tv7, tv8;

  public static void open(Context context) {
    Intent intent = new Intent();
    intent.setClass(context, BinaryActivity.class);
    context.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_binary);

    ll = (LinearLayout) findViewById(R.id.activity_binary);
    tv1 = (TextView) findViewById(R.id.tv1);
    tv2 = (TextView) findViewById(R.id.tv2);
    tv3 = (TextView) findViewById(R.id.tv3);
    tv4 = (TextView) findViewById(R.id.tv4);
    tv5 = (TextView) findViewById(R.id.tv5);
    tv6 = (TextView) findViewById(R.id.tv6);
    tv7 = (TextView) findViewById(R.id.tv7);
    tv8 = (TextView) findViewById(R.id.tv8);

    MyDivider cd = new MyDivider(Color.GRAY);
    cd.setBounds(0, 0, AndroidUtil.getScreenWidth(this), 2);
    ll.setDividerDrawable(cd);
    ll.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    cd.invalidateSelf();

    tv1.append(BinaryUtils.intToBinaryStrComplement(-10));
    tv2.append(BinaryUtils.intToBinaryStrComplement(10));
    tv3.append(BinaryUtils.binaryToIntStrComplement("11111111111111111111111111110110"));
    tv4.append(BinaryUtils.binaryToIntStrComplement("00001010"));
    tv5.append(BinaryUtils.intToBinaryStr("-10"));
    tv6.append(BinaryUtils.intToBinaryStr("10"));
    tv7.append(BinaryUtils.binaryToIntStr("11111111111111111111111111110110"));
    tv8.append(BinaryUtils.binaryToIntStr("00001010"));
  }

  class MyDivider extends ColorDrawable {
    MyDivider(int color) {
      super(color);
    }

    @Override public int getIntrinsicHeight() {
      return 2;
    }

    @Override public int getIntrinsicWidth() {
      return AndroidUtil.getScreenWidth(BinaryActivity.this);
    }
  }
}
