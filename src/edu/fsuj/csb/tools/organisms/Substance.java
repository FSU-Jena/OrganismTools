package edu.fsuj.csb.tools.organisms;

import java.io.Serializable;
import java.util.TreeSet;
import java.util.Vector;

import edu.fsuj.csb.tools.urn.URN;
import edu.fsuj.csb.tools.xml.Tools;

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
	
  @Override
  public StringBuffer getCode() {
  	Tools.startMethod("Substance.getCode()");
  	setValue("id", "s"+id());
  	setValue("name", mainName().replace("&", "&amp;").replace("<", "&lt;").replace("\"", "'"));
  	setValue("initialConcentration", "1.0");
  	StringBuffer result = super.getCode();
  	Tools.endMethod(result,40);
    return result;
  }
}
