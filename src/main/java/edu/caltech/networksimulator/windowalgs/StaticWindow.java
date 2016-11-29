package edu.caltech.networksimulator.windowalgs;

import edu.caltech.networksimulator.Packet;

public class StaticWindow extends WindowAlgorithm{

	public StaticWindow(String name, int size) {
		super(name);
		this.window = size;
	}

	@Override
	public void droppedPacket(boolean dupACK) {
		// never changes window size
		return;
	}

	@Override
	public void ACKPacket(Packet p) {
		// never changes window size
		return;
	}

	@Override
	public void newRTT() {
		return;
	}

}
