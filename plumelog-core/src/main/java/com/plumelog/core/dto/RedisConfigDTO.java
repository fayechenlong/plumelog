package com.plumelog.core.dto;

import java.io.Serializable;
import java.util.List;

/**
 * RedisConfigDTO
 *
 * @Author caijian
 * @Date 2020/12/29 1:43 下午
 */
public class RedisConfigDTO implements Serializable {

    private String configId;
    /**
     * redis 连接类型
     * single:      单节点
     * sentinel:    哨兵
     * cluster:     集群
     */
    private String type;
    private List<RedisConfigHostAndPort> hostAndPorts;
    private String password;
    private String masterName;
    private int index = 0;
    private List<String> keys;


    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RedisConfigHostAndPort> getHostAndPorts() {
        return hostAndPorts;
    }

    public void setHostAndPorts(List<RedisConfigHostAndPort> hostAndPorts) {
        this.hostAndPorts = hostAndPorts;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public class RedisConfigHostAndPort{
        private String host;
        private Integer port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }


}
