## EasyLog  easy_log_dubbo 提供dubbo 跨服务传递traceID 

##### 使用注意事项

1.引入
    
                   <dependency>
                       <groupId>com.beeplay</groupId>
                       <artifactId>easy_log_dubbo</artifactId>
                       <version>2.0.RELEASE</version>
                   </dependency>
                         


2. dubbo依赖 scope是provided 所以需要自己引入依赖 当前filter是基于apache的版本。阿里版本需要自己写

            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo</artifactId>
                <version>2.7.3</version>
                <scope>provided</scope>
            </dependency>

3. 引入模块后rpc调用时就会自动传traceId 但是请注意如果自己有自定义的filter，名字不能与当前filter名重复


    traceIdFilter=com.beeplay.easylog.dubbo.filter.TraceIdFilter