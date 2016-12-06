/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.ArrayList;
import java.util.List;

import edu.caltech.networksimulator.datacapture.DataCaptureTool;

/**
 * @authors Francesco, Carly
 *
 */
public abstract class NetworkComponent implements Runnable, Comparable<NetworkComponent> {
	
	/**
	 * Used to differentiate between hosts and network components which switch between multiple components
	 * @author Francesco
	 *
	 */
	public static enum ComponentType {
		HOST, SWITCH;
	}
	
	private String name;
	private boolean stop;
	private List<DataCaptureTool> dataCollectors;

	public NetworkComponent(String name){
		this.name = name;
		stop = false;
		dataCollectors = new ArrayList<>();
	}
	
	/**
	 * Gets the name of this network component
	 * @return A String of the component name
	 */
	public String getComponentName() {
		return name;
	}
	
	/**
	 * Offer this NetworkComponent a packet, to do with as desired.
	 * 
	 * This method is not gueranteed to return immediately, but should return  as soon as possible in implementations.
	 * @param p The packet being offered
	 * @param n Who is offering
	 */
	public abstract void offerPacket(Packet p, NetworkComponent n);
	
	/** 
	 * Get whether this component has completed everything it wants to do.
	 * @return
	 */
	public abstract boolean finished();
	
	/**
	 * Cause this component to gracefully stop
	 */
	public void stop() {
		stop = true;
	}
	
	/** 
	 * Return whether this component has received the command to stop running
	 * @return
	 */
	public boolean receivedStop() {
		return stop;
	}
	
	/**
	 * Adds a data collector to this component 
	 * @param dct A non-null datacollector implementation
	 */
	public void addDataDollector(DataCaptureTool dct) {
		dataCollectors.add(dct);
	}
	
	/**
	 * Get a list of all the data collectors this component has
	 * @return
	 */
	public List<DataCaptureTool> getDataCollectors() {
		return dataCollectors;
	}
	
	@Override
	public int compareTo(NetworkComponent n) {
		return n.name.compareTo(name);
	}
	
	@Override
	public String toString() {
		return name;
	}

}
