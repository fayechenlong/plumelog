<p align="center" >
    <img src="https://gitee.com/frankchenlong/plumelog/raw/master/pic/icon.png" width="150">
    <h3 align="center">Plumelog</h3>
    <p align="center">
        Plumelog一个简单易用的java分布式日志组件
</p>

[![star](https://gitee.com/frankchenlong/plumelog/badge/star.svg?theme=gvp)](https://gitee.com/frankchenlong/plumelog/stargazers)
[![fork](https://gitee.com/frankchenlong/plumelog/badge/fork.svg?theme=gvp)](https://gitee.com/frankchenlong/plumelog/members)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.plumelog/plumelog/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.plumelog/plumelog)
[![Fork me on Gitee](https://gitee.com/frankchenlong/plumelog/widgets/widget_1.svg)](https://gitee.com/frankchenlong/plumelog)
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

* plumelog-demo 基于springboot的使用案例

* 下面是全功能图，红色部分是4.0内容，目前开发中

 ![avatar](/pic/guihua.png)
   
### 三.使用方法

   ### [点我快速开始](/FASTSTART.md)

  ### 自己编译安装如下
  
  ### 前提:kafka或者redis  和 elasticsearch 自行安装完毕，版本兼容已经做了，理论不用考虑ES版本
    
1. 打包

* maven deploy -DskipTests 上传包到自己的私服
   
     私服地址到plumelog/pom.xml改
```xml
       <properties>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
          <distribution.repository.url>http://172.16.249.94:4000</distribution.repository.url>
        </properties>
```   
* 非maven项目，到发行版中（https://gitee.com/frankchenlong/plumelog/releases ）下载lib.zip，解压放到自己的lib目录，目前只上传了log4j的版本
  可能会涉及log4j jar冲突，需要自行排除
  
* jdk1.6的项目下载源码，编译打包plumelog-client-jdk6，引入到自己的项目
  
### [使用文档](/FASTSTART.md)

### [版本升级注意事项](/update.md)

### 四.重要版本
 
   |  版本   | 内容  |
   |  ----  | ----  |
   | 2.2.2  | 基于traceId的日志记录，日志查询，日志缓冲队列 redis或者kafka |
   | 3.0  | 增加错误报警模块，增加内容组合查询功能，日志分应用统计条数功能 |
   | 3.1  | 增加扩展字段功能（MDC），优化日志搜集性能，合并UI和server模块，减少部署成本 |
   | 3.2  | 重大升级优化，老用户赶紧升级 plumelog-server 到3.2|
   | 3.3  | 用做redis队列的时候，不同的服务可以使用不通的队列，极大增大了redis模式下的吞吐|
   
### 五.联系交流

   * QQ群：1072991065   
   
   * 遇到使用问题加群讨论
   

### 六.测试地址

   * 查询界面地址：http://demo.plumelog.com  用户名：admin 密码：admin   （测试服务器配置比较低大家简单体验即可）
      
   * 访问这个地址产生测试log数据：http://log.plumelog.com/index?data=1234  data参数自己随便传，传什么打印什么
