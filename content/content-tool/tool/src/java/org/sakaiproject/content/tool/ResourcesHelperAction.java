/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.content.tool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.util.ResourceLoader;

public class ResourcesHelperAction extends VelocityPortletPaneledAction 
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("types");

	protected  static final String ACCESS_HTML_TEMPLATE = "resources/sakai_access_html";
	protected  static final String ACCESS_TEXT_TEMPLATE = "resources/sakai_access_text";
	protected  static final String ACCESS_UPLOAD_TEMPLATE = "resources/sakai_access_upload";
	protected  static final String ACCESS_URL_TEMPLATE = "resources/sakai_access_url";
	
	protected  static final String CREATE_HTML_TEMPLATE = "resources/sakai_create_html";
	protected  static final String CREATE_TEXT_TEMPLATE = "resources/sakai_create_text";
	protected  static final String CREATE_UPLOAD_TEMPLATE = "resources/sakai_create_upload";
	protected  static final String CREATE_URL_TEMPLATE = "resources/sakai_create_url";
	
	protected  static final String REVISE_HTML_TEMPLATE = "resources/sakai_revise_html";
	protected  static final String REVISE_TEXT_TEMPLATE = "resources/sakai_revise_text";
	protected  static final String REVISE_UPLOAD_TEMPLATE = "resources/sakai_revise_upload";
	protected  static final String REVISE_URL_TEMPLATE = "resources/sakai_revise_url";

	public String buildAccessPanelContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		String template = ACCESS_TEXT_TEMPLATE;
		return template;
	}

	public String buildCreatePanelContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		String template = CREATE_TEXT_TEMPLATE;
		return template;
	}

	public String buildRevisePanelContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		String template = REVISE_TEXT_TEMPLATE;
		return template;
	}

}
