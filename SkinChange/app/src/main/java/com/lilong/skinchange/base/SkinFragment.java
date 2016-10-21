package com.lilong.skinchange.base;

import com.lilong.skinchange.callback.SkinStatusChangeCallback;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

/**
 * skinizable fragment should inherit this base class
 */

public abstract class SkinFragment extends Fragment implements SkinStatusChangeCallback {

    @Override
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {

        if (getActivity() instanceof SkinActivity == false) {
            return super.getLayoutInflater(savedInstanceState);
        }

        SkinActivity hostActivity = (SkinActivity) getActivity();
        return hostActivity.getLayoutInflater() == null ? super.getLayoutInflater(savedInstanceState) : hostActivity.getLayoutInflater();
    }

}
