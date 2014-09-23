#!/bin/sh
PID=`ps -ef|grep bricks-0.0.1-SNAPSHOT-shaded.jar |grep -v grep|awk '{print $2}'`
if [ x"$PID" != x ];then
echo "----- Force kill bricks-0.0.1-SNAPSHOT-shaded.jar processId $PID"
kill -9 $PID;
fi

