/**
 * 
 */
package edu.caltech.networksimulator.datacapture;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;

import edu.caltech.networksimulator.NetworkComponent;


public class GraphicalCaptureTool extends JFrame implements DataCaptureTool, ActionListener {

	private TreeMap<String, NetworkComponentContainer> components;
	private int height, width;
	private TreeMap<String, Boolean> binaryTracker;
	
	private Timer timer;

	/**
	 * 
	 */
	public GraphicalCaptureTool() {
		components = new TreeMap<String, NetworkComponentContainer>();
		binaryTracker = new TreeMap<String, Boolean>();
		timer = new Timer(50, this);

		this.setSize(700, 700);

		height = 150;
		width = 600;


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
		addData(n, dataName, time, (double)value);
	}

	@Override
	public void addData(NetworkComponent n, String dataName, long time, boolean value) {
		Boolean b = binaryTracker.get(n.getComponentName() + "." + dataName);
		if(b == null)
			binaryTracker.put(n.getComponentName() + "." + dataName, value);
		else if(b != value){
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
			list = new NetworkComponentContainer();
			components.put(n.getComponentName(), list);
			this.getContentPane().add(list);
		}

		return list;
	}

	private class NetworkComponentContainer extends JComponent {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2721174287672393834L;
		TreeMap<String, DataLine> data;
		private Queue<Color> colors;

		public NetworkComponentContainer() {
			data = new TreeMap<String, DataLine>();

			this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			this.setBackground(Color.GRAY);
			this.setSize(width, height+3);

			colors = new LinkedList<Color>();

			colors.add(Color.BLUE);
			colors.add(Color.CYAN);
			colors.add(Color.DARK_GRAY);
			colors.add(Color.GRAY);
			colors.add(Color.GREEN);
			colors.add(Color.LIGHT_GRAY);
			colors.add(Color.MAGENTA);
			colors.add(Color.ORANGE);
			colors.add(Color.PINK);
			colors.add(Color.RED);
			colors.add(Color.WHITE);
			colors.add(Color.YELLOW);
			colors.add(Color.BLACK);

		}

		public void addValue(String dataName, long time, double value) {

			DataLine data = getDataLine(dataName);
			if (data == null) {
				data = new DataLine(colors.poll());
				addDataLine(dataName, data);
			}

			data.addValue(time, value);

		}

		public DataLine getDataLine(String name) {
			return data.get(name);
		}

		public void addDataLine(String name, DataLine sd) {
			data.put(name, sd);
			sd.setDimensions(width, height);
			this.invalidate();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			for (DataLine sd : data.values())
				sd.paint(g);
		}

	}

	/**
	 * Represents a line on a plot
	 * @author Francesco
	 *
	 */
	private class DataLine {

		private ConcurrentLinkedQueue<Entry<Long, Double>> values;
		private int height;
		private int width;
		private Color c;
		private double maxValue;
		private int timeRange;
		private long lastTime;
		private long lastUpdate;

		/**
		 * Creates a new data line with the given color
		 * @param color
		 */
		public DataLine(Color color) {
			values = new ConcurrentLinkedQueue<Entry<Long, Double>>();
			c = color;
			timeRange = 10000;
		}
		
		public void setMaxValue(double maxValue) {
			this.maxValue = maxValue;
		}

		public void setDimensions(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public void setColor(Color c) {
			this.c = c;
		}

		public void addValue(long time, double value) {
			values.add(new AbstractMap.SimpleEntry<Long, Double>(time, value));
			lastTime = time;
			lastUpdate = System.currentTimeMillis();
			while(values.peek().getKey() - time > timeRange)
				values.remove();
			if (value > maxValue)
				maxValue = value;
		}

		public void paint(Graphics g) {

			g.setColor(c);
			int oldX = -1, lastHeight = -1;

			double scalar = height / maxValue;
			
			long timeSince = System.currentTimeMillis() - lastUpdate;
			
			Iterator<Entry<Long, Double>> it = values.iterator();
			while (it.hasNext()) {
				
				Entry<Long, Double> next = it.next();
				int newHeight = (int) ((Double) next.getValue() * scalar);
				
				int x = (int) (width * (((double)(timeSince + lastTime - next.getKey())) / timeRange));
				
				if(oldX == -1)
					oldX = x;
				
				if(lastHeight == -1)
					lastHeight = newHeight;
				
				g.drawLine(width - oldX, height - lastHeight + 1, width - x, height - newHeight + 1);

				oldX = x;
				lastHeight = newHeight;
			}
			
			// Paint a line to the current time
			if(timeSince > 0)
				g.drawLine(width - oldX, height - lastHeight + 1, width, height - lastHeight + 1);

		}

	}

}
