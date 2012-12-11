/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
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

import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.SimplePage;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;                                                                                          
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;                                                               
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class RemovePageProducer implements ViewComponentProducer {
	public static final String VIEW_ID = "RemovePage";

	public String getViewID() {
		return VIEW_ID;
	}

	private SimplePageBean simplePageBean;

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public MessageLocator messageLocator;

	public void setMessageLocator(MessageLocator s) {
		messageLocator = s;
	}

	public LocaleGetter localeGetter;                                                                                             

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

                if (!simplePageBean.canEditPage())
		    return;

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		simplePageBean.removePage();

		UIOutput.make(tofill, "alert-text", messageLocator.getMessage("simplepage.remove_page_done"));

	}

}