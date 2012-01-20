package org.sakaiproject.delegatedaccess.util;

import java.util.Comparator;

import org.sakaiproject.delegatedaccess.model.NodeModel;

/**
 * 
 * Compares node models for sorting by id or title
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class NodeModelComparator implements Comparator<NodeModel> {

	private int compareField = -1;

	public NodeModelComparator(int compareField){
		this.compareField = compareField;
	}

	public int compare(NodeModel arg0, NodeModel arg1) {
		switch (compareField) {
		case DelegatedAccessConstants.SEARCH_COMPARE_SITE_ID:
			return arg0.getNode().description.compareTo(arg1.getNode().description);
		case DelegatedAccessConstants.SEARCH_COMPARE_TERM:
			return arg0.getSiteTerm().compareToIgnoreCase(arg1.getSiteTerm());
		case DelegatedAccessConstants.SEARCH_COMPARE_INSTRUCTOR:
			return arg0.getSiteInstructors().compareToIgnoreCase(arg1.getSiteInstructors());
		case DelegatedAccessConstants.SEARCH_COMPARE_AUTHORIZATION:
			return arg0.getNodeShoppingPeriodAuth().compareToIgnoreCase(arg1.getNodeShoppingPeriodAuth());
		case DelegatedAccessConstants.SEARCH_COMPARE_SHOPPERS_BECOME:
			String nodeAuth0= arg0.getNodeAccessRealmRole()[0] + ":" + arg0.getNodeAccessRealmRole()[1];
			String nodeAuth1= arg1.getNodeAccessRealmRole()[0] + ":" + arg1.getNodeAccessRealmRole()[1];
			nodeAuth0.compareToIgnoreCase(nodeAuth1);
		case DelegatedAccessConstants.SEARCH_COMPARE_START_DATE:
			if(arg0.getNodeShoppingPeriodStartDate() == null && arg1.getNodeShoppingPeriodStartDate() == null){
				return 0;
			}else if(arg0.getNodeShoppingPeriodStartDate() == null){
				return 1;
			}else if(arg1.getNodeShoppingPeriodStartDate() == null){
				return -1;
			}else{
				return arg0.getNodeShoppingPeriodStartDate().compareTo(arg1.getNodeShoppingPeriodStartDate());
			}
		case DelegatedAccessConstants.SEARCH_COMPARE_END_DATE:
			if(arg0.getNodeShoppingPeriodEndDate() == null && arg1.getNodeShoppingPeriodEndDate() == null){
				return 0;
			}else if(arg0.getNodeShoppingPeriodEndDate() == null){
				return 1;
			}else if(arg1.getNodeShoppingPeriodEndDate() == null){
				return -1;
			}else{
				return arg0.getNodeShoppingPeriodEndDate().compareTo(arg1.getNodeShoppingPeriodEndDate());
			}
		case DelegatedAccessConstants.SEARCH_COMPARE_SITE_TITLE:
		default:
			return arg0.getNode().title.compareToIgnoreCase(arg1.getNode().title);
		}
	}

}
