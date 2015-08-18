package cn.iam007.plugin;

import android.content.Context;

import java.io.File;
import java.util.HashMap;

import cn.iam007.plugin.base.PluginConstants;
import cn.iam007.plugin.model.PluginFileSpec;
import cn.iam007.plugin.model.PluginItem;


public class PluginManager {

    private static HashMap<String, PluginItem> mPluginItemMap = new HashMap<String, PluginItem>();

    /**
     * 加入新插件
     *
     * @param item
     */
    public static void addPluginItem(PluginItem item) {
        mPluginItemMap.put(item.getPluginId(), item);
    }

    /**
     * 根据id获取某个插件
     *
     * @param pluginId 插件id, 插件的唯一标识符
     * @return
     */
    public static PluginItem getPluginItem(String pluginId) {
        return mPluginItemMap.get(pluginId);
    }

    private static File mPluginRootDir = null;

    /**
     * 设置插件缓存存放的位置，如果设置过多次，则之前缓存的插件信息会丢失
     * 如果没有设置，会使用应用内部缓存路径
     *
     * @param dir 插件缓存路径
     */
    public static void setPluginDir(File dir) {
        if (dir != null) {
            mPluginRootDir = dir;
            mPluginRootDir.mkdirs();
        }
    }

    /**
     * 获取插件缓存根目录
     *
     * @param context
     * @return 返回插件缓存根目录
     */
    public static File getPluginDir(Context context) {
        if (mPluginRootDir != null) {
            if (!mPluginRootDir.exists()) {
                // 创建缓存目录
                mPluginRootDir.mkdirs();
                throw new RuntimeException("无法创建插件缓存目录！");
            }
        } else {
            mPluginRootDir = context.getCacheDir();
        }
        return mPluginRootDir;
    }

    /**
     * 返回插件安装文件
     *
     * @return
     */
    public static File getPluginFile(Context context, PluginFileSpec item) {
        File dir = PluginManager.getPluginDir(context);
        dir = new File(dir, item.getPluginId());
        return new File(dir, item.getPluginMD5() + PluginConstants.PLUGIN_SUFFIX);
    }
}
