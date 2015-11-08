package org.jn.test.serialization;

import java.util.Date;
import java.util.Properties;

import org.jn.JN;
import org.jn.Nodes;
import org.jn.test.SimpleMessageProcessor;
import org.junit.Test;

import junit.framework.Assert;

public class SerializationTest {
	
	@Test
	public void testSerialization (){
		try{
			JN jn = new JN (new SimpleMessageProcessor()).sync();
			
			Properties properties = new Properties();
			properties.setProperty(Nodes.PROP_NODES, "127.0.0.1");
			JN jnNode2 = new JN (properties, new SimpleMessageProcessor()).sync();
			try{
				CreateBlockMsg createBlockMsg = new CreateBlockMsg(34561, 
						"000000000000000001e1435dcf809ae0d782b903f8d133cd8564078faab6f513", 
						new Date(), "00000000000000000c39ce1cbddb12da679b784002d3942bbf93f81ee97f23c7");
				jnNode2.sendMessage(createBlockMsg);
				Thread.sleep(500);
			}finally {
				jn.shutdown();
				jnNode2.shutdown();
			}
		}catch(Exception e){
			Assert.fail(e.getMessage());
		}
	}
	
}
