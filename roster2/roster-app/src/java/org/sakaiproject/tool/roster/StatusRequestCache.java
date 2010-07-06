/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.roster;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;

/**
 * Some of our session-scoped beans make frequent and expensive calls to services.
 * We can't cache the results in those beans because of their scope.  We therefore
 * use this request-scoped bean to cache objects returned by the services, and use
 * the JSF variable resolver to ensure that we're using the same RequestCache
 * throughout a single request. 
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public class StatusRequestCache {
	private static final Log log = LogFactory.getLog(StatusRequestCache.class);

	protected boolean init;
	List<EnrollmentSet> enrollmentSets;

	protected void init(ServicesBean services) {
		enrollmentSets = new ArrayList<EnrollmentSet>();

		// Get the site
		String siteId = services.toolManager.getCurrentPlacement().getContext();
		Site site = null;
		try {
			site = services.siteService.getSite(siteId);
		} catch (IdUnusedException ide) {
			log.warn("Unable to find site " + siteId);
			throw new RuntimeException(ide);
		}
		String providerId = site.getProviderGroupId();
		GroupProvider groupProvider = services.getGroupProvider();
		if(groupProvider == null) {
			if(log.isDebugEnabled()) log.debug("No group provider installed");
		} else {
			String[] sectionEids = groupProvider.unpackId(providerId);
			for(int i=0; i<sectionEids.length; i++) {
				Section section = null;
				try {
					section = services.cmService.getSection(sectionEids[i]);
				} catch (IdNotFoundException ide) {
					log.warn("Unable to find CM section " + sectionEids[i]);
					continue;
				}
				EnrollmentSet es = section.getEnrollmentSet();
				if(es != null) {
					enrollmentSets.add(es);
				}
			}
		}
		init = true;
	}

	public boolean isInitialized() {
		return init;
	}
}
