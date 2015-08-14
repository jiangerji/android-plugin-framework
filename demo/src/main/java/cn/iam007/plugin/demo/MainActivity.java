package cn.iam007.plugin.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.base.PluginActivity;
import cn.iam007.plugin.model.PluginFragmentSpec;
import cn.iam007.plugin.model.PluginItem;

/**
 * Created by Administrator on 2015/8/14.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PluginItem item = new PluginItem(this, makeTmpPluginItem());
        PluginManager.addPluginItem(item);

        setContentView(R.layout.activity_main);
        findViewById(R.id.launch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PluginActivity.class);

                String fragmentCode = "1";
                String fragmentName = "cn.iam007.plugin.flappybatta.GameFragment";

                PluginFragmentSpec fragmentSpec =
                        new PluginFragmentSpec(fragmentCode, fragmentName);
                intent.putExtra("_fragment", fragmentSpec);

                intent.putExtra("_pluginId", "id_1");
                startActivity(intent);
            }
        });
    }

    private JSONObject makeTmpPluginItem() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", "id_1");
            object.put("name", "name_1");
            object.put("desc", "desc_1");
            object.put("icon", "icon_1");
            object.put("md5", "md5_1");
            object.put("url", "url_1");
            object.put("type", "type_1");
            object.put("version", "version_1");
            object.put("forceUpdate", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}
