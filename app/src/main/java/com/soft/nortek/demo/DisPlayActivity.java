package com.soft.nortek.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DisPlayActivity extends Activity {
    private ImageView colorImg;
    private Button displayBackBtn;
    private TextView titleBatTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        colorImg = findViewById(R.id.color_img);
        colorImg.setImageResource(R.mipmap.colors);

        titleBatTitle = findViewById(R.id.title_bar_title);
        titleBatTitle.setText("Colors");

        displayBackBtn = findViewById(R.id.back);

        displayBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }
}
