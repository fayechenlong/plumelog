 ![avatar](/pic/icon.png)
 # Plumelog一个简单易用的java分布式日志组件

### 一、架构

 ![avatar](/pic/plumelog.png)
 
### 二、使用方法

#### （1）安装
    
   1.安装 redis 或者 kafka（一般公司redis足够） redis 官网:https://redis.io   kafka：http://kafka.apache.org
     
   2.安装 elasticsearch 官网下载地址:https://www.elastic.co/cn/downloads/past-releases
    
   3.下载安装包，plumelog-server 下载地址：https://gitee.com/frankchenlong/plumelog/releases
   
   备注：3.1版本以后UI和server合并，plumelog-ui这个项目可以不用部署
   
   4.配置plumelog-server，并启动
   
   配置文件 plumelog-server/application.properties 详解：

```properties
        spring.application.name=plumelog_server
        server.port=8891
        spring.thymeleaf.mode=LEGACYHTML5
        spring.mvc.view.prefix=classpath:/templates/
        spring.mvc.view.suffix=.html
        spring.mvc.static-path-pattern=/plumelog/**
        
        #值为4种 redis,kafka,rest,restServer
        #redis 表示用redis当队列
        #kafka 表示用kafka当队列
        #rest 表示从rest接口取日志
        #restServer 表示作为rest接口服务器启动
        #ui 表示单独作为ui启动
        plumelog.model=redis
        
        #如果使用kafka,启用下面配置
        #plumelog.kafka.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
        #plumelog.kafka.kafkaGroupName=logConsumer
        
        
        plumelog.queue.redis.redisHost=127.0.0.1:6379
        #如果使用redis有密码,启用下面配置
        #plumelog.queue.redis.redisPassWord=plumelog
        #plumelog.queue.redis.redisDb=0
        
        #redis配置,3.0版本必须配置redis地址，因为需要监控报警
        plumelog.redis.redisHost=127.0.0.1:6379
        #如果使用redis有密码,启用下面配置
        #plumelog.redis.redisPassWord=plumelog
        
        #如果使用rest,启用下面配置
        #plumelog.rest.restUrl=http://127.0.0.1:8891/getlog
        #plumelog.rest.restUserName=plumelog
        #plumelog.rest.restPassWord=123456
        
        #elasticsearch相关配置
        plumelog.es.esHosts=172.19.11.43:9200,172.19.11.44:9200,172.19.11.45:9200,172.19.11.46:9200,172.19.11.47:9200
        #ES7.*已经去除了索引type字段，所以如果是es7不用配置这个，7.*以下不配置这个会报错
        #plumelog.es.indexType=plumelog
        plumelog.es.shards=5
        plumelog.es.replicas=1
        plumelog.es.refresh.interval=30s
        #日志索引建立方式day表示按天、hour表示按照小时，如果每天日志量超过了500G建议启动小时模式
        plumelog.es.indexType.model=day
        #ES设置密码,启用下面配置
        #plumelog.es.userName=elastic
        #plumelog.es.passWord=123456
        
        #单次拉取日志条数
        plumelog.maxSendSize=100
        #拉取时间间隔，kafka不生效
        plumelog.interval=200
        
        #plumelog-ui的地址 如果不配置，报警信息里不可以点连接
        plumelog.ui.url=https://127.0.0.1:8891
        
        #管理密码，手动删除日志的时候需要输入的密码
        admin.password=123456
        #日志保留天数,配置0或者不配置默认永久保留
        admin.log.keepDays=30
       
        #链路保留天数,配置0或者不配置默认永久保留
        admin.log.trace.keepDays=30
        
        #登录用户名密码，为空没有登录界面
        login.username=admin
        login.password=admin
```       

#### （2）项目使用

#### 推荐使用logback,特别是springboot，springcloud项目

   （1）如果项目使用的log4j，引入
```xml
   <dependency>
       <groupId>com.plumelog</groupId>
       <artifactId>plumelog-log4j</artifactId>
       <version>3.2</version>
   </dependency>
```                       
   配置log4j配置文件，增加下面这个Appender
    
```properties
    log4j.rootLogger = INFO,stdout,L
    log4j.appender.stdout = org.apache.log4j.ConsoleAppender
    log4j.appender.stdout.Target = System.out
    log4j.appender.stdout.layout = org.apache.log4j.PatternLayout
    log4j.appender.stdout.layout.ConversionPattern = [%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS} [%c.%t]%n%m%n
    #kafka做为中间件
    #<!-- 字段说明 -->
    #<!-- appName:应用名称 -->
    #<!-- kafkaHosts：kafka集群地址 -->
    #<!-- runModel：runModel 1,2  1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1-->
    log4j.appender.L=com.plumelog.log4j.appender.KafkaAppender
    #appName系统的名称(自己定义就好)
    log4j.appender.L.appName=plumelog
    log4j.appender.L.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
    #redis做为中间件
    #字段说明
    #<!-- appName:应用名称 -->
    #<!-- redisHost：redis地址 -->
    #<!-- redisPort：redis端口号 不配置，默认使用6379-->
    #<!-- runModel：runModel 1,2  1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1- -->
    log4j.appender.L=com.plumelog.log4j.appender.RedisAppender
    log4j.appender.L.appName=plumelog
    log4j.appender.L.redisHost=172.16.249.72
    log4j.appender.L.redisPort=6379
    #redis没有密码这一项为空或者不需要
    #log4j.appender.L.redisAuth=123456
```
   同理如果使用logback,和log4j2配置如下
    
#### logback

* 引入
```xml
   <dependency>
       <groupId>com.plumelog</groupId>
       <artifactId>plumelog-logback</artifactId>
       <version>3.2</version>
   </dependency>
```  
* 配置
```xml
 <appenders>
    <!--使用redis启用下面配置-->
    <!-- 字段说明 -->
    <!-- appName:应用名称 -->
    <!-- redisHost：redis地址 -->
    <!-- redisPort：redis端口号 不配置，默认使用6379-->
    <!-- runModel：runModel 1,2  1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1- -->
    <!-- expand：整合其他链路插件，启用这个字段 expand=“sleuth” 表示整合springcloud.sleuth- -->
    <appender name="plumelog" class="com.plumelog.logback.appender.RedisAppender">
        <appName>plumelog</appName>
        <redisHost>172.16.249.72</redisHost>
        <redisAuth>123456</redisAuth>
        <redisPort>6379</redisPort>
    </appender>
   <!-- 使用kafka启用下面配置 -->
   <!-- 字段说明 -->
   <!-- appName:应用名称 -->
   <!-- kafkaHosts：kafka集群地址 -->
   <!-- runModel：runModel 1,2  1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1- -->
   <!-- expand：整合其他链路插件，启用下面配置 sleuth表示整合springcloud.sleuth- -->
    <appender name="plumelog" class="com.plumelog.logback.appender.KafkaAppender">
        <appName>plumelog</appName>
        <kafkaHosts>172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092</kafkaHosts>
    </appender>
 </appenders>
    <!-- 上面两个配置二选一 -->
    <!-- 日志输出级别 -->
    <root level="INFO">
        <appender-ref ref="plumelog" />
    </root>
```   
#### log4j2

* 引入
```xml
   <dependency>
       <groupId>com.plumelog</groupId>
       <artifactId>plumelog-log4j2</artifactId>
       <version>3.2</version>
   </dependency>       
```   
* 配置
```xml
 <appenders>
   <!-- 使用kafka启用下面配置 -->
   <!-- 字段说明 -->
   <!-- appName:应用名称 -->
   <!-- kafkaHosts：kafka集群地址 -->
   <!-- runModel：runModel 1,2  1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1- -->
   <!-- expand：整合其他链路插件，启用下面配置 sleuth表示整合springcloud.sleuth- -->
  <KafkaAppender name="kafkaAppender" appName="plumelog" kafkaHosts="172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092" >
      <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] [%-5p] {%F:%L} - %m%n" />
  </KafkaAppender>
     <!--使用redis启用下面配置-->
     <!-- 字段说明 -->
     <!-- appName:应用名称 -->
     <!-- redisHost：redis地址 -->
     <!-- redisPort：redis端口号 不配置，默认使用6379-->
     <!-- runModel：runModel 1,2  1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1- -->
     <!-- expand：整合其他链路插件，启用这个字段 expand=“sleuth” 表示整合springcloud.sleuth- -->
  <RedisAppender name="redisAppender" appName="plumelog" redisHost="172.16.249.72" redisPort="6379" redisAuth="123456">
      <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] [%-5p] {%F:%L} - %m%n" />
  </RedisAppender>
  </appenders>
  <!-- 上面两个配置二选一 -->
  <loggers>
      <root level="INFO">
          <appender-ref ref="redisAppender"/>
      </root>
  </loggers>
```    
#### （3）示例(所有的列子都在plumelog-demo里面)

### 配置详解

RedisAppender

|  字段值   | 用途  |
|  ----  | ----  |
| appName  | 自定义应用名称 |
| redisHost  | redis地址 |
| redisPort  | redis端口号 |
| redisDb  | redis db |
| runModel  | 1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1 |
| expand  | 整合其他链路插件，启用这个字段 expand=“sleuth” 表示整合springcloud.sleuth |
| maxCount  | （3.1）批量提交日志数量，默认100 |
| logQueueSize  | （3.1.2）缓冲队列数量大小，默认10000，太小可能丢日志，太大容易内存溢出，根据实际情况，如果项目内存足够可以设置到100000+ |

KafkaAppender

|  字段值   | 用途  |
|  ----  | ----  |
| appName  | 自定义应用名称 |
| kafkaHosts  | kafka集群地址，用逗号隔开 |
| runModel  | 1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1 |
| expand  | 整合其他链路插件，启用这个字段 expand=“sleuth” 表示整合springcloud.sleuth |
| maxCount  | 批量提交日志数量，默认100 |
| logQueueSize  | （3.1.2）缓冲队列数量大小，默认10000，太小可能丢日志，太大容易内存溢出，根据实际情况，如果项目内存足够可以设置到100000+ |

* 普通日志使用

   要想产生traceID，需要再拦截器里增加，如下：(也可以加载过滤器里，如果是定时任务放在定时任务的最前端)
```java
        @Component
        public class Interceptor extends HandlerInterceptorAdapter{
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                String traceid= uuid.substring(uuid.length() - 7);
                TraceId.logTraceID.set(traceid);//设置TraceID值，不埋此点链路ID就没有
                return true;
            }
        }
```   
  spring cloud 项目引入sleuth
```xml
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-sleuth</artifactId>
            </dependency>
``` 
   skywalking traceid获取方式
```java
     String traceId = TraceContext.traceId();  
``` 
* 扩展字段MDC用法，例如
```java
            MDC.put("orderid", "1");
            MDC.put("userid", "4");
            logger.info("扩展字段");
``` 
* 错误报警说明

   在ui的报警管理里配置报警规则：
   
   字段说明：
     
     1.应用名称   需要错误报警的应用名称（appName）
     
     2.模块名称   需要错误报警的className
     
     3.接收人  填手机号码，所有人填写ALL
     
     4.钩子地址  群机器人webhook地址
     
     5.错误数量 错误累计超过多少条报警
     
     6.时间间隔  错误在多少秒内累计到上面错误数量开始报警
     
   报警记录里为报警历史记录，点击可以直接连接到错误内容
   1.在系统扩展字段里添加扩展字段，字段值为 orderid 显示值为 订单编号
   2.查询的时候选择应用名，下面会显示扩展字段，可以通过扩展字段查询

* [链路追踪使用点我](/plumelog-trace/README.md)  《==要想产生链路信息请看这边文档，否则没有链路信息展示

* TraceId跨线程传递

    如果不使用线程池，不用特殊处理，如果使用线程池，有两种使用方式，（plumelog-demo也有）

    #### 修饰线程池

```java
        private static ExecutorService executorService = TtlExecutors.getTtlExecutorService(
                    new ThreadPoolExecutor(8, 8,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>()));
          //省去每次Runnable和Callable传入线程池时的修饰，这个逻辑可以在线程池中完成      
          executorService.execute(() -> {
                      logger.info("子线程日志展示");
          });
```        
   #### 修饰Runnable和Callable
   
```java
        private static ThreadPoolExecutor threadPoolExecutor= ThreadPoolUtil.getPool(4, 8, 5000);
        
        threadPoolExecutor.execute(TtlRunnable.get(() -> {
                   TraceId.logTraceID.get();
                   logger.info("tankSay =》我是子线程的日志！{}", TraceId.logTraceID.get());
         }));
```       
* [Dubbo的分布式系统traceId传递点我 ](/plumelog-dubbo/README.md)

* springcloud(fegin)的分布式系统traceId传递,参考plumelog-rest项目

* [docker版本安装点我](/docker-file/DOCKER.md)

### 三、联系交流

   * QQ群：1072991065
   
### 四、测试地址（暂时停用）

   * 查询界面地址：http://demo.plumelog.com
      
   * 访问这个地址产生测试log数据：http://demo.plumelog.com/demo/index?data=1234  data参数自己随便传，传什么打印什么
