/**
 * 
 */
package edu.caltech.networksimulator.datacapture;

import edu.caltech.networksimulator.NetworkComponent;

/**
 * @author Francesco Macagno
 * 
 */
public interface DataCaptureTool {

	// public void addData(Sensor s, String dataName, long time, Object value);
	/**
	 * Adds data to this DataCaptureTool. Should probably be called outside of
	 * main thread, since there is no guarantee that the DataCaptureTool will
	 * return quickly. Consider using DataCaptureToolHelper.
	 * 
	 * @param s Sensor sending the data
	 * @param dataName Name of the data
	 * @param time Timestamp of data point
	 * @param value Value of data point
	 */
	public void addData(NetworkComponent n, String dataName, long time, int value);

	/**
	 * Adds data to this DataCaptureTool. Should probably be called outside of
	 * main thread, since there is no guarantee that the DataCaptureTool will
	 * return quickly. Consider using DataCaptureToolHelper.
	 * 
	 * @param s Sensor sending the data
	 * @param dataName Name of the data
	 * @param time Timestamp of data point
	 * @param value Value of data point
	 */
	public void addData(NetworkComponent n, String dataName, long time, boolean value);

	/**
	 * Adds data to this DataCaptureTool. Should probably be called outside of
	 * main thread, since there is no guarantee that the DataCaptureTool will
	 * return quickly. Consider using DataCaptureToolHelper.
	 * 
	 * @param s Sensor sending the data
	 * @param dataName Name of the data
	 * @param time Timestamp of data point
	 * @param value Value of data point
	 */
	public void addData(NetworkComponent n, String dataName, long time, long value);

	/**
	 * Adds data to this DataCaptureTool. Should probably be called outside of
	 * main thread, since there is no guarantee that the DataCaptureTool will
	 * return quickly. Consider using DataCaptureToolHelper.
	 * 
	 * @param s Sensor sending the data
	 * @param dataName Name of the data
	 * @param time Timestamp of data point
	 * @param value Value of data point
	 */
	public void addData(NetworkComponent n, String dataName, long time, double value);

}
