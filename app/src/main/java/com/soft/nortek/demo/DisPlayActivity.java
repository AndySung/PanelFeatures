package com.soft.nortek.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class DisPlayActivity extends Activity {
    private ImageView colorImg;
    private Button displayBackBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        colorImg = findViewById(R.id.color_img);
        colorImg.setImageResource(R.mipmap.colors);

        displayBackBtn = findViewById(R.id.display_back);

        displayBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }
}
