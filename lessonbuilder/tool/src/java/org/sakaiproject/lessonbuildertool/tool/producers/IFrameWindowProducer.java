/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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


package org.sakaiproject.lessonbuildertool.tool.producers;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.IframeUrlUtil;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;

/**
 * Places an iframe in a new window. Used to display assignments.
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
@Slf4j
public class IFrameWindowProducer implements ViewComponentProducer, ViewParamsReporter {
	private SimplePageBean simplePageBean;
	public LocaleGetter localeGetter;                                                                                             

	public static final String VIEW_ID = "IFramePage";

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters params, ComponentChecker checker) {
	        if (!simplePageBean.canReadPage())
		    return;

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		if (((GeneralViewParameters) params).getSendingPage() != -1) {
		    try {
			simplePageBean.updatePageObject(((GeneralViewParameters) params).getSendingPage());
		    } catch (Exception e) {
			log.info("IFrameWindowsProducer permission exception " + e);
			return;
		    }			
		}

		// just displays args, nothing that needs permission checking
		if (!((GeneralViewParameters) params).getTitle().equals("") && !((GeneralViewParameters) params).getSource().equals("")) {
			UIOutput.make(tofill, "title", ((GeneralViewParameters) params).getTitle());
			UIOutput.make(tofill, "toptitle", ((GeneralViewParameters) params).getTitle());
			UIOutput iframe = UIOutput.make(tofill, "iframe");
			iframe.decorate(new UIFreeAttributeDecorator("title", ((GeneralViewParameters) params).getTitle()));
			iframe.decorate(new UIFreeAttributeDecorator("src", ((GeneralViewParameters) params).getSource()));
			iframe.decorate(new UIFreeAttributeDecorator("name", ((GeneralViewParameters) params).getId()));
			iframe.decorate(new UIFreeAttributeDecorator("id", ((GeneralViewParameters) params).getId()));
			iframe.decorate(new UIFreeAttributeDecorator("allow", ServerConfigurationService.getBrowserFeatureAllowString()));
			String source = ((GeneralViewParameters) params).getSource();
			if (!IframeUrlUtil.isLocalToSakai(source, ServerConfigurationService.getServerUrl())) {
				iframe.decorate(new UIFreeAttributeDecorator("class", "portletMainIframe sakai-iframe-force-light"));
			}
		}
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	/*
	 * public List reportNavigationCases() { List<NavigationCase> togo = new
	 * ArrayList<NavigationCase>(); togo.add(new NavigationCase(null, new
	 * SimpleViewParameters(ShowPageProducer.VIEW_ID))); togo.add(new NavigationCase("success", new
	 * SimpleViewParameters(ReloadPageProducer.VIEW_ID))); togo.add(new NavigationCase("cancel", new
	 * SimpleViewParameters(ShowPageProducer.VIEW_ID))); togo.add(new NavigationCase("failure", new
	 * SimpleViewParameters(ReloadPageProducer.VIEW_ID)));
	 * 
	 * return togo; }
	 */

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

}
