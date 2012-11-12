package edu.fsuj.csb.tools.organisms.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.fsuj.csb.tools.urn.URN;

public class UrnNode extends DefaultMutableTreeNode implements ActionListener {

  private static final long serialVersionUID = -7645971873225468360L;
	private URN urn;
	public UrnNode(URN urn) {
		this.urn=urn;
  }
	
	public URN getUrn(){
		return urn;
	}
	
	public JMenuItem menuItem() {
		JMenuItem result = new JMenuItem("Resolve "+urn);
		result.addActionListener(this);
	  return result;
  }

	public void actionPerformed(ActionEvent arg0) {
		try {
	    Runtime.getRuntime().exec("gnome-open http://www.ebi.ac.uk/miriamws/main/rest/resolve/"+urn);
    } catch (IOException e) {
	    e.printStackTrace();
    }
  }
	
	public String toString(){
		return urn.toString();
	}
}
