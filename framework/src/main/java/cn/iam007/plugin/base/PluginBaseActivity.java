package cn.iam007.plugin.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.model.PluginFileSpec;
import cn.iam007.plugin.model.PluginFragmentSpec;
import cn.iam007.plugin.model.PluginItem;

/**
 * 主Activity容器，负责启动并装载Fragment
 * <p/>
 * 启动前所有依赖的资源必须加载完毕（由urlMapping和LoaderActivity负责）
 * <p/>
 * Intent参数：<br>
 * _site:SiteSpec，指定的site地图<br>
 * _code:String，ClassLoader所需要载入的FileID，如果为空则使用APK自带ClassLoader<br>
 * _fragment:String，Fragment的类名<br>
 *
 * @author Yimin
 */
public class PluginBaseActivity extends AppCompatActivity {

    //    ArrayList<PluginFragmentSpec> mFragmentSpecs = null;
    private String mPluginId;
    protected PluginFileSpec mPluginFileSpec;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Intent intent = getIntent();

        mPluginId = intent.getStringExtra("_pluginId");
        mPluginFileSpec = PluginManager.getPluginItem(mPluginId).getPluginFileSpec();
        //
        //        mFragmentSpecs = intent.getParcelableArrayListExtra("_fragmentSpecs");
    }

    protected PluginFileSpec getPluginFileSpec() {
        return mPluginFileSpec;
    }

    public final String getPluginId() {
        return mPluginId;
    }

    // 开始解析插件的信息
    public final Intent baseUrlMap(Intent intent) {
        boolean validation = false;

        do {
            Uri uri = intent.getData();
            if (uri == null) {
                break;
            }

            if (uri.getScheme() == null) {
                break;
            }

            if (!(PluginConstants.PRIMARY_SCHEME.equalsIgnoreCase(
                    uri.getScheme()))) {
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
            intent.putExtra("_pluginId", mPluginId);
            intent.putExtra("_fragment", fragmentSpec);

            validation = true;

        } while (false);

        return intent;
    }

    @Override
    public void startActivity(Intent intent) {
        intent = urlMap(intent);
        super.startActivity(intent);
    }

    public Intent urlMap(Intent intent) {
        return baseUrlMap(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent = urlMap(intent);
        super.startActivityForResult(intent, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent,
                                          int requestCode) {
        intent = urlMap(intent);
        super.startActivityFromFragment(fragment, intent, requestCode);
    }

    public void startActivity(String urlSchema) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlSchema)));
    }

    public void startActivityForResult(String urlSchema, int requestCode) {
        startActivityForResult(
                new Intent(Intent.ACTION_VIEW, Uri.parse(urlSchema)),
                requestCode);
    }
}
