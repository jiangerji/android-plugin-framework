package cn.iam007.plugin;

import java.util.HashMap;

import cn.iam007.plugin.model.PluginItem;


public class PluginManager {

    private static HashMap<String, PluginItem> mPluginItemMap = new HashMap<String, PluginItem>();

    public static void addPluginItem(PluginItem item) {
        mPluginItemMap.put(item.getPluginId(), item);
    }

    public static PluginItem getPluginItem(String pluginId) {
        return mPluginItemMap.get(pluginId);
    }

}
