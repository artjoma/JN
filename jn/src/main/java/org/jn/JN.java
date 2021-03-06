package org.jn;

import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jn.node.message.JNMessage;
import org.jn.node.message.MessageProcessor;
import org.jn.node.server.NodeServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * JAVA node framework main class
 * @author ArtjomAminov
 *
 * 30 Oct 2015 14:09:31
 */
public class JN {
	private static final Logger LOGGER = LogManager.getLogger(JN.class);
	/**
	 * Clients for nodes
	 */
	private Nodes nodes = null;
	/**
	 * Current node server socket.
	 */
	private NodeServer nodeServer = null;
	/**
	 * Store clients and server clients channels
	 */
	private ChannelGroup channels = null;
	
 	private MessageProcessor incomeMessageProcessor;
 	private Properties properties;
 	private JNState jnState;
 	//start time
 	private Date startTime;
 	
 	public JN (MessageProcessor incomeMessageProcessor) throws Exception{
 		this (null, incomeMessageProcessor);
 	}
 	
	public JN(Properties prop, MessageProcessor incomeMessageProcessor) throws Exception{
		LOGGER.info("Start init JN");
		
		this.channels = new DefaultChannelGroup("all-nodes", GlobalEventExecutor.INSTANCE);
		this.jnState = JNState.SHUTDOWN;
		this.properties = prop;
		this.incomeMessageProcessor = incomeMessageProcessor;
		incomeMessageProcessor.setJn(this);
		
		this.nodeServer = new NodeServer(properties, incomeMessageProcessor, channels);
		nodeServer.init();
		this.nodes = new Nodes (properties, this, channels);
		
		this.startTime = new Date ();
		LOGGER.info("End init JN");
	}
	
	/**
	 * Is node ok.
	 * Wait until node complete SYNCHRONIZATION
	 * @author ArtjomAminov
	 * 5 Nov 2015 13:56:17
	 * @throws InterruptedException
	 */
	public JN sync () throws InterruptedException{
		while (jnState != JNState.SYNCHRONIZED && jnState != JNState.SHUTDOWN){
			Thread.sleep(10);
		}
		return this;
	}
	/**
	 * Get current node state
	 * @author ArtjomAminov
	 * 5 Nov 2015 13:40:13
	 * @return
	 */
	public JNState getState (){
		return jnState;
	}
	
	/**
	 * Send message to all nodes
	 * @author ArtjomAminov
	 * 2 Nov 2015 18:52:57
	 * @param msg
	 */
	public void sendMessage (ByteBuf msg){
		if (!channels.isEmpty()){
			channels.writeAndFlush(msg);
		}
	}
	
	public void sendMessage (JNMessage message){
		if (!channels.isEmpty()){
			channels.writeAndFlush(message.serialize());
		}
	}
	
	/**
	 * Return nodes count in cluster
	 * @author ArtjomAminov
	 * 6 Nov 2015 11:10:57
	 */
	public int getNodesCount (){
		//size + self
		return channels.size() + 1;
	}
	
	/**
	 * Shutdown current node
	 * @author ArtjomAminov
	 * 30 Oct 2015 15:00:37
	 */
	public void shutdown (){
		LOGGER.info("Start shutdown");
		//channels
		channels.close(); 
		//clients
		nodes.shutdown();
		//server
		nodeServer.shutdown();
		jnState = JNState.SHUTDOWN;
		LOGGER.info("End shutdown");
	}

	public Nodes getNodes() {
		return nodes;
	}
	
	public NodeServer getNodeServer() {
		return nodeServer;
	}

	public MessageProcessor getIncomeMessageProcessor() {
		return incomeMessageProcessor;
	}
	public JNState getJnState() {
		return jnState;
	}
	
	protected void setJnState(JNState jnState) {
		this.jnState = jnState;
	}
	/**
	 * DateTime when node started
	 * @author ArtjomAminov
	 * 6 Nov 2015 11:19:08
	 * @return
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	@Override
	public String toString() {
		return "node:" + nodeServer.getPort()+" nodes:" +
				nodes.getNodeClients().size() + " clients:" 
				+ nodeServer.getServerClients().size();
		
	}
}
