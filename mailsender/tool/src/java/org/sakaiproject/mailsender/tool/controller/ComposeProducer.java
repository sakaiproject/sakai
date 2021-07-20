/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.tool.controller;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.user.api.User;



public class ComposeProducer  {
	public static final String VIEW_ID = "compose";

	// Spring injected beans
	private ExternalLogic externalLogic;
	
	public void setExternalLogic(ExternalLogic externalLogic)
	{
		this.externalLogic = externalLogic;
	}
	
	{
		String emailBean = "emailBean.newEmail";
		
		// get the user then name & email
		User curUser = externalLogic.getCurrentUser();

		String fromEmail = "";
		String fromDisplay = "";
		if (curUser != null)
		{
			fromEmail = curUser.getEmail();
			fromDisplay = curUser.getDisplayName();
		}
		String from = fromDisplay + " <" + fromEmail + ">";
		
		// create the select by role link
		
		
		// create the select by section link
		
		if (externalLogic.isEmailArchiveAddedToSite())
		{
			
		}

		
	}

}