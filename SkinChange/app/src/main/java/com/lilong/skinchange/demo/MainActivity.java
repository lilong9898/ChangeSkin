package com.lilong.skinchange.demo;

import com.lilong.skinchange.R;
import com.lilong.skinchange.manager.SkinManager;
import com.lilong.skinchange.utils.SkinInfo;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private SkinManager skinManager;
    private RecyclerView rcSkin;
    private LinearLayoutManager llm;
    private SkinListAdapter skinListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        skinManager = SkinManager.getInstance(getApplicationContext());
        SkinInfo lastSkinInfo = skinManager.getLastSkinInfo();
        changeSkin(lastSkinInfo);

        rcSkin = (RecyclerView) findViewById(R.id.rc_skin);
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
    }

    private class OnSkinItemClickedListener implements SkinListAdapter.OnItemClickListener {

        @Override
        public void onItemClicked(int position, SkinInfo info, View v) {

            for (int i = 0; i < rcSkin.getChildCount(); i++) {
                View child = rcSkin.getChildAt(i);
                child.setSelected(false);
            }
            View selectedChild = rcSkin.getChildAt(position);
            selectedChild.setSelected(true);
            rcSkin.scrollToPosition(position);

            changeSkin(info);
        }

    }
}
