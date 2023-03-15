package net.test.openai.adapter;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;



import net.test.openai.R;
import net.test.openai.pojo.MsgInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {
    private List<MsgInfo> allInfo;
    Context context;
    private addClickListener listener;

    public ContentAdapter(List<MsgInfo> allInfo, Context context,addClickListener listener) {
      //  Collections.reverse(allInfo);
        this.allInfo = allInfo;
        this.context = context;
        this.listener = listener;
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView address;
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        ImageView isSend;
        ImageView default_avatar;
        TextView name_tag;
        TextView time_tag;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.send_name);
            leftLayout = itemView.findViewById(R.id.chat_left);
            rightLayout = itemView.findViewById(R.id.chat_right);
            leftMsg = itemView.findViewById(R.id.user_content);
            rightMsg = itemView.findViewById(R.id.me_content);
            isSend = itemView.findViewById(R.id.send_status);
            name_tag = itemView.findViewById(R.id.name_tag);
            default_avatar = itemView.findViewById(R.id.default_avatar);
            time_tag = itemView.findViewById(R.id.time_tag);
            imageView = itemView.findViewById(R.id.mms_left);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MsgInfo all_msg = allInfo.get(position);
        int type = all_msg.getType();
        String msg = all_msg.getMsg();
        Bitmap image = all_msg.getImage();

        if (type==1){
            holder.rightMsg.setText(msg);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
        }else {
            if (image!=null){

                holder.leftMsg.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageBitmap(image);

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bigImageLoader(image,context);
                    }
                });

            }else {

                holder.leftMsg.setText(msg);
                holder.leftMsg.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.GONE);

            }

            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return allInfo.size();
    }
    //方法里直接实例化一个imageView不用xml文件，传入bitmap设置图片
    private void bigImageLoader(Bitmap bitmap,Context context){
        Dialog dialog = new Dialog(context);
        ImageView image = new ImageView(context);
        image.setImageBitmap(bitmap);
        dialog.setContentView(image);
        //将dialog周围的白块设置为透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //显示
        dialog.show();
        //点击图片取消
        image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.cancel();
            }
        });
        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(listener!= null) {
                    listener.addClick(bitmap,image);
                }
                return false;
            }
        });
    }


    public static interface addClickListener{

        public void addClick(Bitmap bitmap,View v);  //自行配置参数  需要传递到activity的值

    }
}
