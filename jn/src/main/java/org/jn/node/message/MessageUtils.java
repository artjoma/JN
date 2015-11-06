package org.jn.node.message;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class MessageUtils {
	/**
	 * Max length Short.MAX_VALUE = 32767
	 * @author ArtjomAminov
	 * 1 Nov 2015 18:57:21
	 * @throws UnsupportedEncodingException 
	 */
	public static void writeUTFString (ByteBuf msg, String text){
		msg.writeShort((short)text.length());
		msg.writeBytes(text.getBytes(CharsetUtil.UTF_8));
	}
	
	public static String readUTFString (ByteBuf msg){
		short len = msg.readShort();
		byte [] text = new byte [len];
		msg.readBytes(text);
		return new String (text, CharsetUtil.UTF_8);
	}
	
	
	
}
