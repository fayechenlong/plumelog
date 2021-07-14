package com.plumelog.server.monitor;

import org.springframework.util.StringUtils;

import java.util.List;

/**
 * className：PlumeLogMonitorTextMessage
 * description： TODO
 * time：2020-07-03.13:56
 *
 * @author Tank
 * @version 1.0.0
 */
public class PlumeLogMonitorTextMessage {

    private String text;
    private List<String> atMobiles;
    private boolean isAtAll = false;

    private PlumeLogMonitorTextMessage(Builder builder) {
        this.text = builder.toString();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getAtMobiles() {
        return atMobiles;
    }

    public void setAtMobiles(List<String> atMobiles) {
        this.atMobiles = atMobiles;
    }

    public boolean isAtAll() {
        return isAtAll;
    }

    public void setAtAll(boolean atAll) {
        isAtAll = atAll;
    }

    public static final class Builder {


        private Long count;

        private Long errorCount;

        private Long time;

        private String appName;

        private String env;

        private String className;

        private String monitorUrl;

        private String errorContent;

        public Builder(String appName, String env) {
            this.appName = appName;
            this.env = env;
        }

        public Builder className(String className) {
            this.className = className;
            return this;
        }


        public Builder count(Long count) {
            this.count = count;
            return this;
        }

        public Builder time(long time) {
            this.time = time;
            return this;
        }

        public Builder errorCount(long errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public Builder monitorUrl(String monitorUrl) {
            this.monitorUrl = monitorUrl;
            return this;
        }

        public Builder errorContent(String errorContent) {
            this.errorContent = errorContent;
            return this;
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder();
            builder.append("#### 报警通知：").append(" \n> ")
                    .append("应用名 【").append(appName).append("】\n\n>")
                    .append("应用环境【").append(env).append("】\n\n> ");
            if (!StringUtils.isEmpty(className)) {
                builder.append("类路径【").append(className).append("】 \n\n> ");
            }
            builder.append("时间区间【").append(time).append("】秒 \n\n> ");
            builder.append("错误阀值【").append(errorCount).append("】条数 \n\n> ");
            builder.append("实际错误【").append(count).append("】条数 \n\n> ");
            builder.append("错误信息【").append(errorContent).append("】 \n\n> ");
            builder.append("[点击查看](").append(monitorUrl).append(") \n\n");
            return builder.toString();
        }


        public PlumeLogMonitorTextMessage build() {
            return new PlumeLogMonitorTextMessage(this);
        }

    }
}
