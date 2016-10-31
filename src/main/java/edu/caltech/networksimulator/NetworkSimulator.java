/**
 * 
 */
package edu.caltech.networksimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Francesco
 *
 */
public class NetworkSimulator implements Runnable {

	private ArrayList<NetworkComponent> networkComponents;

	private boolean forceStop;

	private InputListener inputListener;

	/**
	 * 
	 */
	public NetworkSimulator() {
		networkComponents = new ArrayList<NetworkComponent>();
		forceStop = false;

		inputListener = new InputListener();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		NetworkSimulator sim = new NetworkSimulator();

		Link l = new Link("Link1", 10000000, 1000, 64000);

		sim.addComponent(l);
		Host source = new Host("Host1", l, 1000);
		source.setIP(1);
		Packet p = new Packet(1, 2);
		p.setMeta("Plaintext");
		p.setPayload("DOOM");
		source.addPacket(p);
		sim.addComponent(source);

		Host sink = new Host("Host2", l, 2000);
		sink.setIP(2);
		sim.addComponent(sink);

		sim.run();

	}

	/**
	 * Add a network component to this simulation
	 * 
	 * @param comp
	 *            The component to add
	 */
	public void addComponent(NetworkComponent comp) {
		networkComponents.add(comp);
	}

	@Override
	public void run() {

		for (NetworkComponent n : networkComponents) {
			new Thread(n).start();
		}

		new Thread(inputListener).start();

		while (!forceStop) {
			try {
				Thread.sleep(1000);
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
