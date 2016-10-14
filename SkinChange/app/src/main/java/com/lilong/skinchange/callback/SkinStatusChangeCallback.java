package com.lilong.skinchange.callback;

import com.lilong.skinchange.utils.SkinInfo;

import java.util.ArrayList;

/**
 * callbacks when skin status changed,
 * usually after skin manager completes certain skin operations
 */

public interface SkinStatusChangeCallback {

    /**
     * called when skin manager starts to load skins
     */
    public void onSkinLoadStart();

    /**
     * called when skin manager finish loading skins
     *
     * @param skinInfos skinInfos of all loaded skin apks
     */
    public void onSkinLoadFinish(ArrayList<SkinInfo> skinInfos);
}
