#!/bin/sh
project=`ls *.jar`
pid=`ps aux|grep $project|grep -Ev "reload|restart|grep" |cut -c 10-15`
if [[ -n $pid ]];then
    echo "kill -9 " $pid
    kill -9 $pid
else
    echo "$PROJECT is not running"
fi
nohup java -jar $project > /dev/null 2>&1 &
echo "$project has been restarting"
