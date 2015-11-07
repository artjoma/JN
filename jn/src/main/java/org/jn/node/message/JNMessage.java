package org.jn.node.message;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * JN messages. 
 * Message id's 0..29 reserved for future
 * @author ArtjomAminov
 *
 * 1 Nov 2015 14:50:38
 */
public class JNMessage {
	/**
	 * REQUEST Get all nodes 
	 */
	public static final byte GET_ALL_NODES_REQUEST_MSG = 0;
	/**
	 * RESPONSE Get all nodes
	 */
	public static final byte GET_ALL_NODES_RESPONSE_MSG = 1;
	/**
	 * Send to node this node server port
	 */
	public static final byte SET_NODE_SERVER_PORT_REQUEST_MSG = 2;
	
	/**
	 * User defined message
	 */
	public static final byte USER_DEFINED_MSG = 30;
	
	/**
	 * GET_ALL_NODES_MSG
	 * @author ArtjomAminov
	 * 1 Nov 2015 14:51:58
	 * @return
	 */
	public static ByteBuf getAllNodesRequest (int serverPort){
		ByteBuf msg = Unpooled.buffer();
		msg.writeByte(GET_ALL_NODES_REQUEST_MSG);
		msg.writeInt(serverPort);
		return msg; 
	}
	
	public static void getAllNodesResponse (ByteBuf msg, Map<Channel, String> serverClients, Set<String> nodes){
		msg.writeByte(GET_ALL_NODES_RESPONSE_MSG);
		StringBuilder allNodes = new StringBuilder(serverClients.entrySet().stream().map(x->x.getValue()).
				collect(Collectors.joining(";")));
		allNodes.append(nodes.stream().map(Object::toString).collect(Collectors.joining(";")));
		MessageUtils.writeUTFString(msg, allNodes.toString());
	}
	/**
	 * Send message from current host to node with this server port number
	 * @author ArtjomAminov
	 * 3 Nov 2015 12:02:27
	 * @param serverPort
	 * @return
	 */
	public static ByteBuf setNodeServerPort (int serverPort){
		ByteBuf msg = Unpooled.buffer();
		msg.writeByte(SET_NODE_SERVER_PORT_REQUEST_MSG);
		msg.writeInt(serverPort);
		return msg; 
	}
}
