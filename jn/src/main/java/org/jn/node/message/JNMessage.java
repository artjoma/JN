package org.jn.node.message;

import io.netty.buffer.ByteBuf;
/**
 * Message marshalling
 * @author ArtjomAminov
 *
 * 8 Nov 2015 15:32:36
 */
public interface JNMessage {
	
	public ByteBuf serialize ();
	
	public void deserialize (int msgSize, ByteBuf msg);
	
	default void writeUTFString (ByteBuf msg, String txt){
		MessageUtils.writeUTFString(msg, txt);
	}
	
	default String readUTFString (ByteBuf msg){
		return MessageUtils.readUTFString(msg);
	}
}
