package edu.caltech.networksimulator.windowalgs;

import edu.caltech.networksimulator.NetworkException;
import edu.caltech.networksimulator.Packet;

public class TCPTahoe extends WindowAlgorithm{

	private enum TahoePhase {
		SLOW_START,
		CONG_AVOID;
	}
	
	private double window;
	private TahoePhase phase;
	private int ssthresh;
	
	public TCPTahoe(String name) {
		super(name);
		window = 1.0;
		phase = TahoePhase.SLOW_START;
		this.FR = true;
		ssthresh = 2;
	}

	@Override
	public void droppedPacket(boolean dupACK) {
		ssthresh = Math.max((int) (window/2.0), 2);
		window = 1;
		phase = TahoePhase.SLOW_START;
	}

	@Override
	public void ACKPacket(Packet p) {
		if (phase == TahoePhase.SLOW_START) {
			window++;
			checkPhase();
		} else if (phase == TahoePhase.CONG_AVOID) {
			window += (1.0 / window);
		} else {
			throw new NetworkException("Window algorithm phase unrecognized");
		}
	}

	@Override
	public void newRTT() {
	}
	
	private void checkPhase() {
		if (window > ssthresh) {
			phase = TahoePhase.CONG_AVOID;
		}
	}
	
	@Override
	// getter for the window size
	public int getW() {
		return (int) window;
	}
	
	private void printStuff() {
		if (phase == TahoePhase.SLOW_START) {
			System.out.println("RTT. Phase: SLOW START.");
		} else if (phase == TahoePhase.CONG_AVOID) {
			System.out.println("RTT. Phase: CONG AVOID.");
		}
		System.out.println("w: " + getW() + " ssthresh: " + ssthresh);
	}
}
