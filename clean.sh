#!/bin/bash
ps -ef | grep spring | awk '{print $2}' | xargs kill
ps -ef | grep chrome | awk '{print $2}' | xargs kill
ps -ef | grep you-get | awk '{print $2}' | xargs kill