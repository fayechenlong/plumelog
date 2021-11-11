#!/bin/sh
#create by 2020-06-19 xiaobai021sdo
#update by 2020-08-20 xiaobai021sdo
#update by 2021-07-16 xiaobai021sdo
#update by 2021-11-11 xiaobai021sdo 更新3.5版本
#拉取docker镜像
docker pull xiaobai021sdo/plumelog:3.5

#获取镜像的image id
IMAGEID=`docker images -q --filter reference=xiaobai021sdo/plumelog*:3.5`
echo $IMAGEID
#启动plumelog-server和plumelog-ui的docker镜像
docker run -itd --name plumelog-v3.5 -v /data/plumelog-server:/plumelog-server -p 8891:8891 $IMAGEID /bin/bash

echo "plumelog-server 启动完成"
