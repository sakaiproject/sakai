package org.sakaiproject.delegatedaccess.util;

import java.util.Comparator;

import org.sakaiproject.delegatedaccess.model.SiteSearchResult;

/**
 * 
 * Compares search results for sorting
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class SiteSearchResultComparator implements Comparator<SiteSearchResult> {

	private int compareField = -1;

	public SiteSearchResultComparator(int compareField){
		this.compareField = compareField;
	}

	public int compare(SiteSearchResult arg0, SiteSearchResult arg1) {
		switch (compareField) {
		case DelegatedAccessConstants.SEARCH_COMPARE_SITE_ID:
			return arg0.getSiteTitle().compareTo(arg1.getSiteTitle());
		case DelegatedAccessConstants.SEARCH_COMPARE_TERM:
			return arg0.getSiteTerm().compareToIgnoreCase(arg1.getSiteTerm());
		case DelegatedAccessConstants.SEARCH_COMPARE_INSTRUCTOR:
			return arg0.getInstructorsString().compareToIgnoreCase(arg1.getInstructorsString());
		case DelegatedAccessConstants.SEARCH_COMPARE_AUTHORIZATION:
			return arg0.getShoppingPeriodAuth().compareToIgnoreCase(arg1.getShoppingPeriodAuth());
		case DelegatedAccessConstants.SEARCH_COMPARE_ACCESS:
			arg0.getAccessString().compareToIgnoreCase(arg1.getAccessString());
		case DelegatedAccessConstants.SEARCH_COMPARE_START_DATE:
			if(arg0.getShoppingPeriodStartDate() == null && arg1.getShoppingPeriodStartDate() == null){
				return 0;
			}else if(arg0.getShoppingPeriodStartDate() == null){
				return 1;
			}else if(arg1.getShoppingPeriodStartDate() == null){
				return -1;
			}else{
				return arg0.getShoppingPeriodStartDate().compareTo(arg1.getShoppingPeriodStartDate());
			}
		case DelegatedAccessConstants.SEARCH_COMPARE_END_DATE:
			if(arg0.getShoppingPeriodEndDate() == null && arg1.getShoppingPeriodEndDate() == null){
				return 0;
			}else if(arg0.getShoppingPeriodEndDate() == null){
				return 1;
			}else if(arg1.getShoppingPeriodEndDate() == null){
				return -1;
			}else{
				return arg0.getShoppingPeriodEndDate().compareTo(arg1.getShoppingPeriodEndDate());
			}
		case DelegatedAccessConstants.SEARCH_COMPARE_SITE_TITLE:
		default:
			return arg0.getSiteTitle().compareToIgnoreCase(arg1.getSiteTitle());
		}
	}

}
