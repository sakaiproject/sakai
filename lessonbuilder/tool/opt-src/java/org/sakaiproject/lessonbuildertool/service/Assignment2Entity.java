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

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.jdom.Element;
import org.jdom.Namespace;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.assignment2.model.Assignment2;
import org.sakaiproject.assignment2.model.AssignmentAttachment;
import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.service.gradebook.shared.*;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Validator;

/**
 * Interface to Assignment
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
@Slf4j
public class Assignment2Entity implements LessonEntity, AssignmentInterface {

    class Assignment {
	String context;
	Date dueTime;
	Long gradebookitem;
	String title;
    }

    class AssignGroup {
	boolean update;
	Long id;
	Long version;
	String groupid;
    }

    private static Cache assignmentCache = null;
    protected static final int DEFAULT_EXPIRATION = 10 * 60;
    static boolean haveA2 = false;
    Object dao = null;

    private SimplePageBean simplePageBean;

    public void setSimplePageBean(SimplePageBean simplePageBean) {
	this.simplePageBean = simplePageBean;
    }

    private LessonEntity nextEntity = null;
    public void setNextEntity(LessonEntity e) {
	nextEntity = e;
    }
    public LessonEntity getNextEntity() {
	return nextEntity;
    }
    
    public void setPrevEntity(LessonEntity e) {
	e.setNextEntity(this);
    }

    static MemoryService memoryService = null;
    public void setMemoryService(MemoryService m) {
	memoryService = m;
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    static GradebookService gradebookService = null;
    public void setGradebookService(GradebookService g) {
	gradebookService = g;
    }

    static OptSql optSql = null;
    Method saveMethod = null;

    public void init () {
    //	if (ToolManager.getTool("sakai.assignment2") != null)
	if (ComponentManager.get("org.sakaiproject.assignment2.service.api.Assignment2Service") != null)
	    haveA2 = true;

	if (haveA2) {
	    assignmentCache = memoryService
		.getCache("org.sakaiproject.lessonbuildertool.service.Assignment2Entity.cache");

	    // Unfortunately the interface class is part of the component, not the shared library,
	    // so we can't get to it. The only way to invoke stuff in the DAO is through introspection.
	    dao = ComponentManager.get("org.sakaiproject.assignment2.dao.AssignmentDao");
	    // get the methods we need from the DAO
	    try {
		saveMethod = dao.getClass().getMethod("save", Object.class);
	    } catch (Exception f) {
		log.info("assignment2 lessons interface unable to get save method from A2 dao " + f);
	    };

	    //try {
	    //		Method [] methods = dao.getClass().getMethods();
	    //		for (int i = 0; i < methods.length; i++) {
	    //		    Method method = methods[i];
	    //		    if (method.getName().equals("save")) {
	    //			saveMethod = method;
	    //		    }
	    //		}
	    //	    } catch (Exception e) {
	    //		log.info("getmethod failed " + e);
	    //	    }

	    String vendor = SqlService.getVendor();
	    try {
		optSql = (OptSql) Assignment2Entity.class.getClassLoader().loadClass("org.sakaiproject.lessonbuildertool.service.OptSql" + vendor).newInstance();
	    } catch (Exception e) {
		try {
		    optSql = (OptSql) Assignment2Entity.class.getClassLoader().loadClass("org.sakaiproject.lessonbuildertool.service.OptSqlDefault").newInstance();
		} catch (Exception ee) {
		}
	    }
	}
	log.info("init()");

    }

    public void destroy()
    {
	if (haveA2)
	    assignmentCache.destroy();
	assignmentCache = null;

	log.info("destroy()");
    }


    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

    protected Assignment2Entity() {
    }

    /* id is a string containing a long in this case */
    protected Assignment2Entity(int type, String id, int level) 
	throws NumberFormatException {
	this.type = type;
	this.id = Long.parseLong(id);
	this.level = level;
    }

    public String getToolId() {
	return "sakai.assignment2";
    }

    // the underlying object, something Sakaiish
    protected Long id;
    protected int type;
    protected int level;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them
    protected Assignment assignment;

    // ref looks like /assignment2/id
    public Assignment getAssignment(Long id) {
	return getAssignment(id, false);
    }

    public Assignment getAssignment(Long id, boolean nocache) {
	Assignment ret = (Assignment)assignmentCache.get(id.toString());
	if (!nocache && ret != null)
	    return ret;

	Connection connection = null;
	try {
	    String siteId = ToolManager.getCurrentPlacement().getContext();
	    connection = SqlService.borrowConnection();
	    String sql="select context, due_date, gradebook_item_id, title from A2_ASSIGNMENT_T where assignment_id = ? and draft=0 and removed=0";
	    Object fields[] = new Object[1];
	    fields[0] = id;

	    List<Assignment>assignments = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    Assignment a = new Assignment();
			    a.context = result.getString(1);
			    a.dueTime = result.getDate(2);
			    a.gradebookitem = result.getLong(3);
			    a.title = result.getString(4);
			    return a;
			} catch (Exception ignore) {};
			return null;
		    }
		});

	    if (assignments.size() == 1) {
		ret = assignments.get(0);
		if (!ret.context.equals(siteId))
		    ret = null;  // assignment not in current site; security problem
	    }
	    
	} catch (Exception e) {
	    log.info("Assignment2Entity Eexception " + e);
	    ret = null;
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}	
	if (ret != null)
	    assignmentCache.put(id.toString(), ret, DEFAULT_EXPIRATION);
	return ret;
    }

    // type of the underlying object
    public int getType() {
	return type;
    }

    public int getLevel() {
	return level;
    }

    public int getTypeOfGrade() {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return 1;

	if (assignment.gradebookitem == 0)
	    return 1;
	else 
	    return 3;
    }

  // hack for forums. not used for assessments, so always ok
    public boolean isUsable() {
	return true;
    }

    public String getReference() {
	return "/" + ASSIGNMENT2 + "/" + id;
    }

    public List<LessonEntity> getEntitiesInSite() {
	return getEntitiesInSite(null);
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite(final SimplePageBean bean) {

	if (!haveA2) {
	    if (nextEntity != null) 
		return nextEntity.getEntitiesInSite();
	    else
		return new ArrayList<LessonEntity>();
	}

	Connection connection = null;
	List <LessonEntity> ret = new ArrayList<LessonEntity>();
	try {
	    String siteId = ToolManager.getCurrentPlacement().getContext();
	    connection = SqlService.borrowConnection();
	    String sql="select context, due_date, gradebook_item_id, title, assignment_id from A2_ASSIGNMENT_T where context = ? and draft=0 and removed=0";
	    Object fields[] = new Object[1];
	    fields[0] = siteId;

	    ret = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    Assignment a = new Assignment();
			    a.context = result.getString(1);
			    a.dueTime = result.getDate(2);
			    a.gradebookitem = result.getLong(3);
			    a.title = result.getString(4);
			    Assignment2Entity a2 = new Assignment2Entity(TYPE_ASSIGNMENT2, result.getString(5), 1);
			    a2.assignment = a;
			    a2.simplePageBean = bean;
			    return a2;
			} catch (Exception ignore) {};
			return null;
		    }
		});

	} catch (Exception e) {
	    // leave ret as null list
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

    public LessonEntity getEntity(String ref) {
	return getEntity(ref, null);
    }

    public LessonEntity getEntity(String ref, SimplePageBean bean) {

	int i = ref.indexOf("/",1);

	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);

	if (typeString.equals(ASSIGNMENT2)) {
	    Assignment2Entity entity = new Assignment2Entity(TYPE_ASSIGNMENT2, idString, 1); // 
	    entity.setSimplePageBean(bean);
	    return entity;
	} else if (nextEntity != null) {
	    return nextEntity.getEntity(ref, bean);
	} else
	    return null;
    }
	

    // properties of entities
    public String getTitle() {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	return assignment.title;
    }

    // http://heidelberg.rutgers.edu/portal/tool/575de542-a928-41a8-aab0-348a67e2ccc1/student-submit/5
    public String getUrl() {
	

	if (simplePageBean != null) {
	    return ServerConfigurationService.getToolUrl() + "/" + simplePageBean.getCurrentTool("sakai.assignment2") + 
		"/student-submit/" + id;
	}

	Site site = null;
	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	} catch (Exception impossible) {
	    return null;
	}

	ToolConfiguration tool = site.getToolForCommonId("sakai.assignment2");
	
	if(tool == null) {
	    return null;
	}
	
	String placement = tool.getId();
	return ServerConfigurationService.getToolUrl()+ "/" + placement + "/student-submit/" + id;

	// following is broken in 2.8.1
        // return "/direct/assignment2/" + id;

    }

    public Date getDueDate() {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	return assignment.dueTime;
    }

    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    List<AssignGroup> getAssignGroups() {
	Connection connection = null;
	try {
	    connection = SqlService.borrowConnection();
	    String sql="select assignment_group_id, version, group_ref from A2_ASSIGN_GROUP_T where assignment_id = ?";
	    Object fields[] = new Object[1];
	    fields[0] = id;

	    List<AssignGroup>assignGroups = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    AssignGroup a = new AssignGroup();
			    a.id = result.getLong(1);
			    a.version = result.getLong(2);
			    a.groupid = result.getString(3);
			    a.update = false;
			    return a;
			} catch (Exception ignore) {};
			return null;
		    }
		});

	    return assignGroups;
	    
	} catch (Exception e) {
	    return null;
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}	

    }

    // access control
    // no longer used, so there's no way to test them
    public boolean addEntityControl(String siteId, final String groupId) throws IOException {
	return false;

    }

    public boolean removeEntityControl(String siteId, String groupId) throws IOException {
	return false;
    }

    // submission
    // do we need the data from submission?
    public boolean needSubmission(){
	return true;
    }

    public Double toDouble(Object f) {
	if (f instanceof Double)
	    return (Double)f;
	else if (f instanceof Float)
	    return ((Float)f).doubleValue();
        else
	    return null;
    }

    public LessonSubmission getSubmission(String userId) {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null) {
	    log.warn("can't find assignment " + id);
	    return null;
	}
	Connection connection = null;
	try {
	    connection = SqlService.borrowConnection();
	    String sql="select completed from A2_SUBMISSION_T where assignment_id = ? and user_id = ?";
	    Object fields[] = new Object[2];
	    fields[0] = id;
	    fields[1] = userId;
	    List<String>submissions = SqlService.dbRead(connection, sql, fields, null);
	    if (submissions != null && submissions.size() > 0) {
		String completed = submissions.get(0);
		if (!("1".equals(completed) || "true".equals(completed)))
		    return null;
		if (assignment.gradebookitem == null)
		    return new LessonSubmission(null);
		// following will give a security error if assignment not released. I think that's better than
		// checking myself, as that would require fetchign the assignment definition from the gradebook
		// A2 doesn't seem to save that.  Score is scaled, so need * 10
		Double score = toDouble(gradebookService.getAssignmentScore(assignment.context, assignment.gradebookitem, userId));
		if (score != null) {
		    LessonSubmission ret = new LessonSubmission(score);
		    // shouldn't actually need the string value
		    score = score * 10.0;
		    ret.setGradeString(Long.toString(score.longValue()));
		    return ret;
		}
		else
		    return null;
	    } else 
		return null;
	} catch (Exception e) {
	    return null;
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}	

    }

// we can do this for real, but the API will cause us to get all the submissions in full, not just a count.
// I think it's cheaper to get the best assessment, since we don't actually care whether it's 1 or >= 1.
    public int getSubmissionCount(String user) {
	if (getSubmission(user) == null)
	    return 0;
	else
	    return 1;
    }

    // URL to create a new item. Normally called from the generic entity, not a specific one                                                 
    // can't be null                                                                                                                         
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	ArrayList<UrlItem> list = new ArrayList<UrlItem>();
	if (haveA2) {
	    String tool = bean.getCurrentTool("sakai.assignment2");
	    if (tool != null) {
		tool = ServerConfigurationService.getToolUrl()+ "/" + tool + "/assignment";
		list.add(new UrlItem(tool, messageLocator.getMessage("simplepage.create_assignment2")));
	    }
	}
	if (nextEntity != null)
	    list.addAll(nextEntity.createNewUrls(bean));
	return list;
    }


    // URL to edit an existing entity.                                                                                                       
    // Can be null if we can't get one or it isn't needed                                                                                    
    public String editItemUrl(SimplePageBean bean) {
	String tool = bean.getCurrentTool("sakai.assignment2");
	if (tool == null)
	    return null;
    
	return ServerConfigurationService.getToolUrl()+ "/" + tool + "/assignment/" + id;
    }


    // for most entities editItem is enough, however tests allow separate editing of                                                         
    // contents and settings. This will be null except in that situation                                                                     
    public String editItemSettingsUrl(SimplePageBean bean) {
	return null;
    }

   public boolean objectExists() {
       if (assignment == null && haveA2)
	    assignment = getAssignment(id);
       return assignment != null;
   }

    public boolean notPublished(String ref) {
	return false;
    }

    public boolean notPublished() {
	if (assignment == null && haveA2)
	    assignment = getAssignment(id);
	// getAssignment tests draft and removed, so if it's been removed we'll get null
	return (assignment == null);
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public Collection<String> getGroups(boolean nocache) {
	List<String>ret = new ArrayList<String>();
	List<AssignGroup> assignGroups = getAssignGroups();

	for (AssignGroup a: assignGroups) {
	    ret.add(a.groupid);
	}

	return ret;
    }
  
    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    //
    // We can't access the A2 support code, because it's inside the component,with no
    // external API. This code was written before I figured out how to use the DAO.
    // I may be able to rewrite it that way. Note that Oracle needs slightly diffferent
    // code, so we use a loadable class for some of the SQL statements
    //
    public void setGroups(Collection<String> tgroups) {
	ArrayList<String> groups = null;
	if (tgroups != null)
	    groups = new ArrayList<String>(tgroups);
	List<AssignGroup> assignGroups = getAssignGroups();

	Iterator<AssignGroup>i = assignGroups.iterator();
	while (i.hasNext()) {
	    AssignGroup a = i.next();
	    if (groups != null && groups.contains(a.groupid)) {
		groups.remove(a.groupid);
		a.update = true;  // it's in new set, so update it
	    }
	}
	
	final Collection<String> fgroups = groups;
	final List<AssignGroup> fassignGroups = assignGroups;
	// fgroups is now stuff to add
	// fassignGroups is stuff to update or remove

	// in a transaction                                                                                        
	SqlService.transact(new Runnable()
	    {
		public void run()
		{
		    String updatesql = "update A2_ASSIGN_GROUP_T set version=? where assignment_group_id = ?";
		    //	"insert into A2_ASSIGN_GROUP_T (version, assignment_id, group_ref) values (0, ?, ?)";
		    String addsql = optSql.Assignment2InsertGroupSql();
		    String deletesql = "delete from A2_ASSIGN_GROUP_T where assignment_group_id = ?";

		    for (AssignGroup a: fassignGroups) {
			if (a.update) {
			    Object fields[] = new Object[2];
			    fields[0] = a.version + 1;
			    fields[1] = a.id;
			    SqlService.dbWrite(updatesql, fields);
			} else {
			    Object fields[] = new Object[1];
			    fields[0] = a.id;
			    SqlService.dbWrite(deletesql, fields);
			}
		    }
		    if (fgroups != null) 
			for (String g:fgroups) {
			    Object fields[] = new Object[2];
			    fields[0] = id;
			    fields[1] = g;
			    SqlService.dbWrite(addsql, fields);
			}
		}
	    }, "assignment2setgroups");

    }

    // currently assignment 2 does not participate in the fixup. However I'm going to include
    // the ID anyway, just in case it happens in the future
    public String getObjectId(){
	String title = getTitle();
	if (title == null)
	    return null;
	return "assignment2/" + id + "/" + title;
    }

    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {
	if (!objectid.startsWith("assignment2/")) {
	    if (nextEntity != null)
		return nextEntity.findObject(objectid, objectMap, siteid);
	    return null;
	}

	// isolate forum_topic/NNN from title
	int i = objectid.indexOf("/", "assignment2/".length());
	if (i <= 0)
	    return null;
	String realobjectid = objectid.substring(0, i);

	// now see if it's in the map. not currently possible, but who knows
	String newAssignment = objectMap.get(realobjectid);
	if (newAssignment != null)
	    return "/" + newAssignment;  // sakaiid is /assignment2/ID

	// Can't find the topic in the map
	// i is start of title
	String title = objectid.substring(i+1);

	String sql="select assignment_id from A2_ASSIGNMENT_T where context = ? and title = ?";
	Object fields[] = new Object[2];
	fields[0] = siteid;
	fields[1] = title;

	List<Long>assignments = SqlService.dbRead(sql, fields, new SqlReader()
	    {
		public Object readSqlResultRecord(ResultSet result)
		{
		    try {
			return (Long)result.getLong(1);
		    } catch (Exception ignore) {};
		    return null;
		}
	    });

	if (assignments == null || assignments.size() < 1)
	    return null;
	return "/assignment2/" + assignments.get(0);

    }

    public String importObject(String title, String href, String mime, boolean hide){
	String contextId = ToolManager.getCurrentPlacement().getContext();
        Assignment2 assignment = new Assignment2();
        assignment.setContextId(contextId);
        assignment.setCreateDate(new Date());
        assignment.setCreator("ADMIN");
        assignment.setDraft(hide);
        assignment.setInstructions(messageLocator.getMessage("simplepage.assign_seeattach"));
        assignment.setSendSubmissionNotifications(false);
        assignment.setOpenDate(new Date());
        assignment.setRemoved(false);
        assignment.setSubmissionType(AssignmentConstants.SUBMIT_INLINE_AND_ATTACH);
        assignment.setGraded(false);
        assignment.setHonorPledge(false);
        assignment.setHasAnnouncement(false);
        assignment.setAddedToSchedule(false);
	//        assignment.setSortIndex(sortIndex);
        assignment.setTitle(title);
        assignment.setRequiresSubmission(true);
        assignment.setNumSubmissionsAllowed(1);
	//	Set <Assignment2> assignSet = new HashSet<Assignment2>();
	//	assignSet.add(assignment);

	String newAttRef = null;
	// in theory we should probably copy this into the attachment area. But
	// if there are any relative URLs in it, they're fail in the copy. So
	// unless there are problems, we leave it where it is. This code to make
	// the copy has been tested and should work.
	if (false) {
	try {
	    ContentResource oldAttachment = ContentHostingService.getResource(href);
	    String toolTitle = "Assignments 2";
	    String name = Validator.escapeResourceName(oldAttachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
	    String type = oldAttachment.getContentType();
	    byte[] content = oldAttachment.getContent();
	    ResourceProperties properties = oldAttachment.getProperties();
	    ContentResource newResource = ContentHostingService.addAttachmentResource(name,
	       contextId, toolTitle, type, content, properties);
	    newAttRef = newResource.getId();
	} catch (Exception e) {
	    log.info("unable to make attachment resource " + e);
	}
	}
	newAttRef = href;

	AssignmentAttachment attachment = new AssignmentAttachment(assignment, newAttRef);

	//	Set<AssignmentAttachment> attachments = new HashSet<AssignmentAttachment>();
	//	attachments.add(new AssignmentAttachment(assignment, "/content/" + href));
	try {
	    saveMethod.invoke(dao, assignment);
	    saveMethod.invoke(dao, attachment);
	    return "/assignment2/" + assignment.getId();

	} catch (Exception e) {
	    log.info("invoke failed " + e);
	}

	return null;
    }


    public String importObject(Element resource, Namespace ns, String base, String baseDir, List<String>attachments, boolean hide) {
	String contextId = ToolManager.getCurrentPlacement().getContext();
        Assignment2 assignment = new Assignment2();
        assignment.setContextId(contextId);
        assignment.setCreateDate(new Date());
        assignment.setCreator("ADMIN");
        assignment.setDraft(hide);

	// instructions
	String instructions = resource.getChildText("text", ns);
	if (instructions == null)
	    assignment.setInstructions("");
	else {
	    Element instructionsElement = resource.getChild("text", ns);
	    String type = instructionsElement.getAttributeValue("texttype");
	    if ("text/plain".equals(type))
		instructions = FormattedText.convertPlaintextToFormattedText(instructions);
	    else
		instructions = instructions.replaceAll("\\$IMS-CC-FILEBASE\\$", base);

	    assignment.setInstructions(instructions);
	}

        assignment.setSendSubmissionNotifications(false);
        assignment.setOpenDate(new Date());
        assignment.setRemoved(false);

	// submission type
	Element submissiontypes = resource.getChild("submission_formats", ns);
	int submittype;
	if (submissiontypes == null)
	    submittype = 2;
	else {
	    boolean inline = false;
	    boolean attach = false;
	    List<Element> submissionTypeList = submissiontypes.getChildren();
	    if (submissionTypeList != null)
		for (Element submissionType: submissionTypeList) {
		    String type = submissionType.getAttributeValue("type");
		    if ("html".equals(type) || "text".equals(type))
			inline = true;
		    if ("url".equals(type) || "file".equals(type))
			attach = true;
		}
	    if (attach) {
		if (inline)
		    submittype = 2;
		else
		    submittype = 1;
	    } else
		submittype = 0;
	}
	assignment.setSubmissionType(submittype);

	assignment.setGraded(false);
	assignment.setHonorPledge(false);
	assignment.setHasAnnouncement(false);
	assignment.setAddedToSchedule(false);
	//        assignment.setSortIndex(sortIndex);
	String title = resource.getChildText("title", ns);
        assignment.setTitle(title);
        assignment.setRequiresSubmission(true);
        assignment.setNumSubmissionsAllowed(1);

	try {
	    saveMethod.invoke(dao, assignment);
	} catch (Exception e) {
	    log.info("invoke failed " + e);
	    return null;
	}

	if (attachments != null) {
	    for (String attach: attachments) {
		AssignmentAttachment attachment = new AssignmentAttachment(assignment, removeDotDot(baseDir + attach));
		try {
		    saveMethod.invoke(dao, attachment);
		} catch (Exception e) {
		    log.info("invoke failed " + e);
		}
	    }
	}

	return "/assignment2/" + assignment.getId();

    }

    public String removeDotDot(String s) {
	while (true) {
	    int i = s.indexOf("/../");
	    if (i < 1)
		return s;
	    int j = s.lastIndexOf("/", i-1);
	    if (j < 0)
		j = 0;
	    else
		j = j + 1;
	    s = s.substring(0, j) + s.substring(i+4);
	}
    }

    public String getSiteId() {
	// can't use getassignment because it assumes we are working
	// with current site
	String sql="select context from A2_ASSIGNMENT_T where assignment_id = ?";
	Object fields[] = new Object[1];
	fields[0] = id;
	
	List<String> contexts = SqlService.dbRead(sql, fields, null);
	if (contexts != null && contexts.size() > 0)
	    return contexts.get(0);
	return null;
    }

}
