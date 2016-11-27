/**
 * 
 */
package edu.caltech.networksimulator;

import edu.caltech.networksimulator.datacapture.graphical.GraphicalCaptureTool;

/**
 * @author Francesco
 *
 */
public class SimulationRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Set up case 0
		NetworkSimulator sim = new NetworkSimulator();
		
		sim.addDataCollector(new GraphicalCaptureTool());

		if (args.length > 0) {
			try {
				setupCase(Integer.parseInt(args[0]), sim);
			} catch (NumberFormatException e) {
				System.err.println("First arg must be an integer!");
				return;
			}
		} else
			setupCase(0, sim);
		// run the simulation
		sim.run();

	}

	public static void setupCase(int n, NetworkSimulator sim) {
		switch (n) {
		case 0:
			setupCase0(sim);
			break;
		case 1:
			setupCase1(sim);
			break;
		case 2:
			setupCase2(sim);
			break;
		case 3:
			setupCase3(sim);
			break;
		}
	}

	public static void setupCase0(NetworkSimulator sim) {
		// create link
		Link l = new Link("Link1", 10000000, 10, 64000); // 3000
		sim.addComponent(l);

		// Add source
		Host source = new Host("Host1", l, 1000);
		source.setIP(1);
		Flow f = new Flow(1, 2, 1, 20, 1000);
		source.addFlow(f);
		sim.addComponent(source);

		// Add sink
		Host sink = new Host("Host2", l, 2000);
		sink.setIP(2);
		sim.addComponent(sink);
	}

	public static void setupCase1(NetworkSimulator sim) {
		// make links
		Link l0 = new Link("Link0", 12500000, 10, 64000);
		sim.addComponent(l0);
		Link l1 = new Link("Link1", 10000000, 10, 64000);
		sim.addComponent(l1);
		Link l2 = new Link("Link2", 10000000, 10, 64000);
		sim.addComponent(l2);
		Link l3 = new Link("Link3", 10000000, 10, 64000);
		sim.addComponent(l3);
		Link l4 = new Link("Link4", 10000000, 10, 64000);
		sim.addComponent(l4);
		Link l5 = new Link("Link5", 12500000, 10, 64000);
		sim.addComponent(l5);

		// Add source
		Host source2 = new Host("Host1", l0, 1000);
		source2.setIP(1);
		Flow f2 = new Flow(1, 2, 1, 20, 500);
		source2.addFlow(f2);
		sim.addComponent(source2);

		// Add sink
		Host sink2 = new Host("Host2", l5, 2000);
		sink2.setIP(2);
		sim.addComponent(sink2);

		// Add routers
		Router r1 = new Router("Router 1");
		r1.addRouting(l0, l1);
		r1.addRouting(l1, l0);
		r1.addRouting(l2, l0);
		sim.addComponent(r1);

		Router r4 = new Router("Router 4");
		r4.addRouting(l3, l5);
		r4.addRouting(l4, l5);
		r4.addRouting(l5, l4);
		sim.addComponent(r4);

		Router r2 = new Router("Router 2");
		r2.addRouting(l1, l3);
		r2.addRouting(l3, l1);
		sim.addComponent(r2);

		Router r3 = new Router("Router 3");
		r3.addRouting(l2, l4);
		r3.addRouting(l4, l2);
		sim.addComponent(r2);
	}

	public static void setupCase2(NetworkSimulator sim) {

	}

	public static void setupCase3(NetworkSimulator sim) {

	}

}
