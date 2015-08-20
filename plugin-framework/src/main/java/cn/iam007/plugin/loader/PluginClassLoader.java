package cn.iam007.plugin.loader;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

/**
 * 插件的classloader, 可以动态的加载插件里的类文件
 */
public class PluginClassLoader extends DexClassLoader {

    private final static String TAG = "PluginClassLoader";

    /**
     * 该插件类依赖插件的classloader
     */
    PluginClassLoader[] mDeps;

    /**
     * 用于缓存已经加载的classloader
     */
    static final HashMap<String, PluginClassLoader> mLoaders = new HashMap<>();

    PluginClassLoader(
            String dexPath,
            String optimizedDirectory,
            String libraryPath,
            ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
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

    /**
     * 返回插件的classloader, 如果插件apk文件不存在或者有其他错误，返回null
     *
     * @param context    加载classloader的上下文
     * @param pluginFile 插件安装文件
     * @return 返回已经加载插件类的classloader
     */
    public static PluginClassLoader getClassLoader(Context context, File pluginFile) {
        PluginClassLoader classLoader = null;

        do {
            if (pluginFile == null) {
                break;
            }

            if (!pluginFile.isFile()) {
                break;
            }

            String classLoaderId =
                    Base64.encodeToString(pluginFile.getAbsolutePath().getBytes(), Base64.URL_SAFE);
            classLoader = mLoaders.get(classLoaderId);
            if (classLoader != null) {
                break;
            }

            // TODO: 加载插件依赖的classloader

            File pluginDir = pluginFile.getParentFile();
            File dexDir = new File(pluginDir, "dex");
            if (!dexDir.mkdirs()) {
                Log.d(TAG, "创建dex失败！");
                break;
            }

            classLoader =
                    new PluginClassLoader(pluginFile.getAbsolutePath(), dexDir.getAbsolutePath(),
                            null, context.getClassLoader());
            mLoaders.put(classLoaderId, classLoader);
        } while (false);

        return classLoader;
    }
}
