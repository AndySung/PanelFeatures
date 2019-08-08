package com.soft.nortek.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class PermissionRequest {
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;
    public static final int RECORD_PERMISSION_REQUEST_CODE = 2;
    public static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;


    Activity activity;

    public PermissionRequest(Activity activity) {
        this.activity = activity;
    }

    public void requestPermissionForCamera(){
        //第一次申请权限被拒后每次进入activity就会调用，或者用户之前允许了，之后又在设置中去掉了该权限
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)){
            Toast.makeText(activity, "Camera permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    public boolean checkPermissionForCamera(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForRecord() {
        //第一次申请权限被拒后每次进入activity就会调用，或者用户之前允许了，之后又在设置中去掉了该权限
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)){
            Toast.makeText(activity, "Record permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.RECORD_AUDIO},RECORD_PERMISSION_REQUEST_CODE);
        }
    }

    public boolean checkPermissionForRecord(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForWriteExternalStorage() {
        //第一次申请权限被拒后每次进入activity就会调用，或者用户之前允许了，之后又在设置中去掉了该权限
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(activity, "Write_external_storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    public boolean checkPermissionForWriteExternalStorage(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

}
