package org.jn.node.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jn.JNUtils;
import org.jn.node.message.MessageProcessor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Current node server socket
 * @author ArtjomAminov
 *
 * 30 Oct 2015 16:54:33
 */
public class NodeServer {
	private static final Logger LOGGER = LogManager.getLogger(NodeServer.class);
	
	public static final int DEFAULT_NODE_PORT = 14500;
	public static final String PROP_NODE_PORT = "port";
	
	//Store all clients. host:port (server port client node)
	private Map<Channel, String> serverClients = null; 
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private int port = DEFAULT_NODE_PORT;
	private MessageProcessor messageProcessor;
	private ChannelGroup channels;
	
	private void validate (Properties prop) throws Exception{
		if (prop != null){
			if (prop.containsKey(NodeServer.PROP_NODE_PORT)){
				this.port = JNUtils.validateNetworkPort((String)prop.get(PROP_NODE_PORT));
			}
		}
	}
	
	public NodeServer(Properties properties, MessageProcessor messageProcessor, ChannelGroup channels) throws Exception{
		this.serverClients = new HashMap<>();
		this.messageProcessor = messageProcessor;
		this.channels = channels;
		validate (properties);
		
	}
	
	public void init () throws Exception{
		LOGGER.info("Start init server");
        // Configure the server.
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        
        ServerBootstrap boot = new ServerBootstrap();
       
        boot.option(ChannelOption.SO_BACKLOG, 2048)
         .option(ChannelOption.SO_KEEPALIVE, true)
         .option(ChannelOption.TCP_NODELAY, true);
        boot.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new ChannelInitializer<SocketChannel>(){

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4));
				ch.pipeline().addLast(new LengthFieldPrepender(4));
				ch.pipeline().addLast(new NodeServerHandlerAdapter(serverClients, channels, messageProcessor));
				
			}});
        
        boolean serverDown = true;
        while (serverDown){
        	 try{
             	boot.bind(port).syncUninterruptibly();
             	serverDown = false;
             }catch(Exception e){
            	 //increase port number, and try again
             	port++;
             }
        }
       
        LOGGER.info("End init server! Port: " + port);
	}
	
	/**
	 * Destroy server
	 * @author ArtjomAminov
	 * 1 Nov 2015 12:10:51
	 */
	public void shutdown (){
		bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
	}
	
	public Map<Channel, String> getServerClients() {
		return serverClients;
	}

	/**
	 * Server port
	 * @author ArtjomAminov
	 * 1 Nov 2015 20:48:57
	 * @return
	 */
	public int getPort() {
		return port;
	}
	
}
class NodeServerHandlerAdapter extends ChannelHandlerAdapter{
	private final Logger LOGGER = LogManager.getLogger(NodeServerHandlerAdapter.class);
	private ChannelGroup channelGroup;
	private MessageProcessor messageProcessor;
	private Map<Channel, String> serverClients;
	
	public NodeServerHandlerAdapter(Map<Channel, String> serverClients, ChannelGroup channelGroup, MessageProcessor messageProcessor) {
		super();
		this.channelGroup = channelGroup;
		this.messageProcessor = messageProcessor;
		this.serverClients = serverClients;
	}

	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		serverClients.remove(ctx.channel());
	    ctx.close();
	    LOGGER.debug("Server client connection exc: " + cause.getMessage(), cause);
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		channelGroup.add(ctx.channel());
		LOGGER.debug("Server client registered");
	}
	
	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		serverClients.remove(ctx.channel());
		LOGGER.debug("Server client disconnected: " + ctx);
		super.disconnect(ctx, promise);
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
       messageProcessor.route(ctx, msg);
    }

}