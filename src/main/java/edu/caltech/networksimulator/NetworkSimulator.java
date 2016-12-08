/**
 * 
 */
package edu.caltech.networksimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.caltech.networksimulator.datacapture.DataCaptureTool;

/**
 * @author Francesco, Carly
 *
 */
public class NetworkSimulator implements Runnable {
	
	public static boolean PRINT_ROUTING = false;
	
	// Print packets at each destination?
	public static boolean PRINT_LINK_PACKETS = false;
	public static boolean PRINT_ROUTER_PACKETS = false;
	public static boolean PRINT_HOST_PACKETS = false;
	
	public static boolean PRINT_FLOW_STUFF = false;

	private ArrayList<NetworkComponent> networkComponents;

	private boolean forceStop;

	private InputListener inputListener;

	private List<DataCaptureTool> dataCollectors;

	/**
	 * 
	 */
	public NetworkSimulator() {
		
		networkComponents = new ArrayList<NetworkComponent>();
		forceStop = false;
		inputListener = new InputListener();
		
		dataCollectors = new ArrayList<DataCaptureTool>();
		
	}
	
	public void addDataCollector(DataCaptureTool dct) {
		if(!networkComponents.isEmpty())
			throw new IllegalArgumentException("Cannot add data capture tools after network components!");
		dataCollectors.add(dct);
	}

	/**
	 * Add a network component to this simulation
	 * 
	 * @param comp
	 *            The component to add
	 */
	public void addComponent(NetworkComponent comp) {
		for(DataCaptureTool dct : dataCollectors)
			comp.addDataDollector(dct);
		networkComponents.add(comp);
	}

	@Override
	public void run() {
		
		for(DataCaptureTool dct : dataCollectors)
			dct.start();

		for (NetworkComponent n : networkComponents) {
			new Thread(n).start();
		}

		new Thread(inputListener).start();

		while (!forceStop) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			boolean isFinished = true;
			for (NetworkComponent n : networkComponents) {
				if (!n.finished())
					isFinished = false;
			}

			if (isFinished)
				break;
		}

		inputListener.stop();

		for (NetworkComponent n : networkComponents)
			n.stop();
		
		for(DataCaptureTool dct : dataCollectors)
			dct.finish();

		// Complete calculations, get data and print it, etc.
	}

	/**
	 * Stops the simulator from running, and safely finishes running
	 */
	public void stop() {
		forceStop = true;
	}

	/**
	 * 
	 * @author Francesco
	 *
	 */
	private class InputListener implements Runnable {

		private boolean stop;
		
		public InputListener() {
			stop = false;
		}

		@Override
		public void run() {

			try {

				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String com = "";
				while (!stop) {
					

					if (reader.ready()) {
						char c = (char) reader.read();
						if (c == '\n') {

							interpretCommand(com.trim());
							com = "";

						} else
							com = com + c;
					} else {
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
					}

				}

				reader.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void stop() {
			stop = true;
		}

		private void interpretCommand(String input) {
			if (input.equalsIgnoreCase("stop"))
				NetworkSimulator.this.stop();
			else if(input.equalsIgnoreCase("ping"))
				System.out.println("PONG");
		}
	}

}
