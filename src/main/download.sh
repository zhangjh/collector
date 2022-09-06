#!/bin/bash
## 下载前已经通过start.sh保证了you-get的安装，程序里也保证了参数1是下载路径，参数2是待下载的正确url，参数3随便填，填了代表需要下载字幕、弹幕等，默认不下
if [ ! -d $1 ];then
    mkdir -p $1
fi
if [[ "X$3" != 'X' ]];then
    you-get --playlist -o $1 $2
else
    you-get --playlist --no-caption -o $1 $2
fi
