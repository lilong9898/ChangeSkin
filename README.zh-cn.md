# 高透明度Android换肤框架
### 目录
 - [特性](#特性)
 - [Demo](#demo)
 - [使用方法](#使用方法)
 	- [皮肤包的生成](#皮肤包的生成)
  - [换肤框架的接入](#换肤框架的接入)
  - [换肤API的使用](#换肤api的使用)
  - [换肤规则的约定](#换肤规则的约定)
  - [默认皮肤](#默认皮肤)
 - [原理](#原理)
  - [ViewFactory拦截布局文件的inflate过程，统计可换肤的资源](#ViewFactory拦截布局文件的inflate过程，统计可换肤的资源)
  - [解析皮肤包，统计要换肤的资源](#解析皮肤包，统计要换肤的资源)
  - [创建皮肤包的Resources实例](#创建皮肤包的resources实例)
  - [匹配app中可换肤的资源和皮肤包中要换肤的资源](#匹配app中可换肤的资源和皮肤包中要换肤的资源)
  - [根据匹配情况完成换肤](#根据匹配情况完成换肤)
  - [完整过程](#完整过程)
  
***

### 特性
#### * 动态加载皮肤apk获取皮肤资源，皮肤apk无需安装
#### * 通过重设相关控件的属性来实现换肤，无需重新生成任何控件，无需重启任何组件
#### * 根据资源类型和引用名匹配原则自动搜索可以换肤的组件和属性，无需任何自定义控件或属性支持
#### * 支持android.app包中Fragment, Activity的换肤
#### * 支持android.support.v4.app包中的Fragment, FragmentActivity的换肤

***

### Demo
![image](https://github.com/lilong9898/ChangeSkin/blob/master/demo.gif)

***

### 使用方法
通过Android Studio导入两个project, 分别是Skin和SkinChange，其中Skin用于成皮肤包，SkinChange是demo app.
建议不要改动两个project的相对位置，这样demo可以直接运行.

#### 皮肤包的生成
皮肤包由Skin project生成

皮肤包是仅包含资源，不包含代码的apk. 通过设置productFlavor来同时生成多个皮肤的皮肤包apk：
```Groovy
//默认生成用于demo的三个皮肤包，分别是沙漠（橘色），草地（绿色）和海洋（蓝色）的主题
//另外加上demo app中默认的主题（灰色），本demo一共包含4个皮肤
productFlavors {
        desert {

        }
        grass {

        }
        sea {

        }
    }
```
每个皮肤包不包含java代码，仅包含换肤所用的资源：
```xml
<!--这是草地（绿色）皮肤所指定的三个资源引用的颜色值-->
<!--demo app中所有含有这三个资源引用的属性，都会在换肤时被换成该皮肤包中的资源值-->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="background">@android:color/holo_green_light</color>
    <color name="tv_title">@android:color/holo_green_dark</color>
    <color name="tv_title_frag">@android:color/holo_green_dark</color>
</resources>
```
生成皮肤包时，务必保证每个flavor的res目录所出现的资源引用是完全一致的，仅资源值不一样.这是为了保证不同皮肤包的资源引用对称，不会出现某个资源引用在某个皮肤里有，另外的皮肤里没有的情况，防止换肤时出现某些资源引用的值无法更新的情况.

不同皮肤的res目录都准备好后，运行builld.gradle中定义的gradle task:
```groovy
task buildSkins(dependsOn: "assembleRelease") {

    delete fileTree(DEST_PATH) {
        include SKIN_APK_FILE_NAME_PATTERN
    }

    copy {
        from(FROM_PATH) {
            include SKIN_APK_FILE_NAME_PATTERN
        }
        into DEST_PATH
    }

}
```
该task会将三个不同flavor下生成的apk，也就是三个皮肤包，改名成skin_[皮肤名]的形式，此demo中会生成skin_desert.apk, skin_grass.apk和skin_sea.apk. 这三个apk会被该task复制到SkinChange project的assets目录下，也就是将皮肤包装到demo app的assets目录下，以便demo app在换肤时加载.

#### 换肤框架的接入

demo app对应于SkinChange project， 其接入了本换肤框架. 接入方法:

(1) Application继承SkinApplication:
```xml
<application
        android:name=".base.SkinApplication"
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">
        ....
```
(2) Activity继承SkinActivity:
```java
public class DemoActivity extends SkinActivity {
....
```
(3) Fragment继承SkinFragment:
```java
public class DemoFragment extends SkinFragment {
...
```
(4) LayoutInflater需要用SkinActivity和SkinFragment的getLayoutInflater()方法来获取. 如果系统方法的传入参数提供了layoutInflater则可用这个inflater，比如:
```java
...
// 生成一个FragmentPagerAdapter，其内部使用的layoutInflater需要从SkinActivity或SkinFragment的getLayoutInflater()方法获取后再传入
skinAdapter = new SkinTestFragmentPagerAdapter(getSupportFragmentManager(), getLayoutInflater());
...
```
#### 换肤API的使用

使用SkinManager的changeSkin(Context　context, View rootView, HashMap<String, ArrayList<SkinizedAttributeEntries>> map, SkinInfo info)方法来换肤:

```java
...
private SkinManager skinManager;
...
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        skinManager = SkinManager.getInstance(getApplicationContext());
        ....
         skinManager.changeSkin(getApplicationContext(), getWindow().getDecorView(), getSkinizedAttributeEntries(), info);
         ....
```
第二个参数rootView为需要换肤的viewTree的rootView，第三个参数是SkinActivity中换肤所需的数据结构，可以通过SkinActivity的getSkinizedAttributeEntries()方法获取，第四个参数是皮肤包信息，可以通过SkinManager的getCurSkinInfo()方法来获取当前皮肤包的信息.

注意：该方法仅对rootView开头的viewTree下所有控件有效，也就是说不同的activity因为rootView不同，需要单独换肤. 而fragment的rootView会在fragment added时被添加到host activity的viewTree里，所以不需单独换肤，hostActivity的换肤会同时作用到其所有的fragment里.

#### 换肤规则的约定

**所有拥有id的View的通过资源引用来赋值的属性，都可以参与换肤;**

**如果该属性的资源引用类型和名字与皮肤包中的某个资源引用的类型和名字都相同，则其值取得是皮肤包中该资源引用的值，由此实现换肤;**
举例，SkinChange project，即demo app中DemoActivity的布局文件里的根布局RelativeLayout:
```xml
...
<RelativeLayout
    android:id="@+id/container"
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background"
    >
    ...
```
它的background属性使用了资源引用，类型是color，名字是background，而在上文的皮肤包skin_grass.apk的资源中同样出现了类型color，引用名为background的资源:
```xml
...
<resources>
    <color name="background">@android:color/holo_green_light</color>
    ...
```
此时该RelativeLayout的background属性所引用的color类型的名叫background的资源，取值会从皮肤包中取，该属性换肤完成.

#### 可换肤的控件和属性

支持View, TextView和ImageView的大部分属性的换肤.

#### 默认皮肤

如果SkinChange project的assets文件夹中没有任何名字为skin_[皮肤名].apk的文件，则demo app会使用默认的灰色皮肤. 这个皮肤没有对应的皮肤包，就是demo app各控件属性的初始资源引用值，但该皮肤的换肤原理与其他皮肤包相同.其他皮肤和默认皮肤直接可以切换.

***

### 原理

#### ViewFactory拦截布局文件的inflate过程，统计可换肤的资源

```java
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
```
可换肤的属性以SkinizedAttributeEntry来表示.一个控件的一条可换肤的属性即构成这样一个Entry. 完整的属性名：可换肤属性的对应表为HashMap&lt;String, ArrayList&lt;SkinizedAttributeEntry>>，其中key为引用的资源类型 + "/" + 资源引用名，value为当前SkinViewFactory所拦截的inflate过程中，所有出现的引用了该类型资源，并且引用名为该名称的属性的列表.比如这样一个TextView：
```xml
<TextView
        android:id="@+id/tv_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/tv_title_frag"
        android:textColor="@color/tv_title_frag"
        android:textSize="20sp"
        android:textStyle="bold|italic"/>
```
会生成两个key，分别为"string/tv_title_frag"和"color/tv_title_frag",　分别对应的value是一个只有一个元素的ArrayList. "string/tv_title_frag"对应的list包含一个skinizedAttributeEntry，内容是这个TextView控件的引用，属性名text，以及其引用的资源类型string和引用名tv_title_frag. “color/tv_title_frag"对应的list包含一个skinizedAttributeEntry，内容是这个TextView控件的引用，属性名textColor，以及其引用的资源类型color和引用名tv_title_frag. 

每个Activity/FragmentActivity会有这样一个skinizedAttrMap，用于换肤时与皮肤apk中的资源进行匹配.

#### 解析皮肤包，统计要换肤的资源

```java
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
```
皮肤包中一条要换肤的资源用ResourceEntry来表示，包含一条资源的资源类型，资源名，资源id. 这些信息通过反射解析皮肤包中的R文件获取.解析一个皮肤包的资源，会返回这个皮肤包中所有要换肤的资源，也就是ResourceEntry的列表.

#### 创建皮肤包的Resources实例

```java
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
```

#### 匹配app中可换肤的资源和皮肤包中要换肤的资源

```java
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
                    //TODO duplicate id within the same view tree is a problem
                    // e.g. when fragment's layout has a child view with the same id as the parent view
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
```
遍历皮肤包中要换肤的资源ResourceEntry，将其中资源类型和资源名与app中可换肤的资源列表，即SkinizedAttributeEntry的列表来对比. 如果对比上，则将SkinizedAttributeEntry中的控件引用和控件id提取出来，获取到控件，再根据SkinizedAttributeEntry中的属性名，反射调用该控件的对应方法将皮肤包中的资源提取出来，设置到该控件上，实现换肤.

#### 根据匹配情况完成换肤

```java
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
            v.setPadding(v.getPaddingLeft(), v.
            ......
```
根据控件，需换肤的属性名，皮肤包的Resources实例，皮肤包中的资源id来调用控件的set方法来换肤.

#### 完整过程

```java
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

        ArrayList<ResourceEntry> resourceEntries = null;
        Resources resources = null;

        // restore to default skin
        if (info.isSelf()) {
            // parse R.java file of THIS APP's apk, get all attributes and their values(references) in it
            resourceEntries = SkinUtil.getThisAppResourceEntries(context);
            // resources instance from this app
            resources = context.getResources();
        }
        // change skin according to skin apk
        else {
            // parse R.java file of skin apk, get all attributes and their values(references) in it
            resourceEntries = SkinUtil.getSkinApkResourceEntries(context, context.getClassLoader(), info.getSkinApkPath());
            // get Resources instance of skin apk
            resources = SkinUtil.getApkResources(context.getResources(), info.getSkinApkPath());
        }

        changeSkinByResourceEntries(rootView, skinizedAttrMap, resourceEntries, resources);
    }
```
