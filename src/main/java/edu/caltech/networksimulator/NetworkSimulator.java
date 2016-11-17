/**
 * 
 */
package edu.caltech.networksimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.caltech.networksimulator.datacapture.GraphicalCaptureTool;

/**
 * @author Francesco
 *
 */
public class NetworkSimulator implements Runnable {

	private ArrayList<NetworkComponent> networkComponents;

	private boolean forceStop;

	private InputListener inputListener;

	private GraphicalCaptureTool dataCollector;

	/**
	 * 
	 */
	public NetworkSimulator() {
		// TODO: Should take a description of a network we
		// specify and implement that
		networkComponents = new ArrayList<NetworkComponent>();
		forceStop = false;

		inputListener = new InputListener();
		
		dataCollector = new GraphicalCaptureTool();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		NetworkSimulator sim = new NetworkSimulator();

		Link l = new Link("Link1", 10000000, 1000, 3000);// 64000);

		sim.addComponent(l);
		Host source = new Host("Host1", l, 1000);
		source.setIP(1);
		Flow f = new Flow(1, 2, 1, 20, 1000);
		source.addFlow(f);
		sim.addComponent(source);

		Host sink = new Host("Host2", l, 2000);
		sink.setIP(2);
		sim.addComponent(sink);

		
		// run the simulation
		sim.run();
		
		// TODO: record data for user-specified simulation variables
		// at regular intervals.
		
		// TODO: Output graphs after each run showing
		// the progression of the specified variables over time.
		/*
		 * Per-link buffer occupancy, packet loss, and flow rate.
		 * Per-flow send/receive rate and packet round-trip delay.
		 * Per-host send/receive rate.
		 */
	}

	/**
	 * Add a network component to this simulation
	 * 
	 * @param comp
	 *            The component to add
	 */
	public void addComponent(NetworkComponent comp) {
		comp.addDataDollector(dataCollector);
		networkComponents.add(comp);
	}

	@Override
	public void run() {
		
		dataCollector.start();

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
		
		dataCollector.finish();

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
