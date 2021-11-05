# Plumelog

官网: http://www.plumelog.com

Plumelog一个简单易用的java分布式日志组件

 1. 无入侵的分布式日志系统，基于log4j、com.plumelog.lite.log4j2、logback搜集日志，设置链路ID，方便查询关联日志
 
 2. 基于elasticsearch作为查询引擎
 
 3. 高吞吐，查询效率高
 
 4. 全程不占应用程序本地磁盘空间，免维护;对于项目透明，不影响项目本身运行
 
 5. 无需修改老项目，引入直接使用，支持dubbo,支持springcloud

# 使用环境要求

使用前请先安装以下组件

1.安装 redis 或者 kafka

>一般公司redis足够

redis 官网:https://redis.io 

kafka 官网：http://kafka.apache.org

2.安装 elasticsearch 

elasticsearch 官网:https://www.elastic.co