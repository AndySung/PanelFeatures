package com.soft.nortek.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.soft.nortek.demo.mtouch.DrawView;

public class DrawViewActivity extends Activity implements View.OnClickListener {
    DrawView mDrawView;
    TextView titleBarTitle;
    Button updataWidthBtn;
    boolean isTouch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_view);
        //mDrawView = new DrawView(DrawViewActivity.this,160);
        findViewById(R.id.back).setOnClickListener(v ->  {
            finish();
        });
        mDrawView = findViewById(R.id.palette);
        mDrawView.PaintStrokeWidth(26);
        titleBarTitle = findViewById(R.id.title_bar_title);
        titleBarTitle.setText("multi touch");
        updataWidthBtn = findViewById(R.id.paste_file); //修改画笔的宽度
        updataWidthBtn.setOnClickListener(this);
        updataWidthBtn.setVisibility(View.VISIBLE);
        updataWidthBtn.setText("Smear");
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.paste_file:
               mDrawView.clear();
                 if(isTouch){
                     updataWidthBtn.setText("Smear");
                     mDrawView.PaintStrokeWidth(26);
                     isTouch = false;
                 }else{
                     updataWidthBtn.setText("Touch");
                     mDrawView.PaintStrokeWidth(160);
                     isTouch = true;
                 }
//               AlertDialog.Builder builder=new AlertDialog.Builder(this);
//               builder.setIcon(android.R.drawable.ic_dialog_info);
//               builder.setTitle("Please select a brush type");
//               final String []items=new String[]{"Multi-finger","Single finger"};
//               builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
//                   //which指的是用户选择的条目的下标
//                   //dialog:触发这个方法的对话框
//                   @Override
//                   public void onClick(DialogInterface dialog, int which) {
//                       if(items[which].equals("Multi-finger")){
//                           mDrawView.PaintStrokeWidth(50);
//                       }else {
//                           mDrawView.PaintStrokeWidth(160);
//                       }
//                       dialog.dismiss();//当用户选择了一个值后，对话框消失
//                   }
//               });
//               builder.show();//这样也是一个show对话框的方式，上面那个也可以
               break;
           default:
               break;
       }
    }
}
