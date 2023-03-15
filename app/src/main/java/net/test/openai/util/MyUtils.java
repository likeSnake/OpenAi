package net.test.openai.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyUtils {

    /**
     * 通知媒体库更新文件
     *
     * @param context
     * @param filePath 文件全路径
     */
    public static void scanFile(Context context, String filePath) {
        if (context == null || TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            return;
        }
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

    /**
     * 保存图片到沙盒目录
     * @param fileName 文件名
     * @param bitmap 文件
     * @return 路径，为空时表示保存失败
     */
    public static String FileSaveToInside(Context context,String fileName, Bitmap bitmap) {
        FileOutputStream fos = null;
        String path = null;
        File externalStorage = Environment.getExternalStorageDirectory();
        String ss = externalStorage.getAbsolutePath();
        try {
            File dir = new File(ss + "/Pictures/Ai");
            //System.out.println(getExternalFilesDir(null).getPath()+"myApk");
            if (!dir.exists()) {
                dir.mkdir();
            }
            //创建文件
            File file = new File(dir, fileName+".jpeg");

            fos = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            path = file.getAbsolutePath();

            //通知系统相册更新
            scanFile(context,path);
            Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (fos != null) {
                    //关闭流
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //返回路径
        Log.i("path:",path);
        return path;
    }

    public static void showToast(Context context,String txt){
        Toast toast = Toast.makeText(context, txt, Toast.LENGTH_SHORT);
        toast.setText(txt);
        toast.show();
    }
}
