# JN
<p>JN - Java Node is simple java clustering - messaging library based on <a href="http://netty.io">Netty</a> framework  Apache 2.0 license. Java 8.	<img src="https://github.com/artjoma/JN/blob/master/jn/misc/schema.png" />
</p>
<h4>Features</h4>
<ul>
  <li>Simple usage</li>
  <li>NIO asynchronous event-driven model</li>
  <li>Messages based on heap/off-heap <a href="http://netty.io/5.0/api/io/netty/buffer/ByteBuf.html">ByteBuf</a></li>
</ul>
<h4>Kick start</h4>
  <pre><code>
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
		msg.writeByte(JNMessage.USER_DEFINED_MSG);
		MessageUtils.writeUTFString(msg, "testMsg");
		jnNode.sendMessage(msg);
		return jnNode;	
    }
    
public class SimpleMessageProcessor extends MessageProcessor{
	private static final Logger LOGGER = LogManager.getLogger(SimpleMessageProcessor.class);
	@Override
	public void processMessage(int msgSize, byte sysCommand, ChannelHandlerContext ctx, ByteBuf msg) {
		//user message ? 
		if (sysCommand == JNMessage.USER_DEFINED_MSG){
			LOGGER.info(jn.toString() + " User defined msg: " + MessageUtils.readUTFString(msg));
		}
	}	
}


</code></pre>
