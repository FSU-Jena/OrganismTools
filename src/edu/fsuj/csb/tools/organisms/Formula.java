package edu.fsuj.csb.tools.organisms;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
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



	private String formula;
	private TreeMap<String, Double> atoms = new TreeMap<String, Double>(ObjectComparator.get());
	public static final double VARIABLE_REPLACEMENT = 5.0;


	/**
	 * creates new formula
	 * 
	 * @param formula
	 * @throws DataFormatException
	 */
	
	public Formula(String formula) throws DataFormatException {
		Stack<Character> stack=new Stack<Character>();
		for (int i=formula.length(); i>0; i--) stack.push(formula.charAt(i-1));
		this.formula=formula;
		parseFormula(stack);
  }

	
	
	private void parseFormula(Stack<Character> stack) throws DataFormatException {
		Tools.startMethod("parseFormula["+stackString(stack)+"]");
		initialize();
		atoms=parseMolecule(stack);
	  while (!stack.isEmpty()){
	  	parseSeparator(stack);
	  	atoms=unite(atoms,parseMolecule(stack));
	  }
	  Tools.endMethod();
  }

	private TreeMap<String, Double> unite(TreeMap<String, Double> atoms, TreeMap<String, Double> summand) {
		for (String key:summand.keySet()){
			if (atoms.containsKey(key)){
				atoms.put(key, atoms.get(key)+summand.get(key));
			} else atoms.put(key, summand.get(key));
		}
	  return atoms;
  }

	private void parseSeparator(Stack<Character> stack) throws DataFormatException {
		Tools.startMethod("parseSeparator["+stackString(stack)+"]");
		while (stack.peek()==' ') stack.pop();
		if (!(stack.pop()=='.')) dataFormatException(stack);
		while (stack.peek()==' ') stack.pop();
		Tools.endMethod();
  }

	private void dataFormatException(Stack<Character> stack) throws DataFormatException {
	  throw new DataFormatException(stackString(stack));
  }

	private String stackString(Stack<Character> stack) {
		StringBuffer sb=new StringBuffer();
		sb.append(' ');
		Object[] array = stack.toArray();
		for (Object o:array) sb.insert(1, o);
		sb.append(' ');
	  return sb.toString();
  }

	private TreeMap<String, Double> parseMolecule(Stack<Character> stack) throws DataFormatException {
		Tools.startMethod("parseMolecule["+stackString(stack)+"]");
		
		Double variable=null;
		if (Character.isLowerCase(stack.peek())) variable=parseVariable(stack); 
		TreeMap<String,Double> groups=parseGroup(stack);
		if (groups==null) dataFormatException(stack);
		TreeMap<String, Double> group=parseGroup(stack);
		while (group!=null){
			groups=unite(groups,group);
			group=parseGroup(stack);
		}
		if (variable!=null) groups=multiply(groups,variable);
		Tools.endMethod();
		return groups;
  }

	private TreeMap<String, Double> multiply(TreeMap<String, Double> atoms, double factor) {
		for (String key:atoms.keySet()){
			atoms.put(key, factor*atoms.get(key));
		}
	  return atoms;
  }

	private TreeMap<String, Double> parseGroup(Stack<Character> stack) throws DataFormatException {
		Tools.startMethod("parseGroup("+stackString(stack)+")");
		if (stack.isEmpty()){
			Tools.endMethod(null);
			return null;
		}
		if (stack.peek()=='('){
			Tools.indent("found opening bracket!");
			stack.pop();
			TreeMap<String, Double> sum = parseGroup(stack);
			if (sum==null || stack.isEmpty()) dataFormatException(stack);
			while (!(stack.peek()==')')){
				if (stack.peek()==' '||stack.peek()=='.') parseSeparator(stack);
				TreeMap<String, Double> summand = parseGroup(stack);
				sum=unite(sum, summand);
				if (stack.isEmpty()) dataFormatException(stack);
			}			
			Tools.indent("found closing bracket!");
			stack.pop();
			Double count=parseCount(stack);
			if (count!=null) sum=multiply(sum, count);
			Tools.endMethod(sum);
			return sum;
		}
		TreeMap<String, Double> sum = parseStoich(stack);
		while (!stack.isEmpty()){
			TreeMap<String, Double> summand = parseStoich(stack);
			if (summand==null) break;
			sum=unite(sum, summand);
		}
		Tools.endMethod(sum);
		return sum;
	}

	private Double parseCount(Stack<Character> stack) throws DataFormatException {
		Tools.startMethod("parseCount["+stackString(stack)+"]");
		if (stack.isEmpty()){
			Tools.endMethod(null);			
			return null;			
		}
		if (Character.isLowerCase(stack.peek())) {
			Double result = parseVariable(stack);
			Tools.endMethod(result);			
			return result;
		}
		Double result = parseNumber(stack);
		Tools.endMethod(result);
	  return result;
  }

	private Double parseNumber(Stack<Character> stack) {
		Tools.startMethod("parseNumber["+stackString(stack)+"]");
		if (stack.isEmpty()|| !Character.isDigit(stack.peek())){
			Tools.endMethod(null);
			return null;
		}
		StringBuffer sb=new StringBuffer();
		while (!stack.isEmpty() && Character.isDigit(stack.peek()))	sb.append(stack.pop());
		if (!stack.isEmpty() && stack.peek()=='.'){
			sb.append(stack.pop());
			if (!stack.isEmpty() && !Character.isDigit(stack.peek())){
				stack.push('.'); // if we find a dot, which is not followed by a digit, this dot does not belong to the formula. Put it back!
			} else {
				while (!stack.isEmpty() && Character.isDigit(stack.peek())){
					sb.append(stack.pop());
				}
			}			
		}
	  String dummy=sb.toString();
	  Double result=null;
	  if (dummy.length()>0) result=Double.parseDouble(dummy);
	  Tools.endMethod(result);
	  return result;
  }

	private TreeMap<String, Double> parseStoich(Stack<Character> stack) throws DataFormatException {
		Tools.startMethod("parseStoich["+stackString(stack)+"]");
		if (stack.isEmpty() || !Character.isUpperCase(stack.peek())){
			Tools.endMethod(null);
			return null;
		}
		String atom=parsAtom(stack);		
		Double number=parseNumber(stack);		
		TreeMap<String, Double> result=new TreeMap<String, Double>(ObjectComparator.get());
		result.put(atom, 1.0);
		if (number!=null) result=multiply(result, number);
		Tools.endMethod(result);
	  return result;
  }

	private String parsAtom(Stack<Character> stack) throws DataFormatException {
		Tools.startMethod("parsAtom["+stackString(stack)+"]");
		String result="";
		if (!Character.isUpperCase(stack.peek())) dataFormatException(stack);
		result+=stack.pop();		
		if (!stack.empty() && Character.isLowerCase(stack.peek())) result+=stack.pop();
		Tools.endMethod(result);
	  return result;
  }

	private Double parseVariable(Stack<Character> stack) throws DataFormatException {
		Tools.startMethod("parseVariable["+stackString(stack)+"]");
		String name="";
		if (!Character.isLowerCase(stack.peek())) dataFormatException(stack);
		while (!stack.isEmpty() && Character.isLowerCase(stack.peek())) name+=stack.pop();
		Tools.indent("found "+name);
		Tools.endMethod(VARIABLE_REPLACEMENT);
	  return VARIABLE_REPLACEMENT;
  }

	private void initialize() {
	  atoms=new TreeMap<String, Double>(ObjectComparator.get());
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
/*	private static void multiply(TreeMap<String, Double> substanceSum, Double factor) {
		Tools.startMethod("multiply("+substanceSum+" x "+factor+")");
		if (factor != 1) for (Iterator<String> it = substanceSum.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			substanceSum.put(key, substanceSum.get(key) * factor);
		}
		Tools.endMethod();
	}*/

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


	public static void main(String[] args) throws DataFormatException {
		Formula f2 = new Formula("(C8H8)n. (C4H6)n");
		System.out.println(f2.atoms());
		Formula	f1 = new Formula("C16H26N5O8. Gd. xH2O");
		System.out.println(f1.atoms());
	}

	


}
