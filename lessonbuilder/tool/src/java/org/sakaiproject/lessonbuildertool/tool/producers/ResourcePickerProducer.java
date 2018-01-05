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

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.FilePickerViewParameters;
import org.sakaiproject.rsf.helper.HelperViewParameters;
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
public class ResourcePickerProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
	public static final String VIEW_ID = "ResourcePicker";

	public String getViewID() {
		return VIEW_ID;
	}

	private SimplePageBean simplePageBean;

	// helper tool
	public static final String HELPER = "sakai.filepicker";

	private SessionManager sessionManager;

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private ContentHostingService contentHostingService;

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
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

	    // this doesn't show anything in LB. I don't see any real permissions issues

		if (((FilePickerViewParameters) viewparams).getSender() != -1) {
			// will fail if page not in this site
			// security then depends upon making sure that we only deal with this page
			try {
				simplePageBean.updatePageObject(((FilePickerViewParameters) viewparams).getSender());
			} catch (Exception e) {
				log.info("ResourcePicker permission exception " + e);
				return;
			}
		}

		if (!simplePageBean.canReadPage())
		    return;

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		Long itemId = ((FilePickerViewParameters) viewparams).getPageItemId();
		
		// parameters for helper
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, "Please Choose a File");
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT, messageLocator.getMessage("simplepage.filepicker_instructions"));
		// multiple files not valid if replacing an existing item
		if (itemId == null || itemId == -1)
		    toolSession.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, FilePickerHelper.CARDINALITY_MULTIPLE);
		else
		    toolSession.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, new Integer(1));
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, new ArrayList());
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER, null);
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS, ServerConfigurationService.getString("lessonbuilder.attachlinks", "true"));
		toolSession.setAttribute(SimplePageBean.LESSONBUILDER_ITEMID, itemId);
		toolSession.setAttribute(SimplePageBean.LESSONBUILDER_ADDBEFORE, ((FilePickerViewParameters) viewparams).getAddBefore());
		toolSession.setAttribute(SimplePageBean.LESSONBUILDER_ITEMNAME, ((FilePickerViewParameters) viewparams).getName());

		if (simplePageBean.isStudentPage(simplePageBean.getCurrentPage())) {
		    toolSession.setAttribute(FilePickerHelper.DEFAULT_COLLECTION_ID, "/user/" + simplePageBean.getCurrentUserId() + "/");
		}

		// rsf:id helper-id
		UIOutput.make(tofill, HelperViewParameters.HELPER_ID, HELPER);

		// rsf:id helper-binding method binding
		String process = null;

		if (((FilePickerViewParameters) viewparams).isWebsite())
		    process = "#{simplePageBean.processWebSite}";
		else if (((FilePickerViewParameters) viewparams).getCaption())
		    process = "#{simplePageBean.processCaption}";
		else if (((FilePickerViewParameters) viewparams).getResourceType())
		    process = "#{simplePageBean.processMultimedia}";
		else
		    process = "#{simplePageBean.processResource}";		    

		UICommand.make(tofill, HelperViewParameters.POST_HELPER_BINDING, process);

	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public List reportNavigationCases() {
		List l = new ArrayList();
		l.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		l.add(new NavigationCase("importing", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return l;
	}

	public ViewParameters getViewParameters() {
		return new FilePickerViewParameters();
	}
}
