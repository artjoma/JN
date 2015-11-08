package org.jn.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jn.node.message.MessageProcessor;
import org.jn.node.message.MessageUtils;
import org.jn.test.serialization.CreateBlockMsg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import junit.framework.Assert;
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

	
	@Override
	public void processMessage(int msgSize, byte sysCommand, ChannelHandlerContext ctx, ByteBuf msg) {
		LOGGER.info("Msg length: " + msgSize);
		//router
		switch (sysCommand){
			case USER_DEFINED_MSG : userDefinedMsg (ctx, msg); break;
			case CreateBlockMsg.CREATE_BLOCK_MSG : createBlock(msgSize, ctx, msg); break;
		}

	}	
	
	private void userDefinedMsg (ChannelHandlerContext ctx, ByteBuf msg) {
		LOGGER.info(jn.toString() + " User defined msg: " + MessageUtils.readUTFString(msg));
	}
	
	private void createBlock (int msgSize, ChannelHandlerContext ctx, ByteBuf msg){
		CreateBlockMsg createBlockMsg = new CreateBlockMsg();
		createBlockMsg.deserialize(msgSize, msg);
		
		LOGGER.info(jn.toString()  + " " + createBlockMsg.toString());
		Assert.assertEquals("000000000000000001e1435dcf809ae0d782b903f8d133cd8564078faab6f513", createBlockMsg.getHash());
	}
}
