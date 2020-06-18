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
import java.util.List;

import org.sakaiproject.mailsender.logic.ExternalLogic;

import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class PermissionsProducer implements ViewComponentProducer, NavigationCaseReporter
{
	public static final String VIEW_ID = "permissions";

	public String getViewID()
	{
		return VIEW_ID;
	}

	// Injection
	private ExternalLogic externalLogic;


	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{

		UIOutput output = UIOutput.make(tofill, "permissions-wc", "");
		UIFreeAttributeDecorator locationDecorator = new UIFreeAttributeDecorator("on-refresh", externalLogic.getCurrentToolURL());
		output.decorate(locationDecorator);
	}

	public List<NavigationCase> reportNavigationCases()
	{
		List<NavigationCase> l = new ArrayList<NavigationCase>();
		// default navigation case
		l.add(new NavigationCase(null, new SimpleViewParameters(ComposeProducer.VIEW_ID)));
		return l;
	}

	public void setExternalLogic(ExternalLogic externalLogic)
	{
		this.externalLogic = externalLogic;
	}

}
