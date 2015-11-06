package org.jn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jn.node.message.JNMessage;
import org.jn.node.message.MessageProcessor;
import org.jn.node.message.MessageUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
/**
 * Simple message processor
 * @author ArtjomAminov
 *
 * 6 Nov 2015 14:45:43
 */
public class SimpleMessageProcessor extends MessageProcessor{
	private static final Logger LOGGER = LogManager.getLogger(SimpleMessageProcessor.class);

	@Override
	public void processMessage(int msgSize, byte sysCommand, ChannelHandlerContext ctx, ByteBuf msg) {
		//user message ? 
		if (sysCommand == JNMessage.USER_DEFINED_MSG){
			LOGGER.info(jn.toString() + " User defined msg: " + MessageUtils.readUTFString(msg));
		}
	}
	
}
