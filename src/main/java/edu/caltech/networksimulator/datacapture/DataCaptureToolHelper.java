package edu.caltech.networksimulator.datacapture;

import java.awt.EventQueue;
import java.util.List;

import edu.caltech.networksimulator.NetworkComponent;

public class DataCaptureToolHelper {

	/**
	 * Sends data to all the given data capture tools asynchronously. Returns immediately.
	 * @param dcts DataCaptureTools to add data to
	 * @param s Sensor sending the data
	 * @param dataName Name of the data
	 * @param time Timestamp of the data
	 * @param value Value of the data
	 */
	public static void addData(final List<DataCaptureTool> dcts,
			final NetworkComponent n, final String dataName, final long time,
			final int value) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				for (DataCaptureTool dct : dcts) {
					dct.addData(n, dataName, time, value);
				}
			}
		});
	}
	
	/**
	 * Sends data to all the given data capture tools asynchronously. Returns immediately.
	 * @param dcts DataCaptureTools to add data to
	 * @param s Sensor sending the data
	 * @param dataName Name of the data
	 * @param time Timestamp of the data
	 * @param value Value of the data
	 */
	public static void addData(final List<DataCaptureTool> dcts, final NetworkComponent n,
			final String dataName, final long time, final boolean value) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				for (DataCaptureTool dct : dcts) {
					dct.addData(n, dataName, time, value);
				}
			}
		});
	}

	/**
	 * Sends data to all the given data capture tools asynchronously. Returns immediately.
	 * @param dcts DataCaptureTools to add data to
	 * @param s Sensor sending the data
	 * @param dataName Name of the data
	 * @param time Timestamp of the data
	 * @param value Value of the data
	 */
	public static void addData(final List<DataCaptureTool> dcts, final NetworkComponent n,
			final String dataName, final long time, final long value) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				for (DataCaptureTool dct : dcts) {
					dct.addData(n, dataName, time, value);
				}
			}
		});
	}

	/**
	 * Sends data to all the given data capture tools asynchronously. Returns immediately.
	 * @param dcts DataCaptureTools to add data to
	 * @param s Sensor sending the data
	 * @param dataName Name of the data
	 * @param time Timestamp of the data
	 * @param value Value of the data
	 */
	public static void addData(final List<DataCaptureTool> dcts, final NetworkComponent n,
			final String dataName, final long time, final double value) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				for (DataCaptureTool dct : dcts) {
					dct.addData(n, dataName, time, value);
				}
			}
		});
	}

}
