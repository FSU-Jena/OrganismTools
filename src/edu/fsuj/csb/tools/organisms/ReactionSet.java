package edu.fsuj.csb.tools.organisms;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import edu.fsuj.csb.tools.xml.ObjectComparator;

/**
 * implements a set of reactions. has been separated from Compartment, since there shall be methods not only related to compartments
 * @author Stephan Richter
 *
 */
public class ReactionSet implements Serializable,Iterable<Integer> {

  private static final long serialVersionUID = 3743143615241167238L;
	private TreeSet<Integer> reactions;
	
	/**
	 * create a new reaction set
	 */
	public ReactionSet() {
		reactions=new TreeSet<Integer>(ObjectComparator.get());
  }
	
	/**
	 * @return all the database ids of reactions belonging to this reaction set
	 */
	public TreeSet<Integer> get() {
	  TreeSet<Integer> result=new TreeSet<Integer>(ObjectComparator.get());
	  result.addAll(reactions);
		return result;
  }
	/**
	 * add a singular reaction to this reaction set
	 * @param reactionId the id of the reaction, that shall be added
	 */
	public void add(Integer reactionId) {
		reactions.add(reactionId);
  }
	/**
	 * adds a bunch of reactions to the reaction set
	 * @param reactionIds the set of ids of the reactions to be added
	 */
	public void addAll(TreeSet<Integer> reactionIds) {
		reactions.addAll(reactionIds);
  }
	
	/**
	 * calculates the set of substances which can be produced in the current compartment, when given a set of substances in excess
	 * @param substanceIds the set of input substance
	 * @return the set of substances which may be formed
	 * @throws SQLException
	 */
	public Collection<Integer> calculateProductsOf(Collection<Integer> substanceIds,Compartment compartment) {
		substanceIds = new TreeSet<Integer>(substanceIds);
		int substanceNumber = 0;
		do {
			substanceNumber = substanceIds.size();
			for (Iterator<Integer> reactionIds = reactions.iterator(); reactionIds.hasNext();) {

				Reaction reaction = Reaction.get(reactionIds.next());

				if (reaction.firesForwardIn(compartment)) {
					if (substanceIds.containsAll(reaction.substrateIds())) {
/*						System.out.println("\ntesting reaction "+reaction.id()+" ("+reaction+")");
						System.out.println("it fires forward...");
						System.out.println("Substrates: "+reaction.substrateIds());
						System.out.println("Given substances: "+substanceIds);
						System.out.println("Reaction forward enabled. Adding products: "+reaction.productIds());//*/
						substanceIds.addAll(reaction.productIds());
						// System.exit(0);
					}
				}
				if (reaction.firesBackwardIn(compartment)) {
					if (substanceIds.containsAll(reaction.productIds())) {
/*						System.out.println("\ntesting reaction "+reaction.id()+" ("+reaction+")");
						System.out.println("it fires forward...");
						System.out.println("Substrates: "+reaction.substrateIds());
						System.out.println("Given substances: "+substanceIds);
						System.out.println("Reaction backward enabled. Adding products: "+reaction.substrateIds());//*/
						substanceIds.addAll(reaction.substrateIds());
						// System.exit(0);
					}
				}
			}
		} while (substanceIds.size() != substanceNumber);
		return substanceIds;
	}
	
	/**
	 * returns the set of substances, which occur in reactions that may proceed in this compartment (including spontaneous reactions)
	 * @return a set of substance ids
	 */
	public TreeSet<Integer> utilizedSubstances() {
		TreeSet<Integer> result = new TreeSet<Integer>();

		for (Iterator<Integer> reactionIterator = reactions.iterator(); reactionIterator.hasNext();) {
			int rid=reactionIterator.next();
			Reaction r = Reaction.get(rid);
			//System.err.println("......"+r);
			if (r == null) throw new NoSuchElementException("Not reaction for database id:"+rid);
			result.addAll(r.products().keySet());
			result.addAll(r.substrates().keySet());
		}

		return result;
	}
	
	public String toString() {
	  return reactions.toString();
	}

	public void addAll(ReactionSet reactions) {
		addAll(reactions.get());
  }

	public Iterator<Integer> iterator() {
	  return reactions.iterator();
  }
	
	public ReactionSet clone() throws CloneNotSupportedException {
		
	  ReactionSet result=new ReactionSet();
	  result.addAll(reactions);
		return result;
	}

	public void removeAll(ReactionSet rs) {
		this.reactions.removeAll(rs.reactions);
  }

	public boolean contains(Integer rid) {
	  return reactions.contains(rid);
  }
}
