package com.soft.nortek.demo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.soft.nortek.demo.wifiiperf.CommandHelper;
import com.soft.nortek.demo.wifiiperf.CommandResult;
import com.tj24.easywifi.wifi.WifiConnector;
import com.tj24.easywifi.wifi.WifiUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = getClass().getSimpleName();
    private ImageView mPicture;
    //private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    //private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //private static final int REQUEST_PERMISSION_CODE = 267;
    private static final int TAKE_PHOTO = 189;
    private static final int CHOOSE_PHOTO = 385;
    private static final String FILE_PROVIDER_AUTHORITY = "com.soft.nortek.demo.fileprovider";
    private Uri mImageUri, mImageUriFromFile;
    private File imageFile;
    private Button WifiBtn,CameraBtn,RecordBtn,PlayBtn,TouchBtn,DisplayBtn,PlayRecBtn,IperfBtn,ResetBtn;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private File recAudioFile;
    private MediaRecorder mMediaRecorder;
    private WifiManager wifiManager;
    private TextView ipAddress,mShroughput;
    private WifiManager mWm;
    private WifiConnector connector;

    private static final int IPERF_ERROR = 1;
    private static final int IPERF_SCCESS = 2;
    /*** 1. 拷贝iperf到该目录下*/
    private static final String IPERF_PATH = "/data/data/com.soft.nortek.demo/iperf";
//    private static String curIperfCmd = IPERF_PATH + " -s &";
    private static String curIperfCmd = IPERF_PATH + " -s -t 5 -p 8888";

    private String iperfreply;
    private boolean IPERF_OK = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        viewOnClick();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //mWifiAdmin = new WifiAdmin(wifiManager);
//        /*申请读取存储的权限*/
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(PERMISSION_WRITE_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{PERMISSION_WRITE_STORAGE}, REQUEST_PERMISSION_CODE);
//            }
//        }
//        //权限判断，如果没有权限就请求权限
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        } else {
            initMediaPlayer();//初始化播放器 MediaPlayer
//        }
//        getPermission();
        /**将iperf文件拷贝到别处**/
        File file = new File(IPERF_PATH);
        Log.i(TAG,"file.exists(): "+file.exists());
        if(!file.exists()){
            copyiperf();
        }

//        ConnectivityManager manager = (ConnectivityManager)MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
//        if (activeNetwork != null) { // connected to the internet
//            if (activeNetwork.isConnected()) {
//                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                    // connected to wifi
//
//                    Log.e(TAG, "当前WiFi连接可用 ");
//                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                    // connected to the mobile provider's data plan
//
//                    Log.e(TAG, "当前移动网络连接可用 ");
//                }
//            } else {
//                Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
//            }
//        } else {   // not connected to the internet
//            Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
//
//        }
        checkState_23orNew();

    }

    public void checkState_23orNew(){
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        //获取所有网络连接的信息
        Network[] networks = connMgr.getAllNetworks();
        //用于存放网络连接信息
        StringBuilder sb = new StringBuilder();
        //通过循环将网络信息逐个取出来
        for (int i=0; i < networks.length; i++){
            //获取ConnectivityManager对象对应的NetworkInfo对象
            NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
            sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
            Toast.makeText(MainActivity.this,sb,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //检查权限
        PermissionRequest pm_request = new PermissionRequest(MainActivity.this);
        if(!pm_request.checkPermissionForCamera()){
            pm_request.requestPermissionForCamera();
        }
        if(!pm_request.checkPermissionForRecord()){
            pm_request.requestPermissionForRecord();
        }
        if(!pm_request.checkPermissionForWriteExternalStorage()){
            pm_request.requestPermissionForWriteExternalStorage();
        }
        ipAddress.setText("Wifi IP Address:"+getLocalIpAddress());
    }

    //初始化播放器 MediaPlayer
    private void initMediaPlayer() {
        try {
            //File file = new File(Environment.getExternalStorageDirectory(), "max_1k.wav");
            File file = new File("/mnt/sdcard/", "max_1k.wav");
            mMediaPlayer.setDataSource(file.getPath());//指定音频文件路径
            mMediaPlayer.setLooping(true);//设置为循环播放
            mMediaPlayer.prepare();//初始化播放器MediaPlayer

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PermissionRequest.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer();
                    Toast.makeText(this,"Got Write_External_Storage permissions",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Deny permissions and you will not be able to use the program.", Toast.LENGTH_LONG).show();
                    //finish();
                }
                break;
            case PermissionRequest.CAMERA_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("chj","PERMISSION_GRANTED");
                    //申请成功 用户同意的处理代码
                    Toast.makeText(this,"Got camera permissions",Toast.LENGTH_SHORT).show();
                } else {
                    //用户拒绝或者直接返回了
                    Toast.makeText(this,"Did not get camera permissions",Toast.LENGTH_SHORT).show();
                }
                break;
            case PermissionRequest.RECORD_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("chj","PERMISSION_GRANTED");
                    //申请成功 用户同意的处理代码
                    Toast.makeText(this,"Got recording permission",Toast.LENGTH_SHORT).show();
                } else {
                    //用户拒绝或者直接返回了
                    Toast.makeText(this,"Did not get record permissions",Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }

    }

    private void viewOnClick() {
        WifiBtn.setOnClickListener(this);
        CameraBtn.setOnClickListener(this);
        RecordBtn.setOnClickListener(this);
        PlayBtn.setOnClickListener(this);
        TouchBtn.setOnClickListener(this);
        DisplayBtn.setOnClickListener(this);
        PlayRecBtn.setOnClickListener(this);
        IperfBtn.setOnClickListener(this);
        ResetBtn.setOnClickListener(this);
    }

    @SuppressLint("WifiManagerLeak")
    private void initView() {
        WifiBtn = findViewById(R.id.wifi_btn);
        CameraBtn = findViewById(R.id.camera_btn);
        RecordBtn = findViewById(R.id.record_btn);
        PlayBtn = findViewById(R.id.play_btn);
        TouchBtn = findViewById(R.id.touch_btn);
        DisplayBtn = findViewById(R.id.display_btn);
        PlayRecBtn = findViewById(R.id.play_rec_btn);
        IperfBtn = findViewById(R.id.iperf_btn);
        ResetBtn = findViewById(R.id.reset_btn);
        ipAddress = findViewById(R.id.ip_address);
        mShroughput = findViewById(R.id.txt_throughput);
        mWm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.wifi_btn:
                File file = new File("/mnt/sdcard/wifidemo/wifidata.txt");
                if(!file.exists()){
                    Toast.makeText(MainActivity.this, "wifi file not found",Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    String fileContent = getFileContent(file);
                    /**读取txt文件中的数据**/
                    String[] str = fileContent.split(";");
                    String wifi_ssid = str[0];
                    String wifi_pwd = str[1];
                    final String SSID = wifi_ssid.substring(wifi_ssid.indexOf("<")+1,wifi_ssid.indexOf(">"));
                    final String PWD = wifi_pwd.substring(wifi_pwd.indexOf("{")+1,wifi_pwd.indexOf("}"));
                    Toast.makeText(MainActivity.this, "SSID:"+SSID+"\nPWD:"+PWD,Toast.LENGTH_SHORT).show();

                    /**根据SSID和PWD连接Wi-Fi**/
                    connector = new WifiConnector(MainActivity.this);
                    connector.connectWifi(SSID, PWD, WifiUtil.TYPE_WPA, new WifiConnector.WifiConnectCallBack() {
                        @Override
                        public void onConnectSucess(){
                            Toast.makeText(MainActivity.this,"connection succeeded！！",Toast.LENGTH_SHORT).show();
                            ///连接成功后往文件里写Wi-Fi信号强度和Wi-Fi网速
                            updateContent("/mnt/sdcard/wifidemo/wifidata.txt","wifi_ssid:<"+SSID+">;wifi_password:{" + PWD + "}" + "\n" + getSignalStrength(),false);
                        }

                        @Override
                        public void onConnectFail(String msg) {
                            Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case R.id.camera_btn:
                takePhoto();
                break;
            case R.id.record_btn:
                Intent goRecordActivity = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(goRecordActivity);
                break;
            case R.id.play_btn:     //播放音乐
                //如果没在播放中，立刻开始播放。
                if(!mMediaPlayer.isPlaying()){
                    mMediaPlayer.start();
                }
                break;
            case R.id.touch_btn:
                Intent drawActivity = new Intent(MainActivity.this, DrawActivity.class);
                startActivity(drawActivity);
                break;
            case R.id.display_btn:
                Intent displayActivity = new Intent(MainActivity.this, DisPlayActivity.class);
                startActivity(displayActivity);
                break;
            case R.id.play_rec_btn:
                //创建录音文件
                recAudioFile = new File("/mnt/sdcard/Music", "new.amr");
                //开始录音
                startRecorder();
                //开始播放音乐，如果没在播放中，立刻开始播放
                if(!mMediaPlayer.isPlaying()){
                    mMediaPlayer.start();
                }
                PlayRecBtn.setEnabled(false);
                PlayRecBtn.setTextColor(Color.RED);
                break;
            case R.id.iperf_btn:
                sercomfun(curIperfCmd);
                Toast.makeText(this,"ip command:"+curIperfCmd,Toast.LENGTH_SHORT).show();
                break;
            case R.id.reset_btn:
                if (mMediaRecorder!=null) {
                    //停止录音
                    stopRecorder();
                }else{
                    Toast.makeText(MainActivity.this,"No recording, no music playing",Toast.LENGTH_SHORT).show();
                }
                //如果在播放中，立刻停止。
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.reset();
                    initMediaPlayer();//初始化播放器 MediaPlayer
                }
                PlayRecBtn.setEnabled(true);
                PlayRecBtn.setTextColor(Color.WHITE);
                break;
                default:
                    break;

        }
    }


    /**
     * 调用系统录音，开始录音
     * **/
    private void startRecorder() {
        mMediaRecorder = new MediaRecorder();
        if (recAudioFile.exists()) {
            recAudioFile.delete();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setOutputFile(recAudioFile.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecorder(){
        if (recAudioFile!=null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


    /**
     * 拍照
     */
    private void takePhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//打开相机的Intent
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {//这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
            imageFile = createImageFile();//创建用来保存照片的文件
            mImageUriFromFile = Uri.fromFile(imageFile);
            Log.i(TAG, "takePhoto: uriFromFile " + mImageUriFromFile);
            if (imageFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    /*7.0以上要通过FileProvider将File转化为Uri*/
                    mImageUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, imageFile);
                } else {
                    /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                    mImageUri = Uri.fromFile(imageFile);
                }
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);//将用于输出的文件Uri传递给相机
                startActivityForResult(takePhotoIntent, TAKE_PHOTO);//打开相机
            }
        }
    }

    /**相机或者相册返回来的数据**/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        /*如果拍照成功，将Uri用BitmapFactory的decodeStream方法转为Bitmap*/
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
                        Log.i(TAG, "onActivityResult: imageUri " + mImageUri);
                        galleryAddPic(mImageUriFromFile);
                        //mPicture.setImageBitmap(bitmap);//显示到ImageView上
                        //拍照成功后保存
                        AlertDialog.Builder saveDialog = new AlertDialog.Builder(MainActivity.this);
                        saveDialog.setTitle("Tips");
                        saveDialog.setMessage("Image's address:"+mImageUriFromFile);
                        saveDialog.setPositiveButton("ok", null);
                        /*saveDialog.setNeutralButton("Check", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (imageFile != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        *//*7.0以上要通过FileProvider将File转化为Uri*//*
                                        mImageUri = FileProvider.getUriForFile(MainActivity.this, FILE_PROVIDER_AUTHORITY, imageFile);
                                    } else {
                                        *//*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*//*
                                        mImageUri = Uri.fromFile(imageFile);
                                    }
                                    openAssignFolder(mImageUri.getPath());
                                }
                            }
                        });*/
                        saveDialog.show();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (data == null) {//如果没有拍照或没有选取照片，则直接返回
                    return;
                }
                Log.i(TAG, "onActivityResult: ImageUriFromAlbum: " + data.getData());
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        handleImageOnKitKat(data);//4.4之后图片解析
                    } else {
                        handleImageBeforeKitKat(data);//4.4之前图片解析
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 创建用来存储图片的文件，以时间来命名就不会产生命名冲突
     *
     * @return 创建的图片文件
     */
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //File sdCard = Environment.getExternalStorageDirectory();
        //File storageDir = new File(sdCard, "Pictures");
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    /**
     * 将拍的照片添加到相册
     *
     * @param uri 拍的照片的Uri
     */
    private void galleryAddPic(Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        sendBroadcast(mediaScanIntent);
    }

    /**
     * 4.4版本以下对返回的图片Uri的处理：
     * 就是从返回的Intent中取出图片Uri，直接显示就好
     * @param data 调用系统相册之后返回的Uri
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }


    /**
     * 4.4版本以上对返回的图片Uri的处理：
     * 返回的Uri是经过封装的，要进行处理才能得到真实路径
     * @param data 调用系统相册之后返回的Uri
     */
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的Uri，则提供document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则进行普通处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的uri，则直接获取路径
            imagePath = uri.getPath();
        }
       // displayImage(imagePath);
    }

    /**
     * 将imagePath指定的图片显示到ImageView上
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mPicture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 将Uri转化为路径
     * @param uri 要转化的Uri
     * @param selection 4.4之后需要解析Uri，因此需要该参数
     * @return 转化之后的路径
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


    private void openAssignFolder(String path){
        File file = new File(path);
        if(null==file || !file.exists()){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "file/*");
        try {
            startActivity(intent);
//            startActivity(Intent.createChooser(intent,"选择浏览工具"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入文件到本地(这里编写一个名为wifidata的txt文件，内容为wifi_ssid:Andy's iPhone; wifi_password:song0123.com)
     * **/
    private void WriteWifiData() {
       // String filePath =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/";
        //String fileName = "wifi_data.txt";
        String filePath = "/mnt/sdcard/wifidemo/";
        String fileName = "wifidata.txt";
        writeTxtToFile("wifi_ssid:<HUAWEI P9>;wifi_password:{song0123.com}", filePath, fileName);
    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

     /**
       * 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true
       *
       * @param fileName
       * @param content
       */
     public static void appendWifiInfo(String file, String conent) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(conent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     }


    /***向文本文件中追加内容，如果append=true则默认追加，如果append=false会先清空文件再追加内容***/
    public static boolean updateContent(String fileName,String content,boolean append){
        boolean res = true;
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file,append);
            if(append){
                content = System.getProperty("line.separator")+content;
                System.out.println(content);
            }
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            res = false;
            ex.printStackTrace();
        }
        return res;
    }


    /***生成文件***/
    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //生成文件夹
    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }



    /**
     * 读取数据
     * **/
    //读取指定目录下的所有TXT文件的文件内容
    private String getFileContent(File file) {
        String content = "";
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            if (file.getName().endsWith("txt")) {//文件格式为""文件
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "UTF-8");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        instream.close();//关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    Log.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("TestFile", e.getMessage());
                }
            }
        }
        return content;
    }




    /// Iperf 相关功能
    /**
     * 获取wifi地址
     */
    private String getLocalIpAddress() {
        int paramInt = 0;
        WifiInfo info = mWm.getConnectionInfo();
        if (info.getBSSID() == null) {
            return "Please connect and try again";
        } else {
            paramInt = info.getIpAddress();
            Log.i(TAG, "paramInt: " + paramInt+"0xFF&paramInt"+(0xFF & paramInt));
            return new StringBuffer()
                    .append(0xFF & paramInt).append(".")
                    .append(0xFF & paramInt >> 8).append(".")
                    .append(0xFF & paramInt >> 16).append(".")
                    .append(0xFF & paramInt >> 24).toString();
        }
    }


    /**
     * Wifi的连接速度及信号强度
     */
    public String getSignalStrength() {
        int strength = 0;
        String result = "";
        WifiInfo info = mWm.getConnectionInfo();
        if (info.getBSSID() != null) {
            // 链接信号强度，5为获取的信号强度值在5以内
            strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            int speed = info.getLinkSpeed(); // 链接速度
            String units = WifiInfo.LINK_SPEED_UNITS; // 链接速度单位
            String ssid = info.getSSID(); // Wifi源名称
            result = "Wifi signal level: "+strength + "\n" + "Wifi speed: "+speed+units;
        }
        return result;
    }



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case IPERF_ERROR:
                    mShroughput.setText((String)msg.obj);
                    break;
                case IPERF_SCCESS:
                    mShroughput.setText((String)msg.obj);
                    break;
            }
        }
    };

    /**
     * 1. 拷贝iperf到该目录下
     */
    public void copyiperf() {
        File localfile;
        Process p;
        try {
            localfile = new File(IPERF_PATH);
            p = Runtime.getRuntime().exec("chmod 777 " + localfile.getAbsolutePath());
            InputStream localInputStream = getAssets().open("iperf");
            Log.i(TAG,"chmod 777 " + localfile.getAbsolutePath());
            FileOutputStream localFileOutputStream = new FileOutputStream(localfile.getAbsolutePath());
            FileChannel fc = localFileOutputStream.getChannel();
            FileLock lock = fc.tryLock(); //给文件设置独占锁定
            if (lock == null) {
                Toast.makeText(this,"has been locked !",Toast.LENGTH_SHORT).show();
                return;
            } else {
                FileOutputStream fos = new FileOutputStream(new File(IPERF_PATH));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = localInputStream.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                    Log.i(TAG, "byteCount: "+byteCount);
                }
                fos.flush();// 刷新缓冲区
                localInputStream.close();
                fos.close();
            }
            //两次才能确保开启权限成功
            p = Runtime.getRuntime().exec("chmod 777 " + localfile.getAbsolutePath());
            lock.release();
            p.destroy();
            Log.i(TAG, "the iperf file is ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 2. 在Android应用中执行iperf命令
     */
    private void sercomfun(final String cmd) {
        Log.i(TAG, "sercomfun = " + cmd);
        Thread lthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String errorreply = "";
                    CommandHelper.DEFAULT_TIMEOUT = 150000;
                    CommandResult result = CommandHelper.exec(cmd);
                    if (result != null) {
                        //start to connect the service
                        if (result.getError() != null) {
                            errorreply = result.getError();
                            Message m = new Message();
                            m.obj = errorreply;
                            m.what = IPERF_ERROR;
                            handler.sendMessage(m);
                            Log.i(TAG,"Error:" + errorreply);
                        }
                        if (result.getOutput() != null) {
                            iperfreply = getThroughput(result.getOutput());
                            IPERF_OK = true;
                            Message m = new Message();
                            m.obj = iperfreply;
                            m.what = IPERF_SCCESS;
                            handler.sendMessage(m);
                            Log.i(TAG,"Output:" + iperfreply);
                        }
                        Log.i(TAG,"result.getExitValue(): "+result.getExitValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        lthread.start();
    }

    /**
     * 从获取到的吞吐量信息中截取需要的信息，如：
     * 0.0-10.0 sec  27.5 MBytes  23.0 Mbits/sec
     */
    private String getThroughput(String str) {
        String regx = "0.0-.+?/sec";
        String result = "";
        Matcher matcher = Pattern.compile(regx).matcher(str);
        Log.i(TAG,"matcher regx : "+regx+" is "+matcher.matches());
        if(matcher.find()){
            Log.i(TAG,"group: "+matcher.group());
            result = matcher.group();
        }
        return result;
    }



//    //获取权限
//    private void getPermission() {
//        if (EasyPermissions.hasPermissions(this, permissions)) {
//            //已经打开权限
//           // Toast.makeText(this, "已经申请相关权限", Toast.LENGTH_SHORT).show();
//        } else {
//            //没有打开相关权限、申请权限
//            EasyPermissions.requestPermissions(this, "需要获取您的相册、照相使用权限", 1, permissions);
//        }
//
//    }
//
//    @Override
//    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
//       // Toast.makeText(this, "相关权限获取成功", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
//        Toast.makeText(this, "Please agree to the relevant permissions, otherwise the function cannot be used.", Toast.LENGTH_SHORT).show();
//    }
}
