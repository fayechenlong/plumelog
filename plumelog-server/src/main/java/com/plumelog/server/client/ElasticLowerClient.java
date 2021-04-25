package com.plumelog.server.client;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.server.InitConfig;
import com.plumelog.server.client.http.SkipHostnameVerifier;
import com.plumelog.server.client.http.SkipSslVerificationHttpRequestFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.*;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * className：ElasticLowerClient
 * description：ElasticLowerClient
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class ElasticLowerClient {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(ElasticLowerClient.class);
    private static ElasticLowerClient instance;
    private RestClient client;

    public static ElasticLowerClient getInstance(String hosts, String userName, String passWord, boolean trustSelfSigned, boolean hostnameVerification) {
        if (instance == null) {
            synchronized (ElasticLowerClient.class) {
                if (instance == null) {
                    instance = new ElasticLowerClient(hosts, userName, passWord, trustSelfSigned, hostnameVerification);
                }
            }
        }
        return instance;
    }

    /**
     * 带密码认证的
     *
     * @param hosts
     * @param userName
     * @param passWord
     */
    public ElasticLowerClient(String hosts, String userName, String passWord, boolean trustSelfSigned, boolean hostnameVerification) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, passWord));  //es账号密码
        String[] hostsAndPorts = hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostsAndPorts.length];
        for (int i = 0; i < hostsAndPorts.length; i++) {
            httpHosts[i] = HttpHost.create(hostsAndPorts[i]);
        }
        client = RestClient.builder(httpHosts).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.disableAuthCaching();
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

                // 设置ssl配置
                if (trustSelfSigned && !hostnameVerification) {
                    httpClientBuilder.setSSLContext(SkipSslVerificationHttpRequestFactory.getSSLContext());
                    httpClientBuilder.setSSLHostnameVerifier(new SkipHostnameVerifier());
                } else if (trustSelfSigned) {
                    httpClientBuilder.setSSLContext(SkipSslVerificationHttpRequestFactory.getSSLContext());
                }

                return httpClientBuilder;
            }
        }).build();
    }

    /**
     * 带ssl认证的
     *
     * @param hosts
     * @param keyStorePass
     * @param sslFile
     * @param keyStoreName
     */
    public ElasticLowerClient(String hosts, String keyStorePass, String sslFile, String keyStoreName) {

        try {
            Path keyStorePath = Paths.get(sslFile);
            KeyStore truststore = KeyStore.getInstance(keyStoreName);
            try (InputStream is = Files.newInputStream(keyStorePath)) {
                truststore.load(is, keyStorePass.toCharArray());
            }
            SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
            final SSLContext sslContext = sslBuilder.build();
            String[] hostsAndPorts = hosts.split(",");
            HttpHost[] httpHosts = new HttpHost[hostsAndPorts.length];
            for (int i = 0; i < hostsAndPorts.length; i++) {
                httpHosts[i] = HttpHost.create(hostsAndPorts[i]);
            }
            client = RestClient.builder(httpHosts).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder.setSSLContext(sslContext);
                }
            }).build();
        } catch (Exception e) {
            logger.error("ElasticSearch init fail!", e);
        }
    }

    public boolean existIndice(String indice) {
        List<String> existIndexList = new ArrayList<String>();
        try {
            Request request = new Request(
                    "HEAD",
                    "/" + indice + "");
            Response res = client.performRequest(request);
            if (res.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return false;
    }

    public boolean creatIndice(String indice,String type) {
        List<String> existIndexList = new ArrayList<String>();
        try {
            Request request = new Request(
                    "PUT",
                    "/" + indice + "");
            String properties = "\"properties\":{\"appName\":{\"type\":\"keyword\"}," +
                    "\"logLevel\":{\"type\":\"keyword\"}," +
                    "\"serverName\":{\"type\":\"keyword\"}," +
                    "\"traceId\":{\"type\":\"keyword\"}," +
                    "\"dtTime\":{\"type\":\"date\",\"format\":\"strict_date_optional_time||epoch_millis\"}" +
                    "}";
            String ent = "{\"settings\":{\"number_of_shards\":"+ InitConfig.ES_INDEX_SHARDS+",\"number_of_replicas\":"+InitConfig.ES_INDEX_REPLICAS+",\"refresh_interval\":\""+InitConfig.ES_REFRESH_INTERVAL+"\"}";
            if(StringUtils.isEmpty(type)){
                ent=ent+",\"mappings\":{"+properties+"}}";
            }else {
                ent=ent+",\"mappings\":{\""+type+"\":{"+properties+"}}}";
            }

            request.setJsonEntity(ent);
            Response res = client.performRequest(request);
            if (res.getStatusLine().getStatusCode() == 200) {
                logger.info("creat indice {}",indice);
                return true;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return false;
    }
    public boolean creatIndiceTrace(String indice, String type) {
        List<String> existIndexList = new ArrayList<String>();
        try {
            Request request = new Request(
                    "PUT",
                    "/" + indice + "");
            String properties = "\"properties\":{\"appName\":{\"type\":\"keyword\"}," +
                    "\"traceId\":{\"type\":\"keyword\"}" +
                    "}";
            String ent = "{\"settings\":{\"number_of_shards\":"+ InitConfig.ES_INDEX_SHARDS+",\"number_of_replicas\":"+InitConfig.ES_INDEX_REPLICAS+",\"refresh_interval\":\""+InitConfig.ES_REFRESH_INTERVAL+"\"}";
            if(StringUtils.isEmpty(type)){
                ent=ent+",\"mappings\":{"+properties+"}}";
            }else {
                ent=ent+",\"mappings\":{\""+type+"\":{"+properties+"}}}";
            }
            request.setJsonEntity(ent);
            Response res = client.performRequest(request);
            if (res.getStatusLine().getStatusCode() == 200) {
                logger.info("creat indice {}",indice);
                return true;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return false;
    }
    public boolean creatIndiceNomal(String indice, String type) {
        List<String> existIndexList = new ArrayList<String>();
        try {
            Request request = new Request(
                    "PUT",
                    "/" + indice + "");
            String ent = "{\"settings\":{\"number_of_shards\":5,\"number_of_replicas\":0,\"refresh_interval\":\"10s\"}}";
            request.setJsonEntity(ent);
            Response res = client.performRequest(request);
            if (res.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return false;
    }

    public void insertListLog(List<String> list, String baseIndex, String type) throws IOException {

        if (!existIndice(baseIndex)) {
            if(baseIndex.startsWith(LogMessageConstant.ES_INDEX)) {
                creatIndice(baseIndex,type);
            }else {
                creatIndiceNomal(baseIndex,type);
            }
            logger.info("creatIndex:{}", baseIndex);
        }
        insertList(list,baseIndex,type);
    }
    public void insertListTrace(List<String> list, String baseIndex, String type) throws IOException {
        insertList(list,baseIndex,type);
    }
    public void insertListComm(List<String> list, String baseIndex, String type) throws IOException {
        insertList(list,baseIndex,type);
    }
    private void insertList(List<String> list, String baseIndex, String type) throws IOException {

        StringBuffer sendStr = new StringBuffer();
        int size=list.size();
        for (int a = 0; a < size; a++) {
            String map = list.get(a);
            String ent = "{\"index\":{} ";
            sendStr.append(ent);
            sendStr.append("\r\n");
            sendStr.append(map);
            sendStr.append("\r\n");
        }
        list=null;
        String endpoint = "";
        if (StringUtils.isEmpty(type)) {
            endpoint = "/" + baseIndex + "/_bulk";
        } else {
            endpoint = "/" + baseIndex + "/" + type + "/_bulk";
        }
        Request request = new Request(
                "PUT",
                endpoint);
        request.setJsonEntity(sendStr.toString());
        client.performRequestAsync(request, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                request.setEntity(null);
                try {

                    if(response.getStatusLine().getStatusCode()==200){
                        logger.info("ElasticSearch commit! success");
                    }else {
                        String responseStr = EntityUtils.toString(response.getEntity());
                        logger.error("ElasticSearch commit Failure! {}", responseStr);
                    }
                } catch (IOException e) {
                    logger.error("ElasticSearch commit Failure!", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                logger.error("ElasticSearch commit Failure!", e);
            }
        });
    }

    public String cat(String index) {
        String reStr = "";
        Request request = new Request(
                "GET",
                "/_cat/indices/" + index + "?v");
        try {
            Response res = client.performRequest(request);

            InputStream inputStream = res.getEntity().getContent();
            byte[] bytes = new byte[0];
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String str = new String(bytes);
            reStr = str;
        } catch (Exception e) {
            e.printStackTrace();
            reStr = "";
        }
        return reStr;
    }

    public String get(String url, String queryStr) {
        String reStr = "";
        StringEntity stringEntity = new StringEntity(queryStr, "utf-8");
        stringEntity.setContentType("application/json");
        Request request = new Request(
                "GET",
                url);
        request.setEntity(stringEntity);
        try {
            Response res = client.performRequest(request);
            return EntityUtils.toString(res.getEntity(), "utf-8");
        } catch (Exception e) {
            reStr = "";
            e.printStackTrace();
        }
        return reStr;
    }

    public List<String> getExistIndices(String[] indices) {
        List<String> existIndexList = new ArrayList<String>();
        for (String index : indices) {
            try {
                Request request = new Request(
                        "HEAD",
                        "/" + index + "");
                Response res = client.performRequest(request);
                if (res.getStatusLine().getStatusCode() == 200) {
                    existIndexList.add(index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return existIndexList;
    }

    public boolean deleteIndex(String index) {
        try {
            Request request = new Request(
                    "DELETE",
                    "/" + index + "");
            Response res = client.performRequest(request);
            if (res.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            logger.error("", e);
        }
    }

}
