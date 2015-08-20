package cn.iam007.plugin.model;

import android.content.res.AssetManager;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cn.iam007.plugin.utils.MD5Utils;

/**
 * Created by Administrator on 2015/8/20.
 */
public class PluginSpec implements Parcelable {

    /**
     * plugin unique id
     */
    private String mPluginId;

    /**
     * the plugin title, will display in the actionbar
     */
    private String mPluginTitle; // plugin title

    /**
     * the plugin binary executable file
     */
    private String mPluginBinary;

    /**
     * the plugin launch fragment
     */
    private String mPluginLaunchFragment;

    /**
     * the description of the plugin
     */
    private final String mPluginDesc;

    /**
     * the type of the plugin, current useless
     */
    private final String mPluginType;

    /**
     * should force update to this version plugin
     */
    private boolean mPluginForceUpdate = false;

    /**
     * 插件安装文件的md5值
     */
    private String mPluginMD5;

    /**
     * 返回插件id
     *
     * @return
     */
    public String getPluginId() {
        return mPluginId;
    }

    /**
     * 设置插件id
     *
     * @param pluginId
     */
    public void setPluginId(String pluginId) {
        if (pluginId != null) {
            mPluginId = pluginId;
        }
    }

    public String getTitle() {
        return mPluginTitle;
    }

    /**
     * 设置插件标题
     *
     * @param title
     */
    public void setTitle(String title) {
        mPluginTitle = title;
    }

    /**
     * 返回插件可执行文件的路径
     *
     * @return
     */
    public String getPluginBinary() {
        return mPluginBinary;
    }

    /**
     * 返回插件启动fragment的类
     *
     * @return
     */
    public String getPluginLaunchUI() {
        return mPluginLaunchFragment;
    }

    /**
     * 返回该插件的md5值
     *
     * @return
     */
    public String getPluginMD5() {
        return mPluginMD5;
    }

    /**
     * 使用插件assets目录下的plugin.json文件进行初始化
     *
     * @param params
     */
    public PluginSpec(JSONObject params) {
        this.mPluginTitle = params.optString("name");
        this.mPluginDesc = params.optString("desc");
        this.mPluginType = params.optString("type", "normal");
        this.mPluginForceUpdate = params.optBoolean("forceUpdate", false);
    }

    /**
     * 从插件安装文件解析出PluginSpec
     *
     * @param pluginFile
     * @return
     */
    public static PluginSpec parseFromFile(String pluginFile) {
        PluginSpec spec = null;
        do {
            AssetManager am = null;
            InputStream is = null;
            try {
                am = AssetManager.class.newInstance();
                am.getClass().getMethod("addAssetPath", String.class).invoke(am, pluginFile);
                is = am.open("plugin.json");
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                JSONObject object = new JSONObject(new String(buffer, "utf-8"));

                String md5Value = MD5Utils.getFileMd5(new File(pluginFile));

                spec = new PluginSpec(object);
                spec.mPluginMD5 = md5Value;
            } catch (Exception e) {
                break;
            } finally {
                // 释放资源
                if (am != null) {
                    am.close();
                    am = null;
                }

                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                    is = null;
                }
            }
        } while (false);

        return spec;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
