## EasyLog  plumelog 提供集成sleuth的日志输出

#### 集成sleuth 使用注意事项

```xml

<!--使用redis启用下面配置-->
        <!-- 字段说明 -->
        <!-- appName:应用名称 -->
        <!-- redisHost：redis地址 -->
        <!-- redisPort：redis端口号 不配置，默认使用6379-->
        <!-- runModel：runModel 1,2  1表示最高性能模式，2表示低性能模式 但是2可以获取更多信息 不配置默认为1- -->
        <!-- expand：整合其他链路插件，启用这个字段 expand=“sleuth” 表示整合springcloud.sleuth- -->
        <RedisAppender name="plumelog_demo" appName="plumelog_rest" redisHost="172.16.249.72" expand="sleuth"/>
```

##### 当 expand=“sleuth” 时，plumelog 会将sleuth生成的traceid覆盖默认生成的。
##### 也就是如果是用plumelog + sleuth 那plumelog就不需要再到拦截器 或者过滤器内生成traceId.

##### 由于plumelog已经支持跨线程传递traceId，故不需要自己再去实现使用如下

```java

 public String getIndex(){
        try {
            Integer.parseInt("aaa");
        } catch (NumberFormatException e) {
            log.error("error:{}",e);
        }
        threadPoolExecutor.execute(TtlRunnable.get(() -> {
            log.info("tankSay =》我是子线程的日志！");
        }));
       return  feignClientTest.testFeign(100);
    }
```