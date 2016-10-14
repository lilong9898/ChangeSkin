package com.lilong.skinchange.demo;

import com.lilong.skinchange.bean.ResourceEntry;
import com.lilong.skinchange.bean.SkinizedAttributeEntry;
import com.lilong.skinchange.callback.SkinStatusChangeCallback;
import com.lilong.skinchange.manager.SkinManager;
import com.lilong.skinchange.utils.SkinInfo;
import com.lilong.skinchange.utils.SkinUtil;
import com.lilong.skinchange.utils.SkinViewFactory;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * skinizable activity should inherit this base class
 */

public abstract class BaseActivity extends Activity implements SkinStatusChangeCallback {

    private SkinManager skinManager;
    private HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        skinManager = SkinManager.getInstance(getApplicationContext());
        skinizedAttrMap = new HashMap<String, ArrayList<SkinizedAttributeEntry>>();

        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.setFactory(new SkinViewFactory(inflater) {

            @Override
            public void onSkinizedAttributeEntriesGenerated(ArrayList<SkinizedAttributeEntry> list) {
                for (SkinizedAttributeEntry entry : list) {

                    //    use attribute type and entry name as key, to identify a skinizable attribute
                    String key = entry.getResourceTypeName() + "/" + entry.getResourceEntryName();
                    if (skinizedAttrMap.containsKey(key)) {
                        skinizedAttrMap.get(key).add(entry);
                    } else {
                        ArrayList<SkinizedAttributeEntry> l = new ArrayList<SkinizedAttributeEntry>();
                        l.add(entry);
                        skinizedAttrMap.put(key, l);
                    }
                }
            }
        });

    }

    /**
     * change skin using a specified skin apk
     * this apk can be a skin apk, OR this app itself(restore to default skin)
     *
     * @param info skinInfo which contains the target skin's information
     */
    protected void changeSkin(SkinInfo info) {

        ArrayList<ResourceEntry> list = null;
        Resources resources = null;

        // restore to default skin
        if (info.isSelf()) {
            // parse R.java file of THIS APP's apk, get all attributes and their values(references) in it
            list = SkinUtil.getThisAppResourceEntries(getApplicationContext());
            // resources instance from this app
            resources = getResources();
        }
        // change skin according to skin apk
        else {
            // parse R.java file of skin apk, get all attributes and their values(references) in it
            list = SkinUtil.getSkinApkResourceEntries(getApplicationContext(), getClassLoader(), info.getSkinApkPath());
            // get Resources instance of skin apk
            resources = SkinUtil.getApkResources(getResources(), info.getSkinApkPath());
        }

        changeSkinByResourceEntries(list, resources);
        skinManager.setLastSkinInfo(info);
    }

    /**
     * change skin using a specified skin apk
     *
     * @param resourceEntries contains resource entries to match against app's skinized attributes
     * @param fromResources   matched resource entry will get actual resource value from this resources instance
     */
    private void changeSkinByResourceEntries(ArrayList<ResourceEntry> resourceEntries, Resources fromResources) {

        for (ResourceEntry entry : resourceEntries) {

            String key = entry.getTypeName() + "/" + entry.getEntryName();

            if (skinizedAttrMap.containsKey(key)) {
                ArrayList<SkinizedAttributeEntry> l = skinizedAttrMap.get(key);
                for (SkinizedAttributeEntry e : l) {

                    View v = e.getViewRef().get();
                    if (v == null) {
                        v = findViewById(e.getViewId());
                    }
                    if (v == null) {
                        continue;
                    }

                    SkinUtil.applySkinizedAttribute(v, e.getViewAttrName(), fromResources, entry.getResId());
                }
            }
        }
    }
}
