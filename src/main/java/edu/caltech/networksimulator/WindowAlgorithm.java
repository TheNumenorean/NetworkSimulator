package edu.caltech.networksimulator;

import java.util.ArrayList;

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
	
	private String name;
	protected int window;
	
	public WindowAlgorithm(String name){
		this.name = name;
		this.window = window;
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
	
	// Notified of a dropped packet
	public abstract void droppedPacket();
	
	// Notified of an ack packet
	public abstract void ACKPacket(Packet p);
}