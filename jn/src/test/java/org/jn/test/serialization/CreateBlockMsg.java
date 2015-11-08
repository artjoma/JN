package org.jn.test.serialization;

import java.util.Date;

import org.jn.node.message.JNMessage;
import org.jn.test.SimpleMessageProcessor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
/**
 * Block
 * @author ArtjomAminov
 *
 * 8 Nov 2015 15:37:32
 */
public class CreateBlockMsg implements JNMessage{
	/**
	 * User defined message from 0..127
	 */
	public static final byte CREATE_BLOCK_MSG = 1;
	
	private int height;
	private String hash;
	private Date time;
	private String prevHash;
	
	public CreateBlockMsg() {
		
	}
	
	public CreateBlockMsg(int height, String hash, Date time, String prevHash) {
		super();
		this.height = height;
		this.hash = hash;
		this.time = time;
		this.prevHash = prevHash;
	}

	@Override
	public ByteBuf serialize() {
		ByteBuf msg = Unpooled.buffer();
		msg.writeByte(CREATE_BLOCK_MSG);
		msg.writeInt(height);
		writeUTFString(msg, hash);
		msg.writeLong(time.getTime());
		writeUTFString(msg, prevHash);
		return msg;
	}

	@Override
	public void deserialize(int msgSize, ByteBuf msg) {
		this.height = msg.readInt();
		this.hash = readUTFString(msg);
		this.time = new Date (msg.readLong());
		this.prevHash = readUTFString(msg);
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getPrevHash() {
		return prevHash;
	}

	public void setPrevHash(String prevHash) {
		this.prevHash = prevHash;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CreateBlockMsg [height=");
		builder.append(height);
		builder.append(", ");
		if (hash != null) {
			builder.append("hash=");
			builder.append(hash);
			builder.append(", ");
		}
		if (time != null) {
			builder.append("time=");
			builder.append(time);
			builder.append(", ");
		}
		if (prevHash != null) {
			builder.append("prevHash=");
			builder.append(prevHash);
		}
		builder.append("]");
		return builder.toString();
	}
	
	

}
