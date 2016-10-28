/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @authors Francesco, Carly
 *
 */
public class Link extends NetworkComponent {

	private NetworkComponent end1, end2;

	long capacity, delayMS, bufferSize;

	/**
	 * @param name
	 * @param bufferSize
	 */
	public Link(String name, int capacity, long delayMS, long bufferSize) {
		super(name);
		this.capacity = capacity;
		this.delayMS = delayMS;
		this.bufferSize = bufferSize;
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
		while (!super.stop) {

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
		System.out.println(getComponentName() + " recieved packet p: " + p + "\t from " + n.getComponentName());
		try {
			Thread.sleep(delayMS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (end1 != n) {
			end1.offerPacket(p, this);
		} else if (end2 != n) {
			end2.offerPacket(p, this);
		} else {
			System.out.println(n.getComponentName() + " is not connected to either end of " + getComponentName()); 
		}
	}

	@Override
	public boolean finished() {
		// True for a link that has nothing in its buffer.
		return true;
	}

}
