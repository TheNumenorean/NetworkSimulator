package edu.caltech.networksimulator.windowalgs;

import edu.caltech.networksimulator.Packet;


/**
 * Hi all,
 * For FAST, please use the formula in the FAST TCP paper.
 * For gamma, start with  0.5 and see if it works well.
 * If it does, just use that; otherwise adjust gamma, e.g., by reducing it a bit.
 * It depends on how they implement their simulations, either any gamma in [0, 1]
 * should work or a small gamma might work better (converges more smoothly but more slowly).
 * 
 * For alpha, please start with 15 and try to increase it a bit if it doesn’t work well.
 */
public class TCPFAST extends WindowAlgorithm {

	private final double alpha;
	private final double gamma;
	private double window;
	private double baseRTT;
	private double RTT;
	
	public TCPFAST(String name) {
		super(name);
		alpha = 5.0;
		gamma = 0.2;
		window = 1.0;
		baseRTT = 5000;
		RTT = 5000;
		this.FR = true;
	}

	@Override
	public void newRTT() {
		// printStuff();
		double term1 = 2.0 * window;
		double term2 = ((1.0 - gamma) * window) + (gamma * ((baseRTT / RTT) * window + alpha));
		// System.out.println("terms: " + term1 + " " + term2);
		window = Math.min(term1, term2);
	}

	@Override
	public void droppedPacket(boolean dupACK) {
		// ignore
	}

	@Override
	public void ACKPacket(Packet p) {
		RTT = (double) System.currentTimeMillis() - p.getSentTime();
		if (RTT > 0) {
			baseRTT = Math.min(baseRTT, RTT);
			// use round trip time of last packet
		}
	}
	
	@Override
	public int getW(){
		return (int) Math.max(window, 1);
	}
	
	private void printStuff() {
		// System.out.println("w: " + window + " baseRTT: " + baseRTT + " RTT: " + RTT);
	}

}
