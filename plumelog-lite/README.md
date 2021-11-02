## plumelog-lite

#### plumelog-lite版本，不用部署直接引用到项目中直接使用

#### 功能包含，日志查询，链路追踪，日志管理，适合单机小规模项目使用,目前只支持springboot+logback组合

1. 引入

```xml

<dependency>
    <groupId>com.plumelog</groupId>
    <artifactId>plumelog-lite</artifactId>
    <version>3.5</version>
</dependency>

```

3. 配置

```xml
<appender name="plumelog" class="com.plumelog.lite.logback.appender.LiteAppender">
    <appName>plumelog</appName>
    <!-- 日志存储位置 -->
    <logPath>/plumelog/lite</logPath>
    <!-- 日志保留天数 -->
    <keepDay>30</keepDay>
</appender>
```

##### 注意：如果你的项目没有配置可以访问静态文件请做如下配置 在application.properties配置：

```properties
        spring.thymeleaf.mode=LEGACYHTML5
        spring.mvc.view.prefix=classpath:/templates
        spring.mvc.view.suffix=.html
        spring.mvc.static-path-pattern=/**
```

4. 访问

启动你的项目：输入你的项目地址+plumelog/#/访问，例如：http://localhost:8083/plumelog/#/ 一定要加这个/#/后缀