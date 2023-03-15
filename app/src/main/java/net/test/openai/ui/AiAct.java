package net.test.openai.ui;

import static net.test.openai.util.AiUtil.AddUserTalks;
import static net.test.openai.util.AiUtil.chat;
import static net.test.openai.util.AiUtil.getError_info;
import static net.test.openai.util.AiUtil.initChatAi;
import static net.test.openai.util.AiUtil.getImage;
import static net.test.openai.util.AiUtil.installApk;
import static net.test.openai.util.MyUtils.FileSaveToInside;
import static net.test.openai.util.MyUtils.showToast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.test.openai.R;
import net.test.openai.adapter.ContentAdapter;
import net.test.openai.pojo.MsgInfo;
import net.test.openai.util.AiUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class AiAct extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 1024;
    private static int type = 1;

    private ArrayList<MsgInfo> info = new ArrayList<>();
    private TextView msg;
    private ImageView bt_send_message;
    private ImageView add_cx;
    private ContentAdapter adapter;
    private RecyclerView msg_recycler_view;
    private Bitmap save_bitmap = null;
    public static Boolean IS_UPDATE;
    private AiUtil aiUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_act);

        IS_UPDATE = isUpdate();

        init();
    }

    public Boolean isUpdate(){
         aiUtil = new AiUtil();

        Boolean is_update =aiUtil.checkVersion(this, new AiUtil.DownloadCallback() {
            @Override
            public void onComplete(File file) {
                installApk(AiAct.this);
            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onLoading(long count, long current) {
             //   System.out.println("总大小："+count / (1024*1024));
                float FPercent = ((float) current / count) * 100;
                // publishProgress(DOWNLOAD_PROGRESS, len);
                float f = Float.parseFloat(new DecimalFormat(".00").format(FPercent));
                int Progress = (int) FPercent;


                runOnUiThread(() ->{
                    aiUtil.Progress(AiAct.this,Progress,f);
                });
            }
        });
        return is_update;
    }
    private void init(){

        initChatAi();
        msg = findViewById(R.id.msg);
        bt_send_message = findViewById(R.id.bt_send_message);
        msg_recycler_view = findViewById(R.id.msg_recycler_view);
        add_cx = findViewById(R.id.add_cx);
        add_cx.setOnClickListener(this);

        msg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH) && event != null) {
                    //点击搜索要做的操作

                    String s = msg.getText().toString();
                    info.add(new MsgInfo(s,1,null));
                    showResponse();
                    AddUserTalks(s);

                    if (type == 1) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String chat = chat();
                                info.add(new MsgInfo(chat, 0, null));
                                showResponse();

                            }
                        }).start();
                    }else if (type == 2){

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                    Bitmap test = getImage(s);
                                    if (test==null){
                                        String error_info = getError_info();
                                        info.add(new MsgInfo(error_info,0,null));
                                        showResponse();
                                    }else {
                                        info.add(new MsgInfo(null, 0, test));
                                        showResponse();
                                    }

                            }
                        }).start();

                    }

                    msg.clearFocus();
                    hideKeyboard(AiAct.this);
                    msg.setText("");
                    return false;
                }
                return false;
            }
        });
    }
    public static void hideKeyboard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 隐藏软键盘
        imm.hideSoftInputFromWindow(context.getWindow().getDecorView().getWindowToken(), 0);

    }

    private void start(ArrayList<MsgInfo> list){
        LinearLayoutManager manager = new LinearLayoutManager(AiAct.this, LinearLayoutManager.VERTICAL, false);
        adapter = new ContentAdapter(list, this, new ContentAdapter.addClickListener() {
            @Override
            public void addClick(Bitmap bitmap,View v) {
                registerForContextMenu(v);//进行注册
                save_bitmap = bitmap;
            }
        });
        msg_recycler_view.setLayoutManager(manager);
        msg_recycler_view.scrollToPosition(adapter.getItemCount()-2);
        msg_recycler_view.setAdapter(adapter);
    }

    private void showResponse() {
        runOnUiThread(() -> {
            start(info);
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_cx:
                if (type == 1){
                    msg.setHint("图片模式");
                    showToast(this,"图片模式");
                    type = 2;
                }else if (type==2){
                    msg.setHint("聊天模式");
                    showToast(this,"聊天模式");
                    type = 1;
                }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item, menu);
        //menu.add(0,0,0,"保存1");
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.save:
                        System.out.println("开始保存");

                        Long time = System.currentTimeMillis();  //获取当前时间
                        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                        String date = format.format(time);

                        if (save_bitmap!=null) {
                            requestPermission(date, save_bitmap);
                        }else {
                            showToast(AiAct.this,"保存失败！");
                        }
                        break;
                }

                return false;
            }
        });
    }

    private void requestPermission(String name,Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                FileSaveToInside(this,name,bitmap);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                FileSaveToInside(this,name,bitmap);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"储权限已获取",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"请给存储权限，更新应用",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
               // downloadFile(UpdateUrl);
            } else {
                Toast.makeText(this,"请给存储权限，更新应用",Toast.LENGTH_SHORT).show();
            }
        }
        System.out.println(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        if (requestCode == 1001 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 未知来源安装应用权限开启
            System.out.println("请求安装权限反馈");
            boolean haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            System.out.println(haveInstallPermission);
            if (haveInstallPermission) {
                installApk(this);
            }
        }
    }



}