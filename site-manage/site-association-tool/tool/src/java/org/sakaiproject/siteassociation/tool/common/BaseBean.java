/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.siteassociation.tool.common;

import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.siteassociation.api.SiteAssocManager;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.SessionManager;

public class BaseBean {

	private String context;
	private ActiveToolManager toolManager;
	private SiteService siteService;
	private SiteAssocManager siteAssocManager;
	private SessionManager sessionManager;
	
	// Navigation constants
	protected static final String SAVE = "save", BACK = "back",
			CANCEL = "cancel";
	
	protected BaseBean() {
	}

	public String getContext() {
		if (context == null) {
			context = getToolManager().getCurrentPlacement().getContext();
		}
		return context;
	}
	
	public ActiveToolManager getToolManager() {
		return toolManager;
	}
	
	public void setToolManager(ActiveToolManager toolManager) {
		this.toolManager = toolManager;
	}
	
	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public SiteAssocManager getSiteAssocManager() {
		return siteAssocManager;
	}

	public void setSiteAssocManager(SiteAssocManager siteAssocManager) {
		this.siteAssocManager = siteAssocManager;
	}
	
	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

}
