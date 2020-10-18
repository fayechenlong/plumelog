#!/bin/sh
#create by 2020-06-19 xiaobai021sdo
#update by 2020-08-10 xiaobai021sdo
#update by 2020-08-20 xiaobai021sdo
#获取当前路径
basedir=$(cd "$(dirname "$0")"; pwd)

#创建配置文件目录
mkdir -p $basedir/plumelog-server/config/

#plumelog-server配置文件写入
cat << EOF >$basedir/plumelog-server/config/application.properties
spring.application.name=plumelog_server
server.port=8891

#值为4种 redis,kafka,rest,restServer
#redis 表示用redis当队列
#kafka 表示用kafka当队列
#rest 表示从rest接口取日志
#restServer 表示作为rest接口服务器启动
plumelog.model=redis

#如果使用kafka,启用下面配置
#plumelog.kafka.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
#plumelog.kafka.kafkaGroupName=logConsumer

#redis配置,3.0版本必须配置redis地址，因为需要监控报警
plumelog.redis.redisHost=172.16.247.69:6380
#如果使用redis有密码,启用下面配置
#plumelog.redis.redisPassWord=123456

#如果使用rest,启用下面配置
#plumelog.rest.restUrl=http://127.0.0.1:8891/getlog
#plumelog.rest.restUserName=plumelog
#plumelog.rest.restPassWord=123456

#elasticsearch相关配置
plumelog.es.esHosts=10.33.85.101:9200,10.33.85.102:9200,10.33.85.103:9200
#ES7.*已经去除了索引type字段，所以如果是es7不用配置这个，7.*以下不配置这个会报错
#plumelog.es.indexType=plumelog
#ES设置密码,启用下面配置
plumelog.es.userName=elastic
plumelog.es.passWord=easylog123456

#单次拉取日志条数
plumelog.maxSendSize=5000
#拉取时间间隔，kafka不生效
plumelog.interval=1000

#plumelog-ui的地址 如果不配置，报警信息里不可以点连接
plumelog.ui.url=http://localhost:8891

#管理密码，手动删除日志的时候需要输入的密码
admin.password=123456
#日志保留天数,配置0或者不配置默认永久保留
admin.log.keepDays=15

spring.thymeleaf.mode=LEGACYHTML5
spring.mvc.view.prefix=classpath:/templates/
spring.mvc.view.suffix=.html
spring.mvc.static-path-pattern=/plumelog/**

plumelog.es.shards=20
plumelog.es.replicas=1

EOF


echo "请修改$basedir/plumelog-server/config/application.properties 配置文件"

echo "修改完成后启动plumelog-start.sh"
