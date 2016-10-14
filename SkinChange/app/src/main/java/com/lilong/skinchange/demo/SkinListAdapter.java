package com.lilong.skinchange.demo;

import com.lilong.skinchange.R;
import com.lilong.skinchange.utils.SkinInfo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lilong on 16-10-12.
 */

public class SkinListAdapter extends RecyclerView.Adapter<SkinListAdapter.SkinItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClicked(int position, SkinInfo info, View v);
    }

    private OnItemClickListener onItemClickListener;

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<SkinInfo> skinInfos;

    public SkinListAdapter(Context context) {
        this.context = context;
        skinInfos = new ArrayList<SkinInfo>();
        inflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setSkinInfos(ArrayList<SkinInfo> skinInfos) {
        this.skinInfos = skinInfos;
    }

    @Override
    public int getItemCount() {
        return skinInfos.size();
    }

    @Override
    public SkinItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SkinItemViewHolder vh = new SkinItemViewHolder(inflater.inflate(R.layout.layout_skin_item, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(final SkinItemViewHolder holder, final int position) {

        final SkinInfo info = skinInfos.get(position);
        holder.tvSkinName.setText(info.getSkinName());
        holder.tvSkinDescription.setText(info.getSkinDescription());
        if (holder.itemView.hasOnClickListeners() == false && onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClicked(position, info, holder.itemView);
                }
            });
        }

        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (position == 0) {
            lp.leftMargin = lp.rightMargin;
        } else {
            lp.leftMargin = 0;
        }
        holder.itemView.setLayoutParams(lp);
    }

    public static class SkinItemViewHolder extends RecyclerView.ViewHolder {

        public TextView tvSkinName;
        public TextView tvSkinDescription;

        public SkinItemViewHolder(View v) {
            super(v);
            tvSkinName = (TextView) v.findViewById(R.id.tv_skin_name);
            tvSkinDescription = (TextView) v.findViewById(R.id.tv_skin_description);
        }
    }
}
