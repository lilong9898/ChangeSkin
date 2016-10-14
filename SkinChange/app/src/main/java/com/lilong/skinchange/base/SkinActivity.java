package com.lilong.skinchange.base;

import com.lilong.skinchange.bean.SkinizedAttributeEntry;
import com.lilong.skinchange.callback.SkinStatusChangeCallback;
import com.lilong.skinchange.utils.SkinViewFactory;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * skinizable activity should inherit this base class
 */

public abstract class SkinActivity extends Activity implements SkinStatusChangeCallback {

    protected HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        skinizedAttrMap = new HashMap<String, ArrayList<SkinizedAttributeEntry>>();

        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.setFactory(new SkinViewFactory(inflater, skinizedAttrMap));

    }


}
