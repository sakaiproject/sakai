package org.adl.sequencer;

import java.io.Serializable;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

public class SeqActivityComparator implements Comparator<ActivityNode>, Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6253881918230754073L;

	public SeqActivityComparator() {
		super();
	}

	@Override
	public int compare(ActivityNode o1, ActivityNode o2) {
		DefaultMutableTreeNode n1 = o1;
		DefaultMutableTreeNode n2 = o2;

		SeqActivity a1 = (SeqActivity) n1.getUserObject();
		SeqActivity a2 = (SeqActivity) n2.getUserObject();

		int c1 = a1.getCount();
		int c2 = a2.getCount();

		return c1 - c2;
	}

}
