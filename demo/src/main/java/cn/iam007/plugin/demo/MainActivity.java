package cn.iam007.plugin.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.base.PluginActivity;
import cn.iam007.plugin.base.PluginConstants;

/**
 * Created by Administrator on 2015/8/14.
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PluginManager.init(this);

        PluginManager.setPluginDir(new File(getExternalCacheDir(), "plugins"));

//        AVOSCloud.initialize(getApplication(), "detaw87pwodm1ulqc7trjpw596dedjtfzalwu744xvtq6afh",
//                "5ssyubw0t77n2pbf1g1pwcgxletkgzol5ctg0nt6hia0sgov");

//        PluginUtils.init(this);
//
//        PluginItem item = null;
//        try {
//            item = new PluginItem(makeTmpPluginItem());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        PluginManager.addPluginItem(item);

        setContentView(R.layout.activity_main);
        findViewById(R.id.launch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, PluginActivity.class);
//
//                String fragmentCode = "1";
//                String fragmentName = "cn.iam007.plugin.demo.p2048.MainFragment";
//
////                PluginFragmentSpec fragmentSpec =
////                        new PluginFragmentSpec(fragmentCode, fragmentName);
////                fragmentSpec.setTitle("2048");
////                intent.putExtra(PluginConstants.KEY_FRAGMENT, fragmentSpec);
//
//                intent.putExtra(PluginActivity.KEY_TOOLBAR_GONE, false);
//                startActivity(intent);
//                String pluginId = "cn.iam007.plugin.demo.p2048";
//                PluginManager.launchPlugin(MainActivity.this, pluginId);
//
//                PluginManager.getInstalledPlugin(getApplication());
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PluginsActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        PluginUtils.uploadPlugins(MainActivity.this);
                        debugInitPluginDir();
                    }
                }).start();
            }
        });
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, "succ", Toast.LENGTH_SHORT).show();
            return false;
        }
    });

    private JSONObject makeTmpPluginItem() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", "2048");
            object.put("name", "name_1");
            object.put("desc", "desc_1");
            object.put("icon", "icon_1");
            object.put("md5", "2048");
            object.put("url", "url_1");
            object.put("type", "type_1");
            object.put("version", "version_1");
            object.put("forceUpdate", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 该接口主要是debug使用，可以将插件根目录下的plg文件自动安装，并配置到PluginManager中
     */
    private void debugInitPluginDir() {
        File pluginRootDir = new File(Environment.getExternalStorageDirectory(), "plugins");
        pluginRootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(PluginConstants.PLUGIN_SUFFIX)) {
                    // 找到插件文件，进行安装
                    PluginManager.installPlugin(getApplication(), new File(dir, filename), false);
                    return true;
                }
                return false;
            }
        });
    }
//
//    private void debugInstallPlugin(File pluginFile) {
//        if (pluginFile == null || (!pluginFile.isFile())) {
//            return;
//        }
//
//        do {
////            PluginItem item = PluginUtils.getPluginItem(pluginFile.getAbsolutePath());
//            PluginManager.installPlugin(this, pluginFile);
//        } while (false);
//    }
}
