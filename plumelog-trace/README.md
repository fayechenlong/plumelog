## plumelog  plumelog-trace 提供链路日志

#### 使用注意事项，链路追踪模块会产生大量链路日志，并发高的模块不要过度使用，特别是全局打点

1. 引入
```xml
                    <dependency>
                       <groupId>com.plumelog</groupId>
                       <artifactId>plumelog-trace</artifactId>
                       <version>3.3</version>
                   </dependency>

```
2. 引入当前包后，如果还不产生链路日志，请检查你的@ComponentScan目录是否包含 com.plumelog 这个目录，springboot/cloud用户看看自己的启动类在哪个目录下
   没有加@ComponentScan，默认是从启动目录开始扫描，如果你的启动类放在自己的包路径下，要手动加上@ComponentScan({"com.plumelog","你项目的路径"})使得项目包含com.plumelog


3. 需要自己的项目引入aop的 （这里默认scope 为 provided）
```xml
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
                <version>2.1.11.RELEASE</version>
                <scope>provided</scope>
                <!-- scope 为 provided 是为了不与使用者的版本冲突-->
            </dependency>
```         
4. 手动打点 在需要记录的方法上加入 @Trace 就可以记录链路日志了
   
```java
        @Trace
        public void testLog() {
            easyLogDubboService.testLogDubbo();
        }
```
5. 全局打点 需要自己定义切入点 (demo 如下 )  当定义全局打点时。手动打点就会失效

```java
    @Aspect
    @Component
    public class AspectConfig extends AbstractAspect {
    
        @Around("within(com.plumelog..*))")
        public Object around(JoinPoint joinPoint) {
            return aroundExecute(joinPoint);
        }
    }
```