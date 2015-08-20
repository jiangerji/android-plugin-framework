package cn.iam007.plugin.utils;

import java.io.File;

/**
 * 文件工具类
 */
public class FileUtils {

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void rmDir(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                rmDir(f);
            }
            file.delete();
        }
    }

    /**
     * 清空目录，但不删除根目录
     *
     * @param path 需要清空的目录路径
     */
    public static void cleanDir(String path) {
        cleanDir(new File(path));
    }

    /**
     * 清空目录，但不删除根目录
     *
     * @param file 需要清空的目录路径
     */
    public static void cleanDir(File file) {
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                return;
            }
            for (File f : childFile) {
                rmDir(f);
            }
        }
    }
}
