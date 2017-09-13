/**
 * Copyright (c) 2003-2009 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
