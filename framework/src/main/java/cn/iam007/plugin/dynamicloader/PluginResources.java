package cn.iam007.plugin.dynamicloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.iam007.plugin.base.PluginActivity;
import cn.iam007.plugin.model.PluginFileSpec;
import cn.iam007.plugin.utils.CacheUtils;

public class PluginResources {

    PluginFileSpec file;
    String packageName;
    Resources res;
    AssetManager asset;
    PluginResources[] deps;

    /**
     * 加载插件中的资源文件
     *
     * @param fileSpec    插件相关文件描述
     * @param packageName 插件的包名
     * @param res         插件的资源文件
     * @param asset       插件的asset资源管理对象
     * @param deps        插件依赖的资源
     */
    PluginResources(PluginFileSpec fileSpec, String packageName, Resources res,
                    AssetManager asset, PluginResources[] deps) {
        this.file = fileSpec;
        this.packageName = packageName;
        this.res = res;
        this.asset = asset;
        this.deps = deps;
    }

    /**
     * Resources.getDrawable(id)
     */
    public Drawable getDrawable(int id) {
        return res.getDrawable(id);
    }

    /**
     * Resources.getText(id)
     */
    public CharSequence getText(int id) {
        return res.getText(id);
    }

    /**
     * Resources.getString(id)
     */
    public String getString(int id) {
        return res.getString(id);
    }

    /**
     * Resources.getStringArray(id)
     */
    public String[] getStringArray(int id) {
        return res.getStringArray(id);
    }

    /**
     * Resources.getColor(id)
     */
    public int getColor(int id) {
        return res.getColor(id);
    }

    /**
     * Resources.getColorStateList(id)
     */
    public ColorStateList getColorStateList(int id) {
        return res.getColorStateList(id);
    }

    /**
     * Resources.getDimension(id)
     */
    public float getDimension(int id) {
        return res.getDimension(id);
    }

    /**
     * Resources.getFraction(id, base, pbase)
     */
    public float getFraction(int id, int base, int pbase) {
        return res.getFraction(id, base, pbase);
    }

    /**
     * Resources.getDimensionPixelSize(id)
     */
    public int getDimensionPixelSize(int id) {
        return res.getDimensionPixelSize(id);
    }

    /**
     * Resources.getDimensionPixelOffset(id)
     */
    public int getDimensionPixelOffset(int id) {
        return res.getDimensionPixelOffset(id);
    }

    /**
     * Resources.openRawResource(id)
     */
    public InputStream openRawResource(int id) {
        return res.openRawResource(id);
    }

    public byte[] getRawResource(int id) {
        InputStream ins = openRawResource(id);
        try {
            int n = ins.available();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(n > 0 ? n
                    : 4096);
            byte[] buf = new byte[4096];
            int l;
            while ((l = ins.read(buf)) != -1) {
                bos.write(buf, 0, l);
            }
            ins.close();
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * 返回独立的Resources
     * 对Resources进行操作时不会处理依赖关系，所有依赖包的内容均不会出现在该Resources中。
     *
     * @return
     */
    public Resources getResources() {
        return res;
    }

    /**
     * 返回独立的AssetManager
     * <p/>
     * 对AssetManager进行操作时不会处理依赖关系，所有依赖包的内容均不会出现在该AssetManager中。
     *
     * @return
     */
    public AssetManager getAssets() {
        return asset;
    }

    /**
     * 同LayoutInflater.inflate(id, parent, attachToRoot)
     * <p/>
     * 不会处理依赖关系，请确保id对应的layout在当前包内
     *
     * @return
     * @throws Resources.NotFoundException
     */
    public View inflate(Context context, int id, ViewGroup parent, boolean attachToRoot) {
        if (!(context instanceof PluginActivity)) {
            throw new RuntimeException(
                    "unable to inflate without PluginActivity context");
        }
        PluginActivity ma = (PluginActivity) context;
        PluginResources old = ma.getOverrideResources();
        ma.setOverrideResources(this);
        try {
            View v = LayoutInflater.from(context).inflate(id, parent, attachToRoot);
            return v;
        } finally {
            ma.setOverrideResources(old);
        }
    }

    static final HashMap<String, PluginResources> loaders = new HashMap<String, PluginResources>();

    /**
     * return null if not available on the disk
     */
    public static PluginResources getResources(Context context, PluginFileSpec file) {
        PluginResources rl = loaders.get(file.getPluginId());
        if (rl != null)
            return rl;

        File dir = CacheUtils.getPluginDir(context);
        dir = new File(dir, file.getPluginId());
        File path = new File(dir, file.getPluginMD5() + ".apk");
        if (!path.isFile())
            return null;

        try {
            AssetManager am = AssetManager.class.newInstance();
            am.getClass().getMethod("addAssetPath", String.class).invoke(am,
                    path.getAbsolutePath());

            // parse packageName from AndroidManifest.xml
            String packageName = null;
            XmlResourceParser xml = am
                    .openXmlResourceParser("AndroidManifest.xml");
            int eventType = xml.getEventType();
            xmlloop:
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("manifest".equals(xml.getName())) {
                            packageName = xml.getAttributeValue(null, "package");
                            break xmlloop;
                        }
                }
                eventType = xml.nextToken();
            }
            xml.close();
            if (packageName == null) {
                throw new RuntimeException(
                        "package not found in AndroidManifest.xml [" + path + "]");
            }

            Resources superRes = context.getResources();
            Resources res = new Resources(am, superRes.getDisplayMetrics(),
                    superRes.getConfiguration());

            rl = new PluginResources(file, packageName, res, am, null);
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException(e);
        }

        loaders.put(file.getPluginId(), rl);
        return rl;
    }

    /**
     * 从当前类所在的包载入MyResource
     *
     * @param context
     * @param clazz
     * @return
     * @throws RuntimeException 如果当前类不是动态加载包载入的
     */
    public static PluginResources getResource(Context context, Class<?> clazz) {
        if (!(clazz.getClassLoader() instanceof PluginClassLoader)) {
            throw new RuntimeException(clazz
                    + " is not loaded from dynamic loader");
        }
        return getResource(context, (PluginClassLoader) clazz.getClassLoader());
    }

    static PluginResources getResource(Context context, PluginClassLoader mcl) {
        PluginFileSpec file = mcl.getFileSpec();
        PluginResources rl = loaders.get(file.getPluginId());
        if (rl != null)
            return rl;

        PluginResources[] rs = null;
        if (mcl.mDeps != null) {
            rs = new PluginResources[mcl.mDeps.length];
            for (int i = 0; i < rs.length; i++) {
                PluginResources r = getResource(context, mcl.mDeps[i]);
                rs[i] = r;
            }
        }

        File dir = CacheUtils.getPluginDir(context);
        dir = new File(dir, file.getPluginId());
        File path = new File(dir, file.getPluginMD5() + ".apk");
        if (!path.isFile())
            throw new RuntimeException(path + " not exists");

        try {
            AssetManager am = AssetManager.class.newInstance();
            am.getClass().getMethod("addAssetPath", String.class)
                    .invoke(am, path.getAbsolutePath());

            Resources superRes = context.getResources();
            Resources res = new Resources(am, superRes.getDisplayMetrics(),
                    superRes.getConfiguration());

            // parse packageName from AndroidManifest.xml
            String packageName = null;
            XmlResourceParser xml = am
                    .openXmlResourceParser("AndroidManifest.xml");
            int eventType = xml.getEventType();
            xmlloop:
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("manifest".equals(xml.getName())) {
                            packageName = xml.getAttributeValue(null, "package");
                            break xmlloop;
                        }
                }
                eventType = xml.nextToken();
            }
            xml.close();
            if (packageName == null) {
                throw new RuntimeException(
                        "package not found in AndroidManifest.xml [" + path
                                + "]");
            }

            rl = new PluginResources(file, packageName, res, am, rs);
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException(e);
        }

        loaders.put(file.getPluginId(), rl);
        return rl;
    }
}
