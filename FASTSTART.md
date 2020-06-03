 ![avatar](/pic/icon.png)
 # Plumelog一个简单易用的java分布式日志组件

### 一、架构

 ![avatar](/pic/plumelog.png)
 
### 二、使用方法

#### （1）安装
    
   1.安装 redis 或者 kafka（一般公司redis足够）
     
   2.安装 elasticsearch
    
   3.下载安装包，plumelog_ui和plumelog_server 下载地址： https://gitee.com/frankchenlong/plumelog/releases
   
   4.配置plumelog_server，并启动
   
   配置文件 plumelog_server/config/plumelog.properties 详解：

```properties
    #日志缓冲区，kafka，redis两种模式
    plumelog.server.model=redis
    
    #如果使用kafka，下面配置生效
    plumelog.server.kafka.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
    plumelog.server.kafka.kafkaGroupName=logConsumer
    
    #如果使用redis,下面配置生效
    plumelog.server.redis.redisHost=172.16.249.72:6379
    plumelog.server.redis.redisPassWord=
    
    #elasticsearch相关配置
    plumelog.server.es.esHosts=172.16.251.196:9200
    #ES没有密码的不用管这个配置
    plumelog.server.es.userName=elastic
    plumelog.server.es.passWord=plumelog123456
    
    #单次拉取日志条数
    plumelog.server.maxSendSize=5000
    #拉取时间间隔，kafka不生效
    plumelog.server.interval=100
```       
   5.配置plume_ui,并启动，默认端口8989
   
   配置文件 plumelog_ui/application.properties 详解：
   
 ```properties
    spring.application.name=plumelog_ui
    server.port=8989
    spring.thymeleaf.mode=LEGACYHTML5
    spring.mvc.view.prefix=classpath:/templates/
    spring.mvc.view.suffix=.html
    spring.mvc.static-path-pattern=/plumelog/**
    

    #elasticsearch地址
    es.esHosts=172.16.251.196:9200
    #ES没有密码的不用管这个配置
    es.userName=elastic
    es.passWord=easylog123456
    
    #管理密码，删除日志时候要输入的密码
    admin.password=123456
```

#### （2）项目使用

#### 性能排名，log4j2>logback>log4j 如果您的项目没有特殊需求，建议用log4j2

   （1）如果项目使用的log4j，引入
```xml
   <dependency>
       <groupId>com.plumelog</groupId>
       <artifactId>plumelog_log4j</artifactId>
       <version>2.1</version>
   </dependency>
```                       
   配置log4j配置文件，增加下面这个Appender
    
   kafka做为中间件
```properties
    log4j.appender.L=com.plumelog.core.appender.KafkaAppender
    #appName系统的名称(自己定义就好)
    log4j.appender.L.appName=easyjob
    log4j.appender.L.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
```
   redis做为中间件
```properties
    log4j.appender.L=com.plumelog.log4j.appender.RedisAppender
    log4j.appender.L.appName=easyjob
    log4j.appender.L.redisHost=172.16.249.72
    log4j.appender.L.redisPort=6379
    #redis没有密码这一项为空或者不需要
    log4j.appender.L.redisAuth=123456
```
   同理如果使用logback,和log4j2配置如下
    
#### logback

* 引入
```xml
   <dependency>
       <groupId>com.plumelog</groupId>
       <artifactId>plumelog_logback</artifactId>
       <version>2.1</version>
   </dependency>
```  
* 配置
```xml
    <!-- plumelog日志 --><!--redis没有密码 redisAuth 字段可以去掉一 -->
    <appender name="plumelog" class="com.plumelog.logback.appender.RedisAppender">
        <appName>plumelog</appName>
        <redisHost>172.16.249.72</redisHost>
        <redisAuth>123456</redisAuth>
        <redisPort>6379</redisPort>
    </appender>
   
    <appender name="plumelog" class="com.plumelog.logback.appender.KafkaAppender">
        <appName>plumelog</appName>
        <kafkaHosts>172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092</kafkaHosts>
    </appender>
  
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
       <artifactId>plumelog_log4j2</artifactId>
       <version>2.1</version>
   </dependency>       
```   
* 配置
```xml
  <KafkaAppender name="kafkaAppender" appName="plumelog" kafkaHosts="172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092" >
      <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] [%-5p] {%F:%L} - %m%n" />
  </KafkaAppender>
 <!--redis没有密码 redisAuth 字段可以去掉一 -->
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
#### （3）示例(所有的列子都在plumelog_demo里面)

* 普通日志使用

   要想产生traceID，需要再拦截器里增加，如下：
```java
        @Component
        public class Interceptor extends HandlerInterceptorAdapter{
        
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                IdWorker worker = new IdWorker(1,1,1);
                TraceId.logTraceID.set(String.valueOf(worker.nextId()));//设置TraceID值，不埋此点链路ID就没有
                return true;
            }
        }
```   

* [链路追踪使用](/plumelog_trace/README.md)

* TraceId跨线程传递

    如果不使用线程池，不用特殊处理，如果使用线程池，有两种使用方式，（plumelog_demo也有）

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
* [Dubbo的分布式系统traceId传递 ](/plumelog_dubbo/README.md)

* springcloud(fegin)的分布式系统traceId传递,参考plumelog_rest项目

### 三、联系交流

   * QQ群：1072991065
   
### 四、测试地址

   * 查询界面地址：http://demo.plumelog.com
      
   * 访问这个地址产生测试log数据：http://demo.plumelog.com/demo/index?data=1234  data参数自己随便传，传什么打印什么
