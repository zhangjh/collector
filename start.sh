#!/bin/bash
### 安装依赖(只支持ubuntu系统，其他的自行修改)
which docker
if [ $? -ne 0 ];then
    apt -y install docker.io
fi

## you-get
which you-get
if [ $? -ne 0 ];then
    apt -y install you-get
fi

## redis
docker run --name redis-collector -p 6379:6379 -d redis redis-server --save 60 1 --loglevel warning --requirepass redis_passwd

## 下载安装chromedriver驱动
apt -y install chromium-chromedriver

## 安装mvn
which mvn
if [ $? -ne 0 ];then
    apt -y install maven
fi

## fatjar启动
mvn clean package -Dmaven.test.skip=true && mvn spring-boot:run &