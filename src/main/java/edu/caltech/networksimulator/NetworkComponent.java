/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @author Francesco
 *
 */
public abstract class NetworkComponent implements Runnable {
	
	private String name;

	public NetworkComponent(String name){
		this.name = name;
	}
	
	/**
	 * Gets the name of this network component
	 * @return A String of the component name
	 */
	public String getComponentName() {
		return name;
	}
	
	public abstract void offerPacket(Packet p);
	
	

}
