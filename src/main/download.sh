#!/bin/bash
## 下载前已经通过start.sh保证了you-get的安装，程序里也保证了参数1是下载路径，参数2是待下载的正确url
if [ ! -d $1 ];then
    mkdir -p $1
fi
cd $1 && you-get $2
