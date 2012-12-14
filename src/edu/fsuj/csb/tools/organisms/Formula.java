package edu.fsuj.csb.tools.organisms;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.DataFormatException;

import edu.fsuj.csb.tools.xml.ObjectComparator;
import edu.fsuj.csb.tools.xml.Tools;

/**
 * container for formula related stuff
 * 
 * @author Stephan Richter
 * 
 */
public class Formula {

	private class Term {
		String term;
		int position;

		public Term(String term) {
			this.term = term.replace(" ", "");
			position = 0;
		}

		boolean atEnd() {
			return position >= term.length();
		}

		char current() {
			return term.charAt(position);
		}

		char next() {
			return term.charAt(position++);
		}

		public int nextDigit() {
			return Integer.parseInt("" + next());
		}

		public String toString() {
			return "Term(\"" + term + "\" @" + position + " [≙ "+term.substring(position)+"] )";
		}
	}

	private String formula;
	private TreeMap<String, Double> atoms = new TreeMap<String, Double>(ObjectComparator.get());
	public static double nReplacement = 5.0;


	/**
	 * creates new formula
	 * 
	 * @param formula
	 * @throws DataFormatException
	 */
	public Formula(String formula) throws DataFormatException {
		if (formula == null) throw new NullPointerException();
		formula=formula.replace(")mon", ")").replace(")mod", ")").replaceAll("\\^\\d*[+-]", "");
		this.formula = formula;		
		atoms = parseFormula();
	}

	private TreeMap<String, Double> parseFormula() throws DataFormatException {
		Tools.startMethod("parseFormula(input string: "+formula+")");
		TreeMap<String, Double> atomSet;
/*		String regex="\\.\\s*[A-Z]"; // character following dot, i.e. non number-after dot
		if (formula.matches(regex)) {
			String[] parts = formula.split(regex);
			atomSet=new TreeMap<String, Double>(ObjectComparator.get());
			for (int i=0; i<parts.length; i++){
				System.out.println("Part: "+parts[i]);
				TreeMap<String, Double> dummy = parseKomplexTerm(new Term(parts[i].trim()));				
				combine(atomSet, dummy);
			}
		} else*/ atomSet = parseKomplexTerm(new Term(formula));
		Tools.endMethod(atomSet);
		return atomSet;
	}

	private TreeMap<String, Double> parseKomplexTerm(Term komplexTerm) throws DataFormatException {
		Tools.startMethod("parseKomplexTerm("+komplexTerm+")");
		Double factor = parseNumber(komplexTerm);
		if (factor==null) factor=1.0;
		TreeMap<String, Double> atomSum = new TreeMap<String, Double>(ObjectComparator.get());
		while (!komplexTerm.atEnd()) {
			if (komplexTerm.current() == ')') {
				komplexTerm.next();
				multiply(atomSum, factor);
				Tools.endMethod(atomSum);
				return atomSum;
			}
			TreeMap<String, Double> partialSum = parseTerm(komplexTerm);
			combine(atomSum, partialSum);
		}
		multiply(atomSum, factor);
		Tools.endMethod(atomSum);
		return atomSum;
	}

	private static void combine(TreeMap<String, Double> atomSum, TreeMap<String, Double> partialSum) {
		Tools.startMethod("combine("+atomSum+" / "+partialSum);
		for (Iterator<Entry<String, Double>> it = partialSum.entrySet().iterator(); it.hasNext();) {
			Entry<String, Double> entry = it.next();
			Double val = atomSum.get(entry.getKey());
			if (val == null) {
				atomSum.put(entry.getKey(), entry.getValue());
			} else	atomSum.put(entry.getKey(), val + entry.getValue());
		}
		Tools.endMethod();
	}

	private TreeMap<String, Double> parseTerm(Term term) throws DataFormatException {
		Tools.startMethod("parseTerm("+term+")");
		TreeMap<String, Double> subterm = parseSubterm(term);
		Double factor = parseNumber(term);
		if (factor==null) factor=1.0;
		multiply(subterm, factor);
		Tools.endMethod(subterm);
		return subterm;

	}

	private TreeMap<String, Double> parseSubterm(Term subterm) throws DataFormatException {
		Tools.startMethod("parseSubterm("+subterm+")");
		if (subterm.current() == '(') {
			TreeMap<String, Double> result = parseParenthesizedTerm(subterm);
			Tools.endMethod(result);
			return result;
		}
		if (Character.isUpperCase(subterm.current()))	 {
			TreeMap<String, Double> result = parseAtom(subterm);
			Tools.endMethod(result);
			return result;
		}
		throw new DataFormatException("problem with " + subterm);
	}

	private TreeMap<String, Double> parseAtom(Term term) throws DataFormatException {
		Tools.startMethod("parseAtom("+term+")");
		String atom="";
		do {
			if (!Character.isUpperCase(term.current())) throw new DataFormatException("Uppercase letter expected at " + term);
			
			atom = atom + term.next();
			while (!term.atEnd() && Character.isLowerCase(term.current()))	atom = atom + term.next();
			if (!term.atEnd() && term.current() == ',')	atom = atom + term.next();
		}	while (atom.endsWith(",")); 

		TreeMap<String, Double> result = new TreeMap<String, Double>(ObjectComparator.get());
		result.put(atom, 1.0);
		Tools.endMethod(result);
		return result;

	}

	private TreeMap<String, Double> parseParenthesizedTerm(Term term) throws DataFormatException {
		Tools.startMethod("parseParenthesizedTerm("+term+")");
		if (term.current() == '(') {
			term.next();
		} else throw new DataFormatException("something's wrong with the parenthesis at \"" + term);		
		TreeMap<String, Double> result = parseKomplexTerm(term);
		Tools.endMethod(result);
		return result;

	}

	private Double parseNumber(Term term) {
		Tools.startMethod("parseNumber("+term+")");
		Double factor = null;
		String number = "";
		while (true){
			if (term.atEnd()) break;
			
			if (Character.isDigit(term.current()) || term.current()=='.'){
				number+=term.next();
			} else if (term.current()=='m' || term.current()=='n' || term.current()=='w' || term.current()=='x' || term.current()=='y' || term.current()=='z'){				
				term.next();
				factor=nReplacement;
				if (!number.isEmpty()) factor*=Double.parseDouble(number);
				
				if (!term.atEnd() && term.current()=='-'){
					term.next();
					double subtrahend=parseNumber(term);
					factor-=subtrahend;
				}
				if (!term.atEnd() && term.current()=='+'){
					term.next();					
					double summand=parseNumber(term);
					factor+=summand;
				}
				number=""+factor;
			} else {
				break;
			}
		}
		if (number.isEmpty() || number.equals(".")) {
			Tools.endMethod(null);
			return null;
		}
		factor=Double.parseDouble(number);
		Tools.endMethod(factor);
		return factor;
	}

	/**
	 * create a new formula as sum of the cureent one and the given one
	 * 
	 * @param f the formula to be added
	 * @return the sum of the two formulas
	 * @throws DataFormatException
	 */
	public void add(Formula f) throws DataFormatException {
		for (Iterator<Entry<String, Double>> it = f.atoms.entrySet().iterator(); it.hasNext();) {
			Entry<String, Double> atomEntry = it.next();
			String atom = atomEntry.getKey();
			Double stoich = atomEntry.getValue();
			if (atoms.containsKey(atom)) stoich += atoms.get(atom);
			atoms.put(atom, stoich);
		}
		calculateFormula();
	}
	
	public void subtract(Formula f) {
		for (Iterator<Entry<String, Double>> it = f.atoms.entrySet().iterator(); it.hasNext();) {
			Entry<String, Double> atomEntry = it.next();
			String atom = atomEntry.getKey();
			Double stoich = atomEntry.getValue();
			if (!atoms.containsKey(atom)) {
				throw new NoSuchElementException("Tried to remove "+atom+" from "+this+", but "+this+" contains no "+atom+"!");
			} else {
				stoich = atoms.get(atom)-stoich;
				if (stoich<0) throw new NullPointerException("Tried to remove "+f+" from "+this+", but it contains not enough "+atom+"!");
			}
			atoms.put(atom, stoich);
		}
		calculateFormula();
  }

	private void calculateFormula() {
		formula = atoms.toString().replace("{", "").replace("}", "").replace("=", "").replace(", ", "");
	}

	/**
	 * multiplies every atom count in the map with the given factor
	 * 
	 * @param substanceSum
	 * @param factor
	 */
	private static void multiply(TreeMap<String, Double> substanceSum, Double factor) {
		Tools.startMethod("multiply("+substanceSum+" x "+factor+")");
		if (factor != 1) for (Iterator<String> it = substanceSum.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			substanceSum.put(key, substanceSum.get(key) * factor);
		}
		Tools.endMethod();
	}

	/**
	 * create a multiple of this formula
	 * 
	 * @param i the factor to multiply with
	 * @return the multiple of the current formula
	 * @throws DataFormatException
	 */
	public Formula multiply(double i) throws DataFormatException {
		Formula result = new Formula(formula);
		multiply(result.atoms, i);
		result.calculateFormula();
		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return get().replace(".0", "");
	}

	/**
	 * @return the formula string
	 */
	public String get() {
		return formula;
	}

	/**
	 * @param f a formula to compare this formula with
	 * @return true, if both formulas are identical
	 */
	public boolean equals(Formula f) {
		if (f == null) return false;
		return atoms.equals(f.atoms);
	}

	public static void main(String[] args) throws DataFormatException {
		Formula f2 = new Formula("C20.0H32.0");
		System.out.println(f2.atoms());
		Formula	f1 = new Formula("C62H89CoN13O14P.C95H156N8O28P2(C40H64N8O21)n");
		System.out.println(f1.atoms());
	}

	public String atoms() {
		return atoms.toString();
	}

	public Set<String> atomSet() {
		return atoms.keySet();
	}

	public Formula stoichiometricDifference(Formula secondFormula) throws DataFormatException {
		Formula result = new Formula("");

		TreeSet<String> elements = new TreeSet<String>(ObjectComparator.get());
		elements.addAll(atomSet());
		elements.addAll(secondFormula.atomSet());
		for (Iterator<String> it = elements.iterator(); it.hasNext();) {
			String element = it.next();
			Double stoich = atoms.get(element);
			if (stoich == null) stoich = 0.0;
			Double dummy = secondFormula.atoms.get(element);
			if (dummy != null) stoich = Math.abs(stoich - dummy);
			if (stoich != 0.0) result.atoms.put(element, stoich);
		}

		result.calculateFormula();
		return result;
	}

	public String latexDiff() {
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> it = atoms.keySet().iterator(); it.hasNext();) {
			String element = it.next();
			String stoich = atoms.get(element).toString();
			stoich = stoich.replace(".0", "");
			if (!stoich.equals("1")) {
				sb.append(stoich);
				sb.append("$\\times$");
			}
			sb.append(element);
			if (it.hasNext()) sb.append(", ");
		}
		return sb.toString();
	}

	public String latex() {
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> it = atoms.keySet().iterator(); it.hasNext();) {
			String element = it.next();
			String stoich = atoms.get(element).toString().replace(".0", "");
			sb.append(element);
			if (!stoich.equals("1")) {
				sb.append("_{"+stoich+"}");
			}
		}
		return sb.toString();
	}

	
	public Formula elementDifference(Formula formula) throws DataFormatException {
		Formula result = clone();
		result.add(formula);
		Set<String> dummy = formula.atomSet();
		for (Iterator<String> atomIt = atomSet().iterator(); atomIt.hasNext();) {
			String atom = atomIt.next();
			if (dummy.contains(atom)) result.atoms.remove(atom);
		}
		result.calculateFormula();
		return result;
	}

	public Formula clone() {
		try {
			return new Formula(formula);
		} catch (DataFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isEmpty() {
		return atoms.isEmpty();
	}

	public static TreeSet<Formula> set() {
	  return new TreeSet<Formula>(ObjectComparator.get());
  }



	


}
