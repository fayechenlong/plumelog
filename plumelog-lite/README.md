## plumelog-lite

#### plumelog-lite版本，不用部署直接引用到项目中直接使用

#### 功能包含，日志查询，链路追踪，日志管理，适合单机小规模项目使用,目前只支持springboot+logback，log4j2组合

1. 引入

```xml

<dependency>
    <groupId>com.plumelog</groupId>
    <artifactId>plumelog-lite</artifactId>
    <version>3.5</version>
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
```

3. 在springboot启动类里添加扫描路径

```java
@ComponentScan("com.plumelog")
```

##### 注意：如果你的项目访问plumelog页面空白，说明没有配置可以访问静态文件请做如下配置 在application.properties配置：

```properties
spring.mvc.static-path-pattern=/**
spring.resources.static-locations=classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/
```

##### 小提示，拦截器会覆盖spring.resources.static-locations，如果项目中有拦截器，需要在拦截器里配置静态文件访问

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