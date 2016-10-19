package com.cml.cmlweixinrecording;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 作者：陈明亮 on 2016/10/18 17:41
 * 博客：http://blog.csdn.net/zc2_5781
 */

public class AudioManager {

    private MediaRecorder mMediaRecorder;

    private String mDir;

    private String mCurrentFilePath;

    private static AudioManager mInstance;

    private boolean isPrepared;

    private AudioManager(String mDir){
        this.mDir = mDir;
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }


    /**
     * 回调准备完毕
     */
    public interface AudioStateListener{
        void wellPrepared();
    }

    public AudioStateListener mListener;

    public void setOnAudioStateListener(AudioStateListener mListener){
        this.mListener = mListener;
    }

    public static AudioManager getInstance(String mDir){
        if(mInstance == null){
            synchronized (AudioManager.class){
                if(mInstance == null){
                    mInstance = new AudioManager(mDir);
                }
            }
        }

        return mInstance;
    }

    public void prepareAudio(){
        try {
            isPrepared = false;

            File dir = new File(mDir);
            if(!dir.exists())
                dir.mkdirs();

            String fileName = generrateFileName();

            File file = new File(dir, fileName);

            mCurrentFilePath = file.getAbsolutePath();

            mMediaRecorder = new MediaRecorder();
            //设置输出文件
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            //设置音频源
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频格式amr
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            //音频的编码amr
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mMediaRecorder.prepare();
            mMediaRecorder.start();
            //准备结束
            isPrepared = true;
            if(mListener != null){
                mListener.wellPrepared();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 随机生成文件的名称
     */
    private String generrateFileName() {
        return UUID.randomUUID().toString()+".amr";
    }

    public int getVOiceLevel(int maxLevel){
        if(isPrepared){
            try {
                if(mMediaRecorder != null){
                    //mMediaRecorder.getMaxAmplitude()1-32767
                    return maxLevel*mMediaRecorder.getMaxAmplitude()/32768+1;//(1-7)之间的值
                }
            }catch (IllegalStateException e){
                e.printStackTrace();
            }


        }

        return 1;
    }

    public void release(){
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    public void cancle(){
        release();
        if(mCurrentFilePath != null){
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }


    }

}
