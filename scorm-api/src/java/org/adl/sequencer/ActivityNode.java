package org.adl.sequencer;

import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class ActivityNode extends DefaultMutableTreeNode implements TreeNode {
	private static final long serialVersionUID = 1L;

	private int parentLocation = -1;

	private boolean isSelectable = true;

	private boolean isEnabled = true;

	private boolean isHidden = false;

	private boolean isIncluded = false;

	private boolean isInChoice = false;

	private int depth = -1;

	private boolean isLeaf = true;

	private boolean isCurrent = false;

	public ActivityNode(Object obj) {
		super(obj);
	}

	public SeqActivity getActivity() {
		return (SeqActivity) getUserObject();
	}

	@Override
	public int getDepth() {
		return depth;
	}

	public int getParentLocation() {
		return parentLocation;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public boolean isInChoice() {
		return isInChoice;
	}

	public boolean isIncluded() {
		return isIncluded;
	}

	@Override
	public boolean isLeaf() {
		return isLeaf;
	}

	public boolean isSelectable() {
		return isSelectable;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public void setInChoice(boolean isInChoice) {
		this.isInChoice = isInChoice;
	}

	public void setIncluded(boolean isIncluded) {
		this.isIncluded = isIncluded;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public void setParentLocation(int parentLocation) {
		this.parentLocation = parentLocation;
	}

	public void setSelectable(boolean isSelectable) {
		this.isSelectable = isSelectable;
	}

	@SuppressWarnings("unchecked")
	public void sortChildrenRecursively() {
		if (children != null) {
			Collections.sort(children, new SeqActivityComparator());

			for (int i = 0; i < children.size(); i++) {
				ActivityNode child = (ActivityNode) children.get(i);
				child.sortChildrenRecursively();
			}
		}
	}

	@Override
	public String toString() {
		return "ActivityNode [activity=" + getActivity() + "]";
	}

}
