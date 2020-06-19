#!/bin/sh
#create by 2020-06-19 xiaobai021sdo
#获取当前路径
basedir=$(cd "$(dirname "$0")"; pwd)

#创建配置文件目录
mkdir -p $basedir/plumelog-server/config/
mkdir -p $basedir/plumelog-ui/config/

#plumelog-server配置文件写入
cat << EOF >$basedir/plumelog-server/config/application.properties
spring.application.name=plumelog_server
server.port=8891

#值为4种 redis,kafka,rest,restServer
#redis 表示用redis当队列
#kafka 表示用kafka当队列
#rest 表示从rest接口取日志
#restServer 表示作为rest接口服务器启动
plumelog.server.model=redis

#如果使用kafka,启用下面配置
#plumelog.server.kafka.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
#plumelog.server.kafka.kafkaGroupName=logConsumer

#如果使用redis,启用下面配置
plumelog.server.redis.redisHost=172.16.249.72:6379
#如果使用redis有密码,启用下面配置
#plumelog.server.redis.redisPassWord=123456

#如果使用rest,启用下面配置
#plumelog.server.rest.restUrl=http://127.0.0.1:8891/getlog
#plumelog.server.rest.restUserName=plumelog
#plumelog.server.rest.restPassWord=123456

#elasticsearch相关配置
plumelog.server.es.esHosts=10.33.85.101:9200,10.33.85.102:9200,10.33.85.103:9200
#ES7.*已经去除了索引type字段，所以如果是es7不用配置这个，7.*以下不配置这个会报错
#plumelog.server.es.indexType=plumelog
#ES设置密码,启用下面配置
plumelog.server.es.userName=elastic
plumelog.server.es.passWord=easylog123456

#单次拉取日志条数
plumelog.server.maxSendSize=5000
#拉取时间间隔，kafka不生效
plumelog.server.interval=1000
EOF

#plumelog-ui配置文件写入
cat << EOF >$basedir/plumelog-ui/config/application.properties
spring.application.name=plumelog-ui
server.port=8989
spring.thymeleaf.mode=LEGACYHTML5
spring.mvc.view.prefix=classpath:/templates/
spring.mvc.view.suffix=.html
spring.mvc.static-path-pattern=/plumelog/**


#elasticsearch地址
es.esHosts=10.33.85.101:9200,10.33.85.102:9200,10.33.85.103:9200
#ES如果有密码,启用下面配置
es.userName=elastic
es.passWord=easylog123456

#管理密码，手动删除日志的时候需要输入的密码
admin.password=123456
#日志保留天数,配置0或者不配置默认永久保留
admin.log.keepDays=0

EOF


echo "请修改$basedir/plumelog-ui/config/application.properties 配置文件"
echo "请修改$basedir/plumelog-server/config/application.properties 配置文件"

echo "修改完成后启动plumelog-start.sh"
