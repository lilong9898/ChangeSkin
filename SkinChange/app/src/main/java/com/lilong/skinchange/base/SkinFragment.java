package com.lilong.skinchange.base;

import com.lilong.skinchange.callback.SkinStatusChangeCallback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * skinizable fragment should inherit this base class
 */

public abstract class SkinFragment extends Fragment implements SkinStatusChangeCallback {

    /**
     * LayoutInflater used for inflating skin support ui
     */
    private LayoutInflater skinLayoutInflater;

    public void setSkinLayoutInflater(LayoutInflater skinLayoutInflater) {
        this.skinLayoutInflater = skinLayoutInflater;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return onCreateViewSkin(false ? inflater : skinLayoutInflater, container, savedInstanceState);
    }

    public abstract View onCreateViewSkin(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);
}
