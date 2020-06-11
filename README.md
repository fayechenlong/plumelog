 ![avatar](/pic/icon.png)
 # Plumelog一个简单易用的java分布式日志组件
### 一.系统介绍

 1. 无入侵的分布式日志系统，基于log4j、log4j2、logback搜集日志，设置链路ID，方便查询关联日志
 
 2. 基于elasticsearch作为查询引擎
 
 3. 高吞吐，查询效率高
 
 4. 全程不占应用程序本地磁盘空间，免维护;对于项目透明，不影响项目本身运行
 
 5. 无需修改老项目，引入直接使用，支持dubbo,支持springcloud
 
### 二.架构

 ![avatar](/pic/plumelog.png)
 
* plumelog-core 核心组件包含日志搜集端，负责搜集日志并推送到kafka，redis等队列

* plumelog-server 负责把队列中的日志日志异步写入到elasticsearch 

* plumelog-ui 前端展示，日志查询界面

* plumelog-demo 基于springboot的使用案例

   
### 三.使用方法

   ### [点我快速开始](/FASTSTART.md)

  ### 自己编译安装如下
  
  ### 前提:kafka或者redis  和 elasticsearch（版本6.8以上最好） 自行安装完毕，版本兼容已经做了，理论不用考虑ES版本
    
* maven deploy -DskipTests 上传包到自己的私服
   
     私服地址到plumelog/pom.xml改
```xml
       <properties>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
          <distribution.repository.url>http://172.16.249.94:4000</distribution.repository.url>
        </properties>
```   
### [使用文档](/FASTSTART.md)

### 四.版本计划

   2.X功能:
   
   * 基于traceId的日志记录，日志查询，日志缓冲队列 redis或者kafka
   
   * 跨应用支持，dubbo,springCloud
   
   * 支持简单的链路追踪
   
   * 支持跨机房部署
   
   * 简单的日志统计功能
   
   
   3.0计划版本
   
   * 增加错误报警
   
   * 增加慢调用报警
   
   * 增加非java项目日志搜集功能，例如nginx
   
   * 更多功能大家可以建议

   
### 五.联系交流

   * QQ群：1072991065   
   
   * 遇到使用问题加群讨论，群主秒回
   
### 六.测试地址

   * 查询界面地址：http://demo.plumelog.com
      
   * 访问这个地址产生测试log数据：http://demo.plumelog.com/demo/index?data=1234  data参数自己随便传，传什么打印什么
