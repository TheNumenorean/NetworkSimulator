/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.ArrayList;
import java.util.Scanner;

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

		Link l = new Link("Link1", 10000000, 10, 64000);

		sim.addComponent(l);
		Host source = new Host("Host1", l, 1000);
		source.setIP(1);
		Packet p = new Packet(32000, 1, 2);
		p.setMeta("Plaintext");
		p.setPayload("Give me all your chocolate or else");
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

		@Override
		public void run() {
			Scanner scan = new Scanner(System.in);
			
			while(!stop) {
				String input = scan.nextLine();
				
				if(input.equalsIgnoreCase("stop"))
					NetworkSimulator.this.stop();
			}
			
			scan.close();
			
		}
		
		public void stop() {
			stop = true;
		}
		
	}

}
