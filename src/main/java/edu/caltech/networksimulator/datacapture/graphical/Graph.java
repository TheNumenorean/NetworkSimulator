/**
 * 
 */
package edu.caltech.networksimulator.datacapture.graphical;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
	private int smoothingRange;
	private AxisLabel axisLabel;

	/**
	 * @param labels
	 * 
	 */
	public Graph(long dataRange, Legend legend, AxisLabel axisLabel) {
		
		this.dataRange = dataRange;
		this.legend = legend;
		this.axisLabel = axisLabel;

		data = new TreeMap<String, DataLine>();

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
		getDataLine(dataName).maxValue = value;
	}

	public void addValue(String dataName, long time, double value) {
		getDataLine(dataName).addValue(time, value);
	}

	public void setDataRange(long dataRange) {
		this.dataRange = dataRange;
		for (Entry<String, DataLine> dat : data.entrySet())
			dat.getValue().timeRange = dataRange;
	}

	public void setLineColor(String dataName, Color c) {
		getDataLine(dataName).c = c;
	}

	public void setDataSmoothingRange(String dataName, int smoothingRange) {
		getDataLine(dataName).smoothingRange = smoothingRange;
	}

	private DataLine getDataLine(String name) {
		DataLine line = data.get(name);
		if (line == null) {
			line = new DataLine(colors.poll(), dataRange);
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
	 * 
	 * @author Francesco
	 *
	 */
	private class DataLine {

		private Map<Long, Double> values;
		
		private List<Double> smoothing;

		public int smoothingRange;
		public Color c;
		public long timeRange;

		private double maxValue;

		/**
		 * Creates a new data line with the given color
		 * 
		 * @param color
		 */
		public DataLine(Color color, long timeRange) {

			smoothing = new ArrayList<Double>();
			
			// Store values in reverse order
			values = new TreeMap<Long, Double>(new Comparator<Long>() {

				@Override
				public int compare(Long arg0, Long arg1) {
					return (int) (arg1 - arg0);
				}

			});
			c = color;
			this.timeRange = timeRange;

			maxValue = Double.MIN_VALUE;
			smoothingRange = 1;
		}

		public void addValue(long time, double value) {
			synchronized (this) {
				
				if(smoothing.size() == smoothingRange)
					smoothing.remove(0);
				
				smoothing.add(value);
				
				double tot = 0;
				for(double d : smoothing)
					tot += d;
				
				tot = tot / smoothingRange;
				
				values.put(time, tot);
				if (tot > maxValue)
					maxValue = tot;
			}

		}

		/*
		 * Paints this dataline from most recent to least recent to the
		 * encapsulating Graph
		 */
		public void paint(Graphics g) {

			g.setColor(c);

			// Let oldx be the end of the graph area, so that the irst line come
			// out of the end of the graph
			int oldX = Graph.this.getWidth(), lastHeight = -1;

			// Adding -2 accounts for border
			double scalar = (Graph.this.getHeight() - 2) / maxValue;

			synchronized (this) {
				for (Entry<Long, Double> dat : values.entrySet()) {

					int newHeight = (int) (Graph.this.getHeight() - 2
							- dat.getValue() * scalar);

					int x = (int) (Graph.this.getWidth() - Graph.this.getWidth()
							* ((double) (System.currentTimeMillis() - dat.getKey()) / timeRange));

					if (lastHeight == -1)
						lastHeight = newHeight;

					g.drawLine(oldX, lastHeight + 1, x, newHeight + 1);

					oldX = x;
					lastHeight = newHeight;

					// Let the last one paint so that the graph continues to the
					// end
					if (dat.getKey() < System.currentTimeMillis() - timeRange)
						break;
				}
			}
		}

	}

}
