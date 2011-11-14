package org.sakaiproject.delegatedaccess.util;

import javax.swing.tree.DefaultMutableTreeNode;

import org.sakaiproject.delegatedaccess.model.NodeModel;

/**
 * Extends DefaultMutableTreeNode in order for projectLogic to create new instances keeping it serialized for Wicket
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */

public class DelegatedAccessMutableTreeNode extends DefaultMutableTreeNode{
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isLeaf() {
		return ((NodeModel) this.getUserObject()).getNode().childNodeIds.isEmpty();
	}
}
