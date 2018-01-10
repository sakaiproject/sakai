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
import java.text.DateFormat;

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
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * Creates a window for the user to choose which assignment to add to the page.
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
@Slf4j
public class AssignmentPickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	public static final String VIEW_ID = "AssignmentPicker";

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private LessonEntity assignmentEntity;
	public MessageLocator messageLocator;
        public LocaleGetter localeGetter;                                                                                             

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

    	public void setAssignmentEntity(LessonEntity l) {
		assignmentEntity = l;
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

			String assignId = null; // default, normally current

			// if itemid is null, we'll append to current page, so it's ok
			if (itemId != null && itemId != -1) {
			    SimplePageItem currentItem = simplePageToolDao.findItem(itemId);
			    if (currentItem == null)
				return;
			    // trying to hack on item not on this page
			    if (currentItem.getPageId() != page.getPageId())
				return;
			    assignId = currentItem.getSakaiId();
			}
			
			List<UrlItem> createLinks = assignmentEntity.createNewUrls(simplePageBean);
			int toolNum = 0;
			for (UrlItem createLink: createLinks) {
			    UIBranchContainer link = UIBranchContainer.make(tofill, "assignment-create:");
			    GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
			    view.setSendingPage(((GeneralViewParameters) viewparams).getSendingPage());
			    view.setId(Long.toString(((GeneralViewParameters) viewparams).getItemId()));
			    view.setSource("CREATE/ASSIGN/" + (toolNum++));
			    view.setReturnView(VIEW_ID);
			    view.setTitle(messageLocator.getMessage("simplepage.return_assignment"));
			    view.setAddBefore(((GeneralViewParameters) viewparams).getAddBefore());
			    UIInternalLink.make(link, "assignment-create-link", createLink.label , view);
			}

			UIForm form = UIForm.make(tofill, "assignment-picker");
			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			if (sessionToken != null)
			    UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());

			if (createLinks.size() == 0) {
			    log.info("creatlinks " + createLinks.size());
			    UIOutput.make(tofill, "error-div");
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_assignment_tools"));
			    UICommand.make(tofill, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			    return;
			}
			List<LessonEntity> alist = assignmentEntity.getEntitiesInSite(simplePageBean);

			if (alist == null || alist.size() < 1) {
			    UIOutput.make(tofill, "error-div");
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_assignments"));
			    UICommand.make(tofill, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			    return;
			}

			ArrayList<String> values = new ArrayList<String>();
			for (LessonEntity assignment: alist) {
			    values.add(assignment.getReference());
			}

			// if no current item, use first
			if (assignId == null)
			    assignId = alist.get(0).getReference();

			UISelect select = UISelect.make(form, "assignment-span", values.toArray(new String[1]), "#{simplePageBean.selectedAssignment}", assignId);
			for (LessonEntity a : alist) {

				UIBranchContainer row = UIBranchContainer.make(form, "assignment:", String.valueOf(alist.indexOf(a)));

				UISelectChoice.make(row, "select", select.getFullID(), alist.indexOf(a)).
				    decorate(new UIFreeAttributeDecorator("title", a.getTitle()));
				UILink.make(row, "link", a.getTitle(), a.getUrl());
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, new ResourceLoader().getLocale());		
				df.setTimeZone(TimeService.getLocalTimeZone());
				if (a.getDueDate() != null)
				    UIOutput.make(row, "due", df.format(a.getDueDate()));
				else
				    UIOutput.make(row, "due", "");

			}

			UIInput.make(form, "item-id", "#{simplePageBean.itemId}");
			UIInput.make(form, "add-before", "#{simplePageBean.addBefore}", ((GeneralViewParameters) viewparams).getAddBefore());

			UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.addAssignment}");
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
		}
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("failure", new SimpleViewParameters(AssignmentPickerProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return togo;
	}
}
