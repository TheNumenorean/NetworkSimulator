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
	
	void addDataDollector(DataCaptureTool dct) {
		dataCollectors.add(dct);
	}
	
	List<DataCaptureTool> getDataCollectors() {
		return dataCollectors;
	}
	
	@Override
	public int compareTo(NetworkComponent n) {
		return n.name.compareTo(name);
	}

}
