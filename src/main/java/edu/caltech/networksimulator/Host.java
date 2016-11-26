/**
 * 
 */
package edu.caltech.networksimulator;

import edu.caltech.networksimulator.datacapture.DataCaptureTool;
import edu.caltech.networksimulator.datacapture.DataCaptureToolHelper;

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
	private Link link;
	private Flow flow;

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
			if (flow != null) {
				Packet nextPacket = flow.getPacket();
				if (nextPacket != null) {
					link.offerPacket(nextPacket, this);
				}
				
				DataCaptureToolHelper.addData(getDataCollectors(), this, "Flow Index", System.currentTimeMillis(),
						flow.getIndex());
				DataCaptureToolHelper.addData(getDataCollectors(), this, "Window Size", System.currentTimeMillis(),
						flow.getWindow());
				DataCaptureToolHelper.addData(getDataCollectors(), this, "Num Sent", System.currentTimeMillis(),
						flow.getNumSent());
			}
			
			
			
			// Dont run too often
			try {
				Thread.sleep(500);
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
		String message = p.getPayload();
		if (!(message.substring(0, 3).equals("ACK"))) {
			// Send an acknowledgement to the original message
			// Switch source and destination
			n.offerPacket(new Packet(p.getDest(), p.getSrc(), 
					"ACK" + message.substring(4)), this);
				// last char
		} else { // payload is ACK, inform the flow
			flow.recievedPacket(p);
		}
	}

	@Override
	public boolean finished() {
		if (flow == null) {
			return true;
		} else {
			return flow.isDone();
		}
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
		return o instanceof Host && ((Host)o).getMACAddress() == macAddress;
	}

}
