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
				setupCase(Integer.parseInt(args[0]), sim, args[1]);
			} catch (NumberFormatException e) {
				System.err.println("First arg must be an integer!");
				return;
			}
		} else
			// Congestion algs to choose from: Static, Simple, Exponential, TCPTahoe, TCPReno
			setupCase(0, sim, "TCPTahoe");
		// run the simulation
		sim.run();

	}

	public static void setupCase(int n, NetworkSimulator sim, String alg) {
		switch (n) {
		case 0:
			setupCase0(sim, alg);
			break;
		case 1:
			setupCase1(sim, alg);
			break;
		case 2:
			setupCase2(sim, alg);
			break;
		case 3:
			setupCase3(sim, alg);
			break;
		}
	}

	public static void setupCase0(NetworkSimulator sim, String alg) {
		// create link
		Link l = new Link("Link1", 10000000, 10, 64000); // 3000
		sim.addComponent(l);

		// Add source
		Host source = new Host("Host1", l, 1000);
		source.setIP(1);
		Flow f = new Flow(1, 2, "Flow1", 20, 1000, alg);
		source.addFlow(f);
		sim.addComponent(f);
		sim.addComponent(source);

		// Add sink
		Host sink = new Host("Host2", l, 2000);
		sink.setIP(2);
		sim.addComponent(sink);
	}

	public static void setupCase1(NetworkSimulator sim, String alg) {
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
		Flow f2 = new Flow(1, 2, "Flow1", 20, 5000, alg);
		source2.addFlow(f2);
		sim.addComponent(f2);
		sim.addComponent(source2);

		// Add sink
		Host sink2 = new Host("Host2", l5, 2000);
		sink2.setIP(2);
		sim.addComponent(sink2);

		// Add routers
		Router r1 = new Router("Router 1");
		r1.setIP(3);
		r1.addLink(l0);
		r1.addLink(l1);
		r1.addLink(l2);
		sim.addComponent(r1);

		Router r2 = new Router("Router 2");
		r2.setIP(4);
		r2.addLink(l1);
		r2.addLink(l3);
		sim.addComponent(r2);

		Router r3 = new Router("Router 3");
		r3.setIP(5);
		r3.addLink(l2);
		r3.addLink(l4);
		sim.addComponent(r3);

		Router r4 = new Router("Router 4");
		r4.setIP(6);
		r4.addLink(l5);
		r4.addLink(l3);
		r4.addLink(l4);
		sim.addComponent(r4);
	}

	public static void setupCase2(NetworkSimulator sim, String alg) {

	}

	/*
	 * This is a simpler test for routing.
	 * Host - Router - Router - Host
	 */
	public static void setupCase3(NetworkSimulator sim, String alg) {
		// create link
		Link l1 = new Link("Link1", 10000000, 10, 64000); // 3000
		sim.addComponent(l1);
		Link l2 = new Link("Link2", 10000000, 10, 64000); // 3000
		sim.addComponent(l2);
		Link l3 = new Link("Link3", 10000000, 10, 64000); // 3000
		sim.addComponent(l3);

		// Add source
		Host source = new Host("Host1", l1, 1000);
		source.setIP(1);
		Flow f = new Flow(1, 2, "Flow1", 20, 1000, alg);
		source.addFlow(f);

		sim.addComponent(source);

		// Add sink
		Host sink = new Host("Host2", l2, 2000);
		sink.setIP(2);
		sim.addComponent(sink);

		Router r = new Router("Router4");
		r.setIP(4);
		r.addLink(l1);
		r.addLink(l3);

		sim.addComponent(r);
		
		Router r2 = new Router("Router5");
		r2.setIP(5);
		r2.addLink(l3);
		r2.addLink(l2);

		sim.addComponent(r2);
	}

}
