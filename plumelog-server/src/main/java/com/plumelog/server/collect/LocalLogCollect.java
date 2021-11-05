package com.plumelog.server.collect;


import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.TraceLogMessage;
import com.plumelog.core.lucene.LuceneClient;
import com.plumelog.server.InitConfig;
import com.plumelog.server.util.IndexUtil;
import com.plumelog.server.websocket.WebSocketSession;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * className：RedisLogCollect
 * description：RedisLogCollect 获取rest接口中日志，存储到es
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class LocalLogCollect {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(LocalLogCollect.class);
    private LuceneClient luceneClient;
    /**
     * lite模式使用
     */
    protected ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    public LocalLogCollect(LuceneClient luceneClient) {
        this.luceneClient = luceneClient;
    }

    public void start() {
        Thread runLogThread = startRunLogThread();
        Thread traceLogThread = startTraceLogThread();

        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            Thread runLog = runLogThread;
            try {
                boolean runLogThreadAlive = runLog.isAlive();
                if (!runLogThreadAlive) {
                    throw new NullPointerException("runLogThread alive false");
                }
            } catch (Exception ex) {
                System.out.println("runLogThread 重启线程");
                runLog = startRunLogThread();
            }

            Thread traceLog = traceLogThread;
            try {
                boolean traceLogThreadAlive = traceLog.isAlive();
                if (!traceLogThreadAlive) {
                    throw new NullPointerException("traceLogThread alive false");
                }
            } catch (Exception ex) {
                logger.warn("traceLogThread 重启线程");
                traceLog = startTraceLogThread();
            }
        }, 10, 30, TimeUnit.SECONDS);

        logger.info("LocalCollect is starting!");
    }

    private Thread startRunLogThread() {
        Thread runLogThread = new Thread(this::collectRuningLog);
        runLogThread.start();
        return runLogThread;
    }

    private Thread startTraceLogThread() {
        Thread traceLogThread = new Thread(this::collectTraceLog);
        traceLogThread.start();
        return traceLogThread;
    }

    private void collectRuningLog() {
        while (true) {
            List<RunLogMessage> logs = new ArrayList<>();

            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            LocalLogQueue.rundataQueue.drainTo(logs, InitConfig.MAX_SEND_SIZE);

            try {
                if (logs.size() > 0) {
                    luceneClient.insertListLog(logs, getRunLogIndex());
                    logger.info("runLog messages insert success! count:{}",logs.size());
                    //发送到控制台,最多单次发送100条，防止前端卡顿
                    for(int i=0;i<100;i++){
                        WebSocketSession.sendToConsole(logs.get(i));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void collectTraceLog() {
        while (true) {
            List<TraceLogMessage> logs = new ArrayList<>();
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            LocalLogQueue.tracedataQueue.drainTo(logs, InitConfig.MAX_SEND_SIZE);
            try {
                if (logs.size() > 0) {
                    luceneClient.insertListTrace(logs, getTraceLogIndex());
                    logger.info("traceLog messages insert success! count:{}",logs.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String getRunLogIndex() {
        if ("day".equals(InitConfig.ES_INDEX_MODEL)) {
            return IndexUtil.getRunLogIndex(System.currentTimeMillis());
        } else {
            return IndexUtil.getRunLogIndexWithHour(System.currentTimeMillis());
        }
    }

    private String getTraceLogIndex() {
        if ("day".equals(InitConfig.ES_INDEX_MODEL)) {
            return IndexUtil.getTraceLogIndex(System.currentTimeMillis());
        } else {
            return IndexUtil.getTraceLogIndexWithHour(System.currentTimeMillis());
        }
    }
}
