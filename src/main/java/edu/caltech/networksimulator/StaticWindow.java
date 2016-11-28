package edu.caltech.networksimulator;

public class StaticWindow extends WindowAlgorithm{

	public StaticWindow(String name, int size) {
		super(name);
		this.window = size;
	}

	@Override
	public void droppedPacket() {
		// never changes window size
		return;
	}

	@Override
	public void ACKPacket(Packet p) {
		// never changes window size
		return;
	}

}
