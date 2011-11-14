package org.sakaiproject.delegatedaccess.util;

import java.util.Comparator;

import org.sakaiproject.delegatedaccess.model.SearchResult;

/**
 * 
 * Compares user search results for sorting
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class SearchResultComparator implements Comparator<SearchResult>{

	private int compareField = -1;

	public SearchResultComparator(int compareField){
		this.compareField = compareField;
	}

	public int compare(SearchResult o1, SearchResult o2) {
		switch (compareField) {
		case DelegatedAccessConstants.SEARCH_COMPARE_EMAIL:
			if(o1.getEmail() == null && o2.getEmail() == null){
				return 0;
			}else if(o1.getEmail() == null){
				return 1;
			}else if(o2.getEmail() == null){
				return -1;
			}
			return o1.getEmail().compareToIgnoreCase(o2.getEmail());
		case DelegatedAccessConstants.SEARCH_COMPARE_SORT_NAME:
			if(o1.getSortName() == null && o2.getSortName() == null){
				return 0;
			}else if(o1.getSortName() == null){
				return 1;
			}else if(o2.getSortName() == null){
				return -1;
			}
			return o1.getSortName().compareToIgnoreCase(o2.getSortName());
		case DelegatedAccessConstants.SEARCH_COMPARE_TYPE:
			if(o1.getType() == null && o2.getType() == null){
				return 0;
			}else if(o1.getType() == null){
				return 1;
			}else if(o2.getType() == null){
				return -1;
			}
			return o1.getType().compareToIgnoreCase(o2.getType());
		case DelegatedAccessConstants.SEARCH_COMPARE_EID:
		default:
			if(o1.getEid() == null && o2.getEid() == null){
				return 0;
			}else if(o1.getEid() == null){
				return 1;
			}else if(o2.getEid() == null){
				return -1;
			}
			return o1.getEid().compareToIgnoreCase(o2.getEid());
		}
	}

	public int getCompareField() {
		return compareField;
	}

	public void setCompareField(int compareField) {
		this.compareField = compareField;
	}

}