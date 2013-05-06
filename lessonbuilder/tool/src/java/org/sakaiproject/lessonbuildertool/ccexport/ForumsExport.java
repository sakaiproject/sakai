/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2013 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.ccexport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;
import java.util.SortedSet;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Iterator;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

import org.w3c.dom.Document;

import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.db.api.SqlReader;
import java.sql.Connection;
import java.sql.ResultSet;
import org.sakaiproject.lessonbuildertool.ccexport.ZipPrintStream;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;

import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.app.messageforums.MembershipItem;

import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.FormattedText;

/*
 * set up as a singleton, but also instantiated by CCExport.
 * The purpose of the singleton setup is just to get the dependencies.
 * So they are all declared static.
 */

public class ForumsExport {

    private static Log log = LogFactory.getLog(ForumsExport.class);

    private static SimplePageToolDao simplePageToolDao;

    public void setSimplePageToolDao(Object dao) {
	simplePageToolDao = (SimplePageToolDao) dao;
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    static MessageForumsForumManager forumManager = (MessageForumsForumManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsForumManager");
    static MessageForumsMessageManager messageManager = (MessageForumsMessageManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");


    CCExport ccExport = null;

    public void init () {
	// currently nothing to do

	log.info("init()");

    }

    public void destroy()
    {
	log.info("destroy()");
    }

    // find topics in site
    public List<String> getEntitiesInSite(String siteId, CCExport bean) {

	String siteRef = "/group/" + siteId + "/";

	List<String> ret = new ArrayList<String>();

	// LSNBLDR-21. If the tool is not in the current site we shouldn't query
	// for topics owned by the tool.
	Site site = null;
	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	} catch (Exception impossible) {
	    return ret;
	}
    	
	if(site.getToolForCommonId("sakai.forums") == null) {
	    // Forums is not in this site. Move on to the next provider.
	    return ret;
	}

	for (DiscussionForum forum: forumManager.getForumsForMainPage()) {
	    if (!forum.getDraft()) {
		for (DiscussionTopic topic: (Set<DiscussionTopic>)forum.getTopicsSet()) {
		    if (topic.getDraft().equals(Boolean.FALSE)) {
			ret.add(LessonEntity.FORUM_TOPIC + "/" + topic.getId());
			List<Attachment> attachments = topic.getAttachments();
			for (Attachment attachment: attachments) {
			    String sakaiId = attachment.getAttachmentId();
			
			    // this code is to identify attachments that aren't in the normal
			    // site resources. In that case we have to make a copy of it
			    String url = null;
			    // if it is a URL, need the URL rather than copying the file
			    try {
				ContentResource res = ContentHostingService.getResource(sakaiId);
				String type = res.getContentType();
				if ("text/url".equals(type)) {
				    url = new String(res.getContent());
				}
			    } catch (Exception e) {
			    }
			
			    if (url != null)
				;  // if it's a URL we don't need a file
			    else if (! sakaiId.startsWith(siteRef)) {  // if in resources, already included
				int lastSlash = sakaiId.lastIndexOf("/");
				String lastAtom = sakaiId.substring(lastSlash + 1);
				bean.addFile(sakaiId, "attachments/msgcntr-topic-" + topic.getId() + "/" + lastAtom, null);
			    }
			}
		    }
		}
	    }
	}
	return ret;
    }

    public boolean outputEntity(String forumRef, ZipPrintStream out, PrintStream errStream, CCExport bean, CCExport.Resource resource, int version) {

	int i = forumRef.indexOf("/");
	String forumString = forumRef.substring(i+1);
	Long forumId = new Long(forumString);

	Topic topic = forumManager.getTopicById(true, forumId);

	String title = topic.getTitle();
	String text = topic.getExtendedDescription();  // html
	if (text == null || text.trim().equals("")) {
	    text = topic.getShortDescription();
	    if (text != null)
		text = FormattedText.convertPlaintextToFormattedText(text);
	}
	if (text == null)
	    text = "";

	List<Attachment> attachments = topic.getAttachments();

// according to the spec, attachments must be Learnning Object web content. That is, they can
// be files but not URLs, and they must be in a special directory for this forum topic.
// Since we need to be able to support URLs, don't include any attachments. Instead, 
// append it as URLs at the end of the document. 

	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	switch (version) {
	case CCExport.V11:
	    out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1\"");
	    out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imsdt_v1p1.xsd\">");
	    break;
	default:
	    out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p2/imsdt_v1p2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p2/imsdt_v1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_imsdt_v1p2.xsd\">");
	}
	out.println("  <title>" + StringEscapeUtils.escapeXml(title) + "</title>");

	ccExport = bean;

	text = "<div>" + text + "</div>";

	for (Attachment attachment: attachments) {
	    String sakaiId = attachment.getAttachmentId();

	    String location = bean.getLocation(sakaiId);
	    int lastSlash = sakaiId.lastIndexOf("/");
	    String lastAtom = sakaiId.substring(lastSlash + 1);

	    String URL = null;

	    try {
		ContentResource res = ContentHostingService.getResource(sakaiId);
		String type = res.getContentType();
		if ("text/url".equals(type)) {
		    URL = new String(res.getContent());
		}
	    } catch (Exception e) {
	    }

	    if (URL != null)
		lastAtom = URL; // for URL use the whole URL for the text
	    else {
		URL = "../" + bean.getLocation(sakaiId); 
		URL = "$IMS-CC-FILEBASE$" + Validator.escapeUrl(URL.replaceAll("//", "/"));
	    }
	    text += "\n<a href=\"" + URL + "\">" + StringEscapeUtils.escapeHtml(lastAtom) + "</a><br/>";
	    bean.addDependency(resource, sakaiId);
	}
	out.println("  <text texttype=\"text/html\">" + bean.fixup(text, resource) + "</text>");
	out.println("</topic>");

	return true;
   }

}












































































































































































































































































































































































