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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@NoArgsConstructor
public class AssignmentEntity implements LessonEntity, AssignmentInterface {

    public static final int CACHE_MAX_ENTRIES = 5000;
    public static final int CACHE_TIME_TO_LIVE_SECONDS = 600;
    public static final int CACHE_TIME_TO_IDLE_SECONDS = 360;

    @Setter private static AssignmentService assignmentService;
    @Setter private static MessageLocator messageLocator;
    @Setter private static AssignmentSupplementItemService assignmentSupplementItemService;

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

    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

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
		try {
	     	return assignmentService.getAssignment(ref);
		} catch (IdUnusedException | PermissionException e) {
			log.warn("Attempted to retrieve assignment: {}, {}", ref, e.toString());
			return null;
		}
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
    return assignment.getTypeOfGrade().ordinal();
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


	List<LessonEntity> ret = new ArrayList<LessonEntity>();
	// security. assume this is only used in places where it's OK, so skip security checks
	for (Assignment a : assignmentService.getAssignmentsForContext(ToolManager.getCurrentPlacement().getContext())) {
	    // this somewhat odd test for deleted is the one used in the Assignment code
	    if (!a.getDraft()) {
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
	return assignment.getTitle();
    }

    public String getDescription(){
        return "";
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

	public Boolean isHiddenDueDate() {
		if (assignment == null) {
			assignment = getAssignment(id);
		}

		if (assignment == null) {
			return false;
		}

		return assignment.getHideDueDate();
	}

    public Date getDueDate() {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	return Date.from(assignment.getDueDate());
    }

	public Date getOpenDate() {
		if (assignment == null) {
			assignment = getAssignment(id);
		}
		if (assignment == null) {
			return null;
		}
		return Date.from(assignment.getOpenDate());
	}

    public LessonSubmission getSubmission(String userId) {

		if (assignment == null) {
			assignment = getAssignment(id);
		}

		if (assignment == null) {
			log.warn("can't find assignment {}", id);
			return null;
		}

		AssignmentSubmission submission = null;
		try {
			submission = assignmentService.getSubmission(assignment.getId(), UserDirectoryService.getUser(userId));
		} catch (Exception e) {
			log.warn(e.getMessage());
			return null;
		}

		if (submission == null || !submission.getSubmitted()) {
			return null;
		}

		LessonSubmission ret = new LessonSubmission(null);

		if (submission.getGradeReleased())	{
			String grade = submission.getGrade();
			ret.setGradeString(grade);
		}

		ret.setUserSubmission(submission.getUserSubmission());

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
	    assignment = getAssignment(id);
	return assignment != null;
    }
	
    public boolean notPublished(String ref) {
	return false;
    }

    public boolean notPublished() {
	if (!objectExists())
	    return true;

	// this somewhat odd test for deleted is the one used in the Assignment code
	if (!assignment.getDeleted() && !assignment.getDraft())
	    return false;
	else
	    return true;
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public Collection<String> getGroups(boolean nocache) {
	if (nocache)
	    assignment = getAssignment(id);
	else if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	
	if (assignment.getTypeOfAccess() != Assignment.Access.GROUP)
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

	try {
	    site = SiteService.getSite(siteId);
	} catch (Exception e) {
	    log.warn("Unable to find site " + siteId, e);
	    return;
	}

	Assignment edit = assignment;
	boolean doCancel = true;

	try {
	    // need this to make sure we always unlock
	    if (groups != null && groups.size() > 0) {
			Set<String> groupRefs = new HashSet<>();
			for (String groupId : groups) {
				Group group = site.getGroup(groupId);
				if (group != null) groupRefs.add(group.getReference());
			}
			edit.setGroups(groupRefs);
			edit.setTypeOfAccess(Assignment.Access.GROUP);
	    } else {
			edit.setTypeOfAccess(Assignment.Access.SITE);
			edit.setGroups(new HashSet<>());
	    }

	    assignmentService.updateAssignment(edit);
	    doCancel = false;
	    return;

	} catch (Exception e) {
	    log.warn(e.getMessage());
	    return;
	} finally {
	    if (doCancel) {
			assignmentService.resetAssignment(edit);
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

	// security. assume this is only used in places where it's OK, so skip security checks
	for (Assignment a : assignmentService.getAssignmentsForContext(siteid)) {
	    if (title.equals(a.getTitle()))
		return "/assignment/" + a.getId();
	}

	return null;

    }

    public String importObject(String title, String href, String mime, boolean hide){
	String context = ToolManager.getCurrentPlacement().getContext();
	try {
	    Assignment a = assignmentService.addAssignment(context);
	    a.setTitle(title);
	    // no instructions. It causes problems on export, because we can' recognize it as the special case.
	    a.setInstructions("");
	    // c.setInstructions(messageLocator.getMessage("simplepage.assign_seeattach"));
	    a.setHonorPledge(false);  // no
	    a.setTypeOfSubmission(Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION);  // inline and attachment
	    a.setContentReview(false);
	    a.setTypeOfGrade(Assignment.GradeType.UNGRADED_GRADE_TYPE);   // ungraded
	    a.setAllowAttachments(true);
	    Set<String> attachments = new HashSet<>();
	    attachments.add(EntityManager.newReference("/content" + href).getReference());
	    a.setAttachments(attachments);
	    a.setContext(context);
	    a.setOpenDate(Instant.now());
	    Instant dueDate = ZonedDateTime.now().plusYears(1).toInstant();
	    a.setDueDate(dueDate);
	    a.setCloseDate(dueDate);
	    a.setDraft(hide);
	    a.setTypeOfAccess(Assignment.Access.SITE);
	    a.setGroups(new HashSet<>());
	    a.setSection("");
	    a.setTitle(title);

	    assignmentService.updateAssignment(a);
	    return "/assignment/" + a.getId();
	} catch (Exception e) {
	    log.info("can't create assignment " + e);
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
	    Assignment a = assignmentService.addAssignment(context);
	    // title
	    String title = resource.getChildText("title", ns);
	    a.setTitle(title);

	    // instructions
	    String instructions = resource.getChildText("text", ns);
	    if (instructions == null)
		a.setInstructions("");
	    else {
		Element instructionsElement = resource.getChild("text", ns);
		String type = instructionsElement.getAttributeValue("texttype");
		if ("text/plain".equals(type))
		    instructions = ComponentManager.get(FormattedText.class).convertPlaintextToFormattedText(instructions);
		else
		    instructions = instructions.replaceAll("\\$IMS-CC-FILEBASE\\$", base);
		a.setInstructions(instructions);
	    }

	    // c.setInstructions(messageLocator.getMessage("simplepage.assign_seeattach"));
	    a.setHonorPledge(false);  // no

	    // submission type
	    Element submissiontypes = resource.getChild("submission_formats", ns);
	    if (submissiontypes == null)
		a.setTypeOfSubmission(Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION);  // inline and attachment
	    else {
		Assignment.SubmissionType submittype = Assignment.SubmissionType.ASSIGNMENT_SUBMISSION_TYPE_NONE;
		List<Element> submissionTypeList = submissiontypes.getChildren();
		if (submissionTypeList != null)
		    for (Element submissionType: submissionTypeList) {
			String type = submissionType.getAttributeValue("type");
			if ("html".equals(type) || "text".equals(type))
			    submittype = Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION; // inline
			if ("url".equals(type) || "file".equals(type))
			    submittype = Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION; // attach
		    }
		a.setTypeOfSubmission(submittype);
	    }
	    
	    a.setContentReview(false);

	    String gradable = resource.getChildText("gradable", ns);
	    if (gradable == null || "false".equals(gradable))
		a.setTypeOfGrade(Assignment.GradeType.UNGRADED_GRADE_TYPE);   // ungraded
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
		a.setTypeOfGrade(Assignment.GradeType.SCORE_GRADE_TYPE);   // points
		a.setMaxGradePoint(points);
	    }

	    a.setAllowAttachments(true);

	    Set<String> attachs = new HashSet<>();
	    if (attachments != null) {
			for (String attach: attachments) {
				attachs.add(EntityManager.newReference("/content" + removeDotDot(baseDir + attach)).getReference());
			}
	    }
		a.setAttachments(attachs);

		a.setContext(context);
	    a.setOpenDate(Instant.now());
	    Instant dueDate = ZonedDateTime.now().plusYears(1).toInstant();
	    a.setDueDate(dueDate);
	    a.setCloseDate(dueDate);

	    a.setDraft(hide);
	    a.setTypeOfAccess(Assignment.Access.SITE);
	    a.setGroups(new HashSet<>());
	    a.setSection("");
	    a.setTitle(title);

	    assignmentService.updateAssignment(a);

	    // instructor text -- map to note
	    String note = resource.getChildText("instructor_text", ns);
	    if (note != null) {
		Element instructionsElement = resource.getChild("instructor_text", ns);
		String type = instructionsElement.getAttributeValue("texttype");
		// unfortunately we can only store plain text. Replacing IMS-CC-FILEBASE may be futile in this case,
		// but it seems better to do it than not.
		if ("text/html".equals(type))
		    note = ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(note.replaceAll("\\$IMS-CC-FILEBASE\\$", base));
		
		AssignmentNoteItem nNote = assignmentSupplementItemService.newNoteItem();
		nNote.setAssignmentId(a.getId());
		nNote.setNote(note);
		nNote.setShareWith(0);
		nNote.setCreatorId(UserDirectoryService.getCurrentUser().getId());
		assignmentSupplementItemService.saveNoteItem(nNote);
	    }

	    return "/assignment/" + a.getId();
	} catch (Exception e) {
	    log.info("can't create assignment " + e);
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

	public Integer getScaleFactor() {
		return assignment.getScaleFactor();
	}

	@Override
    public void preShowItem(SimplePageItem simplePageItem)
    {
		// Not currently used
    }
}
