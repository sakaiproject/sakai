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

import java.net.URLEncoder;

import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.tool.cover.SessionManager;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.BltiTool;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.component.cover.ServerConfigurationService;             

import org.sakaiproject.lessonbuildertool.service.BltiEntity;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;                                                                                          
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIVerbatim;
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

/**
 * Creates a list of LTI Content Items for the user to choose from. Their choice will be added
 * to the end of the list of items on this page.
 * 
 * @author Charles Severance <csev@umich.edu>
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
public class BltiPickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	public static final String VIEW_ID = "BltiPicker";

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
        private BltiEntity bltiEntity;
	public MessageLocator messageLocator;
        public LocaleGetter localeGetter;                                                                                             

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

    	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

    	public void setBltiEntity(BltiEntity l) {
		bltiEntity = l;
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
			System.out.println("QuizPicker permission exception " + e);
			return;
		    }
		}

	        Integer bltiToolId = ((GeneralViewParameters)viewparams).addTool;
		BltiTool bltiTool = null;
		if (bltiToolId == -1)
		    bltiToolId = null;
		else
		    bltiTool = simplePageBean.getBltiTool(bltiToolId);

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		//errorMessage=&id=&title=&source=&backPath=&sendingPage=92&path=&clearAttr=&recheck=&itemId=-1&returnView=

		Long itemId = ((GeneralViewParameters) viewparams).getItemId();

		simplePageBean.setItemId(itemId);

		if (bltiTool != null)
		    UIOutput.make(tofill, "mainhead", bltiTool.title);
		else
		    UIOutput.make(tofill, "mainhead", messageLocator.getMessage("simplepage.blti.chooser"));

		// here is a URL to return to this page
		String comeBack = ServerConfigurationService.getToolUrl()+ "/" + ToolManager.getCurrentPlacement().getId() + "/BltiPicker?" +
		    ((GeneralViewParameters) viewparams).getSendingPage() + "&itemId=" + itemId + (bltiTool == null? "" : "&addTool=" + bltiToolId);
		if ( bltiEntity instanceof BltiEntity ) ( (BltiEntity) bltiEntity).setReturnUrl(comeBack);

		// here is a URL to return to the main lesson builder page
		// System.out.println("/portal/tool/" + ToolManager.getCurrentPlacement().getId() + "/ShowPage?");

		if (simplePageBean.canEditPage()) {

			SimplePage page = simplePageBean.getCurrentPage();

		        String currentItem = null; // default value, normally current
			if (itemId != null && itemId != -1) {
			    SimplePageItem i = simplePageToolDao.findItem(itemId);
			    if (i == null)
				return;
			    // trying to hack on item not on this page
			    if (i.getPageId() != page.getPageId())
				return;
			    currentItem = i.getSakaiId();
			}

			List<UrlItem> createLinks = bltiEntity.createNewUrls(simplePageBean, bltiToolId);
			UrlItem mainLink = null;
			int toolcount = 0;
			for (UrlItem createLink: createLinks) {
			    if (createLink.Url.indexOf("panel=Main") >= 0) {
				mainLink = createLink;
				continue;
			    }
			    toolcount = 1;
			    UIBranchContainer link = UIBranchContainer.make(tofill, "blti-create:");
			    GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
			    view.setSendingPage(((GeneralViewParameters) viewparams).getSendingPage());
			    view.setItemId(((GeneralViewParameters) viewparams).getItemId());
			    view.setSource(createLink.Url);
			    view.setReturnView(VIEW_ID);
			    view.setTitle(messageLocator.getMessage("simplepage.return_blti"));
			    UIInternalLink.make(link, "blti-create-link", (bltiTool == null ? createLink.label : bltiTool.addText), view);
			}
			
			if (bltiTool != null) {
			    if (bltiTool.description != null)
				UIOutput.make(tofill, "blti-tools-text", bltiTool.description);
			    if (bltiTool.addInstructions != null)
				UIVerbatim.make(tofill, "blti-add-instructions", bltiTool.addInstructions);
			} else if (bltiTool == null && toolcount > 0) 
			    UIOutput.make(tofill, "blti-tools-text", messageLocator.getMessage("simplepage.blti.tools.text"));

			// only show manage link if we aren't simulating a native tool
			if (bltiTool == null) {
			    UIOutput.make(tofill, "manageblti");
			if (mainLink != null) {
			    GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
			    view.setSendingPage(((GeneralViewParameters) viewparams).getSendingPage());
			    view.setItemId(((GeneralViewParameters) viewparams).getItemId());
			    view.setSource(mainLink.Url);
			    view.setReturnView(VIEW_ID);
			    view.setTitle(messageLocator.getMessage("simplepage.return_blti"));
			    UIInternalLink.make(tofill, "blti-main-link", mainLink.label , view);
			}
			}

			UIForm form = UIForm.make(tofill, "blti-picker");
			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			if (sessionToken != null)
			    UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());

			List<LessonEntity> plist = bltiEntity.getEntitiesInSite(null, bltiToolId);

			if (plist == null || plist.size() < 1) {
			    UIOutput.make(tofill, "no-blti-items");
			    if (bltiToolId == null)
				UIOutput.make(tofill, "no-blti-items-text", messageLocator.getMessage("simplepage.no_blti_items"));
			    else
				UIOutput.make(tofill, "no-blti-items-text", messageLocator.getMessage("simplepage.no_blti_native"));
			} else
			    UIOutput.make(tofill, "select-blti-text", messageLocator.getMessage("simplepage.select_blti.text"));

			ArrayList<String> values = new ArrayList<String>();

			if (plist != null)
			    for (LessonEntity blti: plist) {
				values.add(blti.getReference());
			    }

			// if no current item, use first
			if (currentItem == null && plist != null && plist.size() > 0)
			    currentItem = plist.get(0).getReference();

			UISelect select = UISelect.make(form, "blti-span", values.toArray(new String[1]), "#{simplePageBean.selectedBlti}", currentItem);
			if (plist != null)
			    for (LessonEntity a : plist) {

				UIBranchContainer row = UIBranchContainer.make(form, "blti:", String.valueOf(plist.indexOf(a)));

				UISelectChoice.make(row, "select", select.getFullID(), plist.indexOf(a)).
				    decorate(new UIFreeAttributeDecorator("title", a.getTitle()));

				UILink.make(row, "link", a.getTitle(), a.getUrl());
			    }

			UIInput.make(form, "item-id", "#{simplePageBean.itemId}");

			if (plist != null && plist.size() > 0) {

			    if (false) {
				// this code works, but I think the resulting UI is too complex
			    UIOutput.make(form, "format-explain", messageLocator.getMessage("simplepage.format.heading"));

			    UISelect radios = UISelect.make(form, "format-select",
						new String[] {"window", "inline", "page"},
							    "#{simplePageBean.format}", "page");
			    UISelectChoice.make(form, "format-window", radios.getFullID(), 0);
			    UISelectChoice.make(form, "format-inline", radios.getFullID(), 1);
			    UISelectChoice.make(form, "format-page", radios.getFullID(), 2);
			    }

			    UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.addBlti}");
			}
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");

		}
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("failure", new SimpleViewParameters(BltiPickerProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return togo;
	}
}
