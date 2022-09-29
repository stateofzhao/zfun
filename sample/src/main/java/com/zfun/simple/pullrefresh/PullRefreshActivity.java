package com.zfun.simple.pullrefresh;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.zfun.lib.widget.pullrefresh.PullRefreshLayout;
import com.zfun.simple.R;

public class PullRefreshActivity extends AppCompatActivity {
    ListView listView;
    PullRefreshLayout pullRefreshLayout;

    Handler handler = new Handler(Looper.getMainLooper());

    public static void open(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, PullRefreshActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pullrefresh);
        initView();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.list);
        pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.rl);
        refreshData();

        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new RefreshTask(), 20000);//延迟两秒，模拟网络操作
            }

            @Override
            public void onRefreshComplete() {
            }
        });
    }

    private void refreshData() {
        String[] data = new String[20];
        for (int i = 0; i < data.length; i++) {
            data[i] = "TEST" + Math.random();
        }
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.list_item, R.id.tv, data));
    }

    public class RefreshTask implements Runnable {

        @Override
        public void run() {
            refreshData();
            pullRefreshLayout.stopRefresh();
        }
    }
}
