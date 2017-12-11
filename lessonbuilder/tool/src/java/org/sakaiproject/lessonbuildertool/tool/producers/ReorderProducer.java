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
import java.util.stream.Collectors;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.util.FormattedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.Status;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;

import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
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
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.FormattedText;


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
	@Setter private FormattedText formattedText;
	@Setter private SessionManager sessionManager;

	public MessageLocator messageLocator;
	public LocaleGetter localeGetter;                                                                                             
	public static final String VIEW_ID = "Reorder";

	@Setter
	private Map<String,String> imageToMimeMap;

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

		ToolSession toolSession = sessionManager.getCurrentToolSession();
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

			List<Long> moreItemIds = new ArrayList<>();
			if (secondPage != null) {
			    List<SimplePageItem> moreItems = simplePageToolDao.findItemsOnPage(secondPageId);

			    if (moreItems != null && moreItems.size() > 0) {
				items.add(null); //marker
				while(moreItems.size() > 0 && moreItems.get(0).getSequence() <= 0) {
				    moreItems.remove(0);
				}
				moreItemIds = moreItems.stream().collect(Collectors.mapping(SimplePageItem::getId, Collectors.toList()));
				items.addAll(moreItems);
			    }
			} else
			    items.add(null); // if no 2nd page, put marker at the end

			UIOutput.make(tofill, "intro", messageLocator.getMessage("simplepage.reorder_header"));
			UIOutput.make(tofill, "instructions", messageLocator.getMessage("simplepage.reorder_instructions"));

			UIOutput.make(tofill, "itemTable");

			boolean second = false;

			UIBranchContainer sectionContainer = null;
			UIBranchContainer columnContainer = null;

			for (SimplePageItem i : items) {

				if (i == null) {
				    continue;
				}

				if (moreItemIds.contains(i.getId())) {
					second = true;
				} else {
					second = false;
				}

				if (i.getType() == SimplePageItem.BREAK) {
					if ("section".equals(i.getFormat()) || sectionContainer == null) {
						sectionContainer = UIBranchContainer.make(tofill, "sectionContainer:");
					}
					columnContainer = UIBranchContainer.make(sectionContainer, "columnContainer:");
				} else if (sectionContainer == null) {
					sectionContainer = UIBranchContainer.make(tofill, "sectionContainer:");
					columnContainer = UIBranchContainer.make(sectionContainer, "columnContainer:");
				}

				String subtype = null;
				if (i.getType() == 7) {
					i.setType(1); // Temporarily change multimedia to standard resource
								  // so that links work properly.
					subtype = i.getAttribute("multimediaDisplayType");
				}

				UIContainer row = UIBranchContainer.make(columnContainer, "item:");
				UIContainer modalContainer = UIBranchContainer.make(tofill, "modal:");

				// * prefix indicates items are from the other page, and have to be copied.
				UIOutput.make(row, "seq", (second ? "*" : "") +
					                   String.valueOf(i.getSequence()));

				UIOutput icon = UIOutput.make(row, "icon");
				icon.decorate(this.getImageSourceDecorator(i));

				if (i.getType() == SimplePageItem.TEXT) {
					String text = FormattedText.convertFormattedTextToPlaintext(i.getHtml());
					if (StringUtils.isBlank(text)) {
						text = messageLocator.getMessage("simplepage.text.item");
					} else if (StringUtils.length(text) > 50) {
						text = StringUtils.substring(text, 0, 50) + "...";
					}
				    UIOutput.make(row, "text-snippet2", text);
					UIOutput previewLink = UIOutput.make(row, "previewlink");
					previewLink.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.preview.link")));
					previewLink.decorate(new UIFreeAttributeDecorator("href", "#preview" + i.getId()));
					UIOutput.make(modalContainer, "previewmodal").decorate(new UIFreeAttributeDecorator("id", "preview" + i.getId()));
					UIVerbatim.make(modalContainer, "previewcontent", i.getHtml());
				} else if ("1".equals(subtype)) {
				    // embed code, nothing useful to show
				    UIOutput.make(row, "text-snippet2", messageLocator.getMessage("simplepage.embedded-video"));
					UIOutput previewLink = UIOutput.make(row, "previewlink");
					previewLink.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.preview.link")));
					previewLink.decorate(new UIFreeAttributeDecorator("href", "#preview" + i.getId()));
					UIOutput.make(modalContainer, "previewmodal").decorate(new UIFreeAttributeDecorator("id", "preview" + i.getId()));
					UIVerbatim.make(modalContainer, "previewcontent", i.getAttribute("multimediaEmbedCode"));
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
				    if ("section".equals(i.getFormat())) {
				    	String sectionName = (second ? ">> " : "") + messageLocator.getMessage("simplepage.break-here") + (StringUtils.isBlank(i.getName()) ? "" : " (" + i.getName() + ")");
				    	UIOutput.make(row, "section-label", sectionName);
						row.decorate(new UIStyleDecorator("active"));
					} else {
						String text = messageLocator.getMessage("simplepage.break-column-here");
						UIOutput textSnippet = UIOutput.make(row, "text-snippet", text);
						textSnippet.decorate(new UIStyleDecorator("column-section-break-text"));
				    	row.decorate(new UIStyleDecorator("list-group-item-info"));
					}
				} else {
					String description = i.getDescription();
					if (StringUtils.isNotBlank(description) && i.getType() != SimplePageItem.COMMENTS && i.getType() != SimplePageItem.STUDENT_CONTENT) {
						description = " | " + description;
						if (StringUtils.length(description) > 50) {
							description = StringUtils.substring(description, 0, 50) + "...";
						}
					}
				    UIOutput.make(row, "description", description);
				    showPageProducer.makeLink(row, "link", i, simplePageBean, simplePageToolDao, messageLocator, true, currentPage, false, Status.NOT_REQUIRED);
				}
				UIComponent del = UIOutput.make(row, "dellink").decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.delete")));
				if (i.getType() == SimplePageItem.BREAK && "section".equals(i.getFormat())) {
					del.decorate(new UIFreeAttributeDecorator("style", "display:none;"));
					UIOutput.make(row, "dellinkoptions").decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.delete")));
				}

				if(second) {
					if (i.getType() != SimplePageItem.BREAK) {
						UIOutput.make(row, "import-indicator");
						row.decorate(new UIStyleDecorator("list-group-item-warning"));
					}
				}
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
			Object sessionToken = sessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			if (sessionToken != null)
			    UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());

			if (secondPageId != null)
			    UIInput.make(form, "otherpage", "#{simplePageBean.selectedEntity}", secondPageId.toString());
			UIInput.make(form, "order", "#{simplePageBean.order}");
			UIInput.make(form, "section", "#{simplePageBean.selectedSectionForColumn}");
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
		togo.add(new NavigationCase("preview", new SimpleViewParameters(ReorderProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

		return togo;
	}

	private UIStyleDecorator getImageSourceDecorator(SimplePageItem pageItem) {

		switch (pageItem.getType()) {
			case SimplePageItem.FORUM:
				return new UIStyleDecorator("icon-sakai--sakai-forums darkblueLessonsIcon");
			case SimplePageItem.ASSIGNMENT:
				return new UIStyleDecorator("icon-sakai--sakai-assignment-grades darkblueLessonsIcon");
			case SimplePageItem.ASSESSMENT:
				return new UIStyleDecorator("icon-sakai--sakai-samigo darkblueLessonsIcon");
			case SimplePageItem.QUESTION:
				return new UIStyleDecorator("fa fa-question darkgrayLessonsIcon");
			case SimplePageItem.COMMENTS:
				return new UIStyleDecorator("fa fa-commenting darkgrayLessonsIcon");
			case SimplePageItem.BLTI:
				return new UIStyleDecorator("fa fa-globe darkgrayLessonsIcon");
			case SimplePageItem.PAGE:
				return new UIStyleDecorator("fa fa-folder-open-o darkgrayLessonsIcon");
			case SimplePageItem.CHECKLIST:
				return new UIStyleDecorator("fa fa-list darkgrayLessonsIcon");
			case SimplePageItem.URL:
				return new UIStyleDecorator("fa fa-external-link blueLessonsIcon");
			case SimplePageItem.STUDENT_CONTENT:
				return new UIStyleDecorator("fa fa-user darkgrayLessonsIcon");
			case SimplePageItem.PEEREVAL:
				return new UIStyleDecorator("fa fa-users darkgrayLessonsIcon");
			case SimplePageItem.RESOURCE:
				return getImageSourceDecoratorFromMimeType(pageItem);
			case SimplePageItem.MULTIMEDIA:
				return getImageSourceDecoratorFromMimeType(pageItem);
			case SimplePageItem.TEXT:
				return new UIStyleDecorator("fa fa-font darkgrayLessonsIcon");
			default:
				return new UIStyleDecorator("");
		}
	}

	private UIStyleDecorator getImageSourceDecoratorFromMimeType(SimplePageItem pageItem) {

		String mimeType = pageItem.getHtml();

		if(SimplePageItem.TEXT == pageItem.getType()) {
			mimeType = "text/html";
		} else if("application/octet-stream".equals(mimeType)) {
			// OS X reports octet stream for things like MS Excel documents.
			// Force a mimeType lookup so we get a decent icon.
			mimeType = null;
		}

		if (mimeType == null || mimeType.equals("")) {
			String s = pageItem.getSakaiId();
			int j = s.lastIndexOf(".");
			if (j >= 0)
				s = s.substring(j+1);
			mimeType = contentTypeImageService.getContentType(s);
		}

		String src = null;

		src = imageToMimeMap.get(mimeType);

		if (src == null) {
			src = "fa-file-o darkgrayLessonsIcon";
		}

		return new UIStyleDecorator("fa " + src);
	}

	@Setter
	private ContentTypeImageService contentTypeImageService;


}
