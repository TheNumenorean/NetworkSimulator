/**
 * 
 */
package edu.caltech.networksimulator;

import edu.caltech.networksimulator.datacapture.DataCaptureToolHelper;

/**
 * @author Francesco
 *
 * Routers represent the network equipment that sits between hosts.
 * 
 * Routers may have an arbitrary number of links connected.
 * Routers will implement a dynamic routing protocol that uses link cost as a
 * distance metric and route packets along a shortest path according to this
 * metric. Each link will have a static cost, based on some intrinsic property
 * of the link (e.g. its 'length'), and a dynamic cost, dependent on link congestion.
 * This dynamic routing protocol must be decentralized, and thus will use message
 * passing to communicate among routers. This message passing must send packets
 * along the link (and thus coexist with the rest of the simulation); 'telepathy'
 * among nodes is not permitted, and all of the constraints that come with the
 * fact that networks are distributed systems must be followed.
 */
public abstract class Router extends NetworkComponent implements Addressable {

	private NetworkComponent end1, end2;
	
	/**
	 * 
	 */
	public Router(String name) {
		super(name);
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
//		while (!super.receivedStop()) {
//			// Nothing. Purely reactionary.
//			// Future: broadcast routing table
//		}
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
		System.out.println(
				getComponentName() + "\t successfully received packet p: " + p + "\t from " + n.getComponentName());
		// *Look up in the routing table*
		if (end1.equals(n)) {
			end2.offerPacket(p, this);
		} else { // should be else if
			end1.offerPacket(p, this);
		}
	}


}
