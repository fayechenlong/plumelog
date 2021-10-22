package com.plumelog.server.collect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalLogQueue{
        public static BlockingQueue<String> rundataQueue=new LinkedBlockingQueue<>(1000000);
        public static BlockingQueue<String> tracedataQueue=new LinkedBlockingQueue<>(1000000);
}
