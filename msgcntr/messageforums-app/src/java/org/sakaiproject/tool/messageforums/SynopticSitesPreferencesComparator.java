package org.sakaiproject.tool.messageforums;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrItem;

public class SynopticSitesPreferencesComparator implements Comparator<SynopticMsgcntrItem>{
	
	private List<String> orderedSiteList;
	
	public SynopticSitesPreferencesComparator(List<String> orderedSiteList){
		this.orderedSiteList = orderedSiteList;
	};

	public int compare(SynopticMsgcntrItem o1, SynopticMsgcntrItem o2) {
		String o1SiteId = o1.getSiteId();
		String o2SiteId = o2.getSiteId();
		for (Iterator<String> iterator = orderedSiteList.iterator(); iterator.hasNext();) {
			String orderedSiteId = (String) iterator.next();
			if(o1SiteId.equals(orderedSiteId)){
				return -1;
			}else if(o2SiteId.equals(orderedSiteId)){
				return 1;
			}
		}
		
		return 0;
	}

}
