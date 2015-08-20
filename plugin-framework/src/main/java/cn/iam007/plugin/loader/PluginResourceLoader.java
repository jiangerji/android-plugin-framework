package cn.iam007.plugin.loader;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 加载插件中的资源文件的类
 */
public class PluginResourceLoader {

    Resources mRes;
    AssetManager mAsset;

    static final HashMap<String, PluginResourceLoader> loaders = new HashMap<>();

    PluginResourceLoader(Resources res, AssetManager assetManager) {
        mRes = res;
        mAsset = assetManager;
    }

    /**
     * Resources.getDrawable(id)
     */
    public Drawable getDrawable(int id) {
        return mRes.getDrawable(id);
    }

    /**
     * Resources.getText(id)
     */
    public CharSequence getText(int id) {
        return mRes.getText(id);
    }

    /**
     * Resources.getString(id)
     */
    public String getString(int id) {
        return mRes.getString(id);
    }

    /**
     * Resources.getStringArray(id)
     */
    public String[] getStringArray(int id) {
        return mRes.getStringArray(id);
    }

    /**
     * Resources.getColor(id)
     */
    public int getColor(int id) {
        return mRes.getColor(id);
    }

    /**
     * Resources.getColorStateList(id)
     */
    public ColorStateList getColorStateList(int id) {
        return mRes.getColorStateList(id);
    }

    /**
     * Resources.getDimension(id)
     */
    public float getDimension(int id) {
        return mRes.getDimension(id);
    }

    /**
     * Resources.getFraction(id, base, pbase)
     */
    public float getFraction(int id, int base, int pbase) {
        return mRes.getFraction(id, base, pbase);
    }

    /**
     * Resources.getDimensionPixelSize(id)
     */
    public int getDimensionPixelSize(int id) {
        return mRes.getDimensionPixelSize(id);
    }

    /**
     * Resources.getDimensionPixelOffset(id)
     */
    public int getDimensionPixelOffset(int id) {
        return mRes.getDimensionPixelOffset(id);
    }

    /**
     * Resources.openRawResource(id)
     */
    public InputStream openRawResource(int id) {
        return mRes.openRawResource(id);
    }

    /**
     * 返回插件自身的资源对象
     *
     * @return
     */
    public Resources getResources() {
        return mRes;
    }

    /**
     * 返回插件自身的AssetManager
     *
     * @return
     */
    public AssetManager getAssets() {
        return mAsset;
    }


    static PluginResourceLoader getResource(Context context, File pluginFile) {
        PluginResourceLoader resourceLoader = null;

        do {
            if (pluginFile == null) {
                break;
            }

            if (!pluginFile.isFile()) {
                break;
            }

            String resourceLoaderId =
                    Base64.encodeToString(pluginFile.getAbsolutePath().getBytes(), Base64.URL_SAFE);
            resourceLoader = loaders.get(resourceLoaderId);
            if (resourceLoader != null) {
                break;
            }

            AssetManager am;
            try {
                am = AssetManager.class.newInstance();
                am.getClass().getMethod("addAssetPath", String.class).invoke(
                        am, pluginFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

            Resources superRes = context.getResources();
            Resources res =
                    new Resources(am, superRes.getDisplayMetrics(), superRes.getConfiguration());

            resourceLoader = new PluginResourceLoader(res, am);
            loaders.put(resourceLoaderId, resourceLoader);
        } while (false);

        return resourceLoader;
    }
}
