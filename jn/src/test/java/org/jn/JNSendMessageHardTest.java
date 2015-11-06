package org.jn;

import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jn.node.message.JNMessage;
import org.jn.node.message.MessageUtils;
import org.jn.node.server.NodeServer;
import org.junit.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import junit.framework.Assert;

/**
 * Send message to nodes
 */
public class JNSendMessageHardTest {
	private static final Logger LOGGER = LogManager.getLogger(JNSendMessageHardTest.class);
	
	public JN startNode1 () throws Exception{
		Properties prop = new Properties ();
		return new JN(prop, new SimpleMessageProcessor());
	}
	
	@Test
	public void messageHardTest () throws Exception{
		final JN jn1 = startNode1 ();
		final JN jn2 = startNode2 ();
		final JN jn3 = startNode3 ();
		final JN jn4 = startNode4 ();
		
		try{
			ForkJoinPool pool = new ForkJoinPool(4);
			pool.submit(()->{
				for (int i = 0; i < 100; i++){
					ByteBuf msg = Unpooled.buffer();
					msg.writeByte(JNMessage.USER_DEFINED_MSG);
					MessageUtils.writeUTFString(msg, "jn1 - > testMsg:" + i);
					jn1.sendMessage(msg);
				}
			});
			
			pool.submit(()->{
				for (int i = 0; i < 100; i++){
					ByteBuf msg = Unpooled.buffer();
					msg.writeByte(JNMessage.USER_DEFINED_MSG);
					MessageUtils.writeUTFString(msg, "jn2 - > testMsg:" + i);
					jn2.sendMessage(msg);
				}
			});
			
			pool.submit(()->{
				for (int i = 0; i < 100; i++){
					ByteBuf msg = Unpooled.buffer();
					msg.writeByte(JNMessage.USER_DEFINED_MSG);
					MessageUtils.writeUTFString(msg, "jn3 - > testMsg:" + i);
					jn3.sendMessage(msg);
				}
			});
			
			pool.submit(()->{
				for (int i = 0; i < 100; i++){
					ByteBuf msg = Unpooled.buffer();
					msg.writeByte(JNMessage.USER_DEFINED_MSG);
					MessageUtils.writeUTFString(msg, "jn4 - > testMsg:" + i);
					jn4.sendMessage(msg);
				}
			});
			
			
			pool.awaitQuiescence(5, TimeUnit.SECONDS);
			
			Thread.sleep(500);
		}catch(Exception e){
			LOGGER.error("Err: " + e.getMessage(), e);
			Assert.fail(e.getMessage());
		}finally {
			jn1.shutdown();
			jn2.shutdown();
			jn3.shutdown();
			jn4.shutdown();
		}
	}
	
    public JN startNode2 () throws Exception{
		Properties prop = new Properties ();
		prop.setProperty(NodeServer.PROP_NODE_PORT, "10501");
		prop.setProperty(Nodes.PROP_NODES, "127.0.0.1");
		//create sync
		return new JN(prop, new SimpleMessageProcessor()).sync();
    }
	
    public JN startNode3 ()  throws Exception{
		Properties prop = new Properties ();
		prop.setProperty(NodeServer.PROP_NODE_PORT, "10502");
		prop.setProperty(Nodes.PROP_NODES, "127.0.0.1:10501");
		//create sync
		return new JN(prop, new SimpleMessageProcessor()).sync();
    }
	
    public JN startNode4 () throws Exception{
		Properties prop = new Properties ();
		prop.setProperty(NodeServer.PROP_NODE_PORT, "10503");
		prop.setProperty(Nodes.PROP_NODES, "127.0.0.1");
		return new JN(prop, new SimpleMessageProcessor()).sync();	
    }
	
}

