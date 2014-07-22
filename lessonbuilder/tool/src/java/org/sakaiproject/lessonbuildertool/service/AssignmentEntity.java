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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Element;
import org.jdom.Namespace;

import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.util.FormattedText;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentContentEdit;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;

import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.component.cover.ServerConfigurationService;             
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;

import uk.org.ponder.messageutil.MessageLocator;

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

public class AssignmentEntity implements LessonEntity, AssignmentInterface {

    private static Log log = LogFactory.getLog(AssignmentEntity.class);

    private static Cache assignmentCache = null;
    protected static final int DEFAULT_EXPIRATION = 10 * 60;

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
    
    static MemoryService memoryService = null;
    public void setMemoryService(MemoryService m) {
	memoryService = m;
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    static AssignmentSupplementItemService assignmentSupplementItemService = null;
    public void setAssignmentSupplementItemService(AssignmentSupplementItemService a) {
	assignmentSupplementItemService = a;
    }

    public void init () {
	assignmentCache = memoryService
	    .newCache("org.sakaiproject.lessonbuildertool.service.AssignmentEntity.cache");

	log.info("init()");

    }

    public void destroy()
    {
	assignmentCache.destroy();
	assignmentCache = null;

	log.info("destroy()");
    }


    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

    protected AssignmentEntity() {
    }

    protected AssignmentEntity(int type, String id, int level) {
	this.type = type;
	this.id = id;
	this.level = level;
    }

    public String getToolId() {
	return "sakai.assignment.grades";
    }

    // the underlying object, something Sakaiish
    protected String id;
    protected int type;
    protected int level;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them
    protected Assignment assignment;

    public Assignment getAssignment(String ref) {
	return getAssignment(ref, false);
    }

    public Assignment getAssignment(String ref, boolean nocache) {
	Assignment ret = (Assignment)assignmentCache.get(ref);
	if (!nocache && ret != null)
	    return ret;

	try {
	    ret = AssignmentService.getAssignment(ref);
	} catch (Exception e) {
	    ret = null;
	}
	
	if (ret != null)
	    assignmentCache.put(ref, ret, DEFAULT_EXPIRATION);
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

	AssignmentContent content = assignment.getContent();
	if (content == null) {
	    return 1;
	} else 
	    return assignment.getContent().getTypeOfGrade();
    }

  // hack for forums. not used for assessments, so always ok
    public boolean isUsable() {
	return true;
    }

    public String getReference() {
	return "/" + ASSIGNMENT + "/" + id;
    }

    public List<LessonEntity> getEntitiesInSite() {
	return getEntitiesInSite(null);
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean) {

	Iterator i = AssignmentService.getAssignmentsForContext(ToolManager.getCurrentPlacement().getContext());

	List<LessonEntity> ret = new ArrayList<LessonEntity>();
	// security. assume this is only used in places where it's OK, so skip security checks
	while (i.hasNext()) {
	    Assignment a = (Assignment) i.next();
	    String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
	    // this somewhat odd test for deleted is the one used in the Assignment code
	    if ((deleted == null || "".equals(deleted)) && !a.getDraft()) {
		AssignmentEntity entity = new AssignmentEntity(TYPE_ASSIGNMENT, a.getId(), 1);
		entity.assignment = a;
		entity.simplePageBean = bean;
		ret.add(entity);
	    }
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
	if (i < 0) {
	    // old format, just the number
	    AssignmentEntity entity = new AssignmentEntity(TYPE_ASSIGNMENT, ref, 1);
	    entity.simplePageBean = bean;
	    return entity;
	}
	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);
	String id = "";
	try {
	    id = idString;
	} catch (Exception ignore) {
	    return null;
	}

	if (typeString.equals(ASSIGNMENT)) {
	    AssignmentEntity entity = new AssignmentEntity(TYPE_ASSIGNMENT, id, 1);
	    entity.simplePageBean = bean;
	    return entity;
	} else if (nextEntity != null) {
	    return nextEntity.getEntity(ref, simplePageBean);
	} else
	    return null;
    }
	

    // properties of entities
    public String getTitle() {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	return assignment.getTitle();
    }

    public String getUrl() {
	
	if (simplePageBean != null) {
	    return ServerConfigurationService.getToolUrl() + "/" + simplePageBean.getCurrentTool("sakai.assignment.grades") + 
		"?assignmentReference=/assignment/a/" + simplePageBean.getCurrentSiteId() + "/" + id + "&panel=Main&sakai_action=doView_submission";
	}

	Site site = null;
	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	} catch (Exception impossible) {
	    return null;
	}

	ToolConfiguration tool = site.getToolForCommonId("sakai.assignment.grades");
	
	if(tool == null) {
	    return null;
	}
	
	String placement = tool.getId();

	// https://sakai-test2.oirt.rutgers.edu/portal/tool/6b328952-cbcb-494b-0035-3c07120e4499?assignmentReference=/assignment/a/0aaae6ef-cb01-4578-0099-888d344b524b/0e52c5f6-ba73-40d2-961c-286533d59148&panel=Main&sakai_action=doView_submission
	return ServerConfigurationService.getToolUrl() + "/" +  placement + "?assignmentReference=/assignment/a/" + site.getId() + "/" + id + "&panel=Main&sakai_action=doView_submission";
	// following was broken in 2.8.1
        // return "/direct/assignment/" + id;
    }

    public Date getDueDate() {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	return new Date(assignment.getDueTime().getTime());
    }

    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException {
	Site site = null;
	String ref = "/assignment/a/" + siteId + "/" + id;

	try {
	    site = SiteService.getSite(siteId);
	} catch (Exception e) {
	    log.warn("Unable to find site " + siteId, e);
	    return false;
	}

	AssignmentEdit edit = null;
	
	try {
	    edit = AssignmentService.editAssignment(ref);
	} catch (IdUnusedException e) {
	    log.warn("ID unused ", e);
	    return false;
	} catch (PermissionException e) {
	    log.warn(e);
	    return false;
	} catch (InUseException e) {
	    log.warn(e);
	    return false;
	}

	boolean doCancel = true;

	try {
	    // need this to make sure we always unlock
	    
	    if (edit.getAccess() == Assignment.AssignmentAccess.GROUPED) {
		Collection<String> groups = edit.getGroups();
		groupId = "/site/" + siteId + "/group/" + groupId;

		if (groups.contains(groupId)) {
		    return true;
		}
		
		Group group = site.getGroup(groupId);
		if (group == null) {
		    return false;
		}
		
		// odd; getgruops returns a list of string
		// but setgroupacces wants a collection of actual groups
		// so we have to copy the list
		Collection<Group> newGroups = new ArrayList<Group>();
		for (String gid : groups) {
		    newGroups.add(site.getGroup(gid));
		}
		
		// now add in this one
		newGroups.add(group);

		try {
		    edit.setGroupAccess(newGroups);
		} catch (PermissionException e) {
		    log.warn(e);
		    return false;
		}

		AssignmentService.commitEdit(edit);
		doCancel = false;
		return true;

	    } else {
		// currently not grouped
		Collection groups = new ArrayList<String>();
		Group group = site.getGroup(groupId);
		
		if (group == null) {
		    log.warn("Could not find Group");
		    return false;
		}
		
		groups.add(group);
		
		try {
		    // this change mode to grouped
		    edit.setGroupAccess(groups);
		} catch (PermissionException e) {
		    log.warn(e);
		    return false;
		}
		
		AssignmentService.commitEdit(edit);
		doCancel = false;
		return true;
	    }
	} catch (Exception e) {
	    log.warn(e);
	    return false;
	} finally {
	    if (doCancel) {
		AssignmentService.commitEdit(edit);
	    }
	}
    }

    public boolean removeEntityControl(String siteId, String groupId) throws IOException {
	Site site = null;
	String ref = "/assignment/a/" + siteId + "/" + id;
	try {
	    site = SiteService.getSite(siteId);
	} catch (Exception e) {
	    log.warn("Unable to find site " + siteId, e);
	    return false;
	}
	
	AssignmentEdit edit = null;
	
	try {
	    edit = AssignmentService.editAssignment(ref);
	} catch (IdUnusedException e) {
	    log.warn(e);
	    return false;
	} catch (PermissionException e) {
	    log.warn(e);
	    return false;
	} catch (InUseException e) {
	    log.warn(e);
	    return false;
	}
	
	boolean doCancel = true;
	
	try {
	    // need this to make sure we always unlock
	    
	    if (edit.getAccess() == Assignment.AssignmentAccess.GROUPED) {
		Collection<String> groups = edit.getGroups();
		groupId = "/site/" + siteId + "/group/" + groupId;
		
		if (!groups.contains(groupId)) {
		    // nothing to do
		    return true;
		}
		
		// odd; getgruops returns a list of string
		// but setgroupacces wants a collection of actual groups
		// so we have to copy the list
		Collection<Group> newGroups = new ArrayList<Group>();
		for (String gid : groups) {
		    // remove our group
		    if (!gid.equals(groupId)) {
			newGroups.add(site.getGroup(gid));
		    }
		}
		
		if (newGroups.size() > 0) {
		    // there's groups left, just remove ours
		    try {
			edit.setGroupAccess(newGroups);
		    } catch (PermissionException e) {
			log.warn(e);
			return false;
		    }
		} else {
		    // no groups left, put site access back
		    edit.setAccess(Assignment.AssignmentAccess.SITE);
		    edit.clearGroupAccess();
		}
		
		AssignmentService.commitEdit(edit);
		doCancel = false;
		return true;
		
	    } else {
		// currently not grouped
		// nothing to do
		
		return true;
	    }
	    
	} catch (Exception e) {
	    log.warn(e);
	    return false;
	} finally {
	    if (doCancel) {
		AssignmentService.commitEdit(edit);
	    }
	}
	
    }

    // submission
    // do we need the data from submission?
    public boolean needSubmission(){
	return true;
    }

    public LessonSubmission getSubmission(String userId) {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null) {
	    log.warn("can't find assignment " + id);
	    return null;
	}

	User user = null;
	AssignmentSubmission submission = null;
	try {
	    user = UserDirectoryService.getUser(userId);
	    submission = AssignmentService.getSubmission(assignment.getReference(), user);
	} catch (Exception e) {
	    return null;
	}

	if (submission == null || !submission.getSubmitted())
	    return null;

	LessonSubmission ret= new LessonSubmission(null);

	if (submission.getGradeReleased())  {
	    String grade = submission.getGrade();
	    ret.setGradeString(grade);
	}

	return ret;

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
	String tool = bean.getCurrentTool("sakai.assignment.grades");
	if (tool != null) {
	    tool = ServerConfigurationService.getToolUrl() + "/" + tool + "?view=lisofass1&panel=Main&sakai_action=doView";
	    list.add(new UrlItem(tool, messageLocator.getMessage("simplepage.create_assignment")));
	}
	if (nextEntity != null)
	    list.addAll(nextEntity.createNewUrls(bean));
	return list;
    }


    // URL to edit an existing entity.                                                                                                       
    // Can be null if we can't get one or it isn't needed                                                                                    
    public String editItemUrl(SimplePageBean bean) {
	String tool = bean.getCurrentTool("sakai.assignment.grades");
	if (tool == null)
	    return null;
    
	return ServerConfigurationService.getToolUrl() + "/" + tool + "?assignmentId=/assignment/a/" + bean.getCurrentSiteId() +
	    "/" + id + "&panel=Main&sakai_action=doEdit_assignment";
    }


    // for most entities editItem is enough, however tests allow separate editing of                                                         
    // contents and settings. This will be null except in that situation                                                                     
    public String editItemSettingsUrl(SimplePageBean bean) {
	return null;
    }

    public boolean objectExists() {
	if (assignment == null)
	    assignment = getAssignment(id, true);
	return assignment != null;
    }
	
    public boolean notPublished(String ref) {
	return false;
    }

    public boolean notPublished() {
	if (!objectExists())
	    return true;
	String deleted = assignment.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
	// this somewhat odd test for deleted is the one used in the Assignment code
	if ((deleted == null || "".equals(deleted)) && !assignment.getDraft())
	    return false;
	else
	    return true;
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public Collection<String> getGroups(boolean nocache) {
	if (nocache)
	    assignment = getAssignment(id, true);
	else if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	
	if (assignment.getAccess() != Assignment.AssignmentAccess.GROUPED)
	    return null;
	    
	Collection<String> groupRefs = assignment.getGroups();

	List<String> groupIds = new ArrayList<String>();

	for (String ref: groupRefs) {
	    int i = ref.lastIndexOf("/");
	    if (i >= 0)
		ref = ref.substring(i+1);
	    groupIds.add(ref);
	}

	return groupIds;
    }
  
    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    public void setGroups(Collection<String> groups) {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return;

	String siteId = ToolManager.getCurrentPlacement().getContext();
	Site site = null;
	String ref = "/assignment/a/" + siteId + "/" + id;

	try {
	    site = SiteService.getSite(siteId);
	} catch (Exception e) {
	    log.warn("Unable to find site " + siteId, e);
	    return;
	}

	AssignmentEdit edit = null;
	
	try {
	    edit = AssignmentService.editAssignment(ref);
	} catch (IdUnusedException e) {
	    log.warn("ID unused ", e);
	    return;
	} catch (PermissionException e) {
	    log.warn(e);
	    return;
	} catch (InUseException e) {
	    log.warn(e);
	    return;
	}

	boolean doCancel = true;

	try {
	    // need this to make sure we always unlock
	    
	    if (groups != null && groups.size() > 0) {
		List<Group> groupObjs = new ArrayList<Group>();
		
		for (String groupId : groups) {
		    Group group = site.getGroup(groupId);
		    if (group != null)
			groupObjs.add(group);
		}

		edit.setGroupAccess(groupObjs);
	    } else {
		edit.setAccess(Assignment.AssignmentAccess.SITE);
		edit.clearGroupAccess();
	    }

	    AssignmentService.commitEdit(edit);
	    doCancel = false;
	    return;

	} catch (Exception e) {
	    log.warn(e);
	    return;
	} finally {
	    if (doCancel) {
		AssignmentService.commitEdit(edit);
	    }
	}

    }

    public String getObjectId() {
	String title = getTitle();
	if (title == null)
	    return null;
	return "assignment/" + id + "/" + title;
    }

    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {
	if (!objectid.startsWith("assignment/")) {
	    if (nextEntity != null) {
		return nextEntity.findObject(objectid, objectMap, siteid);
	    }
	    return null;
	}

	String realobjectid = objectid;
	// isolate forum_topic/NNN from title
	int i = objectid.indexOf("/", "assignment/".length());
	if (i > 0)
	    realobjectid = objectid.substring(0, i);

	String newassignment = objectMap.get(realobjectid);
	if (newassignment != null)
	    return "/" + newassignment;  // sakaiid is /assignment/ID

	// not in map. try title, but only if title given
	if (i <= 0)
	    return null; // no title

	// i is start of title
	String title = objectid.substring(i+1);

	Iterator aIter = AssignmentService.getAssignmentsForContext(ToolManager.getCurrentPlacement().getContext());

	// security. assume this is only used in places where it's OK, so skip security checks
	while (aIter.hasNext()) {
	    Assignment a = (Assignment) aIter.next();
	    if (title.equals(a.getTitle()))
		return "/assignment/" + a.getId();
	}

	return null;

    }

    public String importObject(String title, String href, String mime, boolean hide){
	String context = ToolManager.getCurrentPlacement().getContext();
	try {
	    AssignmentContentEdit c = AssignmentService.addAssignmentContent(context);
	    c.setTitle(title);
	    // no instructions. It causes problems on export, because we can' recognize it as the special case.
	    c.setInstructions("");
	    // c.setInstructions(messageLocator.getMessage("simplepage.assign_seeattach"));
	    c.setHonorPledge(1);  // no 
	    c.setTypeOfSubmission(3);  // inline and attachment
	    c.setAllowReviewService(false);
	    c.setTypeOfGrade(1);   // ungraded
	    c.setAllowAttachments(true);
	    c.clearAttachments();
	    c.addAttachment(EntityManager.newReference("/content" + href));
	    c.setContext(context);
	    AssignmentService.commitEdit(c);

	    AssignmentEdit a = AssignmentService.addAssignment(context);
	    Time now = TimeService.newTime();
	    a.setOpenTime(now);

	    TimeBreakdown year = now.breakdownLocal();
	    year.setYear(year.getYear() + 1);
	    now = TimeService.newTimeLocal(year);
	    a.setDueTime(now);

	    a.setDraft(hide);
	    a.setAccess(Assignment.AssignmentAccess.SITE);
	    a.clearGroupAccess();
	    a.setSection("");
	    a.setTitle(title);
	    a.setContent(c);
	    
	    AssignmentService.commitEdit(a);
	    return "/assignment/" + a.getId();
	} catch (Exception e) {
	    System.out.println("can't create assignment " + e);
	};
	return null;
    }

    /*
    <assignment xmlns="http://www.imsglobal.org/xsd/imscc_extensions/assignment"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.imsglobal.org/xsd/imscc_extensions/assignment http://www.imsglobal.org/profile/cc/cc_extensions/cc_extresource_assignmentv1p0_v1p0.xsd"
  identifier="AssigmentId">
  <title>Title of the Assignment</title>
  <!-- text/plain or text/html -->
  <text texttype="text/plain">Example of text for the learner  i.e. the assignment being set.</text>
  <instructor_text texttype="text/plain">Example of test for the instructor.</instructor_text>
  <gradable points_possible="10.0">true</gradable>
  <attachments>
	<!-- All, Learner, Manager, Instructor -->
    <attachment href="Variant.html" role="All" />
  </attachments>
  <submission_formats>
	<!-- text, html, uri or file -->
    <format  type="html" />
  </submission_formats>
</assignment>

    */

    public String importObject(Element resource, Namespace ns, String base, String baseDir, List<String>attachments, boolean hide) {
	String context = ToolManager.getCurrentPlacement().getContext();
	try {
	    AssignmentContentEdit c = AssignmentService.addAssignmentContent(context);
	    // title
	    String title = resource.getChildText("title", ns);
	    c.setTitle(title);

	    // instructions
	    String instructions = resource.getChildText("text", ns);
	    if (instructions == null)
		c.setInstructions("");
	    else {
		Element instructionsElement = resource.getChild("text", ns);
		String type = instructionsElement.getAttributeValue("texttype");
		if ("text/plain".equals(type))
		    instructions = FormattedText.convertPlaintextToFormattedText(instructions);
		else
		    instructions = instructions.replaceAll("\\$IMS-CC-FILEBASE\\$", base);
		c.setInstructions(instructions);
	    }

	    // c.setInstructions(messageLocator.getMessage("simplepage.assign_seeattach"));
	    c.setHonorPledge(1);  // no 

	    // submission type
	    Element submissiontypes = resource.getChild("submission_formats", ns);
	    if (submissiontypes == null)
		c.setTypeOfSubmission(3);  // inline and attachment
	    else {
		int submittype = 0;
		List<Element> submissionTypeList = submissiontypes.getChildren();
		if (submissionTypeList != null)
		    for (Element submissionType: submissionTypeList) {
			String type = submissionType.getAttributeValue("type");
			if ("html".equals(type) || "text".equals(type))
			    submittype |= 1; // inline
			if ("url".equals(type) || "file".equals(type))
			    submittype |= 2; // attach
		    }
		c.setTypeOfSubmission(submittype);
	    }
	    
	    c.setAllowReviewService(false);

	    String gradable = resource.getChildText("gradable", ns);
	    if (gradable == null || "false".equals(gradable))
		c.setTypeOfGrade(1);   // ungraded
	    else {
		Element gradeElement = resource.getChild("gradable", ns);
		String pointString = gradeElement.getAttributeValue("points_possible");
		Double pointF = 100.0;
		int points = 1000;
		if (pointString != null) {
		    try {
			pointF = Double.parseDouble(pointString);
		    } catch (Exception ignore) {
		    }
		    // points is scaled by 10
		    points = (int)Math.round(pointF * 10);
		    if (points < 1)
			points = 1000;
		}
		c.setTypeOfGrade(3);   // points
		c.setMaxGradePoint(points);
	    }

	    c.setAllowAttachments(true);
	    c.clearAttachments();

	    if (attachments != null) {
		for (String attach: attachments) {
		    c.addAttachment(EntityManager.newReference("/content" + removeDotDot(baseDir + attach)));
		}
	    }

	    c.setContext(context);
	    AssignmentService.commitEdit(c);

	    AssignmentEdit a = AssignmentService.addAssignment(context);
	    Time now = TimeService.newTime();
	    a.setOpenTime(now);

	    TimeBreakdown year = now.breakdownLocal();
	    year.setYear(year.getYear() + 1);
	    now = TimeService.newTimeLocal(year);
	    a.setDueTime(now);

	    a.setDraft(hide);
	    a.setAccess(Assignment.AssignmentAccess.SITE);
	    a.clearGroupAccess();
	    a.setSection("");
	    a.setTitle(title);
	    a.setContent(c);
	    
	    AssignmentService.commitEdit(a);

	    // instructor text -- map to note
	    String note = resource.getChildText("instructor_text", ns);
	    if (note != null) {
		Element instructionsElement = resource.getChild("instructor_text", ns);
		String type = instructionsElement.getAttributeValue("texttype");
		// unfortunately we can only store plain text. Replacing IMS-CC-FILEBASE may be futile in this case,
		// but it seems better to do it than not.
		if ("text/html".equals(type))
		    note = FormattedText.convertFormattedTextToPlaintext(note.replaceAll("\\$IMS-CC-FILEBASE\\$", base));
		
		AssignmentNoteItem nNote = assignmentSupplementItemService.newNoteItem();
		nNote.setAssignmentId(a.getId());
		nNote.setNote(note);
		nNote.setShareWith(0);
		nNote.setCreatorId(UserDirectoryService.getCurrentUser().getId());
		assignmentSupplementItemService.saveNoteItem(nNote);
	    }

	    return "/assignment/" + a.getId();
	} catch (Exception e) {
	    System.out.println("can't create assignment " + e);
	};
	return null;
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
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	return assignment.getContext();
    }

}
