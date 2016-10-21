package com.lilong.skinchange.other;

import com.lilong.skinchange.demo.DemoFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;

import java.util.ArrayList;

public class SkinTestFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int FRAGMENT_COUNT = 4;

    private ArrayList<DemoFragment> fragmentList;

    public SkinTestFragmentPagerAdapter(FragmentManager fm, LayoutInflater skinLayoutInflater) {

        super(fm);

        fragmentList = new ArrayList<DemoFragment>();

        for (int i = 0; i < FRAGMENT_COUNT; i++) {
            DemoFragment f = new DemoFragment();
            Bundle bundle = new Bundle();
            bundle.putString(DemoFragment.ARGUMENT_TAG_ID, "" + i);
            f.setArguments(bundle);
            fragmentList.add(f);
        }
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_UNCHANGED;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

}
