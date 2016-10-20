package com.lilong.skinchange.utils;

import com.lilong.skinchange.R;
import com.lilong.skinchange.bean.ResourceEntry;
import com.lilong.skinchange.bean.SkinizedAttributeEntry;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import dalvik.system.DexClassLoader;

/**
 * low-level skin manipulation
 */

public class SkinUtil {

    private static final String TAG = "SkinUtil";

    /**
     * DFS search all files in assets folder
     * pick up files with certain name pattern and return their paths relative to the assets folder
     *
     * @param am          AssetManager used to open assets folder
     * @param root        root path to begin recursive search
     * @param namePattern regexp to filter files' name pattern
     * @param result      search results, element is the file's path (relative to assets folder)
     */
    public static void searchAssetFilesDFS(AssetManager am, String root, Pattern namePattern, ArrayList<String> result) {

        if (root != null && namePattern.matcher(root).find()) {
            result.add(root);
            return;
        }

        String[] assetFiles = null;

        try {
            assetFiles = am.list(root);
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (assetFiles.length == 0) {
            return;
        }

        for (String filePath : assetFiles) {
            if (root.equals("")) {
                searchAssetFilesDFS(am, filePath, namePattern, result);
            } else {
                searchAssetFilesDFS(am, root + File.separator + filePath, namePattern, result);
            }
        }

    }

    /**
     * copy a file under asset folder to a specified location
     *
     * @param assetFilePath file's path in the asset folder
     * @param destFilePath  file's destination folder, absolute path
     * @return is copy successful
     */
    public static boolean copyAssetFileToPath(AssetManager am, String assetFilePath, String destFilePath) {

        File skinDestF = new File(destFilePath);

        InputStream is = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            is = am.open(assetFilePath);
            bis = new BufferedInputStream(is);

            fos = new FileOutputStream(skinDestF);
            bos = new BufferedOutputStream(fos);

            byte[] buffer = new byte[1];
            while ((bis.read(buffer)) != -1) {
                bos.write(buffer);
            }

            bos.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return false;
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

    }

    /**
     * get all resource entries in THIS APP's apk
     *
     * @return a list of all the resource entries in app's apk
     */
    public static ArrayList<ResourceEntry> getThisAppResourceEntries(Context context) {

        ArrayList<ResourceEntry> list = new ArrayList<ResourceEntry>();

        try {

            // get all member classes of R.java, i.e. all resource types in this package
            Class[] memberClassArray = R.class.getClasses();
            for (Class c : memberClassArray) {
                // get all int type declared fields, i.e. all resource entries in this resource type
                for (Field entryField : c.getDeclaredFields()) {
                    if ("int".equals(entryField.getType().getSimpleName())) {
                        ResourceEntry e = new ResourceEntry(context.getPackageName(), c.getSimpleName(), entryField.getName(), entryField.getInt(null));
                        list.add(e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return list;
    }

    /**
     * use DexClassLoader to get all resource entries in a specified apk
     * dynamic load this apk, no need to install it
     * in this senario, "a specified apk" refers to the skin apk
     *
     * @param hostClassLoader main application's classloader
     * @param apkPath         absolute path of this specified apk
     * @return a list of all the resource entries in the specified apk
     */
    public static ArrayList<ResourceEntry> getSkinApkResourceEntries(Context context, ClassLoader hostClassLoader, String apkPath) {

        ArrayList<ResourceEntry> list = new ArrayList<ResourceEntry>();

        try {
            // odex path of the specified apk is main application's FILES dir
            DexClassLoader dexClassLoader = new DexClassLoader(apkPath, context.getFilesDir().getAbsolutePath(), null, hostClassLoader);
            String packageName = getPackageNameOfApk(context.getPackageManager(), apkPath);

            // get all member classes of R.java, i.e. all resource types in this package
            Class[] memberClassArray = loadMemberClasses(dexClassLoader, packageName + ".R");
            for (Class c : memberClassArray) {
                // get all int type declared fields, i.e. all resource entries in this resource type
                for (Field entryField : c.getDeclaredFields()) {
                    if ("int".equals(entryField.getType().getSimpleName())) {
                        ResourceEntry e = new ResourceEntry(packageName, c.getSimpleName(), entryField.getName(), entryField.getInt(null));
                        list.add(e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return list;
    }

    /**
     * get packageName in the manifest of an apk, whose path is specified
     * no need to install this apk
     *
     * @param apkPath absolute path of the apk
     * @return packageName in the manifest
     */
    public static String getPackageNameOfApk(PackageManager pm, String apkPath) {
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, 0);
        return info.packageName;
    }

    /**
     * parse meta-data in manifest, get skin name and description
     * OF THIS APP
     *
     * @return skin info bean which contains skin information of this skin apk
     */
    public static SkinInfo getSkinInfoOfThisApp(Context context) {

        PackageInfo info = null;

        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (info == null || info.applicationInfo == null || info.applicationInfo.metaData == null) {
            return null;
        }

        String skinName = info.applicationInfo.metaData.getString(SkinInfo.META_DATA_NAME_SKIN_NAME);
        String skinDescription = info.applicationInfo.metaData.getString(SkinInfo.META_DATA_NAME_SKIN_DESCRIPTION);
        return new SkinInfo(SkinInfo.SKIN_ID_SELF, skinName, skinDescription);
    }

    /**
     * parse meta-data in manifest, get skin name and description
     * if it's not a skin apk, return a skininfo with no info inside
     *
     * @param apkPath absolute path of the apk
     * @return skin info bean which contains skin information of this skin apk
     */
    public static SkinInfo getSkinInfoOfSkinApk(Context context, String apkPath) {

        PackageInfo info = context.getPackageManager().getPackageArchiveInfo(apkPath, PackageManager.GET_META_DATA);

        if (info == null || info.applicationInfo == null || info.applicationInfo.metaData == null) {
            return null;
        }

        int skinId = info.applicationInfo.metaData.getInt(SkinInfo.META_DATA_NAME_SKIN_ID);
        String skinName = info.applicationInfo.metaData.getString(SkinInfo.META_DATA_NAME_SKIN_NAME);
        String skinDescription = info.applicationInfo.metaData.getString(SkinInfo.META_DATA_NAME_SKIN_DESCRIPTION);
        return new SkinInfo(skinId, skinName, skinDescription, apkPath);
    }

    /**
     * get full name of corresponding view class, associated with certain layout xml tag
     *
     * @param tagName tagName extracted by inflater's xml parser, i.e. "RelativeLayout", "Button", etc.
     * @return full class name, i.e. "android.widget.RelativeLayout", "android.widget.Button"
     */
    public static String getFullClassNameFromXmlTag(Context context, String tagName, AttributeSet attrs) {

        String fullClassName = tagName;

        if (tagName.indexOf('.') != -1) {
            return tagName;
        } else if (tagName.startsWith("View")) {
            return "android.view." + tagName;
        } else {
            fullClassName = "android.widget." + tagName;
        }

        return fullClassName;
    }

    /**
     * intercept the layout inflater's inflating process, extract the view's identity and its attributes
     * use these information to generate a skinizedAttributeEntry list,
     * which contains all the attribute values of this view, that are referenced to user-defined resource
     *
     * @param v     the view which gets inflated
     * @param attrs attributeSet used to inflate this view
     * @return the list of all the skinized attribute entries of this view
     */
    public static ArrayList<SkinizedAttributeEntry> generateSkinizedAttributeEntry(Context context, View v, AttributeSet attrs) {

        ArrayList<SkinizedAttributeEntry> list = new ArrayList<SkinizedAttributeEntry>();

        if (v.getId() == -1) {
            return list;
        }

        for (int i = 0; i < attrs.getAttributeCount(); i++) {

            String attrName = attrs.getAttributeName(i);
            String attrValue = attrs.getAttributeValue(i);

            if (attrName.equals("id")) {
                continue;
            }

            if (attrValue.startsWith("@") == false) {
                continue;
            }

            int resId = attrs.getAttributeResourceValue(i, 0);
            String packageName = context.getResources().getResourcePackageName(resId);
            String typeName = context.getResources().getResourceTypeName(resId);
            String entryName = context.getResources().getResourceEntryName(resId);

            // if an attribute uses system resources, it's not skinizable
            if (packageName.startsWith("android:")) {
                continue;
            }

            // certain resource types are not skinizable
            if (typeName.equals("id") || typeName.equals("layout")) {
                continue;
            }


            Log.d(TAG, v.getClass().getSimpleName() + "@" + v.hashCode() + ", " + attrName + " = " + typeName + "/" + entryName);

            SkinizedAttributeEntry entry = new SkinizedAttributeEntry(typeName, entryName, v.getId(), new WeakReference<View>(v), attrName);
            list.add(entry);

        }

        return list;
    }

    /**
     * get Resources instance of a specified apk
     * this instance can be used to retrieve resource id/name/value of this apk
     *
     * @param hostResources main application's resources instance
     * @param apkPath       absolute path of the skin apk
     * @return Resources instance of the specified apk
     */
    public static Resources getApkResources(Resources hostResources, String apkPath) {

        try {
            AssetManager am = AssetManager.class.newInstance();
            Method methodAddAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            methodAddAssetPath.setAccessible(true);
            methodAddAssetPath.invoke(am, apkPath);
            Resources apkResources = new Resources(am, hostResources.getDisplayMetrics(), hostResources.getConfiguration());
            return apkResources;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    /**
     * reset view's attribute due to skin change
     *
     * @param v             view whose attribute is to be reset due to skin change
     * @param attributeName name of the attribute
     * @param skinResources Resources instance of the skin apk
     * @param skinResId     new attribute's value's resId within Resources instance of the skin apk
     */
    public static void applySkinizedAttribute(View v, String attributeName, Resources skinResources, int skinResId) {

        // android.view.View
        if ("layout_width".equals(attributeName)) {
            // only workable when layout_width attribute in xml is a precise dimen
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.width = (int) skinResources.getDimension(skinResId);
            v.setLayoutParams(lp);
        } else if ("layout_height".equals(attributeName)) {
            // only workable when layout_height attribute in xml is a precise dimen
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = (int) skinResources.getDimension(skinResId);
            v.setLayoutParams(lp);
        } else if ("background".equals(attributeName)) {
            Drawable backgroundDrawable = skinResources.getDrawable(skinResId);
            v.setBackgroundDrawable(backgroundDrawable);
        } else if ("alpha".equals(attributeName)) {
            float alpha = skinResources.getFraction(skinResId, 1, 1);
            v.setAlpha(alpha);
        } else if ("padding".equals(attributeName)) {
            int padding = (int) skinResources.getDimension(skinResId);
            v.setPadding(padding, padding, padding, padding);
        } else if ("paddingLeft".equals(attributeName)) {
            int paddingLeft = (int) skinResources.getDimension(skinResId);
            v.setPadding(paddingLeft, v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
        } else if ("paddingTop".equals(attributeName)) {
            int paddingTop = (int) skinResources.getDimension(skinResId);
            v.setPadding(v.getPaddingLeft(), paddingTop, v.getPaddingRight(), v.getPaddingBottom());
        } else if ("paddingRight".equals(attributeName)) {
            int paddingRight = (int) skinResources.getDimension(skinResId);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), paddingRight, v.getPaddingBottom());
        } else if ("paddingBottom".equals(attributeName)) {
            int paddingBottom = (int) skinResources.getDimension(skinResId);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingLeft(), paddingBottom);
        } else if ("scrollX".equals(attributeName)) {
            int scrollX = (int) skinResources.getDimension(skinResId);
            v.setScrollX(scrollX);
        } else if ("scrollY".equals(attributeName)) {
            int scrollY = (int) skinResources.getDimension(skinResId);
            v.setScrollY(scrollY);
        } else if ("transformPivotX".equals(attributeName)) {
            float transformPivotX = skinResources.getDimension(skinResId);
            v.setPivotX(transformPivotX);
        } else if ("transformPivotY".equals(attributeName)) {
            float transformPivotY = skinResources.getDimension(skinResId);
            v.setPivotY(transformPivotY);
        } else if ("translationX".equals(attributeName)) {
            float translationX = skinResources.getDimension(skinResId);
            v.setTranslationX(translationX);
        } else if ("translationY".equals(attributeName)) {
            float translationY = skinResources.getDimension(skinResId);
            v.setTranslationY(translationY);
        } else if ("rotation".equals(attributeName)) {
            float rotation = skinResources.getDimension(skinResId);
            v.setRotation(rotation);
        } else if ("rotationX".equals(attributeName)) {
            float rotationX = skinResources.getDimension(skinResId);
            v.setRotationX(rotationX);
        } else if ("rotationY".equals(attributeName)) {
            float rotationY = skinResources.getDimension(skinResId);
            v.setRotationY(rotationY);
        }
        // android.widget.TextView
        else if (v instanceof TextView) {

            TextView tv = (TextView) v;

            if ("text".equals(attributeName)) {
                String text = skinResources.getString(skinResId);
                tv.setText(text);
            } else if ("hint".equals(attributeName)) {
                String hint = skinResources.getString(skinResId);
                tv.setHint(hint);
            } else if ("textColor".equals(attributeName)) {
                tv.setTextColor(skinResources.getColorStateList(skinResId));
            } else if ("textColorHighlight".equals(attributeName)) {
                tv.setHighlightColor(skinResources.getColor(skinResId));
            } else if ("textColorHint".equals(attributeName)) {
                tv.setHintTextColor(skinResources.getColorStateList(skinResId));
            } else if ("textColorLink".equals(attributeName)) {
                tv.setLinkTextColor(skinResources.getColorStateList(skinResId));
            } else if ("textSize".equals(attributeName)) {
                tv.setTextSize(skinResources.getDimension(skinResId));
            } else if ("drawableLeft".equals(attributeName)) {
                Drawable drawableLeft = skinResources.getDrawable(skinResId);
                Drawable[] drawablesOriginal = tv.getCompoundDrawables();
                tv.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawablesOriginal[1], drawablesOriginal[2], drawablesOriginal[3]);
            } else if ("drawableTop".equals(attributeName)) {
                Drawable drawableTop = skinResources.getDrawable(skinResId);
                Drawable[] drawablesOriginal = tv.getCompoundDrawables();
                tv.setCompoundDrawablesWithIntrinsicBounds(drawablesOriginal[0], drawableTop, drawablesOriginal[2], drawablesOriginal[3]);
            } else if ("drawableRight".equals(attributeName)) {
                Drawable drawableRight = skinResources.getDrawable(skinResId);
                Drawable[] drawablesOriginal = tv.getCompoundDrawables();
                tv.setCompoundDrawablesWithIntrinsicBounds(drawablesOriginal[0], drawablesOriginal[1], drawableRight, drawablesOriginal[3]);
            } else if ("drawableBottom".equals(attributeName)) {
                Drawable drawableBottom = skinResources.getDrawable(skinResId);
                Drawable[] drawablesOriginal = tv.getCompoundDrawables();
                tv.setCompoundDrawablesWithIntrinsicBounds(drawablesOriginal[0], drawablesOriginal[1], drawablesOriginal[2], drawableBottom);
            } else if ("drawablePadding".equals(attributeName)) {
                int drawablePadding = (int) skinResources.getDimension(skinResId);
                tv.setCompoundDrawablePadding(drawablePadding);
            } else if ("cursorVisible".equals(attributeName)) {
                boolean cursorVisible = skinResources.getBoolean(skinResId);
                tv.setCursorVisible(cursorVisible);
            } else if ("textCursorDrawable".equals(attributeName)) {
                Drawable textCursorDrawable = skinResources.getDrawable(skinResId);
                Drawable[] mCursorDrawable = new Drawable[]{textCursorDrawable, textCursorDrawable};
                setFieldValue(tv.getClass(), "mCursorDrawable", tv, mCursorDrawable);
                tv.invalidate();
            } else if ("lines".equals(attributeName)) {
                int lines = skinResources.getInteger(skinResId);
                tv.setLines(lines);
            } else if ("minLines".equals(attributeName)) {
                int minLines = skinResources.getInteger(skinResId);
                tv.setMinLines(minLines);
            } else if ("maxLines".equals(attributeName)) {
                int maxLines = skinResources.getInteger(skinResId);
                tv.setMaxLines(maxLines);
            } else if ("width".equals(attributeName)) {
                int width = (int) skinResources.getDimension(skinResId);
                tv.setWidth(width);
            } else if ("minWidth".equals(attributeName)) {
                int minWidth = (int) skinResources.getDimension(skinResId);
                tv.setMinWidth(minWidth);
            } else if ("maxWidth".equals(attributeName)) {
                int maxWidth = (int) skinResources.getDimension(skinResId);
                tv.setMaxWidth(maxWidth);
            } else if ("height".equals(attributeName)) {
                int height = (int) skinResources.getDimension(skinResId);
                tv.setMaxHeight(height);
            } else if ("minHeight".equals(attributeName)) {
                int minHeight = (int) skinResources.getDimension(skinResId);
                tv.setMinHeight(minHeight);
            } else if ("maxHeight".equals(attributeName)) {
                int maxHeight = (int) skinResources.getDimension(skinResId);
                tv.setMaxHeight(maxHeight);
            } else if ("ems".equals(attributeName)) {
                int ems = skinResources.getInteger(skinResId);
                tv.setEms(ems);
            } else if ("minEms".equals(attributeName)) {
                int minEms = skinResources.getInteger(skinResId);
                tv.setMinEms(minEms);
            } else if ("maxEms".equals(attributeName)) {
                int maxEms = skinResources.getInteger(skinResId);
                tv.setMaxEms(maxEms);
            } else if ("gravity".equals(attributeName)) {
                int gravity = skinResources.getInteger(skinResId);
                tv.setGravity(gravity);
            } else if ("scrollHorizontally".equals(attributeName)) {
                boolean scrollHorizontally = skinResources.getBoolean(skinResId);
                tv.setHorizontallyScrolling(scrollHorizontally);
            } else if ("freezesText".equals(attributeName)) {
                boolean freezesText = skinResources.getBoolean(skinResId);
                tv.setFreezesText(freezesText);
            } else if (attributeName.startsWith("shadow")) {
                TextPaint paint = (TextPaint) getFieldValue(tv.getClass(), "mTextPaint", tv);
                float shadowRadius = (Float) getFieldValue(paint.getClass(), "shadowRadius", paint);
                float shadowDx = (Float) getFieldValue(paint.getClass(), "shadowDx", paint);
                float shadowDy = (Float) getFieldValue(paint.getClass(), "shadowDy", paint);
                int shadowColor = (Integer) getFieldValue(paint.getClass(), "shadowColor", paint);
                if ("shadowRadius".equals(attributeName)) {
                    tv.setShadowLayer(skinResources.getFraction(skinResId, 1, 1), shadowDx, shadowDy, shadowColor);
                } else if ("shadowDx".equals(attributeName)) {
                    tv.setShadowLayer(shadowRadius, skinResources.getFraction(skinResId, 1, 1), shadowDy, shadowColor);
                } else if ("shadowDy".equals(attributeName)) {
                    tv.setShadowLayer(shadowRadius, shadowDx, skinResources.getFraction(skinResId, 1, 1), shadowColor);
                } else if ("shadowColor".equals(attributeName)) {
                    tv.setShadowLayer(shadowRadius, shadowDx, shadowDy, skinResources.getInteger(skinResId));
                }
            } else if (attributeName.startsWith("lineSpacing")) {
                float lineSpacingExtra = (Float) getFieldValue(tv.getClass(), "mSpacingAdd", tv);
                float lineSpacingMultiplier = (Float) getFieldValue(tv.getClass(), "mSpacingMult", tv);
                if ("lineSpacingExtra".equals(attributeName)) {
                    tv.setLineSpacing(skinResources.getDimension(skinResId), lineSpacingMultiplier);
                } else if ("lineSpacingMultiplier".equals(attributeName)) {
                    tv.setLineSpacing(lineSpacingExtra, skinResources.getFraction(skinResId, 1, 1));
                }

            }
        }
        // android.widget.ImageView
        else if (v instanceof ImageView) {

            ImageView iv = (ImageView) v;

            if ("src".equals(attributeName)) {
                Drawable srcDrawable = skinResources.getDrawable(skinResId);
                iv.setImageDrawable(srcDrawable);
            } else if ("baselineAlignBottom".equals(attributeName)) {
                boolean baselineAlignBottom = skinResources.getBoolean(skinResId);
                iv.setBaselineAlignBottom(baselineAlignBottom);
            } else if ("baseline".equals(attributeName)) {
                int baseline = (int) skinResources.getDimension(skinResId);
                iv.setBaseline(baseline);
            } else if ("adjustViewBounds".equals(attributeName)) {
                boolean adjustViewBounds = skinResources.getBoolean(skinResId);
                iv.setAdjustViewBounds(adjustViewBounds);
            } else if ("maxWidth".equals(attributeName)) {
                int maxWidth = (int) skinResources.getDimension(skinResId);
                iv.setMaxWidth(maxWidth);
                iv.requestLayout();
                iv.invalidate();
            } else if ("maxHeight".equals(attributeName)) {
                int maxHeight = (int) skinResources.getDimension(skinResId);
                iv.setMaxHeight(maxHeight);
                iv.requestLayout();
                iv.invalidate();
            } else if ("scaleType".equals(attributeName)) {
                int scaleType = skinResources.getInteger(skinResId);
                ImageView.ScaleType[] scaleTypeArray = (ImageView.ScaleType[]) getFieldValue(iv.getClass(), "sScaleTypeArray", iv);
                iv.setScaleType(scaleTypeArray[scaleType]);
            } else if ("tint".equals(attributeName)) {
                int tint = skinResources.getInteger(skinResId);
                iv.setColorFilter(tint);
            } else if ("cropToPadding".equals(attributeName)) {
                boolean cropToPadding = skinResources.getBoolean(skinResId);
                setFieldValue(iv.getClass(), "cropToPadding", iv, cropToPadding);
            }
        }
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
    public static void changeSkin(Context context, View rootView, HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap, SkinInfo info) {

        ArrayList<ResourceEntry> list = null;
        Resources resources = null;

        // restore to default skin
        if (info.isSelf()) {
            // parse R.java file of THIS APP's apk, get all attributes and their values(references) in it
            list = SkinUtil.getThisAppResourceEntries(context);
            // resources instance from this app
            resources = context.getResources();
        }
        // change skin according to skin apk
        else {
            // parse R.java file of skin apk, get all attributes and their values(references) in it
            list = SkinUtil.getSkinApkResourceEntries(context, context.getClassLoader(), info.getSkinApkPath());
            // get Resources instance of skin apk
            resources = SkinUtil.getApkResources(context.getResources(), info.getSkinApkPath());
        }

        changeSkinByResourceEntries(rootView, skinizedAttrMap, list, resources);
    }

    /**
     * change skin using a specified skin apk
     *
     * @param rootView        rootView of android activity/fragment who is using skin change feature
     * @param skinizedAttrMap hashmap
     *                        key is a skinized attribute identifier, formed as "resource typename/resource entryname"
     *                        value is a list, contains all views that have this kind of skinized attribute
     *                        each ownership relation is a skinizedAttributeEntry
     * @param resourceEntries contains resource entries which are used to match against app's skinized attributes
     * @param fromResources   matched resource entry will get actual resource value from this resources instance
     */
    public static void changeSkinByResourceEntries(View rootView, HashMap<String, ArrayList<SkinizedAttributeEntry>> skinizedAttrMap, ArrayList<ResourceEntry> resourceEntries, Resources fromResources) {

        for (ResourceEntry entry : resourceEntries) {

            String key = entry.getTypeName() + "/" + entry.getEntryName();

            if (skinizedAttrMap.containsKey(key)) {
                ArrayList<SkinizedAttributeEntry> l = skinizedAttrMap.get(key);
                for (SkinizedAttributeEntry e : l) {

                    View v = e.getViewRef().get();
                    if (v == null) {
                        v = rootView.findViewById(e.getViewId());
                    }
                    if (v == null) {
                        continue;
                    }

                    SkinUtil.applySkinizedAttribute(v, e.getViewAttrName(), fromResources, entry.getResId());
                }
            }
        }
    }

    public static Class loadClass(ClassLoader classLoader, String className) {

        Class c = null;

        try {
            c = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return c;
    }

    public static Class loadMemberClass(ClassLoader classLoader, String enclosingClassName, String memberClassName) {

        Class c = null;

        try {
            Class[] memberClasses = classLoader.loadClass(enclosingClassName).getClasses();
            for (Class memberClass : memberClasses) {
                if (memberClass.getSimpleName().equals(memberClassName)) {
                    c = memberClass;
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return c;
    }

    public static Class[] loadMemberClasses(ClassLoader classLoader, String enclosingClassName) {

        Class[] c = null;

        try {
            c = classLoader.loadClass(enclosingClassName).getClasses();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return c;
    }

    public static Field getField(Class clazz, String fieldName) {

        Field field = null;

        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (field != null) {
            return field;
        }

        try {
            field = clazz.getField(fieldName);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return field;
    }

    public static Method getMethod(Class clazz, String methodName, Class[] argsClasses) {

        Method method = null;

        try {
            method = clazz.getDeclaredMethod(methodName, argsClasses);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (method != null) {
            return method;
        }

        try {
            method = clazz.getMethod(methodName, argsClasses);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return method;
    }

    public static Object getFieldValue(Class clazz, String fieldName) {
        return getFieldValue(clazz, fieldName, null);
    }

    public static Object getFieldValue(Class clazz, String fieldName, Object o) {

        Object value = null;
        Field field = getField(clazz, fieldName);

        if (field == null) {
            return value;
        }

        field.setAccessible(true);

        int modifier = field.getModifiers();
        try {
            if (Modifier.isStatic(modifier)) {
                value = field.get(null);
            } else {
                value = field.get(o);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        field.setAccessible(false);

        return value;
    }

    public static void setFieldValue(Class clazz, String fieldName, Object value) {
        setFieldValue(clazz, fieldName, null, value);
    }

    public static void setFieldValue(Class clazz, String fieldName, Object o, Object value) {

        Field field = getField(clazz, fieldName);

        if (field == null) {
            return;
        }

        try {
            field.setAccessible(true);
            field.set(o, value);
            field.setAccessible(false);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static void invokeMethod(Class c, String methodName, Object[] args) {
        invokeMethod(c, methodName, args, null);
    }

    public static void invokeMethod(Class c, String methodName, Object o) {
        invokeMethod(c, methodName, new Object[]{}, o);
    }

    public static void invokeMethod(Class c, String methodName) {
        invokeMethod(c, methodName, new Object[]{}, null);
    }

    public static void invokeMethod(Class c, String methodName, Object[] args, Object o) {

        Class[] argsClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argsClasses[i] = args[i].getClass();
        }

        Method method = getMethod(c, methodName, argsClasses);

        if (method == null) {
            return;
        }

        try {
            method.setAccessible(true);
            method.invoke(o, args);
            method.setAccessible(false);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
