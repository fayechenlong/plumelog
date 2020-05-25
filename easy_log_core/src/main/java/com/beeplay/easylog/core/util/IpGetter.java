package com.beeplay.easylog.core.util;


import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * IP工具类
 *
 * @author chenlongfei
 * @version 2017-03-16
 * @see IpGetter
 */
public class IpGetter {

    /**
     * 当前IP。类初始化时调用一次就可以了
     */
    public final static String CURRENT_IP = getIp();

    /**
     * 单网卡名称
     */
    private static final String NETWORK_CARD = "eth0";
    /**
     * 绑定网卡名称
     */
    private static final String NETWORK_CARD_BAND = "bond0";

    /**
     * Description: 得到本机名<br>
     *
     * @return
     * @see
     */
    public static String getLocalHostName() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Description: linux下获得本机IPv4 IP<br>
     *
     * @return
     * @see
     */
    public static String getLocalIP() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = e1.nextElement();
                //单网卡或者绑定双网卡
                if ((NETWORK_CARD.equals(ni.getName())) || (NETWORK_CARD_BAND.equals(ni.getName()))) {
                    Enumeration<InetAddress> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = e2.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;
                        }
                        ip = ia.getHostAddress();
                    }
                    break;
                } else {
                    continue;
                }
            }
        } catch (SocketException e) {
        }
        return ip;
    }
    public static Collection<InetAddress> getAllHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Collection<InetAddress> addresses = new ArrayList<InetAddress>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public static String getIp(){
        Collection<InetAddress> colInetAddress =getAllHostAddress();
        for (InetAddress address : colInetAddress) {
            if (!address.isLoopbackAddress()) {
                String ip=address.getHostAddress();
                if(ip.startsWith("10.")||ip.startsWith("172.")) {
                    return ip;
                }
            }
        }
        return "";
    }
}