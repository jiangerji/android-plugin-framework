package cn.iam007.plugin.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.model.PluginFragmentSpec;
import cn.iam007.plugin.model.PluginItem;

public class PluginBaseFragment extends Fragment {

    private String mPluginId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        mPluginId = bundle.getString(PluginConstants.KEY_PLUGIN_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

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

            PluginItem pluginItem = PluginManager.getPluginItem(mPluginId);
            if (pluginItem == null) {
                break;
            }

            String fragmentCode = uri.getHost();
            PluginFragmentSpec fragmentSpec = pluginItem.getFragment(fragmentCode);

            intent = new Intent();
            intent.setClass(getActivity(), PluginActivity.class);
            intent.setData(uri);
            intent.putExtra("_pluginId", mPluginId);
            intent.putExtra("_fragment", fragmentSpec);
        } while (false);

        return intent;
    }

    private boolean isIntentValidation(Intent intent) {
        boolean validation = false;

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

            validation = true;

        } while (false);

        return validation;
    }

    @Override
    public void startActivity(Intent intent) {
        if (isIntentValidation(intent)) {
            intent = urlMap(intent);
            super.startActivity(intent);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (isIntentValidation(intent)) {
            intent = urlMap(intent);
            super.startActivityForResult(intent, requestCode);
        }
    }
}
