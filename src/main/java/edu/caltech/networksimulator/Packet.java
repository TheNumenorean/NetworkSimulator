
/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @author Francesco
 *
 */
public class Packet {

	private long size;
	private long src;
	private long dest;

	/**
	 * 
	 */
	public Packet(int size) {
		this.size = size;
	}
	
	public Packet(int size, long src, long dest) {
		this.src = src;
		this.dest = dest;
	}
	
	
	
	public void setMeta(String header) {
		
	}
	
	public String getMeta() {
		return null;
		
	}
	
	public void setPayload(String payload) {
		
	}
	
	public String getPayload() {
		return null;
		
	}

	/**
	 * @return the dest
	 */
	public long getDest() {
		return dest;
	}

	/**
	 * @param dest the dest to set
	 */
	public void setDest(long dest) {
		this.dest = dest;
	}

	/**
	 * @return the src
	 */
	public long getSrc() {
		return src;
	}

	/**
	 * @param src the src to set
	 */
	public void setSrc(long src) {
		this.src = src;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}
	
	public String toString() {
		return "Meta: " + getMeta() + "\t Payload: " + getPayload();
	}

}
