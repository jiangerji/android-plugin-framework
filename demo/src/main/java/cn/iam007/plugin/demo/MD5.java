package cn.iam007.plugin.demo;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Created by Administrator on 2015/8/17.
 */
public class MD5 {
    private final static char HEX_DIGITS[] =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getFileMd5(File file) {
        FileInputStream fis;
        String sString;
        char str[] = new char[16 * 2];
        int k = 0;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5Utils");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[2048];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] b = md.digest();

            for (int i = 0; i < 16; i++) {
                byte byte0 = b[i];
                str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
                str[k++] = HEX_DIGITS[byte0 & 0xf];
            }
            fis.close();
            sString = new String(str);

            return sString;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
