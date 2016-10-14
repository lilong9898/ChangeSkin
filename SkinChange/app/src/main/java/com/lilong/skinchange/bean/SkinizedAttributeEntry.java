package com.lilong.skinchange.bean;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * represents an attribute value viewRef of a view, whose referencing value might be changed by skin change
 * all views that hold any attribute with this value viewRef, need to reconfigure when the skin changes
 */

public class SkinizedAttributeEntry {

    private String resourceTypeName;
    private String resourceEntryName;
    private int viewId;
    private WeakReference<View> viewRef;
    private String viewAttrName;

    public SkinizedAttributeEntry(String resourceTypeName, String resourceEntryName, int viewId, WeakReference<View> viewRef, String viewAttrName) {
        this.resourceTypeName = resourceTypeName;
        this.resourceEntryName = resourceEntryName;
        this.viewId = viewId;
        this.viewRef = viewRef;
        this.viewAttrName = viewAttrName;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public String getResourceEntryName() {
        return resourceEntryName;
    }

    public int getViewId() {
        return viewId;
    }

    public WeakReference<View> getViewRef() {
        return viewRef;
    }

    public String getViewAttrName() {
        return viewAttrName;
    }
}
