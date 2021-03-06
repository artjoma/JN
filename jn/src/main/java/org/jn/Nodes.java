package org.jn;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jn.node.client.NodeClient;
import org.jn.node.message.JNMessageSys;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
/**
 * Nodes details
 * @author ArtjomAminov
 *
 * 30 Oct 2015 16:44:37
 */
public class Nodes {
	private static final Logger LOGGER = LogManager.getLogger(Nodes.class);
	public static final String PROP_NODES = "nodes";

	private Map<Channel, NodeClient> nodeClients = null;
	
	private JN jn;
	private ChannelGroup channels = null;
	
	private void validate (Properties prop) throws Exception{
		if (prop != null){
			if (prop.contains(PROP_NODES)){
				if (((String)prop.get(PROP_NODES)).trim().length() < 8){
					throw new Exception("Property 'nodes' has incorrect value !");
				}
			}
		}
	}
	
	public Nodes(Properties prop, JN jn, ChannelGroup channels) throws Exception{
		this.nodeClients = new HashMap<>();
		this.jn = jn;
		validate (prop);
		this.channels = channels;
		
		
		String nodesLine = null;
		if (prop != null){
			nodesLine = prop.getProperty(PROP_NODES);
		}
		
		if (nodesLine == null){
			LOGGER.info("Property not found: '" + PROP_NODES + "' single node");
			jn.setJnState(JNState.SYNCHRONIZED);
		}else{
			jn.setJnState(JNState.SYNCHRONIZATION);
			String nodes [] = nodesLine.split("\\;");
			
			/*
			 * Try connect to first node from nodes list
			 */
			int i = 0;
			int nodesCount = nodes.length;
			LOGGER.info("Nodes count from props:" + nodesCount);
			boolean done = false;
			
			NodeClient client = null;
			while (!done && i < nodesCount){
				String node = nodes[i];
				if (node.length() > 3){
					try{
						client = new NodeClient (node, this, jn.getIncomeMessageProcessor());
						done = true;
					}catch(Exception e){
						LOGGER.debug("Err connect to: " + node);
					}
				}
				i++;
			}
			
			if (done){
				//send message GET_ALL_NODES_REQUEST_MSG
				client.sendMessageSync(JNMessageSys.getAllNodesRequest(jn.getNodeServer().getPort()));
				channels.add(client.getChannel());
				//add node to list
				nodeClients.put(client.getChannel(), client);
			}else{
				jn.setJnState(JNState.SYNCHRONIZED);
				LOGGER.info("Single node");
			}
		}
	}
	/**
	 * Nodes line host:port;host:port...
	 * @author ArtjomAminov
	 * 1 Nov 2015 12:22:24
	 * @param line
	 */
	public void createClients (String line){
		if (line.length() > 5){
			String [] array = line.split("\\;");
			LOGGER.debug("Start create clients from list: [" + line + "] size: " + array.length);
			int count = 0;
			for (String addr : array){
				new Thread (()->{
					createClient(addr);
				}).start();
				if (count % 20 == 0){
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						LOGGER.error("Thread err: " + e.getMessage(), e);
					}
				}
				count++;
			}
			while (count != array.length){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					LOGGER.error("Thread err: " + e.getMessage(), e);
				}
			}
			
		}
	
		jn.setJnState(JNState.SYNCHRONIZED);
		LOGGER.info("State: " + JNState.SYNCHRONIZED);
	}

	/**
	 * @author ArtjomAminov
	 * 9 Nov 2015 12:58:39
	 * @param addr
	 * @throws Exception
	 * @throws InterruptedException
	 */
	public void createClient(String addr){
		if (addr.length() > 4){
			try{
				NodeClient client = new NodeClient (addr, this, jn.getIncomeMessageProcessor());
				client.sendMessageSync(JNMessageSys.setNodeServerPort(jn.getNodeServer().getPort()));
				nodeClients.put(client.getChannel(), client);
				channels.add(client.getChannel());
			}catch(Exception e){
				LOGGER.debug("Can't connect to node: " + addr);
			}
		}else{
			LOGGER.error("Invalid host.port " + addr);
		}
	}
	
	/**
	 * Remove and shutdown
	 * @author ArtjomAminov
	 * 1 Nov 2015 17:26:57
	 * @param nodeClient
	 */
	public void removeClient (NodeClient nodeClient){
		nodeClients.remove(nodeClient.getChannel()).destroy();
	}
	
	/**
	 * Shutdown all clients and clean resources
	 * @author ArtjomAminov
	 * 31 Oct 2015 14:55:34
	 */
	public void shutdown (){
		for (NodeClient nodeClient : nodeClients.values()){
			nodeClient.destroy();
		}
		nodeClients.clear();
	}
	
	public ChannelFuture sendMessageToNode (NodeClient client, ByteBuf msg){
		return client.sendMessage(msg);
	}

	public Map<Channel, NodeClient> getNodeClients() {
		return nodeClients;
	}
	
}
