package com.zfun.example.pullrefresh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zfun.lib.widget.xpullrefresh.XPullRefreshLayout;
import com.zfun.example.R;

/**
 * Created by zfun on 2021/12/8 2:30 PM
 */
public class XPullRefreshActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    XPullRefreshLayout pullRefreshLayout;

    Handler handler = new Handler(Looper.getMainLooper());

    public static void open(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, XPullRefreshActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xpullrefresh);
        initView();
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        pullRefreshLayout = findViewById(R.id.rl);
        recyclerView.setVisibility(View.VISIBLE);

        configRefreshLayout();
        refreshData();
    }

    private void configRefreshLayout(){
        final XPullRefreshLayout.Params params = new XPullRefreshLayout.Params(XPullRefreshLayout.STYLE_PULL_REFRESH);
        params.disableControlHeaderView = true;
        params.headerPullRefreshLimit = 400;
        pullRefreshLayout.setOnRefreshListener(() -> {
            handler.postDelayed(new RefreshTask(), 2000);//延迟两秒，模拟网络操作
        });
        pullRefreshLayout.setParams(params);
    }

    private void refreshData() {
        boolean isRecycler = recyclerView.getVisibility() == View.VISIBLE;
        String[] data = new String[20];
        for (int i = 0; i < data.length; i++) {
            data[i] = isRecycler ?"TEST - RECYCLER":"TEST - LIST" + Math.random();
        }
        if (recyclerView.getAdapter() instanceof RecyclerAdapter) {
            ((RecyclerAdapter) recyclerView.getAdapter()).refresh(data);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new RecyclerAdapter(data));
        }
    }

    public class RefreshTask implements Runnable {

        @Override
        public void run() {
            refreshData();
            pullRefreshLayout.setRefreshEnd();
        }
    }//

    public static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyHolder> {
        String[] data;

        public RecyclerAdapter(String[] data) {
            this.data = data;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            holder.bind(data[position]);
        }

        @Override
        public int getItemCount() {
            return null == data ? 0 : data.length;
        }

        public void refresh(String[] data) {
            this.data = data;
            notifyDataSetChanged();
        }

        public static class MyHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.tv);
            }

            public void bind(String text) {
                textView.setText(text);
            }
        }//
    }//
}
