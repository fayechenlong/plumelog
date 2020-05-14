## EasyLog  easy_log_trace 提供链路日志

#### 使用注意事项

1. 引入当前包后，如果目录对不上，需要手动加入 @ComponentScan 把TraceAspect扫描进来否则可能不生效

2. 需要自己的项目引入aop的 （这里默认scope 为 provided）

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
                <version>2.1.11.RELEASE</version>
                <scope>provided</scope>
                <!-- scope 为 provided 是为了不与使用者的版本冲突-->
            </dependency>
            
3. 在需要记录的方法上加入 @Trace 就可以记录链路日志了 跨应用传送traceId 目前只支持dubbo

        @Trace
        public void testLog() {
            easyLogDubboService.testLogDubbo();
        }