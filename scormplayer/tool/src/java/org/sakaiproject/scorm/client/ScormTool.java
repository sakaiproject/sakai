/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.client;

import org.adl.api.ecmascript.APIErrorManager;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.pages.ContentFrame;
import org.sakaiproject.scorm.client.pages.ManageContent;

import wicket.markup.html.AjaxServerAndClientTimeFilter;
import wicket.protocol.http.WebApplication;

public class ScormTool extends WebApplication {
	
	private ScormClientFacade clientFacade;
	private APIErrorManager errorManager;
	
	
	protected void init()
	{
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());
		getDebugSettings().setAjaxDebugModeEnabled(true);
		this.mountBookmarkablePage("/content", ContentFrame.class);
		errorManager = new APIErrorManager(APIErrorManager.SCORM_2004_API);
	}

	@Override
	public Class getHomePage() {
		return ManageContent.class;
	}
	
	public APIErrorManager getErrorManager() {
		return errorManager;
	}
	
	public ScormClientFacade getClientFacade() {
		return clientFacade;
	}

	public void setClientFacade(ScormClientFacade clientFacade) {
		this.clientFacade = clientFacade;
	}
		
}
