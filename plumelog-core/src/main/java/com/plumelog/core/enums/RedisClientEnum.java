package com.plumelog.core.enums;

/**
 * 连接类型
 *
 * @author caijian
 */
public enum RedisClientEnum {
    single("single", "单节点"),
    sentinel("sentinel", "哨兵"),
    cluster("cluster", "集群");

    private String code;
    private String name;

    /**
     * 为了更好的返回代号和说明，必须重写构造方法
     *
     * @param code
     * @param name
     */
    RedisClientEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 根据value返回枚举类型,主要在switch中使用
     *
     * @param value
     * @return
     */
    public static RedisClientEnum getByValue(String value) {
        for (RedisClientEnum code : values()) {
            if (code.getCode().equals(value)) {
                return code;
            }
        }
        return null;
    }


}
