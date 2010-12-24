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

import org.sakaiproject.util.FormattedText;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.Status;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Uses the Fluid reorderer to reorder elements on the page.
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
public class ReorderProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private ShowPageProducer showPageProducer;

	public MessageLocator messageLocator;
	public static final String VIEW_ID = "Reorder";

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters params, ComponentChecker checker) {
		SimplePage currentPage = simplePageBean.getCurrentPage();

		if (((GeneralViewParameters) params).getSendingPage() != -1) {
		    // will fail if page not in this site
		    // security then depends upon making sure that we only deal with this page
		    try {
			simplePageBean.updatePageObject(((GeneralViewParameters) params).getSendingPage());
		    } catch (Exception e) {
			System.out.println("Reorder permission exception " + e);
			return;
		    }
		}

		// doesn't use any item parameters, so this should be safe

		if (simplePageBean.canEditPage()) {
			SimplePage page = simplePageBean.getCurrentPage();
			List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(page.getPageId());

			UIOutput.make(tofill, "intro", messageLocator.getMessage("simplepage.reorder_header"));
			UIOutput.make(tofill, "instructions", messageLocator.getMessage("simplepage.reorder_instructions"));

			UIOutput.make(tofill, "itemTable");
			for (SimplePageItem i : items) {
				if (i.getType() == 7) {
					i.setType(1); // Temporarily change multimedia to standard resource
					// so that links work properly.
				}

				UIContainer row = UIBranchContainer.make(tofill, "item:");
				UIOutput.make(row, "seq", String.valueOf(i.getSequence()));
				UIOutput.make(row, "description", i.getDescription());
				if (i.getType() == 5) {
					String text = FormattedText.convertFormattedTextToPlaintext(i.getHtml());
					if (i.getHtml().length() > 100) {
					    UIOutput.make(row, "text-snippet", text);
					} else {
					    UIOutput.make(row, "text-snippet", text);
					}
				} else {
				    showPageProducer.makeLink(row, "link", i, simplePageBean, simplePageToolDao, messageLocator, true, currentPage, false, Status.NOT_REQUIRED);
				}
			}

			UIForm form = UIForm.make(tofill, "form");
			UIInput.make(form, "order", "#{simplePageBean.order}");
			UICommand.make(form, "save", "Save", "#{simplePageBean.reorder}");
			UICommand.make(form, "cancel", "Cancel", "#{simplePageBean.cancel}");
		}
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
		this.simplePageToolDao = simplePageToolDao;
	}

	public void setShowPageProducer(ShowPageProducer p) {
		this.showPageProducer = p;
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("success", new SimpleViewParameters(ReloadPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

		return togo;
	}
}
