# easy_log
### 1.分布式日志系统介绍

 1. 无入侵的分布式日志系统，基于log4j搜集日志，设置链路ID，方便查询关联日志
 
 2. 基于elasticsearch作为查询引擎
 
 3. 高吞吐，查询效率高
 
 4. 全程日志不落磁盘，免维护
 
       

### 2.架构

* easy_log_core 核心组件包含日志搜集端，负责搜集日志并推送到kafka，redis等队列

* easy_log_server 负责把队列中的日志日志异步写入到elasticsearch 

* easy_log_search 前端展示，日志查询界面

### 3.系统流程
   1. easy_log_core 搜集日志发送到=>kafka或者redis
   
   2. easy_log_server kafka或者redis=>elasticsearch
   
### 4.使用方法
1. 引入easy_log_core到项目中，安装kafka，安装elasticsearch

2. 在使用的地方加入代码

       private static EasyLogger logger = EasyLogger.getLogger(Logger.getLogger(LogTest.class));
       
       public class LogTest{
           private static EasyLogger logger = EasyLogger.getLogger(Logger.getLogger(LogTest.class));
           public static void main( String[] args ){
               TransId.logTranID.set(UUID.randomUUID().toString());
               for(int i=0;i<223;i++) {
               logger.info("I am log name:"+UUID.randomUUID().toString());
               }
           }
       }
       
3. 配置log4j配置文件，增加下面这个Appender

        log4j.appender.L=com.beeplay.easylog.core.appender.KafkaAppender
        #appName系统的名称(自己定义就好)
        log4j.appender.L.appName=easyjob
        log4j.appender.L.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
        #topic(kafka的topic)这里面要和easy_log_server中的一致
        log4j.appender.L.topic=beeplay_log_list
4. 启动easy_log_server和easy_log_search
 
 * easylog.properties详解
 
       easylog.server.model=kafka
       #kafka集群地址
       easylog.server.host.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
       #elasticsearch集群地址
       easylog.server.host.esHosts=172.16.251.196:9200
       easylog.server.maxSendSize=100
       #log的key名称，如果用的kafka就是kafka的topic
       easylog.server.logkey=beeplay_log_list
    
   
### 5.联系交流
* wx
  
   longfeiclf
