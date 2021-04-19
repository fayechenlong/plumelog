#!/bin/sh
project=`ls *.jar`
nohup java -jar $project > /dev/null 2>&1 &
echo "$project has been starting!!!"
