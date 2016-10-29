
/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * 
 * Stores a single packet of indeterminate size
 * 
 * @author Francesco
 *
 */
public class Packet {
	
	public static final int CHAR_SIZE = 2;

	
	private final long src, dest;
	
	private String payload;
	private String meta;
	
	/**
	 * Creates a new packet with the given source and destination.
	 * @param src The source IP
	 * @param dest The destination IP
	 */
	public Packet(long src, long dest) {
		this.src = src;
		this.dest = dest;
	}
	
	/**
	 * Gets the size of this packet in bytes, caluclated based on the size of the metadata and payload.
	 * @return The number of bytes this packets contents use
	 */
	public long getPacketSize() {
		return (meta.length() + payload.length()) * CHAR_SIZE;
	}
	
	/**
	 * Sets the meta data for this packet
	 * @param meta
	 */
	public void setMeta(String meta) {
		this.meta = meta;
	}
	
	/**
	 * Get this packet's meta-data
	 * @return
	 */
	public String getMeta() {
		return meta;
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
	
	public String toString() {
		return "Meta: " + getMeta() + "\t Payload: " + getPayload();
	}

}
