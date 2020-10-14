package com.plumelog.core;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceIdGenerator {

    private static final String   EMPTY_STRING             = "";
    private static String       P_ID_CACHE  = null;
    private static String      IP_16 = "ffffffff";
    private static AtomicInteger count = new AtomicInteger(1000);

    static {
        try {
            String ipAddress = getInetAddress();
            if (ipAddress != null) {
                IP_16 = getIP_16(ipAddress);
            }
        } catch (Throwable e) {
            /*
             * empty catch block
             */
        }
    }
    private static String getInetAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
                        return address.getHostAddress();
                    }
                }
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }
    private static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
   /**
     * This method can be a better way under JDK9, but in the current JDK version, it can only be implemented in this way.
     *
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

        if (isBlank(processName)) {
            return EMPTY_STRING;
        }

        String[] processSplitName = processName.split("@");

        if (processSplitName.length == 0) {
            return EMPTY_STRING;
        }

        String pid = processSplitName[0];

        if (isBlank(pid)) {
            return EMPTY_STRING;
        }
        P_ID_CACHE = pid;
        return pid;
    }

    private static String getTraceId(String ip, long timestamp, int nextId) {
        StringBuilder appender = new StringBuilder(30);
        appender.append(ip).append(timestamp).append(nextId).append(getPID());
        return appender.toString();
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
        for (;;) {
            int current = count.get();
            int next = (current > 9000) ? 1000 : current + 1;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
