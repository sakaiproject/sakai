package org.sakaiproject.delegatedaccess.util;

import java.util.Comparator;

import org.sakaiproject.delegatedaccess.model.NodeModel;

public class NodeModelComparator implements Comparator<NodeModel> {

	private int compareField = -1;

	public NodeModelComparator(int compareField){
		this.compareField = compareField;
	}
	
	public int compare(NodeModel arg0, NodeModel arg1) {
		switch (compareField) {
		case DelegatedAccessConstants.SEARCH_COMPARE_SITE_ID:
			return arg0.getNode().description.compareTo(arg1.getNode().description);
		case DelegatedAccessConstants.SEARCH_COMPARE_SITE_TITLE:
		default:
			return arg0.getNode().title.compareToIgnoreCase(arg1.getNode().title);
		}
	}

}
