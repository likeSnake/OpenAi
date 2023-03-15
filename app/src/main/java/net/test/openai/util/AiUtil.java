package net.test.openai.util;

import static net.test.openai.ui.AiAct.REQUEST_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import net.test.openai.R;
import net.test.openai.pojo.VersionInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class AiUtil {
    private DownloadCallback downloadCallback;
    private static String Error_info = "";
    public static Boolean IS_DOWNLOAD = false;

    private static   Map<String, Object> paramMap = new HashMap<>();
    private static List<Map<String, String>> dataList = new ArrayList<>();
    private static String apiKey = "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";
    private static String chatEndpoint = "https://api.openai.com/v1/chat/completions";

    private AlertDialog dialog;
    private Button update;
    private Button installApk;
    private TextView versionContent;

    private ProgressBar mProgressBar;

    private TextView upDownText;
    private LinearLayout updateLayout;
    private LinearLayout update_percent;
    private LinearLayout installApkLayout;
    /**
     * 发送消息
     *
     * @return {@link String}
     */
    public static String chat() {

        JSONObject message = null;

        try {
            String body = HttpRequest.post(chatEndpoint)
                    .header("Authorization", "Bearer "+apiKey)
                    .header("Content-Type", "application/json")
                    .body(JsonUtils.toJson(paramMap))
                    .execute()
                    .body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject result = choices.get(0, JSONObject.class, Boolean.TRUE);
            message = result.getJSONObject("message");
        } catch (HttpException | ConvertException |IORuntimeException e) {
            setError_info(e.toString());
            return null;
        }
        return message.getStr("content");
    }

    public static void AddUserTalks(String s){
        HashMap<String, String> map = new HashMap<>();
        map.put("role", "user");
        map.put("content", s);
        AddAllTalks(map);

    }


    public static void initChatAi(){
        apiKey = "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";
        HashMap<String, String> map = new HashMap<>();
        map.put("role", "system");
        map.put("content", "你是ChatGPT，一个由OpenAI训练的大型语言模型。回答尽可能简洁");
       // map.put("content", "你是蒋玲玲的贴心助手，回答尽可能简洁");
        dataList.add(map);
        paramMap.put("model", "gpt-3.5-turbo");
        paramMap.put("temperature", 0.2);
        paramMap.put("max_tokens", 2048);
    }

    public static void AddAllTalks(HashMap<String, String> map){
        dataList.add(map);
        paramMap.put("messages", dataList);
    }
    public static Bitmap getImage(String txt){
        String body = "";
        Map<String, Object> map_test = new HashMap<>();
        map_test.put("prompt",txt);
        map_test.put("n",1);
        map_test.put("size","1024x1024");
        System.out.println(JsonUtils.toJson(map_test));

        String images = "https://api.openai.com/v1/images/generations";
        try {
         body = HttpRequest.post(images)
                .header("Authorization", "Bearer "+apiKey)
                .header("Content-Type", "application/json")
                .body(JsonUtils.toJson(map_test))
                .execute()
                .body();

        }catch (IORuntimeException e){
            setError_info(e.toString());
            return null;
        }
        System.out.println(body);
        JSONObject jsonObject = JSONUtil.parseObj(body);
        JSONObject error = jsonObject.getJSONObject("error");
        if (error==null){

            JSONArray data = jsonObject.getJSONArray("data");
            JSONObject js = data.get(0, JSONObject.class, Boolean.TRUE);
            String url = js.getStr("url");
            System.out.println(url);

            try {
                URL urls = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urls.openConnection();
                conn.setConnectTimeout(5000);

                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    System.out.println("ok");
                    InputStream inputStream = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                }

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            String error_message = error.getStr("message");
            setError_info(error_message);
            return null;
        }
        return null;
    }
    public static void setError_info(String s){
        Error_info = s;
    }
    public static String getError_info(){
        return Error_info;
    }

    public Boolean checkVersion(Context context,DownloadCallback downloadCallback){
        final Boolean[] isUpdate = {false};
        this.downloadCallback = downloadCallback;
        PackageManager manager =context.getPackageManager();
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    VersionInfo version = HttpUtils.getVersion(new URL("https://ai.ecycat.top/down/version"));
                    String version_content = version.getVersion_content();
                    int NewVersionCode = Integer.parseInt(version.getVersionName());
                    PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                    int AppVersionCode = info.versionCode;
                    System.out.println(AppVersionCode);
                    if (NewVersionCode > AppVersionCode) {
                        System.out.println("需要更新");
                        isUpdate[0] = true;
                        Activity activity = (Activity)context;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(version_content);
                                showUpdateDialog(context,version_content,version.getUrl());
                            }
                        });

                    }else {
                        isUpdate[0] = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();
        return isUpdate[0];
    }

    @SuppressLint("PrivateResource")
    private void showUpdateDialog(Context context, String content,String url) {
        // 创建一个 dialogView 弹窗
        AlertDialog.Builder builder = new
                AlertDialog.Builder(context);
        dialog = builder.create();
        View dialogView = null;
        //设置对话框布局
        dialogView = View.inflate(context,
                R.layout.update_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(com.google.android.material.R.color.mtrl_btn_transparent_bg_color);
        dialog.show();
        // 获取布局控件
        versionContent = dialogView.findViewById(R.id.versionContent);
        mProgressBar = (ProgressBar) dialogView.findViewById(R.id.id_progress);
        update =  dialogView.findViewById(R.id.update);
        versionContent.setText(Html.fromHtml(content));
        upDownText = dialogView.findViewById(R.id.upDownText);
        updateLayout = dialogView.findViewById(R.id.updateLayout);
        update_percent = dialogView.findViewById(R.id.update_percent);
        installApk = dialogView.findViewById(R.id.installApk);
        installApkLayout = dialogView.findViewById(R.id.installApkLayout);
        installApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installApk(context);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IS_DOWNLOAD) {
                    installApk(context);
                }else {
                    requestPermission(context, url);
                    //dialog.dismiss();
                }
            }
        });
    }
    /**
     * 安装最新Apk
     */
    public static void installApk(Context context) {
        // 文件绝对路径
        File file = new File(context.getExternalFilesDir(null).getPath()+"myApk/ai.apk");
        System.out.println(file);
        try {
            // 这里有文件流的读写，需要处理一下异常
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24
                String packageName = context.getApplicationContext().getPackageName();
                String authority = packageName + ".provider";
                Uri uri = FileProvider.getUriForFile(context, authority, file);
                System.out.println(uri.toString());
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            } else {
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            }
            Activity activity = (Activity)context;
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPermission(Context context,String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                updateLayout.setVisibility(View.GONE);
                update_percent.setVisibility(View.VISIBLE);
                downloadFile(context,url);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                ((Activity)(context)).startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadFile(context,url);
                updateLayout.setVisibility(View.GONE);
                update_percent.setVisibility(View.VISIBLE);
            } else {
                ActivityCompat.requestPermissions(((Activity)(context)), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        } else {
            downloadFile(context,url);
            updateLayout.setVisibility(View.GONE);
            update_percent.setVisibility(View.VISIBLE);
        }
    }

    private void downloadFile(Context context,String fileDownLoad_path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        // 下载文件
                        HttpURLConnection conn = (HttpURLConnection) new URL(fileDownLoad_path).openConnection();
                        conn.setConnectTimeout(5 * 1000);
                        conn.setReadTimeout(5000);
                        conn.setRequestMethod("GET");
                        //  conn.connect();
                        if (conn.getResponseCode() == 200) {
                            System.out.println("开始下载");
                            // String _express = FileUtility.getExtensionFromFilename("");
                            InputStream is = conn.getInputStream();
                            //     is.skip()
                            int length = conn.getContentLength();

                            //创建文件路径
                            File dir = new File(context.getExternalFilesDir(null).getPath() + "myApk");
                            //System.out.println(getExternalFilesDir(null).getPath()+"myApk");
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            //创建文件
                            File file = new File(dir + "/" + "ai.apk");
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            FileOutputStream fos = new FileOutputStream(file);
                            long completeLen = 0;
                            int len = 0;
                            byte[] buffer = new byte[1024];
                            while ((len = is.read(buffer)) != -1) {
                                completeLen += len;

                                if (downloadCallback!=null) {
                                    downloadCallback.onLoading(length,completeLen);
                                }

                                fos.write(buffer, 0, len);
                                fos.flush();

                                if (completeLen == length){
                                    if (downloadCallback!=null) {
                                        downloadCallback.onComplete(file);
                                        IS_DOWNLOAD = true;

                                        ((Activity)(context)).runOnUiThread(() ->{
                                            update.setText("安装");
                                            updateLayout.setVisibility(View.VISIBLE);
                                            update_percent.setVisibility(View.GONE);
                                        });

                                    }
                                }
                            }

                            fos.close();
                            is.close();
                        }
                    }else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void Progress(Context context,int mProgress,Float FPercent){
        String upPercent = String.format(context.getResources().getString(R.string.upDown), FPercent);
        upDownText.setText(upPercent);
        mProgressBar.setProgress(mProgress);
    }

    public static interface DownloadCallback {
        /**
         * 下载成功
         * @param file 目标文件
         */
        void onComplete(File file);

        /**
         * 下载失败
         * @param e
         */
        void onError(Exception e);

        /**
         * 下载中
         * @param count 总大小
         * @param current 当前下载的进度
         */
        void onLoading(long count, long current);
    }

}
