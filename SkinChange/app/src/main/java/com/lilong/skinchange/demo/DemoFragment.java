package com.lilong.skinchange.demo;

import com.lilong.skinchange.R;
import com.lilong.skinchange.base.SkinFragment;
import com.lilong.skinchange.utils.SkinInfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class DemoFragment extends SkinFragment {

    public static final String ARGUMENT_TAG_ID = "argument_tag_id";

    private TextView tvTitleFrag;

    @Nullable
    @Override
    public View onCreateViewSkin(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_demo, container, false);
        tvTitleFrag = (TextView) rootView.findViewById(R.id.tv_title_frag);
        tvTitleFrag.setText("" + tvTitleFrag.getText() + " " + getArguments().get(ARGUMENT_TAG_ID));
        return rootView;
    }

    @Override
    public void onSkinLoadStart() {

    }

    @Override
    public void onSkinLoadFinish(ArrayList<SkinInfo> skinInfos) {

    }
}
