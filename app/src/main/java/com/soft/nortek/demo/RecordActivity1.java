package com.soft.nortek.demo;

import android.media.AudioFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.soft.nortek.demo.recordmanage.AudioView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.listener.RecordFftDataListener;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;
import com.zlw.main.recorderlib.recorder.listener.RecordSoundSizeListener;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;
import com.zlw.main.recorderlib.recorder.player.PlayDialog;
import com.zlw.main.recorderlib.utils.Logger;

import java.io.File;
import java.util.Locale;

public class RecordActivity1 extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = RecordActivity1.class.getSimpleName();
    private ImageButton startRecordBtn,stopRecordBtn,listRecordBtn;
    private Button backBtn;
    private ImageView voiceImg;
    private Chronometer cutdownTime;
    private AudioView mAudioView;
    private TextView tvState,tvSoundSize,titleBarTitle;

    private boolean isStart = false;
    private boolean isPause = false;
    private long curBase = 0;
    final RecordManager recordManager = RecordManager.getInstance();
    private static final String[] STYLE_DATA = new String[]{"STYLE_ALL", "STYLE_NOTHING", "STYLE_WAVE", "STYLE_HOLLOW_LUMP"};
    private static String RECORD_ADD = "";
    private File recordFile;    //录音文件


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record1);
        initView();
        initEvent();
        initRecord();
        /***请求录音权限**/
        AndPermission.with(this).runtime().permission(new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.RECORD_AUDIO}).start();
        stopRecordBtn.setEnabled(false);
        stopRecordBtn.setImageResource(R.mipmap.record_done_un_enable_btn);
    }

    /**默认为WAV格式文件**/
    private void initEvent() {
        RECORD_ADD = "/data/data/"+ AppUtils.getPackageName(RecordActivity1.this)+"/files/record/";
        recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
        ///音频采样频率（8000，16000，44100）
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(16000));
        ///设置单通道
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setChannelConfig(AudioFormat.CHANNEL_IN_MONO));
        ///音宽位置设置8bit（8bit，16bit）
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
        /**初始化设置audioView的样式**/
        mAudioView.setStyle(AudioView.ShowStyle.getStyle(STYLE_DATA[3]), mAudioView.getDownStyle());
        mAudioView.setStyle(mAudioView.getUpStyle(), AudioView.ShowStyle.getStyle(STYLE_DATA[3]));
    }

    private void initView() {
        backBtn = findViewById(R.id.back);
        titleBarTitle = findViewById(R.id.title_bar_title);
        startRecordBtn = findViewById(R.id.bt_start);
        stopRecordBtn = findViewById(R.id.bt_stop);
        voiceImg = findViewById(R.id.iv_voice_img);
        cutdownTime = findViewById(R.id.com_voice_time);
        mAudioView = findViewById(R.id.audioView);
        tvState = findViewById(R.id.tvState);
        tvSoundSize = findViewById(R.id.tvSoundSize);
        listRecordBtn = findViewById(R.id.file_list);
        backBtn.setOnClickListener(this);
        startRecordBtn.setOnClickListener(this);
        stopRecordBtn.setOnClickListener(this);
        titleBarTitle.setText("Recording");
        listRecordBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        doStop();
        initRecordEvent();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doStop();
    }

    private void initRecord() {
        recordManager.init(BaseApplication.getInstance(), BuildConfig.DEBUG);
        recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
        recordManager.changeRecordDir(RECORD_ADD);
        initRecordEvent();
    }

    private void initRecordEvent() {
        recordManager.setRecordStateListener(new RecordStateListener() {
            @Override
            public void onStateChange(RecordHelper.RecordState state) {
                Logger.i(TAG, "onStateChange %s", state.name());
                switch (state) {
                    case PAUSE:
                        tvState.setText("暂停中");
                        break;
                    case IDLE:
                        tvState.setText("空闲中");
                        break;
                    case RECORDING:
                        tvState.setText("录音中");
                        break;
                    case STOP:
                        tvState.setText("停止");
                        break;
                    case FINISH:
                        tvState.setText("录音结束");
                        tvSoundSize.setText("---");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(String error) {
                Logger.i(TAG, "onError %s", error);
            }
        });
        recordManager.setRecordSoundSizeListener(new RecordSoundSizeListener() {
            @Override
            public void onSoundSize(int soundSize) {
                tvSoundSize.setText(String.format(Locale.getDefault(), "声音大小：%s db", soundSize));
                animateVoice((float)(soundSize/ 200.0));
            }
        });
        recordManager.setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                recordFile = result;
                Toast.makeText(RecordActivity1.this, "录音文件： " + result.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                listRecordBtn.setVisibility(View.VISIBLE);
            }
        });
        recordManager.setRecordFftDataListener(new RecordFftDataListener() {
            @Override
            public void onFftData(byte[] data) {
                mAudioView.setWaveData(data);
            }
        });
    }

    private void doStop() {
        recordManager.stop();
        //startRecordBtn.setText("Start");
        startRecordBtn.setImageResource(R.mipmap.record_recording_btn);
        voiceImg.setImageResource(R.drawable.mic_default);
        curBase = 0;
        cutdownTime.stop();
        animateVoice(0f);
        isPause = false;
        isStart = false;
    }

    private void doPlay() {
        if (isStart) {
            recordManager.pause();
            //startRecordBtn.setText("Start");
            startRecordBtn.setImageResource(R.mipmap.record_recording_btn);
            curBase = SystemClock.elapsedRealtime() - cutdownTime.getBase();
            cutdownTime.stop();
            voiceImg.setImageResource(R.drawable.mic_default);
            animateVoice(0f);
            isPause = true;
            isStart = false;
        } else {
            if (isPause) {
                recordManager.resume();
            } else {
                recordManager.start();
            }
            voiceImg.setImageResource(R.drawable.mic_selected);
            cutdownTime.setBase(SystemClock.elapsedRealtime() - curBase);
            cutdownTime.start();
            //startRecordBtn.setText("Pause");
            startRecordBtn.setImageResource(R.mipmap.record_pause_btn);
            isStart = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start:
                doPlay();
                deleteFile(new File(RECORD_ADD));
                stopRecordBtn.setEnabled(true);
                stopRecordBtn.setImageResource(R.mipmap.record_done_btn);
                break;
            case R.id.bt_stop:
                doStop();
                stopRecordBtn.setEnabled(false);
                stopRecordBtn.setImageResource(R.mipmap.record_done_un_enable_btn);
                break;
            case R.id.back:
                finish();
                break;
            case R.id.file_list:
                if (recordFile != null && recordFile.exists()) {
                    dialogPlay(recordFile);
                    Logger.i("play---->",recordFile+"");
                }else{
                    Toast.makeText(RecordActivity1.this,"Record file not exists",Toast.LENGTH_SHORT).show();
                }
                break;

                default:
                    break;
        }
    }


    private void dialogPlay(File file){
        new PlayDialog(RecordActivity1.this).addWavFile(file).showDialog();
    }

    /**判断文件夹是否存在如果存在返回true，不存在则新建文件夹**/
    boolean isFolderExists(String strFolder) {
        File file = new File(strFolder);
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**删除文件夹所有内容**/
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

    private void animateVoice(Float maxPeak) {
        if (maxPeak < 0f || maxPeak > 0.5f) {
            return;
        }
        voiceImg.animate()
                .scaleX(1 + maxPeak)
                .scaleY(1 + maxPeak)
                .setDuration(10)
                .start();
    }


    //flie：要删除的文件夹的所在位置
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            Logger.i("Record-file-count","文件数量："+files.length);
            if(files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    deleteFile(f);
                }
                Logger.i("Record--deletefile", "删除文件夹里面的");
            }else{
                Logger.i("Record--deletefile", "文件夹还没有文件");
            }
            //file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
            Logger.i("Record--is-exists","删除文件");
        }
    }

}
