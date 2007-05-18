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
package org.sakaiproject.scorm.tool;

import org.adl.api.ecmascript.APIErrorManager;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.pages.BlankPage;
import org.sakaiproject.scorm.tool.pages.ContentFrame;
import org.sakaiproject.scorm.tool.pages.LaunchFrameset;
import org.sakaiproject.scorm.tool.pages.ManageContent;
import org.sakaiproject.scorm.tool.pages.NavigationFrame;

import org.apache.wicket.markup.html.AjaxServerAndClientTimeFilter;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.io.IObjectStreamFactory;
import org.apache.wicket.util.lang.Objects;

public class ScormTool extends WebApplication {
	
	private ScormClientFacade clientFacade;
	private APIErrorManager errorManager;
	
	
	protected void init()
	{
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(true);
		//getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());
		getDebugSettings().setAjaxDebugModeEnabled(true);
		this.mountBookmarkablePage("/pages/navigate", "nav", NavigationFrame.class);
		this.mountBookmarkablePage("/pages/content", "content", ContentFrame.class);
		this.mountBookmarkablePage("/pages/blank", BlankPage.class);
		this.mountBookmarkablePage("/pages/manage", "content", ManageContent.class);
		this.mountBookmarkablePage("/pages/launch", "launch", LaunchFrameset.class);
		//this.mount("/pages", PackageName.forPackage(LaunchFrameset.class.getPackage()));
		errorManager = new APIErrorManager(APIErrorManager.SCORM_2004_API);
		Objects.setObjectStreamFactory(new IObjectStreamFactory.DefaultObjectStreamFactory());
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
