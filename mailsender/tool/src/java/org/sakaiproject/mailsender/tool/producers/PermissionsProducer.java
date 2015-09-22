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
package org.sakaiproject.mailsender.tool.producers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

import org.sakaiproject.rsf.helper.HelperViewParameters;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class PermissionsProducer implements ViewComponentProducer, ViewParamsReporter,
		NavigationCaseReporter
{
	public static final String VIEW_ID = "permissions";

	public String getViewID()
	{
		return VIEW_ID;
	}

	// Injection
	private SessionManager sessionManager;
	private ExternalLogic externalLogic;
	private MessageLocator messageLocator;

	private static final String HELPER = "sakai.permissions.helper";

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		String locationId = externalLogic.getCurrentLocationId();
		ToolSession session = sessionManager.getCurrentToolSession();

		session.setAttribute(PermissionsHelper.TARGET_REF, locationId);
		session.setAttribute(PermissionsHelper.DESCRIPTION, messageLocator.getMessage(
				"mailsender.permissions.header", externalLogic.getCurrentSiteTitle()));
		session.setAttribute(PermissionsHelper.PREFIX, "mailtool.");

		List<String> perms = externalLogic.getPermissionKeys();
		HashMap<String, String> pRbValues = new HashMap<String, String>();
		for (int i = 0; i < perms.size(); i++)
		{
			String perm = perms.get(i);
			String descr = messageLocator.getMessage("desc-" + perm);
			pRbValues.put("desc-" + perm, descr);
		}

		session.setAttribute("permissionDescriptions", pRbValues);
		UIOutput.make(tofill, HelperViewParameters.HELPER_ID, HELPER);
		UICommand.make(tofill, HelperViewParameters.POST_HELPER_BINDING, "", null);
	}

	public ViewParameters getViewParameters()
	{
		return new HelperViewParameters();
	}

	public List<NavigationCase> reportNavigationCases()
	{
		List<NavigationCase> l = new ArrayList<NavigationCase>();
		// default navigation case
		l.add(new NavigationCase(null, new SimpleViewParameters(ComposeProducer.VIEW_ID)));
		return l;
	}

	public void setMessageLocator(MessageLocator messageLocator)
	{
		this.messageLocator = messageLocator;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	public void setExternalLogic(ExternalLogic externalLogic)
	{
		this.externalLogic = externalLogic;
	}

}
