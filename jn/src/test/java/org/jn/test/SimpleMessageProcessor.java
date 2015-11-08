package org.jn.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	
	/**
	 * User defined message from 0..127
	 */
	public static final byte USER_DEFINED_MSG = 0;
	/**
	 * User defined message from 0..127
	 */
	public static final byte CREATE_BLOCK_MSG = 1;
	
	@Override
	public void processMessage(int msgSize, byte sysCommand, ChannelHandlerContext ctx, ByteBuf msg) {
		LOGGER.info("Msg length: " + msgSize);
		//router
		switch (sysCommand){
			case USER_DEFINED_MSG : userDefinedMsg (ctx, msg); break;
			case CREATE_BLOCK_MSG  :createBlock(ctx, msg); break;
		}

	}	
	
	private void userDefinedMsg (ChannelHandlerContext ctx, ByteBuf msg) {
		LOGGER.info(jn.toString() + " User defined msg: " + MessageUtils.readUTFString(msg));
	}
	
	private void createBlock (ChannelHandlerContext ctx, ByteBuf msg){
		/*
		int height = msg.readInt();
		String hash = MessageUtils.readUTFString(msg);
		long time = msg.readLong();
		String prevHash = MessageUtils.readUTFString(msg);
		*/
	}
}
