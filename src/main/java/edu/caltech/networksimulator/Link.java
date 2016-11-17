/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.caltech.networksimulator.datacapture.DataCaptureToolHelper;

/**
 * @authors Francesco, Carly
 *
 * Links represent the communication lines that connect hosts and
 * routers together.
 * 
 * Links connect hosts and routers, and carry packets from one end to the other.
 * Every link has a specified capacity in bits per second. You may assume that
 * every host and router can process an infinite amount of incoming data
 * instantaneously, but outgoing data must sit on a link buffer until the link is
 * free. Link buffers are first-in, first-out. Packets that try to enter a full
 * buffer will be dropped. For the purpose of this project, all links are
 * half-duplex (data can flow in both directions, but only in one direction at a time).
 */
public class Link extends NetworkComponent {
	
	private int sentPackets, droppedPackets;

	private NetworkComponent end1, end2;

	// Packets trying to enter a full queue will be dropped
	private LinkedBlockingQueue<Sendable> queue;

	private long capacity, propagationDelay, bufferSize, currentSize;

	/**
	 * @param name of the link
	 * @param capacity of the link in bits per second
	 * @param propagationDelay in milliseconds
	 * @param bufferSize in bytes (?)
	 */
	public Link(String name, int capacity, long propagationDelay, long bufferSize) {
		super(name);
		this.capacity = capacity;
		this.propagationDelay = propagationDelay;
		this.bufferSize = bufferSize;
		this.currentSize = 0;

		queue = new LinkedBlockingQueue<Sendable>();
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
		while (!super.receivedStop()) {

			if (!queue.isEmpty()) {
				// This doesn't quite work, because imagine the link has a
				// really low bandwidth.
				// TODO: account for bandwidth restrictions
				// can send next in same direction after transmission delay
				// can send next in different direction after transmission delay + size of packet

				synchronized (queue) {

					// Get the next packet that should be sent
					Sendable next = queue.peek();
					long timeToSend = next.sendAt - System.currentTimeMillis();

					// If time to send is now, send it and continue
					if (timeToSend < 1) {
						next.to.offerPacket(next.packet, this);
						currentSize -= next.packet.getPacketSize();
						queue.poll();
						sentPackets++;
					} else {
						// In case something is added that will need to be sent
						// sooner, wait on the queue, then check again

						try {
							queue.wait(timeToSend);
						} catch (InterruptedException e) {
						}
					}
				}

			} else {
				
				// If queue is empty, wait until it is filled again
				synchronized (queue) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			
			DataCaptureToolHelper.addData(getDataCollectors(), this, "Sent Packets", System.currentTimeMillis(), sentPackets);
			DataCaptureToolHelper.addData(getDataCollectors(), this, "Dropped Packets", System.currentTimeMillis(), droppedPackets);
			DataCaptureToolHelper.addData(getDataCollectors(), this, "Buffer Size", System.currentTimeMillis(), currentSize);
			
			

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
			System.out.println(getComponentName() + "\t successfully received packet p: " + p + "\t from " + n.getComponentName());
			synchronized (queue) {
				// Add the packet to the queue, with the delay as specified 
				queue.add(new Sendable(System.currentTimeMillis() + propagationDelay, p, end1.equals(n) ? end2 : end1));
				queue.notifyAll();
			}
			currentSize += p.getPacketSize();
		} else {
			droppedPackets++;
			System.out.println(getComponentName() + "\t is dropping packet p: " + p + "\t from " + n.getComponentName());
		}
	}

	@Override
	public void stop() {
		super.stop();

		synchronized (queue) {
			queue.notifyAll();
		}
	}

	@Override
	public boolean finished() {
		return queue.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Link && ((Link) o).end1.equals(end1) && ((Link) o).end2.equals(end2);
	}

	private class Sendable{

		// Instead of sendAt, we should have sendNET (no earlier than)
		public long sendAt;
		public Packet packet;
		public NetworkComponent to;

		public Sendable(long sendAt, Packet packet, NetworkComponent to) {
			this.sendAt = sendAt;
			this.packet = packet;
			this.to = to;
		}
	}

}
