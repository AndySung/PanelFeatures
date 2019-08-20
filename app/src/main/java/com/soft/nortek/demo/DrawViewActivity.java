package com.soft.nortek.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.soft.nortek.demo.mtouch.DrawView;

public class DrawViewActivity extends Activity {
    DrawView mDrawView;
    TextView titleBarTitle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_view);
        mDrawView = new DrawView(DrawViewActivity.this);
        findViewById(R.id.back).setOnClickListener(v ->  {
            finish();
        });
        titleBarTitle = findViewById(R.id.title_bar_title);
        titleBarTitle.setText("multi touch");
    }
}
