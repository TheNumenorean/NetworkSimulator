/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @author Carly
 *
 * Flows represent active connections.
 * Flows will have source and destination addresses; packets generated by each
 * flow will have the same destination address, to ensure they are routed correctly.
 * 
 * Flows have a source and destination address, and generate packets at a rate
 * controlled by the congestion control algorithm defined for that flow. You
 * should implement at least two different congestion control algorithms, e.g.
 * TCP Reno and FAST-TCP, and be able to choose independently between them for
 * each flow. Flows may send a continuous stream of data, or may send a finite
 * user-specifiable amount of data; they may also start immediately or after some
 * user-specifiable delay.
 */
public class Flow {
	
	private static final String ctrl_alg = "Naive"; // for example
	// probably want src as a networkComponent
	private final long src, dest;
	private final int _id;
	private final long data_size;
	private final long start_at;
	private final long num_packets;
	private long i; // how far along we are in the flow
	private int window;
	private int numSent;
	
	/**
	 * @param src The source IP
	 * @param dest The destination IP
	 * @param _id The ID number of the flow
	 * @param data_size The amount of data to send as part of this flow, in MB
	 * @param start_delay The delay in starting to send this flow, in millis
	 */
	public Flow(long src, long dest, int _id, long data_size, long start_delay) {
		this.src = src;
		this.dest = dest;
		this._id = _id;
		this.data_size = data_size;
		// 1000 = millis in second
		this.start_at = System.currentTimeMillis() + start_delay;
		// convert MB to bytes then divide then round up
		this.num_packets = ((data_size * 1000000) / 1024) + 1;
		System.out.println(this.num_packets);
		this.i = 0;
		this.numSent = 0;
		this.window = 5; // fixed window size
	}
	
	public Packet getPacket() {
		if ((this.start_at < System.currentTimeMillis()) && (!this.isDone()) && (this.numSent < this.window)) {
			this.numSent++;
			return new Packet(this.src, this.dest, "DOOM" + this.i);
		}
		return null;
	}
	
	public void recievedPacket(Packet p) {
		// Algorithm: send same packet at a time until done.
		if (p.getPayload().equals("ACK" + this.i)) {
			this.i = this.i + 1;
			this.numSent = 0;
		}
	}
	
	public String toString() {
		return "{Src: " + src + " Dest: " + dest + "}";
	}
	
	public long getIndex() {
		return this.i;
	}
	
	public boolean isDone() {
		return this.i >= this.num_packets;
	}

}
