/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles R. Severance
 *
 * Copyright (c) 2015 Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Map;
import java.util.Properties;

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
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.lti.api.LTIService;
import org.tsugi.lti2.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.Placement;

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
 * 
 */
public class LtiImportItemProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	private static final Logger log = LoggerFactory.getLogger(LtiImportItemProducer.class);
	public static final String VIEW_ID = "LtiImportItem";

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	public MessageLocator messageLocator;
	public LocaleGetter localeGetter;
	private LTIService ltiService = null;
	private ToolManager toolManager = null;


	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

	public void setLtiService(LTIService service) {
		ltiService = service;
	}

	public void setToolManager(ToolManager service) {
		toolManager = service;
	}

	public String getViewID() {
		return VIEW_ID;
	}

	public void init() {
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		if (((GeneralViewParameters) viewparams).getSendingPage() != -1) {
			// will fail if page not in this site
			// security then depends upon making sure that we only deal with this page
			try {
				simplePageBean.updatePageObject(((GeneralViewParameters) viewparams).getSendingPage());
			} catch (Exception e) {
				log.info("LtiImportItemProducer permission exception " + e);
				return;
			}
		}

		UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
			.decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));


		// Check to see if we have just come back from the POST back from the external tool
		// SimplePageBean sets indicators and success/fail messages in the session to communcate
		// Back to us.  We are in the popup iframe at this point
                ToolSession toolSession = SessionManager.getCurrentToolSession();
                if (toolSession != null) {
			String done = (String ) toolSession.getAttribute("lessonbuilder.fileImportDone");
			toolSession.removeAttribute("lessonbuilder.fileImportDone");
			if ( done != null ) {
				List<String> errors = (List<String>)toolSession.getAttribute("lessonbuilder.errors");
				if (errors != null) {
					toolSession.removeAttribute("lessonbuilder.errors");
					UIOutput.make(tofill, "error-div");
					for (String e: errors) {
						UIBranchContainer er = UIBranchContainer.make(tofill, "errors:");
						UIOutput.make(er, "error-message", e);
					}
				} else {
					if ( ! "true".equals(done) ) UIOutput.make(tofill, "mainhead", done);
				}
				UIOutput.make(tofill, "blti-continue");
				return;
			}
		}

		// We are not in the pop-up iframe, create a list of  tools registered as importers
		List<Map<String, Object>> toolsImportItem = ltiService.getToolsImportItem();
		if ( toolsImportItem.size() < 1 ) {
			UIOutput.make(tofill, "error-div");
			UIBranchContainer er = UIBranchContainer.make(tofill, "errors:");
			UIOutput.make(er, "error-message", messageLocator.getMessage("simplepage.lti-import-error-no-tools"));
			return;
		}

		UIOutput.make(tofill, "mainhead", messageLocator.getMessage("simplepage.lti.import.chooser"));
		UIOutput.make(tofill, "lti-import-notes");

		// Can set ContentItemSelection launch values or put in our own data items
		// which will come back later.  Be mindful of GET length limitations enroute
		// to the /access/basiclti servlet.
		Properties contentData = new Properties();
		contentData.setProperty(ContentItem.ACCEPT_MEDIA_TYPES, ContentItem.MEDIA_CC);
		contentData.setProperty("answer", "42");  // An example

		// Loving the simple elegance of how RSF handles an incoming POST and routes it to
		// a method in SimplePageBean
		String hack = "command link parameters&Submitting control=submit&Fast track action=simplePageBean.handleImportItem";
		Placement placement = toolManager.getCurrentPlacement();

		if (simplePageBean.canEditPage()) {
			for ( int i = 0 ; i < toolsImportItem.size(); i++ ) {
				Map<String, Object> tool = toolsImportItem.get(i);
				if ( tool == null ) continue;
				Long toolId = SakaiBLTIUtil.getLongNull(tool.get(LTIService.LTI_ID));

				// Create a POSTable URL back to this application with the right parameters
				// Since the external tool will be setting all the POST data per Content Item spec, we need to 
				// include GET data for things that we would send to ourselves as "hidden" POST data

				// We want the borderless (/portal/tool/) URL as our return URL as it will be in a dialog iframe.
				String contentReturn = ServerConfigurationService.getToolUrl() + "/" + placement.getId() + "/LtiImportItem?" +
					"back=true" + "&toolId=" + toolId + "&" + URLEncoder.encode(hack) + "=hello" ;

				String contentLaunch  = ltiService.getToolLaunch(tool, placement.getContext());
				contentLaunch = ContentItem.buildLaunch(contentLaunch , contentReturn, contentData);

				String title = (String) tool.get(LTIService.LTI_TITLE);
				if ( title == null ) title = (String) tool.get(LTIService.LTI_PAGETITLE);
				if ( title == null ) title = messageLocator.getMessage("simplepage.blti.config");
				UIBranchContainer link = UIBranchContainer.make(tofill, "blti-launch:");
				UILink.make(link, "blti-launch-link", title, contentLaunch)
					.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.blti.config")));
				String fa_icon = (String) tool.get(LTIService.LTI_FA_ICON);
				if ( fa_icon != null ) {
					UIOutput.make(link, "blti-import-icon", "")
						.decorate(new UIFreeAttributeDecorator("class", "fa " + fa_icon));
				}
			}

			UIForm cancelform = UIForm.make(tofill, "blti-cancel");
			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			if ( sessionToken != null ) {	
                                UIInput.make(cancelform, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
			}
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
		togo.add(new NavigationCase("failure", new SimpleViewParameters(LtiImportItemProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return togo;
	}

}
