#!/bin/sh
#create by 2020-06-19 xiaobai021sdo
#update by 2020-08-20 xiaobai021sdo

$IMAGE="xiaobai021sdo/plumelog:3.2"

echo "拉取docker镜像 $IMAGE......"
docker pull $IMAGE

echo "启动plumelog-server和plumelog-ui的docker镜像......"
docker run -d --name plumelog -v $(pwd)/plumelog-server:/plumelog-server -p 8891:8891 $IMAGE
echo "plumelog-server 启动完成"
