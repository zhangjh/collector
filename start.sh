#!/bin/bash
### 安装依赖(只支持ubuntu系统，其他的自行修改)
which docker
if [ $? -ne 0 ];then
    apt -y install docker.io
fi

which ffmpeg
if [ $? -ne 0 ];then
    apt -y install ffmpeg
fi

## you-get
which you-get
if [ $? -ne 0 ];then
    pip3 install --upgrade you-get
fi

## redis
docker ps | grep redis-collector
if [ $? -ne 0 ];then
    docker run --name redis-collector -p 6379:6379 -d redis redis-server --save 60 1 --loglevel warning --requirepass redis_passwd
fi

## 下载安装chrome
which chromium
if [ $? -ne 0 ];then
    snap install chromium
fi

## 安装mvn
which mvn
if [ $? -ne 0 ];then
    apt -y install maven
fi

## fatjar启动
if [[ "X$1" == "Xpackage" ]];then
    mvn clean package -Dmaven.test.skip=true
fi
nohup mvn spring-boot:run &
