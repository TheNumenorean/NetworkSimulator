package edu.caltech.networksimulator;

/*
 * This class mimics the behavior of Congestion Avoidance
 */
public class SimpleWindow extends WindowAlgorithm {
	
	public SimpleWindow(String name) {
		super(name);
		this.window = 1;
	}

	@Override
	public void droppedPacket(boolean dupACK) {
		// Ensure the window is always at least one
		this.window = Math.max(window - 1, 1);
	}

	@Override
	public void ACKPacket(Packet p) {
		// Simply increment by one.
		this.window++;
	}

	@Override
	public void newRTT() {
		return;
	}

}
