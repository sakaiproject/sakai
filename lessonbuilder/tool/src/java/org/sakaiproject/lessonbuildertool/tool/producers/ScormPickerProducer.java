/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2015 Rutgers, the State University of New Jersey
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
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Creates a list of SCORM packages for the user to choose from. Their choice will be added
 * to the end of the list of items on this page.
 */
@Slf4j
public class ScormPickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	public static final String VIEW_ID = "ScormPicker";

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private LessonEntity scormEntity;
	public MessageLocator messageLocator;
	public LocaleGetter localeGetter;

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

	public void setScormEntity(LessonEntity l) {
		scormEntity = l;
	}

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		if (((GeneralViewParameters) viewparams).getSendingPage() != -1) {
		    try {
			simplePageBean.updatePageObject(((GeneralViewParameters) viewparams).getSendingPage());
		    } catch (Exception e) {
			log.info("ScormPicker permission exception {}", e.toString());
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
			SimplePageItem existingItem = null;
			if (itemId != null && itemId != -1) {
			    existingItem = simplePageToolDao.findItem(itemId);
			    if (existingItem == null)
				return;
			    // trying to hack on item not on this page
			    if (existingItem.getPageId() != page.getPageId())
				return;
			    currentItem = existingItem.getSakaiId();
			}

			if (scormEntity == null) {
			    UIOutput.make(tofill, "error-div");
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_scorm_tool"));
			    UICommand.make(tofill, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			    return;
			}

			List<UrlItem> createLinks = scormEntity.createNewUrls(simplePageBean);
			int toolNum = 0;
			for (UrlItem createLink: createLinks) {
			    UIBranchContainer link = UIBranchContainer.make(tofill, "scorm-create:");
			    GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
			    view.setSendingPage(((GeneralViewParameters) viewparams).getSendingPage());
			    view.setId(Long.toString(((GeneralViewParameters) viewparams).getItemId()));
			    view.setSource("CREATE/SCORM/" + (toolNum++));
			    view.setReturnView(VIEW_ID);
			    view.setAddBefore(((GeneralViewParameters) viewparams).getAddBefore());
			    view.setTitle(messageLocator.getMessage("simplepage.return_scorm"));
			    UIInternalLink.make(link, "scorm-create-link", createLink.label , view);
			}

			UIForm form = UIForm.make(tofill, "scorm-picker");
			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			if (sessionToken != null)
			    UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());

			List<LessonEntity> plist = scormEntity.getEntitiesInSite(simplePageBean);

			if (createLinks.size() == 0) {
			    UIOutput.make(tofill, "error-div");
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_scorm_tool"));
			    UICommand.make(tofill, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			    return;
			}

			if (plist == null || plist.size() < 1) {
			    UIOutput.make(tofill, "error-div");
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_scorm_packages"));
			    UICommand.make(tofill, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			    return;
			}

			ArrayList<String> values = new ArrayList<String>();

			for (LessonEntity s: plist) {
			    values.add(s.getReference());
			}

			// if no current item, use first
			if (currentItem == null)
			    currentItem = plist.get(0).getReference();

			UISelect select = UISelect.make(form, "scorm-span", values.toArray(new String[1]), "#{simplePageBean.selectedScorm}", currentItem);
			for (int idx = 0; idx < plist.size(); idx++) {
				LessonEntity a = plist.get(idx);
				UIBranchContainer row = UIBranchContainer.make(form, "scorm:", String.valueOf(idx));

				UISelectChoice.make(row, "select", select.getFullID(), idx).
				    decorate(new UIFreeAttributeDecorator("title", a.getTitle()));

				UILink.make(row, "link", a.getTitle(), a.getUrl());
			}

			// Display Options: default to "window"
			String currentFormat = "window";
			if (existingItem != null && existingItem.getFormat() != null && !existingItem.getFormat().isEmpty()) {
			    currentFormat = existingItem.getFormat();
			}
			UISelect formatSelect = UISelect.make(form, "format-span",
			    new String[] {"window", "inline"},
			    "#{simplePageBean.format}", currentFormat);
			UISelectChoice.make(form, "format-window", formatSelect.getFullID(), 0);
			UISelectChoice.make(form, "format-inline", formatSelect.getFullID(), 1);

			UIInput.make(form, "item-id", "#{simplePageBean.itemId}");
			UIInput.make(form, "add-before", "#{simplePageBean.addBefore}", ((GeneralViewParameters) viewparams).getAddBefore());

			UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.addScorm}");
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
		}
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("failure", new SimpleViewParameters(ScormPickerProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return togo;
	}
}
