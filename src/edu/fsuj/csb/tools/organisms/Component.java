package edu.fsuj.csb.tools.organisms;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.DataFormatException;

import edu.fsuj.csb.tools.urn.URN;
import edu.fsuj.csb.tools.xml.Tools;

/**
 * the base class for different classes in this package
 * provides an id handling system
 * @author Stephan Richter
 *
 */
public abstract class Component {
	
	private TreeSet<String> names;
	private String mainName;
	private Vector<URN> urns;
	private int id;
	private static TreeMap<Integer,Component> createdComponents=new TreeMap<Integer, Component>();
	
	/**
	 * creates a new Component object
	 * @param id the objects id
	 * @param names the set of names of this object, may be null
	 * @param urns the set of urns of this object, may be null
	 * @param mainName the preferred name of this object
	 */
	public Component(int id,TreeSet<String> names,String mainName,Vector<URN> urns) {
		this.id=id;		
		this.names=names;			
		if (mainName!=null){
			if (names==null) names=Tools.StringSet();
			names.add(mainName);
		} else {
			if (names!=null)	mainName=names.first();
		}
		this.urns=urns;
		createdComponents.put(id, this);
  }	
	
	/**
	 * creates a new component with an specific id
	 * @param id
	 */
	public Component(int id) {
		this(id,null,null,null);
	}

	/**
	 * @return the component specific database id
	 */
	public int id() {
		return id;
	}
	
	/**
	 * @return the id as string
	 */
	public String idString(){
		return ""+id;
	}
	
	/**
	 * @return the set of names of this component
	 */
	public TreeSet<String> names() {
		return names;
	}
	
	/**
	 * @return the urns of this component
	 * @throws DataFormatException 
	 */
	public Vector<URN> urns() throws DataFormatException {
	  return urns;
  }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
    return this.getClass().getSimpleName()+" "+id()+" ("+mainName()+")";
	}
	
	

	/**
	 * @return the shortest of the names of this component
	 * @throws SQLException 
	 */
	public String mainName() {		
		if (mainName==null){
			mainName=shortestName();
		}
	  return mainName;
  }
	
	private String shortestName() {
		if (names()==null) return null;
		String shortest=null;		
		for (Iterator<String> it = names().iterator(); it.hasNext();){
			String name=it.next();
			if (shortest==null||name.length()<shortest.length()) shortest=name;
		}
	  return shortest;
  }

	/**
	 * create the component belonging to the given id
	 * @param id the id of the requested component
	 * @return the component belonging to the given id
	 */
	public static Component get(int id){
		return createdComponents.get(id);
	}
	
	/**
	 * compose urls out of the components urns and return them
	 * @return the set of registered urls
	 * @throws DataFormatException 
	 * @throws MalformedURLException 
	 */
	public Vector<URL> urls() throws MalformedURLException, DataFormatException {
	  Vector<URL> result=new Vector<URL>();
		for (Iterator<URN> it = urns().iterator(); it.hasNext();){
			Set<URL> urls = it.next().urls();
			if (urls!=null)	result.addAll(urls);
		}		
		return result;
  }
	
	/**
	 * add Names to this Component
	 * @param n the set of names to be added
	 */
	public void addNames(TreeSet<String> n){
		if (n==null) return;
		if (names==null) names=Tools.StringSet();
		names.addAll(n);
	}
	
	/**
	 * add URNs to this component
	 * @param vector the set of urns to be added
	 */
	public void addUrns(Collection<URN> vector) {
		if (vector==null) return;
		if (urns==null) urns=new Vector<URN>();
		urns.addAll(vector);
	  
  }

	public void addName(String name) {
		if (name==null) return;
		if (names==null) names=Tools.StringSet();
		names.add(name);	  
  }
}
