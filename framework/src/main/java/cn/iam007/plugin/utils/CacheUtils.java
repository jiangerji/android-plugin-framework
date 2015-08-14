package cn.iam007.plugin.utils;

import android.content.Context;

import java.io.File;

/**
 * Created by Administrator on 2015/8/14.
 */
public class CacheUtils {

    public static File getPluginDir(Context context) {
        return context.getCacheDir();
    }
}
