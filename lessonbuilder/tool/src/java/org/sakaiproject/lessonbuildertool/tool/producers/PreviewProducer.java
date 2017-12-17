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

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;                                                                                          
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;                                                               
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.tool.api.ToolManager;

/**
 * 
 * Lightweight preview to help user identify the page. Based on ReorderProducer. Keep them synced
 * @author Charles Hedrick <hedrick@rutgers.edu
 * 
 */
@Slf4j
public class PreviewProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private ShowPageProducer showPageProducer;
	private ToolManager toolManager;

	public MessageLocator messageLocator;
	public LocaleGetter localeGetter;                                                                                             

	public static final String VIEW_ID = "Preview";

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters params, ComponentChecker checker) {

		UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		SimplePage currentPage = simplePageBean.getCurrentPage();

		// we specifically can't update the current page, because we
		// want to be able to preview stuff without affecting current state.
		// so we have to do permission check
		Long currentPageId = currentPage.getPageId();
		if (((GeneralViewParameters) params).getSendingPage() != -1) {
		    currentPageId = ((GeneralViewParameters) params).getSendingPage();
		}

		SimplePage page = simplePageToolDao.getPage(currentPageId);
		String siteId = toolManager.getCurrentPlacement().getContext();

		// page should always be in this site, or someone is gaming us                                                   
		if (!page.getSiteId().equals(siteId)) {
		    log.info("attempt to preview page not in the site");
		    return;
		}

		UIOutput.make(tofill, "title", messageLocator.getMessage("simplepage.preview").replace("{}", page.getTitle()));
		UIOutput.make(tofill, "title2", messageLocator.getMessage("simplepage.preview").replace("{}", page.getTitle()));

		if (simplePageBean.canEditPage()) {
			List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(currentPageId);

			if (items.size() == 0) {
			    UIOutput.make(tofill, "message", messageLocator.getMessage("simplepage.noitems_error_user"));
			} else {

			    UIOutput.make(tofill, "itemTable");
			    for (SimplePageItem i : items) {
				if (i.getType() == 7) {
					i.setType(1); // Temporarily change multimedia to standard resource
					// so that links work properly.
				}

				UIContainer row = UIBranchContainer.make(tofill, "item:");
				// UIOutput.make(row, "seq", String.valueOf(i.getSequence()));
				UIOutput.make(row, "description", i.getDescription());
				if (i.getType() == 5) {
					if (i.getHtml().length() > 100) {
						UIVerbatim.make(row, "boxed", i.getHtml().substring(0, 100) + "...");
					} else {
						UIVerbatim.make(row, "boxed", i.getHtml());
					}
				} else {
					UIOutput.make(row, "text", i.getName());
				}
			    }
			}

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

	public void setToolManager(ToolManager t) {
	    this.toolManager = t;
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("success", new SimpleViewParameters(PreviewProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

		return togo;
	}
}
