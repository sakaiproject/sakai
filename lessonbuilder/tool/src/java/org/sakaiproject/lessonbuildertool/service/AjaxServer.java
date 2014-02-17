/**********************************************************************************
 * $URL: $
 * $Id: $
 **********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.lessonbuildertool.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;
import org.springframework.context.MessageSource;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.SimplePage;

/**
 * <p>
 * Ajax server
 * </p>
 *  .../lessonbuilder-tool/ajax?op=getmessage&code=simplepage.new-page&locale=fr
 *       look up text messages in messages.properties.  "" if can't find it
 *       this lets us avoid passing the text of some larger messages as text on 
 *       every HTML page. The server doesn't have enough context to know the right
 *       locale for itself, but the UI should know it
 *  .../lessonbuilder-tool/ajax?op=getmimetype&url=http://www.rutgers.edu
 *       check a URL to see what MIME type it is, "" if can't find it
 *       though the URL should actually be urlencoded
 * 
 * @author Charles Hedrick, Rutgers University.
 * @version $Revision: $
 */
@SuppressWarnings({ "serial", "deprecation" })
public class AjaxServer extends HttpServlet
{
   private static final String UTF8 = "UTF-8";

   private static MessageSource messageSource;
   private static SiteService siteService;

   /** Our log (commons). */
   private static Log log = LogFactory.getLog(AjaxServer.class);
   
   public static final String FILTERHTML = "lessonbuilder.filterhtml";
   private static String filterHtml = ServerConfigurationService.getString(FILTERHTML);

    static Class levelClass = null;
    static Object[] levels = null;
    static Class ftClass = null;
    static Method ftMethod = null;
    static Object ftInstance = setupFtStuff();

    static Object setupFtStuff () {
	Object ret = null;
	try {
	    levelClass = Class.forName("org.sakaiproject.util.api.FormattedText$Level");
	    levels = levelClass.getEnumConstants();
	    ftClass = Class.forName("org.sakaiproject.util.api.FormattedText");
	    ftMethod = ftClass.getMethod("processFormattedText", 
					 new Class[] { String.class, StringBuilder.class, levelClass }); 
	    ret = org.sakaiproject.component.cover.ComponentManager.get("org.sakaiproject.util.api.FormattedText");
	    return ret;
	} catch (Exception e) {
	    log.error("Formatted Text with levels not available: " + e);
	    return null;
	}
    }

   /**
    * Access the Servlet's information display.
    * 
    * @return servlet information.
    */
   public String getServletInfo()
   {
      return "Lessons Ajax Server";
   }
   
   /**
    * Initialize the servlet.
    * 
    * @param config
    *        The servlet config.
    * @throws ServletException
    */
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
   }
   
   /**
    * Shutdown the servlet.
    */
   public void destroy()
   {
      log.info("destroy()");
      
      super.destroy();
   }
   
   /**
    * Respond to Get requests:
    *   display main content by redirecting to it and adding
    *     user= euid= site= role= serverurl= time= sign=
    *   for privileged users, add a bar at the top with a link to
    *     the setup screen
    *   ?Setup generates the setup screen
    * 
    * @param req
    *        The servlet request.
    * @param res
    *        The servlet response.
    * @throws ServletException.
    * @throws IOException.
    */
   
    public static String getMessage(String code, String localeName) {
	Locale locale = null;

	if (localeName != null && localeName.length() > 0) {
	    String langLoc[] = localeName.split("_");
	    if (langLoc.length >= 2) {
		if ("en".equals(langLoc[0]) && "ZA".equals(langLoc[1])) {
		    locale = new Locale("en", "GB");
		} else {
		    locale = new Locale(langLoc[0], langLoc[1]);
		}
	    } else {
		locale = new Locale(langLoc[0]);
	    }
	}
	if (locale == null)
	    locale = Locale.getDefault();

	String value = "";
	try {
	    value = messageSource.getMessage(code, new String[0], locale);
	} catch (Exception e) {};

	return value;
    }
	
    // in order to match the logic in ShowPageProducer
    // * youtube has to return something other than html, in this case application/youtube,
    // * image types have to return something with image, using the same test as SimplePageBean.isimage
    // * html has to return an html type even based on extension, matching test in ShowPageProducer

    public String getMimeType(String url) {
	Session s = SessionManager.getCurrentSession();
	if (s == null || s.getUserId() == null) {
	    //	    return "";
	}

	if (SimplePageBean.getYoutubeKeyFromUrl(url) != null)
	    return "application/youtube";

	String mimeType = "";
	URLConnection conn = null;
	try {
	    conn = new URL(new URL(ServerConfigurationService.getServerUrl()),url).openConnection();
	    conn.setConnectTimeout(10000);
	    conn.setReadTimeout(10000);
	    conn.connect();
	    String t = conn.getContentType();
	    if (t != null && !t.equals("")) {
		int i = t.indexOf(";");
		if (i >= 0)
		    t = t.substring(0, i);
		t = t.trim();
		mimeType = t;
	    }
	} catch (Exception e) {
	} finally {
	    if (conn != null) {
		try {
		    conn.getInputStream().close();
		} catch (Exception e) {
		    // log.error("getTypeOfUrl unable to close " + e);
		}
	    }
	}

	if (mimeType == null || mimeType.equals("")) {
	    String name = url;

	    // starts after last /
	    int i = name.lastIndexOf("/");
	    if (i >= 0)
		name = name.substring(i+1);
	    
	    String extension = null;	    
	    i = name.lastIndexOf(".");
	    if (i > 0)
		extension = name.substring(i+1);
	    
	    if (extension == null)
		return "";
	
	    if (SimplePageBean.imageTypes.contains(extension)) {
		return "image/unknown";
	    } if (extension.equals("html") || extension.equals("htm")) {
		return "text/html";
	    } else if (extension.equals("xhtml") || extension.equals("xht")) {
		return "application/xhtml+xml";
	    } else {
		return "";
	    }

	}

	return mimeType;
    }

    // run antisamy or whatever on a string
    
    // WARNING: keep in sync with submit in SimplePageBean.

    public static String filterHtml(String contents) {

	StringBuilder error = new StringBuilder();

	final Integer FILTER_DEFAULT=0;
	final Integer FILTER_HIGH=1;
	final Integer FILTER_LOW=2;
	final Integer FILTER_NONE=3;

	// Sakai currently defaults to high. Unfortunately many
	// embeds won't work with that. Might want to default this
	// to low, or add another config parameter, but currently
	// using same code as for text blocks.

	String html = contents;

	// figure out how to filter
	Integer filter = FILTER_DEFAULT;
	// simplepagebean checks filterHtml property of tool. We can't really do that.

	String filterSpec = filterHtml;
	System.out.println("filterspec " + filterSpec);
	if (filterSpec == null) // should never be null. unspeciifed should give ""
	    filter = FILTER_DEFAULT;
	// old specifications
	else if (filterSpec.equalsIgnoreCase("true"))
	    filter = FILTER_HIGH; // old value of true produced the same result as missing
	else if (filterSpec.equalsIgnoreCase("false"))			    
	    filter = FILTER_NONE;
	// new ones
	else if (filterSpec.equalsIgnoreCase("default"))			    
	    filter = FILTER_DEFAULT;
	else if (filterSpec.equalsIgnoreCase("high")) 
	    filter = FILTER_HIGH;
	else if (filterSpec.equalsIgnoreCase("low")) 
	    filter = FILTER_LOW;
	else if (filterSpec.equalsIgnoreCase("none")) 
	    filter = FILTER_NONE;
	// unspecified
	else
	    filter = FILTER_DEFAULT;

	if (filter.equals(FILTER_NONE)) {
	    html = FormattedText.processHtmlDocument(contents, error);
	} else if (filter.equals(FILTER_DEFAULT)) {
	    html = FormattedText.processFormattedText(contents, error);
	} else if (ftInstance != null) {
	    try {
		// now filter is set. Implement it. Depends upon whether we have the anti-samy code
		Object level = null;
		if (filter.equals(FILTER_HIGH))
		    level = levels[1];
		else
		    level = levels[2];
		
		html = (String)ftMethod.invoke(ftInstance, new Object[] { contents, error, level });
	    } catch (Exception e) {
		// this should never happen. If it does, emulate what the anti-samy
		// code does if antisamy is disabled. It always filters
		html = FormattedText.processFormattedText(contents, error);
	    }
	} else {
	    // don't have antisamy. For LOW, use old instructor behavior, since
	    // LOW is the default. For high, it makes sense to filter
	    if (filter.equals(FILTER_HIGH))
		html = FormattedText.processFormattedText(contents, error);
	    else
		html = FormattedText.processHtmlDocument(contents, error);
	    
	}
	return html;
    }

    // argument is comma separated list, locale, site, group, group ...
    public static String groupErrors(String siteId, String locale, String groupString) {

	locale = locale.trim();
	if (locale.length() == 0)
	    locale = null;
	if (siteId == null)
	    siteId = "";
	siteId = siteId.trim();

	// currently this is only needed by the instructor
	String ref = "/site/" + siteId;
	if (!SecurityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref))
	    return getMessage("simplepage.nowrite", locale);
	
	if (groupString == null)
	    groupString = "";
	Map<String,Set<String>> user2groups = new HashMap<String,Set<String>>();
	Set<String> overlapGroups = new HashSet<String>();
	String [] groupIdArray= groupString.trim().split(",");
	List<String> groupIds = new ArrayList<String>();
        for (String g: groupIdArray) {
	    g = g.trim();
	    if (g.length() > 0)
		groupIds.add(g);
	}

	Site site = null;
	Collection<String> users = null;
	Collection<Group> groups = null;

	try {
	    // get all users in site and add entries to user@groups
	    // this will have all the groups each user belongs to
	    site = siteService.getSite(siteId);
	    HashSet<String> siteGroup = new HashSet<String>();
	    siteGroup.add("/site/" + siteId);
	    // not in 2.8   users = AuthzGroupService.getAuthzUsersInGroups(siteGroup);
	    users = AuthzGroupService.getUsersIsAllowed("site.visit", siteGroup);

	    for (String userId: users) {
		user2groups.put(userId, null);
	    }

	    // get list of groups, either specified list or all groups in site
	    if (groupIds.size() > 0) {
		groups = new HashSet<Group>();
		for (String groupId: groupIds) {
		    groups.add(site.getGroup(groupId));
		}
	    } else
		groups = site.getGroups();

	    // for each group
	    //    for each user in group
	    //       if user already in a different group, there's an overlap
	    //       otherwise remember this user is in this group
	    for (Group group: groups) {
		users = group.getUsers();
		for (String groupUser: users) {
		    Set<String> userGroups = user2groups.get(groupUser);
		    if (userGroups != null) {
			userGroups.add(group.getId());
			overlapGroups.addAll(userGroups);
		    } else {
			userGroups = new HashSet<String>();
			userGroups.add(group.getId());
			user2groups.put(groupUser, userGroups);
		    }
		}
	    }

	    // now output warnings
	    String retmessage = null;
	    if (overlapGroups.size() > 0) {
		String overlaps = "";
		for (String groupId: overlapGroups) {
		    Group group = site.getGroup(groupId);
		    overlaps += ", " + group.getTitle();
		}
		retmessage = getMessage("simplepage.groupcheckoverlaps", locale).replace("{}", overlaps.substring(2));
	    }
	    String missing = "";
	    for (Map.Entry<String, Set<String>> entry: user2groups.entrySet()) {
		if (entry.getValue() == null) {
		    String eid = UserDirectoryService.getUserEid(entry.getKey());
		    missing += ", " + eid;
		}
	    }
	    if (missing.length() > 1) {
		if (retmessage == null)
		    retmessage = "";
		else
		    retmessage += "\n\n";
		retmessage += getMessage("simplepage.groupchecknogroups", locale).replace("{}", missing.substring(2));
	    }
	    if (retmessage != null)
		return "\n\n" + retmessage + "\n\n" + getMessage("simplepage.insist", locale);
	    else
		return "ok";

	} catch (Exception e) {
	    return getMessage("simplepage.groupcheckfailed", locale).replace("{}", e.toString());
	}
    }	    


   @SuppressWarnings("unchecked")
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {

      // get the Tool
      Placement placement = ToolManager.getCurrentPlacement();
      Properties config = null;
      String placementId = "none";
      
      if (placement != null) {
         config = placement.getConfig();
         placementId = placement.getId();
      }
      
      res.setContentType("text/html; charset=utf-8");
      PrintWriter out = res.getWriter();

      String op = req.getParameter("op");
      if (op.equals("getmessage")) {
	  String code = req.getParameter("code");
	  String locale = req.getParameter("locale");
	  out.println(getMessage(code, locale));
      } else if (op.equals("getmimetype")) {
	  String url = req.getParameter("url");
	  out.println(getMimeType(url));
      } else if (op.equals("filterhtml")) {
	  String html = req.getParameter("html");
	  out.print(filterHtml(html));
      } else if (op.equals("getgrouperrors")) {
	  String siteid = req.getParameter("site");
	  String locale = req.getParameter("locale");
	  String groups = req.getParameter("groups");
	  out.print(groupErrors(siteid, locale, groups));
      }
      
   }
   
    public void setMessageSource(MessageSource s) {
	messageSource = s;
    }

    public void setSiteService(SiteService s) {
	siteService = s;
    }

}
