package cn.iam007.plugin.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.iam007.plugin.PluginManager;
import cn.iam007.plugin.base.PluginActivity;
import cn.iam007.plugin.model.PluginSpec;

/**
 * Created by Administrator on 2015/8/21.
 */
public class PluginsActivity extends Activity {

    private ListView mPluginList;
    private PluginAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_plugins);

        mPluginList = (ListView) findViewById(R.id.plugin_list);

        init();
    }

    private void init() {
        mSpecs = PluginManager.getInstalledPlugin(this);
        mAdapter = new PluginAdapter();
        mPluginList.setAdapter(mAdapter);
        mPluginList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PluginManager.launchPlugin(PluginsActivity.this, mAdapter.getItem(position));
            }
        });
    }

    private ArrayList<PluginSpec> mSpecs = null;

    class PluginAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSpecs == null ? 0 : mSpecs.size();
        }

        @Override
        public PluginSpec getItem(int position) {
            return mSpecs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(PluginsActivity.this, R.layout.plugin_item, null);
            }

            PluginSpec spec = getItem(position);
            TextView pluginTitle = (TextView) convertView.findViewById(R.id.plugin_title);
            pluginTitle.setText(spec.getTitle());

            TextView pluginDesc = (TextView) convertView.findViewById(R.id.plugin_desc);
            pluginDesc.setText(spec.getPluginDesc());
            return convertView;
        }
    }
}
