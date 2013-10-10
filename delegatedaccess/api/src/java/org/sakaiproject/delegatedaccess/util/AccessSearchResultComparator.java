package org.sakaiproject.delegatedaccess.util;

import java.util.Comparator;

import org.sakaiproject.delegatedaccess.model.AccessSearchResult;

public class AccessSearchResultComparator implements Comparator<AccessSearchResult> {

	private int compareField = -1;

	public AccessSearchResultComparator(int compareField){
		this.compareField = compareField;
	}
	
	@Override
	public int compare(AccessSearchResult o1, AccessSearchResult o2) {
		switch (compareField) {
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
			return o2.getType() - o1.getType();
		case DelegatedAccessConstants.SEARCH_COMPARE_LEVEL:
			return o2.getLevel() - o1.getLevel();
		case DelegatedAccessConstants.SEARCH_COMPARE_ACCESS:
			if(o1.getAccess() == null && o2.getAccess() == null){
				return 0;
			}else if(o1.getAccess() == null){
				return 1;
			}else if(o2.getAccess() == null){
				return -1;
			}else{
				String o1Access = o1.getAccess()[0] + ":" + o1.getAccess()[1];
				String o2Access = o2.getAccess()[0] + ":" + o2.getAccess()[1];
				return o1Access.compareTo(o2Access);
			}
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

}
