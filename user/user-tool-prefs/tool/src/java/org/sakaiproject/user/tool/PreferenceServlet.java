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
		String defaultPreference="prefs_tab_title, prefs_noti_title, prefs_timezone_title, prefs_lang_title";
		String Notification="prefs_noti_title", CustomTab="prefs_tab_title", Timezone="prefs_timezone_title", Language="prefs_lang_title";
		String tabOrder=ServerConfigurationService.getString("preference.pages",defaultPreference);
		String[] tablist=tabOrder.split(",");
		String defaultPage=null;

		if(tablist[0].equals(Notification)) defaultPage="noti";
		else if(tablist[0].equals(CustomTab)){			
			if (ServerConfigurationService.getBoolean ("portal.use.dhtml.more", false))
				defaultPage = "tab-dhtml-moresites";
			else
				defaultPage="tab";
		}
		else if(tablist[0].equals(Timezone)) defaultPage="timezone";
		else if (tablist[0].equals(Language)) defaultPage="locale";

		return defaultPage;
	}



}	
