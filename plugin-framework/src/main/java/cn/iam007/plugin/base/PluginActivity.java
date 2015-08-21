package cn.iam007.plugin.base;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import cn.iam007.plugin.R;
import cn.iam007.plugin.loader.PluginClassLoader;
import cn.iam007.plugin.loader.PluginResourceLoader;
import cn.iam007.plugin.model.PluginSpec;

/**
 * 显示插件的宿主activity
 */
public class PluginActivity extends AppCompatActivity {
    /**
     * toolbar是否浮动在UI上层，true表示浮动在UI上层
     */
    public final static String KEY_TOOLBAR_OVERLAY = "KEY_TOOLBAR_OVERLAY";

    /**
     * 是否显示toolbar, true表示不显示
     */
    public final static String KEY_TOOLBAR_GONE = "KEY_TOOLBAR_GONE";

    private PluginClassLoader mClassLoader;
    private PluginResourceLoader mPluginResourcesLoader;
    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;

    private Toolbar mToolbar;
    private FrameLayout mContainer;
    private TextView mErrorInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.iam007_plugin_activity);
        initView();

        setupPluginFragment();
    }

    private void initView() {
        Intent intent = getIntent();
        boolean toolbarOverlay = false;
        boolean notDisplayToolbar = false;
        if (intent != null) {
            toolbarOverlay = intent.getBooleanExtra(KEY_TOOLBAR_OVERLAY, false);
            notDisplayToolbar = intent.getBooleanExtra(KEY_TOOLBAR_GONE, false);
        }

        mContainer = (FrameLayout) findViewById(R.id.container);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mErrorInfo = (TextView) findViewById(R.id.error_info);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (notDisplayToolbar) {
            mToolbar.setVisibility(View.GONE);
        }

        if (!toolbarOverlay) {
            // 不隐藏toolbar
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
            layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar);
        }
    }

    /**
     * 设置插件的
     */
    private void setupPluginFragment() {
        int errorCode = PluginConstants.NO_ERROR;
        do {
            Intent intent = getIntent();


            PluginSpec pluginSpec = intent.getParcelableExtra(PluginConstants.KEY_PLUGIN_SPEC);

            if (pluginSpec == null) {
                errorCode = PluginConstants.ERROR_ACTIVITY_PLUGIN_NOT_LOAD;
                break;
            }

            String pluginId = pluginSpec.getPluginId();
            if (TextUtils.isEmpty(pluginId)) {
                errorCode = PluginConstants.ERROR_ACTIVITY_PLUGIN_ID_IS_EMPTY;
                break;
            }

            // 设置为插件的title
            String title = pluginSpec.getTitle();
            setTitle(title);


            String pluginBinaryPath = pluginSpec.getPluginBinary();
            File pluginFile = new File(pluginBinaryPath);
            if (!pluginFile.isFile()) {
                // 插件可执行文件不存在
                errorCode = PluginConstants.ERROR_ACTIVITY_PLUGIN_BINARY_ERROR;
                break;
            }

            // 加载classloader
            mClassLoader = PluginClassLoader.getClassLoader(this, pluginFile);
            if (mClassLoader == null) {
                errorCode = PluginConstants.ERROR_ACTIVITY_PLUGIN_BINARY_FORMAT_ERROR;
                break;
            }

            // 加载插件启动fragment
            String launchFragment = pluginSpec.getPluginLaunchUI();
            Fragment fragment;
            try {
                fragment = (Fragment) getClassLoader().loadClass(launchFragment).newInstance();
                Bundle argument = new Bundle();
                argument.putParcelable(PluginConstants.KEY_PLUGIN_SPEC, pluginSpec);
                fragment.setArguments(argument);
            } catch (Exception e) {
                e.printStackTrace();
                errorCode = PluginConstants.ERROR_ACTIVITY_PLUGIN_LAUNCH_UI_IS_EMPTY;
                break;
            }

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        } while (false);

        if (errorCode != PluginConstants.NO_ERROR) {
            showErrorInfo(getString(errorCode));
        }
    }

    private void showErrorInfo(String errorInfo) {
        mErrorInfo.setVisibility(View.VISIBLE);
        mErrorInfo.setText(errorInfo);
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

    @Override
    public ClassLoader getClassLoader() {
        return mClassLoader == null ? super.getClassLoader() : mClassLoader;
    }

    /**
     * 设置PluginActivity使用新的资源加载器，主要是插件中inflate接口使用,插件通过R文件插件资源使用
     *
     * @param pluginResourceLoader
     */
    public void setOverrideResources(PluginResourceLoader pluginResourceLoader) {
        if (pluginResourceLoader == null) {
            this.mPluginResourcesLoader = null;
            this.mResources = null;
            this.mAssetManager = null;
            this.mTheme = null;
        } else {
            this.mPluginResourcesLoader = pluginResourceLoader;
            this.mResources = pluginResourceLoader.getResources();
            this.mAssetManager = pluginResourceLoader.getAssets();
            Resources.Theme t = pluginResourceLoader.getResources().newTheme();
            t.setTo(getTheme());
            this.mTheme = t;
        }
    }

    @Override
    public Resources.Theme getTheme() {
        return mTheme == null ? super.getTheme() : mTheme;
    }

    @Override
    public AssetManager getAssets() {
        return mAssetManager == null ? super.getAssets() : mAssetManager;
    }

    @Override
    public Resources getResources() {
        return mResources == null ? super.getResources() : mResources;
    }


    public PluginResourceLoader getOverrideResources() {
        return mPluginResourcesLoader;
    }
}
