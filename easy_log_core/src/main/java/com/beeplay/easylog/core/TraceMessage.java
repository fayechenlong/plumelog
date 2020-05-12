package com.beeplay.easylog.core;


import java.util.concurrent.atomic.AtomicInteger;

public class TraceMessage {

    private String traceId;
    private String messageType;
    private String position;
    private AtomicInteger positionNum = new AtomicInteger(0);

    public AtomicInteger getPositionNum() {
        return positionNum;
    }

    public void setPositionNum(AtomicInteger positionNum) {
        this.positionNum = positionNum;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }


    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
