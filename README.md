# PanelFeatures
Test whether the function on the Android board is normal, camera, recording, wireless network, etc.<br><br>
主要包括以下功能：
----
* WIFICON（Wi-Fi直连，通过读取txt文件里面的Wi-Fi SSID和PWD进行Wi-Fi连接）
* CAMERA（自定义相机预览界面，拍照界面，拍照后文件保存到包名下的files/pictures中）
* RECORD（录音功能，开始、暂停、播放，还配有声音波形效果）
* TOUCH（多点触控画板，画笔颜色随机）
* FILES（简易版文件管理器）
* BLUETOOTH（通过系统蓝牙传输文件）
* TRANSMISSION（在局域网中通过Wi-Fi进行文件传输，参考多看Wi-Fi传书功能）
* IPERF（iperf网络测试吞吐量，目前没测试成功，好像需要编译源码，如果有小伙伴知道该怎么弄，清不吝赐教）
* 页面上其他的按钮功能均为播放不同音频文件按钮，RESET为停止播放按钮

README
=====

![read](https://github.com/AndySung/readme_add_pic/blob/master/panelfetrues_demand.png "read")

如果不能工作，查看是否是路径不对(这个为我们自己测试的Demo所有将目录全部放到了自己的包名下面)
====

wifi 连接（需要手动添加和修改文件）
----

路径：/data/data/com.soft.nortek.demo/files/wifidata.txt <br>
文件内容：wifi_ssid:<HUAWEI P9>;wifi_password:{song0123.com};<br>
需要修改<>{}里面的字符，<>里面表示Wi-Fi名称，{}里面字符表示Wi-Fi密码<br>


边录音边播放音乐（不需要手动添加文件）
----
路径：/data/data/com.soft.nortek.demo/files/new.amr<br>



音乐（需要手动添加文件）
----
路径： /data/data/com.soft.nortek.demo/files/max_1k.wav <br>
请自行下载音乐放到相应的目录 <br>

其他的播放文件也是放这里（/data/data/com.soft.nortek.demo/files/）的，下载后会在asset目录中看到这些文件，我是将这些文件从asset中复制到了包名下的files中
----
<br>


iPerf（需要编译源码）
=====
测试方式
-----
1. 测试上行速率（Mac作Server，手机作Client）<br>
2. 测试下行速率（手机作Server，Mac作Client）<br>


Server启动指令
-----

1. iperf -s -p 5001 -P 1 -f m -i 2 <br>
2. 说明：<br>
3. -s      表示作为服务端启动 <br>
4. -p 5001 表示监听的端口为5001 <br>
5. -P 1    表示服务端与客户端之前的线程数为1。客户端同时使用此参数 <br>
6. -f m    表示显示数据相关大小时输出格式为Mbps <br>
7. -i 2    表示每2秒刷新一次log <br>


Client启动指令
-----

1. iperf -c xx.xx.xx.xx -p 5001 -t 60 -P 1 -n 100M <br>
2. 说明：<br>
3. -c xxxx 表示作为客户端启动，xx.xx.xx.xx即服务端ip地址 <br>
4. -p 5001 表示服务端监听的端口为5001 <br>
5. -t 60   表示测试时长为60秒 <br>
6. -P 1    表示客户端与服务端之间的线程数为1。服务端同时使用此参数 <br>
7. -n 100M 表示发送总数据量为100M。此时-t限定的时长将无效 <br>
8. -i 2    表示每2秒刷新一次log <br>


![mainscreen](https://github.com/AndySung/readme_add_pic/blob/master/screenshot_main.png "MainScreen")


![Camera](https://github.com/AndySung/readme_add_pic/blob/master/screenshot_camera.png "Camera")

![Recorder](https://github.com/AndySung/readme_add_pic/blob/master/screenshot_record.png "Recorder")

![Touch](https://github.com/AndySung/readme_add_pic/blob/master/screenshot_duodian.png "Touch")

![Display](https://github.com/AndySung/readme_add_pic/blob/master/screenshot_display.png "Display")

![Files](https://github.com/AndySung/readme_add_pic/blob/master/screenshot_filemanage.png "Files")

![Transmission](https://github.com/AndySung/readme_add_pic/blob/master/screenshot_wifitransfer.png "Transmission")

