
/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * 
 * Stores a single packet of indeterminate size
 * 
 * @authors Francesco, Carly
 *
 */
public class Packet {
	
	private static final int PACKET_SIZE = 1024; // bytes
	private static final int ACK_SIZE = 64; // bytes
	
	// public static final int CHAR_SIZE = 2;
	private final long src, dest;
	private long sent_time;
	
	// to identify its flow
	private final int sequence_number;
	private final int sequence_id;
	
	private String payload;
	
	/**
	 * Creates a new packet with the given source and destination.
	 * @param src The source IP
	 * @param dest The destination IP
	 * @param payload The contents of the message
	 */
	public Packet(long src, long dest, String payload, int sequence_number, int sequence_id) {
		this.src = src;
		this.dest = dest;
		this.payload = payload;
		this.sequence_number = sequence_number;
		this.sequence_id = sequence_id;
	}
	
	/**
	 * Gets the size of this packet in bytes, caluclated based on the size of the metadata and payload.
	 * @return The number of bytes this packets contents use
	 */
//	public long getPacketSize() {
//		return (meta.length() + payload.length()) * CHAR_SIZE;
//	}
	public long getPacketSize() {
		if (this.payload.equals("ACK")) {
			return ACK_SIZE; // acknowledgements are shorter
		}
		return PACKET_SIZE; // packets are all a given size
	}
	
	/**
	 * Set the data payload of this packet
	 * @param payload
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	/**
	 * Get the data payload of this packet
	 * @return
	 */
	public String getPayload() {
		return payload;
	}

	/**
	 * @return the dest
	 */
	public long getDest() {
		return dest;
	}

	/**
	 * @return the src
	 */
	public long getSrc() {
		return src;
	}
	
	/**
	 * @return the sequence ID
	 */
	public long getSeqID() {
		return sequence_id;
	}
	
	/**
	 * @return the sequence number
	 */
	public long getSeqNum() {
		return sequence_number;
	}
	
	/**
	 * @return the sent time
	 */
	public long getSentTime() {
		return sent_time;
	}
	
	@Override
	public String toString() {
		return "{Src: " + getSrc() + " Dest: " + getDest() + " Payload: " + getPayload() + "}";
	}

	public long getPacketSizeBits() {
		return 8 * getPacketSize();
	}
	
	public void setSentTime() {
		System.out.println("setting packet time");
		this.sent_time = System.currentTimeMillis();
	}
	
	public Packet getACK() {
		Packet p = new Packet(this.dest, this.src, "ACK", this.sequence_number, this.sequence_id);
		p.sent_time = this.sent_time;
		return p;
	}

	
}
