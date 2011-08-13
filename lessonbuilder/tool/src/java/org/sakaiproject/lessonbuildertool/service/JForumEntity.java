/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Date;
import javax.servlet.ServletRequest;
import javax.servlet.ServletContext;

import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.tool.api.ToolManager;
import java.sql.Connection;
import java.sql.ResultSet;

import uk.org.ponder.messageutil.MessageLocator;

/**
 * Interface to JForums, an optional forums system from Foothills
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */

// NOTE: almost no other class should import this. We want to be able
// to support both forums and jforum. So typically there will be a 
// forumEntity, but it's injected, and it can be either forum and jforum.
// Hence it has to be declared LessonEntity. That leads to a lot of
// declarations like LessonEntity forumEntity.  In this case forumEntity
// means either a ForumEntity or a JForumEntity. We can't just call the
// variables lessonEntity because the same module will probably have an
// injected class to handle tests and quizes as well. That will eventually
// be converted to be a LessonEntity.

public class JForumEntity implements LessonEntity, ForumInterface {

    static boolean initdone = false;
    static boolean haveJforum = false;

    public void init() {

	if (toolManager.getTool("sakai.jforum.tool") != null)
	    haveJforum = true;
	System.out.println("JforumEntity init: haveJforum = " + haveJforum);
    }

    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized
    // static, because only the bean configured in applicationContext get these set

    private static LessonEntity nextEntity = null;
    public void setNextEntity(LessonEntity e) {
	nextEntity = e;
    }
    public LessonEntity getNextEntity() {
	return nextEntity;
    }

    private static ToolManager toolManager = null;
    public void setToolManager(ToolManager t) {
	toolManager = t;
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    protected JForumEntity() {
    }

    protected JForumEntity(int type, int id, int level) {
	this.type = type;
	this.id = id;
	this.level = level;
    }

    protected JForumEntity(int type, int id, int level, String name) {
	this.type = type;
	this.id = id;
	this.level = level;
	this.name = name;
    }

    public String getToolId() {
	return "sakai.jforum.tool";
    }

    // standard info about object
    protected int id;
    protected int type;
    protected int level;
    protected String name = null;
    protected String url = null;

    // type of the underlying object
    public int getType() {
	return type;
    }

    public int getLevel() {
	return level;
    }

    public int getTypeOfGrade() {
	return 1;
    }

    public boolean isUsable() {
	if (type == TYPE_JFORUM_TOPIC)
	    return true;
	else
	    return false;
    }

    public String getReference() {
	if (type == TYPE_JFORUM_TOPIC)
	    return "/" + JFORUM_TOPIC + "/" + id;
	else if (type == TYPE_JFORUM_CATEGORY)
	    return "/" + JFORUM_CATEGORY + "/" + id;
	else
	    return "/" + JFORUM_FORUM + "/" + id;
    }

    public List<LessonEntity> getEntitiesInSite() {
	return getEntitiesInSite(null);
    }

    // find topics in site, but organized by forum and category
    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean) {
	// all other code is driven by current objects. If we skip this code, nothing else
	// in this module will be called.
	if (!haveJforum) {
	    if (nextEntity != null) 
		return nextEntity.getEntitiesInSite();
	    else
		return new ArrayList<LessonEntity>();
	}
	
	List<LessonEntity>ret = new ArrayList<LessonEntity>();
	
	// LSNBLDR-21. If the tool is not in the current site we shouldn't query
	// for topics owned by the tool.
	String siteId = toolManager.getCurrentPlacement().getContext();
	
	Site site = null;
	try {
	    site = SiteService.getSite(siteId);
	} catch (Exception impossible) {
	    return ret;
	}
    	
    ToolConfiguration siteTool = site.getToolForCommonId("sakai.jforum.tool");
	
    if(siteTool == null) {
    	
    	// JForum is not in this site. Move on to the next provider.
    	
    	if (nextEntity != null) 
    		ret.addAll(nextEntity.getEntitiesInSite());
    	
    	return ret;
    }

	String url = null;

	try {
	    // String toolid = "8f83cd4b-74ca-4428-0055-85ddd19a8d00";
	    url = "/portal/tool/" + siteTool.getId() + "/posts/list/";

	    // String toolid = "8f83cd4b-74ca-4428-0055-85ddd19a8d00";
	} catch (Exception e) {
	    System.out.println("tool problem " + e);
	}

	Connection connection = null;
	try {
	    connection = SqlService.borrowConnection();
	    // jforum_sakai_course_categories: course_id, categories_id

	    String sql="select b.categories_id, b.title from jforum_sakai_course_categories a, jforum_categories b where a.course_id=? and a.categories_id = b.categories_id order by b.display_order";
	    Object fields[] = new Object[1];
	    fields[0] = siteId;

	    List<JForumEntity>categories = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    return new JForumEntity(TYPE_JFORUM_CATEGORY, result.getInt(1), 1, result.getString(2));
			} catch (Exception ignore) {};
			return null;
		    }
		});

	    if (categories != null && categories.size() > 0)
		for (JForumEntity c : categories) {
		    boolean categoryAdded = false;

		    sql = "select forum_id,forum_name from jforum_forums where categories_id = ? order by forum_order";
		    fields[0] = c.id;

		    List<JForumEntity>forums = SqlService.dbRead(connection, sql, fields, new SqlReader()
			{
			    public Object readSqlResultRecord(ResultSet result)
			    {
				try {
				    return new JForumEntity(TYPE_JFORUM_FORUM, result.getInt(1), 1, result.getString(2));
				} catch (Exception ignore) {};
				return null;
			    }
			});

		    if (forums != null && forums.size() > 0) 
			for (JForumEntity f : forums) {
		    
			    sql = "select topic_id,topic_title from jforum_topics where forum_id = ? order by topic_time";
			    fields[0] = f.id;
			    
			    List<JForumEntity>topics = SqlService.dbRead(connection, sql, fields, new SqlReader()
				{
				    public Object readSqlResultRecord(ResultSet result)
				    {
					try {
					    return new JForumEntity(TYPE_JFORUM_TOPIC, result.getInt(1), 2, result.getString(2));
					} catch (Exception ignore) {};
					return null;
				    }
				});

			    if (topics != null && topics.size() > 0) {
				if (!categoryAdded) {
				    ret.add(c);
				    categoryAdded = true;
				}
				ret.add(f);
				for (JForumEntity t: topics) {
				    t.url = url + t.id + ".page";
				    ret.add(t);
				}
			    }
			}
		}
	} catch (Exception e) {
	    System.out.println("JForum Lesson Builder find all in site error " + e);
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}

	if (nextEntity != null) 
	    ret.addAll(nextEntity.getEntitiesInSite(bean));

	return ret;
    }

    public LessonEntity getEntity(String ref, SimplePageBean o) {
	return getEntity(ref);
    }

    public LessonEntity getEntity(String ref) {
	int i = ref.indexOf("/",1);
	if (i < 0)
	    return null;
	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);
	int id = 0;
	if (typeString.equals(JFORUM_TOPIC) || typeString.equals(JFORUM_FORUM) || typeString.equals(JFORUM_CATEGORY)) {
		try {
			id = Integer.parseInt(idString);
		} catch (Exception ignore) {
			return null;
		}
	}

	// note: I'm returning the minimal structures, not those with
	// topics and postings attached
	if (typeString.equals(JFORUM_TOPIC)) {
	    return new JForumEntity(TYPE_JFORUM_TOPIC, id, 2);
	} else if (typeString.equals(JFORUM_FORUM)) {
	    return new JForumEntity(TYPE_JFORUM_FORUM, id, 1);
	} else if (typeString.equals(JFORUM_CATEGORY)) {
	    return new JForumEntity(TYPE_JFORUM_CATEGORY, id, 1);
	} else if (nextEntity != null) {
	    return nextEntity.getEntity(ref);
	} else
	    return null;
	    
    }
	

    // properties of entities
    public String getTitle() {
	if (name != null)
	    return name;

	Connection connection = null;
	try {
	    connection = SqlService.borrowConnection();
	    String sql = null;

	    if (type == TYPE_JFORUM_TOPIC)
		sql = "select topic_title from jforum_topics where topic_id = ?";
	    else if (type == TYPE_JFORUM_CATEGORY)
		sql = "select title from jforum_categories where categories_id =?";
	    else
		sql = "select forum_name from jforum_forums where forum_id=?";

	    Object fields[] = new Object[1];
	    fields[0] = (Integer)id;

	    List<String>titles = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    return result.getString(1);
			} catch (Exception ignore) {};
			return null;
		    }
		});

	    if (titles != null && titles.size() > 0) 
		name = titles.get(0);

	} catch (Exception e) {
	    System.out.println("JForum Lesson Builder get name error " + e);
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}

	return name;
    }


    public String getUrl() {
	if (url != null)
	    return url;
	
	String siteId = toolManager.getCurrentPlacement().getContext();
	
	Site site = null;
	try {
	    site = SiteService.getSite(siteId);
	} catch (Exception impossible) {
	    return null;
	}
	
	ToolConfiguration siteTool = site.getToolForCommonId("sakai.jforum.tool");
	
	// LSNBLDR-21. If the tool is not in the current site we shouldn't return a url
	if(siteTool == null) {
	    return null;
	}

	String prefix = null;
	try {
	    // String toolid = "8f83cd4b-74ca-4428-0055-85ddd19a8d00";
	    prefix = "/portal/tool/" + siteTool.getId();
	    // String toolid = "8f83cd4b-74ca-4428-0055-85ddd19a8d00";
	} catch (Exception e) {
	    System.out.println("tool problem " + e);
	    return null;
	}

	if (type == TYPE_JFORUM_TOPIC)
	    url = prefix + "/posts/list/" + id + ".page";
	else if (type == TYPE_JFORUM_CATEGORY)
	    url = prefix + "/forums/list.page"; // no way to go directly to a category
	else // forum
	    url = prefix + "/forums/show/" + id + ".page";

	return url;
    }

    // I don't think they have this
    public Date getDueDate() {
	return null;
    }

    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException {
	return false;
	// not yet
    };
	
    public boolean removeEntityControl(String siteId, String groupId) throws IOException {
	return false;
	// not yet
    };

    // submission
    // do we need the data from submission?
    //  not for the moment. If a posting is required, we just check whether one
    //  has been done. While you can grade submissions, grading is done manually
    //  later. It's unlikely that faculty will want to test on those grades
    public boolean needSubmission(){
	return false;
    }
    public LessonSubmission getSubmission(String user) {
	return null; // not used
    }
    public int getSubmissionCount(String user) {

	Connection connection = null;
	try {
	    connection = SqlService.borrowConnection();
	    String sql = "select count(a.post_id) from jforum_posts a, jforum_users b where a.topic_id=? and b.sakai_user_id=? and a.user_id=b.user_id";

	    Object fields[] = new Object[2];
	    fields[0] = (Integer)id;
	    fields[1] = user;

	    List<Integer>counts = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    return result.getInt(1);
			} catch (Exception ignore) {};
			return null;
		    }
		});

	    if (counts != null && counts.size() > 0) {
		return counts.get(0);
	    }

	} catch (Exception e) {
	    System.out.println("JForum Lesson Builder get name error " + e);
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}

	return 0;
    }

    // URL to create a new item. Normally called from the generic entity, not a specific one                                                 
    // can't be null                                                                                                                         
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	ArrayList<UrlItem> list = new ArrayList<UrlItem>();
	String tool = bean.getCurrentTool("sakai.jforum.tool");
	if (tool != null) {
	    tool = "/portal/tool/" + tool + "/forums/list.page";
	    list.add(new UrlItem(tool, messageLocator.getMessage("simplepage.create_jforum")));
	}
	if (nextEntity != null)
	    list.addAll(nextEntity.createNewUrls(bean));
	return list;
    }

    // URL to edit an existing entity.                                                                                                       
    // Can be null if we can't get one or it isn't needed                                                                                    
    public String editItemUrl(SimplePageBean bean) {
	return getUrl();
    }


    // for most entities editItem is enough, however tests allow separate editing of                                                         
    // contents and settings. This will be null except in that situation                                                                     
    public String editItemSettingsUrl(SimplePageBean bean) {
	return null;
    }

    public String importObject(String title, String topicTitle, String text, boolean texthtml, String base, String siteId,  List<String>attachmenthrefs) {
	return SimplePageItem.DUMMY;
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public Collection<String> getGroups(boolean nocache) {
	return null;
    }

    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    public void setGroups(Collection<String> groups) {
    }

}
