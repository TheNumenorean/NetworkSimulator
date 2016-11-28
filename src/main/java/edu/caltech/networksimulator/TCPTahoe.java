package edu.caltech.networksimulator;

public class TCPTahoe extends WindowAlgorithm{

	private enum Phase {
		SLOW_START,
		CONG_AVOID;
	}
	
	private double window;
	
	private Phase phase;
	private int ssthresh;
	
	public TCPTahoe(String name) {
		super(name);
		window = 1.0;
		phase = Phase.SLOW_START;
		this.FR = true;
	}

	@Override
	public void droppedPacket(boolean dupACK) {
		ssthresh = Math.max((int) (window/2.0), 2);
		window = 1;
	}

	@Override
	public void ACKPacket(Packet p) {
		if (phase == Phase.SLOW_START) {
			window++;
			checkPhase();
		} else if (phase == Phase.CONG_AVOID) {
			window += (1.0 / window);
		} else {
			throw new NetworkException("Window algorithm phase unrecognized");
		}
	}

	@Override
	public void newRTT() {
		if (phase == Phase.SLOW_START) {
			window *= 2.0;
			checkPhase();
		} else if (phase == Phase.CONG_AVOID) {
			window++;
		} else {
			throw new NetworkException("Window algorithm phase unrecognized");
		}
	}
	
	private void checkPhase() {
		if (window >= ssthresh) {
			phase = Phase.CONG_AVOID;
		}
	}
	
	@Override
	// getter for the window size
	public int getW() {
		return (int) window;
	}
}
