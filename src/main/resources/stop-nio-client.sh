#!/bin/sh
PID=`ps -ef|grep bricks-client.jar |grep -v grep|awk '{print $2}'`
if [ x"$PID" != x ];then
echo "----- Force kill bricks-client.jar processId $PID"
kill -9 $PID;
fi

