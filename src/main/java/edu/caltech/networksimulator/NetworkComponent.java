/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @authors Francesco, Carly
 *
 */
public abstract class NetworkComponent implements Runnable {
	
	private String name;
	
	private boolean stop;

	public NetworkComponent(String name){
		this.name = name;
		stop = false;
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

}
