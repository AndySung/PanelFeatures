package com.soft.nortek.demo;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * 新建一个RecordPlayer类，用来播放保存好的录音
 * **/
public class RecordPlayer {
    private static MediaPlayer mediaPlayer;
    private Context mcontext;
    public RecordPlayer(Context context) {
        this.mcontext = context;
    }
    // 播放录音文件
    public void playRecordFile(File file) {
        if (file.exists() && file != null) {
            if (mediaPlayer == null) {
                Uri uri = Uri.fromFile(file);
                mediaPlayer = MediaPlayer.create(mcontext, uri);
            }
            mediaPlayer.start();
            //监听MediaPlayer播放完成
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer paramMediaPlayer) {
                    // TODO Auto-generated method stub
                    //弹窗提示
                    Toast.makeText(mcontext, mcontext.getResources().getString(R.string.ok),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    // 暂停播放录音
    public void pausePalyer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.e("TAG", "暂停播放");
        }
    }
    // 停止播放录音
    public void stopPalyer() {
        // 这里不调用stop()，调用seekto(0),把播放进度还原到最开始
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            Log.e("TAG", "停止播放");
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
