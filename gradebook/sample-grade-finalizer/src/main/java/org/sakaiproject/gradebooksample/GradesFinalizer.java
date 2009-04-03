/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.gradebooksample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 *
 */
public class GradesFinalizer {
    private static final Log log = LogFactory.getLog(GradesFinalizer.class);
    
	private GradebookService gradebookService;
	private SessionManager sessionManager;
	private UserDirectoryService userDirectoryService;
	private SiteService siteService;
	private String siteUid;
	private String academicSessionEid;
	private String actAsUserEid;
	
	public void execute() {
		if ((siteUid == null) && (academicSessionEid == null)) {
			log.warn("No siteUid or academicSessionEid specified; nothing to do");
			return;
		}
		actAsUser(actAsUserEid);
		if (siteUid != null) {
			finalizeGradesForSiteUid(siteUid);
		} else {
			finalizeGradesForTermEid(academicSessionEid);
		}
	}
	
	public void finalizeGradesForSiteUid(String siteUid) {
		String gradebookUid = siteUid;
		if (gradebookService.isGradebookDefined(gradebookUid)) {
			if (log.isInfoEnabled()) log.info("About to finalize grades in gradebook " + gradebookUid);
			gradebookService.finalizeGrades(gradebookUid);
		} else {
			if (log.isInfoEnabled()) log.info("No gradebook to finalize for site context " + gradebookUid);
		}		
	}

	public void finalizeGradesForTermEid(String termEid) {
		Map<String, String> sitePropertyCriteria = new HashMap<String, String>();
		sitePropertyCriteria.put("term_eid", termEid);
		List<Site> sites = siteService.getSites(SiteService.SelectionType.NON_USER, null, null, sitePropertyCriteria, SiteService.SortType.NONE, null);
		if (log.isInfoEnabled()) log.info(sites.size() + " sites found for academicSessionEid=" + termEid);
		for (Site site : sites) {
			finalizeGradesForSiteUid(site.getId());
		}
	}
	
	/**
	 * TODO This piece of logic is repeated in enough places that we might want to add it
	 * to the SessionManager service.
	 */
	protected void actAsUser(String userEid) {
		try {
			User user = userDirectoryService.getUserByEid(userEid);
			Session sakaiSession = sessionManager.getCurrentSession();
			sakaiSession.setUserEid(userEid);
			sakaiSession.setUserId(user.getId());
		} catch (UserNotDefinedException e) {
			log.error("Could not act as user EID=" + userEid, e);
		}
		
	}

	public void setGradebookService(GradebookService gradebookService) {
		this.gradebookService = gradebookService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setSiteUid(String siteUid) {
		this.siteUid = siteUid;
	}

	public void setAcademicSessionEid(String academicSessionEid) {
		this.academicSessionEid = academicSessionEid;
	}

	public void setActAsUserEid(String actAsUserEid) {
		this.actAsUserEid = actAsUserEid;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

}
