package edu.fsuj.csb.tools.organisms.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.DataFormatException;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.fsuj.csb.tools.organisms.Compartment;
import edu.fsuj.csb.tools.urn.URN;

/**
 * implements an adaption of DefaultMutableTreeNode to display comartment properties in a JTree
 * @author Stephan Richter
 *
 */
public class CompartmentNode extends SortedTreeNode {
	
  private static final long serialVersionUID = 1L;
	private boolean detailsLoaded=false;
	private Compartment compartment;
	
	/**
	 * creates a new named compartment node for the compartment determined by the compartment id 
	 * @param cid the compartment id
	 * @param name the caption for the node
	 */
	public CompartmentNode(Compartment c) {
		super(c.mainName()+" ("+"id: "+c.id()+")");
		compartment=c;
  }
	
	/**
	 * @return the id of the related compartment
	 */
	public Compartment compartment(){
		return compartment;
	}

	/**
	 * loads the detail information about the related compartment into the node
	 * @throws SQLException 
	 * @throws DataFormatException 
	 * @throws IOException 
	 */
	public void loadDetails() throws SQLException, DataFormatException, IOException {
		//System.out.println("CompartmentNode.loadDetails() | loaded="+detailsLoaded);
		if (!detailsLoaded){
			detailsLoaded=true;
			addNames();
			addUrns();
			addUrls();
			addContainers();
		}
  }

	private void addUrns() throws DataFormatException {
		Vector<URN> urns = compartment.urns();
		if (!urns.isEmpty()){
			DefaultMutableTreeNode urnNode=new DefaultMutableTreeNode("URNs");
			for (Iterator<URN> it = urns.iterator();it.hasNext();){
				urnNode.add(new UrnNode(it.next()));
			}
			add(urnNode);
		}
  }

	private void addContainers() throws SQLException, IOException {
		TreeSet<Integer> containers = compartment.containingCompartments();
		if (containers.size()==0) return;
		DefaultMutableTreeNode containerNode=new DefaultMutableTreeNode("contained in");
		for (Iterator<Integer> it = containers.iterator();it.hasNext();){
			containerNode.add(ComponentNode.create(it.next()));
		}
		add(containerNode);
  }

	/**
	 * adds the compartment's urls to the compartmentNode
	 * @throws MalformedURLException
	 * @throws DataFormatException 
	 */
	private void addUrls() throws MalformedURLException, DataFormatException {
		Vector<URL> urls = compartment.urls();
		if (urls==null || urls.isEmpty()) return;
		DefaultMutableTreeNode urlNode=new DefaultMutableTreeNode("URLs");		
	  for (Iterator<URL> it = urls.iterator();it.hasNext();){	  	
	  	urlNode.add(new URLNode(it.next()));
	  }
	  add(urlNode);
  }

	/**
	 * adds the compartment's names to the compartmentNode
	 */
	private void addNames() {
		TreeSet<String> names = compartment.names();
		if (names!=null && names.size()>1){
			DefaultMutableTreeNode nameNode=new DefaultMutableTreeNode("synonyms");
			for (Iterator<String> it = compartment.names().iterator();it.hasNext();){
				nameNode.add(new DefaultMutableTreeNode(it.next()));
			}
		  add(nameNode);
		}
  }
}
