package com.soft.nortek.demo.filesmanage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.soft.nortek.demo.R;

public class Files_Manage_Property extends Activity {
    private Button backBtn;
    private TextView titleBatTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files_manage_property);

        Intent intent = getIntent();
        final String name = intent.getStringExtra("Name");
        final String size = intent.getStringExtra("Size");
        final String content = intent.getStringExtra("Content");
        final String lastmodified = intent.getStringExtra("Lastmodified");
        final String path = intent.getStringExtra("Path");

        TextView txtName = findViewById(R.id.file_name);
        TextView txtSize = findViewById(R.id.file_size);
        TextView txtContent = findViewById(R.id.file_content);
        TextView txtContent0 = findViewById(R.id.file_content0);
        TextView txtLastmodified = findViewById(R.id.file_lastmodified);
        TextView txtPath = findViewById(R.id.file_path);
        backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(v -> {
            finish();
        });
        titleBatTitle = findViewById(R.id.title_bar_title);
        titleBatTitle.setText("File attribute");

        txtName.setText(name);
        txtSize.setText(size);
        if(content == null)
        {
            txtContent.setVisibility(View.GONE);
            txtContent0.setVisibility(View.GONE);
        }
        else txtContent.setText(content);
        txtLastmodified.setText(lastmodified);
        txtPath.setText(path);
    }

}
