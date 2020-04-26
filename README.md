# easy_log
### 1.分布式日志系统介绍

 1. 无入侵的分布式日志系统，基于log4j、log4j2、logback搜集日志，设置链路ID，方便查询关联日志
 
 2. 基于elasticsearch作为查询引擎
 
 3. 高吞吐，查询效率高
 
 4. 全程日志不落磁盘，免维护
 
 5. 无需修改老项目，引入直接使用
 
       

### 2.架构

* easy_log_core 核心组件包含日志搜集端，负责搜集日志并推送到kafka，redis等队列

* easy_log_server 负责把队列中的日志日志异步写入到elasticsearch 

* easy_log_search 前端展示，日志查询界面

### 3.系统流程
   1. easy_log_core 搜集日志发送到=>kafka或者redis
   
   2. easy_log_server kafka或者redis=>elasticsearch
   
### 4.使用方法

1. 打包

* maven install -DskipTest 打包
   
* 到easy_log_core,easy_log_log4j,easy_log_log4j2,easy_log_logback四个目录下 执行maven deploy 上传包到自己的私服
   
     私服地址到easy_log目录的pom.xml改
     
            <properties>
              <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
              <distribution.repository.url>http://172.16.249.94:4000</distribution.repository.url>
            </properties>

2. 配置

    （1）如果用log4j，引入
    
                   <dependency>
                       <groupId>com.beeplay</groupId>
                       <artifactId>easy_log_log4j</artifactId>
                       <version>1.0</version>
                   </dependency>
                         
    配置log4j配置文件，增加下面这个Appender
    
    kafka做为中间件
    
        log4j.appender.L=com.beeplay.easylog.core.appender.KafkaAppender
        #appName系统的名称(自己定义就好)
        log4j.appender.L.appName=easyjob
        log4j.appender.L.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
        #topic(kafka的topic)这里面要和easy_log_server中的一致
        log4j.appender.L.topic=beeplay_log_list

    redis做为中间件
    
        log4j.appender.L=com.beeplay.easylog.log4j.appender.RedisAppender
        log4j.appender.L.appName=easyjob
        log4j.appender.L.reidsHost=172.16.249.72
        log4j.appender.L.redisPort=6379
        log4j.appender.L.redisAuth=
        log4j.appender.L.redisKey=beeplay_log_list

    同理如果使用logback,和log4j2配置如下
    
    这里注意：spring boot项目默认用的logback;easy_log_search里面有基于springboot的使用demo
    
#### logback

* 引入
    
       <dependency>
           <groupId>com.beeplay</groupId>
           <artifactId>easy_log_logback</artifactId>
           <version>1.0</version>
       </dependency>
    
* 配置
    
        <!-- easylog日志 -->
        <appender name="easylog" class="com.beeplay.easylog.logback.appender.RedisAppender">
            <appName>easylog</appName>
            <reidsHost>172.16.249.72</reidsHost>
            <redisPort>6379</redisPort>
            <redisKey>beeplay_log_list</redisKey>
        </appender>
       
        <appender name="easylog" class="com.beeplay.easylog.logback.appender.KafkaAppender">
            <appName>easylog</appName>
            <kafkaHosts>172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092</kafkaHosts>
            <topic>beeplay_log_list</topic>
        </appender>
      
        <!-- 上面两个配置二选一 -->
   
        <!-- 日志输出级别 -->
        <root level="INFO">
            <appender-ref ref="STDOUT" />
            <appender-ref ref="FILE" />
            <appender-ref ref="easylog" />
        </root>

#### log4j2

* 引入

       <dependency>
           <groupId>com.beeplay</groupId>
           <artifactId>easy_log_logBack</artifactId>
           <version>1.0</version>
       </dependency>       

* 配置

          <KafkaAppender name="kafkaAppender" appName="easyjob" kafkaHosts="172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092" topic="beeplay_log_list">
              <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] [%-5p] {%F:%L} - %m%n" />
          </KafkaAppender>
    
          <RedisAppender name="redisAppender" appName="easyjob" reidsHost="172.16.249.72" redisPort="6379" redisKey="beeplay_log_list">
              <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] [%-5p] {%F:%L} - %m%n" />
          </RedisAppender>
     
          </appenders>
          <!-- 上面两个配置二选一 -->
          <loggers>
              <root level="INFO">
                  <appender-ref ref="Console"/>
                  <appender-ref ref="redisAppender"/>
              </root>
          </loggers>
  
3. 示例代码
  
       import com.beeplay.easylog.core.TransId;
       import org.slf4j.Logger;
       import org.slf4j.LoggerFactory;
       import java.util.UUID;
       
       public class LogTest
       {
           private static Logger logger=LoggerFactory.getLogger(LogTest.class);
           public static void main( String[] args )
           {
               TransId.logTranID.set(UUID.randomUUID().toString());
               logger.info("{}","I am log name:"+UUID.randomUUID().toString());
               logger.info("{}","I am log name:"+UUID.randomUUID().toString());
           }
       }

4. 启动服务

 * 步骤一打包完的 easy_log_server 和 easy_log_search 两个项目
   
   备注：可用不用启动easy_log_search这个项目，这是个日志查询界面，可以使用kibana代替
 
 * easy_log_server中easylog.properties详解
 
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
