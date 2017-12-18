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

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;										  
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIInput;
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
import org.sakaiproject.lessonbuildertool.service.BltiEntity;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.BltiTool;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;   
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * Creates a list of LTI Content Items for the user to choose from. Their choice will be added
 * to the end of the list of items on this page.
 * 
 * @author Charles Severance <csev@umich.edu>
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
@Slf4j
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
			log.info("QuizPicker permission exception " + e);
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

		// Reach down and grab a GET parameter from ThreadLocal
		String ltiItemId = ToolUtils.getRequestParameter("ltiItemId");

		simplePageBean.setItemId(itemId);

		if (bltiTool != null)
		    UIOutput.make(tofill, "mainhead", bltiTool.title);
		else
		    UIOutput.make(tofill, "mainhead", messageLocator.getMessage("simplepage.blti.chooser"));

		// here is a URL to return to this page
		String comeBack = ToolUtils.getToolBaseUrl()+ "/" + ToolManager.getCurrentPlacement().getId() + "/BltiPicker?" +
		    ((GeneralViewParameters) viewparams).getSendingPage() + "&itemId=" + itemId + "&addBefore=" + ((GeneralViewParameters) viewparams).getAddBefore() + (bltiTool == null? "" : "&addTool=" + bltiToolId);
		if ( bltiEntity instanceof BltiEntity ) ( (BltiEntity) bltiEntity).setReturnUrl(comeBack);

		// here is a URL to return to the main lesson builder page
		// log.info("/portal/tool/" + ToolManager.getCurrentPlacement().getId() + "/ShowPage?");

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

			    UILink.make(link, "blti-create-link", (bltiTool == null ? createLink.label : bltiTool.addText), createLink.Url)
				.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.blti.config")));

			    if ( createLink.fa_icon != null ) {
				UIOutput.make(link, "blti-create-icon", "")
				    .decorate(new UIFreeAttributeDecorator("class", "fa " + createLink.fa_icon));
			    }

			    if ( createLink.search ) {
				UIOutput.make(link, "blti-search-icon");
			    }

			}
			
			if ( toolcount > 0 ) {
			    UIOutput.make(tofill, "blti-tools-text", messageLocator.getMessage("simplepage.blti.tools.text"));
			} else { 
			    UIOutput.make(tofill, "no-blti-tools");
			    UIOutput.make(tofill, "no-blti-tools-text", messageLocator.getMessage("simplepage.no_blti_tools"));
			}

			UICommand.make(tofill, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");

			// only show manage link if we aren't simulating a native tool
			if (bltiTool == null) {
			    UIOutput.make(tofill, "manageblti");
			    if (mainLink != null) {
				UILink.make(tofill, "blti-main-link", mainLink.label, mainLink.Url)
					.decorate(new UIFreeAttributeDecorator("title", mainLink.label) );
			    }
			}

			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			if ( ltiItemId != null ) {
				UIForm fb = UIForm.make(tofill, "blti-autosubmit");
				if (sessionToken != null)
					UIInput.make(fb, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
				UIInput.make(fb, "select", "simplePageBean.selectedBlti", ltiItemId);
				UIInput.make(fb, "add-before", "#{simplePageBean.addBefore}", ((GeneralViewParameters) viewparams).getAddBefore());
				UIInput.make(fb, "item-id", "#{simplePageBean.itemId}");
				UICommand.make(fb, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.addBlti}");
				UICommand.make(fb, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");

			}

			UIForm cancelform = UIForm.make(tofill, "blti-cancel");
			if (sessionToken != null)
				UIInput.make(cancelform, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
			UIInput.make(cancelform, "select", "simplePageBean.selectedBlti", ltiItemId);
			UICommand.make(cancelform, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			UICommand.make(cancelform, "cancel2", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
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
