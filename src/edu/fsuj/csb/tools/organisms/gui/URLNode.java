package edu.fsuj.csb.tools.organisms.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * extends DefaultMutableTreeNode to represent urls
 * @author Stephan Richter
 *
 */
public class URLNode extends DefaultMutableTreeNode implements MutableTreeNode, ActionListener {

  private static final long serialVersionUID = -1766396897712691435L;
  private URL url;
	/**
	 * creates an UrlNode for the given url
	 * @param u the url to which the node refers
	 */
	public URLNode(URL u) {
		super(u);
		url=u;
  }

	/**
	 * @return the url to which the node refers
	 */
	public URL getUrl() {
	  return url;
  }

	public JMenuItem menuItem() {
		JMenuItem result = new JMenuItem("Go to "+url);
		result.addActionListener(this);
	  return result;
  }

	@Override
  public void actionPerformed(ActionEvent arg0) {
		try {
	    Runtime.getRuntime().exec("gnome-open "+url);
    } catch (IOException e) {
	    e.printStackTrace();
    }
  }

}
