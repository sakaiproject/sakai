/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui.tool.pages;

import javax.servlet.http.HttpServletRequest;

import org.adl.sequencer.SeqNavRequests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.tool.RunState;
import org.sakaiproject.scorm.ui.tool.ScoBean;
import org.sakaiproject.scorm.ui.tool.behaviors.ActivityAjaxEventBehavior;
import org.sakaiproject.scorm.ui.tool.components.AjaxImageButton;
import org.sakaiproject.scorm.ui.tool.components.ButtonForm;
import org.sakaiproject.scorm.ui.tool.components.ChoicePanel;
import org.sakaiproject.scorm.ui.tool.components.LaunchPanel;
import org.sakaiproject.scorm.ui.tool.components.LazyLoadPanel;
import org.sakaiproject.user.api.User;


public class View extends BaseToolPage {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(View.class);
	
	private static final ResourceReference ROLLOVER_SCRIPT = new CompressedResourceReference(View.class, "res/rollover.js");

	
	private static final ResourceReference ORGANIZE_DIMENSIONS_JS = new CompressedResourceReference(View.class, "organize-dimensions.js");
	private static final String ON_DOM_READY_JS_BEGIN = "setDimensions(";
	private static final String ON_DOM_READY_JS_END = ");";
	
	//private static final String ONBEFOREUNLOAD_JS="alert('quitting');";

	@SpringBean
	ScormClientFacade clientFacade;
	
	private RunState runState;
	
	private LaunchPanel launchPanel;
	private ActivityAjaxEventBehavior closeWindowBehavior;
	private ButtonForm buttonForm;
	
	public View() {
		this(new PageParameters());
	}
	
	public View(final PageParameters pageParams) {
		super();
		//add(new Label("title", "SCORM 2004 3rd Edition Player for Sakai"));
		
				
		String contentPackageId = pageParams.getString("contentPackage");
		
		if (contentPackageId != null) 
			contentPackageId = contentPackageId.replace(':', '/');
			
		User user = clientFacade.getCurrentUser();
		
		String userId = user.getId();
		
		RequestCycle cycle = getRequestCycle();
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
		WebRequest webRequest = (WebRequest)getRequest();
		HttpServletRequest servletRequest = webRequest.getHttpServletRequest();
		String toolUrl = servletRequest.getContextPath();
		CharSequence completionUrl = encoder.encode(cycle, new BookmarkablePageRequestTarget(CompletionPage.class, new PageParameters()));
		AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(toolUrl).append("/").append(completionUrl);
		//CharSequence contentUrl = encoder.encode(cycle, new BookmarkablePageRequestTarget(ContentPage.class, new PageParameters()));
		//AppendingStringBuffer fullContentUrl = new AppendingStringBuffer();
		//fullContentUrl.append(toolUrl).append("/").append(contentUrl);
		
		runState = new RunState(clientFacade, contentPackageId, contentPackageId, userId, url.toString());
		
		buttonForm = new ButtonForm("buttonForm", runState, this);
		buttonForm.setOutputMarkupId(true);
		buttonForm.synchronizeState(runState, null);
		add(buttonForm);
		
		//add(launch("actionPanel", pageParams, null));

		/*add(new LazyLoadPanel("actionPanel") {
			@Override
		    public Component getLazyLoadComponent(String lazyId, AjaxRequestTarget target) {
				return new Label(lazyId, "Nothing");
			}
		});*/
		
		//add(new Label("actionPanel", "Nothing"));
		
		add(new LazyLoadPanel("actionPanel") {
			private static final long serialVersionUID = 1L;

			@Override
		    public Component getLazyLoadComponent(String lazyId, AjaxRequestTarget target) {
				return launch(runState, lazyId, pageParams, target);
			}
		});
		
		closeWindowBehavior = new ActivityAjaxEventBehavior("closeWindow") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				log.warn("closeWindowBehavior onEvent()");
				if (runState != null && runState.isStarted() && !runState.isEnded()) {
					log.warn("----> Going to exit on next terminate request");
					runState.setCloseOnNextTerminate();
				}
			}
			
			@Override
			protected void onComponentTag(final ComponentTag tag) {
				// Do nothing -- we don't want to add the javascript to the component.
			}
			
		};
		
		add(closeWindowBehavior);
	}
		
	
	private Component launch(RunState runState, String lazyId, PageParameters pageParams, AjaxRequestTarget target) {
		
		int navRequest = SeqNavRequests.NAV_START;
			
		String result = null;
		
		try {
			if (pageParams.containsKey("navRequest"))
				navRequest = pageParams.getInt("navRequest");
			
			
			result = runState.navigate(navRequest, null);
			
			if (result == null || result.contains("_TOC_")) {
				launchPanel = new LaunchPanel(lazyId, runState, View.this);
				ScoBean scoBean = runState.produceScoBean(runState.getCurrentSco());
				scoBean.clearState();
				View.this.synchronizeState(runState, target);
						
				runState.displayContent(target);
				
				return launchPanel;
			} 
			
			log.warn("Result is " + result);
			
		} catch (Exception e) {
			result = e.getMessage();
			e.printStackTrace();
			
			log.error("Caught an exception: " , e);
		} 
		
		ChoicePanel choicePanel = new ChoicePanel(lazyId, pageParams, result);
		
		return choicePanel;
	}
	
	public void synchronizeState(RunState runState, AjaxRequestTarget target) {
		buttonForm.synchronizeState(runState, target);
	}
	
		
	/**
	 * No need for versioning this frame.
	 * 
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}
	
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		//response.renderJavascriptReference(ORGANIZE_DIMENSIONS_JS);
		
		response.renderOnBeforeUnloadJavascript(closeWindowBehavior.getCall());
		response.renderJavascriptReference(ROLLOVER_SCRIPT);
		
		/*if (launchPanel != null) {
			StringBuffer onDomReadyJs = new StringBuffer().append(ON_DOM_READY_JS_BEGIN);
			onDomReadyJs.append(launchPanel.getMarkupId()).append(", ").append(launchPanel.getTreePanel().getMarkupId());
			onDomReadyJs.append(", ").append(" scormContent");
			onDomReadyJs.append(ON_DOM_READY_JS_END);
			
			response.renderOnDomReadyJavascript(onDomReadyJs.toString());
		}*/
	}
	

	public ButtonForm getButtonForm() {
		return buttonForm;
	}
	
	public LaunchPanel getLaunchPanel() {
		return launchPanel;
	}

	
}
