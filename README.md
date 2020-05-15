# EasyLog 一个简单易用的java分布式日志组件
### 1.系统介绍

 1. 无入侵的分布式日志系统，基于log4j、log4j2、logback搜集日志，设置链路ID，方便查询关联日志
 
 2. 基于elasticsearch作为查询引擎
 
 3. 高吞吐，查询效率高
 
 4. 全程日志不落磁盘，免维护
 
 5. 无需修改老项目，引入直接使用
 
       

### 2.架构

* easy_log_core 核心组件包含日志搜集端，负责搜集日志并推送到kafka，redis等队列

* easy_log_server 负责把队列中的日志日志异步写入到elasticsearch 

* easy_log_ui 前端展示，日志查询界面

* easy_log_demo 基于springboot的使用案例

### 3.系统流程
   1. easy_log_core 搜集日志发送到=>kafka或者redis
   
   2. easy_log_server kafka或者redis=>elasticsearch
   
### 4.使用方法

1. 打包

* maven install -DskipTests 打包
   
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
                       <version>2.0</version>
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
    
    这里注意：spring boot项目默认用的logback;easy_log_demo里面有基于springboot的使用demo
    
    #### 【dubbo rpc 跨服务传送trace_id 可以参考 easy_log_dubbo】
    
    
#### logback

* 引入
    
       <dependency>
           <groupId>com.beeplay</groupId>
           <artifactId>easy_log_logback</artifactId>
           <version>2.0</version>
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
           <version>2.0</version>
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

* 要想产生traceID，需要再拦截器里增加，如下：

        @Component
        public class Interceptor extends HandlerInterceptorAdapter{
        
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                IdWorker worker = new IdWorker(1,1,1);
                TraceId.logTraceID.set(String.valueOf(worker.nextId()));//设置TraceID值，不埋此点链路ID就没有
                return true;
            }
        }


* 链路追踪使用 [传送门](/easy_log_trace/README.md)

* 跨线程追踪使用 有两种使用方式，（easy_log_demo也有）

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
      
       
   #### 以上两种方式执行都支持traceId传递。如果不使用线程的方式。默认就是支持传递
   
4. 启动服务

 * 步骤一打包完的 启动 easy_log_server-1.0.jar ，高可用的话直接启动多个服务就行

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
       
  * 查询界面
     
     1.到easy_log_ui界面下，进入src目录 修改配置文件 config.json
     
     注意：需要自行安装nodejs环境
     
          {
              "es": "http://172.16.251.196:9200/", //es地址
              "port" : 8989 //端口号，和上面api端口号
          }
     
     2.运行 npm run build 打包
     
     3.dist为打包后的目录，进入这个目录运行  node app
     
     4.http://你的部署服务器地址:8989 访问前端
     
### 5.版本计划

   2.0版本增加全链路追踪功能，优化日志抓取写入，可以做生产用，1.0是demo版本，只做思路上的参考
   
   3.0计划版本
   
      1.增加获取非java项目日志API接口
      
      2.增加性能分析，和API超时、时间窗口错误数、等信息告警功能
   
### 6.联系交流

   * 有问题在留言区留言，会在第一时间内回复
      
   * 欢迎有兴趣的一起加入研究；QQ群：1072991065
   
  ![avatar](/pic/EasyLog官方群群二维码.png)
### 7.测试地址

   * 查询界面地址：https://easylog-demo.beeplaying.com
      
   * 访问这个地址产生测试log数据：https://easylog-demo.beeplaying.com/demo/index?data=1234  data参数自己随便传，传什么打印什么
