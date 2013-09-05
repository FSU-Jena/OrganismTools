package edu.fsuj.csb.tools.organisms;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.DataFormatException;

import edu.fsuj.csb.tools.urn.URN;
import edu.fsuj.csb.tools.xml.XmlToken;

/**
 * reaction is a class extending the component class. it represents natural chemical reactions occuring in all metabolic systems
 * 
 * @author Stephan Richter
 * 
 */
public class Reaction extends Component {

	public static final byte FORWARD = 1;
	public static final byte BACKWARD = -1;

	private TreeMap<Integer, Integer> substrates; // substanceID => stohiometry
	private TreeMap<Integer, Integer> products; // substanceID => stohiometry
	private TreeMap<Integer, Byte> directions; // mapping from componentId to direction: -1: only backward / 0 both directions / 1 forward only
	protected Boolean spontan=null;
	
	/**
	 * @param id the id of the reaction
	 * @param names the names of this reaction
	 * @param mainName the preferred name of the reaction
	 * @param urns the urns of this reaction
	 * @param directions the mapping from compartments to reactions
	 */
	public Reaction(int id, TreeSet<String> names, String mainName, Vector<URN> urns, TreeMap<Integer, Integer> substrates, TreeMap<Integer, Integer> products, TreeMap<Integer, Byte> directions) {
	  super(id, names, mainName, urns);
	  this.directions=directions;
	  this.products=products;
	  this.substrates=substrates;
  }
	
	/**
	 * @return the substance ids (sid) for all the reaction's products mapped to their stochiometries
	 */
	public TreeMap<Integer, Integer> products() {
		return products;
	}

	/**
	 * checks, whether a certain substance (given by its database id) is a reactant (substrate) to this reaction
	 * 
	 * @param sid the id of the substance to be checked
	 * @return true, if the denoted substance is within the reaction's substrate set
	 */
	public boolean hasReactant(int sid) {
		if (substrates==null) return false;
		if (substrates.isEmpty()) return false;
		return substrates.keySet().contains(sid);
	}

	/**
	 * checks, whether a certain substance (given by its database id) is a product of the reaction
	 * 
	 * @param sid the database id of the substance to be checked
	 * @return true, if the denoted substance is within the product set of the reaction
	 */
	public boolean hasProduct(int sid) {
		if (products==null) return false;
		if (products.isEmpty()) return false;
		return products.keySet().contains(sid);
	}

	/**
	 * @return the substance ids (sid) for all the reaction's substrates mapped to their stochiometries
	 */
	public TreeMap<Integer, Integer> substrates() {
		return substrates;
	}

	/**
	 * @return the database ids of the products of this reaction
	 */
	public Collection<Integer> productIds() {
		return products().keySet();
	}

	/**
	 * @return the database ids of the substrates of this reaction
	 */
	public Collection<Integer> substrateIds() {
		return substrates().keySet();
	}

	/**
	 * tests, whether this reaction is forward-enabled in a certain compartment
	 * 
	 * @param compartment the compartment of interest
	 * @return true, if the reaction may fire forward in the given compartment or is bidirectional in the given compartment
	 * @throws SQLException
	 */
	public boolean firesForwardIn(Compartment compartment) {
		return directions(compartment.id()) >= 0;
	}

	/**
	 * tests, whether this reaction is backward-enabled in a certain compartment
	 * 
	 * @param compartment the compartment of interest
	 * @return true, if the reaction may fire backward in the given compartment or is bidirectional in the given compartment
	 * @throws SQLException
	 */
	public boolean firesBackwardIn(Compartment compartment) {
		return directions(compartment.id()) <= 0;
	}

	/**
	 * determines, in which directions this reaction may fire in a certain compartment
	 * 
	 * @param cid the id of the compartment of interest
	 * @return 1, if the reaction may fire only forward, -1 if it may only fire backward and 0 if its bidirectional in the compartment of interest
	 * @throws SQLException
	 */
	protected byte directions(int cid) {
		return directions.get(cid);
	}
	
	/**
	 * test, whether the reaction is stiochiometrically balanced
	 * @return true, if the sum of the substrates elements mathces the sum of the products elements
	 * @throws DataFormatException 
	 */
	public boolean isBalanced() throws DataFormatException{
		Formula substrateSum=null;
		for (Iterator<Entry<Integer, Integer>> sit = substrates.entrySet().iterator(); sit.hasNext();){
			Entry<Integer, Integer> entry = sit.next();
			Formula f = Substance.get(entry.getKey()).formula();
			int stoich=entry.getValue();
			if (f==null) throw new NullPointerException();
			f=f.multiply(stoich);
			if (substrateSum==null) {
				substrateSum=f;
			} else substrateSum.add(f);
		}
		Formula productSum=null;
		for (Iterator<Entry<Integer, Integer>> sit = products.entrySet().iterator(); sit.hasNext();){
			Entry<Integer, Integer> entry = sit.next();
			Formula f = Substance.get(entry.getKey()).formula();
			int stoich=entry.getValue();
			if (f==null) throw new NullPointerException();
			f=f.multiply(stoich);
			if (productSum==null) {
				productSum=f;
			} else productSum.add(f);
		}
		return productSum.equals(substrateSum);
	}
	
	/**
	 * return the reaction belonging to the given id
	 * @param id the id of the requested reaction
	 * @return the reaction belonging to the id
	 */
	public static Reaction get(int id){
		return (Reaction) Component.get(id);
	}

	/**
	 * add the given products to the product list of this reaction
	 * @param p the products with their stoichiometries
	 */
	public void addProducts(TreeMap<Integer, Integer> p) {
		if (p==null) return;
		if (products==null) products=new TreeMap<Integer, Integer>();
		products.putAll(p);	  
  }

	/**
	 * add the given substrates to the substrate list of this reaction
	 * @param s the substrates with their stoichiometries
	 */
	public void addSubstrates(TreeMap<Integer, Integer> s) {
		if (s==null) return;
		if (substrates==null) substrates=new TreeMap<Integer, Integer>();
		substrates.putAll(s);	  
  }

	public void addDirection(int cid, Byte b) {
	  if (b==null) return;
	  if (directions==null) directions=new TreeMap<Integer, Byte>();
	  directions.put(cid, b);
  }
	
	public boolean hasUnchangedSubstances() {
		boolean isMagic = false;
		Map<Integer, Integer> prods = products();
		for (Iterator<Entry<Integer, Integer>> subs = substrates().entrySet().iterator(); subs.hasNext();) {
			Entry<Integer, Integer> subsEntry = subs.next();
			int subst = subsEntry.getKey();
			int consumption = subsEntry.getValue();

			if (prods.containsKey(subst)) {
				int production = prods.get(subst);
				if (production == consumption) {
					isMagic = true;
					return isMagic;
				}
			}
		}
		return isMagic;
	}
	
	/**
	 * get the xml code of this reaction
	 * 
	 * @param backward if set to true, product and substrates will be swapped
	 * @return teh xml code for this reaction with a certain direction
	 * @throws SQLException 
	 */
	public StringBuffer getCode(boolean backward) {		
		String rid = (backward ? "rb" : "r") + id();
		
		setValue("id", rid);
		setValue("name", mainName().replace("&", "&amp;").replace("<", "&lt;").replace("\"", "'"));
		
		Map<Integer, Integer> prods, subs;
		if (backward) {
			prods = substrates();
			subs = products();
		} else {
			prods = products();
			subs = substrates();
		}
		
		XmlToken reactantList=new XmlToken("listOfReactants");		
		for (Entry<Integer, Integer> substrate:subs.entrySet()){
			XmlToken sRef=new XmlToken("speciesReference");
			Substance substance = Substance.get(substrate.getKey());
			sRef.setValue("species", "s"+substance.id());
			sRef.setValue("name", substance.mainName());
			if (substrate.getValue()!=1) sRef.setValue("stoichiometry", substrate.getValue());
			reactantList.add(sRef);
		}		
		add(reactantList);

		XmlToken productList=new XmlToken("listOfProducts");		
		for (Entry<Integer, Integer> product:prods.entrySet()){
			XmlToken sRef=new XmlToken("speciesReference");
			Substance substance = Substance.get(product.getKey());
			sRef.setValue("species", "s"+substance.id());
			sRef.setValue("name", substance.mainName());
			if (product.getValue()!=1) sRef.setValue("stoichiometry", product.getValue());
			productList.add(sRef);
		}
		add(productList);

		XmlToken kinetics=new XmlToken("kineticLaw");
		XmlToken math=new XmlToken("math");
		math.setValue("xmlns", "http://www.w3.org/1998/Math/MathML");
		math.add(substrateKineticTerm(subs));
		kinetics.add(math);
		add(kinetics);
		
		return super.getCode();
	}
	
	/**
	 * create the kinetic term for the substrates of this reaction for usage in getCode
	 * 
	 * @param subs the mapping from the substrates to their stochiometric factors
	 * @return the kinetic rule term
	 */
	private XmlToken substrateKineticTerm(Map<Integer, Integer> subs) {
		Iterator<Entry<Integer, Integer>> it = subs.entrySet().iterator();
		if (!it.hasNext()) {
			XmlToken result = new XmlToken("cn");
			result.setContent(1);
			return result;
		}
		XmlToken lastToken = substrateKineticTerm(it.next());
		while (it.hasNext()) {
			XmlToken apply = new XmlToken("apply");
			XmlToken times=new XmlToken("times");
			times.add(substrateKineticTerm(it.next()));
			times.add(lastToken);
			apply.add(times);
			lastToken=apply;
			//lastToken = "<apply>\n<times/>\n" + substrateKineticTerm(it.next()) + "\n" + lastToken + "\n</apply>";
		}
		return lastToken;
	}
	
	/**
	 * create the kinetic term for a certain substrate of a reaction
	 * 
	 * @param entry the substrate entry containing the substrate id and the substrate's stochiometry
	 * @return the string representation of the kinetic term
	 */
	private XmlToken substrateKineticTerm(Entry<Integer, Integer> entry) {
		XmlToken ci=new XmlToken("ci");
		ci.setContent("s"+entry.getKey());
		if (entry.getValue() == 1) return ci;
		
		XmlToken apply=new XmlToken("apply");
		XmlToken times=new XmlToken("times");
		XmlToken cn=new XmlToken("cn");
		cn.setContent(entry.getValue());
		
		times.add(cn);
		times.add(ci);
		apply.add(times);
		return apply;
		
		//return "<apply>\n\t<times/>\n\t<cn> " + entry.getValue() + " </cn>\n\t<ci> s" + entry.getKey() + " </ci>\n</apply>";
	}
	
	public Boolean isSpontan(){
		return spontan;
	}
	
	@Override
	public StringBuffer getCode() {
	  return getCode(false);
	}
}
