package com.beeplay;

import static org.junit.Assert.assertTrue;

import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.core.util.GfJsonUtil;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws UnsupportedEncodingException {




        StringBuffer sent=new StringBuffer();

        String[] hostsAndPorts="172.16.251.196:9200".split(",");
        HttpHost[] httpHosts = new HttpHost[hostsAndPorts.length];
        for(int i=0;i<hostsAndPorts.length;i++){
            httpHosts[i] = HttpHost.create(hostsAndPorts[i]);
        }
        RestClient client = RestClient.builder(httpHosts).build();


        for(int a=0;a<100;a++) {
            LogMessage logMessage = new LogMessage();
            logMessage.setAppName("11");
            logMessage.setMethod("aaa");
            logMessage.setContent(String.valueOf(a));
            String ent="{\"index\":{\"_id\":\""+UUID.randomUUID().toString()+"\"}} ";
            sent.append(ent);
            sent.append("\r\n");
            sent.append(GfJsonUtil.toJSONString(logMessage));
            sent.append("\r\n");
        }
        //sent.delete(sent.length()-2,sent.length());
        System.out.println(sent.toString());
        Request request = new Request(
                "POST",
                "/productindex/product/_bulk");
        request.setJsonEntity(sent.toString());

        client.performRequestAsync(request, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {

                System.out.println("onSuccess");

            }

            @Override
            public void onFailure(Exception exception) {
                System.out.println(exception.getMessage());
            }
        });

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
