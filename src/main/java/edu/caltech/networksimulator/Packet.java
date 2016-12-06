
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
	
	private static int lastPacketID = 0;
	
	// public static final int CHAR_SIZE = 2;
	private final long src, dest;
	private long sent_time;
	
	// to identify its flow
	private final int sequence_number;
	private final String sequence_id;
	
	private String payload;
	
	private int packetUID;
	
	/**
	 * Creates a packet as part of a sequence
	 * @param src The source of the packet's IP
	 * @param dest The destination IP
	 * @param payload The packet payload
	 * @param sequence_number The sequence number
	 * @param sequence_id The sequence ID
	 */
	public Packet(long src, long dest, String payload, int sequence_number, String sequence_id) {
		this.src = src;
		this.dest = dest;
		this.payload = payload;
		this.sequence_number = sequence_number;
		this.sequence_id = sequence_id;
		
		packetUID = lastPacketID++;
	}
	
	/**
	 * Creates a packet that is not part of a sequence
	 * @param src The src of the packet's IP
	 * @param dest The destination of the packet's IP
	 * @param payload The packet payload
	 */
	public Packet(long src, long dest, String payload) {
		this(src, dest, payload, -1, null);
	}

	/**
	 * Gets the size of this packet in bytes, depending on whether this packet is an ACK or a regular packet.
	 * @return The number of bytes this packets contents use
	 */
	public long getPacketSize() {
		return this.payload.equals("ACK") ? ACK_SIZE : PACKET_SIZE;
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
	public String getSeqID() {
		return sequence_id;
	}
	
	/**
	 * @return the sequence number
	 */
	public int getSeqNum() {
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
		return "{Src: " + getSrc() +
				" Dest: " + getDest() +
				" Payload: " + getPayload() +
				" Sequence ID: " + getSeqID() + 
				" Sequence Num: " + getSeqNum() +  
				" packetUID: " + packetUID + "}";
	}

	/**
	 * Get the packet's size in bits
	 * @return
	 */
	public long getPacketSizeBits() {
		return 8 * getPacketSize();
	}
	
	/**
	 * Set the time at which the packet was sent
	 */
	public void setSentTime() {
		this.sent_time = System.currentTimeMillis();
	}
	
	/**
	 * Get a new packet which is the acknowledgement for this packet
	 * @param seq_num
	 * @return
	 */
	public Packet getACK(int seq_num) {
		Packet p = new Packet(this.dest, this.src, "ACK", seq_num, this.sequence_id);
		p.sent_time = this.sent_time;
		return p;
	}

	/**
	 * Return whether this packet is a routing packet
	 * @return True if it is, otherwise false
	 */
	public boolean isRouting() {
		return payload.startsWith(Router.ROUTING_PACKET_HEADER);
	}

	
}
