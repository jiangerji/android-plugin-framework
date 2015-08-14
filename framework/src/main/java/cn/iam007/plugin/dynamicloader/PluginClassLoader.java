package cn.iam007.plugin.dynamicloader;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

import cn.iam007.plugin.model.PluginFileSpec;
import cn.iam007.plugin.utils.CacheUtils;
import dalvik.system.DexClassLoader;

public class PluginClassLoader extends DexClassLoader {
    //    FileSpec file;
    PluginFileSpec mFileSpec;
    PluginClassLoader[] mDeps;

    PluginClassLoader(PluginFileSpec fileSpec,
                      String dexPath,
                      String optimizedDirectory,
                      String libraryPath,
                      ClassLoader parent,
                      PluginClassLoader[] deps) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.mFileSpec = fileSpec;
        this.mDeps = deps;
    }

    /**
     * @return the mFileSpec
     */
    public PluginFileSpec getFileSpec() {
        return mFileSpec;
    }

    /**
     * @param fileSpec the mFileSpec to set
     */
    public void setFileSpec(PluginFileSpec fileSpec) {
        this.mFileSpec = fileSpec;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(className);
        if (clazz != null) {
            return clazz;
        }

        try {
            clazz = getParent().loadClass(className);
        } catch (ClassNotFoundException e) {
        }

        if (clazz != null) {
            return clazz;
        }

        if (mDeps != null) {
            for (PluginClassLoader c : mDeps) {
                try {
                    clazz = c.findClass(className);
                    break;
                } catch (ClassNotFoundException e) {
                }
            }
        }

        if (clazz != null) {
            return clazz;
        }

        clazz = findClass(className);
        return clazz;
    }

    static final HashMap<String, PluginClassLoader> loaders = new HashMap<>();

    /**
     * return the classloader of the plugin, if the plugin apk file is not
     * exsites on the disk, return null
     *
     * @param pluginFileSpec
     * @return
     */
    public static PluginClassLoader getClassLoader(Context context, PluginFileSpec pluginFileSpec) {
        String pluginId = pluginFileSpec.getPluginId();

        PluginClassLoader classLoader = loaders.get(pluginId);
        if (classLoader != null) {
            return classLoader;
        }

        // TODO: 加载该插件依赖的插件

        // 加载classloader
        File dir = CacheUtils.getPluginDir(context);

        // 获取插件apk
        dir = new File(dir, pluginId);
        File path = new File(dir, pluginFileSpec.getPluginMD5() + ".apk");
        if (!path.isFile()) {
            Log.d("PluginClassLoader", "" + path.getAbsolutePath() + " is not exsits!");
            return null;
        }

        File outdir = new File(dir, "dex");
        outdir.mkdir();
        classLoader = new PluginClassLoader(pluginFileSpec,
                path.getAbsolutePath(),
                outdir.getAbsolutePath(),
                null,
                context.getClassLoader(),
                null);
        loaders.put(pluginId, classLoader);
        return classLoader;
    }
}
