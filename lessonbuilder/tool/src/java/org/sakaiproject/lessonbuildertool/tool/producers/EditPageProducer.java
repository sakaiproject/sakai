/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2007 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 *
 * This file was originally part of SimplePageTool, by Joshua Ryan josh@asu.edu
 * 
 * Changes for Lesson Builder are
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.cover.ContentHostingService;


/**
 * Uses an FCK editor to edit blocks of text.
 * 
 * @author Joshua Ryan josh@asu.edu alt^I
 * @author Eric Jeney <jeney@rutgers.edu>
 */
public class EditPageProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	private TextInputEvolver richTextEvolver;
	private SimplePageBean simplePageBean;

	public MessageLocator messageLocator;

	public static final String VIEW_ID = "EditPage";

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		GeneralViewParameters gparams = (GeneralViewParameters) viewparams;

		if (gparams.getSendingPage() != -1) {
		    // will fail if page not in this site
		    // security then depends upon making sure that we only deal with this page
		    try {
			simplePageBean.updatePageObject(gparams.getSendingPage());
		    } catch (Exception e) {
			System.out.println("EditPage permission exception " + e);
			return;
		    }
		}

		SimplePage page = simplePageBean.getCurrentPage();

		Long itemId = gparams.getItemId();

		if (itemId != null && itemId != -1) {
		    SimplePageItem i = simplePageBean.findItem(itemId);
		    if (i.getPageId() != page.getPageId()) {
			System.out.println("EditPage asked to edit item not in current page");
			return;
		    }
		}

   	        String editor = ServerConfigurationService.getString("wysiwyg.editor");

		if (simplePageBean.canEditPage()) {
			simplePageBean.setItemId(itemId);

			UIForm form = UIForm.make(tofill, "page_form");

			// Rich Text Input
			UIInput instructions = UIInput.make(form, "page:", "#{simplePageBean.contents}");
			instructions.decorate(new UIFreeAttributeDecorator("height", "500"));
			instructions.decorate(new UIFreeAttributeDecorator("width", "700"));

			form.parameters.add(new UIELBinding("#{simplePageBean.itemId}", itemId));

			richTextEvolver.evolveTextInput(instructions);

			UICommand.make(form, "save", "#{simplePageBean.submit}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.save_message")));

			UICommand.make(form, "cancel", "#{simplePageBean.cancel}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.cancel_message")));

			if (itemId != null && itemId != -1) {
				form.parameters.add(new UIELBinding("#{simplePageBean.itemId}", gparams.getItemId()));
				UICommand.make(form, "delete", "#{simplePageBean.deleteItem}");
				UIOutput.make(form, "delete-div");
			}
		} else {
			UIBranchContainer error = UIBranchContainer.make(tofill, "error");
			UIOutput.make(error, "message", messageLocator.getMessage("simplepage.not_available"));
		}
	}

	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
		this.richTextEvolver = richTextEvolver;
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

		return togo;
	}
}
