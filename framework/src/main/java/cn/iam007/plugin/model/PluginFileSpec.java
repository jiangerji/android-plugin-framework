package cn.iam007.plugin.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 插件的基本信息
 */
public class PluginFileSpec implements Parcelable {

    /**
     * 插件ID，相当于插件的唯一标示符
     */
    private String pluginId;

    /**
     * 插件下载链接地址
     */
    private String pluginUrl;

    /**
     * 插件文件的md5值
     */
    private String pluginMD5;

    /**
     * 该插件依赖的其他插件的id
     */
    private String[] pluginDepends;

    public PluginFileSpec(String id, String url, String md5, String[] deps) {
        this.pluginId = id;
        this.pluginUrl = url;
        this.pluginMD5 = md5;
        this.pluginDepends = deps;
    }

    public PluginFileSpec(JSONObject json) throws JSONException {
        pluginId = json.getString("id");
        pluginUrl = json.getString("url");
        pluginMD5 = json.optString("md5");
        JSONArray arr = json.optJSONArray("deps");
        if (arr != null) {
            pluginDepends = new String[arr.length()];
            for (int i = 0; i < pluginDepends.length; i++) {
                pluginDepends[i] = arr.getString(i);
            }
        }
    }

    /**
     * 获取插件id
     *
     * @return 插件id
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * 设置插件id
     *
     * @param pluginId 插件id
     */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * 获取该插件下载链接地址
     *
     * @return 返回插件下载地址
     */
    public String getPluginUrl() {
        return pluginUrl;
    }

    /**
     * @param pluginUrl the pluginUrl to set
     */
    public void setPluginUrl(String pluginUrl) {
        this.pluginUrl = pluginUrl;
    }

    /**
     * @return the pluginMD5
     */
    public String getPluginMD5() {
        return pluginMD5;
    }

    /**
     * @param pluginMD5 the pluginMD5 to set
     */
    public void setPluginMD5(String pluginMD5) {
        this.pluginMD5 = pluginMD5;
    }

    /**
     * @return the pluginDepends
     */
    public String[] getPluginDepends() {
        return pluginDepends;
    }

    /**
     * @param pluginDepends the pluginDepends to set
     */
    public void setPluginDepends(String[] pluginDepends) {
        this.pluginDepends = pluginDepends;
    }

    @Override
    public int hashCode() {
        return pluginId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PluginFileSpec))
            return false;
        return pluginId.equals(((PluginFileSpec) o).pluginId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(pluginId);
        if (pluginDepends != null && pluginDepends.length > 0) {
            sb.append(':');
            sb.append(pluginDepends[0]);
            for (int i = 1; i < pluginDepends.length; i++) {
                sb.append(',').append(pluginDepends[i]);
            }
        }
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(pluginId);
        out.writeString(pluginUrl);
        out.writeString(pluginMD5);
        out.writeStringArray(pluginDepends);
    }

    public static final Parcelable.Creator<PluginFileSpec> CREATOR =
            new Parcelable.Creator<PluginFileSpec>() {
                public PluginFileSpec createFromParcel(Parcel in) {
                    return new PluginFileSpec(in);
                }

                public PluginFileSpec[] newArray(int size) {
                    return new PluginFileSpec[size];
                }
            };

    protected PluginFileSpec(Parcel in) {
        pluginId = in.readString();
        pluginUrl = in.readString();
        pluginMD5 = in.readString();
        pluginDepends = in.createStringArray();
    }

}
