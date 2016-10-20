package com.lilong.skinchange.base;

import com.lilong.skinchange.bean.SkinizedAttributeEntry;
import com.lilong.skinchange.callback.SkinStatusChangeCallback;
import com.lilong.skinchange.utils.SkinViewFactory;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * skinizable activity should inherit this base class
 */

public abstract class SkinActivity extends FragmentActivity implements SkinStatusChangeCallback {

    private static final String TAG = "SkinActivity";

    /**
     * hashmap
     * key is a skinized attribute identifier, formed as "resource typename/resource entryname"
     * value is a list, contains all views of this activity, that own this kind of skinized attribute
     * each ownership relation is a skinizedAttributeEntry
     */
    private HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap;

    protected HashMap<String, ArrayList<SkinizedAttributeEntry>> getSkinizedAttributeEntries() {
        return skinizedAttrMap;
    }

    /**
     * LayoutInflater cloned from system layoutInflater, to support skin feature
     */
    private LayoutInflater skinLayoutInflater;

    protected LayoutInflater getSkinLayoutInflater() {
        return skinLayoutInflater;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        skinizedAttrMap = new HashMap<String, ArrayList<SkinizedAttributeEntry>>();

        LayoutInflater systemInflater = LayoutInflater.from(this);
        skinLayoutInflater = systemInflater.cloneInContext(this);
        SkinViewFactory factory = new SkinViewFactory(skinLayoutInflater, systemInflater.getFactory(), skinizedAttrMap);
        skinLayoutInflater.setFactory(factory);
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(skinLayoutInflater.inflate(layoutResID, null));
    }
}
