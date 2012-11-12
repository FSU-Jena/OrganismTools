package edu.fsuj.csb.tools.organisms.gui;

import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.fsuj.csb.tools.xml.ObjectComparator;

public class SortedTreeNode extends DefaultMutableTreeNode {
  private static final long serialVersionUID = 4301644935372602427L;
	private ChangeListener changeListener;	

	public SortedTreeNode(String s) {
	  super(s);
  }
	
	public void addWithoutPublishing(DefaultMutableTreeNode child){
		if (getChildByName(child.toString())!=null) return;
		super.add(child);
	}

	public void add(DefaultMutableTreeNode child){
		addWithoutPublishing(child);
		publish();		
	}
	
	@SuppressWarnings("unchecked")
  public void publish() {
		if (super.children!=null) Collections.sort(super.children, ObjectComparator.get());
		if (changeListener!=null) changeListener.stateChanged(new ChangeEvent(this));
  }

	public void addAll(TreeSet<? extends DefaultMutableTreeNode> treeSet) {
		for (Iterator<? extends DefaultMutableTreeNode> it = treeSet.iterator(); it.hasNext();)	addWithoutPublishing(it.next());
		publish();
  }

	public void addChangeListener(ChangeListener speciesList) {
		changeListener=speciesList;
  }

	@SuppressWarnings("rawtypes")
  public Object getChildByName(String name) {
		if (children==null) return null;
		for (Iterator it = children.iterator();it.hasNext();){
			Object child = it.next();
			if (child.toString().equals(name)) return child;
		}
		return null;
  }

}
