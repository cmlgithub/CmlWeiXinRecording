package com.cml.cmlweixinrecording;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 作者：陈明亮 on 2016/10/18 16:47
 * 博客：http://blog.csdn.net/zc2_5781
 */

public class DialogManager {

    public Dialog mDialog;

    private ImageView mIcon;
    private ImageView mVoice;

    private TextView mLable;

    private Context context;

    public DialogManager(Context context){
        this.context = context;
    }

    public void showRecordingDialog(){
        mDialog = new Dialog(context,R.style.Theme_AudioDiao);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_recorder,null);
        mDialog.setContentView(view);
        mIcon = (ImageView) mDialog.findViewById(R.id.id_recorder_dialog_icon);
        mVoice = (ImageView) mDialog.findViewById(R.id.id_recorder_dialog_voice);
        mLable = (TextView) mDialog.findViewById(R.id.id_recorder_dialog_label);
        mDialog.show();
    }

    public void recording(){
        if(mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.mipmap.recorder);
            mLable.setText("手指上滑,取消发送");
        }
    }

    public void wantToCancle(){
        if(mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.mipmap.cancel);
            mLable.setText("松开手指,取消发送");
        }
    }

    public void tooShort(){
        if(mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.mipmap.voice_to_short);
            mLable.setText("录音时间过短");
        }
    }

    public void dimissDialog(){
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
    }

    /**
     * 通过level更新voice上的图片
     * @param level 1-7
     */
    public void updateVoiceLevel(int level){
        if(mDialog != null && mDialog.isShowing()){
            int resId = context.getResources().getIdentifier("v"+level,"mipmap",context.getPackageName());
            mVoice.setImageResource(resId);
        }
    }

}
