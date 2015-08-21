package cn.iam007.plugin;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import cn.iam007.plugin.base.PluginActivity;
import cn.iam007.plugin.base.PluginConstants;
import cn.iam007.plugin.model.PluginSpec;
import cn.iam007.plugin.utils.FileUtils;
import cn.iam007.plugin.utils.PluginUtils;

/**
 * 提供插件管理相关功能类
 */
public class PluginManager {
    private final static String TAG = "PluginManager";

    /**
     * 存放插件的位置
     */
    private static File mPluginDir = null;

    private static ConcurrentHashMap<String, PluginSpec> mPluginSpecMap = null;

    /**
     * 初始化插件管理器
     *
     * @param context
     */
    public static void init(Context context) {
        if (context == null) {
            throw new RuntimeException("初始化PluginManager的context为空!");
        }

        createPluginsDB(context);

        context.getDir("plugins", Context.MODE_PRIVATE);
        mPluginSpecMap = new ConcurrentHashMap<>();
    }

    /**
     * 安装插件
     *
     * @param context
     * @param pluginFile
     * @param overWrite
     */
    public static boolean installPlugin(Context context, File pluginFile, boolean overWrite) {
        if ((!pluginFile.isFile()) ||
                (!pluginFile.getName().endsWith(PluginConstants.PLUGIN_SUFFIX))) {
            // 插件文件不存在或者格式错误，不进行安装
            return false;
        }

        boolean succ = false;
        Log.d(TAG, "开始安装：" + pluginFile.getAbsolutePath());
        /**
         * 1.  获取plugin id
         * 2.  在plugin dir下创建一个以plugin id命名的目录
         * 3.  将plugin file移动到新创建的目录下
         * 4.  读取plugin的基本信息，加载到PluginManager中
         */

        String plugiFilePath = pluginFile.getAbsolutePath();
        do {
            PluginSpec pluginSpec = PluginSpec.parseFromFile(plugiFilePath);

            if (pluginSpec == null) {
                // 插件中没有plugin.json文件
                Log.d(TAG, "请检查assets/plugin.json文件");
                break;
            }

            // plugin id就是pluginFile的包名
            PackageManager pm = context.getPackageManager();
            PackageInfo info =
                    pm.getPackageArchiveInfo(plugiFilePath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = plugiFilePath;
            appInfo.publicSourceDir = plugiFilePath;

            // 插件id
            String pluginId = appInfo.packageName;
            pluginSpec.setPluginId(pluginId);

            // 插件标题
            String pluginTitle = appInfo.loadLabel(pm).toString();
            pluginSpec.setTitle(pluginTitle);

            // 创建插件安装目录
            File installDir = new File(getPluginDir(), pluginId);
            if (installDir.isDirectory()) {
                // 如果存在，覆盖安装，清理安装目录
                FileUtils.cleanDir(pluginFile);
            } else {
                if (!installDir.mkdirs()) {
                    // 创建安装目录失败
                    Log.d(TAG, "  创建安装目录失败");
                    break;
                }
            }

            Drawable appIcon = appInfo.loadIcon(pm);
            File plugIconFile = new File(installDir, pluginSpec.getPluginId() + ".png");
            if (!PluginUtils.draw2File(appIcon, plugIconFile)) {
                Log.d(TAG, "  获取插件图标失败！");
            }

            File installedFile =
                    new File(installDir, pluginSpec.getPluginMD5() + PluginConstants.PLUGIN_SUFFIX);
            pluginSpec.setPluginBinary(installedFile.getAbsolutePath());

            // 加入插件列表
            if (addPluginSpec(context, pluginSpec)) {
                if (!pluginFile.renameTo(installedFile)) {
                    Log.d(TAG, "  安装失败：复制插件文件失败！");
                    break;
                }
            } else {
                break;
            }
            mPluginSpecMap.put(pluginId, pluginSpec);
            Log.d(TAG, "  安装成功！");
        } while (false);

        return succ;
    }

    /**
     * 启动插件
     *
     * @param pluginId 需要启动插件的id
     */

    public static void launchPlugin(Context context, String pluginId) {
        PluginSpec pluginSpec = mPluginSpecMap.get(pluginId);
        Intent intent = new Intent();
        intent.setClass(context, PluginActivity.class);
        intent.putExtra(PluginConstants.KEY_PLUGIN_SPEC, pluginSpec);
        context.startActivity(intent);
    }

    public static void launchPlugin(Context context, PluginSpec pluginSpec) {
        Intent intent = new Intent();
        intent.setClass(context, PluginActivity.class);
        intent.putExtra(PluginConstants.KEY_PLUGIN_SPEC, pluginSpec);
        context.startActivity(intent);
    }

    /**
     * 设置插件存放位置
     *
     * @param pluginDir
     */
    public static void setPluginDir(File pluginDir) {
        if (pluginDir != null) {
            mPluginDir = pluginDir;
        }
    }

    /**
     * 获取插件根目录
     *
     * @return
     */
    public static File getPluginDir() {
        return mPluginDir;
    }

    private final static String PLUGIN_DB_NAME = "plugins.sb";
    private final static String PLUGIN_TABLE_NAME = "plugins";

    private static void createPluginsDB(Context context) {
        SQLiteDatabase db =
                context.openOrCreateDatabase(PLUGIN_DB_NAME, Context.MODE_PRIVATE, null);
        String createTableCmdFormat = "CREATE TABLE IF NOT EXISTS %s (" +
                "'pluginId' TEXT PRIMARY KEY NOT NULL UNIQUE , " +
                "'pluginTitle' TEXT, " +
                "'pluginBinary' TEXT, " +
                "'pluginLaunchFragment' TEXT, " +
                "'pluginDesc' TEXT, " +
                "'pluginType' TEXT, " +
                "'pluginForceUpdate' BOOL DEFAULT false, " +
                "'pluginMD5' TEXT NOT NULL )";
        String createTableCmd = String.format(createTableCmdFormat, PLUGIN_TABLE_NAME);
        try {
            db.execSQL(createTableCmd);
        } catch (Exception e) {
            Log.d(TAG, "创建插件数据库失败！");
        }
        db.close();
    }

    private static boolean addPluginSpec(Context context, PluginSpec spec) {
        boolean result = false;

        SQLiteDatabase db =
                context.openOrCreateDatabase(PLUGIN_DB_NAME, Context.MODE_PRIVATE, null);

        ContentValues cv = new ContentValues();
        cv.put("pluginId", spec.getPluginId());
        cv.put("pluginTitle", spec.getTitle());
        cv.put("pluginBinary", spec.getPluginBinary());
        cv.put("pluginLaunchFragment", spec.getPluginLaunchUI());
        cv.put("pluginDesc", spec.getPluginDesc());
        cv.put("pluginType", spec.getPluginType());
        cv.put("pluginForceUpdate", spec.getPluginForceUpdate());
        cv.put("pluginMD5", spec.getPluginMD5());

        //插入PluginSpec中的数据
        Cursor cursor = null;
        try {
            String cmd =
                    String.format("select count(*) from %s where pluginId = ?;", PLUGIN_TABLE_NAME);
            cursor = db.rawQuery(cmd, new String[]{spec.getPluginId()});
            cursor.moveToFirst();
            if (cursor.getInt(0) > 0) {
                db.update(PLUGIN_TABLE_NAME, cv, "pluginId = ?", new String[]{spec.getPluginId()});
            } else {
                db.insert(PLUGIN_TABLE_NAME, null, cv);
            }

            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        db.close();
        return result;
    }

    /**
     * 返回当前已经加载的插件
     *
     * @return
     */
    public static ArrayList<PluginSpec> getInstalledPlugin(Context context) {
        SQLiteDatabase db =
                context.openOrCreateDatabase(PLUGIN_DB_NAME, Context.MODE_PRIVATE, null);

        String cmd = String.format("SELECT * FROM %s;", PLUGIN_TABLE_NAME);
        Cursor c = db.rawQuery(cmd, null);
        c.moveToFirst();

        ArrayList<PluginSpec> specs = new ArrayList<>();
        do {
            int pluginIdIndex = c.getColumnIndex("pluginId");
            if (pluginIdIndex < 0) {
                break;
            }
            int pluginTitleIndex = c.getColumnIndex("pluginTitle");
            if (pluginTitleIndex < 0) {
                break;
            }
            int pluginBinaryIndex = c.getColumnIndex("pluginBinary");
            if (pluginBinaryIndex < 0) {
                break;
            }
            int pluginLaunchFragmentIndex = c.getColumnIndex("pluginLaunchFragment");
            if (pluginLaunchFragmentIndex < 0) {
                break;
            }
            int pluginDescIndex = c.getColumnIndex("pluginDesc");
            if (pluginDescIndex < 0) {
                break;
            }
            int pluginTypeIndex = c.getColumnIndex("pluginType");
            if (pluginTypeIndex < 0) {
                break;
            }
            int pluginForceUpdateIndex = c.getColumnIndex("pluginForceUpdate");
            if (pluginForceUpdateIndex < 0) {
                break;
            }
            int pluginMD5Index = c.getColumnIndex("pluginMD5");
            if (pluginMD5Index < 0) {
                break;
            }


            String pluginId;
            String pluginTitle;
            String pluginBinary;
            String pluginLaunchFragment;
            String pluginDesc;
            String pluginType;
            boolean pluginForceUpdate;
            String pluginMD5;
            while (!c.isAfterLast()) {
                PluginSpec pluginSpec = new PluginSpec();

                try {
                    pluginId = c.getString(pluginIdIndex);
                    pluginSpec.setPluginId(pluginId);

                    pluginTitle = c.getString(pluginTitleIndex);
                    pluginSpec.setTitle(pluginTitle);

                    pluginBinary = c.getString(pluginBinaryIndex);
                    pluginSpec.setPluginBinary(pluginBinary);

                    pluginLaunchFragment = c.getString(pluginLaunchFragmentIndex);
                    pluginSpec.setPluginLaunchUI(pluginLaunchFragment);

                    pluginDesc = c.getString(pluginDescIndex);
                    pluginSpec.setPluginDesc(pluginDesc);

                    pluginType = c.getString(pluginTitleIndex);
                    pluginSpec.setPluginType(pluginType);

                    pluginForceUpdate = Boolean.valueOf(c.getString(pluginForceUpdateIndex));
                    pluginSpec.setPluginForceUpdate(pluginForceUpdate);

                    pluginMD5 = c.getString(pluginMD5Index);
                    pluginSpec.setPluginMD5(pluginMD5);

                    specs.add(pluginSpec);
                    Log.d(TAG, "get:" + pluginSpec);
                } catch (Exception e) {

                }

                c.moveToNext();
            }
        } while (false);

        return specs;
    }

    /**
     * 加载已经安装插件的PluginSpec
     *
     * @param context
     * @param pluginId
     */
    private static PluginSpec getPluginSpec(Context context, String pluginId) {
        SQLiteDatabase db =
                context.openOrCreateDatabase(PLUGIN_DB_NAME, Context.MODE_PRIVATE, null);

        Cursor c = db.rawQuery("SELECT * FROM plugins WHERE pluginId = ?", new String[]{pluginId});
        c.moveToFirst();

        PluginSpec pluginSpec = null;

        do {
            try {
                int index = c.getColumnIndex("pluginId");
                if (index < 0) {
                    break;
                }
                pluginId = c.getString(index);

                index = c.getColumnIndex("pluginTitle");
                if (index < 0) {
                    break;
                }
                String pluginTitle = c.getString(index);

                index = c.getColumnIndex("pluginBinary");
                if (index < 0) {
                    break;
                }
                String pluginBinary = c.getString(index);

                index = c.getColumnIndex("pluginLaunchFragment");
                if (index < 0) {
                    break;
                }
                String pluginLaunchFragment = c.getString(index);

                index = c.getColumnIndex("pluginDesc");
                if (index < 0) {
                    break;
                }
                String pluginDesc = c.getString(index);

                index = c.getColumnIndex("pluginType");
                if (index < 0) {
                    break;
                }
                String pluginType = c.getString(index);

                index = c.getColumnIndex("pluginForceUpdate");
                if (index < 0) {
                    break;
                }
                boolean pluginForceUpdate = Boolean.valueOf(c.getString(index));

                index = c.getColumnIndex("pluginMD5");
                if (index < 0) {
                    break;
                }
                String pluginMD5 = c.getString(index);

                pluginSpec = new PluginSpec();
                pluginSpec.setPluginId(pluginId);
                pluginSpec.setTitle(pluginTitle);
                pluginSpec.setPluginBinary(pluginBinary);
                pluginSpec.setPluginLaunchUI(pluginLaunchFragment);
                pluginSpec.setPluginDesc(pluginDesc);
                pluginSpec.setPluginType(pluginType);
                pluginSpec.setPluginForceUpdate(pluginForceUpdate);
                pluginSpec.setPluginMD5(pluginMD5);
            } catch (Exception e) {

            }
        } while (false);

        c.close();
        db.close();

        return pluginSpec;
    }
}
