package com.zfun.sharelib.widget;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zfun.sharelib.R;

import java.util.List;

public class ShareDialog extends BottomRoundDialog {
    private final List<ShareMenuItemProvider> mMenuData;
    private final int mItemResource;

    private View.OnClickListener mCancelListener;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    private TextView tvTitle;
    private TextView tvCancel;
    private GridView gridView;
    private String mTitle;

    public ShareDialog(Context context, List<ShareMenuItemProvider> menuData, int itemResource) {
        super(context);
        this.mMenuData = menuData;
        this.mItemResource = itemResource;
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup rootView) {
        View view = inflater.inflate(R.layout.layout_share_dialog, rootView, true);
        tvTitle = view.findViewById(R.id.tv_title);
        tvCancel = view.findViewById(R.id.tv_cancel);
        gridView = view.findViewById(R.id.gridview);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        final ShareAppAdapter adapter = new ShareAppAdapter(getContext(), mMenuData, mItemResource);
        gridView.setAdapter(adapter);
        gridView.setNumColumns(4);
        tvTitle.setText(mTitle);
        tvCancel.setText("关闭");
        tvCancel.setOnClickListener(mCancelListener);
        gridView.setOnItemClickListener(mOnItemClickListener);
    }

    public void setTitle(String text) {
        this.mTitle = text;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setCancelBtn(View.OnClickListener listener) {
        this.mCancelListener = listener;
    }


    //分享菜单适配器
    public static class ShareAppAdapter extends BaseAdapter {
        private final Context mContext;
        private final List<ShareMenuItemProvider> mProviders;
        private final int mItemResource;

        public ShareAppAdapter(Context pContext, List<ShareMenuItemProvider> providers, int itemResource) {
            this.mContext = pContext;
            this.mItemResource = itemResource;
            this.mProviders = providers;
        }

        @Override
        public int getCount() {
            return this.mProviders.size();
        }

        @Override
        public ShareMenuItemProvider getItem(final int pPosition) {
            return this.mProviders.get(pPosition);
        }

        @Override
        public long getItemId(final int pPosition) {
            return pPosition;
        }

        @Override
        public View getView(final int pPosition, View pConvertView, final ViewGroup pParent) {
            if (pConvertView == null) {
                if (mItemResource <= 0) {
                    pConvertView = LayoutInflater.from(this.mContext).inflate(R.layout.share_provider_item, pParent, false);
                } else {
                    pConvertView = LayoutInflater.from(this.mContext).inflate(mItemResource, pParent, false);
                }
                final ShareProviderHolder holder = new ShareProviderHolder();
                holder.icon = (ImageView) pConvertView.findViewById(R.id.icon);
                holder.name = (TextView) pConvertView.findViewById(R.id.name);
                pConvertView.setTag(holder);
            }

            final ShareProviderHolder holder = (ShareProviderHolder) pConvertView.getTag();

            holder.icon.setImageResource(this.getItem(pPosition).icon);
            holder.name.setText(this.getItem(pPosition).name);
            return pConvertView;
        }

        private static class ShareProviderHolder {
            public ImageView icon;
            public TextView name;
        }
    }//ShareAppAdapter end
}
