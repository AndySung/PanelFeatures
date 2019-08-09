# PanelFeatures
Test whether the function on the Android board is normal, camera, recording, wireless network, etc.

README
=====
![github](https://github.com/AndySung/readme_add_pic/blob/master/github-logo.png "Github logo")  

![read](https://github.com/AndySung/readme_add_pic/blob/master/panelfetrues_demand.png "read")

##如果不能工作，查看是否是路径不对

###wifi 连接（需要手动添加和修改文件）
#####路径：/mnt/sdcard/wifidemo/wifidata.txt
#####文件内容：wifi_ssid:<HUAWEI P9>;wifi_password:{song0123.com};
#####<color="#f00">需要修改<>{}里面的字符，<>里面表示Wi-Fi名称，{}里面字符表示Wi-Fi密码</color>




##边录音边播放音乐（不需要手动添加文件）
#####路径：/mnt/sdcard/Music/new.amr



##音乐（需要手动添加文件，名字必须为max_1k.wav）
#####路径： /mnt.sdcard/max_1k.wav
#####请自行下载音乐放到相应的目录



##iPerf
####测试方式
######1. 测试上行速率（Mac作Server，手机作Client）
######2. 测试下行速率（手机作Server，Mac作Client）


####Server启动指令

######1. iperf -s -p 5001 -P 1 -f m -i 2
######2. # 说明：
######3. # -s      表示作为服务端启动
######4. # -p 5001 表示监听的端口为5001
######5. # -P 1    表示服务端与客户端之前的线程数为1。客户端同时使用此参数
######6. # -f m    表示显示数据相关大小时输出格式为Mbps
######7. # -i 2    表示每2秒刷新一次log


####Client启动指令

######1. iperf -c xx.xx.xx.xx -p 5001 -t 60 -P 1 -n 100M
######2. # 说明：
######3. # -c xxxx 表示作为客户端启动，xx.xx.xx.xx即服务端ip地址
######4. # -p 5001 表示服务端监听的端口为5001
######5. # -t 60   表示测试时长为60秒
######6. # -P 1    表示客户端与服务端之间的线程数为1。服务端同时使用此参数
######7. # -n 100M 表示发送总数据量为100M。此时-t限定的时长将无效
######8. # -i 2    表示每2秒刷新一次log
