/**
 * 
 */
package edu.caltech.networksimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.caltech.networksimulator.datacapture.graphical.GraphicalCaptureTool;

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
	public NetworkSimulator(int testcase) {
		// TODO: Should take a description of a network we
		// specify and implement that
		
		networkComponents = new ArrayList<NetworkComponent>();
		forceStop = false;
		inputListener = new InputListener();
		dataCollector = new GraphicalCaptureTool();
		
		// for now: use the case specified to set up the network
		if (testcase == 0) {
			// create link
			Link l = new Link("Link1", 10000000, 1000, 3000);// 3000 // 64000);
			this.addComponent(l);
		
			// Add source
			Host source = new Host("Host1", l, 1000);
			source.setIP(1);
			Flow f = new Flow(1, 2, 1, 20, 1000);
			source.addFlow(f);
			this.addComponent(source);

			// Add sink
			Host sink = new Host("Host2", l, 2000);
			sink.setIP(2);
			this.addComponent(sink);
		} else if (testcase == 1) {
			// make links
			Link l0 = new Link("Link0", 12500000, 10, 64000);
			this.addComponent(l0);
			Link l1 = new Link("Link1", 10000000, 10, 64000);
			this.addComponent(l1);
			Link l2 = new Link("Link2", 10000000, 10, 64000);
			this.addComponent(l2);
			Link l3 = new Link("Link3", 10000000, 10, 64000);
			this.addComponent(l3);
			Link l4 = new Link("Link4", 10000000, 10, 64000);
			this.addComponent(l4);
			Link l5 = new Link("Link5", 12500000, 10, 64000);
			this.addComponent(l5);
			
			// Add source
			Host source2 = new Host("Host1", l0, 1000);
			source2.setIP(1);
			Flow f2 = new Flow(1, 2, 1, 20, 500);
			source2.addFlow(f2);
			this.addComponent(source2);
			
			// Add sink
			Host sink2 = new Host("Host2", l5, 2000);
			sink2.setIP(2);
			this.addComponent(sink2);
			
			// Add routers
			Router r1 = new Router("Router 1");
			r1.addRouting(l0, l1);
			r1.addRouting(l1, l0);
			r1.addRouting(l2, l0);
			this.addComponent(r1);
			
			Router r4 = new Router("Router 4");
			r4.addRouting(l3, l5);
			r4.addRouting(l4, l5);
			r4.addRouting(l5, l4);
			this.addComponent(r4);
			
			Router r2 = new Router("Router 2");
			r2.addRouting(l1, l3);
			r2.addRouting(l3, l1);
			this.addComponent(r2);
			
			Router r3 = new Router("Router 3");
			r3.addRouting(l2, l4);
			r3.addRouting(l4, l2);
			this.addComponent(r2);
		} else {
			System.out.println("ERROR: Test case not recognized.");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Set up case 0
		NetworkSimulator sim = new NetworkSimulator(0);
		// run the simulation
		sim.run();
		
		// Set up case 1
		// NetworkSimulator sim2 = new NetworkSimulator(1);
		// run case 1
		// sim2.run();
		
		
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
