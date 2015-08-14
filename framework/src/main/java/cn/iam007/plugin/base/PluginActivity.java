package cn.iam007.plugin.base;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.iam007.plugin.dynamicloader.PluginClassLoader;
import cn.iam007.plugin.dynamicloader.PluginResources;
import cn.iam007.plugin.model.PluginFileSpec;
import cn.iam007.plugin.model.PluginFragmentSpec;

public class PluginActivity extends PluginBaseActivity {

    private String fragmentName;
    private boolean loaded;
    private PluginClassLoader classLoader;
    private PluginResources myResources;
    private AssetManager assetManager;
    private Resources resources;
    private Theme theme;
    private FrameLayout rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int error = 0;

        PluginFileSpec fileSpec = getPluginFileSpec();

        // must be load at the first start
        do {
            // 获取需要启动的fragment spec
            PluginFragmentSpec fragmentSpec = intent.getParcelableExtra("_fragment");

            // 设置标题
            setTitle(fragmentSpec.title());

            fragmentName = fragmentSpec.name();
            if (TextUtils.isEmpty(fragmentName)) {
                // TODO: 没有设置启动fragment
                error = 202;
                break;
            }

            classLoader = PluginClassLoader.getClassLoader(this, fileSpec);
            loaded = classLoader != null;
            if (!loaded) {
                // TODO: 加载插件class出现异常
                error = 210;
                break;
            }
        } while (false);

        rootView = new FrameLayout(this);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rootView.setId(android.R.id.primary);
        setContentView(rootView);

        if (!loaded) {
            TextView text = new TextView(this);
            text.setText("无法载入页面" + (error == 0 ? "" : " #" + error));
            text.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            rootView.addView(text);

            return;
        }

        // the fragment will be restored by framework
        if (savedInstanceState != null)
            return;

        Fragment fragment = null;
        try {
            fragment = (Fragment) getClassLoader().loadClass(fragmentName)
                    .newInstance();
        } catch (Exception e) {
            // TODO: 初始化fragment出现异常
            loaded = false;
            classLoader = null;
            error = 211; // #211
            TextView text = new TextView(this);
            text.setText("无法载入页面#" + error);
            text.append("\n" + e);
            text.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            rootView.addView(text);

            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.primary, fragment);
        ft.commit();
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader == null ? super.getClassLoader() : classLoader;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    @Override
    public Intent urlMap(Intent intent) {
        do {
            // only process my scheme uri
            Uri uri = intent.getData();
            if (uri == null)
                break;
            if (uri.getScheme() == null)
                break;
            if (!(PluginConstants.PRIMARY_SCHEME.equalsIgnoreCase(uri.getScheme())))
                break;
        } while (false);

        return super.urlMap(intent);
    }

    @Override
    public AssetManager getAssets() {
        return assetManager == null ? super.getAssets() : assetManager;
    }

    @Override
    public Resources getResources() {
        return resources == null ? super.getResources() : resources;
    }

    @Override
    public Theme getTheme() {
        return theme == null ? super.getTheme() : theme;
    }

    public PluginResources getOverrideResources() {
        return myResources;
    }

    public void setOverrideResources(PluginResources myres) {
        if (myres == null) {
            this.myResources = null;
            this.resources = null;
            this.assetManager = null;
            this.theme = null;
        } else {
            this.myResources = myres;
            this.resources = myres.getResources();
            this.assetManager = myres.getAssets();
            Theme t = myres.getResources().newTheme();
            t.setTo(getTheme());
            this.theme = t;
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
    }

}
