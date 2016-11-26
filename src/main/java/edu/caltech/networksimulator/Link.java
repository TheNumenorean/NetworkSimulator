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

	private int sentPackets, droppedPackets;
	
	private long lastPacketDropped, lastPacketSent;

	private NetworkComponent end1, end2;

	// Packets trying to enter a full queue will be dropped
	private LinkedBlockingQueue<Sendable> queue;

	private long capacity, propagationDelay, bufferSize, currentSize;

	/**
	 * @param name
	 *            of the link
	 * @param capacity
	 *            of the link in bits per second
	 * @param propagationDelay
	 *            in milliseconds
	 * @param bufferSize
	 *            in bytes (?)
	 */
	public Link(String name, int capacity, long propagationDelay, long bufferSize) {
		super(name);
		this.capacity = capacity;
		this.propagationDelay = propagationDelay;
		this.bufferSize = bufferSize;

		queue = new LinkedBlockingQueue<Sendable>();

		sentPackets = 0;
		droppedPackets = 0;
		currentSize = 0;
		
		lastPacketDropped = System.currentTimeMillis();
		lastPacketSent = System.currentTimeMillis();

		// Initialize data capture tools
		for (DataCaptureTool dc : getDataCollectors()) {
			
			dc.addData(this, "Sent Packets", System.currentTimeMillis(),
					0);
			dc.addData(this, "Dropped Packets", System.currentTimeMillis(),
					0);
			dc.addData(this, "Buffer Size", System.currentTimeMillis(), currentSize);
			dc.setMax(this, "Sent Packets", 1);
			dc.setMax(this, "Dropped Packets", 1);
			dc.setMax(this, "Buffer Size", bufferSize);
		}
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

		int linkState = IDLE;
		while (!super.receivedStop()) {

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
			next.to.offerPacket(next.packet, this);
			currentSize -= next.packet.getPacketSize();
			sentPackets++;

			DataCaptureToolHelper.addData(getDataCollectors(), this, "Sent Packets", System.currentTimeMillis() - (System.currentTimeMillis() - this.lastPacketSent) / 2,
					1.0 / (System.currentTimeMillis() - this.lastPacketSent + 1));
			DataCaptureToolHelper.addData(getDataCollectors(), this, "Buffer Size", System.currentTimeMillis(),
					currentSize);

			
			lastPacketSent= System.currentTimeMillis();
			
			if (queue.isEmpty())
				linkState = IDLE;

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

		// drops packet if the buffer is full
		if (currentSize + p.getPacketSize() <= bufferSize) {
			System.out.println(
					getComponentName() + "\t successfully received packet p: " + p + "\t from " + n.getComponentName());
			// Add the packet to the queue, with the delay as specified
			queue.add(new Sendable(System.currentTimeMillis() + propagationDelay, p, end1.equals(n) ? end2 : end1));
			currentSize += p.getPacketSize();
		} else {
			droppedPackets++;
			DataCaptureToolHelper.addData(getDataCollectors(), this, "Dropped Packets", System.currentTimeMillis() - (System.currentTimeMillis() - this.lastPacketDropped) / 2,
					1 / (System.currentTimeMillis() - this.lastPacketDropped + 1));
			
			lastPacketDropped = System.currentTimeMillis();
			System.out
					.println(getComponentName() + "\t is dropping packet p: " + p + "\t from " + n.getComponentName());
		}
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public boolean finished() {
		return queue.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Link && ((Link) o).end1.equals(end1) && ((Link) o).end2.equals(end2);
	}

	private class Sendable {

		// Send no earlier than this time
		public long sendNET;
		public Packet packet;
		public NetworkComponent to;

		public Sendable(long sendNET, Packet packet, NetworkComponent to) {
			this.sendNET = sendNET;
			this.packet = packet;
			this.to = to;
		}
	}

}
