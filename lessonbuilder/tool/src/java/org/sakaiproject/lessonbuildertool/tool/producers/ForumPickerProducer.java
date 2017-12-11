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

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIInternalLink;
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
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Creates a window for the user to choose which forum topic to add to the page.
 * 
 * @author Charles Hedrick <jeney@rutgers.edu>
 * 
 */
@Slf4j
public class ForumPickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	public static final String VIEW_ID = "ForumPicker";

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
        private LessonEntity forumEntity = null;
	public MessageLocator messageLocator;
	public LocaleGetter localeGetter;                                                                                             

        public void setForumEntity(Object e) {
	    forumEntity = (LessonEntity)e;
        }

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		if (((GeneralViewParameters) viewparams).getSendingPage() != -1) {
		    // will fail if page not in this site
		    // security then depends upon making sure that we only deal with this page
		    try {
			simplePageBean.updatePageObject(((GeneralViewParameters) viewparams).getSendingPage());
		    } catch (Exception e) {
			log.info("AssignmentPicker permission exception " + e);
			return;
		    }
		}

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		Long itemId = ((GeneralViewParameters) viewparams).getItemId();

		simplePageBean.setItemId(itemId);

		if (simplePageBean.canEditPage()) {
		        
			SimplePage page = simplePageBean.getCurrentPage();

		        String currentItem = null; // default value, normally current
			// if itemid is null, we'll append to current page, so it's ok
			if (itemId != null && itemId != -1) {
			    SimplePageItem i = simplePageToolDao.findItem(itemId);
			    if (i == null)
				return;
			    // trying to hack on item not on this page
			    if (i.getPageId() != page.getPageId())
				return;
			    currentItem = i.getSakaiId();
			}

			List<UrlItem> createLinks = forumEntity.createNewUrls(simplePageBean);
			int toolNum = 0;
			for (UrlItem createLink: createLinks) {
			    UIBranchContainer link = UIBranchContainer.make(tofill, "forum-create:");
			    GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
			    view.setSendingPage(((GeneralViewParameters) viewparams).getSendingPage());
			    view.setId(Long.toString(((GeneralViewParameters) viewparams).getItemId()));
			    view.setSource("CREATE/FORUM/" + (toolNum++));
			    view.setReturnView(VIEW_ID);
			    view.setAddBefore(((GeneralViewParameters) viewparams).getAddBefore());
			    view.setTitle(messageLocator.getMessage("simplepage.return_forum"));
			    UIInternalLink.make(link, "forum-create-link", createLink.label , view);
			}

			List<LessonEntity> topics = forumEntity.getEntitiesInSite();

			UIForm form = UIForm.make(tofill, "forum-picker");
			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			if (sessionToken != null)
			    UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());

			if (createLinks.size() == 0) {
			    UIOutput.make(tofill, "error-div");
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_forum_tools"));
			    UICommand.make(tofill, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			    return;
			}

			ArrayList<String> values = new ArrayList<String>();
			for (LessonEntity topic: topics)
				values.add(topic.getReference());

			if (values.size() < 1) {
			    UIOutput.make(tofill, "error-div");
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_topics"));
			    UICommand.make(tofill, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			    return;
			}

			// if no current item, use first
			if (currentItem == null) {
			    for (LessonEntity topic: topics)
				if (topic.isUsable()) {
				    currentItem = topic.getReference();
				    break;
				}
			}

			if (currentItem == null) {
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_topics"));
			    return;
			}
			    
			UISelect select = UISelect.make(form, "forum-span", values.toArray(new String[1]), "#{simplePageBean.selectedEntity}", currentItem);
			for (LessonEntity topic: topics) {

				UIBranchContainer row = UIBranchContainer.make(form, "forum:", String.valueOf(topics.indexOf(topic)));

				if (topic.isUsable()) {
				    // this is the right code:
				    // String titleTemplate = messageLocator.getMessage("simplepage.forum.title." + (topic.getLevel() == 2 ? "topic" : "forum"));
				    // to avoid adding a string this late in the cycle for 11, use this. Only real disadvantage is for languages where colon isn't right.
				    String titleTemplate = messageLocator.getMessage("simplepage.cc-default" + (topic.getLevel() == 2 ? "topic" : "forum")) + ": {}";
				    String title = titleTemplate.replace("{}", topic.getTitle());
				    UISelectChoice.make(row, "select", select.getFullID(), topics.indexOf(topic)).
					decorate(new UIFreeAttributeDecorator("title", title)).
					decorate(new UIStyleDecorator(topic.getLevel() == 2 ? "forumTopic" : "forumForum"));

				    UILink.make(row, "link", title, topic.getUrl());
				} else {
				    UIOutput.make(row, "name", topic.getTitle());
				}

			}

			UIInput.make(form, "item-id", "#{simplePageBean.itemId}");
			UIInput.make(form, "add-before", "#{simplePageBean.addBefore}", ((GeneralViewParameters) viewparams).getAddBefore());

			UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.addForum}");
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");

		}

	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("failure", new SimpleViewParameters(ForumPickerProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return togo;
	}
}
