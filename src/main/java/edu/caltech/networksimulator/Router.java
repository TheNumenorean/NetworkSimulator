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
	private static final long ROUTING_DELAY = 3000;

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
		// Routing table keeps track of which IPs we can reach, at which costs,
		// and through which links.
		routingTable = new ConcurrentSkipListMap<Long, Routing>();

		// We want to know our neighbors.
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

		// Broadcast our existence in the network
		Packet p = new Packet(ip, -1, IDENTITY_REQUEST_HEADER);

		// Keep broadcasting while we figure out our neighboring switches
		// (routers) and hosts
		while (hostLinks.size() + switchLinks.size() != connectedLinks.size()) {
			for (Link l : connectedLinks) {
				if (!hostLinks.values().contains(l) && !switchLinks.values().contains(l))
					l.offerPacket(p, this);
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// local host links should be 0 (we add in the dynamic cost of the link
		// upon sending)
		for (Entry<Long, Link> host : hostLinks.entrySet()) {
			routingTable.get(host.getKey()).cost = 0;
		}
		initialRoutingTableBuilt = true;

		System.out.println("Router " + this + " successfully completed pinging local links");

		while (!this.receivedStop()) {

			// broadcast multiple times in quick succession to neighbors
			// to accommodate for everyone changing their routing tables at the same time
			for (int i = 0; i < 5; i++) {
				for (NetworkComponent link : switchLinks.values()) {
					// Only send out routing packets if we have a routing table
					if (!routingTable.isEmpty()) {
						String payload = "ROUTING";

						for (Entry<Long, Routing> routing : routingTable.entrySet()) {
							if (!routing.getValue().link.equals(link))
								// dynamically add the link cost
								payload = payload + " " + routing.getKey() + ":"
									+ (routing.getValue().cost + routing.getValue().link.getBufferFill());
						}

						Packet broadcast = new Packet(ip, -1, payload);
						link.offerPacket(broadcast, this);
					}
				}
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// quiet down for a bit
			try {
				Thread.sleep(ROUTING_DELAY);
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

		if ((NetworkSimulator.PRINT_ROUTING && p.isRouting())
				|| (!p.isRouting() && NetworkSimulator.PRINT_ROUTER_PACKETS))
			System.out.println(
					getComponentName() + "\t successfully received packet p: " + p + "\t from " + n.getComponentName());

		if (p.getDest() == ip || p.getDest() == -1) {

			if (p.getPayload().startsWith(IDENTITY_REQUEST_HEADER)) {
				n.offerPacket(new Packet(ip, p.getSrc(), IDENTITY_REQUEST_RESPONSE_HEADER + " " + ComponentType.SWITCH),
						this);

			} else if (p.getPayload().startsWith(IDENTITY_REQUEST_RESPONSE_HEADER)) {
				ComponentType type = ComponentType.valueOf(p.getPayload().split(" ")[1]);

				switch (type) {
				case SWITCH:
					if (!switchLinks.containsKey(p.getSrc()))
						switchLinks.put(p.getSrc(), (Link) n);
					break;
				case HOST:
					if (!hostLinks.containsKey(p.getSrc())) {
						hostLinks.put(p.getSrc(), (Link) n);
						// Hosts directly connected have zero cost (we add the link in later)
						routingTable.put(p.getSrc(), new Routing(0, (Link) n));
					}
					break;
				}

			} else if (p.getPayload().startsWith(ROUTING_PACKET_HEADER)) {

				if (!initialRoutingTableBuilt)
					return;

				String reducedPayload = p.getPayload().substring(ROUTING_PACKET_HEADER.length()).trim();
				for (String routing : reducedPayload.split(" ")) {

					// An empty routing table was sent
					if (routing.isEmpty())
						break;

					String[] routingElements = routing.split(":");

					try {

						// Update the routing table iff the ip is not a local
						// host, and the ip either doesnt already exist or the
						// offered one is better.

						Routing newRouting = new Routing(
								Double.parseDouble(routingElements[1]), (Link) n);

						long routingIP = Long.parseLong(routingElements[0]);

						if (!hostLinks.containsKey(routingIP) && (!routingTable.containsKey(routingIP)
								|| (routingTable.get(routingIP).cost + routingTable.get(routingIP).link.getBufferFill()
									> newRouting.cost + newRouting.link.getBufferFill()) )) {
								// || routingTable.get(routingIP).link.equals(n))) {
							routingTable.put(routingIP, newRouting);
						}

					} catch (Exception e) {
						System.err.println("Received a malformed routing packet!" + p.getPayload());
						e.printStackTrace();
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
				System.out.println(this + " dropping packet: no routing entry");
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

	/**
	 * Add a link to this router
	 * @param l The link to add (must not be null)
	 */
	public void addLink(Link l) {
		connectedLinks.add(l);
		l.setConnection(this);
	}

	/**
	 * Represents a routing for a routing table
	 * @author Francesco
	 *
	 */
	private class Routing {
		public double cost;
		public Link link;

		/**
		 * Creates a new routing with the given cost and link
		 * @param cost The cost of the routing
		 * @param link The link which is routed to
		 */
		public Routing(double cost, Link link) {
			this.cost = cost;
			this.link = link;
		}
	}

}
