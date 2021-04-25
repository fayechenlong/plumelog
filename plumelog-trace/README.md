## plumelog-trace 提供链路日志

#### 本模块原理是利用springAOP切面产生链路日志，核心是配置springAOP,配置之前不熟悉springAOP的建议先熟悉下

#### 使用注意事项，链路追踪模块会产生大量链路日志，并发高的模块不要过度使用，特别是全局打点

#### 手动打点和全局打点不能同时使用用了全局打点，手动的会失效

1. 引入
```xml
                    <dependency>
                       <groupId>com.plumelog</groupId>
                       <artifactId>plumelog-trace</artifactId>
                       <version>3.4.1</version>
                   </dependency>

```
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
   
  * 3.4.2之前版本如果不产生链路日志，请检查你的@ComponentScan目录是否包含 com.plumelog 这个目录，springboot/cloud用户看看自己的启动类在哪个目录下
  * 默认是从启动目录开始扫描，如果你的启动类放在自己的包路径下，要手动加上@ComponentScan({"com.plumelog","你项目的路径"})使得项目包含com.plumelog
  * 原理就是让扫描路径扫描到@Trace这个自定义注解

```java
        @Trace
        public void testLog() {
            easyLogDubboService.testLogDubbo();
        }
```
5. 全局打点 需要自己定义切入点 (demo 如下 )  当定义全局打点时，手动打点就会失效

```java
    @Aspect
    @Component
    public class AspectConfig extends AbstractAspect {
    
        @Around("within(com.xxxx..*))")//这边写自己的包的路径
        public Object around(JoinPoint joinPoint) {
            return aroundExecute(joinPoint);
        }
    }
```
6. 如果不想再自己的控制台或者文件输出里看到trace日志可以通过添加过滤器过滤掉,logback的例子如下

```xml
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <!--此过滤器过滤掉所有的trace日志，3.4.1版本logback自带的过滤类-->
        <filter class="com.plumelog.logback.util.FilterSyncLogger">
            <level>info</level>
            <filterPackage>com.plumelog.trace.aspect.AbstractAspect</filterPackage>
        </filter>
        <encoder>
            <Pattern>${CONSOLE_LOG_PATTERN}</Pattern>\
            <charset>UTF-8</charset>
        </encoder>
    </appender>
```
3.4之前的版本可以复制以下代码创建一个过滤器再配置到logback里面去

```java
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class FilterSyncLogger extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {

        String filterPackage = "com.plumelog.trace.aspect.AbstractAspect";

        if (getPackName(event.getLoggerName()).equals(filterPackage)
                || getPackName(event.getLoggerName()).equals(filterPackage)) {
            return FilterReply.DENY;
        } else {
            return FilterReply.ACCEPT;
        }
    }

    public String getPackName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

}
```