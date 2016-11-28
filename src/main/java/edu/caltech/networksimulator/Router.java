/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

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

	public static final String IDENTITY_REQUEST_HEADER = "HELLO";
	public static final String IDENTITY_REQUEST_RESPONSE_HEADER = "HI";
	public static final String ROUTING_PACKET_HEADER = "ROUTING";

	// Routing table as map
	private Map<Long, Routing> routingTable;

	// for keeping track of which links we are directly connected to
	private Set<Link> connectedLinks;
	private Map<Long, Link> hostLinks, switchLinks;

	private boolean initialRoutingTableBuilt;

	private long ip;

	/**
	 * 
	 */
	public Router(String name) {
		super(name);
		// how to initialize map?
		routingTable = new ConcurrentSkipListMap<Long, Routing>();
		connectedLinks = new TreeSet<Link>();
		initialRoutingTableBuilt = false;
		
		hostLinks = new TreeMap<Long, Link>();
		switchLinks = new TreeMap<Long, Link>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		Packet p = new Packet(ip, -1, IDENTITY_REQUEST_HEADER);

		while (hostLinks.size() + switchLinks.size() != connectedLinks.size()) {
			for (Link l : connectedLinks) {
				if (!hostLinks.values().contains(l) && !switchLinks.values().contains(l))
					l.offerPacket(p, this);
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		initialRoutingTableBuilt = true;

		System.out.println("Router "  + this + " successfully completed pinging local links");
		
		while (!this.receivedStop()) {
			String payload = "ROUTING";

			for (Entry<Long, Routing> routing : routingTable.entrySet())
				payload = payload + " " + routing.getKey() + ":" + routing.getValue().cost;

			for (NetworkComponent link : switchLinks.values())
				link.offerPacket(p, this);

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

		if (p.getDest() == ip || p.getDest() == -1) {

			if (p.getPayload().startsWith(IDENTITY_REQUEST_HEADER)) {
				n.offerPacket(new Packet(ip, p.getSrc(), IDENTITY_REQUEST_RESPONSE_HEADER + " " + ComponentType.SWITCH), this);
				
			} else if (p.getPayload().startsWith(IDENTITY_REQUEST_RESPONSE_HEADER)) {
				ComponentType type = ComponentType.valueOf(p.getPayload().split(" ")[1]);

				switch (type) {
				case SWITCH:
					switchLinks.put(p.getSrc(), (Link) n);
					break;
				case HOST:
					hostLinks.put(p.getSrc(), (Link) n);
					routingTable.put(p.getSrc(), new Routing(((Link) n).getBufferFill(), n));
					break;
				}

			} else if (p.getPayload().startsWith(ROUTING_PACKET_HEADER)) {

				for (String routing : p.getPayload().substring(ROUTING_PACKET_HEADER.length()).split(" ")) {

					String[] routingElements = routing.split(":");

					try {
						
						// Update the routing table iff the ip is not a local host, and the ip either doesnt already exist or the offered one is better.

						Routing newRouting = new Routing(Double.parseDouble(routingElements[1]) + ((Link) n).getBufferFill(), n);
						long routingIP = Long.parseLong(routingElements[0]);
						if (!hostLinks.containsKey(routingIP) && (!routingTable.containsKey(routingElements[0]) || routingTable.get(routingElements).cost < newRouting.cost)) {
							routingTable.put(routingIP, newRouting);
						}
						
					} catch (Exception e) {
						System.err.println("Received a malformed routing packet!");
						return;
					}

				}
			}

		} else {

			// *Look up in the routing table*
			Routing l = routingTable.get(p.getDest());
			if (l != null)
				l.link.offerPacket(p, this);
			else
				System.out.println("Router " + this + " dropping packet: no routing entry");
		}
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
		return initialRoutingTableBuilt;
	}

	public void addLink(Link l) {
		connectedLinks.add(l);
		l.setConnection(this);
	}

	private class Routing {
		public double cost;
		public NetworkComponent link;

		public Routing(double cost, NetworkComponent link) {
			this.cost = cost;
			this.link = link;
		}
	}

}
