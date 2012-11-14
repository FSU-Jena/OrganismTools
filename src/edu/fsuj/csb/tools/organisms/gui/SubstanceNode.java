package edu.fsuj.csb.tools.organisms.gui;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.DataFormatException;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.fsuj.csb.tools.organisms.Substance;
import edu.fsuj.csb.tools.urn.URN;



/**
 * Extends DefaultMutableTreeNode to represent a substance in a JTree
 * @author Stephan Richter
 *
 */
public class SubstanceNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -6994997554575190960L;
	private Substance substance;
	private boolean detailsLoaded;

	/**
	 * creates a new substance node for the given substance
	 * @param s the database id of the substance to be displayed
	 * @throws SQLException 
	 */
	public SubstanceNode(Substance s) {
		super(s.mainName()+" ("+"id: "+s.id()+")");
		substance=s;
		detailsLoaded=false;
  }

	/**
	 * @return the substance of the substance this node refers to
	 */
	public Substance substance() {
		return substance;
	}

	/**
	 * load the substance details for this substance
	 * @throws MalformedURLException
	 * @throws DataFormatException 
	 */
	public void loadDetails() throws MalformedURLException, DataFormatException {
		if (!detailsLoaded){
			addNames();
			addUrns();
			getUrls();
		}
  }
	
	private void addUrns() throws DataFormatException {
		Vector<URN> urns = substance.urns();
		if (!urns.isEmpty()){
			DefaultMutableTreeNode urnNode=new DefaultMutableTreeNode("URNs");
			for (Iterator<URN> it = urns.iterator();it.hasNext();){
				urnNode.add(new UrnNode(it.next()));
			}
			add(urnNode);
		}
  }

	/**
	 * adds the urls of this substance to the SubstanceNode
	 * @throws MalformedURLException
	 * @throws DataFormatException 
	 */
	private void getUrls() throws MalformedURLException, DataFormatException {
		TreeSet<URL> urls = substance.urls();
		if (!urls.isEmpty()){
			DefaultMutableTreeNode urlNode=new DefaultMutableTreeNode("referenced URLs");
			for (Iterator<URL> it = urls.iterator();it.hasNext();){
	  		urlNode.add(new URLNode(it.next()));
	  	}
	  	add(urlNode);
		}
  }

	/**
	 * adds the substance's names to the SubstanceNode
	 */
	private void addNames() {
		DefaultMutableTreeNode nameNode=new DefaultMutableTreeNode("synonyms");
	  for (Iterator<String> it = substance.names().iterator();it.hasNext();){
	  	nameNode.add(new DefaultMutableTreeNode(it.next()));
	  }
	  add(nameNode);
  }

	/*public static int substanceId(Object key) {
		return mapping.get(key);
	}*/
}
