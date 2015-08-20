package cn.iam007.plugin.base;

import cn.iam007.plugin.R;

public class PluginConstants {

    /**
     * 用于标示插件之间intent跳转的scheme
     */
    public static final String PRIMARY_SCHEME = "app";

    public static final String PLUGIN_SUFFIX = ".plg";

    /**
     * activity接收参数的键值
     */
    public static final String KEY_PLUGIN_SPEC = "KEY_PLUGIN_SPEC";

    public static final String KEY_PLUGIN_ID = "pluginId";
    public static final String KEY_PLUGIN_TITLE = "pluginTitle";
    public static final String KEY_FRAGMENT = "fragment";

    /**
     * error code
     */

    public final static int NO_ERROR = 0x00;

    /**
     * plugin activity需要显示的plugin未加载
     */
    public final static int ERROR_ACTIVITY_PLUGIN_NOT_LOAD = R.string.error_plugin_not_loaded;

    /**
     * plugin activity接收的intent参数错误
     */
    public final static int ERROR_ACTIVITY_PLUGIN_ID_IS_EMPTY = R.string.error_plugin_id_is_empty;

    /**
     * 插件可执行文件不存在或不是有效格式
     */
    public final static int ERROR_ACTIVITY_PLUGIN_BINARY_ERROR = R.string.error_plugin_binary_error;

    /**
     * 插件可执行文件格式错误
     */
    public final static int ERROR_ACTIVITY_PLUGIN_BINARY_FORMAT_ERROR =
            R.string.error_plugin_binary_format_error;

    public final static int ERROR_ACTIVITY_PLUGIN_LAUNCH_UI_IS_EMPTY =
            R.string.error_plugin_launch_ui_is_empty;
}
