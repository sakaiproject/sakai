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

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.lessonbuildertool.ccexport.ForumsExport.ForumItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;

/* 
 * Most contrib code is put in opt-src. However this uses no
 * APIs from jforum, and it's disabled if jforum isn't in the system,
 * so there's no obvious reason to exclude it from the default build.
 */

/*
 * set up as a singleton, but also instantiated by CCExport.
 * The purpose of the singleton setup is just to get the dependencies.
 * So they are all declared static.
 */
@Slf4j
public class JForumExport extends ForumsExport {
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

    public void setPrev(ForumsExport p) {
	p.setNext(this);
    }

    static boolean haveJforum = false;

    static final String ATTACHMENTS_STORE_DIR = "etudes.jforum.attachments.store.dir";

    public void init () {

	// they changed the capitalization, so check for both
	if (ComponentManager.get("org.etudes.api.app.jforum.JforumService") != null ||
	    ComponentManager.get("org.etudes.api.app.jforum.JForumService") != null)
	    haveJforum = true;
	log.info("JforumEntity init: haveJforum = " + haveJforum);

	log.info("init()");

    }

    public void destroy()
    {
	log.info("destroy()");
    }

    public List<ForumItem> getItemsInSite(String siteId) {
	List<ForumItem> ret = new ArrayList<ForumItem>();

	if(!haveJforum) {
	    // Forums is not in this site. Move on to the next provider.
	    if (next != null)
		return next.getItemsInSite(siteId);
	    return null;
	}

	String siteRef = "/group/" + siteId + "/";

	Site site = null;
	try {
	    site = SiteService.getSite(siteId);
	} catch (Exception impossible) {
	    // impossible, one hopes
	    return null; // site doesn't exist. no point trying another provider
	}
    	
	ToolConfiguration siteTool = site.getToolForCommonId("sakai.jforum.tool");

	if(siteTool == null) {
	    // Forums is not in this site. Move on to the next provider.
	    if (next != null)
		return next.getItemsInSite(siteId);
	    return null;
	}

	Connection connection = null;
	try {
	    connection = SqlService.borrowConnection();
	    // jforum_sakai_course_categories: course_id, categories_id

	    String sql="select b.categories_id from jforum_sakai_course_categories a,jforum_categories b where a.course_id=? and a.categories_id = b.categories_id";
	    Object fields[] = new Object[1];
	    fields[0] = siteId;

	    List<Integer>categories = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    return result.getInt(1);
			} catch (Exception ignore) {};
			return null;
		    }
		});

	    if (categories != null && categories.size() > 0)
		for (Integer c : categories) {

		    sql = "select forum_id from jforum_forums where categories_id = ?";
		    fields[0] = c;

		    List<Integer>forums = SqlService.dbRead(connection, sql, fields, new SqlReader()
			{
			    public Object readSqlResultRecord(ResultSet result)
			    {
				try {
				    return result.getInt(1);
				} catch (Exception ignore) {};
				return null;
			    }
			});

		    if (forums != null && forums.size() > 0) 
			for (Integer f : forums) {
		    
			    sql = "select topic_id from jforum_topics where forum_id = ?";
			    fields[0] = f;
			    
			    List<Integer>topics = SqlService.dbRead(connection, sql, fields, new SqlReader()
				{
				    public Object readSqlResultRecord(ResultSet result)
				    {
					try {
					    return result.getInt(1);
					} catch (Exception ignore) {};
					return null;
				    }
				});

			    if (topics != null && topics.size() > 0) {
				for (Integer t: topics) {
				    ForumItem item = new ForumItem();
				    item.id = LessonEntity.JFORUM_TOPIC + "/" + t;

				    // found a topic. Need attachments
				    sql = "select c.physical_filename, c.real_filename from jforum_topics a,jforum_attach b, jforum_attach_desc c where a.topic_id=? and a.topic_first_post_id=b.post_id and b.attach_id=c.attach_id";
				    fields[0] = t;

				    item.attachments = SqlService.dbRead(connection, sql, fields, new SqlReader() {
					    public ForumAttachment readSqlResultRecord(ResultSet result) {
						try {
						    ForumAttachment a = new ForumAttachment();
						    a.logical = result.getString(2);

						    String jforumBase = ServerConfigurationService.getString(ATTACHMENTS_STORE_DIR);
						    a.physical = "///" + jforumBase + "/" + result.getString(1);
						    return a;
						} catch (Exception ignore) {};
						return null;
					    }});

				    ret.add(item);

				}
			    }
			}
		}
	} catch (Exception e) {
	    log.error("JForum Lesson Builder find all in site error " + e);
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}

	if (next != null) {
	    List<ForumItem> newItems = next.getItemsInSite(siteId);
	    if (newItems != null)
		ret.addAll(newItems);
	}

	return ret;
    }

    public ForumItem getContents(String forumRef) {

	if (!forumRef.startsWith(LessonEntity.JFORUM_TOPIC + "/")) {
	    if (next == null)
		return null;
	    else 
		return next.getContents(forumRef);
	}

	int i = forumRef.indexOf("/");
	String forumString = forumRef.substring(i+1);
	Integer forumId = new Integer(forumString);

	Connection connection = null;
	try {
	    connection = SqlService.borrowConnection();
	    // jforum_sakai_course_categories: course_id, categories_id

	    String sql="select a.topic_title, b.post_text from jforum_topics a, jforum_posts_text b where a.topic_id = ? and a.topic_first_post_id = b.post_id";
	    Object fields[] = new Object[1];
	    fields[0] = forumId;

	    List<ForumItem>topics = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public ForumItem readSqlResultRecord(ResultSet result)
		    {
			try {
			    ForumItem item = new ForumItem();
			    item.title = result.getString(1);
			    item.text = result.getString(2);
			    return item;
			} catch (Exception ignore) {};
			return null;
		    }
		});
	    
	    if (topics.size() < 1)
		return null;

	    ForumItem item = topics.get(0);
	    item.id = forumRef;

	    sql = "select c.physical_filename, c.real_filename from jforum_topics a,jforum_attach b, jforum_attach_desc c where a.topic_id=? and a.topic_first_post_id=b.post_id and b.attach_id=c.attach_id";
	    
	    item.attachments = SqlService.dbRead(connection, sql, fields, new SqlReader() {
		    public ForumAttachment readSqlResultRecord(ResultSet result) {
			try {
			    ForumAttachment a = new ForumAttachment();
			    a.logical = result.getString(2);
			    String jforumBase = ServerConfigurationService.getString(ATTACHMENTS_STORE_DIR);
			    a.physical = "///" + jforumBase + "/" + result.getString(1);
			    return a;
			} catch (Exception ignore) {};
			return null;
		    }});

	    return item;

	} catch (Exception e) {
	    log.error("JForum Lesson Builder find all in site error " + e);
	    return null;
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}

    }


}
