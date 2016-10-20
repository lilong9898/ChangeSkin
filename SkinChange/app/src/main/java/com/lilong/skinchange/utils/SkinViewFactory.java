package com.lilong.skinchange.utils;

import com.lilong.skinchange.bean.SkinizedAttributeEntry;
import com.lilong.skinchange.manager.SkinManager;

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

    private SkinManager skinManager;
    /**
     * factory of system LayoutInflater, if not null, execute code of this default factory first
     * see if it returns a non-null view
     * this is for android support lib, e.g. FragmentActivity, who set its own factory in onCreate()
     */
    private LayoutInflater.Factory defaultFactory;
    private LayoutInflater skinInflater;

    /**
     * skinized attr map of this factory's inflater's enclosing activity
     */
    private HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMapGlobal;

    /**
     * a temporary skinizedAttrMap for immediate skin change when completing inflating this view
     */
    private HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMapThisView;

    public SkinViewFactory(LayoutInflater skinInflater, LayoutInflater.Factory defaultFactory, HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap) {
        this.skinManager = SkinManager.getInstance(skinInflater.getContext());
        this.skinInflater = skinInflater;
        this.defaultFactory = defaultFactory;
        this.skinizedAttrMapGlobal = skinizedAttrMap;
        this.skinizedAttrMapThisView = new HashMap<String, ArrayList<SkinizedAttributeEntry>>();
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

                skinizedAttrMapThisView.clear();
                if (skinizedAttrMapThisView.containsKey(key)) {
                    skinizedAttrMapThisView.get(key).add(entry);
                } else {
                    ArrayList<SkinizedAttributeEntry> l = new ArrayList<SkinizedAttributeEntry>();
                    l.add(entry);
                    skinizedAttrMapThisView.put(key, l);
                }

                // immediate skin change of this view
                SkinUtil.changeSkin(skinInflater.getContext(), v, skinizedAttrMapThisView, skinManager.getCurSkinInfo());

                // meanwhile add these skinized attr entries to the global map for future skin change
                if (skinizedAttrMapGlobal.containsKey(key)) {
                    skinizedAttrMapGlobal.get(key).add(entry);
                } else {
                    ArrayList<SkinizedAttributeEntry> l = new ArrayList<SkinizedAttributeEntry>();
                    l.add(entry);
                    skinizedAttrMapGlobal.put(key, l);
                }
            }

        } catch (ClassNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return v;
    }

}
