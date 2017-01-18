/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.player.components;

import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.sakaiproject.scorm.model.api.SessionBean;

public class CommunicationPanel extends Panel { //implements IHeaderContributor {
	public static final ResourceReference API = new CompressedResourceReference(
			CommunicationPanel.class, "res/API.js");

	public static final ResourceReference API_WRAPPER = new CompressedResourceReference(
			CommunicationPanel.class, "res/APIWrapper.js");

	private static final long serialVersionUID = 1L;

	//private LaunchPanel launchPanel;
	
	/*@SpringBean
	LearningManagementSystem lms;
	
	@SpringBean
	ScormApplicationService applicationService;
	@SpringBean
	ScormResourceService resourceService;
	@SpringBean
	ScormSequencingService sequencingService;
	
	private SessionBean sessionBean;*/
	
	
	public CommunicationPanel(String id, final SessionBean sessionBean, final LaunchPanel launchPanel) {
		super(id);
		//this.launchPanel = launchPanel;
		//this.sessionBean = sessionBean;
		
		SjaxContainer container = new SjaxContainer("sjaxContainer", sessionBean, launchPanel);
		
		add(container);
		
		
	}
	
	
	/*public void updatePageSco(String scoId, AjaxRequestTarget target) {
		if (target != null)
			target.appendJavascript("sco = '" + scoId + "';");
	}*/

	protected String getFirstArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 0)
			return "";

		return argumentValues.get(0);
	}
	
	protected String getSecondArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 1)
			return "";
		
		return argumentValues.get(1);
	}
	
	
	
	/*public void renderHead(IHeaderResponse response) {
		StringBuffer js = new StringBuffer();
		
		js.append("function APIAdapter() { };\n")	
			.append("var API_1484_11 = APIAdapter;\n")
			.append("var api_result = new Array();\n")
			.append("var call_number = 0;\n")
			.append("var sco = undefined;\n");
		
		for (int i=0;i<calls.length;i++) {
			js.append(calls[i].getJavascriptCode()).append("\n");
		}
		
		response.renderJavascript(js.toString(), "SCORM_API");
	}
	
	
	public class ScormSjaxCall extends SjaxCall {
		
		private static final long serialVersionUID = 1L;
		
		public ScormSjaxCall(String event, int numArgs) {
			super(event, numArgs);
		}
		
		@Override
		protected SessionBean getSessionBean() {
			return sessionBean;
		}
		
		@Override
		protected LearningManagementSystem lms() {
			return lms;
		}
		
		@Override
		protected ScormApplicationService applicationService() {
			return applicationService;
		}

		@Override
		protected ScormResourceService resourceService() {
			return resourceService;
		}
		
		@Override
		protected ScormSequencingService sequencingService() {
			return sequencingService;
		}
		
		@Override
		protected String getChannelName() {
			return "1|s";
		}
		
		public void updatePageSco(String scoId, AjaxRequestTarget target) {
			if (target != null)
				target.appendJavascript("sco = '" + scoId + "';");
		}
	}*/
	
	
}
