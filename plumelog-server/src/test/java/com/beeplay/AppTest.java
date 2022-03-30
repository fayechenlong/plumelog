package com.beeplay;


import com.plumelog.server.client.ElasticLowerClient;
import org.junit.Test;
import redis.embedded.RedisServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {

        RedisServer redisServer;
        redisServer = RedisServer.builder()
                .port(6379) //端口
                .setting("bind 127.0.0.1") //绑定ip
                .setting("requirepass 123456") //设置密码
                .build();
        redisServer.start();

    }

    private List<String> getIndex(String indexStr) {
        List<String> indexsList = new ArrayList<>();
        String[] indexs = indexStr.split(",");
        for (int i = 0; i < indexs.length; i++) {
            if (indexs[i].contains("*")) {
                File dir = new File(".");
                if (dir.isDirectory()) {
                    //获取当前目录下的所有子项
                    File[] subs = dir.listFiles();
                    for (File sub : subs) {
                        String name = sub.getName();
                        if (Pattern.matches(indexs[i].replace("*", ".*"), name)) {
                            indexsList.add(name);
                        }
                    }
                }
            } else {
                indexsList.add(indexs[i]);
            }
        }
        return indexsList;
    }
}
