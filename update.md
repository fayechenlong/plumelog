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