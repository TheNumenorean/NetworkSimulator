/**
 * 
 */
package edu.caltech.networksimulator.datacapture.graphical;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;

import edu.caltech.networksimulator.NetworkComponent;
import edu.caltech.networksimulator.datacapture.DataCaptureTool;

public class GraphicalCaptureTool extends JFrame implements DataCaptureTool, ActionListener {

	public static int GRAPH_HEIGHT = 150;
	public static int GRAPH_WIDTH = 600;

	private TreeMap<String, NetworkComponentContainer> components;
	private TreeMap<String, Boolean> binaryTracker;

	private Timer timer;

	/**
	 * 
	 */
	public GraphicalCaptureTool() {
		components = new TreeMap<String, NetworkComponentContainer>();
		binaryTracker = new TreeMap<String, Boolean>();
		timer = new Timer(50, this);

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		this.setSize(700, 700);

	}

	public void start() {
		this.setVisible(true);
		timer.start();
	}

	/**
	 * 
	 */
	public void finish() {
		timer.stop();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	@Override
	public void addData(NetworkComponent n, String dataName, long time, int value) {
		addData(n, dataName, time, (double) value);
	}

	@Override
	public void addData(NetworkComponent n, String dataName, long time, boolean value) {
		Boolean b = binaryTracker.get(n.getComponentName() + "." + dataName);
		if (b == null)
			binaryTracker.put(n.getComponentName() + "." + dataName, value);
		else if (b != value) {
			addData(n, dataName, time, value ? 0.0 : 1.0);
			binaryTracker.put(n.getComponentName() + "." + dataName, value);
		}
		addData(n, dataName, time, value ? 1.0 : 0.0);
	}

	@Override
	public void addData(NetworkComponent n, String dataName, long time, long value) {
		addData(n, dataName, time, (double) value);
	}

	@Override
	public void addData(NetworkComponent n, String dataName, long time, double value) {
		NetworkComponentContainer list = getComponentContainer(n);
		list.addValue(dataName, time, value);
	}

	private NetworkComponentContainer getComponentContainer(NetworkComponent n) {
		NetworkComponentContainer list = components.get(n.getComponentName());
		if (list == null) {
			list = new NetworkComponentContainer(n.getComponentName());
			components.put(n.getComponentName(), list);
			getContentPane().add(list);
			
			// Create spacing between components
			getContentPane().add(Box.createRigidArea(new Dimension(5,5)));
			
			validate();
		}

		return list;
	}

	private class NetworkComponentContainer extends JComponent {

		public Graph g;
		public Legend legend;
		
		public NetworkComponentContainer(String name) {

			this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			this.setBackground(Color.GREEN);
			//this.setSize(GRAPH_WIDTH + 100, GRAPH_HEIGHT + 100);

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			add(new JLabel(name));


			legend = new Legend();
			legend.setMaximumSize(new Dimension(GRAPH_WIDTH, 100));
			
			g = new Graph(legend);
			g.setMaximumSize(new Dimension(GRAPH_WIDTH, GRAPH_HEIGHT));
			g.setMinimumSize(new Dimension(GRAPH_WIDTH, GRAPH_HEIGHT));
			this.add(g);
			
			this.add(legend);
		}

		@Override
		public Insets getInsets() {
			return new Insets(5, 5, 5, 50);
		}

		public void addValue(String dataName, long time, double value) {
			g.addValue(dataName, time, value);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
		}

	}

	@Override
	public void setMax(NetworkComponent n, String dataName, int value) {
		setMax(n, dataName, (long)value);
	}

	@Override
	public void setMax(NetworkComponent n, String dataName, long value) {
		setMax(n, dataName, (double)value);
	}

	@Override
	public void setMax(NetworkComponent n, String dataName, double value) {
		getComponentContainer(n).g.setMaxValue(dataName, value);
	}

}
