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

    private TextView tvTitle;
    private TextView tvNumber;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_demo, container, false);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        tvNumber = (TextView) rootView.findViewById(R.id.tv_number);

        String tag = "";
        if (getArguments() != null && getArguments().getString(ARGUMENT_TAG_ID) != null) {
            tag = getArguments().getString(ARGUMENT_TAG_ID);
        }
        tvNumber.setText(tag);
        return rootView;
    }

    @Override
    public void onSkinLoadStart() {

    }

    @Override
    public void onSkinLoadFinish(ArrayList<SkinInfo> skinInfos) {

    }
}
