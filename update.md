 ## 版本升级日历
 
 ### 3.3版本
 
 #### 升级到3.3版本注意事项（2020-12-17）本版本查询性能上做了很大与优化
  
 * client没有变化可以不用升级
 
 * 版本内容
 
   1. 拆分redis队列和redis管理，支持不同的项目不同的队列
   
   2. 报警支持类名模糊匹配
   
   3. 报警信息输出部分详细日志
   
   4. 开放了API接口方便非java项目，或者其他组件日志输入
   
   5. 支持按小时分片日志，大大增加了性能，使得在机械硬盘上也能够跑出不错的效果
   
   6. 其他内部优化
   
 
 * server配置进行了变化，增加了配置，原来的配置不变
    
|  增加配置   | 用途  |
|  ----  | ----  |
|  plumelog.queue.redis.redisHost   | 队列redis独立开来，好处不同的项目可以用不通的redis当队列，提高性能 |
|  plumelog.es.shards   | 单个日志分片数，如果每个索引日志大小超过20个G这边要配置大小，默认5个  |
|  plumelog.es.replicas   | 副本数，日志大了要提升检索效率可以增加副本数，一般机械硬盘下面可以设置1，SSD设置0，设置副本增大成倍硬盘开销  |
|  plumelog.es.refresh.interval   | 日志缓冲区时间，默认30秒  |
|  plumelog.es.indexType.model   | 按天，还是按小时划分日志索引，单天超过200G，可以设置按小时，设置按小时要注意设置ES总分片数  |
|  admin.log.trace.keepDays   | 链路日志可以单独设置过期时间  |
|  login.username   | 登录用户名  |
|  login.password   | 登录密码 |

* 推荐配置

   #### 提升性能推荐参考配置方法
      
    #### 单日日志体量在50G以内，并使用的SSD硬盘
      
      plumelog.es.shards=5
      
      plumelog.es.replicas=0
      
      plumelog.es.refresh.interval=30s
      
      plumelog.es.indexType.model=day
      
    #### 单日日志体量在50G以上，并使用的机械硬盘
      
      plumelog.es.shards=5
      
      plumelog.es.replicas=0
      
      plumelog.es.refresh.interval=30s
      
      plumelog.es.indexType.model=hour
      
    #### 单日日志体量在100G以上，并使用的机械硬盘
      
      plumelog.es.shards=10
      
      plumelog.es.replicas=0
      
      plumelog.es.refresh.interval=30s
      
      plumelog.es.indexType.model=hour
      
    #### 单日日志体量在1000G以上，并使用的SSD硬盘，这个配置可以跑到10T一天以上都没问题
      
      plumelog.es.shards=10
      
      plumelog.es.replicas=1
      
      plumelog.es.refresh.interval=30s
      
      plumelog.es.indexType.model=hour
      
    #### plumelog.es.shards的增加和hour模式下需要调整ES集群的最大分片数
      
       PUT /_cluster/settings
       {
         "persistent": {
           "cluster": {
             "max_shards_per_node":100000
           }
         }
       }

### 3.4版本

#### 升级到3.4版本注意事项（2021-02-4）本版本提供了日志压缩功能


* client

    1. kafka提供了'compressionType'字段配置kafka的压缩模式

    2. redis提供了'compressor'字段配置来开启日志压缩功能，开启后会使用新的队列
    
* server

    1. 提供消费压缩队列的配置：plumelog.redis.compressor

    2. 开启消费压缩队列后server端讲不再消费非压缩队列，版本升级时记得消费完相应队列数据

* 版本内容

    1. 支持日志压缩

* 实测压缩率，使用demo接口

![实测压缩率](/pic/compress.png)


### 3.4.2版本

* client 增加了环境配置字段env
* server 增加了环境配置字段env,ui增加了控制台风格的显示模式,优化内存管理


### 3.5版本

* 增加lite启动模式，此时不需要配置redis和es，lite模式下，扩展字段，错误统计，错误报警都不能使用，适合小规模项目使用
* 增加了plumelog-lite模块，plumelog-lite作为plumelog依赖包的形式存在，直接引用直接使用，无需部署，嵌入到项目中
* 增加日志控制台，查看实时输出情况，在部署和测试时候堪称神器，打开控制台的时候会影响性能，注意使用时机
* 修复了链路追踪，有可能最外层不能显示的bug
* 增加自动检测ES版本，不需要再配置
* 增加自动配置ES最大分片数量，不需要去手动设置
* 优化了界面，优化报警界面出界保存按钮显示一半的bug
* 优化redis模式下配置，如果所有应用只用一个队列redis，管理用redis可以不用配置，会自动启用队列的redis作为管理redis
* 修复已经已知的bug,其他优化
* 老用户升级直接替换plumelog-server-3.5.jar，重启即可
* lite模式，需要升级客户端到3.5
* 内嵌springboot-admin，方便管理springboot项目，可以利用springbootadmin,动态调整日志输出级别
