package com.soft.nortek.demo;

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
import com.zlw.main.recorderlib.utils.Logger;

import java.io.File;
import java.util.Locale;

public class RecordActivity1 extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = RecordActivity1.class.getSimpleName();
    private ImageButton startRecordBtn,stopRecordBtn;
    private Button playBtn,stopBtn,backBtn;
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

    }

    /**默认为WAV格式文件**/
    private void initEvent() {
        RECORD_ADD = "/data/data/"+ AppUtils.getPackageName(RecordActivity1.this)+"/files/";
        recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
        /**初始化设置audioView的样式**/
        mAudioView.setStyle(AudioView.ShowStyle.getStyle(STYLE_DATA[2]), mAudioView.getDownStyle());
        mAudioView.setStyle(mAudioView.getUpStyle(), AudioView.ShowStyle.getStyle(STYLE_DATA[3]));
    }

    private void initView() {
        backBtn = findViewById(R.id.back);
        titleBarTitle = findViewById(R.id.title_bar_title);
        startRecordBtn = findViewById(R.id.bt_start);
        stopRecordBtn = findViewById(R.id.bt_stop);
        playBtn = findViewById(R.id.play_start_btn);
        stopBtn = findViewById(R.id.play_stop_btn);
        voiceImg = findViewById(R.id.iv_voice_img);
        cutdownTime = findViewById(R.id.com_voice_time);
        mAudioView = findViewById(R.id.audioView);
        tvState = findViewById(R.id.tvState);
        tvSoundSize = findViewById(R.id.tvSoundSize);
        backBtn.setOnClickListener(this);
        startRecordBtn.setOnClickListener(this);
        stopRecordBtn.setOnClickListener(this);
        titleBarTitle.setText("Recording");
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
                Toast.makeText(RecordActivity1.this, "录音文件： " + result.getAbsolutePath(), Toast.LENGTH_SHORT).show();
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
        startRecordBtn.setImageDrawable(getResources().getDrawable(R.mipmap.record_recording_btn));
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
            startRecordBtn.setImageDrawable(getResources().getDrawable(R.mipmap.record_recording_btn));
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
            startRecordBtn.setImageDrawable(getResources().getDrawable(R.mipmap.record_pause_btn));
            isStart = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start:
                doPlay();
                break;
            case R.id.bt_stop:
                doStop();
                break;
            case R.id.back:
                finish();
                break;

                default:
                    break;
        }
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

}
