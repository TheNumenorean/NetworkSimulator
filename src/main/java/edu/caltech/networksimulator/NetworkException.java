/**
 * 
 */
package edu.caltech.networksimulator;

/**
 * @author Francesco
 * 
 * An exception class to represent an error that occurs in a simulated network
 *
 */
public class NetworkException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4398315253844604064L;

	/**
	 * 
	 */
	public NetworkException() {
		this("Unknown network exception!");
	}

	/**
	 * @param arg0
	 */
	public NetworkException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public NetworkException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NetworkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
