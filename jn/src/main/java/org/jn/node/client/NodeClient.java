package org.jn.node.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jn.JNUtils;
import org.jn.Nodes;
import org.jn.node.message.MessageProcessor;
import org.jn.node.server.NodeServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Node X client
 * @author ArtjomAminov
 *
 * 30 Oct 2015 16:45:07
 */
public class NodeClient {
	private static final Logger LOGGER = LogManager.getLogger(NodeClient.class);
	
	private Nodes nodes = null; 
	private String nodeId = null;
	private String host = null;
	private int port = NodeServer.DEFAULT_NODE_PORT;
	private NodeClientState clientState = NodeClientState.SHUTDOWN;
	
	private Channel channel;
	private EventLoopGroup workerGroup;
	private MessageProcessor processor;
	
	public NodeClient (String hostPort, Nodes nodes, MessageProcessor processor) throws Exception{
		this.nodes = nodes;
		this.processor = processor;
		hostPort = hostPort.trim();
		
		String data[] = hostPort.split("\\:");
		
		host = data [0].trim();
		if (data.length == 2){
			port = JNUtils.validateNetworkPort(data[1]);
		}
		
		validate();
		//build id
		this.nodeId = host + ":" + port;
		initConnection();
	}
	/**
	 * Init connection
	 * @author ArtjomAminov
	 * 30 Oct 2015 17:58:37
	 */
	private void initConnection () throws Exception{
		LOGGER.debug("Start init connection. Host.port: "+ nodeId);
		this.clientState = NodeClientState.INIT;
		
        this.workerGroup = new NioEventLoopGroup();
     
        Bootstrap b = new Bootstrap(); 
        b.group(workerGroup); 
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true); 
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000);
        
        final NodeClient thiz = this;
        
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
            	ch.pipeline().addLast(new LengthFieldPrepender(4));
            	ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4));
                ch.pipeline().addLast(new NodeClientHandler(nodes, thiz, processor));
                
            }
        });
        try{
        	// Start the client.
        	this.channel = b.connect(host, port).syncUninterruptibly().channel();
        	LOGGER.debug("End init connection. Connected to: " + nodeId);
        }catch(Exception e){
        	this.clientState = NodeClientState.SHUTDOWN;
        	throw e;
        }
		this.clientState = NodeClientState.ACTIVE;
	}
	
	/**
	 * Reconnect
	 * @author ArtjomAminov
	 * 9 Nov 2015 13:13:00
	 * @param addr
	 */
	public void reconnect (){
		new Thread (()->{
			LOGGER.debug("Start reconnect");
			try {
				Thread.sleep(250);
				initConnection();
			} catch (Exception e) {
				LOGGER.debug("Can't reconnect", e);
			}
		}).start();
	}
	
	/**
	 * Destroy client
	 * @author ArtjomAminov
	 * 31 Oct 2015 14:47:43
	 */
	public void destroy (){
		workerGroup.shutdownGracefully();
		clientState = NodeClientState.SHUTDOWN;
		LOGGER.debug("Destroy client. Id: " + nodeId);
	}
	
	public Channel getChannel() {
		return channel;
	}
	/**
	 * Send message async
	 * @author ArtjomAminov
	 * 31 Oct 2015 14:46:55
	 * @param buffer
	 * @return
	 */
	public ChannelFuture sendMessage (ByteBuf buffer){
		return channel.writeAndFlush(buffer);
	}
	/**
	 * Send message sync
	 * @author ArtjomAminov
	 * 3 Nov 2015 10:29:20
	 * @param buffer
	 * @return
	 * @throws InterruptedException
	 */
	public ChannelFuture sendMessageSync (ByteBuf buffer) throws InterruptedException {
		return channel.writeAndFlush(buffer).syncUninterruptibly();
	}
	
	/**
	 * Validate host & port
	 * @author ArtjomAminov
	 * 30 Oct 2015 17:58:26
	 * @throws Exception
	 */
	private void validate () throws Exception{
		if (host.trim().length() < 9){
			throw new Exception ("Incorrect host format");
		}
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	/**
	 * Get client state
	 * @author ArtjomAminov
	 * 1 Nov 2015 20:57:08
	 * @return
	 */
	public NodeClientState getClientState() {
		return clientState;
	}
	public String getNodeId() {
		return nodeId;
	}
	
}

class NodeClientHandler extends ChannelHandlerAdapter {
	private static final Logger LOGGER = LogManager.getLogger(NodeClientHandler.class);
	private Nodes nodes;
	private NodeClient nodeClient;
	private MessageProcessor messageProcessor;
	
    public NodeClientHandler(Nodes nodes, NodeClient nodeClient, MessageProcessor messageProcessor) {
		super();
		this.nodes = nodes;
		this.nodeClient = nodeClient;
		this.messageProcessor = messageProcessor;
	}

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try{
			messageProcessor.route(ctx, msg);
		}catch(Exception e){
			LOGGER.error("Err msg process : " + e.getMessage(), e);
			throw e;
		}
    }
	
    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    	super.disconnect(ctx, promise);
    	nodes.removeClient(nodeClient);
    	//reconnect
    	nodeClient.reconnect();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	nodes.removeClient(nodeClient);
        ctx.close();
        //reconnect
    	nodeClient.reconnect();
    }
}