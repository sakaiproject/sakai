/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.user.tool;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.util.JsfTool;

public class PreferenceServlet extends JsfTool {

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		//UserPrefsTool tool = (UserPrefsTool) config.getServletContext().getAttribute("UserPrefsTool");
		m_default= defaultValue();
	}

	protected String defaultValue()
	{
		String defaultPreference="prefs_noti_title, prefs_timezone_title, prefs_lang_title, prefs_hidden_title";
		String Notification="prefs_noti_title", Timezone="prefs_timezone_title", Language="prefs_lang_title", Hidden="prefs_hidden_title";
		String tabOrder=ServerConfigurationService.getString("preference.pages",defaultPreference);
		String[] tablist=tabOrder.split(",");
		String defaultPage=null;

		if(tablist[0].equals(Notification)) defaultPage="noti";
		else if(tablist[0].equals(Timezone)) defaultPage="timezone";
		else if(tablist[0].equals(Language)) defaultPage="locale";
		else if(tablist[0].equals(Hidden))defaultPage="hidden";

		return defaultPage;
	}
}	
