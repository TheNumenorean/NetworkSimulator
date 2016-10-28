/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @author Francesco
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
	
	public void setConnection(NetworkComponent comp) {
		if(end1 == null)
			end1 = comp;
		else if(end2 == null)
			end2 = comp;
		else
			throw new NetworkException("Links can only link 2 network components");
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(!super.stop) {
			
			
			
			
		}

	}

	/* (non-Javadoc)
	 * @see edu.caltech.networksimulator.NetworkComponent#offerPacket(edu.caltech.networksimulator.Packet)
	 */
	@Override
	public void offerPacket(Packet p) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean finished() {
		// Always true for a link
		return true;
	}

}
