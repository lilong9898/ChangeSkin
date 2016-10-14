package com.lilong.skinchange.other;

import com.lilong.skinchange.manager.SkinManager;
import com.lilong.skinchange.utils.SkinInfo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class SkinListRecyclerView extends RecyclerView {

    private SkinManager skinManager;

    public SkinListRecyclerView(Context context) {
        super(context);
        init();
    }

    public SkinListRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkinListRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        skinManager = SkinManager.getInstance(getContext());
    }

    private void setItemSelected(int position, boolean scrollToSelected) {

        for (int i = 0; i < getChildCount(); i++) {
            View unselectedChild = getChildAt(i);
            unselectedChild.setSelected(false);
        }

        View selectedChild = getChildAt(position);
        if (selectedChild == null) {
            return;
        }

        selectedChild.setSelected(true);

        if (scrollToSelected) {
            scrollToPosition(position);
        }

    }

    public void setItemSelectedByCurSkinInfo() {
        SkinInfo curSkinInfo = skinManager.getCurSkinInfo();
        int index = skinManager.getSkinInfoIndex(curSkinInfo);
        if (index != SkinManager.SKIN_INFO_INDEX_INVALID || index < getChildCount()) {
            setItemSelected(index, true);
        }
    }

    @Override
    public void onChildAttachedToWindow(View child) {
        super.onChildAttachedToWindow(child);
        setItemSelectedByCurSkinInfo();
    }
}
