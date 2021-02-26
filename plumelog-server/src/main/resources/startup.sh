#!/bin/sh
project=`ls *.jar`
nohup java -jar $project &
echo "$project has been starting!!!"
