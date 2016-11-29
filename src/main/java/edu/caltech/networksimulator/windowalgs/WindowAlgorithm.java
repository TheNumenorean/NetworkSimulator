package edu.caltech.networksimulator.windowalgs;

import java.util.ArrayList;

import edu.caltech.networksimulator.Packet;

/**
 * @author Carly
 *
 * Every flow has a window adjusting algorithm.
 * Each window adjusting algorithm should take some action upon a received packet,
 * and take a different action upon a dropped packet.
 * 
 * Detection of a dropped packet is left to the flow, but this interface keeps W.
 */

public abstract class WindowAlgorithm {
	
	private final String name;
	protected int window;
	public boolean FR; // whether the algorithm supports fast response/ fast retransmit on 3 dupACKs
	
	public WindowAlgorithm(String name){
		this.name = name;
		this.FR = false;
	}
	
	/**
	 * Gets the name of the window algorithm
	 * @return A String of the window algorithm's name
	 */
	public String getComponentName() {
		return name;
	}

	// getter for the window size
	public int getW() {
		return window;
	}
	
	// Every round trip time, do something
	public abstract void newRTT();
	
	// Notified of a dropped packet
	public abstract void droppedPacket(boolean dupACK);
	
	// Notified of an ack packet
	public abstract void ACKPacket(Packet p);
}