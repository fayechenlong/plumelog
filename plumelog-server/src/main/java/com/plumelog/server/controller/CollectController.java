package com.plumelog.server.controller;

import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.TraceLogMessage;
import com.plumelog.server.InitConfig;
import com.plumelog.server.client.AbstractServerClient;
import com.plumelog.server.collect.LocalLogQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * className：CollectController
 * description：lite模式下日志推送接口
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@RestController
@CrossOrigin
public class CollectController {

    private final Logger logger = LoggerFactory.getLogger(CollectController.class);

    @Autowired
    private AbstractServerClient abstractServerClient;

    @RequestMapping({"/sendRunLog", "/plumelogServer/sendRunLog"})
    public Result sendRunLog(@RequestBody List<RunLogMessage> logs) {
        Result result = new Result();
        if ("redis".equals(InitConfig.START_MODEL)) {
            try {
                LocalLogQueue.rundataQueue.addAll(logs);
                logger.info("receive runLog messages! count:{}",logs.size());
            } catch (Exception e) {
                result.setCode(500);
                result.setMessage("send logs error! :" + e.getMessage());
            }
            result.setCode(200);
            result.setMessage("send logs! success");
        } else {
            result.setCode(500);
            result.setMessage("send logs error! rest model only support redis model");
        }
        return result;
    }

    @RequestMapping({"/sendTraceLog", "/plumelogServer/sendTraceLog"})
    public Result sendTraceLog(@RequestBody List<TraceLogMessage> logs) {
        Result result = new Result();
        if ("redis".equals(InitConfig.START_MODEL)) {
            try {
                LocalLogQueue.tracedataQueue.addAll(logs);
                logger.info("receive traceLog messages! count:{}",logs.size());
            } catch (Exception e) {
                result.setCode(500);
                result.setMessage("send logs error! :" + e.getMessage());
            }
            result.setCode(200);
            result.setMessage("send logs! success");
        } else {
            result.setCode(500);
            result.setMessage("send logs error! rest model only support redis model");
        }
        return result;
    }
}
