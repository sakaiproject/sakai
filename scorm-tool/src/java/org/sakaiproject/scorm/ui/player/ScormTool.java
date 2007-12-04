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
package org.sakaiproject.scorm.ui.player;

import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

public class ScormTool extends SakaiWebApplication {
	
	private ScormClientFacade clientFacade;
	
	@Override
	public void init() {
		super.init();
	}
	
	@Override
	public Class getHomePage() {
		return PackageListPage.class;
	}
	
	public ScormClientFacade getClientFacade() {
		return clientFacade;
	}

	public void setClientFacade(ScormClientFacade clientFacade) {
		this.clientFacade = clientFacade;
	}
		
}
