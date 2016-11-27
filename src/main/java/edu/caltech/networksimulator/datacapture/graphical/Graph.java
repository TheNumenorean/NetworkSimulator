/**
 * 
 */
package edu.caltech.networksimulator.datacapture.graphical;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;

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
	private long dataRange;
	
	/**
	 * @param labels 
	 * 
	 */
	public Graph(Legend legend, long dataRange) {
		this.dataRange = dataRange;
		
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
			line = new DataLine(colors.poll(), dataRange);
			data.put(name, line);
			legend.addLabel(line.c, name);

			invalidate();
		}
		
		return line;
	}
	

	public void setDataRange(long dataRange) {
		this.dataRange = dataRange;
		for(Entry<String, DataLine> dat : data.entrySet())
			dat.getValue().timeRange = dataRange;
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

		private Map<Long, Double> values;
		
		public Color c;
		public long timeRange;

		private double maxValue;

		/**
		 * Creates a new data line with the given color
		 * @param color
		 */
		public DataLine(Color color, long timeRange) {
			
			// Store values in reverse order
			values = new TreeMap<Long, Double>(new Comparator<Long>(){

				@Override
				public int compare(Long arg0, Long arg1) {
					return (int) (arg1 - arg0);
				}
				
			});
			c = color;
			this.timeRange = timeRange;
		}
		
		public void setMaxValue(double maxValue) {
			this.maxValue = maxValue;
		}

		public void addValue(long time, double value) {
			values.put(time, value);
			if (value > maxValue)
				maxValue = value;
		}

		/*
		 *  Paints this dataline from most recent to least recent to the encapsulating Graph
		 */
		public void paint(Graphics g) {

			g.setColor(c);
			
			// Let oldx be the end of the graph area, so that the irst line come out of the end of the graph
			int oldX = Graph.this.getWidth(), lastHeight = -1;

			//Adding -2 accounts for border
			double scalar = (Graph.this.getHeight() - 2) / maxValue;
			
			for(Entry<Long, Double> dat : values.entrySet()) {
				
				
				int newHeight = (int) (Graph.this.getHeight() - 2 - dat.getValue() * scalar);
				
				int x = (int) (Graph.this.getWidth() - Graph.this.getWidth() * ((double)(System.currentTimeMillis() - dat.getKey()) / timeRange));
				
				if(lastHeight == -1)
					lastHeight = newHeight;
				
				g.drawLine(oldX, lastHeight + 1, x, newHeight + 1);

				oldX = x;
				lastHeight = newHeight;
				
				// Let the last one paint so that the graph continues to the end
				if(dat.getKey() < System.currentTimeMillis() - timeRange)
					break;
			}
		}

	}



}
