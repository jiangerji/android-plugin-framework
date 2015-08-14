package cn.iam007.plugin.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 插件UI fragment描述类
 */
public class PluginFragmentSpec implements Parcelable {

    /**
     * 相当于该fragment在该apk包中的别名
     */
    private String code;

    /**
     * 插件UI fragment的完整包名
     */
    private String name;

    // fragment title
    private String title;

    public PluginFragmentSpec(String code, String name) {
        this.code = code;
        this.name = name;
        this.title = "";
    }

    public PluginFragmentSpec(JSONObject json) throws JSONException {
        code = json.optString("code", "");
        name = json.optString("name", "");
        title = json.optString("title", "");
    }

    /**
     * the action code to launch the fragment
     *
     * @return
     */
    public String code() {
        return code;
    }

    /**
     * the full class path of the fragment
     *
     * @return
     */
    public String name() {
        return name;
    }

    /**
     * fragment's title
     *
     * @return
     */
    public String title() {
        return title;
    }

    /**
     * set the fragment's title
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("xxx://").append(name).append(':');
        sb.append('(');
        if (code == null) {
            sb.append('.');
        } else {
            sb.append(code);
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(code);
        out.writeString(name);
        out.writeString(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PluginFragmentSpec> CREATOR =
            new Parcelable.Creator<PluginFragmentSpec>() {
                public PluginFragmentSpec createFromParcel(Parcel in) {
                    return new PluginFragmentSpec(in);
                }

                public PluginFragmentSpec[] newArray(int size) {
                    return new PluginFragmentSpec[size];
                }
            };

    protected PluginFragmentSpec(Parcel in) {
        code = in.readString();
        name = in.readString();
        title = in.readString();
    }
}
