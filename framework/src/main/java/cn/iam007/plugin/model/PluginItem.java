package cn.iam007.plugin.model;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.base.PluginActivity;
import cn.iam007.plugin.base.PluginConstants;
import cn.iam007.plugin.dynamicloader.PluginResources;
import cn.iam007.plugin.utils.FileUtils;

public class PluginItem {

    private Context context;

    // plugin unique id
    private String pluginId;

    // the name of the plugin
    private String pluginName;

    // the description of the plugin
    private String pluginDesc;

    // the icon to represent the plugin
    private String pluginIcon;

    // the md5 of the plugin apk file
    private String pluginMD5;

    // the url to download the plugin file
    private String pluginUrl;

    // the type of the plugin
    private String pluginType;

    // the version of the plugin
    private String pluginVersion;

    // the plugin whether need to force update to current version
    private boolean pluginForceUpdate = false;

    // 插件文件spec
    private PluginFileSpec mPluginFileSpec = null;

    // 插件目前处于的状态
    public enum Mode {
        NOT_READY, // 插件状态暂时未知
        ERROR, // 插件状态出现错误
        NOT_INSTALL, // 插件未安装
        INSTALLING, // 插件安装中
        INSTALLED, // 插件安装完成
        NEED_UPDATE // 插件需要更新
    }

    private Mode mode = Mode.NOT_READY;
    private String pluginParams;

    public PluginItem(Context context, String json) {
        try {
            init(new JSONObject(json));
        } catch (JSONException e) {
            mode = Mode.ERROR;
        }

        pluginParams = json;
        this.context = context;
    }

    public PluginItem(Context context, JSONObject params) {
        init(params);
        this.context = context;
        this.pluginParams = params.toString();
    }

    private void init(JSONObject params) {
        this.pluginId = params.optString("id");
        this.pluginName = params.optString("name");
        this.pluginDesc = params.optString("desc");
        this.pluginIcon = params.optString("icon");
        this.pluginMD5 = params.optString("md5");
        this.pluginUrl = params.optString("url");
        this.pluginType = params.optString("type");
        this.pluginVersion = params.optString("version");
        this.pluginForceUpdate = params.optBoolean("forceUpdate", false);

        mPluginFileSpec = new PluginFileSpec(this.pluginId,
                this.pluginUrl,
                this.pluginMD5,
                null);

        PluginManager.addPluginItem(this);
    }

    /**
     * @return the mFragmentSpecs
     */
    public ArrayList<PluginFragmentSpec> getFragmentSpecs() {
        return mFragmentSpecs;
    }

    /**
     * @param mFragmentSpecs the mFragmentSpecs to set
     */
    public void setFragmentSpecs(
            ArrayList<PluginFragmentSpec> mFragmentSpecs) {
        this.mFragmentSpecs = mFragmentSpecs;
    }

    /**
     * 获取指定的code的fragment
     *
     * @param code
     */
    public PluginFragmentSpec getFragment(String code) {
        PluginFragmentSpec result = null;
        for (PluginFragmentSpec fragmentSpec : mFragmentSpecs) {
            if (fragmentSpec.code().equalsIgnoreCase(code)) {
                result = fragmentSpec;
            }
        }

        return result;
    }

    /**
     * @return the mPluginFileSpec
     */
    public PluginFileSpec getPluginFileSpec() {
        return mPluginFileSpec;
    }

    /**
     * @param mPluginFileSpec the mPluginFileSpec to set
     */
    public void setPluginFileSpec(PluginFileSpec mPluginFileSpec) {
        this.mPluginFileSpec = mPluginFileSpec;
    }

    /**
     * @return the pluginId
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * @param pluginId the pluginId to set
     */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * @return the pluginName
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * @param pluginName the pluginName to set
     */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * @return the pluginIcon
     */
    public String getPluginIcon() {
        return pluginIcon;
    }

    /**
     * @param pluginIcon the pluginIcon to set
     */
    public void setPluginIcon(String pluginIcon) {
        this.pluginIcon = pluginIcon;
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

    public boolean validation() {
        boolean validation = true;

        do {
            if (TextUtils.isEmpty(this.pluginId)) {
                validation = false;
                break;
            }

            if (TextUtils.isEmpty(this.pluginName)) {
                validation = false;
                break;
            }

            if (TextUtils.isEmpty(this.pluginMD5)) {
                validation = false;
                break;
            }

            if (TextUtils.isEmpty(this.pluginUrl)) {
                validation = false;
                break;
            }

            if (TextUtils.isEmpty(this.pluginVersion)) {
                validation = false;
                break;
            }

        } while (false);

        return validation;
    }

    // plugin apk安装的地址
    private String pluginInstalledPath = null;

    /**
     * 获取plugin安装的地址，主要是apk存放的目录路径
     *
     * @param context
     * @return
     */
    public String getInstalledDir(Context context) {
        String installedPath = null;

        if (!TextUtils.isEmpty(pluginInstalledPath)) {
            installedPath = pluginInstalledPath;
        } else {
            installedPath =
                    context.getCacheDir().getAbsolutePath() + File.separator + this.pluginId;
        }

        File file = new File(installedPath);

        if (!file.isDirectory()) {
            file.mkdirs();
        }

        return installedPath;
    }

    /**
     * 获取已安装插件的配置信息文件
     *
     * @param context
     * @return
     */
    private String getInstalledConfigFile(Context context) {
        String configPath = getInstalledDir(context) + File.separator + "plugin.properties";
        return configPath;
    }

    /**
     * 下载安装或者更新插件
     *
     * @return
     */
    public void install(Context context) {
        // 检查是否有安装的版本
        // 插件的apk文件是否存在
        File installedFile = new File(getInstalledDir(context), this.pluginMD5 + ".apk");

        boolean needInstall = false;
        if (installedFile.isFile()) {
            // 已经安装程序，检查是否需要更新
        } else {
            // 插件未安装
            needInstall = true;
        }

        // 需要安装插件
        if (needInstall) {
            // 清空插件安装目录
            FileUtils.cleanDir(getInstalledDir(context));

            // todo: 下载新apk文件
//            CommonHttpUtils.download(this.pluginUrl,
//                    installedFile.getAbsolutePath(),
//                    new DownloadCallback() {
//
//                        @Override
//                        public void onFinish(boolean state, File file) {
//                            Log.d("PluginItem", "download finish:" + state + " " + file);
//                            if (state) {
//                                initFragmentSpecs();
//                            }
//                        }
//                    });

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(getInstalledConfigFile(context));
                fos.write(this.pluginParams.getBytes("utf-8"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                fos = null;
            }

        } else {
            // 解析apk中插件配置数据，
            initFragmentSpecs();
        }
    }

    /**
     * 该插件所配置的fragment界面列表
     */
    private ArrayList<PluginFragmentSpec> mFragmentSpecs = null;

    private void initFragmentSpecs() {
        if (mFragmentSpecs == null) {
            mFragmentSpecs = new ArrayList<PluginFragmentSpec>();
        } else {
            return;
        }

        PluginResources pluginResources = PluginResources.getResources(context, mPluginFileSpec);
        AssetManager assetManager = pluginResources.getAssets();
        try {
            InputStream is = assetManager.open("plugin.properties");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            JSONObject object = new JSONObject(new String(buffer, "utf-8"));
            JSONArray fragments = object.getJSONArray("fragments");
            JSONObject fragment = null;
            String fragmentName = null;
            String fragmentCode = null;
            String fragmentTitle = null;
            for (int i = 0; i < fragments.length(); i++) {
                fragment = fragments.getJSONObject(i);
                fragmentName = fragment.optString("name");
                fragmentCode = fragment.optString("code");
                fragmentTitle = fragment.optString("title", this.pluginName);

                PluginFragmentSpec fragmentSpec =
                        new PluginFragmentSpec(fragmentCode,
                                fragmentName);
                fragmentSpec.setTitle(fragmentTitle);
                mFragmentSpecs.add(fragmentSpec);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动插件，返回是否启动成功
     *
     * @return true表示启动成功，false表示无法启动插件，原因可能有插件未安装，插件文件错误，插件配置错误
     */
    public boolean start(Context context) {
        boolean result = false;

        PluginFragmentSpec pluginFragmentSpec =
                mFragmentSpecs.get(0);

        Intent intent = new Intent();
        intent.setClass(context, PluginActivity.class);
        Uri uri = Uri.fromParts(PluginConstants.PRIMARY_SCHEME,
                pluginFragmentSpec.code(), null);
        intent.setData(uri);
        intent.putExtra("_pluginId", this.pluginId);
        intent.putExtra("_fragment", mFragmentSpecs.get(0));

        context.startActivity(intent);

        return result;
    }
}
