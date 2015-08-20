package cn.iam007.plugin;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import cn.iam007.plugin.base.PluginConstants;
import cn.iam007.plugin.model.PluginSpec;
import cn.iam007.plugin.utils.FileUtils;
import cn.iam007.plugin.utils.PluginUtils;

/**
 * 提供插件管理相关功能类
 */
public class PluginManager {
    private final static String TAG = "PluginManager";

    /**
     * 存放插件的位置
     */
    private static File mPluginDir = null;

    private static ConcurrentHashMap<String, PluginSpec> mPluginSpecMap = null;

    /**
     * 初始化插件管理器
     *
     * @param context
     */
    public static void init(Context context) {
        if (context == null) {
            throw new RuntimeException("初始化PluginManager的context为空!");
        }

        context.getDir("plugins", Context.MODE_PRIVATE);

        mPluginSpecMap = new ConcurrentHashMap<>();
    }

    /**
     * 安装插件
     *
     * @param context
     * @param pluginFile
     * @param overWrite
     */
    public static boolean installPlugin(Context context, File pluginFile, boolean overWrite) {
        if ((!pluginFile.isFile()) ||
                (!pluginFile.getName().endsWith(PluginConstants.PLUGIN_SUFFIX))) {
            // 插件文件不存在或者格式错误，不进行安装
            return false;
        }

        boolean succ = false;
        Log.d(TAG, "开始安装：" + pluginFile.getAbsolutePath());
        /**
         * 1.  获取plugin id
         * 2.  在plugin dir下创建一个以plugin id命名的目录
         * 3.  将plugin file移动到新创建的目录下
         * 4.  读取plugin的基本信息，加载到PluginManager中
         */

        String plugiFilePath = pluginFile.getAbsolutePath();
        do {
            PluginSpec pluginSpec = PluginSpec.parseFromFile(plugiFilePath);

            if (pluginSpec == null) {
                // 插件中没有plugin.json文件
                Log.d(TAG, "请检查assets/plugin.json文件");
                break;
            }

            // plugin id就是pluginFile的包名
            PackageManager pm = context.getPackageManager();
            PackageInfo info =
                    pm.getPackageArchiveInfo(plugiFilePath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = plugiFilePath;
            appInfo.publicSourceDir = plugiFilePath;

            // 插件id
            String pluginId = appInfo.packageName;
            pluginSpec.setPluginId(pluginId);

            // 插件标题
            String pluginTitle = appInfo.loadLabel(pm).toString();
            pluginSpec.setTitle(pluginTitle);

            // 创建插件安装目录
            File installDir = new File(getPluginDir(), pluginId);
            if (installDir.isDirectory()) {
                // 如果存在，覆盖安装，清理安装目录
                FileUtils.cleanDir(pluginFile);
            } else {
                if (!installDir.mkdirs()) {
                    // 创建安装目录失败
                    Log.d(TAG, "  创建安装目录失败");
                    break;
                }
            }

            Drawable appIcon = appInfo.loadIcon(pm);
            File plugIconFile = new File(installDir, pluginSpec.getPluginId() + ".png");
            if (!PluginUtils.draw2File(appIcon, plugIconFile)) {
                Log.d(TAG, "  获取插件图标失败！");
            }

            File installedFile =
                    new File(installDir, pluginSpec.getPluginMD5() + PluginConstants.PLUGIN_SUFFIX);
            if (!pluginFile.renameTo(installedFile)) {
                Log.d(TAG, "  安装失败：复制插件文件失败！");
                break;
            }

            Log.d(TAG, "  安装成功！");
        } while (false);

        return succ;
    }

    /**
     * 启动插件
     *
     * @param pluginId 需要启动插件的id
     */

    public static void launchPlugin(Context context, String pluginId) {

    }

    /**
     * 设置插件存放位置
     *
     * @param pluginDir
     */
    public static void setPluginDir(File pluginDir) {
        if (pluginDir != null) {
            mPluginDir = pluginDir;
        }
    }

    /**
     * 获取插件根目录
     *
     * @return
     */
    public static File getPluginDir() {
        return mPluginDir;
    }

    public static PluginSpec getPluginSpec(String pluginId) {
        return mPluginSpecMap.get(pluginId);
    }
}
