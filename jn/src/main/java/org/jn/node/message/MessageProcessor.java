package org.jn.node.message;

import org.jn.JN;
import org.jn.JNUtils;
import org.jn.node.server.NodeServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
/**
 * Income message processor
 * @author ArtjomAminov
 *
 * 1 Nov 2015 13:23:20
 */
public abstract class MessageProcessor {
	protected JN jn;
	
	public void setJn(JN jn) {
		this.jn = jn;
	}

	/**
	 * Route message
	 * @author ArtjomAminov
	 * 1 Nov 2015 17:42:30
	 * @param ctx
	 * @param msgIn
	 */
	public void route(ChannelHandlerContext ctx, Object msgIn) {
		try {
			ByteBuf msg = (ByteBuf) msgIn;
			while (msg.isReadable()) {
				int msgSize = msg.readInt();
				byte sysCmd = msg.readByte();
				switch (sysCmd) {
					// GET all nodes list
					case JNMessageSys.GET_ALL_NODES_REQUEST_MSG:{
						String host = JNUtils.remoteHost(ctx.channel());
						int serverPort = msg.readInt();
						NodeServer nodeServer = jn.getNodeServer();
						ByteBuf response = ctx.alloc().buffer();
						JNMessageSys.getAllNodesResponse(response, nodeServer.getServerClients(), jn.getNodes().getNodeClients().values());
						ctx.writeAndFlush(response);
						nodeServer.getServerClients().put(ctx.channel(), host + ":" + serverPort);
						break;
					}
					// Response all nodes list
					case JNMessageSys.GET_ALL_NODES_RESPONSE_MSG: jn.getNodes().createClients(MessageUtils.readUTFString(msg)); break;
					case JNMessageSys.SET_NODE_SERVER_PORT_REQUEST_MSG: {
						NodeServer nodeServer = jn.getNodeServer();
						String host = JNUtils.remoteHost(ctx.channel());
						int serverPort = msg.readInt();
						nodeServer.getServerClients().put(ctx.channel(), host + ":" + serverPort);
						break;
					}
				}
				//call user defined processor
				processMessage(msgSize, sysCmd, ctx, msg);
			}
	
		} finally {
			ReferenceCountUtil.release(msgIn);
		}
	}
	
	public abstract void processMessage (int msgSize, byte command, ChannelHandlerContext ctx, ByteBuf msg);
	
}
