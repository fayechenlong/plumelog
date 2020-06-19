#!/bin/sh
#create by 2020-06-19 xiaobai021sdo
#拉取docker镜像
docker pull xiaobai021sdo/plumelog:2.2

#获取镜像的image id
IMAGEID=`docker images -q --filter reference=xiaobai021sdo/plumelog*:*`
echo $IMAGEID
#启动plumelog-server和plumelog-ui的docker镜像
docker run -itd --name plumelog-v2.2 -v /data/plumelog-server:/plumelog-server -v /data/plumelog-ui:/plumelog-ui -p 8989:8989 -p 8891:8891 $IMAGEID /bin/bash

echo "plumelog-server and plumelog-ui 启动完成"
