package com.zfun.simple.usecase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.zfun.lib.executor.TaskHandler;
import com.zfun.simple.R;

public class InteractorTestActivity extends AppCompatActivity implements View.OnClickListener {
  TextView tv;
  Button start;
  Button cancel;

  public static void open(Context context) {
    Intent intent = new Intent();
    intent.setClass(context, InteractorTestActivity.class);
    context.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_use_case_test);

    tv = (TextView) findViewById(R.id.tv_info);
    start = (Button) findViewById(R.id.bt_start);
    cancel = (Button) findViewById(R.id.bt_cancel);
    start.setOnClickListener(this);
    cancel.setOnClickListener(this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    TaskHandler.instance().cancel(this);
  }

  private StringBuilder stringBuilder = new StringBuilder();

  @Override public void onClick(View view) {
    if (view == start) {
      final Callback<String, Exception> callback = new Callback<String, Exception>() {
        @Override public void onResponse(String response) {
          stringBuilder.append("结束执行TestTask[ ").append(hashCode()).append(" ]").append(response).append("  ");
          tv.setText(stringBuilder.toString());
        }

        @Override public void onError(Exception error) {
          stringBuilder.append(hashCode()).append(error.toString());
          tv.setText(stringBuilder.toString());
        }
      };
     final InteractorTest interactorTest = new InteractorTest(callback);
      TaskHandler.instance().execute(interactorTest, this);
      stringBuilder.append("\n");
      stringBuilder.append("开始加载TestTask[ ").append(callback.hashCode()).append(" ]").append("  ");
      tv.setText(stringBuilder.toString());
    } else if (view == cancel) {
      TaskHandler.instance().cancel(this);
      stringBuilder.append("取消TestTask的执行").append("  ");
      tv.setText(stringBuilder.toString());
    }
  }
}
