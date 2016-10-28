/**
 * 
 */
package edu.caltech.networksimulator;

import java.util.ArrayList;

/**
 * @author Francesco
 *
 */
public class NetworkSimulator implements Runnable {

	private ArrayList<NetworkComponent> networkComponents;
	
	private boolean forceStop;

	/**
	 * 
	 */
	public NetworkSimulator() {
		networkComponents = new ArrayList<NetworkComponent>();
		forceStop = false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		NetworkSimulator sim = new NetworkSimulator();

		Link l = new Link("Link1", 1000, 10);
		sim.addComponent(l);
		sim.addComponent(new Host("Host1", l, 1000));
		sim.addComponent(new Host("Host2", l, 2000));

		sim.run();

	}

	public void addComponent(NetworkComponent comp) {
		networkComponents.add(comp);
	}

	@Override
	public void run() {

		for (NetworkComponent n : networkComponents) {
			new Thread(n).start();
		}

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
			
			if(isFinished)
				break;
		}
		
		for(NetworkComponent n : networkComponents)
			n.stop();
	}

}
