package com.plumelog.server.client.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 跳过Hostname检查
 * 
 * @author	ylyue
 * @since	2020年8月30日
 */
public class SkipHostnameVerifier implements HostnameVerifier {

	@Override
	public boolean verify(String s, SSLSession sslSession) {
		return true;
	}

}