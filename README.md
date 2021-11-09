<p align="center" >
    <img src="https://gitee.com/plumeorg/plumelog/raw/master/pic/icon.png" width="150">
    <h3 align="center">Plumelog</h3>
    <p align="center">
        Plumelog一个简单易用的java分布式日志组件
</p>

[![star](https://gitee.com/plumeorg/plumelog/badge/star.svg?theme=gvp)](https://gitee.com/frankchenlong/plumelog/stargazers)
[![fork](https://gitee.com/plumeorg/plumelog/badge/fork.svg?theme=gvp)](https://gitee.com/frankchenlong/plumelog/members)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.plumelog/plumelog/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.plumelog/plumelog)

Star趋势图
[![Stargazers over time](https://whnb.wang/img/plumeorg/plumelog)](https://whnb.wang/plumeorg/plumelog)

### 一.系统介绍

 1. 无代码入侵的分布式日志系统，基于log4j、log4j2、logback搜集日志，设置链路ID，方便查询关联日志
 
 2. 基于elasticsearch作为查询引擎
 
 3. 高吞吐，查询效率高
 
 4. 全程不占应用程序本地磁盘空间，免维护;对于项目透明，不影响项目本身运行
 
 5. 无需修改老项目，引入直接使用，支持dubbo,支持springcloud
 
### 二.架构

 ![avatar](/pic/plumelog.png)
 
* plumelog-core 核心组件包含日志搜集端，负责搜集日志并推送到kafka，redis等队列

* plumelog-server 负责把队列中的日志日志异步写入到elasticsearch 

* plumelog-demo 基于springboot的使用案例

* plumelog-lite plumelog的嵌入式集成版本，免部署
   
### 三.使用方法

#### 使用前注意事项

* plumelog分三种启动模式，分别为redis,kafka,lite，外加嵌入式版本plumelog-lite

* lite模式，不依赖任何外部中间件直接启动使用，但是性能有限，一天10个G以内可以应付，还必须是SSD硬盘，适合管理系统类的小玩家

* redis,kafka模式可以集群分布式部署，适合大型玩家，互联网公司

* plumelog-lite plumelog的嵌入式集成版本，直接pom引用，嵌入在项目中，自带查询界面，适合单个独立小项目使用，外包软件的最佳伴侣

#### 1. [使用文档](/FASTSTART.md)

#### 2. [查询后台使用指南](/HELP.md)

#### 3. [版本升级注意事项](/update.md)

#### 4. [lite版本使用文档](/plumelog-lite/README.md)

### 四.重要版本
 
   |  版本   | 内容  |
   |  ----  | ----  |
   | 2.2.2  | 基于traceId的日志记录，日志查询，日志缓冲队列 redis或者kafka |
   | 3.0  | 增加错误报警模块，增加内容组合查询功能，日志分应用统计条数功能 |
   | 3.1  | 增加扩展字段功能（MDC），优化日志搜集性能，合并UI和server模块，减少部署成本 |
   | 3.2  | 重大升级优化，老用户赶紧升级 plumelog-server 到3.2|
   | 3.3  | 用做redis队列的时候，不同的服务可以使用不同的队列，极大增大了redis模式下的吞吐|
   | 3.4.1  | 可以配置日志压缩，redis使用LZ4压缩日志，redis使用LZ4压缩类型，支持redis集群模式和哨兵模式|
   | 3.5  | 增加了lite模式，lite模块，小玩家可以不用装redis和es啦！|

   * 发现严重bug问题版本，大家请跳过

   |  版本   | bug问题  |
   |  ----  | ----  |
   | 3.2  | 阿里巴巴springcloud会出现日志不能上传bug，升级到3.2.1|
   | 3.4  | 升级到3.4.1即可|
   
### 五.联系交流

   * QQ群：二群：609090331 
   
   * 遇到问题可以先提issue，实在处理不了的加群讨论；提问带上你的配置截图和运行截图
   

### 六.测试地址

   * 查询界面地址：http://demo.plumelog.com  用户名：admin 密码：admin   （测试服务器配置比较低大家简单体验即可）
      
   * 访问这个地址产生测试log数据：http://log.plumelog.com/index?data=1234  data参数自己随便传，传什么打印什么

### 七.界面截图

![avatar](/pic/0.png)

![avatar](/pic/1.png)

![avatar](/pic/2.png)

![avatar](/pic/3.png)

![avatar](/pic/4.png)

![avatar](/pic/5.png)

![avatar](/pic/6.png)

