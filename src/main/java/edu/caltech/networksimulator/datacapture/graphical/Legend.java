/**
 * 
 */
package edu.caltech.networksimulator.datacapture.graphical;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * @author Francesco
 *
 */
public class Legend extends JComponent {

	/**
	 * 
	 */
	public Legend() {
		this.setLayout(new FlowLayout());
	}
	
	public void addLabel(Color c, String name) {
		JLabel label = new JLabel(name);
		label.setForeground(c);
		this.add(label);
		validate();
	}

}
