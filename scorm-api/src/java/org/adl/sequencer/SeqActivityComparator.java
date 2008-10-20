package org.adl.sequencer;

import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.adl.sequencer.SeqActivity;

public class SeqActivityComparator implements Comparator {

	public SeqActivityComparator() {
		super();
	}
	
	public int compare(Object o1, Object o2) {
		DefaultMutableTreeNode n1 = (DefaultMutableTreeNode)o1;
		DefaultMutableTreeNode n2 = (DefaultMutableTreeNode)o2;
		
		SeqActivity a1 = (SeqActivity)n1.getUserObject();
		SeqActivity a2 = (SeqActivity)n2.getUserObject();
		
		int c1 = a1.getCount();
		int c2 = a2.getCount();
		
		return c1 - c2;
	}

}
