package com.plumelog.core;

import com.plumelog.core.util.StringUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceIdGenerator {

    private static String P_ID_CACHE = null;
    private static String IP_CACHE = null;
    private static String IP_16 = "ffffffff";
    private static AtomicInteger count = new AtomicInteger(1000);

    static {
        try {
            IP_CACHE = getInetAddress();
            if (IP_CACHE != null) {
                IP_16 = getIP_16(IP_CACHE);
            }
        } catch (Throwable e) {
            /*
             * empty catch block
             */
        }
    }

    public static String getInetAddress() {
        if (IP_CACHE != null) {
            return IP_CACHE;
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
                        IP_CACHE = address.getHostAddress();
                    }
                }
            }
        } catch (Throwable t) {

        }
        return IP_CACHE;
    }

    /**
     * This method can be a better way under JDK9, but in the current JDK version, it can only be implemented in this way.
     * <p>
     * In Mac OS , JDK6，JDK7，JDK8 ,it's OK
     * In Linux OS,JDK6，JDK7，JDK8 ,it's OK
     *
     * @return Process ID
     */
    private static String getPID() {
        //check pid is cached
        if (P_ID_CACHE != null) {
            return P_ID_CACHE;
        }
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();

        if (StringUtil.isBlank(processName)) {
            return StringUtil.EMPTY_STRING;
        }

        String[] processSplitName = processName.split("@");

        if (processSplitName.length == 0) {
            return StringUtil.EMPTY_STRING;
        }

        String pid = processSplitName[0];

        if (StringUtil.isBlank(pid)) {
            return StringUtil.EMPTY_STRING;
        }
        P_ID_CACHE = pid;
        return pid;
    }

    private static String getTraceId(String ip, long timestamp, int nextId) {
        StringBuilder appender = new StringBuilder(30);
        appender.append(ip).append("P").append(getPID()).append("T").append(timestamp).append("N").append(nextId);
        return appender.toString().toUpperCase();
    }

    public static String generate() {
        return getTraceId(IP_16, System.currentTimeMillis(), getNextId());
    }

    private static String getIP_16(String ip) {
        String[] ips = ip.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String column : ips) {
            String hex = Integer.toHexString(Integer.parseInt(column));
            if (hex.length() == 1) {
                sb.append('0').append(hex);
            } else {
                sb.append(hex);
            }

        }
        return sb.toString();
    }

    private static int getNextId() {
        for (; ; ) {
            int current = count.get();
            int next = (current > 9000) ? 1000 : current + 1;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
