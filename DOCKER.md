#### docker版本安装
```docker

docker pull xiaobai021sdo/plumelog:2.2

```


#### 在宿主机上执行下面命令创建配置文件目录 

```jshelllanguage

mkdir -P /data/plumelog-server-2.2 mkdir -P /data/plumelog-ui-2.2

```
#### 然后把修改好的各自的application.properties放进上面创建的目录内

#### 启动docker 命令
```docker
docker run -itd --name plumelog-v2.2 -v 
/data/plumelog-server-2.2:/plumelog-server-2.2 -v 
/data/plumelog-ui-2.2:/plumelog-ui-2.2 -p 8989:8989 -p 8891:8891 {IMAGE ID} 
/bin/bash

```


#### 进入docker
```docker

docker exec -it {CONTAINER ID} /bin/bash


```
