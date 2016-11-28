/**
 * 
 */
package edu.caltech.networksimulator;

import edu.caltech.networksimulator.datacapture.DataCaptureToolHelper;

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
public class Flow extends NetworkComponent {
	
	// Where it's going and what the flow is doing
	private final long src, dest;
	private final long data_size;
	private final long start_at;
	private final long num_packets;
	
	// for window size adjustment
	private WindowAlgorithm alg;
	
	// detecting packet timeouts
	private static final long TIMEOUT = 500;
	private long lastSentTime; // time of last sent packet
	
	// detecting dupACKs
	private int dupACKcount;
	
	// For keeping track of what we've sent
	private int idxReceived; // index of last received ACK
	private int idxSent; // index of last sent packet

	
	/**
	 * @param src The source IP
	 * @param dest The destination IP
	 * @param id The ID number of the flow
	 * @param data_size The amount of data to send as part of this flow, in MB
	 * @param start_delay The delay in starting to send this flow, in millis
	 */
	public Flow(long src, long dest, String name, long data_size, long start_delay) {
		// Setup of what and where
		super(name);
		this.src = src;
		this.dest = dest;
		this.data_size = data_size;
		this.start_at = System.currentTimeMillis() + start_delay;
		// convert MB to bytes then divide then round up
		this.num_packets = ((data_size * 1000000) / 1024) + 1;
		
		// Set up the window algorithm
		setupAlg("Static");
		
		// Set up tracking of where we are in the flow
		this.idxReceived = -1;
		this.idxSent = -1;
		this.dupACKcount = 0;
	}
	
	/*
	 * We can detect dropped packets if the last sent time
	 * plus the timeout is 
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//DataCaptureToolHelper.addData(getDataCollectors(), this, "Index Recieved", System.currentTimeMillis(),
		//		this.idxReceived);
		//DataCaptureToolHelper.addData(getDataCollectors(), this, "Index Sent", System.currentTimeMillis(),
		//		this.idxSent);
		//DataCaptureToolHelper.addData(getDataCollectors(), this, "Window Size", System.currentTimeMillis(),
		//		this.alg.getW());
		
		while (!super.receivedStop()) {
			// keep going if we're done with our flow, because others
			// may not be
			if (this.idxSent > 0) { // make sure we've sent at least one
				if (System.currentTimeMillis() > lastSentTime + TIMEOUT) {
					// detected dropped packet by timeout
					alg.droppedPacket();
					DataCaptureToolHelper.addData(getDataCollectors(), this, "Window Size", System.currentTimeMillis(),
							this.alg.getW());
					// Assume all our packets sent so far were in vain. Reset:
					this.idxSent = this.idxReceived;
					this.dupACKcount = 0;
					DataCaptureToolHelper.addData(getDataCollectors(), this, "Index Sent", System.currentTimeMillis(),
							this.idxSent);
				}
			}
			
			try {
				Thread.sleep(500); // don't try this too often
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Determines which window size algorithm to use and
	 * creates an instance of that algorithm.
	 */
	private void setupAlg(String name) {
		if (name.equals("Simple")) {
			this.alg = new SimpleWindow(name);
		} else {
			// Static window of size 5
			this.alg = new StaticWindow("Static", 5);
		}
	}
	
	/*
	 * Returns a Packet if there is one to send; otherwise returns null.
	 */
	public Packet getPacket() {
		if ((this.start_at < System.currentTimeMillis()) && // head start over
			(!this.finished()) && // haven't sent all the packets yet
			(this.idxReceived + alg.getW() >= this.idxSent)) { // haven't sent all the packets in this window yet
			
			// print some stuff
			System.out.println("Sending packet " + (this.idxSent + 1));
			// increment the index that we've sent at
			this.idxSent++;
			DataCaptureToolHelper.addData(getDataCollectors(), this, "Index Sent", System.currentTimeMillis(),
					this.idxSent);
			
			System.out.println("window size: " + alg.getW() +
					   "\t num recieved: " + idxReceived + 
					   "\t num sent: " + idxSent);
			
			// create and return that packet
			lastSentTime = System.currentTimeMillis();
			return new Packet(this.src, this.dest, "DOOM", this.idxSent, this.getComponentName());
		}
		return null;
	}
	
	/*
	 * This function decides what to do with a received packet;
	 * - determines if it is the correct ACK
	 * - determines if it is a dupACK
	 * - ignores it if not part of this flow
	 * 
	 * (non-Javadoc)
	 * @see edu.caltech.networksimulator.NetworkComponent#offerPacket(edu.caltech.networksimulator.Packet, edu.caltech.networksimulator.NetworkComponent)
	 */
	@Override
	public void offerPacket(Packet p, NetworkComponent n) {
		if (p.getSeqID().equals(getComponentName())) {
			// packet meant for us
			if (p.getSeqNum() == this.idxReceived + 1) {
				// Next packet in the sequence: an ACK! Inform the window algorithm
				alg.ACKPacket(p);
				DataCaptureToolHelper.addData(getDataCollectors(), this, "Window Size", System.currentTimeMillis(),
						this.alg.getW());
				// Reset the dupACK counter
				dupACKcount = 0;
				// Can now send more packets
				idxReceived++;
				DataCaptureToolHelper.addData(getDataCollectors(), this, "Index Recieved", System.currentTimeMillis(),
						this.idxReceived);
			} else if (p.getSeqNum() == this.idxReceived) {
				// start of a dupACK trail
				dupACKcount++;
				
				if (dupACKcount >= 3) { // too many dupACKs => a dropped packet
					alg.droppedPacket();
					DataCaptureToolHelper.addData(getDataCollectors(), this, "Window Size", System.currentTimeMillis(),
							this.alg.getW());
					// Assume all our packets sent so far were in vain. Reset:
					this.idxSent = this.idxReceived;
					this.dupACKcount = 0;
					DataCaptureToolHelper.addData(getDataCollectors(), this, "Index Sent", System.currentTimeMillis(),
							this.idxSent);
				}
			} else { // From some other weird place in the sequence
				// Ignore this.
			}
		}
		
		// graph some things
		DataCaptureToolHelper.addData(getDataCollectors(), this, "RTT", System.currentTimeMillis(),
				System.currentTimeMillis() - p.getSentTime());
		
		// Print some things
		System.out.println("window size: " + alg.getW() +
						   "\t num recieved: " + idxReceived + 
						   "\t num sent: " + idxSent);
		
		
	}
	
	@Override
	public String toString() {
		return "{Src: " + src + " Dest: " + dest + "}";
	}

	@Override
	public boolean finished() {
		return this.idxReceived >= this.num_packets;
	}

}
