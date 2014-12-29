/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.help;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

/**
 * render help frame set 
 * @version $Id$
 */
public class HelpFrameSetRender extends Renderer
{
  private static String DEFAULT_WELCOME_PAGE = "html/help.html";

  private static String HELP_DOC_REGEXP = org.sakaiproject.api.app.help.HelpManager.HELP_DOC_REGEXP;
  
  /**
   * supports component type
   * @param component
   * @return true if supported
   */
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIData);
  }

  /** 
   * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
   */
  public void encodeBegin(FacesContext context, UIComponent component)
      throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    String helpWindowTitle = (String) component.getAttributes().get(
        "helpWindowTitle");
    String searchToolUrl = (String) component.getAttributes().get(
        "searchToolUrl");
    String tocToolUrl = (String) component.getAttributes().get("tocToolUrl");        
    
    String helpParameter = ((HttpServletRequest) context.getExternalContext()
        .getRequest()).getParameter("help");

    if (helpParameter != null) {
	    Pattern p = Pattern.compile(HELP_DOC_REGEXP);
	    Matcher m = p.matcher(helpParameter);
	    
	    if (!m.matches()) {
	    	helpParameter = "unknown";
	    }
    }
    String welcomepage = getWelcomePage(context);

    tocToolUrl = tocToolUrl + "?help=" + helpParameter;

    EventTrackingService.post(EventTrackingService.newEvent("help.access", helpParameter, false));

    helpWindowTitle = ServerConfigurationService.getString("ui.service", "Sakai") + " " + component.getAttributes().get("helpWindowTitle");
    
    writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">\n");
    writer.write("<html><head><title>" + helpWindowTitle + "</title></head>\n");
    writer.write("<FRAMESET cols=\"20%, 80%\"><FRAMESET rows=\"150, 450\">");
    writer.write("<FRAME src=\"" + searchToolUrl + "\" name=\"search\">");
    writer.write("<FRAME src=\"" + tocToolUrl + "\" name=\"toc\">");
    writer.write("</FRAMESET>\n");
    
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{Components['org.sakaiproject.api.app.help.HelpManager']}");
    HelpManager manager  = (HelpManager) binding.getValue(context);    
                  
    if(manager.getWelcomePage() == null) {
        if (welcomepage == DEFAULT_WELCOME_PAGE) {
      	  writer.write("<FRAME src=\"content.hlp?docId=" + welcomepage + "\" name=\"content\">");
        } else {
          writer.write("<FRAME src=\"" + welcomepage + "\" name=\"content\">");
        }

    }
    else {
      writer.write("<FRAME src=\"content.hlp?docId=" + manager.getWelcomePage() + "\" name=\"content\">");             
    }
                                        
    writer.write("</FRAMESET></html>\n");
  }

  /**
   * @param prefLocales
   *            The prefLocales to set.
   */
  private Locale getSelectedLocale() {
	  String language = "";
	  String country = "";

	  Preferences prefs = (PreferencesEdit) PreferencesService
              .getPreferences(UserDirectoryService.getCurrentUser().getId());
      ResourceProperties props = prefs
              .getProperties(ResourceLoader.APPLICATION_ID);
      String prefLocale = props.getProperty(ResourceLoader.LOCALE_KEY);

      if (prefLocale != null && prefLocale.length() > 0) {
    	  if (prefLocale.contains("_")) {
    		 language = prefLocale.substring(0, prefLocale.indexOf("_"));
    		 country = prefLocale.substring(prefLocale.indexOf("_") + 1);
    	  }
    	  else {
    		  language = prefLocale;
    	  }
    	  return new Locale(language, country);
      }
      else {
    	  return Locale.getDefault();
      }
  }

  /**
   * Gets localized welcome page if it exists or fall back on default
   * @return welcome page
   */
  private String getWelcomePage(FacesContext context) {

	  String page = ServerConfigurationService.getString("help.welcomepage");

	  if ("".equals(page)){
		  page = DEFAULT_WELCOME_PAGE;
	  }

	  // Build localized welcome page
      URL urlResource = null;
      StringBuilder sb = new StringBuilder();
      sb.append(page.substring(0, page.lastIndexOf(".")));
      sb.append("_");
      sb.append(getSelectedLocale().toString());
      sb.append(page.substring(page.lastIndexOf(".")));

      // Get localized welcome page
      try {
    	  urlResource = FacesContext.getCurrentInstance().getExternalContext().getResource("/" + sb.toString());
      } catch (MalformedURLException e) {
    	  // Ignore
      }

      // If it doesn't exist, fall back on default
      if (urlResource != null) {
    	  page = sb.toString();
      }
      return page;
  }
}
