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
	
	public void sortChildrenRecursively() {
		if (children != null) {
			Collections.sort(children, new SeqActivityComparator());
			
			for (int i=0;i<children.size();i++) {
				ActivityNode child = (ActivityNode)children.get(i);
				child.sortChildrenRecursively();
			}
		}
	}
	
	public SeqActivity getActivity() {
		return (SeqActivity)getUserObject();
	}
	
	public int getParentLocation() {
		return parentLocation;
	}
	
	public void setParentLocation(int parentLocation) {
		this.parentLocation = parentLocation;
	}
	
	public boolean isSelectable() {
		return isSelectable;
	}
	
	public void setSelectable(boolean isSelectable) {
		this.isSelectable = isSelectable;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public boolean isHidden() {
		return isHidden;
	}
	
	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}
	
	public boolean isIncluded() {
		return isIncluded;
	}
	
	public void setIncluded(boolean isIncluded) {
		this.isIncluded = isIncluded;
	}
	
	public boolean isInChoice() {
		return isInChoice;
	}
	
	public void setInChoice(boolean isInChoice) {
		this.isInChoice = isInChoice;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}
	
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	
	public boolean isCurrent() {
		return isCurrent;
	}
	
	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

}

