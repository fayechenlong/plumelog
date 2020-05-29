# PlumeLog(EasyLog) 一个简单易用的java分布式日志组件
### 一.系统介绍

 1. 无入侵的分布式日志系统，基于log4j、log4j2、logback搜集日志，设置链路ID，方便查询关联日志
 
 2. 基于elasticsearch作为查询引擎
 
 3. 高吞吐，查询效率高
 
 4. 全程不占应用程序本地磁盘空间，免维护
 
 5. 无需修改老项目，引入直接使用
 
       

### 二.架构

* easy_log_core 核心组件包含日志搜集端，负责搜集日志并推送到kafka，redis等队列

* easy_log_server 负责把队列中的日志日志异步写入到elasticsearch 

* easy_log_ui 前端展示，日志查询界面

* easy_log_demo 基于springboot的使用案例

### 三.系统流程
   1. easy_log_core 搜集日志发送到=>kafka或者redis
   
   2. easy_log_server kafka或者redis=>elasticsearch
   
### 四.使用方法

  ###前提:kafka或者redis  和 elasticsearch（版本6.8以上最好） 自行安装完毕，版本兼容已经做了，理论不用考虑ES版本
    
1. 打包

* maven deploy -DskipTests 上传包到自己的私服
   
     私服地址到easy_log/pom.xml改
     
            <properties>
              <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
              <distribution.repository.url>http://172.16.249.94:4000</distribution.repository.url>
            </properties>

2. 配置

#### 性能排名，log4j2>logback>log4j 如果您的项目没有特殊需求，建议用log4j2

   （1）如果用log4j，引入
    
                   <dependency>
                       <groupId>com.beeplay</groupId>
                       <artifactId>easy_log_log4j</artifactId>
                       <version>2.0.RELEASE</version>
                   </dependency>
                         
   配置log4j配置文件，增加下面这个Appender
    
   kafka做为中间件
    
        log4j.appender.L=com.beeplay.easylog.core.appender.KafkaAppender
        #appName系统的名称(自己定义就好)
        log4j.appender.L.appName=easyjob
        log4j.appender.L.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092

   redis做为中间件
    
        log4j.appender.L=com.beeplay.easylog.log4j.appender.RedisAppender
        log4j.appender.L.appName=easyjob
        log4j.appender.L.reidsHost=172.16.249.72
        log4j.appender.L.redisPort=6379

   同理如果使用logback,和log4j2配置如下
    
#### logback

* 引入
    
       <dependency>
           <groupId>com.beeplay</groupId>
           <artifactId>easy_log_logback</artifactId>
           <version>2.0.RELEASE</version>
       </dependency>
    
* 配置
    
        <!-- easylog日志 -->
        <appender name="easylog" class="com.beeplay.easylog.logback.appender.RedisAppender">
            <appName>easylog</appName>
            <reidsHost>172.16.249.72</reidsHost>
            <redisPort>6379</redisPort>
        </appender>
       
        <appender name="easylog" class="com.beeplay.easylog.logback.appender.KafkaAppender">
            <appName>easylog</appName>
            <kafkaHosts>172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092</kafkaHosts>
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
           <artifactId>easy_log_log4j2</artifactId>
           <version>2.0.RELEASE</version>
       </dependency>       

* 配置

          <KafkaAppender name="kafkaAppender" appName="easyjob" kafkaHosts="172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092" >
              <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] [%-5p] {%F:%L} - %m%n" />
          </KafkaAppender>
    
          <RedisAppender name="redisAppender" appName="easyjob" reidsHost="172.16.249.72" redisPort="6379" >
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
  
3. 示例(所有的列子都在easy_log_demo里面)

* 普通日志使用

   要想产生traceID，需要再拦截器里增加，如下：

        @Component
        public class Interceptor extends HandlerInterceptorAdapter{
        
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                IdWorker worker = new IdWorker(1,1,1);
                TraceId.logTraceID.set(String.valueOf(worker.nextId()));//设置TraceID值，不埋此点链路ID就没有
                return true;
            }
        }


* [链路追踪使用](/easy_log_trace/README.md)

* TraceId跨线程传递

    如果不使用线程池，不用特殊处理，如果使用线程池，有两种使用方式，（easy_log_demo也有）

    #### 修饰线程池


        private static ExecutorService executorService = TtlExecutors.getTtlExecutorService(
                    new ThreadPoolExecutor(8, 8,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>()));
          //省去每次Runnable和Callable传入线程池时的修饰，这个逻辑可以在线程池中完成      
          executorService.execute(() -> {
                      logger.info("子线程日志展示");
          });
      
      
      
   #### 修饰Runnable和Callable
   

        private static ThreadPoolExecutor threadPoolExecutor= ThreadPoolUtil.getPool(4, 8, 5000);
        
        threadPoolExecutor.execute(TtlRunnable.get(() -> {
                   TraceId.logTraceID.get();
                   logger.info("tankSay =》我是子线程的日志！{}", TraceId.logTraceID.get());
         }));
      
* [Dubbo的分布式系统traceId传递 ](/easy_log_dubbo/README.md)

   
4. 启动服务

 * 步骤一打包完的 启动 easy_log_server-2.0.RELEASE.jar ，高可用的话直接启动多个服务就行

   注意：打完的包target目录下，lib文件夹（依赖包目录），config文件夹（两个配置文件的目录），easy_log_server-1.0.jar 放到同一个目录下
  
 * easy_log_server中easylog.properties详解    
        
       #日志缓冲区，kafka，redis两种模式
       easylog.server.model=kafka
       #kafka集群地址
       easylog.server.host.kafkaHosts=172.16.247.143:9092,172.16.247.60:9092,172.16.247.64:9092
       #如果是redis 这边填写redis地址
       easylog.server.redis.redisHost=172.16.249.72:6379
       #elasticsearch集群地址
       easylog.server.host.esHosts=172.16.251.196:9200
       #每次获取最大日志条数
       easylog.server.maxSendSize=5000
       #日志读取频次，单位毫秒
       easylog.server.interval=100
       
  * 查询界面
     
     1.到easy_log_ui 配置 application.properties 中  es.esHosts 配置esapi地址 启动easy_log_ui-2.0.RELEASE.jar就行了
     
     2.[前端打包文档](/easy_log_ui/README.md)，也可以不用打包，easy_log_ui里面已经有一份打包好了的，如果自己修改代码那就要打包了
     
     3.界面介绍
     
     ![avatar](/pic/jiaocheng.jpg)
     
### 5.版本计划

   2.0版本增加全链路追踪功能，优化日志抓取写入，可以做生产用，1.0是demo版本，只做思路上的参考
   
   3.0计划版本
   
      1.增加获取非java项目日志API接口
      
      2.增加性能分析，和API超时、时间窗口错误数、等信息告警功能
   
### 6.联系交流

   * QQ群：1072991065
   
### 7.测试地址

   * 查询界面地址：https://easylog-demo.beeplaying.com
      
   * 访问这个地址产生测试log数据：https://easylog-demo.beeplaying.com/demo/index?data=1234  data参数自己随便传，传什么打印什么
