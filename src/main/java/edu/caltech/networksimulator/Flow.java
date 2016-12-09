/**
 * 
 */
package edu.caltech.networksimulator;

import edu.caltech.networksimulator.datacapture.DataCaptureTool;
import edu.caltech.networksimulator.datacapture.DataCaptureToolHelper;
import edu.caltech.networksimulator.windowalgs.ExponentialWindow;
import edu.caltech.networksimulator.windowalgs.SimpleWindow;
import edu.caltech.networksimulator.windowalgs.StaticWindow;
import edu.caltech.networksimulator.windowalgs.TCPFAST;
import edu.caltech.networksimulator.windowalgs.TCPReno;
import edu.caltech.networksimulator.windowalgs.TCPTahoe;
import edu.caltech.networksimulator.windowalgs.WindowAlgorithm;

/**
 * @author Carly
 *
 *         Flows represent active connections. Flows will have source and
 *         destination addresses; packets generated by each flow will have the
 *         same destination address, to ensure they are routed correctly.
 * 
 *         Flows have a source and destination address, and generate packets at
 *         a rate controlled by the congestion control algorithm defined for
 *         that flow. You should implement at least two different congestion
 *         control algorithms, e.g. TCP Reno and FAST-TCP, and be able to choose
 *         independently between them for each flow. Flows may send a continuous
 *         stream of data, or may send a finite user-specifiable amount of data;
 *         they may also start immediately or after some user-specifiable delay.
 */
public class Flow extends NetworkComponent {

	private static final boolean FLOW_DEBUG = false;

	// Where it's going and what the flow is doing
	private final long src, dest;
	private final long data_size;
	private final long start_at;
	private final long num_packets;

	// for window size adjustment
	private WindowAlgorithm alg;

	// detecting packet timeouts
	private long TIMEOUT = 10000;
	private static final long FLOW_SLEEP = 50;
	private long lastSentTime; // time of last sent packet

	// Some algorithms do things every RTT
	private long lastRTT; // length of last RTT
	private long RTTcounter; // last time we called the RTT operation

	// detecting dupACKs
	private int dupACKcount;
	private int dupACKnum;

	// For keeping track of what we've sent
	private int idxReceived; // index of last received ACK
	private int idxSent; // index of last sent packet
	private int maxIdxSent;

	/**
	 * @param src
	 *            The source IP
	 * @param dest
	 *            The destination IP
	 * @param id
	 *            The ID number of the flow
	 * @param data_size
	 *            The amount of data to send as part of this flow, in MB
	 * @param start_delay
	 *            The delay in starting to send this flow, in millis
	 */
	public Flow(long src, long dest, String name, long data_size, long start_delay, String alg_name) {
		// Setup of what and where
		super(name);
		this.src = src;
		this.dest = dest;
		this.data_size = data_size;
		this.start_at = System.currentTimeMillis() + start_delay;
		// convert MB to bytes then divide then round up
		this.num_packets = ((data_size * 1000000) / 1024) + 1;

		// Set up the window algorithm
		setupAlg(alg_name);

		// Set up tracking of where we are in the flow
		this.idxReceived = -1;
		this.idxSent = -1;
		this.dupACKcount = 0;
		this.maxIdxSent = 1;

		this.lastRTT = TIMEOUT;
		this.RTTcounter = start_at;
	}

	/*
	 * We can detect dropped packets if the last sent time plus the timeout is
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		for (DataCaptureTool dc: getDataCollectors()) { 
			dc.setMax(this, "Percent Done", 1);
			// flow rate
			dc.setDataSmoothingRange(this, "Flow Rate", 10);
		}
		
		while (!super.receivedStop() && !finished()) {
			
			// graph some things
			DataCaptureToolHelper.addData(getDataCollectors(), this, "Window Size", System.currentTimeMillis(), this.alg.getW());
			DataCaptureToolHelper.addData(getDataCollectors(), this, "Percent Done", System.currentTimeMillis(), ((double) idxReceived) / num_packets);

			if (this.idxSent >= 0) { // make sure we've sent at least one
				if (System.currentTimeMillis() > lastSentTime + TIMEOUT) {
					if (FLOW_DEBUG) {
						System.out.println("\t\t\t\t Dropped packet detected");
					}
					// detected dropped packet by timeout
					alg.droppedPacket(false);
					// Assume all our packets sent so far were in vain. Reset:
					this.idxSent = this.idxReceived;
					this.dupACKcount = 0;
					this.lastRTT = TIMEOUT;

				}

				if (System.currentTimeMillis() > RTTcounter + lastRTT) {
					// it's been a while since we informed the alg there was
					// another RTT
					alg.newRTT();
					RTTcounter = System.currentTimeMillis();
				}

			}

			try {
				Thread.sleep(0, (int) FLOW_SLEEP); // don't try this too often
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("DONE WITH FLOW");
	}

	/*
	 * Determines which window size algorithm to use and creates an instance of
	 * that algorithm.
	 */
	private void setupAlg(String name) {
		if (name.equals("Simple")) {
			this.alg = new SimpleWindow(name);
		} else if (name.equals("TCPTahoe")) {
			this.alg = new TCPTahoe(name);
		} else if (name.equals("TCPReno")) {
			this.alg = new TCPReno(name);
		} else if (name.equals("Exponential")) {
			this.alg = new ExponentialWindow(name);
		} else if (name.equals("TCPFAST")) {
			this.alg = new TCPFAST(name);
		} else if (name.equals("Static")) {
			// Static window of size 5
			this.alg = new StaticWindow(name, 100);
		} else {
			throw new NetworkException("Unrecognized window algorithm");
		}
	}

	/*
	 * Returns a Packet if there is one to send; otherwise returns null.
	 */
	public Packet getPacket() {
		if ((this.start_at < System.currentTimeMillis()) && // head start over
				(!this.finished()) && // haven't sent all the packets yet
				(this.idxReceived + alg.getW() > this.idxSent)) { // haven't
																	// sent all
																	// the
																	// packets
																	// in this
																	// window
																	// yet
			this.idxSent++;

			if (this.idxReceived + 1 == this.idxSent) {
				// Start of the new window
				this.lastSentTime = System.currentTimeMillis();
			}

			// print some stuff
			if (NetworkSimulator.PRINT_FLOW_STUFF) {
				System.out.println("Sending packet " + (this.idxSent));
			}
			// increment the index that we've sent at
			this.maxIdxSent = Math.max(maxIdxSent, idxSent);

			if (FLOW_DEBUG) {
				System.out.println("Giving packet. window size: " + alg.getW() + "\t num recieved: " + idxReceived
						+ "\t num sent: " + idxSent);
			}

			// create and return that packet
			return new Packet(this.src, this.dest, "DOOM", this.idxSent, this.getComponentName());
		}
		return null;
	}

	/*
	 * This function decides what to do with a received packet; - determines if
	 * it is the correct ACK - determines if it is a dupACK - ignores it if not
	 * part of this flow
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.networksimulator.NetworkComponent#offerPacket(edu.caltech.
	 * networksimulator.Packet, edu.caltech.networksimulator.NetworkComponent)
	 */
	@Override
	public void offerPacket(Packet p, NetworkComponent n) {
		if (p.getSeqID().equals(getComponentName())) {
			// packet meant for us
			if (p.getSeqNum() == this.idxReceived + 1) { // Correct packet!
				// Extract the RTT and update the timemout
				this.lastRTT = (System.currentTimeMillis() - p.getSentTime());
				this.TIMEOUT = Math.max(this.TIMEOUT, this.lastRTT);
				
				DataCaptureToolHelper.addData(getDataCollectors(), this, "Flow Rate",
						System.currentTimeMillis() - (lastRTT) / 2, p.getPacketSizeBits() / (lastRTT));

				// Next packet in the sequence: an ACK! Inform the window
				// algorithm
				alg.ACKPacket(p);
				// Reset the dupACK counter
				dupACKcount = 0;
				// Can now send more packets
				idxReceived++;
			} else if (p.getSeqNum() == this.dupACKnum) {
				// start of a dupACK trail
				dupACKcount++;

				if (dupACKcount >= 3) { // too many dupACKs => a dropped packet
					alg.droppedPacket(true);
					// Assume all our packets sent so far were in vain. Reset:
					this.idxReceived = this.dupACKnum;
					if (alg.FR) {
						this.idxSent = this.idxReceived;
					}
					this.dupACKcount = 0;
				}
			} else {
				this.dupACKnum = p.getSeqNum();
				this.dupACKcount = 0;
				// From some other weird place in the sequence
				// Count this as a possible start of a dup ack
			}
		}

		// graph some things
		DataCaptureToolHelper.addData(getDataCollectors(), this, "RTT", System.currentTimeMillis(),
				this.lastRTT);

		// Print some things
		if (FLOW_DEBUG) {
			System.out.println("Got packet: " + p + " window size: " + alg.getW() + "\t num recieved: " + idxReceived
					+ "\t num sent: " + idxSent);
		}

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
