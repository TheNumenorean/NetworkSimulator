/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @author Francesco
 *
 */
public class Host extends NetworkComponent implements Addressable  {
	
	private long macAddress;
	private long ip;

	/**
	 * @param name
	 */
	public Host(String name, Link l, long physicalAddr) {
		super(name);
		l.setConnection(this);
		macAddress = physicalAddr;
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
		return false;
	}

	@Override
	public long getMACAddress() {
		return macAddress;
	}

	@Override
	public long getIP() {
		return ip;
	}
	
	public void setIP(long ip) {
		this.ip = ip;
	}

}
