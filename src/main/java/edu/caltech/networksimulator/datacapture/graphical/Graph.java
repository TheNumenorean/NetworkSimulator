/**
 * 
 */
package edu.caltech.networksimulator.datacapture.graphical;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

/**
 * @author Francesco
 *
 */
public class Graph extends JComponent {


	TreeMap<String, DataLine> data;
	private Queue<Color> colors;
	private Legend legend;
	
	/**
	 * @param labels 
	 * 
	 */
	public Graph(Legend legend) {
		
		data = new TreeMap<String, DataLine>();
		
		this.legend = legend;

		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		colors = new LinkedList<Color>();

		colors.add(Color.GREEN);
		colors.add(Color.BLUE);
		colors.add(Color.RED);
		colors.add(Color.CYAN);
		colors.add(Color.DARK_GRAY);
		colors.add(Color.GRAY);
		colors.add(Color.LIGHT_GRAY);
		colors.add(Color.MAGENTA);
		colors.add(Color.ORANGE);
		colors.add(Color.PINK);
		colors.add(Color.WHITE);
		colors.add(Color.YELLOW);
		colors.add(Color.BLACK);

	}
	
	public void setMaxValue(String dataName, double value) {
		getDataLine(dataName).setMaxValue(value);
	}

	public void addValue(String dataName, long time, double value) {
		getDataLine(dataName).addValue(time, value);
	}

	public DataLine getDataLine(String name) {
		DataLine line = data.get(name);
		if (line == null) {
			line = new DataLine(colors.poll());
			data.put(name, line);
			legend.addLabel(line.c, name);

			invalidate();
		}
		
		return line;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (DataLine sd : data.values())
			sd.paint(g);
	}
	

	/**
	 * Represents a line on a plot
	 * @author Francesco
	 *
	 */
	private class DataLine {

		private ConcurrentLinkedQueue<Entry<Long, Double>> values;
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

			double scalar = Graph.this.getHeight() / maxValue;
			
			long timeSince = System.currentTimeMillis() - lastUpdate;
			
			Iterator<Entry<Long, Double>> it = values.iterator();
			while (it.hasNext()) {
				
				Entry<Long, Double> next = it.next();
				int newHeight = (int) (next.getValue() * scalar);
				
				int x = (int) (Graph.this.getWidth() * (((double)(timeSince + lastTime - next.getKey())) / timeRange));
				
				if(oldX == -1)
					oldX = x;
				
				if(lastHeight == -1)
					lastHeight = newHeight;
				
				g.drawLine(Graph.this.getWidth() - oldX, Graph.this.getHeight() - lastHeight + 1, Graph.this.getWidth() - x, Graph.this.getHeight() - newHeight + 1);

				oldX = x;
				lastHeight = newHeight;
			}
			
			// Paint a line to the current time
			if(timeSince > 0)
				g.drawLine(Graph.this.getWidth() - oldX, Graph.this.getHeight() - lastHeight + 1, Graph.this.getWidth(), Graph.this.getHeight() - lastHeight + 1);

		}

	}



}
