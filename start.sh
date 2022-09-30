#!/bin/bash
### 安装依赖(只支持ubuntu系统，其他的自行修改)
which docker
if [ $? -ne 0 ];then
    apt -y install docker.io
    if [ $? -ne 0 ];then
        echo "install docker failed"
        exit -1
    fi
fi

which ffmpeg
if [ $? -ne 0 ];then
    apt -y install ffmpeg
    if [ $? -ne 0 ];then
        echo "install ffmpeg failed"
        exit -1
    fi
fi

## you-get
which you-get
if [ $? -ne 0 ];then
    pip3 install --upgrade you-get
    if [ $? -ne 0 ];then
        echo "install you-get failed"
        exit -1
    fi
fi

## redis
docker ps | grep redis-collector
if [ $? -ne 0 ];then
    docker run --name redis-collector -p 6379:6379 -d redis redis-server --save 60 1 --loglevel warning --requirepass redis_passwd
    if [ $? -ne 0 ];then
        echo "docker redis start failed"
        exit -1
    fi
fi

## 下载安装chrome
which chromium
if [ $? -ne 0 ];then
    snap install chromium
    if [ $? -ne 0 ];then
        echo "install chromium failed"
        exit -1
    fi
fi

## 安装mvn
which mvn
if [ $? -ne 0 ];then
    apt -y install maven
    if [ $? -ne 0 ];then
        echo "install maven failed"
        exit -1
    fi
fi

## fatjar启动
if [[ "X$1" == "Xpackage" ]];then
    mvn clean package -Dmaven.test.skip=true
    if [ $? -ne 0 ];then
        echo "mvn package failed"
        exit -1
    fi
fi
nohup mvn spring-boot:run &
