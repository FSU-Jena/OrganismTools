package edu.fsuj.csb.tools.organisms.gui;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.zip.DataFormatException;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.fsuj.csb.tools.organisms.Reaction;
import edu.fsuj.csb.tools.urn.URN;



/**
 * implements a DefaultMutableTreeNode derivate to display Reaction properties in a JTree
 * @author Stephan Richter
 *
 */
public class ReactionNode extends DefaultMutableTreeNode {

  private static final long serialVersionUID = 7428747505689380311L;
	private Reaction reaction;
	private boolean detailsLoaded;

	/**
	 * create a new reaction node for a reaction
	 * @param r the reaction, for which the node shall be created
	 * @throws SQLException 
	 */
	public ReactionNode(Reaction r) {
		super(r.mainName()+" ("+"id: "+r.id()+")");
		reaction=r;
		detailsLoaded=false;
}

	/**
	 * @return the databse id of the reaction this node represents
	 */
	public int reactionId() {
		return reaction.id();
	}

	/**
	 * loads the reaction details into the node
	 * @throws MalformedURLException
	 * @throws SQLException 
	 * @throws DataFormatException 
	 */
	public void loadDetails() throws MalformedURLException, SQLException, DataFormatException {
		if (!detailsLoaded){
			addSubstrates();
			addProducts();
			addNames();
			addUrns();
			addUrls();
			detailsLoaded=true;
		}
  }

	private void addUrns() throws DataFormatException {
		Vector<URN> urns = reaction.urns();
		if (!urns.isEmpty()){
			DefaultMutableTreeNode urnNode=new DefaultMutableTreeNode("URNs");
			for (Iterator<URN> it = urns.iterator();it.hasNext();){
				urnNode.add(new UrnNode(it.next()));
			}
			add(urnNode);
		}
  }

	/**
	 * loads the substrates of the reaction and adds them as leaves to the tree node
	 * @throws SQLException 
	 */
	private void addSubstrates() throws SQLException {
		DefaultMutableTreeNode substratesNode=new DefaultMutableTreeNode("Substrates");
		for (Entry<Integer, Integer> entry:reaction.substrates().entrySet()){
			DefaultMutableTreeNode node = ComponentNode.create(entry.getKey());
			node.add(new DefaultMutableTreeNode("Stoichiometry: "+entry.getValue()));
	  	substratesNode.add(node);
	  }
	  add(substratesNode);
  }
	
	/**
	 * loads the products of the reaction and adds them as leaves to the tree node
	 * @throws SQLException 
	 */
	private void addProducts() throws SQLException {
		DefaultMutableTreeNode productsNode=new DefaultMutableTreeNode("Products");
		for (Entry<Integer, Integer> entry:reaction.products().entrySet()){
			DefaultMutableTreeNode node = ComponentNode.create(entry.getKey());
			node.add(new DefaultMutableTreeNode("Stoichiometry: "+entry.getValue()));
	  	productsNode.add(node);
	  }
	  add(productsNode);
  }

	/**
	 * loads the urls of the reaction and adds them as leaves to the tree node
	 * @throws MalformedURLException
	 * @throws DataFormatException 
	 */
	private void addUrls() throws MalformedURLException, DataFormatException {
		Vector<URL> urls = reaction.urls();
		if (!urls.isEmpty()){
			DefaultMutableTreeNode urlNode=new DefaultMutableTreeNode("URLs");
			for (Iterator<URL> it = urls.iterator();it.hasNext();){
				urlNode.add(new URLNode(it.next()));
			}	
			add(urlNode);
		}
  }

	/**
	 * loads the names of the reaction and adds them as leaves to the tree node
	 */
	private void addNames() {
		DefaultMutableTreeNode nameNode=new DefaultMutableTreeNode("synonyms");
	  for (Iterator<String> it = reaction.names().iterator();it.hasNext();){
	  	nameNode.add(new DefaultMutableTreeNode(it.next()));
	  }
	  add(nameNode);
  }

	/*public static int substanceId(Object key) {
		return mapping.get(key);
	}*/
}
