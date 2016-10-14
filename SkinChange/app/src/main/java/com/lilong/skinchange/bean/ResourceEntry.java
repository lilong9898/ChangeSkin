package com.lilong.skinchange.bean;

/**
 * A fully qualified resource name is of the form "package:type/entry".
 */
public class ResourceEntry {

    /**
     * invalid resource resId is represented by 0
     */
    public static final int INVALID_RESOURCE_ID = 0;

    /**
     * package part of the resource name
     */
    private String packageName = "";
    /**
     * type part of the resource name
     */
    private String typeName = "";
    /**
     * entry part of the resource name
     */
    private String entryName = "";
    /**
     * full resource name, in the form of "package:type/entry"
     */
    private String fullName = "";
    /**
     * resource id, int, the same in R.java
     */
    private int resId = 0;

    public ResourceEntry(String packageName, String typeName, String entryName, int resId) {
        this.packageName = packageName;
        this.typeName = typeName;
        this.entryName = entryName;
        this.fullName = packageName + ":" + typeName + "/" + entryName;
        this.resId = resId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getEntryName() {
        return entryName;
    }

    public int getResId() {
        return resId;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return "ResourceEntry[resId : " +
                Integer.toHexString(resId) + ", full name : " + fullName + "]";
    }
}
