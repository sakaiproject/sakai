/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.tool.producers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.rsf.helper.HelperViewParameters;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

@Slf4j
public class PermissionsProducer implements ViewComponentProducer,NavigationCaseReporter {

	public static final String VIEW_ID = "votePermissions";

	private LocaleGetter localeGetter;

	private static final String PERMISSION_PREFIX ="poll";

	public static final String HELPER = "sakai.permissions.helper";

	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}

	public void setLocaleGetter(LocaleGetter localeGetter) {
		this.localeGetter = localeGetter;
	}

	private ExternalLogic externalLogic;    
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters arg1,
			ComponentChecker arg2) {

		String locale = localeGetter.get().toString();
		Map<String, String> langMap = new HashMap<String, String>();
		langMap.put("lang", locale);
		langMap.put("xml:lang", locale);

		UIOutput.make(tofill, "polls-html", null).decorate(new UIFreeAttributeDecorator(langMap));
		
		UIOutput output = UIOutput.make(tofill, "permissions-wc", "");
		UIFreeAttributeDecorator locationDecorator = new UIFreeAttributeDecorator("on-refresh", externalLogic.getCurrentToolURL());
		output.decorate(locationDecorator);
	}

	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		togo.add(new NavigationCase(null, new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		togo.add(new NavigationCase("Success", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		togo.add(new NavigationCase("Cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		return togo;
	}	

}
