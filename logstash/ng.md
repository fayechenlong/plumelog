## nginx日志搜集

### 一、配置nginx

* 复制nginx.conf里面的配置到自己的nginx配置之下，在http{}中添加下面配置

       log_format json '{"dtTime":"$time_iso8601",'
                   '"logLevel":"INFO",'
                   '"className":"$uri",'
                   '"content":"requestParam=>$query_string \n status=>$status \n size=>$body_bytes_sent \n request_time=>$request_time",'
                   '"serverName":"$server_addr",'
                   '"traceId":"$remote_addr",'
                   '"method":"$http_user_agent",'
                   '"appName":"nginx"'
                   '}';
       access_log  /var/log/nginx/access.log  json;

* nginx -t 后 nginx  -s reload  

### 二、下载并配置logstash

* 下载logstash并安装

1、下载logstash-6.6.0安装包，下载路径：logstash-6.6.0，然后解压之es的同级目录（方便管理）；或直接在服务器上下载：

    wget https://artifacts.elastic.co/downloads/logstash/logstash-6.6.0.tar.gz

2、将安装包上次到服务器，然后解压安装包，例如解压到：/usr/local/

    tar –zxvf logstash-6.6.0.tar.gz 

3、重命名安装目录
     
    mv logstash-6.6.0 logstash

* 配置logstash

1.进入logstash 安装目录

     mkdir nginx-log
     cd nginx-log
     vim logstash.conf

   * 配置内容


     input {
        file {
                path => "/var/log/nginx/access.log" #这边和上面nginx的日志输出地址一致
                type => "nginx_log"
                start_position => "beginning"
                stat_interval => "2"
        }
     filter {
         json {
               source=> "message"
           }
         date {
               match => ["dtTime", "ISO8601"]
              }
         ruby{
               code => "event.set('dtTime',(event.get('@timestamp').to_f.round(3)*1000).to_i)"
             }
         mutate {
               remove_field => ["message"]
             }
       }
      output {
         if[type] =="nginx_log"{
                redis {
                        data_type => "list" #这个不用改
                        host => "10.100.2.54" #plumelog的queue.redis地址
                        db => "0" #plumelog的queue.redis DB
                        port => "6379" #plumelog.queue.redis的端口
                        password => "plumelogredis" #plumelog.queue.redis的密码
                        key => "plume_log_list" #这个不用改
                }
        }
    }
                 


复制logstash.conf内容进去，并保存；可以直接把logstash.conf放到这个目录下

2.启动logstash

进入logstash/bin目录，用下面命令启动

    ./logstash -f ../nginx-log/logstash.conf
     