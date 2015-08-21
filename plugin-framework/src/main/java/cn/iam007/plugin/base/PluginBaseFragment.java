package cn.iam007.plugin.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.io.File;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.loader.PluginResourceLoader;
import cn.iam007.plugin.model.PluginSpec;

/**
 * 插件UI fragment基类
 */
public class PluginBaseFragment extends Fragment {

    private PluginSpec mPluginSpec = null;

    private PluginResourceLoader mPluginResourceLoader = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        mPluginSpec = bundle.getParcelable(PluginConstants.KEY_PLUGIN_SPEC);

        mPluginResourceLoader = PluginResourceLoader.getResource(getActivity().getApplication(),
                new File(mPluginSpec.getPluginBinary()));
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

    /**
     * todo: 将plugin fragment中的intent转换为android系统intent
     *
     * @param intent
     * @return
     */
    protected Intent urlMap(Intent intent) {
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

//            PluginItem pluginItem = PluginManager.getPluginItem(mPluginId);
//            if (pluginItem == null) {
//                break;
//            }
//
//            String fragmentCode = uri.getHost();
//            PluginFragmentSpec fragmentSpec = pluginItem.getFragment(fragmentCode);
//
//            intent = new Intent();
//            intent.setClass(getActivity(), PluginActivity.class);
//            intent.setData(uri);
//            intent.putExtra("_pluginId", mPluginId);
//            intent.putExtra("_fragment", fragmentSpec);
        } while (false);

        return intent;
    }

    public PluginResourceLoader getResource() {
        return mPluginResourceLoader;
    }
}
