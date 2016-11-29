package edu.caltech.networksimulator.windowalgs;

import edu.caltech.networksimulator.Packet;

/*
 * This class mimics the behavior of Slow Start
 */
public class ExponentialWindow extends WindowAlgorithm{
	
	public ExponentialWindow(String name) {
		super(name);
		this.window = 1;
	}

	@Override
	public void droppedPacket(boolean dupACK) {
		// Ensure the window is always at least one
		this.window = Math.max(window/2, 2);
	}

	@Override
	public void ACKPacket(Packet p) {
		// Simply increment by one.
		this.window++;
	}

	@Override
	public void newRTT() {
		this.window *= 2;
	}

}
