/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;


public class ToolModel implements IModel {
	private static final long		serialVersionUID	= 1L;
	
	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	private String					toolId				= "";
	private String					toolName			= "";

	public ToolModel(String toolId, String toolName) {
		this.toolId = toolId;
		this.toolName = toolName;
	}
	
	public ToolModel(ToolInfo e) {
		this.toolId = e.getToolId();
		this.toolName = getFacade().getEventRegistryService().getToolName(this.toolId);
	}

	public Object getObject() {
		return getToolId() + " + " + getToolName();
	}

	public void setObject(Object object) {
		if(object instanceof String){
			String[] str = ((String) object).split(" \\+ ");
			toolId = str[0];
			toolName = str[1];
		}
	}

	public String getToolId() {
		return toolId;
	}

	public String getToolName() {
		if(ReportManager.WHAT_EVENTS_ALLTOOLS.equals(toolName)) {
			return (String) new ResourceModel("all").getObject();
		}else{
			return toolName;
		}
	}

	public void detach() {
		toolId = null;
		toolName = null;
	}
	
	private SakaiFacade getFacade() {
		if(facade == null) {
			InjectorHolder.getInjector().inject(this);
		}
		return facade;
	}

}