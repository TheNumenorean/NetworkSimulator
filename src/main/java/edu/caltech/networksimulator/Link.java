/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.PriorityQueue;

/**
 * @authors Francesco, Carly
 *
 */
public class Link extends NetworkComponent {

	private NetworkComponent end1, end2;

	private PriorityQueue<Sendable> queue;

	private long capacity, delayMS, bufferSize;

	/**
	 * @param name
	 * @param bufferSize
	 */
	public Link(String name, int capacity, long delayMS, long bufferSize) {
		super(name);
		this.capacity = capacity;
		this.delayMS = delayMS;
		this.bufferSize = bufferSize;

		queue = new PriorityQueue<Sendable>();
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

				synchronized (queue) {

					// Get the next packet that should be sent
					Sendable next = queue.peek();
					long timeToSend = next.sendAt - System.currentTimeMillis();

					// If time to send is now, send it and continue
					if (timeToSend < 1) {
						next.to.offerPacket(next.packet, this);
						queue.poll();
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
		System.out.println(getComponentName() + " received packet from " + n.getComponentName());
		//System.out.println(getComponentName() + " received packet p: " + p + "\t from " + n.getComponentName());

		// TODO: Check buffer size and calculate actual time to send

		synchronized (queue) {
			// Add the packet to the queue, with the delay as specified 
			queue.add(new Sendable(System.currentTimeMillis() + delayMS, p, end1.equals(n) ? end2 : end1));
			queue.notifyAll();
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

	private class Sendable implements Comparable<Sendable> {

		// Instead of sendAt, we should have sendNET (no earlier than)
		public long sendAt;
		public Packet packet;
		public NetworkComponent to;

		public Sendable(long sendAt, Packet packet, NetworkComponent to) {
			this.sendAt = sendAt;
			this.packet = packet;
			this.to = to;
		}

		@Override
		public int compareTo(Sendable o) {
			return (int) (sendAt - o.sendAt);
		}
	}

}
