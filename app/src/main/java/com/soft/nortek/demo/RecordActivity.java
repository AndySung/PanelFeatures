package com.soft.nortek.demo;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordActivity extends Activity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private Button startRecBtn,stopRecBtn,playBtn,pauseBtn,stopBtn,deleteAllFileBtn,BackBtn;
    private TextView recordAddress;
    private MediaRecorder mMediaRecorder;
    ///以文件形式保存
    private File recordFile;
    private RecordPlayer mPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initView();
        viewOnClick();
    }

    private File createRecordFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Record_" + timeStamp + "_";
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File sdCard = Environment.getExternalStorageDirectory();
        File storageDir = new File(sdCard, "Music");
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, ".amr", storageDir);
            Log.i(TAG, String.valueOf(imageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    private void viewOnClick() {
        startRecBtn.setOnClickListener(this);
        stopRecBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        deleteAllFileBtn.setOnClickListener(this);
        BackBtn.setOnClickListener(this);
    }

    private void initView() {
        startRecBtn = findViewById(R.id.start_record_btn);
        stopRecBtn = findViewById(R.id.stop_record_btn);
        playBtn = findViewById(R.id.play_btn);
        pauseBtn = findViewById(R.id.pause_btn);
        stopBtn = findViewById(R.id.stop_play_btn);
        recordAddress = findViewById(R.id.record_address);
        deleteAllFileBtn = findViewById(R.id.delete_all_file);

        BackBtn = findViewById(R.id.record_back);

        //刚开始进去
        stopRecBtn.setVisibility(View.INVISIBLE);
        stopRecBtn.setTextColor(Color.GRAY);

        RePlay(false);

    }


    private void RePlay(boolean isExist){
        if(isExist == true){
            playBtn.setVisibility(View.VISIBLE);
            playBtn.setTextColor(Color.WHITE);
            pauseBtn.setVisibility(View.VISIBLE);
            pauseBtn.setTextColor(Color.WHITE);
            stopBtn.setVisibility(View.VISIBLE);
            stopBtn.setTextColor(Color.WHITE);
        }else{
            playBtn.setVisibility(View.INVISIBLE);
            playBtn.setTextColor(Color.GRAY);
            pauseBtn.setVisibility(View.INVISIBLE);
            pauseBtn.setTextColor(Color.GRAY);
            stopBtn.setVisibility(View.INVISIBLE);
            stopBtn.setTextColor(Color.GRAY);
        }
    }


    @Override
    public void onClick(View view) {
        mPlayer = new RecordPlayer(RecordActivity.this);
        switch (view.getId()){
            case R.id.start_record_btn:
                startRecording();
                break;

            case R.id.stop_record_btn:
                stopRecording();
                break;

            case R.id.play_btn:
                playRecording();
                break;

            case R.id.pause_btn:
                pauseplayer();
                break;

            case R.id.stop_play_btn:
                stopplayer();
                break;

            case R.id.delete_all_file:
                deleteAllFiles(new File(Environment.getExternalStorageDirectory(), "Music"));
                break;
            case R.id.record_back:
                finish();
                break;
                default:
                    break;
        }
    }


    private void startRecording() {
        if(mMediaRecorder != null) {
            playRecording();
            stopplayer();
            mMediaRecorder.release();
            mMediaRecorder = null;
            recordFile = null;
        }

        deleteAllFiles(new File(Environment.getExternalStorageDirectory(), "Music"));

        if(recordFile!=null) {
            recordFile.delete();
        }
        mMediaRecorder = new MediaRecorder();
        ///用来保存文件的路径
        recordFile = createRecordFile();
        Log.i(TAG, "Record address: " + Uri.fromFile(recordFile));

        recordAddress.setText("Record file address:"+recordFile);

        // 判断，若当前文件已存在，则删除
        if (recordFile.exists()) {
            recordFile.delete();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setOutputFile(recordFile.getAbsolutePath());
        try {
            // 准备好开始录音
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            startRecBtn.setVisibility(View.INVISIBLE);
            startRecBtn.setTextColor(Color.GRAY);
            stopRecBtn.setVisibility(View.VISIBLE);
            stopRecBtn.setTextColor(Color.WHITE);
            RePlay(false);
        } catch (IllegalStateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
    }


    private void stopRecording() {
        if (recordFile != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
        }
        startRecBtn.setVisibility(View.VISIBLE);
        startRecBtn.setTextColor(Color.WHITE);
        stopRecBtn.setVisibility(View.INVISIBLE);
        stopRecBtn.setTextColor(Color.GRAY);

        RePlay(true);
    }

    private void playRecording() {
        // 判断，当前文件是否存在
        if (recordFile.exists()) {
            mPlayer.playRecordFile(recordFile);
            Log.i("play---->",recordFile+"");
        }else{
            Toast.makeText(RecordActivity.this,"Record file not exists",Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseplayer() {
        // 判断，当前文件是否存在
        if (recordFile.exists()) {
            mPlayer.pausePalyer();
        }else{
            Toast.makeText(RecordActivity.this,"Record file not exists",Toast.LENGTH_SHORT).show();
        }
    }

    private void stopplayer() {
        // TODO Auto-generated method stub
        // 判断，当前文件是否存在
        if (recordFile.exists()) {
            mPlayer.stopPalyer();
        }else{
            Toast.makeText(RecordActivity.this,"Record file not exists",Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }
}
