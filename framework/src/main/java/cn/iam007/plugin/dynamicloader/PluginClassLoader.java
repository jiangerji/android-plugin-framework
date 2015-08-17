package cn.iam007.plugin.dynamicloader;

import android.content.Context;

import java.io.File;
import java.util.HashMap;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.model.PluginFileSpec;
import dalvik.system.DexClassLoader;

public class PluginClassLoader extends DexClassLoader {

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

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
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
                    clazz = c.loadClass(className, false);
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
     * 返回插件的classloader, 如果插件apk文件不存在或者有其他错误，返回null
     *
     * @param pluginFileSpec 插件文件
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
        File dir = PluginManager.getPluginDir(context);

        // 获取插件apk
        dir = new File(dir, pluginId);

        // 使用插件的md值作为文件名
        File path = new File(dir, pluginFileSpec.getPluginMD5() + ".apk");
        if (!path.isFile()) {
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
