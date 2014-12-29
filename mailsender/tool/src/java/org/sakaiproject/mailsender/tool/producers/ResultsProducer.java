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

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ResultsProducer implements ViewComponentProducer
{
	public static final String VIEW_ID = "results";
	private NavBarRenderer navBarRenderer;

	public void setNavBarRenderer(NavBarRenderer nbr)
	{
		navBarRenderer = nbr;
	}

	public String getViewID()
	{
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		// make the navigation bar
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
	}
}