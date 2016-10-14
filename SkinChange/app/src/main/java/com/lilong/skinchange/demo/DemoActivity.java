package com.lilong.skinchange.demo;

import com.lilong.skinchange.R;
import com.lilong.skinchange.base.SkinActivity;
import com.lilong.skinchange.manager.SkinManager;
import com.lilong.skinchange.other.SkinListAdapter;
import com.lilong.skinchange.other.SkinListRecyclerView;
import com.lilong.skinchange.utils.SkinInfo;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.ArrayList;

public class DemoActivity extends SkinActivity {

    private SkinManager skinManager;
    private SkinListRecyclerView rcSkin;
    private LinearLayoutManager llm;
    private SkinListAdapter skinListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        skinManager = SkinManager.getInstance(getApplicationContext());
        SkinInfo lastSkinInfo = skinManager.getCurSkinInfo();
        skinManager.changeSkin(getApplicationContext(), getWindow().getDecorView(), skinizedAttrMap, lastSkinInfo);

        rcSkin = (SkinListRecyclerView) findViewById(R.id.rc_skin);
        llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        skinListAdapter = new SkinListAdapter(getApplicationContext());
        skinListAdapter.setSkinInfos(skinManager.getSkinInfos());
        skinListAdapter.setOnItemClickListener(new OnSkinItemClickedListener());
        rcSkin.setLayoutManager(llm);
        rcSkin.setAdapter(skinListAdapter);
        skinListAdapter.notifyDataSetChanged();
        skinManager.registerSkinStatusChangeListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        skinManager.unregisterSkinStatusChangeListener(this);
    }

    @Override
    public void onSkinLoadStart() {

    }

    @Override
    public void onSkinLoadFinish(ArrayList<SkinInfo> skinInfos) {
        skinListAdapter.setSkinInfos(skinInfos);
        skinListAdapter.notifyDataSetChanged();
        rcSkin.setItemSelectedByCurSkinInfo();
    }


    private class OnSkinItemClickedListener implements SkinListAdapter.OnItemClickListener {

        @Override
        public void onItemClicked(int position, SkinInfo info, View v) {
            skinManager.changeSkin(getApplicationContext(), getWindow().getDecorView(), skinizedAttrMap, info);
            rcSkin.setItemSelectedByCurSkinInfo();
        }

    }
}
