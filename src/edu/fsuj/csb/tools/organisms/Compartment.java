package edu.fsuj.csb.tools.organisms;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import edu.fsuj.csb.tools.urn.URN;
import edu.fsuj.csb.tools.xml.Tools;
import edu.fsuj.csb.tools.xml.XmlObject;
import edu.fsuj.csb.tools.xml.XmlToken;

/**
 * this class represents a compartment (in most cases an organism) as container of enzyme assigned reactions
 * @author Stephan Richter
 *
 */
public class Compartment extends Component implements Serializable,XmlObject {

	private static final long serialVersionUID = 3753572460320086428L;

	private TreeSet<Integer> contained;
	private ReactionSet reactions;
	private TreeSet<Integer> utilizedSubstances;
	private TreeSet<Integer> enzymes;
	private static TreeSet<Integer> allCompartments=new TreeSet<Integer>();

	/**
	 * create a new compartment
	 * @param id the id of this compartment object
	 * @param names the names of this object
	 * @param mainName the primary name
	 * @param urns the urns of this object, may be null
	 * @param containedCompartments the set of compartments contained within this compartment
	 * @param enzymes the set of enzymes associated to this compartment
	 */
	public Compartment(int id, TreeSet<String> names, String mainName, Vector<URN> urns,TreeSet<Integer> containedCompartments,TreeSet<Integer> enzymes) {
	  super(id, names, mainName, urns);
	  allCompartments.add(id);
	  contained=containedCompartments;
	  this.enzymes=enzymes;
  }
	
	/**
	 * tries to return the list of compartments, which are included in this one
	 * @param recursive  determines, whether compartments, which are included in the included compartments shall be listed, too
	 * @return the set of comartments contained within this compartment
	 */
	public TreeSet<Integer> containedCompartments(boolean recursive){
		if (!recursive) return contained;
		if (contained==null) return null;
		TreeSet<Integer> result=new TreeSet<Integer>();
		for (Iterator<Integer> it = contained.iterator();it.hasNext();){
			Integer cid=it.next();
			result.add(cid);
			result.addAll(containedCompartments(true));
		}
		return result;
	}

	/**
	 * @return the set of reactions which can happen in this compartment
	 */
	public ReactionSet reactions(){		
		return reactions;
	}
	
	public TreeSet<Integer> enzymes(){
		return enzymes;
	}
	
	/**
	 * @return the set of substances which may participate in reactions of this compartment
	 */
	public TreeSet<Integer> utilizedSubstances(){
		Tools.startMethod("Compartment.utilizedSubstances()");
		if (utilizedSubstances==null){
			utilizedSubstances=reactions().utilizedSubstances();
		}
		Tools.endMethod(utilizedSubstances);
		return utilizedSubstances;
	}
	
	/**
	 * return the compartment associated to the given id
	 * @param id the id of the requested compartment
	 * @return the compartment belonging to the given id
	 */
	public static Compartment get(int id){
		return (Compartment) Component.get(id);
	}

	/**
	 * @return the set of comartments containing this comaprtment
	 */
	public TreeSet<Integer> containingCompartments() {
	  TreeSet<Integer> result=new TreeSet<Integer>();
		for (Iterator<Integer> it = allCompartments.iterator(); it.hasNext();){
			Compartment c=Compartment.get(it.next());
			if (c.contains(this)) result.add(id());
		}
		return result;
  }

	/**
	 * test, whether this compartment contains another given compartment
	 * @param compartment 
	 * @return true, only if the given compartment is contained in the current one
	 */
	private boolean contains(Compartment compartment) {
	  return containedCompartments(false).contains(compartment.id());
  }
	
	public void addContainedCompartment(int cid) {
		if (contained==null) contained=new TreeSet<Integer>();
		contained.add(cid);
	}

	/**
	 * add new compartments to the list of contained comapartments
	 * @param cc
	 */
	public void addContainedCompartments(TreeSet<Integer> cc) {
		if (cc==null) return;
		if (contained==null) contained=new TreeSet<Integer>();
		contained.addAll(cc);
  }

	public void addEnzymes(TreeSet<Integer> enzymelist) {
		if (enzymelist==null) return;
		if (enzymes==null) enzymes=new TreeSet<Integer>();
		enzymes.addAll(enzymelist);
  }

	public void addReactions(ReactionSet reactionList) {
		if (reactionList==null) return;
		if (reactions==null) reactions=new ReactionSet();
		reactions.addAll(reactionList);
  }

	/**
	 * calculates, which substences can be produced by this compartment, when supplied with a given set of substrates
	 * @param substanceIds the set of substrates for the closure computation
	 * @return a set of substance ids
	 * @throws SQLException
	 */
	public Collection<Integer> calculateProductsOf(Collection<Integer> substanceIds) {
	  return reactions().calculateProductsOf(substanceIds, this);
  }
	
	@Override
	public TreeSet<String> names() {
		if (super.names()!=null && super.names().isEmpty()) addName("unnamed compartment");
	  return super.names();
	}

	@Override
  public StringBuffer getCode() {
		Tools.startMethod("Compartment.getCode()");
		setValue("id", "c"+id());
		setValue("name", mainName());
		setValue("siz", 1);
		StringBuffer result=super.getCode();
		Tools.endMethod(result.subSequence(0, 10)+"...");
		return result;
	}

	protected XmlToken reactionList() {
		Tools.startMethod("Compartment.reactionList()");
		XmlToken rList = new XmlToken("listOfReactions");
		int number=reactions.get().size()/50;
		int count=0;
		System.err.print("\n[");
		for (Integer reactionId : reactions.get()) {
			if (++count%number==0) System.err.print("#");
			rList.add(Reaction.get(reactionId));
		}
		System.err.println(']');
		Tools.endMethod("{"+rList.toString().substring(0, 40)+"...}");
		return rList;
	}

	protected XmlToken speciesList() {
		Tools.startMethod("Compartment.speciesList()");
		XmlToken sList=new XmlToken("listOfSpecies");			
		
		int number=utilizedSubstances().size()/50;
		int count=0;
		System.err.print("\n[");
		for (Integer speciesId : utilizedSubstances()) {
			if (++count%number==0) System.err.print("#");
			Substance substance = Substance.get(speciesId);
			substance.setValue("compartment",id());
			sList.add(substance);
		}
		System.err.println(']');
		Tools.endMethod("{"+sList.toString().substring(0, 40)+"...}");
		return sList;
	}

	public void uniteWith(Compartment c) {
	  // TODO Auto-generated method stub
		System.err.println("not implemented, yet");
		System.err.println(5/0);
	  
  }

	public XmlToken getModel() {
		Tools.startMethod("Compartment.getModel()");

		XmlToken sbml = new XmlToken("sbml");
		sbml.setValue("xmlns", "http://www.sbml.org/sbml/level2");
		sbml.setValue("level", 2);
		sbml.setValue("version", 1);
		
		XmlToken model=new XmlToken("model");
		model.setValue("id", "Compartment_"+id());
		model.setValue("name", names().first());
				
		XmlToken cList = new XmlToken("listOfCompartments");		
		cList.add(this);
		
		model.add(cList);
		model.add(speciesList());
		model.add(reactionList());
		
		sbml.add(model);
		Tools.endMethod("{"+model.toString().substring(0, 40)+"...}");
		return sbml;
	}
}