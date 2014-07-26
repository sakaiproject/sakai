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

import org.w3c.dom.Document;

import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.component.cover.ComponentManager;

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
 * set up as a singleton, but CCExport is not
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

    static ForumsExport next = null;
    public void setNext(ForumsExport n) {
	next = n;
    }

    static MessageForumsForumManager forumManager = (MessageForumsForumManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsForumManager");
    static MessageForumsMessageManager messageManager = (MessageForumsMessageManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");

    public void init () {
	// currently nothing to do

	log.info("init()");

    }

    public void destroy()
    {
	log.info("destroy()");
    }

    public static class ForumAttachment {
	public String logical;
	public String physical;
    }

    public static class ForumItem {
	public String id;
	public String title;
	public String text;
	public List<ForumAttachment> attachments;
    }

    public List<ForumItem> getItemsInSite(String siteId) {
	List<ForumItem> ret = new ArrayList<ForumItem>();

	String siteRef = "/group/" + siteId + "/";

	Site site = null;
	try {
	    site = SiteService.getSite(siteId);
	} catch (Exception impossible) {
	    // if site doesn't exist, it seems silly to try any more tools
	    return null;
	}

	if(site.getToolForCommonId("sakai.forums") == null) {
	    // Forums is not in this site. Move on to the next provider.
	    if (next != null)
		return next.getItemsInSite(siteId);
	    return null;
	}

	for (DiscussionForum forum: forumManager.getForumsForMainPage()) {
	    if (!forum.getDraft()) {
		for (DiscussionTopic topic: (Set<DiscussionTopic>)forum.getTopicsSet()) {
		    if (topic.getDraft().equals(Boolean.FALSE)) {
			ForumItem item = new ForumItem();
			item.attachments = new ArrayList<ForumAttachment>();
			item.id = LessonEntity.FORUM_TOPIC + "/" + topic.getId();

			List<Attachment> attachments = topic.getAttachments();
			for (Attachment attachment: attachments) {
			    String sakaiId = attachment.getAttachmentId();
			    ForumAttachment a = new ForumAttachment();
			    a.logical = sakaiId;
			    a.physical = sakaiId;
			    item.attachments.add(a);
			}
			ret.add(item);
		    }
		}
	    }
	}

	if (next != null) {
	    List<ForumItem> newItems = next.getItemsInSite(siteId);
	    if (newItems != null)
		ret.addAll(newItems);
	}

	return ret;
    }

    // find topics in site
    public List<String> getEntitiesInSite(String siteId, CCExport bean) {

	String siteRef = "/group/" + siteId + "/";

	List<String> ret = new ArrayList<String>();

	List<ForumItem> items = getItemsInSite(siteId);

	if (items != null)
  	  for (ForumItem item: items) {

	    ret.add(item.id);

	    List<ForumAttachment> attachments = item.attachments;
	    if (attachments != null) {
	      for (ForumAttachment attach: attachments) {
		// this code is to identify attachments that aren't in the normal
		// site resources. In that case we have to make a copy of it
		String url = null;
		// if it is a URL, need the URL rather than copying the file
		String logical = attach.logical;
		String physical = attach.physical;
		if (!physical.startsWith("///")) {
		    try {
			ContentResource res = ContentHostingService.getResource(physical);
			if (CCExport.islink(res)) {
			    url = new String(res.getContent());
			}
		    } catch (Exception e) {
		    }
		}
			
		if (url != null)
		    ;  // if it's a URL we don't need a file
		else if (! physical.startsWith(siteRef)) {  // if in resources, already included
		    int lastSlash = logical.lastIndexOf("/");
		    String lastAtom = logical.substring(lastSlash + 1);
		    bean.addFile(physical, "attachments/" + item.id + "/" + lastAtom, null);
		}
	      }
	    }
	}
	return ret;
    }

    public ForumItem getContents(String forumRef) {

	if (!forumRef.startsWith(LessonEntity.FORUM_TOPIC + "/")) {
	    if (next == null)
		return null;
	    else 
		return next.getContents(forumRef);
	}

	int i = forumRef.indexOf("/");
	String forumString = forumRef.substring(i+1);
	Long forumId = new Long(forumString);

	Topic topic = forumManager.getTopicById(true, forumId);
	if (topic == null)
	    return null;

	ForumItem ret = new ForumItem();

	ret.id = forumRef;
	ret.title = topic.getTitle();
	String text = topic.getExtendedDescription();  // html
	if (text == null || text.trim().equals("")) {
	    text = topic.getShortDescription();
	    if (text != null)
		text = FormattedText.convertPlaintextToFormattedText(text);
	}
	if (text == null)
	    text = "";
	ret.text = text;
	ret.attachments = new ArrayList<ForumAttachment>();

	List<Attachment> attachments = topic.getAttachments();
	for (Attachment attachment: attachments) {
	    String sakaiId = attachment.getAttachmentId();
	    ForumAttachment a = new ForumAttachment();
	    a.logical = sakaiId;
	    a.physical = sakaiId;

	    ret.attachments.add(a);
	}

	return ret;
    }

    public boolean outputEntity(String forumRef, ZipPrintStream out, PrintStream errStream, CCExport bean, CCExport.Resource resource, int version) {

	ForumItem item = getContents(forumRef);

// according to the spec, attachments must be Learnning Object web content. That is, they can
// be files but not URLs, and they must be in a special directory for this forum topic.
// Since we need to be able to support URLs, don't include any attachments. Instead, 
// append it as URLs at the end of the document. 
//   However none of their examples actually work this way. So I reimplemented it using
// actual attachments.

	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	switch (version) {
	case CCExport.V11:
	    out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1\"");
	    out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imsdt_v1p1.xsd\">");
	    break;
	case CCExport.V13:
	    out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3\"");
	    out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imsdt_v1p3.xsd\">");
	    break;
	default:
	    out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p2/imsdt_v1p2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p2/imsdt_v1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_imsdt_v1p2.xsd\">");
	}
	out.println("  <title>" + StringEscapeUtils.escapeXml(item.title) + "</title>");

	boolean useAttachments = (item.attachments.size() > 0);
	List<String>attachments = new ArrayList<String>();
	
	// see if we can use <attachments>. We can't if any of the attachments are a URL
	// construct a new list, which is SakaiIds, in case we need to use outputAttachments
	for (ForumAttachment a: item.attachments) {
	    String sakaiId = a.physical;
	    if (sakaiId.startsWith("/content/"))
		sakaiId = sakaiId.substring("/content".length());
	    attachments.add(sakaiId);

	    String URL = null;
	    // if it is a URL, need the URL rather than copying the file
	    if (!sakaiId.startsWith("///")) {
		try {
		    ContentResource res = ContentHostingService.getResource(sakaiId);
		    if (CCExport.islink(res)) {
			useAttachments = false;
		    }
		} catch (Exception e) {
		}
	    }
	}

	String text = bean.fixup("<div>" + item.text + " </div>", resource);

	if (useAttachments || item.attachments.size() == 0 ) 
	    out.println("  <text texttype=\"text/html\">" + text + "</text>");
	else
	    out.println("  <text texttype=\"text/html\">" + text + StringEscapeUtils.escapeXml(AssignmentExport.outputAttachments(resource, attachments, bean, "$IMS-CC-FILEBASE$../")) + "</text>");

	if (useAttachments) {
	    out.println("  <attachments>");

	    for (ForumAttachment a: item.attachments) {

	    String logical = a.logical;
	    String physical = a.physical;
	    String location = bean.getLocation(physical);
	    int lastSlash = logical.lastIndexOf("/");
	    String lastAtom = logical.substring(lastSlash + 1);

	    String URL = null;

	    if (!physical.startsWith("///")) {
		try {
		    ContentResource res = ContentHostingService.getResource(physical);
		    if (CCExport.islink(res)) {
			URL = new String(res.getContent());
		    }
		} catch (Exception e) {
		}
	    }

	    // the spec doesn't seem to ask for URL encoding on file names
	    if (URL != null)
		lastAtom = URL; // for URL use the whole URL for the text
	    else {
		URL = "../" + bean.getLocation(physical); 
		URL = StringEscapeUtils.escapeXml(URL.replaceAll("//", "/"));
	    }
	    out.println("    <attachment href=\"" + URL + "\"/>");
	    bean.addDependency(resource, physical);
	    }

	    out.println("  </attachments>");
	}
	out.println("</topic>");

	return true;
   }

}












































































































































































































































































































































































