/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @author Francesco
 *
 * Every host and router has a network address.
 * This address uniquely identifies each node on the network.
 * 
 * Because hosts and routers implement this shared functionality,
 * we put these features in an interface class.
 */
public interface Addressable {
	
	/**
	 * Get the component's MAC adress
	 * @return
	 */
	public long getMACAddress();
	
	/**
	 * Get the components IP address
	 * @return An IP
	 */
	public long getIP();

}
