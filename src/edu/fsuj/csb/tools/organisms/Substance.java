package edu.fsuj.csb.tools.organisms;

import java.io.Serializable;
import java.util.TreeSet;
import java.util.Vector;

import edu.fsuj.csb.tools.urn.URN;

/**
 * extends the component class to represent substances of chemical systems
 * @author Stephan Richter
 *
 */
public class Substance extends Component implements Serializable {
	private static final long serialVersionUID = -5292898966196728944L;
	private Formula formula;
	
	/**
	 * create a new substance
	 * @param id
	 * @param names
	 * @param mainName
	 * @param urns
	 * @param sumFormula
	 */
	public Substance(int id, TreeSet<String> names, String mainName, Vector<URN> urns,Formula sumFormula) {
	  super(id, names, mainName, urns);
	  this.formula=sumFormula;
  }
	
	/**
	 * sets the formula field to a new value
	 * @param newFormula the new value of formula
	 */
	public void setFormula(Formula newFormula) {
		formula=newFormula;
  }

	/**
	 * @return the fromula associated to this substance
	 */
	public Formula formula(){
		return formula;
	}
	
	/**
	 * return the substance belonging to the given id
	 * @param id the id of the requested substance
	 * @return the requested substance
	 */
	public static Substance get(int id){
		return (Substance) Component.get(id);
	}
	
  /**
   * creates a xml description of this substance, assigning it to the given compartment
   * @param compartmentId the id of the compartment, which shall be referenced in the tag
   * @return the xml tag for this substance
   * @throws SQLException 
   */
  public StringBuffer getCode(String compartmentId) {
		StringBuffer buffer=new StringBuffer();		
		buffer.append("\n\t<species id=\"s"+id()+"\" name=\""+mainName().replace("&", "&amp;").replace("<", "&lt;").replace("\"", "'")+"\" compartment=\""+compartmentId+"\"></species>");
	  return buffer;
  }
}
