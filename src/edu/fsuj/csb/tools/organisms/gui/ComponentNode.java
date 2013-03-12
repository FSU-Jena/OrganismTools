package edu.fsuj.csb.tools.organisms.gui;

import java.io.Serializable;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.fsuj.csb.tools.organisms.Compartment;
import edu.fsuj.csb.tools.organisms.Component;
import edu.fsuj.csb.tools.organisms.Reaction;
import edu.fsuj.csb.tools.organisms.Substance;

public class ComponentNode implements Serializable{

  private static final long serialVersionUID = 1932876663158935586L;
  
  public static DefaultMutableTreeNode create(Component c){
  	if (c instanceof Substance){
  		return new SubstanceNode((Substance)c);
  	}
  	if (c instanceof Compartment){
  		return new CompartmentNode((Compartment)c);
  	}
  	if (c instanceof Reaction){
  		return new ReactionNode((Reaction)c);
  	}
  	return null;  	
  }

  public static DefaultMutableTreeNode create(Integer id){
  	return create(Component.get(id));
  }
}
