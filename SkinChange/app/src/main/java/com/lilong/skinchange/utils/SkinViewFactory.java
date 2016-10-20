package com.lilong.skinchange.utils;

import com.lilong.skinchange.bean.SkinizedAttributeEntry;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * intercept activity content view's inflating process
 * when parsing layout xml, get each view's skinizable attributes and store them for future skin change
 */

public class SkinViewFactory implements LayoutInflater.Factory {

    private static final String TAG = "SkinViewFactory";

    private LayoutInflater.Factory defaultFactory;
    private LayoutInflater skinInflater;
    private HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap;

    public SkinViewFactory(LayoutInflater skinInflater, LayoutInflater.Factory defaultFactory, HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap) {
        this.skinInflater = skinInflater;
        this.defaultFactory = defaultFactory;
        this.skinizedAttrMap = skinizedAttrMap;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {

        View v = null;

        if (defaultFactory != null) {
            v = defaultFactory.onCreateView(name, context, attrs);
        }

        try {

            if (v == null) {

                String fullClassName = SkinUtil.getFullClassNameFromXmlTag(context, name, attrs);
                Log.d(TAG, "fullClassName = " + fullClassName);

                v = skinInflater.createView(fullClassName, null, attrs);
            }

            Log.d(TAG, v.getClass().getSimpleName() + "@" + v.hashCode());

            ArrayList<SkinizedAttributeEntry> list = SkinUtil.generateSkinizedAttributeEntry(context, v, attrs);
            for (SkinizedAttributeEntry entry : list) {

                Log.d(TAG, entry.getViewAttrName() + " = @" + entry.getResourceTypeName() + "/" + entry.getResourceEntryName());

                // use attribute type and entry name as key, to identify a skinizable attribute
                String key = entry.getResourceTypeName() + "/" + entry.getResourceEntryName();
                if (skinizedAttrMap.containsKey(key)) {
                    skinizedAttrMap.get(key).add(entry);
                } else {
                    ArrayList<SkinizedAttributeEntry> l = new ArrayList<SkinizedAttributeEntry>();
                    l.add(entry);
                    skinizedAttrMap.put(key, l);
                }
            }

        } catch (ClassNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return v;
    }

}
