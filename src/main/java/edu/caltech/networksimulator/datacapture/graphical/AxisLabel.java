/**
 * 
 */
package edu.caltech.networksimulator.datacapture.graphical;

import java.awt.Color;
import java.awt.Graphics;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

/**
 * @author Francesco
 *
 */
public class AxisLabel extends JComponent {

	private TreeMap<String, Label> labels;

	/**
	 * 
	 */
	public AxisLabel() {
		
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		labels = new TreeMap<String, Label>();
	}
	
	public void setMax(String name, double max) {
		getLabel(name).max = max;
	}
	
	public void setColor(String name, Color c) {
		getLabel(name).c = c;
	}
	
	private Label getLabel(String name) {
		Label tmp = labels.get(name);
		if(tmp == null) {
			tmp = new Label(name);
			labels.put(name, tmp);
		}
		
		return tmp;
	}
	
	private class Label extends JComponent {
		
		private String name;
		
		public double max;
		public Color c;
		
		public Label(String name) {
			this.name = name;
			
			
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(c);
			
			g.fillRect(0, 0, 10, 10);
		}
		
	}

}
