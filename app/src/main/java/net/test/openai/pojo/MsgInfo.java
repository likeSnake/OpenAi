package net.test.openai.pojo;

import android.graphics.Bitmap;

public class MsgInfo {
    private String msg;
    private int type;
    private Bitmap image;

    public MsgInfo(String msg, int type,Bitmap image) {
        this.msg = msg;
        this.type = type;
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
