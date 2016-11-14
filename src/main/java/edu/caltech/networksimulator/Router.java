/**
 * 
 */
package edu.caltech.networksimulator;

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

	/**
	 * 
	 */
	public Router(String name) {
		super(name);
	}

}
