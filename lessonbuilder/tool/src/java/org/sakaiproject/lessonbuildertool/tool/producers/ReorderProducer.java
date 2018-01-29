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

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIComponent;
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
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.Status;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.FormattedText;

/**
 * Uses the Fluid reorderer to reorder elements on the page.
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
@Slf4j
public class ReorderProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private ShowPageProducer showPageProducer;

	public MessageLocator messageLocator;
	public LocaleGetter localeGetter;                                                                                             
	public static final String VIEW_ID = "Reorder";

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters params, ComponentChecker checker) {

		if (((GeneralViewParameters) params).getSendingPage() != -1) {
		    // will fail if page not in this site
		    // security then depends upon making sure that we only deal with this page
			try {
				simplePageBean.updatePageObject(((GeneralViewParameters) params).getSendingPage());
			} catch (Exception e) {
				log.info("Reorder permission exception " + e);
				return;
			}
		}

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		ToolSession toolSession = SessionManager.getCurrentToolSession();
		String secondPageString = (String)toolSession.getAttribute("lessonbuilder.selectedpage");
		Long secondPageId = null;
		if (secondPageString != null) 
		    secondPageId = Long.parseLong(secondPageString);

		toolSession.setAttribute("lessonbuilder.selectedpage", null);

		// may have been updated by sendingpage
		SimplePage currentPage = simplePageBean.getCurrentPage();

		// doesn't use any item parameters, so this should be safe

		if (simplePageBean.canEditPage()) {

		    // make sure the order is right
		    // go to the database for reads, to make sure we get most recent item data
			simplePageToolDao.setRefreshMode();

			simplePageBean.fixorder();

			SimplePage page = simplePageBean.getCurrentPage();
			List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(page.getPageId());
			
		        SimplePage secondPage = null;
			if (secondPageId != null)
			    secondPage = simplePageBean.getPage(secondPageId);

			// are they hacking us? other page should be in the same site, or tests fail
			// The tests here will handle student pages, but the UI doesn't actually present them.

			if (secondPage != null && !secondPage.getSiteId().equals(page.getSiteId()))
			    secondPage = null;
			if (secondPage != null) {
			    if (!simplePageToolDao.canEditPage(secondPageId))
				secondPage = null;
			}
			
			// Some items are tacked onto the end automatically by setting the sequence to
			// something less than or equal to 0.  This takes them out of the Reorder tool.
			while(items.size() > 0 && items.get(0).getSequence() <= 0) {
				items.remove(0);
			}

			if (secondPage != null) {
			    List<SimplePageItem> moreItems = simplePageToolDao.findItemsOnPage(secondPageId);

			    if (moreItems != null && moreItems.size() > 0) {
				items.add(null); //marker
				while(moreItems.size() > 0 && moreItems.get(0).getSequence() <= 0) {
				    moreItems.remove(0);
				}
				items.addAll(moreItems);
			    }
			} else
			    items.add(null); // if no 2nd page, put marker at the end

			UIOutput.make(tofill, "intro", messageLocator.getMessage("simplepage.reorder_header"));
			UIOutput.make(tofill, "instructions", messageLocator.getMessage("simplepage.reorder_instructions"));

			UIOutput.make(tofill, "itemTable");

			boolean second = false;
			for (SimplePageItem i : items) {

				if (i == null) {
				    // marker between used and not used
				    UIContainer row = UIBranchContainer.make(tofill, "item:");
				    UIOutput.make(row, "seq", "---").decorate(new UIFreeAttributeDecorator("class", "marker"));
				    UIOutput.make(row, "text-snippet", messageLocator.getMessage(secondPageId == null ? "simplepage.reorder-belowdelete" : "simplepage.reorder-aboveuse"));
				    second = true;
				    continue;
				}

				String subtype = null;
				if (i.getType() == 7) {
					i.setType(1); // Temporarily change multimedia to standard resource
								  // so that links work properly.
					subtype = i.getAttribute("multimediaDisplayType");
				}

				UIContainer row = UIBranchContainer.make(tofill, "item:");
				// * prefix indicates items are from the other page, and have to be copied.
				UIOutput.make(row, "seq", (second ? "*" : "") +
					                   String.valueOf(i.getSequence()));

				if (i.getType() == 5) {
				    if (i.getAttribute("isFolder")!=null && i.getAttribute("isFolder").equals("true")){
					    UIOutput.make(row, "text-snippet", messageLocator.getMessage("simplepage.resources-snippet"));
				    }
				    else {
					    String text = FormattedText.convertFormattedTextToPlaintext(i.getHtml());
					    if (text.length() > 100)
						    text = text.substring(0, 100);
					    UIOutput.make(row, "text-snippet", text);
				    }
				} else if (SimplePageItem.ANNOUNCEMENTS == i.getType()) {
					UIOutput.make(row, "text-snippet", messageLocator.getMessage("simplepage.announcements-snippet"));
				} else if (SimplePageItem.FORUM_SUMMARY == i.getType()) {
					UIOutput.make(row, "text-snippet", messageLocator.getMessage("simplepage.forums-snippet"));
				} else if (SimplePageItem.TWITTER == i.getType()) {
					UIOutput.make(row, "text-snippet", messageLocator.getMessage("simplepage.twitter-snippet"));
				} else if (SimplePageItem.RESOURCE_FOLDER == i.getType()) {
					UIOutput.make(row, "text-snippet", messageLocator.getMessage("simplepage.resources-snippet"));
				} else if (SimplePageItem.CALENDAR == i.getType()) {
					UIOutput.make(row, "text-snippet", messageLocator.getMessage("simplepage.calendar-snippet"));
				} else if ("1".equals(subtype)) {
				    // embed code, nothing useful to show
				    UIOutput.make(row, "text-snippet", messageLocator.getMessage("simplepage.embedded-video"));
				} else if ("3".equals(subtype)) {
				    // oembed. use the URL
				    UILink.make(row, "link", i.getAttribute("multimediaUrl"), i.getAttribute("multimediaUrl"));
				} else if (i.getType() == SimplePageItem.QUESTION) {
				    String text = i.getAttribute("questionText");
				    if (text == null)
					text = messageLocator.getMessage("simplepage.questionName");
				    if (text.length() > 100)
					text = text.substring(0,100);
				    UIOutput.make(row, "text-snippet", text);
				} else if (i.getType() == SimplePageItem.BREAK) {
				    String text = null;
				    if ("section".equals(i.getFormat()))
					text = messageLocator.getMessage("simplepage.break-here");
				    else 
					text = messageLocator.getMessage("simplepage.break-column-here");
				    UIOutput.make(row, "text-snippet", text);
				} else {
				    UIOutput.make(row, "description", i.getDescription());
				    showPageProducer.makeLink(row, "link", i, simplePageBean, simplePageToolDao, messageLocator, true, currentPage, false, Status.NOT_REQUIRED);
				}
				UIComponent del = UIOutput.make(row, "dellink").decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.delete")));
				if (second)
				    del.decorate(new UIFreeAttributeDecorator("style", "display:block"));
			}


			// don't offer to add from other page if we already have second page items
			// our bookkeeping can't keep track of more than one extra page
			if(currentPage.getOwner() == null && secondPageId == null) {
			    GeneralViewParameters view = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
			    view.setReturnView("reorder"); // flag to pagepicker that it needs to come back
			    UIOutput.make(tofill, "subpage-div");
			    UIInternalLink.make(tofill, "subpage-choose", messageLocator.getMessage("simplepage.reorder-addpage"), view);
			    view.setSendingPage(currentPage.getPageId());
			}

			UIForm form = UIForm.make(tofill, "form");
			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			if (sessionToken != null)
			    UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());

			if (secondPageId != null)
			    UIInput.make(form, "otherpage", "#{simplePageBean.selectedEntity}", secondPageId.toString());
			UIInput.make(form, "order", "#{simplePageBean.order}");
			UICommand.make(form, "save", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.reorder}");
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel_message"), "#{simplePageBean.cancel}");
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
