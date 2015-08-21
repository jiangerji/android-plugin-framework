package cn.iam007.plugin.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by Administrator on 2015/8/17.
 */
public class PluginUtils {
    private final static String TAG = PluginUtils.class.getName();

    private final static String PLUGINS_DIR = ".plugins";
    private static File mPluginDir = null;

    public static void init(Context context) {
        File dir = new File(Environment.getExternalStorageDirectory(), PLUGINS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        mPluginDir = dir;
    }

    public static void uploadPlugins(final Context context) {
        // 读取目录下的所有的.plg文件
        String[] files = mPluginDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".plg")) {
//                    uploadPlugins(context, new File(dir, filename));
                    return true;
                } else {
                    return false;
                }
            }
        });

        Log.d(TAG, "Read Plugins:");
        for (String file : files) {
            Log.d(TAG, "  plugin:" + file);
        }
    }

    /**
     * 配置插件到测试环境
     *
     * @param context
     * @param file
     */
//    private static void uploadPlugins(Context context, File file) {
//        if (file.isFile()) {
//            /**
//             *  1.  上传plg文件
//             *  2.  上传plg图片
//             *  3.  更新plg配置
//             */
//            do {
//                // 0. 获取plg配置信息
//                AVObject plugConfig = getPlugConfig(file.getAbsolutePath());
//                if (plugConfig == null) {
//                    Log.d(TAG, "不需要更新插件:" + file);
//                    break;
//                }
//
//                // 1.  上传plg文件
//                AVFile plugFile;
//                try {
//                    plugFile = AVFile.withFile(file.getName(), file);
//                    AVACL acl = new AVACL();
//                    acl.setPublicWriteAccess(false);
//                    acl.setPublicReadAccess(true);
//                    plugFile.setACL(acl);
//                    plugFile.save();
//                } catch (IOException e) {
//                    Log.d(TAG, "读取" + file + "失败:" + e.getMessage());
//                    break;
//                } catch (AVException e) {
//                    Log.d(TAG, "上传" + file + "失败:" + e.getMessage());
//                    break;
//                }
//
//                PackageManager pm = context.getPackageManager();
//                PackageInfo info =
//                        pm.getPackageArchiveInfo(file.getAbsolutePath(),
//                                PackageManager.GET_ACTIVITIES);
//                ApplicationInfo appInfo = info.applicationInfo;
//                appInfo.sourceDir = file.getAbsolutePath();
//                appInfo.publicSourceDir = file.getAbsolutePath();
//                Log.d(TAG, "Plugin Label:" + appInfo.loadLabel(pm));
//
//                Drawable appIcon = appInfo.loadIcon(pm);
//                File plugIconFile = new File(file.getParentFile(), file.getName() + ".png");
//                AVFile plugIcon;
//                if (draw2File(appIcon, plugIconFile)) {
//                    // 2.  上传plg图片
//                    try {
//                        plugIcon = AVFile.withFile(plugIconFile.getName(), plugIconFile);
//                        AVACL acl = new AVACL();
//                        acl.setPublicWriteAccess(false);
//                        acl.setPublicReadAccess(true);
//                        plugIcon.setACL(acl);
//                        plugIcon.save();
//                    } catch (IOException e) {
//                        Log.d(TAG, "读取 plug icon 失败:" + e.getMessage());
//                        break;
//                    } catch (AVException e) {
//                        Log.d(TAG, "上传 plug icon 失败:" + e.getMessage());
//                        break;
//                    }
//                } else {
//                    // plg没有设置图片
//                    break;
//                }
//
//                // 4. plg的version code
//                int versionCode = info.versionCode;
//
//                // -1.  上传plg配置
//                if (plugConfig != null) {
//                    // 插件的版本号
//                    plugConfig.put("version", versionCode);
//                    // 插件的名称
//                    plugConfig.put("name", appInfo.loadLabel(pm));
//                    // 插件icon url地址
//                    plugConfig.put("icon", plugIcon.getUrl());
//                    // 插件下载地址
//                    plugConfig.put("url", plugFile.getUrl());
//                    // 插件安装文件大小
//                    plugConfig.put("size", plugFile.getSize());
//
//                    try {
//                        plugConfig.save();
//                    } catch (AVException e) {
//                        Log.d(TAG, "上传plg配置失败！");
//                        e.printStackTrace();
//                    }
//                }
//            } while (false);
//        }
//    }
//
//    public static PluginItem getPluginItem(String pluginFile) {
//        PluginItem item = null;
//        AssetManager am;
//        do {
//            // 计算plg文件md5值
//            String md5 = MD5.getFileMd5(new File(pluginFile));
//            if (md5 == null) {
//                // 获取md5失败
//                Log.d(TAG, "获取MD5值失败!");
//                break;
//            }
//
//            String pluginId;
//            try {
//                am = AssetManager.class.newInstance();
//                am.getClass().getMethod("addAssetPath", String.class).invoke(am, pluginFile);
//                InputStream is = am.open("plugin.json");
//                byte[] buffer = new byte[is.available()];
//                is.read(buffer);
//                JSONObject object = new JSONObject(new String(buffer, "utf-8"));
//
//                item = new PluginItem(object);
//            } catch (Exception e) {
//                Log.d(TAG, "获取插件信息失败：" + e.getMessage());
//                break;
//            }
//        } while (false);
//
//        return item;
//    }
//
//    /**
//     * 获取插件的配置信息，如果返回空则表示插件不需要更新
//     *
//     * @param plugFile
//     * @return
//     */
//    private static AVObject getPlugConfig(String plugFile) {
//        AVObject plugConfig = null;
//        AssetManager am = null;
//        do {
//            // 3. 计算plg文件md5值
//            String md5 = MD5.getFileMd5(new File(plugFile));
//            if (md5 == null) {
//                // 获取md5失败
//                Log.d(TAG, "获取MD5值失败!");
//                break;
//            }
//
//            try {
//                am = AssetManager.class.newInstance();
//                am.getClass().getMethod("addAssetPath", String.class).invoke(am, plugFile);
//                InputStream is = am.open("plugin.json");
//                byte[] buffer = new byte[is.available()];
//                is.read(buffer);
//                JSONObject object = new JSONObject(new String(buffer, "utf-8"));
//
//                // 插件id, 唯一标示符
//                String id = object.getString("id");
//                AVQuery<AVObject> query = new AVQuery<>("Plugins");
//                query.whereEqualTo("pluginId", id);
//                query.setLimit(1);
//                try {
//                    plugConfig = query.getFirst();
//                    if (plugConfig == null) {
//                        plugConfig = new AVObject("Plugins");
//                    } else {
//                        if (md5.equals(plugConfig.getString("md5"))) {
//                            plugConfig = null;
//                            break;
//                        }
//                    }
//                } catch (AVException avException) {
//                    plugConfig = new AVObject("Plugins");
//                } finally {
//                    if (plugConfig != null) {
//                        plugConfig.put("pluginId", id);
//                    }
//                }
//
//                // 插件安装文件的md5值
//                plugConfig.put("md5", md5);
//                // 插件的类型
//                plugConfig.put("type", object.getString("type"));
//                // 插件的描述
//                plugConfig.put("desc", object.getString("desc"));
//                // 该版本插件是否需要强制更新
//                plugConfig.put("forceUpdate", object.getString("forceUpdate"));
//            } catch (Exception e) {
//                // 插件配置有问题
//                Log.d(TAG, "Plugin Config Error:" + e.getMessage());
//                plugConfig = null;
//                break;
//            }
//        } while (false);
//
//        return plugConfig;
//    }

    /**
     * **************** 工具类接口 *****************
     */
    public static boolean draw2File(Drawable drawable, File file) {
        boolean result = false;
        if (drawable != null && file != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap.Config config =
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                result = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            bitmap.recycle();
        }
        return result;
    }
}
