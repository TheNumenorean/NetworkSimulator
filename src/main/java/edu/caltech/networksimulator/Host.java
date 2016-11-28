/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.Map;
import java.util.TreeMap;

import edu.caltech.networksimulator.NetworkComponent.ComponentType;
import edu.caltech.networksimulator.datacapture.DataCaptureTool;
import edu.caltech.networksimulator.datacapture.DataCaptureToolHelper;

/**
 * @authors Francesco, Carly
 *
 *          Hosts represent individual endpoint computers, like desktop
 *          computers or servers.
 * 
 *          Hosts will have at most one link connected.
 */
public class Host extends NetworkComponent implements Addressable {

	private long macAddress;
	private long ip;
	private Link link;
	private Flow flow;

	// Stuff for responding to requests
	// Map between flow IDs and sequence numbers
	// keeps track of last seen sequence number for each flow
	private Map<String, Integer> acks;
	
	/**
	 * @param name
	 */
	public Host(String name, Link l, long physicalAddr) {
		super(name);
		l.setConnection(this);
		this.link = l;
		macAddress = physicalAddr;
		this.acks = new TreeMap<String, Integer>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		while (!super.receivedStop()) {
			if (flow != null) {
				Packet nextPacket = flow.getPacket();
				if (nextPacket != null) {
					nextPacket.setSentTime();
					link.offerPacket(nextPacket, this);
				}
			}

			// Don't run too often
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
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
		System.out.println(getComponentName() + "\t recieved packet p: " + p + "\t from " + n.getComponentName());
		String message = p.getPayload();
		if (p.getDest() == this.ip) { // message meant for us
			System.out.println("Packet meant for us");
			if (!(message.equals("ACK"))) { // Message needs an ACK
				System.out.println("Packet needs responding to");
				String id = p.getSeqID();
				int idx = p.getSeqNum();
				// If this is the next packet in the sequence, increment the sequence number
				if (acks.containsKey(id)) { // we have seen this flow before
					System.out.println("we have seen this flow before");
					if (acks.get(id) + 1 == idx) { // we got the next packet
						acks.put(id, idx);
					} // otherwise, wasn't the next, so don't update last seen
				} else { // we have not seen the flow before
					System.out.println("we have not seen this flow before");
					if (idx == 0) { // start right with the first packet
						System.out.println("First packet in a new flow");
						acks.put(id, 0);
						System.out.println("ACKs: " + acks);
					} // otherwise started with the wrong one, pretend we didn't see it.
				}
				
				// Send an acknowledgement to the original message made
				// with the highest sequence number we have gotten so far
				if (acks.containsKey(id)) { // we have seen flow before
					Packet ackP = p.getACK(acks.get(id));
					System.out.println("Offering ack packet: " + ackP);
					n.offerPacket(ackP, this);
					//n.offerPacket(p.getACK(acks.get(id)), this);
				} // otherwise we pretend packet was dropped.

			} else { // payload is ACK meant for us, inform the flow
				if (flow != null) {
					flow.offerPacket(p, this);
				}
			}
		} else if(p.getDest() == -1) {
			if(p.getPayload().startsWith("HELLO")) {
				n.offerPacket(new Packet(ip, p.getSrc(), "HI " + ComponentType.HOST), this);
			}
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

	public void addFlow(Flow f) {
		System.out.println(getComponentName() + " recieved flow f: " + f);
		this.flow = f;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Host && ((Host) o).getMACAddress() == macAddress;
	}

}
