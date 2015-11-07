package org.jn;

import java.util.Properties;

import org.junit.Test;

import junit.framework.Assert;
/**
 * Run many nodes on localhost
 * @author ArtjomAminov
 *
 * 5 Nov 2015 13:26:08
 */
public class LocalhostIncreasePort {
	
	@Test
	public void createNodes () throws Exception{
		JN node1 = new JN(new SimpleMessageProcessor());
		Assert.assertEquals(14500, node1.getNodeServer().getPort());
		
		JN node2 = new JN(new SimpleMessageProcessor());
		Assert.assertEquals(14501, node2.getNodeServer().getPort());
		
		JN node3 = new JN(new Properties (), new SimpleMessageProcessor());
		Assert.assertEquals(14502, node3.getNodeServer().getPort());
		
		Thread.sleep(500);
		node1.shutdown();
		node2.shutdown();
		node3.shutdown();
	}
	
}
