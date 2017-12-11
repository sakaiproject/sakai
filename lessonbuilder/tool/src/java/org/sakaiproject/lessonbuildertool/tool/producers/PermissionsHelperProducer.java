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
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.localeutil.LocaleGetter;      
import uk.org.ponder.messageutil.MessageLocator;                                                                                    
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;                                                               
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.rsf.helper.HelperViewParameters;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;

/**
 * Uses the ResourcePicker to permit adding resources to the page.
 * 
 * This producer is used both for adding resources and for adding multimedia.
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
@Slf4j
public class PermissionsHelperProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
	public static final String VIEW_ID = "PermissionsHelper";

	public String getViewID() {
		return VIEW_ID;
	}

	private SimplePageBean simplePageBean;

        public void setSimplePageBean(SimplePageBean bean) {
	    simplePageBean = bean;
	}

	// helper tool
	public static final String HELPER = "sakai.permissions.helper";

	private static final String[] PERMISSIONS = new String[] {
			SimplePage.PERMISSION_LESSONBUILDER_UPDATE,
			SimplePage.PERMISSION_LESSONBUILDER_READ,
			SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL};

	private SessionManager sessionManager;

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private SiteService siteService;

	public void setSiteService(SiteService service) {
		siteService = service;
	}

	private ToolManager toolManager;

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	private MessageLocator messageLocator;

	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	public LocaleGetter localeGetter;                                                                                             

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		if (!simplePageBean.canEditPage() && !simplePageBean.canEditSite())
		    return;

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		// this is purely a site config, so no permission other than caneditpage needed

		// parameters for helper
		ToolSession session = sessionManager.getCurrentToolSession();
		Site site = null;

                try {
		    site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
                } catch (Exception impossible) {
		    log.error(impossible.getMessage(), impossible);
		    return;
                }

		session.setAttribute(PermissionsHelper.TARGET_REF, site.getReference());
		session.setAttribute(PermissionsHelper.DESCRIPTION, messageLocator.getMessage("simplepage.editpermissions")
				+ " " +  site.getTitle() + ". " + messageLocator.getMessage("simplepage.ownerpermissions"));
		session.setAttribute(PermissionsHelper.PREFIX, "lessonbuilder.");

		HashMap<String, String> pRbValues = new HashMap<String, String>();
		for (String perm : PERMISSIONS) {
			String descr = messageLocator.getMessage("desc-" + perm);
			pRbValues.put("desc-" + perm, descr);
		}
		session.setAttribute("permissionDescriptions", pRbValues);

		UIOutput.make(tofill, HelperViewParameters.HELPER_ID, HELPER);
		UICommand.make(tofill, HelperViewParameters.POST_HELPER_BINDING, "", null);
	}

	public List reportNavigationCases() {
		List l = new ArrayList();
		l.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return l;
	}

	public ViewParameters getViewParameters() {
		return new HelperViewParameters();
	}
}
