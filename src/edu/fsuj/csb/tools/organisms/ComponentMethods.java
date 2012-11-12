package edu.fsuj.csb.tools.organisms;

import java.net.URL;
import java.util.TreeSet;

public interface ComponentMethods {
	public int id();
	public String idString();
	public TreeSet<String> names();
	public String mainName();
	public Component get();
	public TreeSet<URL> urls();
}
