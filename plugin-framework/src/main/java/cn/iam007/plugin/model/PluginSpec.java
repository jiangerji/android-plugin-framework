package cn.iam007.plugin.model;

import android.content.res.AssetManager;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
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
    private String mPluginDesc;

    /**
     * the type of the plugin, current useless
     */
    private String mPluginType;

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
     * 设置插件可执行文件路径
     *
     * @param binaryPath
     */
    public void setPluginBinary(String binaryPath) {
        mPluginBinary = binaryPath;
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
     * 设置插件启动fragment类
     *
     * @param launchUI
     */
    public void setPluginLaunchUI(String launchUI) {
        mPluginLaunchFragment = launchUI;
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
     * 设置该插件的md5值
     *
     * @return
     */
    public void setPluginMD5(String md5) {
        mPluginMD5 = md5;
    }

    public void setPluginDesc(String desc) {
        mPluginDesc = desc;
    }

    public String getPluginDesc() {
        return mPluginDesc;
    }

    public void setPluginType(String type) {
        mPluginType = type;
    }

    public String getPluginType() {
        return mPluginType;
    }

    public void setPluginForceUpdate(boolean force) {
        mPluginForceUpdate = force;
    }

    public boolean getPluginForceUpdate() {
        return mPluginForceUpdate;
    }


    public PluginSpec() {

    }

    /**
     * 使用插件assets目录下的plugin.json文件进行初始化
     *
     * @param params
     */
    public PluginSpec(JSONObject params) throws JSONException {
        this.mPluginTitle = params.optString("name");
        this.mPluginDesc = params.optString("desc");
        this.mPluginType = params.optString("type", "normal");
        this.mPluginForceUpdate = params.optBoolean("forceUpdate", false);

        JSONArray fragments = params.getJSONArray("fragments");
        if (fragments.length() == 0) {
            throw new RuntimeException("插件没有配置fragment!");
        }

        JSONObject fragmentValue;
        String fragmentName, fragmentCode;
        for (int i = 0; i < fragments.length(); i++) {
            fragmentValue = fragments.getJSONObject(i);
            if (fragmentValue != null) {
                mPluginLaunchFragment = fragmentValue.getString("name");
            }
        }
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

                spec = new PluginSpec(object);

                String md5Value = MD5Utils.getFileMd5(new File(pluginFile));
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
        dest.writeString(mPluginId);
        dest.writeString(mPluginTitle);
        dest.writeString(mPluginDesc);
        dest.writeString(mPluginBinary);
        dest.writeString(mPluginLaunchFragment);
        dest.writeString(mPluginMD5);
        dest.writeString(mPluginType);
        dest.writeString(mPluginForceUpdate ? "true" : "false");
    }

    public static final Parcelable.Creator<PluginSpec> CREATOR =
            new Parcelable.Creator<PluginSpec>() {
                public PluginSpec createFromParcel(Parcel in) {
                    return new PluginSpec(in);
                }

                public PluginSpec[] newArray(int size) {
                    return new PluginSpec[size];
                }
            };

    protected PluginSpec(Parcel in) {
        mPluginId = in.readString();
        mPluginTitle = in.readString();
        mPluginDesc = in.readString();
        mPluginBinary = in.readString();
        mPluginLaunchFragment = in.readString();
        mPluginMD5 = in.readString();
        mPluginType = in.readString();
        mPluginForceUpdate = Boolean.valueOf(in.readString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PluginSpec:" + mPluginId + "\n");
        sb.append("  name:" + mPluginTitle + "\n");
        sb.append("  desc:" + mPluginDesc + "\n");
        sb.append("  launch:" + mPluginLaunchFragment + "\n");
        sb.append("  md5:" + mPluginMD5 + "\n");
        sb.append("  binary:" + mPluginBinary + "\n");
        return sb.toString();
    }
}
