## plumelog-lite

#### plumelog-lite版本，不用部署直接引用到项目中直接使用

#### 功能包含，日志查询，链路追踪，日志管理，适合单机小规模项目使用,目前只支持springboot+logback组合

示例中plumelog相关版本号为示例，实际使用建议取最新的版本，最新的版如下

[最新的版本号：![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.plumelog/plumelog-lite/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.plumelog/plumelog)


1. 引入

```xml

<dependency>
    <groupId>com.plumelog</groupId>
    <artifactId>plumelog-lite</artifactId>
    <version>3.5.3</version>
</dependency>

```

* 或者引用plumelog-lite-spring-boot-starter，则不需要配置扫描路径和静态文件路径


```xml

<dependency>
    <groupId>com.plumelog</groupId>
    <artifactId>plumelog-lite-spring-boot-starter</artifactId>
    <version>3.5.3</version>
</dependency>

```


2. 配置logback.xml

```xml

<appender name="plumelog" class="com.plumelog.lite.logback.appender.LiteAppender">
    <appName>plumelog</appName>
    <!-- 日志存储位置 -->
    <logPath>/plumelog/lite</logPath>
    <!-- 日志保留天数 -->
    <keepDay>30</keepDay>
</appender>
        
        <!-- 添加 ref-->
    <root level="INFO">
    <appender-ref ref="plumelog"/>
    </root>

```

3. 在springboot启动类里添加扫描路径，注意：如果原来你的项目没有扫描路径，不要只加这个，也要把你自己的项目的加了，不然只扫描plumelog的路径了

```java
@ComponentScan("com.plumelog")
```

情况一：如果你的项目访问plumelog页面空白，说明没有配置可以访问静态文件请做如下配置 在application.properties配置：

```properties
spring.mvc.static-path-pattern=/**
spring.resources.static-locations=classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/
```

情况二：拦截器会覆盖spring.resources.static-locations，如果项目中有拦截器，需要在拦截器里配置静态文件访问

示例：

```java
import com.plumelog.core.PlumeLogTraceIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class TraceIdInterceptorsConfig extends WebMvcConfigurerAdapter{
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/"};
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //就是这句addResourceLocations，加上静态文件访问路径
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PlumeLogTraceIdInterceptor());
        super.addInterceptors(registry);
    }

}



```
4. 访问

启动你的项目：输入你的项目地址+plumelog/#/访问，例如：http://localhost:8083/plumelog/#/ 一定要加这个/#/后缀


5. 异常处理

* Lock held by this virtual machine 处理

有些用3.5版本会报错：org.apache.lucene.store.LockObtainFailedException: Lock held by this virtual machine

情况一：springcloud-alibaba需要在你的启动类里面加：System.setProperty("spring.cloud.bootstrap.enabled", "false");

示例：

```java
public static void main(String[] args) {
        System.setProperty("spring.cloud.bootstrap.enabled", "false");
        SpringApplication.run(LogServerStart.class, args);
        }

```

情况二：如果用的logback.xml 改成logback-spring.xml;

情况三：用3.5.1以后版本

* 项目不是根目录导致，滚动日志无法连接

用3.5.2版本