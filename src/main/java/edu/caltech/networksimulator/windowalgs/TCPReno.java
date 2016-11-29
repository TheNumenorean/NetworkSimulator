package edu.caltech.networksimulator.windowalgs;

import edu.caltech.networksimulator.NetworkException;
import edu.caltech.networksimulator.Packet;

public class TCPReno extends WindowAlgorithm{

	private enum RenoPhase {
		SLOW_START,
		CONG_AVOID,
		FAST_RECOVERY;
	}
	
	private double window;
	private RenoPhase phase;
	private int ssthresh;
	private boolean responded = false; // have we responded to a timeout in this window?
	
	public TCPReno(String name) {
		super(name);
		window = 1.0;
		phase = RenoPhase.SLOW_START;
		this.FR = true;
		ssthresh = 2;
	}

	@Override
	public void newRTT() {
		responded = false;
		if (phase == RenoPhase.FAST_RECOVERY) {
			// exit FR/FR after a RTT
			phase = RenoPhase.CONG_AVOID;
		} else if (phase == RenoPhase.SLOW_START) {
			window++;
			checkPhase();
		} else if (phase == RenoPhase.CONG_AVOID) {
			window++;
		} else {
			throw new NetworkException("Window algorithm phase unrecognized");
		}
		//printStuff();
	}

	@Override
	public void droppedPacket(boolean dupACK) {
		ssthresh = Math.max((int) (window/2.0), 2);
		if (dupACK) {
			// Enter Fast Response/Fast Recovery
			// 3 duplicates
			window = ssthresh + 3; // window inflation
			phase = RenoPhase.FAST_RECOVERY;
		} else { // round trip timeout
			if (!responded) {
				window = ssthresh;
				phase = RenoPhase.SLOW_START;
				responded = true;
			}
		}
	}

	@Override
	public void ACKPacket(Packet p) {
		if (phase == RenoPhase.SLOW_START) {
			window++;
			checkPhase();
		} else if (phase == RenoPhase.CONG_AVOID) {
			window += (1.0 / window);
		} else if (phase == RenoPhase.FAST_RECOVERY) {
			window = ssthresh; // window deflation
		} else {
			throw new NetworkException("Window algorithm phase unrecognized");
		}
	}
	
	@Override
	// getter for the window size
	public int getW() {
		return (int) window;
	}
	
	// Only called from slow start
	private void checkPhase() {
		if (window > ssthresh) {
			phase = RenoPhase.CONG_AVOID;
		}
	}
	
	private void printStuff() {
		if (phase == RenoPhase.SLOW_START) {
			System.out.println("RTT. Phase: SLOW START.");
		} else if (phase == RenoPhase.CONG_AVOID) {
			System.out.println("RTT. Phase: CONG AVOID.");
		} else if (phase == RenoPhase.FAST_RECOVERY) {
			System.out.println("RTT. Phase: FAST RECOVERY.");
		}
		System.out.println("w: " + getW() + " ssthresh: " + ssthresh);
	}

}
