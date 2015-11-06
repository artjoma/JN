package org.jn;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;

public class JNUtils {
	/**
	 * Validate network port
	 * @author ArtjomAminov
	 * 1 Nov 2015 13:36:41
	 * @param port
	 * @throws Exception
	 */
	public static final int validateNetworkPort (String portStr) throws Exception{
		int port = 0;
		try{
			port = Integer.parseInt(portStr.trim());
		}catch(Exception e){
			throw new Exception ("Invalid port format");
		}
		if (port > 65535 || port < 10){
			throw new Exception ("Invalid port. Valid value > 10 and < 65535");
		}
		return port;
	}
	
	public static final String remoteHost (Channel channel){
		return ((InetSocketAddress)channel.remoteAddress()).getHostString();
	}
	
}
