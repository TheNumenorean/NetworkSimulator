package edu.caltech.networksimulator;

public class TCPReno extends WindowAlgorithm{

	private enum Phase {
		SLOW_START,
		CONG_AVOID,
		FAST_RECOVERY;
	}
	
	private double window;
	private Phase phase;
	private int ssthresh;
	
	public TCPReno(String name) {
		super(name);
		window = 1.0;
		phase = Phase.SLOW_START;
		this.FR = true;
	}

	@Override
	public void newRTT() {
		if (phase == Phase.FAST_RECOVERY) {
			// exit FR/FR after a RTT
			phase = Phase.CONG_AVOID;
		} else if (phase == Phase.SLOW_START) {
			window *= 2.0;
			if (window >= ssthresh) {
				phase = Phase.CONG_AVOID;
			}
		} else if (phase == Phase.CONG_AVOID) {
			window++;
		} else {
			throw new NetworkException("Window algorithm phase unrecognized");
		}
	}

	@Override
	public void droppedPacket(boolean dupACK) {
		ssthresh = Math.max((int) (window/2.0), 2);
		if (dupACK) {
			// Enter Fast Response/Fast Recovery
			window = ssthresh + 3; // window inflation
		} else { // round trip timeout
			window = ssthresh;
		}
	}

	@Override
	public void ACKPacket(Packet p) {
		if (phase == Phase.SLOW_START) {
			window++;
			if (window >= ssthresh) {
				phase = Phase.CONG_AVOID;
			}
		} else if (phase == Phase.CONG_AVOID) {
			window += (1.0 / window);
		} else if (phase == Phase.FAST_RECOVERY) {
			window = ssthresh;
		} else {
			throw new NetworkException("Window algorithm phase unrecognized");
		}
	}
	
	@Override
	// getter for the window size
	public int getW() {
		return (int) window;
	}

}
