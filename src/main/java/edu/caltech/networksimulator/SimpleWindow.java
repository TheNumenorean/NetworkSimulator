package edu.caltech.networksimulator;

public class SimpleWindow extends WindowAlgorithm {
	
	public SimpleWindow(String name) {
		super(name);
	}
	
//	@Override
//	public int getW() {
//		return window
//		// Ensure the window is always at least one
//		this.window = Math.max(window - 1, 1);
//	}

	@Override
	public void droppedPacket() {
		// Ensure the window is always at least one
		this.window = Math.max(window - 1, 1);
	}

	@Override
	public void ACKPacket(Packet p) {
		// Simply increment by one.
		this.window = window + 1;	
	}

}
