package com.lilong.skinchange.utils;

import android.text.TextUtils;

/**
 * skin info read from corresponding skin apk's manifest
 */

public class SkinInfo {

    public static final String META_DATA_NAME_SKIN_ID = "skin_id";
    public static final String META_DATA_NAME_SKIN_NAME = "skin_name";
    public static final String META_DATA_NAME_SKIN_DESCRIPTION = "skin_description";

    public static final String SP_TAG_SKIN_ID = "skin_id";
    public static final String SP_TAG_SKIN_NAME = "skin_name";
    public static final String SP_TAG_SKIN_DESCRIPTION = "skin_description";
    public static final String SP_TAG_SKIN_APK_PATH = "skin_apk_path";

    public static final int SKIN_ID_SELF = 0;
    public static final int SKIN_ID_INVALID = -1;
    public static final String SKIN_DESCRIPTION_SELF = "skin_description_self";

    /**
     * skin id, this app's default skin is 0, all skin apk's skin > 0
     */
    private int skinId;
    private String skinName;
    private String skinDescription;
    /**
     * skin apk absolute path, load skin apk from this path
     * this app's default skin has no skinApkPath, use SKIN_DESCRIPTION_SELF instead
     */
    private String skinApkPath;

    public SkinInfo(int skinId, String skinName, String skinDescription) {
        this(skinId, skinName, skinDescription, SKIN_DESCRIPTION_SELF);
    }

    public SkinInfo(int skinId, String skinName, String skinDescription, String skinApkPath) {
        this.skinId = skinId;
        this.skinName = skinName;
        this.skinDescription = skinDescription;
        this.skinApkPath = skinApkPath;
    }

    public int getSkinId() {
        return skinId;
    }

    public String getSkinName() {
        return skinName;
    }

    public String getSkinDescription() {
        return skinDescription;
    }

    public String getSkinApkPath() {
        return skinApkPath;
    }

    public boolean isSelf() {
        return skinId == SKIN_ID_SELF;
    }

    public boolean isValid() {
        return skinId >= 0 && !TextUtils.isEmpty(skinName) && !TextUtils.isEmpty(skinDescription) && !TextUtils.isEmpty(skinApkPath);
    }

    @Override
    public String toString() {
        return "skin : [skinId = " + skinId + ", skinName = " + skinName + ", skinDescription = " + skinDescription + ", skinApkPath = " + skinApkPath + "]";
    }
}
