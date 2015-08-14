package cn.iam007.plugin.base;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.dynamicloader.PluginClassLoader;
import cn.iam007.plugin.dynamicloader.PluginResources;
import cn.iam007.plugin.model.PluginFileSpec;
import cn.iam007.plugin.model.PluginFragmentSpec;
import cn.iam007.plugin.model.PluginItem;

public class PluginActivity extends AppCompatActivity {

    private String fragmentName;
    private boolean loaded;
    private PluginClassLoader classLoader;
    private PluginResources mResources;
    private AssetManager assetManager;
    private Resources resources;
    private Theme theme;
    private FrameLayout rootView;

    /**
     * 插件id
     */
    private String mPluginId;
    protected PluginFileSpec mPluginFileSpec;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();

        Intent intent = getIntent();
        int error = 0;

        mPluginId = intent.getStringExtra(PluginConstants.KEY_PLUGIN_ID);
        mPluginFileSpec = PluginManager.getPluginItem(mPluginId).getPluginFileSpec();

        do {
            // 获取需要启动的fragment spec
            PluginFragmentSpec fragmentSpec =
                    intent.getParcelableExtra(PluginConstants.KEY_FRAGMENT);

            // 设置标题
            setTitle(fragmentSpec.title());

            fragmentName = fragmentSpec.name();
            if (TextUtils.isEmpty(fragmentName)) {
                // TODO: 没有设置启动fragment
                error = 202;
                break;
            }

            classLoader = PluginClassLoader.getClassLoader(this, mPluginFileSpec);
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

        if (savedInstanceState != null) {
            return;
        }

        Fragment fragment;
        try {
            fragment = (Fragment) getClassLoader().loadClass(fragmentName).newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(PluginConstants.KEY_PLUGIN_ID, mPluginId);
            fragment.setArguments(bundle);
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

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader == null ? super.getClassLoader() : classLoader;
    }

    /**
     * 解析插件的intent
     */
    private final Intent urlMap(Intent intent) {
        do {
            Uri uri = intent.getData();
            if (uri == null) {
                break;
            }

            if (uri.getScheme() == null) {
                break;
            }

            if (!(PluginConstants.PRIMARY_SCHEME.equalsIgnoreCase(uri.getScheme()))) {
                break;
            }

            PluginItem pluginItem = PluginManager.getPluginItem(mPluginId);
            if (pluginItem == null) {
                break;
            }
            String fragmentCode = uri.getHost();
            PluginFragmentSpec fragmentSpec = pluginItem.getFragment(fragmentCode);

            //            intent = new Intent();
            intent.setClass(this, PluginActivity.class);
            //            intent.setData(uri);
            intent.putExtra(PluginConstants.KEY_PLUGIN_ID, mPluginId);
            intent.putExtra(PluginConstants.KEY_PLUGIN_ID, fragmentSpec);
        } while (false);

        return intent;
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
        return mResources;
    }

    public void setOverrideResources(PluginResources myres) {
        if (myres == null) {
            this.mResources = null;
            this.resources = null;
            this.assetManager = null;
            this.theme = null;
        } else {
            this.mResources = myres;
            this.resources = myres.getResources();
            this.assetManager = myres.getAssets();
            Theme t = myres.getResources().newTheme();
            t.setTo(getTheme());
            this.theme = t;
        }
    }

    @Override
    public void startActivity(Intent intent) {
        intent = urlMap(intent);
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent = urlMap(intent);
        super.startActivityForResult(intent, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void startActivityFromFragment(android.app.Fragment fragment, Intent intent,
                                          int requestCode) {
        intent = urlMap(intent);
        super.startActivityFromFragment(fragment, intent, requestCode);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
