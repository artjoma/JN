package org.jn.test;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jn.JN;
import org.jn.Nodes;
import org.jn.node.message.JNMessageSys;
import org.jn.node.message.MessageUtils;
import org.jn.node.server.NodeServer;
import org.junit.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import junit.framework.Assert;

/**
 * Send message to nodes
 */
public class JNSendMessageTest {
	private static final Logger LOGGER = LogManager.getLogger(JNSendMessageTest.class);
	
	public JN startNode1 () throws Exception{
		Properties prop = new Properties ();
		return new JN(prop, new SimpleMessageProcessor());
	}
	
	@Test
	public void complexTest () throws Exception{
		JN jn1 = null;
		JN jn2 = null;
		JN jn3 = null;
		JN jn4 = null;
		
		try{
			jn1 = startNode1 ();
			jn2 = startNode2 ();
			jn3 = startNode3 ();
			jn4 = startNode4 ();
		
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
		JN jnNode = new JN(prop, new SimpleMessageProcessor()).sync();
		//send message to all nodes in cluster
		ByteBuf msg = Unpooled.buffer();
		msg.writeByte(SimpleMessageProcessor.USER_DEFINED_MSG);
		MessageUtils.writeUTFString(msg, "testMsg");
		jnNode.sendMessage(msg);
		return jnNode;	
    }
	
}

