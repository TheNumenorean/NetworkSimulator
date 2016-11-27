/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Francesco, Carly
 *
 *         Routers represent the network equipment that sits between hosts.
 * 
 *         Routers may have an arbitrary number of links connected. Routers will
 *         implement a dynamic routing protocol that uses link cost as a
 *         distance metric and route packets along a shortest path according to
 *         this metric. Each link will have a static cost, based on some
 *         intrinsic property of the link (e.g. its 'length'), and a dynamic
 *         cost, dependent on link congestion. This dynamic routing protocol
 *         must be decentralized, and thus will use message passing to
 *         communicate among routers. This message passing must send packets
 *         along the link (and thus coexist with the rest of the simulation);
 *         'telepathy' among nodes is not permitted, and all of the constraints
 *         that come with the fact that networks are distributed systems must be
 *         followed.
 */
public class Router extends NetworkComponent implements Addressable {

	// Routing table as map
	private Map<Long, NetworkComponent> routingTable;

	// for keeping track of which links we are directly connected to
	private Set<Link> connectedLinks;
	private Set<Link> hostLinks, switchLinks;

	private boolean routingTableBuilt;

	private long ip;

	/**
	 * 
	 */
	public Router(String name) {
		super(name);
		// how to initialize map?
		routingTable = new TreeMap<Long, NetworkComponent>();
		connectedLinks = new TreeSet<Link>();
		routingTableBuilt = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		Packet p = new Packet(ip, -1, "HELLO");

		while (hostLinks.size() + switchLinks.size() != connectedLinks.size()) {
			for (Link l : connectedLinks) {
				if (!hostLinks.contains(l) && !switchLinks.contains(l))
					l.offerPacket(p, this);
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
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
		System.out.println(
				getComponentName() + "\t successfully received packet p: " + p + "\t from " + n.getComponentName());
		
		if(p.getPayload().startsWith("HELLO")) {
			n.offerPacket(new Packet(ip, -1, "HI " + ComponentType.SWITCH), this);
			return;
		}
		
		if(p.getPayload().startsWith("HI")) {
			ComponentType type = ComponentType.valueOf(p.getPayload().split(" ")[1]);
			
			switch(type) {
			case SWITCH:
				switchLinks.add((Link) n);
				break;
			case HOST:
				hostLinks.add((Link) n);
				break;
			}
			
			routingTable.put(p.getSrc(), n);
			return;
		}

		// *Look up in the routing table*
		NetworkComponent l = routingTable.get(p.getDest());
		if (l != null)
			l.offerPacket(p, this);
		else
			System.out.println("Router " + this + " dropping packet: no routing entry");
	}

	@Override
	public long getMACAddress() {
		return -1;
	}

	@Override
	public long getIP() {
		return ip;
	}

	public void setIP(long ip) {
		this.ip = ip;
	}

	@Override
	public boolean finished() {
		return routingTableBuilt;
	}

	public void addLink(Link l) {
		connectedLinks.add(l);
		l.setConnection(this);
	}

}
