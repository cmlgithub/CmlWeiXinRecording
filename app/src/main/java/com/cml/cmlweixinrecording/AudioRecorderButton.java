package com.cml.cmlweixinrecording;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * 作者：陈明亮 on 2016/10/18 16:12
 * 博客：http://blog.csdn.net/zc2_5781
 */

public class AudioRecorderButton extends Button implements AudioManager.AudioStateListener {

    private static final int DISTANCE_Y_CANCEL = 50;
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;//录音
    private static final int STATE_WANT_TO_CANCEL = 3;//取消

    private int mCurState = STATE_NORMAL;
    private boolean isRecording;
    private DialogManager mDialogManager;
    private AudioManager mAudioManager;
    private float mTime;
    //是否触发onLongClick
    private boolean mReady;

    public AudioRecorderButton(Context context) {
        this(context,null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDialogManager = new DialogManager(context);

        //TODO 判断sd卡是否挂载
        String dir = Environment.getExternalStorageDirectory() + "/youzitang";
        mAudioManager = AudioManager.getInstance(dir);
        mAudioManager.setOnAudioStateListener(this);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }

    /**
     * 录音完成后的回掉
     */
    public interface OnAudioFInishRecorderListener{
        void onFinish(float seconds,String filePath);
    }
    private OnAudioFInishRecorderListener mListener;

    public void setOnAudioFInishRecorderListener(OnAudioFInishRecorderListener mListener){
        this.mListener = mListener;
    }

    /**
     * 获取音量大小的Runnable
     */
    private Runnable mGetVoiceLevelRunnable= new Runnable() {
        @Override
        public void run() {
            while (isRecording){
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;//计时
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGE = 0X111;
    private static final int MSG_DILOGS_DIMISS = 0X112;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_AUDIO_PREPARED:
                    //显示应该在audio pre以后
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGE:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVOiceLevel(7));
                    break;
                case MSG_DILOGS_DIMISS:
                    mDialogManager.dimissDialog();
                    break;
            }
        }
    };

    /**
     * 准备完成
     */
    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:

                if(isRecording){
                    //根据x,y的坐标,判断是否想要取消
                    if(wantToCancle(x,y)){
                        changeState(STATE_WANT_TO_CANCEL);
                    }else{
                        changeState(STATE_RECORDING);
                    }
                }


                break;
            case MotionEvent.ACTION_UP:

                if(!mReady){//未触发onLongClick
                    reset();
                    return super.onTouchEvent(event);
                }

                if(!isRecording || mTime<0.6f){//pre没有完成
                    mDialogManager.tooShort();
                    mAudioManager.cancle();
                    mHandler.sendEmptyMessageDelayed(MSG_DILOGS_DIMISS,1300);//tooShor显示1.3s
                }else if(mCurState == STATE_RECORDING){//正常结束
                    //release
                    mDialogManager.dimissDialog();
                    mAudioManager.release();
                    //callbackToAct
                    if(mListener != null){
                        mListener.onFinish(mTime,mAudioManager.getCurrentFilePath());
                    }
                }else if(mCurState == STATE_WANT_TO_CANCEL){//取消
                    //cancel
                    mDialogManager.dimissDialog();
                    mAudioManager.cancle();
                }
                reset();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 恢复状态及标志位
     */
    private void reset() {
        isRecording = false;
        mReady = false;
        mTime = 0;
        changeState(STATE_NORMAL);
    }

    private boolean wantToCancle(int x, int y) {

        if(x<0 || x>getWidth()){
            return true;
        }

        if(y<-DISTANCE_Y_CANCEL || y>getHeight()+DISTANCE_Y_CANCEL){
            return true;
        }

        return false;
    }

    private void changeState(int state) {
        if(mCurState != state){
            mCurState = state;
            switch (state){
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.btn_recorder_normal);
                    setText(R.string.str_recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_recorder_recording);
                    setText(R.string.str_recorder_recording);
                    if(isRecording){
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recorder_recording);
                    setText(R.string.str_recorder_want_cancel);
                    mDialogManager.wantToCancle();
                    break;
            }
        }
    }


}
