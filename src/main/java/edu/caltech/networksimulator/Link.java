/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.caltech.networksimulator.datacapture.DataCaptureTool;
import edu.caltech.networksimulator.datacapture.DataCaptureToolHelper;

/**
 * @authors Francesco, Carly
 *
 *          Links represent the communication lines that connect hosts and
 *          routers together.
 * 
 *          Links connect hosts and routers, and carry packets from one end to
 *          the other. Every link has a specified capacity in bits per second.
 *          You may assume that every host and router can process an infinite
 *          amount of incoming data instantaneously, but outgoing data must sit
 *          on a link buffer until the link is free. Link buffers are first-in,
 *          first-out. Packets that try to enter a full buffer will be dropped.
 *          For the purpose of this project, all links are half-duplex (data can
 *          flow in both directions, but only in one direction at a time).
 */
public class Link extends NetworkComponent {

	private static final int IDLE = 0, SENDING_FROM_1 = 1, SENDING_FROM_2 = 2;
	private static final double DROPPED_FRACTION = 0.0;
	
	private static final String SENT_LINE_NAME = "Link Rate";
	private static final String DROPPED_LINE_NAME = "Dropped Rate";
	private static final String BUFFER_LINE_NAME = "Buffer size (% capacity)";
	
	private static final int DATA_HIST_SIZE = 50;
	
	private long lastPacketDropped, lastPacketSent;
	
	private NetworkComponent end1, end2;

	// Packets trying to enter a full queue will be dropped
	private LinkedBlockingQueue<Sendable> queue;

	private long capacity, propagationDelay, bufferSize, bufferUsed;

	/**
	 * @param name
	 *            of the link
	 * @param capacity
	 *            of the link in bits per second
	 * @param propagationDelay
	 *            in milliseconds
	 * @param bufferSize
	 *            in bytes
	 */
	public Link(String name, int capacity, long propagationDelay, long bufferSize) {
		super(name);
		this.capacity = capacity;
		this.propagationDelay = propagationDelay;
		this.bufferSize = bufferSize;

		queue = new LinkedBlockingQueue<Sendable>();

		bufferUsed = 0;
		lastPacketDropped = 0;
		lastPacketSent = 0;
	}

	/**
	 * @param comp
	 */
	public void setConnection(NetworkComponent comp) {
		if (end1 == null)
			end1 = comp;
		else if (end2 == null)
			end2 = comp;
		else
			throw new NetworkException("Links can only link 2 network components");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		// Initialize data capture tools
		for (DataCaptureTool dc : getDataCollectors()) {
			
			dc.addData(this, SENT_LINE_NAME, System.currentTimeMillis(),
					0);
			dc.addData(this, DROPPED_LINE_NAME, System.currentTimeMillis(),
					0);
			dc.addData(this, BUFFER_LINE_NAME, System.currentTimeMillis(), bufferUsed);
			dc.setMax(this, SENT_LINE_NAME, this.capacity);
			dc.setMax(this, DROPPED_LINE_NAME, 1);
			dc.setMax(this, BUFFER_LINE_NAME, bufferSize);
			
			dc.setDataSmoothingRange(this, SENT_LINE_NAME, DATA_HIST_SIZE);
			dc.setDataSmoothingRange(this, DROPPED_LINE_NAME, DATA_HIST_SIZE);
		}

		int linkState = IDLE;
		while (!super.receivedStop()) {
			
			DataCaptureToolHelper.addData(getDataCollectors(), this, DROPPED_LINE_NAME, System.currentTimeMillis() - (System.currentTimeMillis() - this.lastPacketDropped) / 2,
					1.0 / (System.currentTimeMillis() - this.lastPacketDropped + 1));

			// Try to get another sendable. if fails, allow loop to start again
			Sendable next;
			try {
				next = queue.poll(500, TimeUnit.MILLISECONDS);
				if (next == null)
					continue;

			} catch (InterruptedException e1) {
				continue;
			}

			// Send data
			if (linkState == IDLE) {
				linkState = next.to.equals(end1) ? SENDING_FROM_2 : SENDING_FROM_1;

				try {
					Thread.sleep(propagationDelay);
				} catch (InterruptedException e) {
				}

			} else {

				// If we need to swap directions, simply start from scratch,
				// since the time has already passed
				if ((next.to.equals(end1) && linkState == SENDING_FROM_1)
						|| (next.to.equals(end2) && linkState == SENDING_FROM_2)) {
					linkState = next.to.equals(end1) ? SENDING_FROM_2 : SENDING_FROM_1;

					try {
						Thread.sleep(propagationDelay);
					} catch (InterruptedException e) {
					}
				} else {

					long capacityWait = next.packet.getPacketSizeBits() / capacity;
					long minWait = next.sendNET - System.currentTimeMillis();

					try {
						Thread.sleep(Long.max(capacityWait, minWait));
					} catch (InterruptedException e) {
					}
				}
			}

			// Having waited, now send
			bufferUsed = Math.max(bufferUsed - next.packet.getPacketSize(), 0); // Sometimes it goes negative
			next.to.offerPacket(next.packet, this);
			
			// If nothing else is happening, move back to idle state
			if (queue.isEmpty())
				linkState = IDLE;
			
			
			DataCaptureToolHelper.addData(getDataCollectors(), this, SENT_LINE_NAME, System.currentTimeMillis(), 1000.0 * next.packet.getPacketSizeBits() / (System.currentTimeMillis() - this.lastPacketSent + 1));
			
			DataCaptureToolHelper.addData(getDataCollectors(), this, BUFFER_LINE_NAME, System.currentTimeMillis(),
					bufferUsed);
			

			lastPacketSent = System.currentTimeMillis();

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.networksimulator.NetworkComponent#offerPacket(edu.caltech.
	 * networksimulator.Packet)
	 */
	@Override
	public void offerPacket(Packet p, NetworkComponent n) {

		// keeps packet if the buffer is not full, but drops a small percentage
		if ((bufferUsed + p.getPacketSize() <= bufferSize) && (Math.random() >= DROPPED_FRACTION)) {
			
			if((NetworkSimulator.PRINT_ROUTING && p.isRouting()) && NetworkSimulator.PRINT_LINK_PACKETS || (!p.isRouting() && NetworkSimulator.PRINT_LINK_PACKETS))
				System.out.println(getComponentName() + "\t successfully received packet p: " + p + "\t from " + n.getComponentName());
			// Add the packet to the queue, with the delay as specified
			queue.add(new Sendable(System.currentTimeMillis() + propagationDelay, p, end1.equals(n) ? end2 : end1));
			bufferUsed += p.getPacketSize();
		} else {
			DataCaptureToolHelper.addData(getDataCollectors(), this, DROPPED_LINE_NAME, System.currentTimeMillis() - (System.currentTimeMillis() - this.lastPacketDropped) / 2,
					1.0 / (System.currentTimeMillis() - this.lastPacketDropped + 1));
			
			lastPacketDropped = System.currentTimeMillis();
			
			if((NetworkSimulator.PRINT_ROUTING && p.isRouting() && NetworkSimulator.PRINT_LINK_PACKETS) || (!p.isRouting() && NetworkSimulator.PRINT_LINK_PACKETS))
				System.out.println(getComponentName() + "\t is dropping packet p: " + p + "\t from " + n.getComponentName());
		}
	}

	@Override
	public boolean finished() {
		return queue.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Link && ((Link) o).end1.equals(end1) && ((Link) o).end2.equals(end2) && ((Link) o).getComponentName().equals(getComponentName());
	}
	
	@Override
	public String toString() {
		return "Link " + getComponentName() + ":" + (end1 != null ? end1.getComponentName() : "NULL") + "<->" + (end2 != null ? end2.getComponentName() : "NULL");
	}
	
	/**
	 * Get the percentage filled of the buffer
	 * @return A double value from 0 to 1
	 */
	public double getBufferFill() {
		if ((((double) bufferUsed) / bufferSize) < 0) {
			throw new NetworkException("Buffer fill percent is negative???");
		}
		return ((double) bufferUsed) / bufferSize;
	}

	/**
	 * Represents something that is going to be sent
	 * @author Francesco
	 *
	 */
	private class Sendable {

		/**
		 * A system time (in ms) that this packet cannot be sent earlier than
		 */
		public long sendNET;
		
		/**
		 * The packet to send
		 */
		public Packet packet;
		
		/**
		 * The end to send it to
		 */
		public NetworkComponent to;

		/**
		 * Creates a new Sendable with the given information
		 * @param sendNET The time which is the absolute easrliest the packet can arrive
		 * @param packet The packet to send
		 * @param to Who to send it to
		 */
		public Sendable(long sendNET, Packet packet, NetworkComponent to) {
			this.sendNET = sendNET;
			this.packet = packet;
			this.to = to;
		}
	}

}
