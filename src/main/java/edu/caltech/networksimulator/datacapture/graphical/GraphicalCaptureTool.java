/**
 * 
 */
package edu.caltech.networksimulator.datacapture.graphical;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.Border;

import edu.caltech.networksimulator.NetworkComponent;
import edu.caltech.networksimulator.datacapture.DataCaptureTool;

public class GraphicalCaptureTool extends JFrame implements DataCaptureTool, ActionListener, MouseWheelListener {

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

		this.setSize(800, 800);
		
		this.addMouseWheelListener(this);

	}

	@Override
	public void start() {
		this.setVisible(true);
		timer.start();
	}

	/**
	 * 
	 */
	@Override
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
	
	@Override
	public void setDataSmoothingRange(NetworkComponent n, String dataName, int smoothingRange) {
		getComponentContainer(n).g.setDataSmoothingRange(dataName, smoothingRange);
	}
	
	public void dontDisplayComponent(NetworkComponent n) {
		getContentPane().remove(getComponentContainer(n));
	}

	private synchronized NetworkComponentContainer getComponentContainer(NetworkComponent n) {
		NetworkComponentContainer list = components.get(n.getComponentName());
		if (list == null) {
			list = new NetworkComponentContainer(n.getComponentName());
			components.put(n.getComponentName(), list);
			getContentPane().add(list);
			
			validate();
		}

		return list;
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
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		for(Entry<String, NetworkComponentContainer> comp : components.entrySet())
			comp.getValue().zoom(e.getWheelRotation());
		this.repaint();
	}

	private class NetworkComponentContainer extends JComponent {

		private static final long DEFAULT_DATA_RANGE = 10000;
		private static final long DATA_STEP_SIZE = 500;
		
		private long dataRange;
		public Graph g;
		public Legend legend;
		public AxisLabel axisLabel;

		
		public NetworkComponentContainer(String name) {
			
			Border inside = BorderFactory.createLineBorder(Color.GRAY, 2);
			Border outside  = BorderFactory.createEmptyBorder(5, 5, 5, 5);

			this.setBorder(BorderFactory.createCompoundBorder(outside, inside));

			setLayout(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			
			dataRange = DEFAULT_DATA_RANGE;
			
			legend = new Legend();
			axisLabel = new AxisLabel();
			g = new Graph(dataRange, legend, axisLabel);
			

			c.gridx = 0;
			c.gridy = 1;
			add(Box.createRigidArea(new Dimension(10, 10)), c);
			
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			c.weighty = 0.1;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(new JLabel(name), c);
			
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			add(g, c);
			
			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = 1;
			c.weightx = 0.1;
			c.fill = GridBagConstraints.VERTICAL;
			add(axisLabel, c);
			
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 2;
			c.weighty = 0.1;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(legend, c);
			
			validate();
			
		}

		@Override
		public Insets getInsets() {
			return new Insets(5, 5, 5, 50);
		}

		public void addValue(String dataName, long time, double value) {
			g.addValue(dataName, time, value);
		}
		
		public void zoom(int steps) {
			dataRange += steps*DATA_STEP_SIZE;
			dataRange = Math.abs(dataRange);
			g.setDataRange(dataRange);
		}

	}

}
