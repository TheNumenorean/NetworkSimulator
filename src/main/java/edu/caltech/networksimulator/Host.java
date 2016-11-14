/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @authors Francesco, Carly
 *
 *  Hosts represent individual endpoint computers, like desktop computers or servers.
 *  
 *  Hosts will have at most one link connected.
 */
public class Host extends NetworkComponent implements Addressable  {
	
	private long macAddress;
	private long ip;
	private Packet packet;
	private Link link;

	/**
	 * @param name
	 */
	public Host(String name, Link l, long physicalAddr) {
		super(name);
		l.setConnection(this);
		this.link = l;
		macAddress = physicalAddr;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		while(!super.receivedStop()) {
			if (packet != null) {
				for (int i = 0; i < 5; i++) { // send a manageable number of packets
					link.offerPacket(packet, this);
				}
			}
			packet = null;
			
			// Dont run too often
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/* (non-Javadoc)
	 * @see edu.caltech.networksimulator.NetworkComponent#offerPacket(edu.caltech.networksimulator.Packet)
	 */
	@Override
	public void offerPacket(Packet p, NetworkComponent n) {
		System.out.println(getComponentName() + "\t recieved packet p: " + p + "\t from " + n.getComponentName());
		
		if (p.getPayload() != "ACK") {
			// Send an acknowledgement to the original message
			// Switch source and destination
			n.offerPacket(new Packet(p.getDest(), p.getSrc(), "ACK"), this);
		}
	}

	@Override
	public boolean finished() {
		return true;
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
	
	public void addPacket(Packet p) {
		System.out.println(getComponentName() + " recieved instruction to send packet p: " + p);
		this.packet = p;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Host && ((Host)o).getMACAddress() == macAddress;
	}

}
