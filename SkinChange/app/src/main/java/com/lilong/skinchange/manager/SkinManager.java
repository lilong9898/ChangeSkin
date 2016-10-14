package com.lilong.skinchange.manager;

import com.lilong.skinchange.bean.SkinizedAttributeEntry;
import com.lilong.skinchange.callback.SkinStatusChangeCallback;
import com.lilong.skinchange.utils.SkinInfo;
import com.lilong.skinchange.utils.SkinUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * high-level skin manipulation
 * singleton manager
 */

public class SkinManager {

    private static final String TAG = "skinManager";
    private static final String SP_NAME = "skinManager";
    private static volatile SkinManager sInstance;

    /**
     * skin info is not found in skinInfos list, return this index as invalid indicator
     */
    public static final int SKIN_INFO_INDEX_INVALID = -1;

    /**
     * dir path where skin apks are stored
     */
    private static String SKIN_PACKAGES_DIR_PATH = null;

    /**
     * name patterns with which certain apks will be regarded as skin apks
     */
    private static final String SKIN_APK_NAME_PATTERN = "skin_.*\\.apk";

    /**
     * skin infos from last load
     */
    private ArrayList<SkinInfo> skinInfos;

    /**
     * skin status change listeners
     */
    private ArrayList<SkinStatusChangeCallback> skinStatusChangeListeners;

    /**
     * skin info of current skin
     */
    private SkinInfo curSkinInfo;
    private SharedPreferences sp;

    private SkinManager(Context context) {

        SKIN_PACKAGES_DIR_PATH = context.getExternalFilesDir(null).getAbsolutePath();
        Log.i(TAG, "skin_packages_dir_path = " + SKIN_PACKAGES_DIR_PATH);

        skinInfos = new ArrayList<SkinInfo>();
        skinStatusChangeListeners = new ArrayList<SkinStatusChangeCallback>();

        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static SkinManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SkinManager.class) {
                if (sInstance == null) {
                    sInstance = new SkinManager(context);
                }
            }
        }
        return sInstance;
    }

    public static void destroyInstance() {
        sInstance.unregisterAllSkinStatusChangeListeners();
        sInstance.clearAllSkinInfos();
        sInstance = null;
    }

    public void setCurSkinInfo(SkinInfo info) {
        curSkinInfo = info;
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SkinInfo.SP_TAG_SKIN_ID, info.getSkinId());
        editor.putString(SkinInfo.SP_TAG_SKIN_NAME, info.getSkinName());
        editor.putString(SkinInfo.SP_TAG_SKIN_DESCRIPTION, info.getSkinDescription());
        editor.putString(SkinInfo.SP_TAG_SKIN_APK_PATH, info.getSkinApkPath());
        editor.commit();
    }

    public SkinInfo getCurSkinInfo() {
        if (curSkinInfo == null) {
            int skinId = sp.getInt(SkinInfo.SP_TAG_SKIN_ID, SkinInfo.SKIN_ID_INVALID);
            String skinName = sp.getString(SkinInfo.SP_TAG_SKIN_NAME, "");
            String skinDescription = sp.getString(SkinInfo.SP_TAG_SKIN_DESCRIPTION, "");
            String skinApkPath = sp.getString(SkinInfo.SP_TAG_SKIN_APK_PATH, "");
            curSkinInfo = new SkinInfo(skinId, skinName, skinDescription, skinApkPath);
        }
        return curSkinInfo;
    }

    /**
     * find index of a certain skinInfo in skinInfo's list of this manager
     */
    public int getSkinInfoIndex(SkinInfo info) {

        for (int i = 0; i < skinInfos.size(); i++) {
            if (info.getSkinId() == skinInfos.get(i).getSkinId()) {
                return i;
            }
        }

        return SKIN_INFO_INDEX_INVALID;
    }

    /**
     * change skin using a specified skin apk
     * this apk can be a skin apk, OR this app itself(restore to default skin)
     *
     * @param rootView        rootView of android activity/fragment who is using skin change feature
     * @param skinizedAttrMap hashmap
     *                        key is a skinized attribute identifier, formed as "resource typename/resource entryname"
     *                        value is a list, contains all views that have this kind of skinized attribute
     *                        each ownership relation is a skinizedAttributeEntry
     * @param info            skinInfo which contains the target skin's information
     */
    public void changeSkin(Context context, View rootView, HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap, SkinInfo info) {
        SkinUtil.changeSkin(context, rootView, skinizedAttrMap, info);
        setCurSkinInfo(info);
    }

    public void registerSkinStatusChangeListener(SkinStatusChangeCallback callback) {
        skinStatusChangeListeners.add(callback);
    }

    public void unregisterSkinStatusChangeListener(SkinStatusChangeCallback callback) {
        skinStatusChangeListeners.remove(callback);
    }

    private void unregisterAllSkinStatusChangeListeners() {
        skinStatusChangeListeners.clear();
    }

    private void clearAllSkinInfos() {
        skinInfos.clear();
    }

    public ArrayList<SkinInfo> getSkinInfos() {
        return skinInfos;
    }

    public void loadSkinApksFromAsset(Context context) {
        new LoadSkinApksFromAssetTask(context, SKIN_APK_NAME_PATTERN).execute();
    }

    class LoadSkinApksFromAssetTask extends AsyncTask {

        private Context context;
        private String skinApkNamePattern;

        public LoadSkinApksFromAssetTask(Context context, String skinApkNamePattern) {
            this.context = context;
            this.skinApkNamePattern = skinApkNamePattern;
        }

        @Override
        protected void onPreExecute() {
            for (SkinStatusChangeCallback callback : skinStatusChangeListeners) {
                callback.onSkinLoadStart();
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {

            ArrayList<String> list = new ArrayList<String>();
            Pattern p = Pattern.compile(skinApkNamePattern);

            // search all skin apks in assets folder
            SkinUtil.searchAssetFilesDFS(context.getAssets(), "", p, list);

            // copy them to designated path and parse their skin infos
            for (String fromPath : list) {
                Matcher m = p.matcher(fromPath);
                if (m.find()) {

                    String destPath = SKIN_PACKAGES_DIR_PATH + File.separator + m.group();
                    boolean isCopySuccessful = SkinUtil.copyAssetFileToPath(context.getAssets(), fromPath, destPath);

                    if (isCopySuccessful) {
                        SkinInfo info = SkinUtil.getSkinInfoOfSkinApk(context, destPath);
                        if (info != null && info.isValid()) {
                            skinInfos.add(info);
                        }
                    }
                }
            }

            // parse skin info of this app
            SkinInfo info = SkinUtil.getSkinInfoOfThisApp(context);
            if (info != null && info.isValid()) {
                skinInfos.add(info);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            for (SkinStatusChangeCallback callback : skinStatusChangeListeners) {
                callback.onSkinLoadFinish(skinInfos);
            }
            Log.i(TAG, skinInfos.toString());
        }
    }
}
