package com.plumelog.server.collect;

import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.TraceLogMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalLogQueue{
        public static BlockingQueue<RunLogMessage> rundataQueue=new LinkedBlockingQueue<>(1000000);
        public static BlockingQueue<TraceLogMessage> tracedataQueue=new LinkedBlockingQueue<>(1000000);
}
