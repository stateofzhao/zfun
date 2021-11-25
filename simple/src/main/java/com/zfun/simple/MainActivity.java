package com.zfun.simple;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zfun.simple.binary.BinaryActivity;
import com.zfun.simple.letternavigation.LetterNavigationActivity;
import com.zfun.simple.pullrefresh.PullRefreshActivity;
import com.zfun.simple.usecase.InteractorTestActivity;
import com.zfun.test.jniclass.ObserverUninstall;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  final String[] mActionData = new String[] { "字母导航控件示例", "UseCase层测试", "二进制学习", "下拉刷新布局" };

  RecyclerView mListView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //启用卸载统计
    ObserverUninstall ou = new ObserverUninstall();
    ou.init(null, "uninstall/test.php", "www.baidu.com");

    findView();
    fillView();
  }

  @Override public void onClick(View view) {
    Object tag = view.getTag();
    int position = (int) tag;
    if (position == 0) {
      LetterNavigationActivity.open(this);
    } else if (position == 1) {
      InteractorTestActivity.open(this);
    } else if (position == 2) {
      BinaryActivity.open(this);
    } else if(position == 3){
      PullRefreshActivity.open(this);
    }
  }

  private void findView() {
    mListView = (RecyclerView) findViewById(R.id.list);
  }

  private void fillView() {
    mListView.setLayoutManager(new LinearLayoutManager(this));
    mListView.setAdapter(new ItemAdapter());
  }

  private class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new NormalItemHolder(getLayoutInflater(), parent, MainActivity.this);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      String itemData = mActionData[position];
      holder.itemView.setTag(position);
      ((TextView) holder.itemView).setText(itemData);
    }

    @Override public int getItemCount() {
      return mActionData.length;
    }
  }// end class ItemAdapter

  static abstract class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(View itemView) {
      super(itemView);
    }
  }// end class ViewHolder

  static class NormalItemHolder extends ViewHolder {
    public NormalItemHolder(LayoutInflater inflater, ViewGroup parent,
        View.OnClickListener listener) {
      super(inflater.inflate(android.R.layout.simple_list_item_1, parent, false));
      itemView.setOnClickListener(listener);
    }
  }// end class NormalItemHolder
}// end class MainActivity
