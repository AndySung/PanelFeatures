package com.zlw.main.recorderlib.recorder.player;


import android.app.Activity;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zlw.main.recorderlib.R;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author maple
 * @time 2018/4/23.
 */
public class PlayDialog extends Dialog {
    ImageButton ib_play;
    ImageView iv_cancel;
    SeekBar sb_bar;
    TextView tv_file_name;
    TextView tv_left_time;
    TextView tv_right_time;

    File file;
    MediaPlayer player;
    Activity activity;

    public PlayDialog(Activity activity) {
        super(activity, R.style.CustomDialog);

        this.activity = activity;
        this.getWindow().getAttributes().gravity = Gravity.CENTER;
        this.setCancelable(true);

        initView();
        initListener();
    }

    private void initView() {
        setContentView(R.layout.dialog_play_view);

        ib_play = findViewById(R.id.ib_play);
        iv_cancel = findViewById(R.id.iv_cancel);
        sb_bar = findViewById(R.id.sb_bar);
        tv_file_name = findViewById(R.id.tv_file_name);
        tv_left_time = findViewById(R.id.tv_left_time);
        tv_right_time = findViewById(R.id.tv_right_time);

        tv_left_time.setText("0:00");
        tv_right_time.setText("0:40");
    }

    private void initListener() {
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ib_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPlay();
            }
        });
        sb_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void clickPlay() {
        if (!isPlaying()) {
            startPlaying();
        } else {
            pausePlay();
        }
    }

    public PlayDialog addWavFile(File file) {
        this.file = file;
        try {
            player = new MediaPlayer();
            player.setDataSource(file.getAbsolutePath());
            player.prepare();
            player.setLooping(true);
            // play over call back
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
            tv_file_name.setText(file.getAbsolutePath());
            int duration = player.getDuration();
            sb_bar.setProgress(0);
            sb_bar.setMax(duration);
            tv_right_time.setText(formatTime(duration));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void showDialog() {
        showDialog(true);
    }

    public void showDialog(boolean cancelable) {
        this.setCancelable(cancelable);
        this.show();
    }

    @Override
    public void dismiss() {
        stopPlaying();
        if (player != null)
            player = null;
        super.dismiss();
    }

    public String formatTime(int ms) {
        DateFormat dateFormat = new SimpleDateFormat("m:ss");
        return dateFormat.format(new Date(ms));
    }

    //--------------------------------------------------------------------------

    public void startPlaying() {
        if (player != null) {
            player.start();
            ib_play.setBackgroundResource(R.drawable.ic_pause);
            //----------定时器记录播放进度---------//
            TimerTask mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isPlaying()) {
                                int curPosition = player.getCurrentPosition();
                                sb_bar.setProgress(curPosition);
                                tv_left_time.setText(formatTime(curPosition));
                            }
                        }
                    });
                }
            };
            new Timer().schedule(mTimerTask, 0, 10);
        }
    }

    public void pausePlay() {
        if (player != null) {
            player.pause();
            ib_play.setBackgroundResource(R.drawable.ic_play);
        }
    }

    public void stopPlaying() {
        if (player != null) {
            player.stop();
            player.reset();
            ib_play.setBackgroundResource(R.drawable.ic_play);
        }
    }

    public boolean isPlaying() {
        try {
            return player != null && player.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

}
