/**
 * 
 */
package edu.caltech.networksimulator.datacapture.graphical;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

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
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.setMaximumSize(new Dimension(100, 100));
		this.setMinimumSize(new Dimension(100, 100));
		this.setPreferredSize(new Dimension(100, 100));
		
		labels = new TreeMap<String, Label>();
	}
	
	public void setMax(String name, double max) {
		getLabel(name).setMax(max);
	}
	
	public void setColor(String name, Color c) {
		getLabel(name).setColor(c);
	}
	
	private Label getLabel(String name) {
		Label tmp = labels.get(name);
		if(tmp == null) {
			tmp = new Label(name);
			labels.put(name, tmp);
			
			this.add(tmp);
			revalidate();
			repaint();
		}
		
		return tmp;
	}
	
	private class Label extends JComponent {
		
		private String name;
		private Color c;

		private JLabel l;
		
		public Label(String name) {
			this.name = name;
			
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			l = new JLabel("<html>" + name + "</html>");
			this.add(l);
		}
		
		public void setColor(Color c) {
			this.c = c;
			l.setForeground(c);
		}
		
		public void setMax(double d) {
			l.setText("<html>" + name + ": " + d + "</html>");
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(c);
		}
		
	}

}
