/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.assignment.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.*;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItemAccess;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemAttachment;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer;
import org.sakaiproject.authz.api.*;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.model.ContentReviewItem;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.email.cover.DigestService;
import org.sakaiproject.email.cover.EmailService;
import org.sakaiproject.entity.api.*;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.*;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.*;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.*;
import org.sakaiproject.util.cover.LinkMigrationHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//Export to excel
import java.text.DecimalFormat;

import org.sakaiproject.entitybroker.DeveloperHelperService;

/**
 * <p>
 * BaseAssignmentService is the abstract service class for Assignments.
 * </p>
 * <p>
 * The Concrete Service classes extending this are the XmlFile and DbCached storage classes.
 * </p>
 */
public abstract class BaseAssignmentService implements AssignmentService, EntityTransferrer, EntityTransferrerRefMigrator
{
	/** Our logger. */
	private static Logger M_log = LoggerFactory.getLogger(BaseAssignmentService.class);

	/** the resource bundle */
	private static ResourceLoader rb = new ResourceLoader("assignment");

	/** A Storage object for persistent storage of Assignments. */
	protected AssignmentStorage m_assignmentStorage = null;

	/** A Storage object for persistent storage of Assignments. */
	protected AssignmentContentStorage m_contentStorage = null;

	/** A Storage object for persistent storage of Assignments. */
	protected AssignmentSubmissionStorage m_submissionStorage = null;

	/** The access point URL. */
	protected static String m_relativeAccessPoint = null;
	
	private static final String NEW_ASSIGNMENT_DUE_DATE_SCHEDULED = "new_assignment_due_date_scheduled";

	protected static final String GROUP_LIST = "group";

	protected static final String GROUP_NAME = "authzGroup";
	
	protected static final String GROUP_SECTION_PROPERTY = "sections_category";

	
	// the file types for zip download
	protected static final String ZIP_COMMENT_FILE_TYPE = ".txt";
	protected static final String ZIP_SUBMITTED_TEXT_FILE_TYPE = ".html";

	// SAK-17606 - Property for whether an assignment uses anonymous grading (user settable)
	protected static final String NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING = "new_assignment_check_anonymous_grading";

	// SAK-29314
	private static final String SUBMISSION_ATTR_IS_USER_SUB = "isUserSubmission";

//	spring service injection
	
	
	protected ContentReviewService contentReviewService;
	public void setContentReviewService(ContentReviewService contentReviewService) {
		this.contentReviewService = contentReviewService;
	}
	
	private AssignmentPeerAssessmentService assignmentPeerAssessmentService = null;
	public void setAssignmentPeerAssessmentService(AssignmentPeerAssessmentService assignmentPeerAssessmentService){
		this.assignmentPeerAssessmentService = assignmentPeerAssessmentService;
	}

	private SecurityService securityService = null;
	public void setSecurityService(SecurityService securityService){
		this.securityService = securityService;
	}

	private DeveloperHelperService developerHelperService = null;
	public void setDeveloperHelperService( DeveloperHelperService developerHelperService ) {
		this.developerHelperService = developerHelperService;
	}

	private AuthzGroupService authzGroupService;
	public void setAuthzGroupService (AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	String newline = "<br />\n";
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a Storage object for Assignments.
	 * 
	 * @return The new storage object.
	 */
	protected abstract AssignmentStorage newAssignmentStorage();

	/**
	 * Construct a Storage object for AssignmentContents.
	 * 
	 * @return The new storage object.
	 */
	protected abstract AssignmentContentStorage newContentStorage();

	/**
	 * Construct a Storage object for AssignmentSubmissions.
	 * 
	 * @return The new storage object.
	 */
	protected abstract AssignmentSubmissionStorage newSubmissionStorage();

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative -
	 *        if true, form within the access path only (i.e. starting with /msg)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	static protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : m_serverConfigurationService.getAccessUrl()) + m_relativeAccessPoint;

	} // getAccessPoint

	/**
	 * Access the internal reference which can be used to assess security clearance.
	 * 
	 * @param id
	 *        The assignment id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	public String assignmentReference(String context, String id)
	{
		String retVal = null;
		if (context == null)
			retVal = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + id;
		else
			retVal = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context + Entity.SEPARATOR + id;
		return retVal;

	} // assignmentReference
	
	/**
	 * I feel silly having to look up the entire assignment object just to get the reference, 
	 * but if there's no context, that seems to be the only way to do it.
	 * @param id
	 * @return
	 */
	public String assignmentReference(String id) {
		String ref = null;
		Assignment assignment = findAssignment(id);
		if (assignment != null)
			ref = assignment.getReference();
		return ref;
	} // assignmentReference

        public List getSortedGroupUsers(Group _g) {
            List retVal = new ArrayList();
            Iterator<Member> _members = _g.getMembers().iterator();
            while (_members.hasNext()) {
                Member _member = _members.next();
                try
                {
                    retVal.add(UserDirectoryService.getUser(_member.getUserId()));
                }
                catch (Exception e)
                {
                    M_log.warn(" BaseAssignmentSubmission Group getSubmitters" + e.getMessage() + _member.getUserId());
                }
            }
            java.util.Collections.sort(retVal, new UserComparator());
            return retVal;
         }

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The content id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	public String contentReference(String context, String id)
	{
		String retVal = null;
		if (context == null)
			retVal = getAccessPoint(true) + Entity.SEPARATOR + "c" + Entity.SEPARATOR + id;
		else
			retVal = getAccessPoint(true) + Entity.SEPARATOR + "c" + Entity.SEPARATOR + context + Entity.SEPARATOR + id;
		return retVal;

	} // contentReference

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The submission id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	public String submissionReference(String context, String id, String assignmentId)
	{
		String retVal = null;
		if (context == null)
			retVal = getAccessPoint(true) + Entity.SEPARATOR + "s" + Entity.SEPARATOR + id;
		else
			retVal = getAccessPoint(true) + Entity.SEPARATOR + "s" + Entity.SEPARATOR + context + Entity.SEPARATOR + assignmentId
					+ Entity.SEPARATOR + id;
		return retVal;

	} // submissionReference

	/**
	 * Access the assignment id extracted from an assignment reference.
	 * 
	 * @param ref
	 *        The assignment reference string.
	 * @return The the assignment id extracted from an assignment reference.
	 */
	protected String assignmentId(String ref)
	{
		if (ref == null) return ref;
		int i = ref.lastIndexOf(Entity.SEPARATOR);
		if (i == -1) return ref;
		String id = ref.substring(i + 1);
		return id;

	} // assignmentId

	/**
	 * Access the content id extracted from a content reference.
	 * 
	 * @param ref
	 *        The content reference string.
	 * @return The the content id extracted from a content reference.
	 */
	protected String contentId(String ref)
	{
		int i = ref.lastIndexOf(Entity.SEPARATOR);
		if (i == -1) return ref;
		String id = ref.substring(i + 1);
		return id;

	} // contentId

	/**
	 * Access the submission id extracted from a submission reference.
	 * 
	 * @param ref
	 *        The submission reference string.
	 * @return The the submission id extracted from a submission reference.
	 */
	protected String submissionId(String ref)
	{
		int i = ref.lastIndexOf(Entity.SEPARATOR);
		if (i == -1) return ref;
		String id = ref.substring(i + 1);
		return id;

	} // submissionId

	/**
	 * Check security permission.
	 * 
	 * @param lock -
	 *        The lock id string.
	 * @param resource -
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowed, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!securityService.unlock(lock, resource))
		{
			return false;
		}

		return true;

	}// unlockCheck
	
	/**
	 * SAK-21525 Groups need to be queried, not just the site.
	 * 
	 * @param lock The security function to be checked, 'asn.submit' for example.
	 * @param resource The resource to be accessed
	 * @param assignment An Assignment object. We use this for the group checks.
	 * @return
	 */
	protected boolean unlockCheckWithGroups(String lock, String resource, Assignment assignment)
	{
		// SAK-23755 addons:
		// super user should be allowed
		if (securityService.isSuperUser())
			return true;
	
		// all.groups permission should apply down to group level
		String context = assignment.getContext();
		String userId = SessionManager.getCurrentSessionUserId();
		if (allowAllGroups(context) && securityService.unlock(lock, SiteService.siteReference(context)))
		{
			return true;
		}
		
		// group level users
		Collection groupIds = null;
		//SAK-23235 this method can be passed a null assignment -DH
		if (assignment != null) 
		{
			groupIds = assignment.getGroups();
		}
		if(groupIds != null && groupIds.size() > 0)
		{
			Iterator i = groupIds.iterator();
			while(i.hasNext())
			{
				String groupId = (String) i.next();
				boolean isAllowed
					= securityService.unlock(lock,groupId);
				
				if(isAllowed) return true;
			}
			
                        if (SECURE_ADD_ASSIGNMENT_SUBMISSION.equals(lock) && assignment.isGroup())
                                return securityService.unlock(lock, resource); 
                        else
                                return false;
		}
		else
		{
			return securityService.unlock(lock, SiteService.siteReference(context));
		}
	}// unlockCheckWithGroups

	/**
	 * Check security permission.
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if either allowed, false if not
	 */
	protected boolean unlockCheck2(String lock1, String lock2, String resource)
	{
		// check the first lock
		if (securityService.unlock(lock1, resource)) return true;

		// if the second is different, check that
		if ((!lock1.equals(lock2)) && (securityService.unlock(lock2, resource))) return true;

		return false;

	} // unlockCheck2

	/**
	 * Check security permission.
	 * 
	 * @param lock -
	 *        The lock id string.
	 * @param resource -
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	protected void unlock(String lock, String resource) throws PermissionException
	{
		if (!unlockCheck(lock, resource))
		{
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), lock, resource);
		}

	} // unlock

	/**
	 * Check security permission.
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access to either.
	 */
	protected void unlock2(String lock1, String lock2, String resource) throws PermissionException
	{
		if (!unlockCheck2(lock1, lock2, resource))
		{
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), lock1 + "/" + lock2, resource);
		}

	} // unlock2

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/


	
	/** Dependency: MemoryService. */
	protected MemoryService m_memoryService = null;

	/**
	 * Dependency: MemoryService.
	 * 
	 * @param service
	 *        The MemoryService.
	 */
	public void setMemoryService(MemoryService service)
	{
		m_memoryService = service;
	}
	
	/** Dependency: ContentHostingService. */
	protected ContentHostingService m_contentHostingService = null;

	/**
	 * Dependency:ContentHostingService.
	 * 
	 * @param service
	 *        The ContentHostingService.
	 */
	public void setContentHostingService(ContentHostingService service)
	{
		m_contentHostingService = service;
	}

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param value true or false
     * @deprecated 7 April 2014 - this has no effect anymore and should be removed in 11 release
	 */
	public void setCaching(String value) {} // intentionally blank

	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;

	/**
	 * Dependency: EntityManager.
	 * 
	 * @param service
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager service)
	{
		m_entityManager = service;
	}

	/** Dependency: ServerConfigurationService. */
	static protected ServerConfigurationService m_serverConfigurationService = null;

	/**
	 * Dependency: ServerConfigurationService.
	 * 
	 * @param service
	 *        The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		m_serverConfigurationService = service;
	}

	/** Dependency: TaggingManager. */
	protected TaggingManager m_taggingManager = null;

	/**
	 * Dependency: TaggingManager.
	 * 
	 * @param manager
	 *        The TaggingManager.
	 */
	public void setTaggingManager(TaggingManager manager)
	{
		m_taggingManager = manager;
	}

	/** Dependency: AssignmentActivityProducer. */
	protected AssignmentActivityProducer m_assignmentActivityProducer = null;

	/**
	 * Dependency: AssignmentActivityProducer.
	 * 
	 * @param assignmentActivityProducer
	 *        The AssignmentActivityProducer.
	 */
	public void setAssignmentActivityProducer(AssignmentActivityProducer assignmentActivityProducer)
	{
		m_assignmentActivityProducer = assignmentActivityProducer;
	}
	
	/** Dependency: GradebookService. */
	protected GradebookService m_gradebookService = null;
	/**
	 * Dependency: GradebookService
	 * 
	 * @param gradebookService
	 *        The GradebookService
	 */
	public void setGradebookService(GradebookService gradebookService)
	{
		m_gradebookService= gradebookService;
	}
	
	/** Dependency: GradebookExternalAssessmentService. */
	protected GradebookExternalAssessmentService m_gradebookExternalAssessmentService = null;
	/**
	 * Dependency: GradebookExternalAssessmentService
	 * 
	 * @param gradebookExternalAssessmentService
	 *        The GradebookExternalAssessmentService
	 */
	public void setGradebookExternalAssessmentService(GradebookExternalAssessmentService gradebookExternalAssessmentService)
	{
		m_gradebookExternalAssessmentService= gradebookExternalAssessmentService;
	}
	/** Dependency: CalendarService. */
	protected CalendarService m_calendarService = null;
	/**
	 * Dependency: CalendarService
	 * 
	 * @param calendarService
	 *        The CalendarService
	 */
	public void setCalendarService(CalendarService calendarService)
	{
		m_calendarService= calendarService;
	}
	
	/** Dependency: AnnouncementService. */
	protected AnnouncementService m_announcementService = null;
	/**
	 * Dependency: AnnouncementService
	 * 
	 * @param announcementService
	 *        The AnnouncementService
	 */
	public void setAnnouncementService(AnnouncementService announcementService)
	{
		m_announcementService= announcementService;
	}

	/** Dependency: allowGroupAssignments setting */
	protected boolean m_allowGroupAssignments = true;

	/**
	 * Dependency: allowGroupAssignments
	 * 
	 * @param allowGroupAssignments
	 *        the setting
	 */
	public void setAllowGroupAssignments(boolean allowGroupAssignments)
	{
		m_allowGroupAssignments = allowGroupAssignments;
	}
	/**
	 * Get
	 * 
	 * @return allowGroupAssignments
	 */
	public boolean getAllowGroupAssignments()
	{
		return m_allowGroupAssignments;
	}
	
	/** Dependency: allowSubmitByInstructor setting */
	protected boolean m_allowSubmitByInstructor = true;

	/**
	 * Dependency: allowSubmitByInstructor
	 * 
	 * @param allowSubmitByInstructor
	 *        the setting
	 */
	public void setAllowSubmitByInstructor(boolean allowSubmitByInstructor)
	{
		m_allowSubmitByInstructor = allowSubmitByInstructor;
	}
	/**
	 * Get
	 * 
	 * @return allowSubmitByInstructor
	 */
	public boolean getAllowSubmitByInstructor()
	{
		return m_allowSubmitByInstructor;
	}
	
	/** Dependency: allowGroupAssignmentsInGradebook setting */
	protected boolean m_allowGroupAssignmentsInGradebook = true;

	/**
	 * Dependency: allowGroupAssignmentsInGradebook
	 * 
	 * @param allowGroupAssignmentsInGradebook
	 */
	public void setAllowGroupAssignmentsInGradebook(boolean allowGroupAssignmentsInGradebook)
	{
		m_allowGroupAssignmentsInGradebook = allowGroupAssignmentsInGradebook;
	}
	/**
	 * Get
	 * 
	 * @return allowGroupAssignmentsGradebook
	 */
	public boolean getAllowGroupAssignmentsInGradebook()
	{
		return m_allowGroupAssignmentsInGradebook;
	}
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		m_relativeAccessPoint = REFERENCE_ROOT;
		M_log.info(this + " init()");

		// construct storage helpers and read
		m_assignmentStorage = newAssignmentStorage();
		m_assignmentStorage.open();
		m_contentStorage = newContentStorage();
		m_contentStorage.open();
		m_submissionStorage = newSubmissionStorage();
		m_submissionStorage.open();

        m_allowSubmitByInstructor = m_serverConfigurationService.getBoolean("assignments.instructor.submit.for.student", true);
        if (!m_allowSubmitByInstructor) {
            M_log.info("Instructor submission of assignments is disabled - add assignments.instructor.submit.for.student=true to sakai config to enable");
        } else {
            M_log.info("Instructor submission of assignments is enabled");
        }

		// register as an entity producer
		m_entityManager.registerEntityProducer(this, REFERENCE_ROOT);

		// register functions
		FunctionManager.registerFunction(SECURE_ALL_GROUPS);
		FunctionManager.registerFunction(SECURE_ADD_ASSIGNMENT);
		FunctionManager.registerFunction(SECURE_ADD_ASSIGNMENT_SUBMISSION);
		FunctionManager.registerFunction(SECURE_REMOVE_ASSIGNMENT);
		FunctionManager.registerFunction(SECURE_ACCESS_ASSIGNMENT);
		FunctionManager.registerFunction(SECURE_UPDATE_ASSIGNMENT);
		FunctionManager.registerFunction(SECURE_GRADE_ASSIGNMENT_SUBMISSION);
		FunctionManager.registerFunction(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS);
		FunctionManager.registerFunction(SECURE_SHARE_DRAFTS);
		
 		//if no contentReviewService was set try discovering it
 		if (contentReviewService == null)
 		{
 			contentReviewService = (ContentReviewService) ComponentManager.get(ContentReviewService.class.getName());
 		}
 		
	} // init

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_assignmentStorage.close();
		m_assignmentStorage = null;
		m_contentStorage.close();
		m_contentStorage = null;
		m_submissionStorage.close();
		m_submissionStorage = null;

		M_log.info(this + " destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Creates and adds a new Assignment to the service.
	 * 
	 * @param context -
	 *        Describes the portlet context - generated with DefaultId.getChannel().
	 * @return The new Assignment object.
	 * @throws IdInvalidException
	 *         if the id contains prohibited characers.
	 * @throws IdUsedException
	 *         if the id is already used in the service.
	 * @throws PermissionException
	 *         if current User does not have permission to do this.
	 */
	public AssignmentEdit addAssignment(String context) throws PermissionException
	{
		M_log.debug(this + " ENTERING ADD ASSIGNMENT : CONTEXT : " + context);

		String assignmentId = null;
		boolean badId = false;

		do
		{
			badId = !Validator.checkResourceId(assignmentId);
			assignmentId = IdManager.createUuid();

			if (m_assignmentStorage.check(assignmentId)) badId = true;
		}
		while (badId);

		String key = assignmentReference(context, assignmentId);
		
		// security check
		if (!allowAddAssignment(context))
		{
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT, key);
		}

		// storage
		AssignmentEdit assignment = m_assignmentStorage.put(assignmentId, context);

		// event for tracking
		((BaseAssignmentEdit) assignment).setEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT);

		
			M_log.debug(this + " LEAVING ADD ASSIGNMENT WITH : ID : " + assignment.getId());

		return assignment;

	} // addAssignment

	/**
	 * Add a new assignment to the directory, from a definition in XML. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param el
	 *        The XML DOM Element defining the assignment.
	 * @return A locked AssignmentEdit object (reserving the id).
	 * @exception IdInvalidException
	 *            if the assignment id is invalid.
	 * @exception IdUsedException
	 *            if the assignment id is already used.
	 * @exception PermissionException
	 *            if the current user does not have permission to add an assignnment.
	 */
	public AssignmentEdit mergeAssignment(Element el) throws IdInvalidException, IdUsedException, PermissionException
	{
		// construct from the XML
		Assignment assignmentFromXml = new BaseAssignment(el);

		// check for a valid assignment name
		if (!Validator.checkResourceId(assignmentFromXml.getId())) throw new IdInvalidException(assignmentFromXml.getId());

		// check security (throws if not permitted)
		unlock(SECURE_ADD_ASSIGNMENT, assignmentFromXml.getReference());

		// reserve a assignment with this id from the info store - if it's in use, this will return null
		AssignmentEdit assignment = m_assignmentStorage.put(assignmentFromXml.getId(), assignmentFromXml.getContext());
		if (assignment == null)
		{
			throw new IdUsedException(assignmentFromXml.getId());
		}

		// transfer from the XML read assignment object to the AssignmentEdit
		((BaseAssignmentEdit) assignment).set(assignmentFromXml);

		((BaseAssignmentEdit) assignment).setEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT);

		ResourcePropertiesEdit propertyEdit = (BaseResourcePropertiesEdit)assignment.getProperties();
		try
		{
			propertyEdit.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
		}
		catch(EntityPropertyNotDefinedException epnde)
		{
			String now = TimeService.newTime().toString();
			propertyEdit.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		}
		catch(EntityPropertyTypeException epte)
		{
			M_log.error(this + " mergeAssignment error when trying to get creation time property " + epte);
		}

		return assignment;
	}

	/**
	 * Creates and adds a new Assignment to the service which is a copy of an existing Assignment.
	 * 
	 * @param assignmentId -
	 *        The Assignment to be duplicated.
	 * @return The new Assignment object, or null if the original Assignment does not exist.
	 * @throws PermissionException
	 *         if current User does not have permission to do this.
	 */
	public AssignmentEdit addDuplicateAssignment(String context, String assignmentReference) throws PermissionException,
			IdInvalidException, IdUsedException, IdUnusedException
	{
		
			M_log.debug(this + " ENTERING ADD DUPLICATE ASSIGNMENT WITH ID : " + assignmentReference);

		AssignmentEdit retVal = null;
		AssignmentContentEdit newContent = null;

		if (assignmentReference != null)
		{
			String assignmentId = assignmentId(assignmentReference);
			if (!m_assignmentStorage.check(assignmentId))
				throw new IdUnusedException(assignmentId);
			else
			{
				
					M_log.debug(this + " addDuplicateAssignment : assignment exists - will copy");

				Assignment existingAssignment = getAssignment(assignmentReference);
				newContent = addDuplicateAssignmentContent(context, existingAssignment.getContentReference());
				commitEdit(newContent);

				retVal = addAssignment(context);
				retVal.setContentReference(newContent.getReference());
				retVal.setTitle(existingAssignment.getTitle() + " - " + rb.getString("assignment.copy"));
				retVal.setSection(existingAssignment.getSection());
				retVal.setOpenTime(existingAssignment.getOpenTime());
				retVal.setDueTime(existingAssignment.getDueTime());
				retVal.setDropDeadTime(existingAssignment.getDropDeadTime());
				retVal.setCloseTime(existingAssignment.getCloseTime());
				retVal.setDraft(true);
                		retVal.setGroup(existingAssignment.isGroup());
               			retVal.setAllowPeerAssessment(existingAssignment.getAllowPeerAssessment());
               			retVal.setPeerAssessmentInstructions(existingAssignment.getPeerAssessmentInstructions());
                		retVal.setPeerAssessmentAnonEval(existingAssignment.getPeerAssessmentAnonEval());
                		retVal.setPeerAssessmentNumReviews(existingAssignment.getPeerAssessmentNumReviews());
                		retVal.setPeerAssessmentPeriod(existingAssignment.getPeerAssessmentPeriod());
                		retVal.setPeerAssessmentStudentViewReviews(existingAssignment.getPeerAssessmentStudentViewReviews());
				ResourcePropertiesEdit pEdit = (BaseResourcePropertiesEdit) retVal.getProperties();
				pEdit.addAll(existingAssignment.getProperties());
				addLiveProperties(pEdit);
			}
		}

		
			M_log.debug(this + " ADD DUPLICATE ASSIGNMENT : LEAVING ADD DUPLICATE ASSIGNMENT WITH ID : "
					+ retVal != null ? retVal.getId() : "");

		return retVal;
	}

	/**
	 * Access the Assignment with the specified reference.
	 * 
	 * @param assignmentReference -
	 *        The reference of the Assignment.
	 * @return The Assignment corresponding to the reference, or null if it does not exist.
	 * @throws IdUnusedException
	 *         if there is no object with this reference.
	 * @throws PermissionException
	 *         if the current user is not allowed to access this.
	 */
	public Assignment getAssignment(String assignmentReference) throws IdUnusedException, PermissionException
	{
		M_log.debug(this + " GET ASSIGNMENT : REF : " + assignmentReference);

		// check security on the assignment
		unlockCheck(SECURE_ACCESS_ASSIGNMENT, assignmentReference);
		
		Assignment assignment = findAssignment(assignmentReference);
		
		String currentUserId = SessionManager.getCurrentSessionUserId();
		
		if (assignment == null) throw new IdUnusedException(assignmentReference);
		
		return checkAssignmentAccessibleForUser(assignment, currentUserId);

	}// getAssignment

	/**
	 * Retrieves the current status of an assignment.
	 * @param assignmentReference
	 * @return
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	public String getAssignmentStatus(String assignmentReference) throws IdUnusedException, PermissionException
	{
		M_log.debug(this + " GET ASSIGNMENT : REF : " + assignmentReference);

		// check security on the assignment
		unlockCheck(SECURE_ACCESS_ASSIGNMENT, assignmentReference);
		
		Assignment assignment = findAssignment(assignmentReference);
		
		if (assignment == null) throw new IdUnusedException(assignmentReference);
		
		return assignment.getStatus();
		
	} // getAssignmentStatus
	
	/**
	 * Check visibility of an assignment for a given user. We consider an
	 * an assignment to be visible to the user if it has been opened and is
	 * not deleted. However, we allow access to deleted assignments if the
	 * user has already made a submission for the assignment.
	 *
	 * Note that this method does not check permissions at all. It should
	 * already be established that the user is permitted to access this
	 * assignment.
	 *
	 * @param assignment the assignment to check
	 * @param userId the user for whom to check
	 * @return true if the assignment is available (open, not deleted) or
	 *         submitted by the specified user; false otherwise
	 */
	private boolean isAvailableOrSubmitted(Assignment assignment, String userId)
	{
		boolean accessible = false;
		String deleted = assignment.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
		if (deleted == null || "".equals(deleted))
		{
			// show not deleted, not draft, opened assignments
			Time openTime = assignment.getOpenTime();
			Time visibleTime = assignment.getVisibleTime();
			if (
				(
				(openTime != null && TimeService.newTime().after(openTime))||
				(visibleTime != null && TimeService.newTime().after(visibleTime))
				)
				&& !assignment.getDraft())
			{
				accessible = true;
			}
		}
		else if (deleted.equalsIgnoreCase(Boolean.TRUE.toString()) && (assignment.getContent().getTypeOfSubmission() != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
				&& getSubmission(assignment.getReference(), userId) != null)
		{
			// and those deleted but not non-electronic assignments but the user has made submissions to them
			accessible = true;
		}
		if(assignment.getAccess() == Assignment.AssignmentAccess.GROUPED){
			Collection<Group> asgGroups = assignment.getGroups();
			Collection<Group> allowedGroups = getGroupsAllowFunction(SECURE_UPDATE_ASSIGNMENT, assignment.getContext(), userId);
			if(isIntersectionGroupRefsToGroups(asgGroups, allowedGroups)){
				accessible=true;
			}
		}
		return accessible;
	}

	private Assignment checkAssignmentAccessibleForUser(Assignment assignment, String currentUserId) throws PermissionException {
		
		if (assignment.getAccess() == Assignment.AssignmentAccess.GROUPED)
		{
		    String context = assignment.getContext();
		    Collection<Group> asgGroups = assignment.getGroups();
		    Collection<Group> allowedGroups = getGroupsAllowGetAssignment(context, currentUserId);
		    // reject and throw PermissionException if there is no intersection
		    if (!allowAllGroups(context) && !currentUserIsCreator(assignment) && !isIntersectionGroupRefsToGroups(asgGroups, allowedGroups)) {
		        throw new PermissionException(currentUserId, SECURE_ACCESS_ASSIGNMENT, assignment.getReference());
		    }
		}
		
		if (allowAddAssignment(assignment.getContext()))
		{
			// always return for users can add assignent in the context
			return assignment;
		}
		else if (isAvailableOrSubmitted(assignment, currentUserId))
		{
			return assignment;
		}
		throw new PermissionException(currentUserId, SECURE_ACCESS_ASSIGNMENT, assignment.getReference());
	}
	
	protected Assignment findAssignment(String assignmentReference)
	{
		Assignment assignment = null;

		String assignmentId = assignmentId(assignmentReference);

		assignment = m_assignmentStorage.get(assignmentId);

		return assignment;
	}

	protected Assignment findAssignmentFromContent(String context, String contentId){
		if(context == null || contentId == null){
			return null;
		}
		List<Assignment> assignmentList = getUnfilteredAssignments(context);
		for(Assignment assignment : assignmentList){
			String contentReference = assignment.getContentReference();
			if(contentId.equals(contentReference)){
				return assignment;
			}
		}
		return null;
	}

	/**
	 * Access all assignment objects - known to us (not from external providers).
	 * 
	 * @return A list of assignment objects.
	 */
	protected List getAssignments(String context)
	{
		return assignments(context, null);

	} // getAssignments
	
	/**
	 * Access all assignment objects - known to us (not from external providers) and accessible by the user
	 * 
	 * @return A list of assignment objects.
	 */
	protected List getAssignments(String context, String userId)
	{
		return assignments(context, userId);

	} // getAssignments

	//
	private List assignments(String context, String userId) 
	{
		List rv = new ArrayList();
		
		if (!allowGetAssignment(context) && getGroupsAllowGetAssignment(context).isEmpty())
		{
			// no permission to read assignment in context
			return rv;
		}
		else
		{
			List assignments = getUnfilteredAssignments(context);

			if (userId == null)
			{
				userId = SessionManager.getCurrentSessionUserId();
			}

			// check for the site and group permissions of these assignments as well as visibility (release time, etc.)
			rv = getAccessibleAssignments(assignments, context, userId);
		}

		return rv;
	}

	/**
	 * Access all assignment objects for a site without considering user permissions.
	 * This should be used with care; almost all scenarios should use {@link getAssignments(String)}
	 * or {@link getAssignments(String, String)}, which do enforce permissions and visibility.
	 *
	 * @return A list of Assignment objects.
	 */
	protected List getUnfilteredAssignments(String context)
	{
		List assignments = m_assignmentStorage.getAll(context);
		return assignments;
	}

	/**
	 * Filter a list of assignments to those that the supplied user can access.
	 *
	 * This method is primarily provided to be called from assignments() for
	 * set-based efficiency over iteration in building a list of assignments
	 * for a given user.
	 *
	 * There are a few ways that we consider an assignment to be accessible:
	 * 1. The user can add assignments to the site, or
	 * 2. The assignment is grouped and the user can view assignments in at
	 *    least one of those groups, or
	 * 3. The assignment is ungrouped and the user can view assignments in
	 *    the site
	 * An additional state check applies, which is that the assignment is
	 * not visible if it is deleted, except when the user has made a
	 * submission for it already or can add (manage) assignments.
	 *
	 * These rules were extracted from assignments() and we are enforcing
	 * them here for a set, rather than a single assignment.
	 *
	 * This is a somewhat awkward signature; it should really either have just the
	 * assignments list or just the siteId, but the other methods are not refactored
	 * now. Namely, getAssignments calls assignments, which has some cache specifics
	 * and other items that would need to be refactored very carefully. Rather than
	 * potentially changing the behavior subtly, this only replaces the iterative
	 * permissions checks with set-based ones.
	 *
	 * @param assignments a list of assignments to filter; must all be from the same site
	 * @param siteId the Site ID for all assignments
	 * @param userId the user whose access should be checked for the assignments
	 * @return a list of the assignments that are accessible; will never be null but may be empty
	 */
	protected List<Assignment> getAccessibleAssignments(List<Assignment> assignments, String siteId, String userId)
	{
		// Make sure that everything is from the specified site
		List<Assignment> siteAssignments = filterAssignmentsBySite(assignments, siteId);

		// Check whether the user can add assignments for the site.
		// If so, return the full list.
		String siteRef = SiteService.siteReference(siteId);
		boolean allowAdd = securityService.unlock(userId, SECURE_ALL_GROUPS, siteRef);
		if (allowAdd)
		{
			return siteAssignments;
		}

		// Partition the assignments into grouped and ungrouped for access checks
		List<List<Assignment>> partitioned = partitionAssignments(siteAssignments);
		List<Assignment> grouped = partitioned.get(0);
		List<Assignment> ungrouped = partitioned.get(1);

		List<Assignment> permitted = new ArrayList<Assignment>();

		// Check the user's site permissions and collect all of the ungrouped
		// assignments if the user has permission
		boolean allowSiteGet = securityService.unlock(userId, SECURE_ACCESS_ASSIGNMENT, siteRef);
		if (allowSiteGet)
		{
			permitted.addAll(ungrouped);
		}

		// Collect grouped assignments that the user can access
		permitted.addAll(filterGroupedAssignmentsForAccess(grouped, siteId, userId));

		// Filter for visibility/submission state
		List<Assignment> visible = (securityService.unlock(userId, SECURE_ADD_ASSIGNMENT, siteRef))? permitted : filterAssignmentsByVisibility(permitted, userId);

		// We are left with the original list filtered by site/group permissions and visibility/submission state
		return visible;
	}

	/**
	 * Filter a list of assignments to those in a given site.
	 *
	 * @param assignments the list of assignments to filter; none may be null
	 * @param siteId the site ID to use to filter
	 * @return a new list with only the assignments that belong to the site;
	 *         never null, but empty if the site doesn't exist, the assignments
	 *         list is empty, or none of the assignments belong to the site
	 */
	protected List<Assignment> filterAssignmentsBySite(List<Assignment> assignments, String siteId)
	{
		List<Assignment> filtered = new ArrayList<Assignment>();
		if (siteId == null)
		{
			return filtered;
		}

		try
		{
			SiteService.getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			return filtered;
		}

		for (Assignment assignment : assignments)
		{
			if (assignment != null && siteId.equals(assignment.getContext()))
			{
				filtered.add(assignment);
			}
		}
		return filtered;
	}

	/**
	 * Partition a list of assignments into those that are grouped and ungrouped.
	 *
	 * @param assignments the list of assignments to inspect and partition
	 * @return a two-element list containing List<Assignment> in both indexes;
	 *         the first is the grouped assignments, the second is ungrouped;
	 *         never null, always two elements, neither list is null;
	 *         any null assignments will be omitted in the final lists
	 */
	protected List<List<Assignment>> partitionAssignments(List<Assignment> assignments)
	{
		List<Assignment> grouped = new ArrayList<Assignment>();
		List<Assignment> ungrouped = new ArrayList<Assignment>();
		for (Assignment assignment : assignments)
		{
			if (assignment != null && assignment.getAccess() == Assignment.AssignmentAccess.GROUPED)
			{
				grouped.add(assignment);
			}
			else
			{
				ungrouped.add(assignment);
			}
		}

		List<List<Assignment>> partitioned = new ArrayList<List<Assignment>>();
		partitioned.add(grouped);
		partitioned.add(ungrouped);
		return partitioned;
	}

	/**
	 * Filter a list of grouped assignments by permissions based on a given site. Note that
	 * this does not consider the assignment or submission state, only permissions.
	 *
	 * @param assignments the list of assignments to filter; should all be grouped and from the same site
	 * @param siteId the site to which all of the assignments belong
	 * @param userId the user for which group permissions should be checked
	 * @return a new list of assignments, containing those supplied that the user
	 *         can access, based on permission to view the assignment in one or more of its
	 *         groups, permission to add assignments in the site, or permission to
	 *         view assignments in all of the site's groups; never null but may be empty
	 *
	 */
	protected List<Assignment> filterGroupedAssignmentsForAccess(List<Assignment> assignments, String siteId, String userId)
	{
		List<Assignment> filtered = new ArrayList<Assignment>();

		// Short-circuit to save the group query if we can't make a reasonable check
		if (assignments == null || assignments.isEmpty() || siteId == null || userId == null)
		{
			return filtered;
		}

		// Collect the groups where the user is permitted to view assignments
		// and the groups covered by the assignments, then check the
		// intersection to keep only visible assignments.
		Collection<Group> allowedGroups = (Collection<Group>) getGroupsAllowGetAssignment(siteId, userId);
		Set<String> allowedGroupRefs = new HashSet<String>();
		for (Group group : allowedGroups)
		{
			allowedGroupRefs.add(group.getReference());
		}

		for (Assignment assignment : assignments)
		{
			for (String groupRef : (Collection<String>) assignment.getGroups())
			{
				if (allowedGroupRefs.contains(groupRef))
				{
					filtered.add(assignment);
					break;
				}
				else {
					if (currentUserIsCreator(assignment))
					{
						filtered.add(assignment);
						break;
					}
				}
			}
		}
		return filtered;
	}
	
	/**
	 * return true if the current session user is the creator of the assignment
	 * @param assignment
	 * @return
	 */
	private boolean currentUserIsCreator(Assignment assignment)
	{
		// return the assignment if the current user is the creator
		String assignmentCreator = assignment.getCreator();
		if (assignmentCreator != null && assignmentCreator.equals(UserDirectoryService.getCurrentUser().getId()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Filter a list of assignments based on visibility (open time, deletion, submission, etc.)
	 * for a specified user. Note that this only considers assignment and submission state and
	 * does not consider permissions so the assignments should have already been checked for
	 * permissions for the given user.
	 *
	 * @param assignments the list of assignments to filter
	 * @param userId the user for whom to check visibility; should be permitted to
	 *               access all of the assignments
	 * @return a new list containing those supplied that the user may access, based
	 *         on visibility; never null but may be empty
	 */
	protected List<Assignment> filterAssignmentsByVisibility(List<Assignment> assignments, String userId)
	{
		List<Assignment> visible = new ArrayList<Assignment>();
		for (Assignment assignment : assignments)
		{
			if (assignment != null && isAvailableOrSubmitted(assignment, userId))
			{
				visible.add(assignment);
			}
		}
		return visible;
	}

	/**
	 * See if the collection of group reference strings has at least one group that is in the collection of Group objects.
	 * 
	 * @param groupRefs
	 *        The collection (String) of group references.
	 * @param groups
	 *        The collection (Group) of group objects.
	 * @return true if there is interesection, false if not.
	 */
	protected boolean isIntersectionGroupRefsToGroups(Collection groupRefs, Collection groups)
	{	
		for (Iterator iRefs = groupRefs.iterator(); iRefs.hasNext();)
		{
			String findThisGroupRef = (String) iRefs.next();
			for (Iterator iGroups = groups.iterator(); iGroups.hasNext();)
			{
				String thisGroupRef = ((Group) iGroups.next()).getReference();
				if (thisGroupRef.equals(findThisGroupRef))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Get a locked assignment object for editing. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param id
	 *        The assignment id string.
	 * @return An AssignmentEdit object for editing.
	 * @exception IdUnusedException
	 *            if not found, or if not an AssignmentEdit object
	 * @exception PermissionException
	 *            if the current user does not have permission to edit this assignment.
	 * @exception InUseException
	 *            if the assignment is being edited by another user.
	 */
	public AssignmentEdit editAssignment(String assignmentReference) throws IdUnusedException, PermissionException, InUseException
	{
		// check security (throws if not permitted)
		unlock(SECURE_UPDATE_ASSIGNMENT, assignmentReference);

		String assignmentId = assignmentId(assignmentReference);

		// check for existance
		if (!m_assignmentStorage.check(assignmentId))
		{
			throw new IdUnusedException(assignmentId);
		}

		// ignore the cache - get the assignment with a lock from the info store
		AssignmentEdit assignmentEdit = m_assignmentStorage.edit(assignmentId);
		if (assignmentEdit == null) throw new InUseException(assignmentId);

		((BaseAssignmentEdit) assignmentEdit).setEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT);

		return assignmentEdit;

	} // editAssignment

	/**
	 * Commit the changes made to an AssignmentEdit object, and release the lock.
	 * 
	 * @param assignment
	 *        The AssignmentEdit object to commit.
	 */
	public void commitEdit(AssignmentEdit assignment)
	{
		// check for closed edit
		if (!assignment.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn(" commitEdit(): closed AssignmentEdit " + e.getMessage() + " assignment id=" + assignment.getId());
			}
			return;
		}

		// update the properties
		addLiveUpdateProperties(assignment.getPropertiesEdit());

		// complete the edit
		m_assignmentStorage.commit(assignment);

		//update peer assessment information:
		if(!assignment.getDraft() && assignment.getAllowPeerAssessment()){
			assignmentPeerAssessmentService.schedulePeerReview(assignment.getId());
		}else{
			assignmentPeerAssessmentService.removeScheduledPeerReview(assignment.getId());
		}

		// track it
		EventTrackingService.post(EventTrackingService.newEvent(((BaseAssignmentEdit) assignment).getEvent(), assignment
				.getReference(), true));

		// close the edit object
		((BaseAssignmentEdit) assignment).closeEdit();

	} // commitEdit

	/**
	 * Cancel the changes made to a AssignmentEdit object, and release the lock.
	 * 
	 * @param assignment
	 *        The AssignmentEdit object to commit.
	 */
	public void cancelEdit(AssignmentEdit assignment)
	{
		// check for closed edit
		if (!assignment.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn(" cancelEdit(): closed AssignmentEdit " + e.getMessage() + " assignment id=" + assignment.getId());
			}
			return;
		}

		// release the edit lock
		m_assignmentStorage.cancel(assignment);

		// close the edit object
		((BaseAssignmentEdit) assignment).closeEdit();

	} // cancelEdit(Assignment)

	/**
	 * {@inheritDoc}
	 */
	public void removeAssignment(AssignmentEdit assignment) throws PermissionException
	{
		if (assignment != null)
		{
			M_log.debug(this + " removeAssignment with id : " + assignment.getId());

			if (!assignment.isActiveEdit())
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					M_log.warn(" removeAssignment(): closed AssignmentEdit" + e.getMessage() + " assignment id=" + assignment.getId());
				}
				return;
			}

			// CHECK PERMISSION
			unlock(SECURE_REMOVE_ASSIGNMENT, assignment.getReference());

			// complete the edit
			m_assignmentStorage.remove(assignment);

			// track event
			EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, assignment.getReference(), true));

			// close the edit object
			((BaseAssignmentEdit) assignment).closeEdit();

			// remove any realm defined for this resource
			try
			{
				authzGroupService.removeAuthzGroup(assignment.getReference());
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(" removeAssignment: removing realm for assignment reference=" + assignment.getReference() + " : " + e.getMessage());
			}
		}

	}// removeAssignment
	
	/**
	 * {@inheritDoc}
	 */
	public void removeAssignmentAndAllReferences(AssignmentEdit assignment) throws PermissionException
	{
		if (assignment != null)
		{
			M_log.debug(this + " removeAssignmentAndAllReferences with id : " + assignment.getId());

			if (!assignment.isActiveEdit())
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					M_log.warn(" removeAssignmentAndAllReferences(): closed AssignmentEdit" + e.getMessage() + " assignment id=" + assignment.getId());
				}
				return;
			}

			// CHECK PERMISSION
			unlock(SECURE_REMOVE_ASSIGNMENT, assignment.getReference());
			
			// we may need to remove associated calendar events and annc, so get the basic info here
			ResourcePropertiesEdit pEdit = assignment.getPropertiesEdit();
			String context = assignment.getContext();
						
			// 1. remove associated calendar events, if exists
			removeAssociatedCalendarItem(getCalendar(context), assignment, pEdit);
			
			// 2. remove associated announcement, if exists
			removeAssociatedAnnouncementItem(getAnnouncementChannel(context), assignment, pEdit);

			// 3. remove Gradebook items, if linked
			removeAssociatedGradebookItem(pEdit, context);

			// 4. remove tags as necessary
			removeAssociatedTaggingItem(assignment);

			// 5. remove assignment submissions
			List submissions = getSubmissions(assignment);
			if (submissions != null) 
			{
			    for (Iterator sIterator=submissions.iterator(); sIterator.hasNext();)
			    {
			        AssignmentSubmission s = (AssignmentSubmission)sIterator.next();
			        String sReference = s.getReference();
			        try
			        {
				        removeSubmission(editSubmission(sReference));
			        }
					catch (PermissionException e) 
					{
						M_log.warn("removeAssignmentAndAllReference: User does not have permission to remove submission " + sReference + " for assignment: " + assignment.getId()  + e.getMessage());
					}
					catch (InUseException e) 
					{
						M_log.warn("removeAssignmentAndAllReference: submission " + sReference + " for assignment: " + assignment.getId() + " is in use. " + e.getMessage());
					}catch (IdUnusedException e) 
					{
						M_log.warn("removeAssignmentAndAllReference: submission " + sReference + " for assignment: " + assignment.getId() + " does not exist. " + e.getMessage());
					}
				}
			}
			
			// 6. remove associated content object
			try
			{
				removeAssignmentContent(editAssignmentContent(assignment.getContent().getReference()));
			}
			catch (AssignmentContentNotEmptyException e)
			{
				M_log.warn(" removeAssignmentAndAllReferences(): cannot remove non-empty AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
			}
			catch (PermissionException e)
			{
				M_log.warn(" removeAssignmentAndAllReferences(): not allowed to remove AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
			}
			catch (InUseException e)
			{
				M_log.warn(" removeAssignmentAndAllReferences(): AssignmentContent object for assignment = " + assignment.getId() + " is in used. " + e.getMessage());
			}
			catch (IdUnusedException e)
			{
				M_log.warn(" removeAssignmentAndAllReferences(): cannot find AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
			}
			
			// 7. remove assignment
			m_assignmentStorage.remove(assignment);

			// close the edit object
			((BaseAssignmentEdit) assignment).closeEdit();

			// 8. remove any realm defined for this resource
			try
			{
				authzGroupService.removeAuthzGroup(assignment.getReference());
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(" removeAssignment: removing realm for assignment reference=" + assignment.getReference() + " : " + e.getMessage());
			}
			
			// track event
			EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, assignment.getReference(), true));

		}

	}// removeAssignment


	/**
	 * remove the associated tagging items
	 * @param assignment
	 */
	private void removeAssociatedTaggingItem(AssignmentEdit assignment) {
		try 
		{
		    if (m_taggingManager.isTaggable()) {
		        for (TaggingProvider provider : m_taggingManager.getProviders()) {
		            provider.removeTags(m_assignmentActivityProducer.getActivity(assignment));
		        }
		    }
		} 
		catch (PermissionException pe) 
		{
		    M_log.warn("removeAssociatedTaggingItem: User does not have permission to remove tags for assignment: " + assignment.getId() + " via transferCopyEntities");
		}
	}



	/**
	 * remove the linked Gradebook item related with the assignment
	 * @param pEdit
	 * @param context
	 */
	private void removeAssociatedGradebookItem(ResourcePropertiesEdit pEdit, String context) {
		String associatedGradebookAssignment = pEdit.getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
		if (associatedGradebookAssignment != null) {
		     boolean isExternalAssignmentDefined = m_gradebookExternalAssessmentService.isExternalAssignmentDefined(context, associatedGradebookAssignment);
		    if (isExternalAssignmentDefined)
		    {
		    	m_gradebookExternalAssessmentService.removeExternalAssessment(context, associatedGradebookAssignment);
		    }
		}
	}
	
	private Calendar getCalendar(String contextId)
	{
		Calendar calendar = null;
		
		String calendarId = m_serverConfigurationService.getString("calendar", null);
		if (calendarId == null)
		{
		    calendarId = m_calendarService.calendarReference(contextId, SiteService.MAIN_CONTAINER);
		    try
		    {
		        calendar = m_calendarService.getCalendar(calendarId);
		    }
		    catch (IdUnusedException e)
		    {
		        M_log.warn("getCalendar: No calendar found for site: " + contextId);
		        calendar = null;
		    }
		    catch (PermissionException e)
		    {
		        M_log.error("getCalendar: The current user does not have permission to access " +
		                "the calendar for context: " + contextId, e);
		    }
		    catch (Exception ex)
		    {
		        M_log.error("getCalendar: Unknown exception occurred retrieving calendar for site: " + contextId, ex);
		        calendar = null;
		    }
		}
		
		return calendar;
	}
	
	/**
	 * Will determine if there is a calendar event associated with this assignment and
	 * remove it, if found.
	 * @param calendar Calendar
	 * @param aEdit AssignmentEdit
	 * @param pEdit ResourcePropertiesEdit
	 */
	private void removeAssociatedCalendarItem(Calendar calendar, AssignmentEdit aEdit, ResourcePropertiesEdit pEdit)
	{
	    String isThereEvent = pEdit.getProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
	    if (isThereEvent != null && isThereEvent.equals(Boolean.TRUE.toString()))
	    {
	        // remove the associated calendar event
	        if (calendar != null)
	        {
	            // already has calendar object
	            // get the old event
	            CalendarEvent event = null;
	            String oldEventId = pEdit.getProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
	            if (oldEventId != null)
	            {
	                try
	                {
	                    event = calendar.getEvent(oldEventId);
	                }
	                catch (IdUnusedException ee)
	                {
	                    // no action needed for this condition
	                    M_log.warn(":removeCalendarEvent " + ee.getMessage());
	                }
	                catch (PermissionException ee)
	                {
	                    M_log.warn(":removeCalendarEvent " + ee.getMessage());
	                }
	            }
	            
	            // remove the event if it exists
	            if (event != null)
	            {
	                try
	                {
	                    calendar.removeEvent(calendar.getEditEvent(event.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
	                    pEdit.removeProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
	                    pEdit.removeProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
	                }
	                catch (PermissionException ee)
	                {
	                    M_log.warn(":removeCalendarEvent not allowed to remove calendar event for assignment = " + aEdit.getTitle() + ". ");
	                }
	                catch (InUseException ee)
	                {
	                    M_log.warn(":removeCalendarEvent someone else is editing calendar event for assignment = " + aEdit.getTitle() + ". ");
	                }
	                catch (IdUnusedException ee)
	                {
	                    M_log.warn(":removeCalendarEvent calendar event are in use for assignment = " + aEdit.getTitle() + " and event =" + event.getId());
	                }
	            }
	        }
	    }
	}


	private AnnouncementChannel getAnnouncementChannel(String contextId)
	{
	    AnnouncementService aService = org.sakaiproject.announcement.cover.AnnouncementService.getInstance();
	    AnnouncementChannel channel = null;
        String channelId = m_serverConfigurationService.getString(m_announcementService.ANNOUNCEMENT_CHANNEL_PROPERTY, null);
	    if (channelId == null)
	    {
	        channelId = m_announcementService.channelReference(contextId, SiteService.MAIN_CONTAINER);
	        try
	        {
	            channel = aService.getAnnouncementChannel(channelId);
	        }
	        catch (IdUnusedException e)
	        {
	            M_log.warn("getAnnouncement:No announcement channel found");
	            channel = null;
	        }
	        catch (PermissionException e)
	        {
	            M_log.warn("getAnnouncement:Current user not authorized to deleted annc associated " +
	            		"with assignment. " + e.getMessage());
	            channel = null;
	        }
	    }
	    
	    return channel;
	}
	
	/**
	 * Will determine if there is an announcement associated
	 * with this assignment and removes it, if found.
	 * @param channel AnnouncementChannel
	 * @param aEdit AssignmentEdit
	 * @param pEdit ResourcePropertiesEdit
	 */
	private void removeAssociatedAnnouncementItem(AnnouncementChannel channel, AssignmentEdit aEdit, ResourcePropertiesEdit pEdit) 
	{
	    if (channel != null)
	    {
	        String openDateAnnounced = StringUtils.trimToNull(pEdit.getProperty("new_assignment_open_date_announced"));
	        String openDateAnnouncementId = StringUtils.trimToNull(pEdit.getProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
	        if (openDateAnnounced != null && openDateAnnouncementId != null)
	        {
	            try
	            {
	                channel.removeMessage(openDateAnnouncementId);
	            }
	            catch (PermissionException e)
	            {
	                M_log.warn(":removeAnnouncement " + e.getMessage());
	            }
	        }
	    }
	}



	/**
	 * Creates and adds a new AssignmentContent to the service.
	 * 
	 * @param context -
	 *        Describes the portlet context - generated with DefaultId.getChannel().
	 * @return AssignmentContent The new AssignmentContent object.
	 * @throws PermissionException
	 *         if current User does not have permission to do this.
	 */
	public AssignmentContentEdit addAssignmentContent(String context) throws PermissionException
	{
		M_log.debug(this + " ENTERING ADD ASSIGNMENT CONTENT");

		String contentId = null;
		boolean badId = false;

		do
		{
			badId = !Validator.checkResourceId(contentId);
			contentId = IdManager.createUuid();

			if (m_contentStorage.check(contentId)) badId = true;
		}
		while (badId);

		// security check
		if (!allowAddAssignmentContent(context))
		{
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_CONTENT, contentId);
		}

		AssignmentContentEdit content = m_contentStorage.put(contentId, context);

		
			M_log.debug(this + " LEAVING ADD ASSIGNMENT CONTENT : ID : " + content.getId());

		// event for tracking
		((BaseAssignmentContentEdit) content).setEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_CONTENT);

		return content;

	}// addAssignmentContent

	/**
	 * Add a new AssignmentContent to the directory, from a definition in XML. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param el
	 *        The XML DOM Element defining the AssignmentContent.
	 * @return A locked AssignmentContentEdit object (reserving the id).
	 * @exception IdInvalidException
	 *            if the AssignmentContent id is invalid.
	 * @exception IdUsedException
	 *            if the AssignmentContent id is already used.
	 * @exception PermissionException
	 *            if the current user does not have permission to add an AssignnmentContent.
	 */
	public AssignmentContentEdit mergeAssignmentContent(Element el) throws IdInvalidException, IdUsedException, PermissionException
	{
		// construct from the XML
		AssignmentContent contentFromXml = new BaseAssignmentContent(el);

		// check for a valid assignment name
		if (!Validator.checkResourceId(contentFromXml.getId())) throw new IdInvalidException(contentFromXml.getId());

		// check security (throws if not permitted)
		unlock(SECURE_ADD_ASSIGNMENT_CONTENT, contentFromXml.getReference());

		// reserve a content with this id from the info store - if it's in use, this will return null
		AssignmentContentEdit content = m_contentStorage.put(contentFromXml.getId(), contentFromXml.getContext());
		if (content == null)
		{
			throw new IdUsedException(contentFromXml.getId());
		}

		// transfer from the XML read content object to the AssignmentContentEdit
		((BaseAssignmentContentEdit) content).set(contentFromXml);

		((BaseAssignmentContentEdit) content).setEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_CONTENT);

		return content;
	}

	/**
	 * Creates and adds a new AssignmentContent to the service which is a copy of an existing AssignmentContent.
	 * 
	 * @param context -
	 *        From DefaultId.getChannel(RunData)
	 * @param contentReference -
	 *        The id of the AssignmentContent to be duplicated.
	 * @return AssignmentContentEdit The new AssignmentContentEdit object, or null if the original does not exist.
	 * @throws PermissionException
	 *         if current User does not have permission to do this.
	 */
	public AssignmentContentEdit addDuplicateAssignmentContent(String context, String contentReference) throws PermissionException,
			IdInvalidException, IdUnusedException
	{
		
			M_log.debug(this + " ENTERING ADD DUPLICATE ASSIGNMENT CONTENT : " + contentReference);

		AssignmentContentEdit retVal = null;
		AssignmentContent existingContent = null;
		List tempVector = null;
		Reference tempRef = null;
		Reference newRef = null;

		if (contentReference != null)
		{
			String contentId = contentId(contentReference);
			if (!m_contentStorage.check(contentId))
				throw new IdUnusedException(contentId);
			else
			{
				M_log.debug(this + " ADD DUPL. CONTENT : found match - will copy");

				existingContent = getAssignmentContent(contentReference);
				retVal = addAssignmentContent(context);
				retVal.setTitle(existingContent.getTitle() + " - " + rb.getString("assignment.copy"));
				retVal.setInstructions(existingContent.getInstructions());
				retVal.setHonorPledge(existingContent.getHonorPledge());
				retVal.setHideDueDate(existingContent.getHideDueDate());
				retVal.setTypeOfSubmission(existingContent.getTypeOfSubmission());
				retVal.setTypeOfGrade(existingContent.getTypeOfGrade());
				retVal.setMaxGradePoint(existingContent.getMaxGradePoint());
				retVal.setFactor(existingContent.getFactor());
				retVal.setGroupProject(existingContent.getGroupProject());
				retVal.setIndividuallyGraded(existingContent.individuallyGraded());
				retVal.setReleaseGrades(existingContent.releaseGrades());
				retVal.setAllowAttachments(existingContent.getAllowAttachments());
				// for ContentReview service
				retVal.setAllowReviewService(existingContent.getAllowReviewService());

				tempVector = existingContent.getAttachments();
				if (tempVector != null)
				{
					for (int z = 0; z < tempVector.size(); z++)
					{
						tempRef = (Reference) tempVector.get(z);
						if (tempRef != null)
						{
							String tempRefId = tempRef.getId();
							String tempRefCollectionId = m_contentHostingService.getContainingCollectionId(tempRefId);
							try
							{
								// get the original attachment display name
								ResourceProperties p = m_contentHostingService.getProperties(tempRefId);
								String displayName = p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
								// add another attachment instance
								String newItemId = m_contentHostingService.copyIntoFolder(tempRefId, tempRefCollectionId);
								ContentResourceEdit copy = m_contentHostingService.editResource(newItemId);
								// with the same display name
								ResourcePropertiesEdit pedit = copy.getPropertiesEdit();
								pedit.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
								m_contentHostingService.commitResource(copy, NotificationService.NOTI_NONE);
								newRef = m_entityManager.newReference(copy.getReference());
								retVal.addAttachment(newRef);
							}
							catch (Exception e)
							{
								
									M_log.warn(" LEAVING ADD DUPLICATE CONTENT : " + e.toString());
							}	
						}
					}
				}
				
				ResourcePropertiesEdit pEdit = (BaseResourcePropertiesEdit) retVal.getPropertiesEdit();
				pEdit.addAll(existingContent.getProperties());
				addLiveProperties(pEdit);
			}
		}

		
			M_log.debug(this + " LEAVING ADD DUPLICATE CONTENT WITH ID : " + retVal != null ? retVal.getId() : "");

		return retVal;
	}

	/**
	 * Access the AssignmentContent with the specified reference.
	 * 
	 * @param contentReference -
	 *        The reference of the AssignmentContent.
	 * @return The AssignmentContent corresponding to the reference, or null if it does not exist.
	 * @throws IdUnusedException
	 *         if there is no object with this reference.
	 * @throws PermissionException
	 *         if the current user is not allowed to access this.
	 */
	public AssignmentContent getAssignmentContent(String contentReference) throws IdUnusedException, PermissionException
	{
		M_log.debug(this + " GET CONTENT : ID : " + contentReference);

		// check security on the assignment content
		unlockCheck(SECURE_ACCESS_ASSIGNMENT_CONTENT, contentReference);
		
		AssignmentContent content = null;

		// if we have it in the cache, use it
		String contentId = contentId(contentReference);
		if (contentId != null) {
			content = m_contentStorage.get(contentId);
		}
		if (content == null) throw new IdUnusedException(contentId);

		M_log.debug(this + " GOT ASSIGNMENT CONTENT : ID : " + content.getId());

		// track event
		// EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT_CONTENT, content.getReference(), false));

		return content;

	}// getAssignmentContent

	/**
	 * Access all AssignmentContent objects - known to us (not from external providers).
	 * 
	 * @return A list of AssignmentContent objects.
	 */
	protected List getAssignmentContents(String context)
	{
		List contents = m_contentStorage.getAll(context);
		return contents;

	} // getAssignmentContents

	/**
	 * Get a locked AssignmentContent object for editing. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param id
	 *        The content id string.
	 * @return An AssignmentContentEdit object for editing.
	 * @exception IdUnusedException
	 *            if not found, or if not an AssignmentContentEdit object
	 * @exception PermissionException
	 *            if the current user does not have permission to edit this content.
	 * @exception InUseException
	 *            if the assignment is being edited by another user.
	 */
	public AssignmentContentEdit editAssignmentContent(String contentReference) throws IdUnusedException, PermissionException,
			InUseException
	{
		// check security (throws if not permitted)
		unlock(SECURE_UPDATE_ASSIGNMENT_CONTENT, contentReference);

		String contentId = contentId(contentReference);

		// check for existance
		if (!m_contentStorage.check(contentId))
		{
			throw new IdUnusedException(contentId);
		}

		// ignore the cache - get the AssignmentContent with a lock from the info store
		AssignmentContentEdit content = m_contentStorage.edit(contentId);
		if (content == null) throw new InUseException(contentId);

		((BaseAssignmentContentEdit) content).setEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_CONTENT);

		return content;

	} // editAssignmentContent

	/**
	 * Commit the changes made to an AssignmentContentEdit object, and release the lock.
	 * 
	 * @param content
	 *        The AssignmentContentEdit object to commit.
	 */
	public void commitEdit(AssignmentContentEdit content)
	{
		// check for closed edit
		if (!content.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn(" commitEdit(): closed AssignmentContentEdit " + e + " content id=" + content.getId());
			}
			return;
		}

		// update the properties
		addLiveUpdateProperties(content.getPropertiesEdit());

		// complete the edit
		m_contentStorage.commit(content);
				
		// track it
		EventTrackingService.post(EventTrackingService.newEvent(((BaseAssignmentContentEdit) content).getEvent(), content
				.getReference(), true));

		// close the edit object
		((BaseAssignmentContentEdit) content).closeEdit();

	} // commitEdit(AssignmentContent)

	/**
	 * Cancel the changes made to a AssignmentContentEdit object, and release the lock.
	 * 
	 * @param content
	 *        The AssignmentContentEdit object to commit.
	 */
	public void cancelEdit(AssignmentContentEdit content)
	{
		// check for closed edit
		if (!content.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn(" cancelEdit(): closed AssignmentContentEdit " + e.getMessage() + " assignment content id=" + content.getId());
			}
			return;
		}

		// release the edit lock
		m_contentStorage.cancel(content);

		// close the edit object
		((BaseAssignmentContentEdit) content).closeEdit();

	} // cancelEdit(Content)

	/**
	 * Removes an AssignmentContent
	 * 
	 * @param content -
	 *        the AssignmentContent to remove.
	 * @throws an
	 *         AssignmentContentNotEmptyException if this content still has related Assignments.
	 * @throws PermissionException
	 *         if current User does not have permission to do this.
	 */
	public void removeAssignmentContent(AssignmentContentEdit content) throws AssignmentContentNotEmptyException,
			PermissionException
	{
		if (content != null)
		{
			if (!content.isActiveEdit())
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					M_log.warn(" removeAssignmentContent(): closed AssignmentContentEdit " + e.getMessage() + " assignment content id=" + content.getId());
				}
				return;
			}

			// CHECK SECURITY
			unlock(SECURE_REMOVE_ASSIGNMENT_CONTENT, content.getReference());

			// complete the edit
			m_contentStorage.remove(content);

			// track event
			EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT_CONTENT, content.getReference(),
					true));

			// close the edit object
			((BaseAssignmentContentEdit) content).closeEdit();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AssignmentSubmissionEdit addSubmission(String context, String assignmentId, String submitterId) throws PermissionException
	{
		M_log.debug(this + " ENTERING ADD SUBMISSION");

		String submissionId = null;
		boolean badId = false;

		do
		{
			badId = !Validator.checkResourceId(submissionId);
			submissionId = IdManager.createUuid();

			if (m_submissionStorage.check(submissionId)) badId = true;
		}
		while (badId);

		String key = submissionReference(context, submissionId, assignmentId);

		M_log.debug(this + " ADD SUBMISSION : SUB REF : " + key);
		
		Assignment assignment = null;
		
		try
		{
			assignment = getAssignment(assignmentId);
		}
		catch(IdUnusedException iue)
		{
			// A bit terminal, this.
			M_log.error("addSubmission called with unknown assignmentId: " + assignmentId);
		}

		// SAK-21525
		if(!unlockCheckWithGroups(SECURE_ADD_ASSIGNMENT_SUBMISSION, key,assignment))
		{
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION, key);
		}

		M_log.debug(this + " ADD SUBMISSION : UNLOCKED");

		// storage
                M_log.debug(this + " SUBMITTER ID " + submitterId);
                
		AssignmentSubmissionEdit submission = m_submissionStorage.put(submissionId, assignmentId, submitterId, null, null, null);

		if (submission != null)
		{
			submission.setContext(context);
			// event for tracking
			((BaseAssignmentSubmissionEdit) submission).setEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_SUBMISSION);
			M_log.debug(this + " LEAVING ADD SUBMISSION : REF : " + submission.getReference());
		}
		else
		{
			M_log.warn(this + " ADD SUBMISSION: cannot add submission object with submission id=" + submissionId + ", assignment id=" + assignmentId + ", and submitter id=" + submitterId);
		}
		return submission;
	}

	/**
	 * Add a new AssignmentSubmission to the directory, from a definition in XML. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param el
	 *        The XML DOM Element defining the submission.
	 * @return A locked AssignmentSubmissionEdit object (reserving the id).
	 * @exception IdInvalidException
	 *            if the submission id is invalid.
	 * @exception IdUsedException
	 *            if the submission id is already used.
	 * @exception PermissionException
	 *            if the current user does not have permission to add a submission.
	 */
	public AssignmentSubmissionEdit mergeSubmission(Element el) throws IdInvalidException, IdUsedException, PermissionException
	{
		// construct from the XML
		BaseAssignmentSubmission submissionFromXml = new BaseAssignmentSubmission(el);

		// check for a valid submission name
		if (!Validator.checkResourceId(submissionFromXml.getId())) throw new IdInvalidException(submissionFromXml.getId());

		// check security (throws if not permitted)
		unlock(SECURE_ADD_ASSIGNMENT_SUBMISSION, submissionFromXml.getReference());

		// reserve a submission with this id from the info store - if it's in use, this will return null
		AssignmentSubmissionEdit submission = m_submissionStorage.put(	submissionFromXml.getId(), 
																		submissionFromXml.getAssignmentId(),
																		submissionFromXml.getSubmitterIdString(),
																		(submissionFromXml.getTimeSubmitted() != null)?String.valueOf(submissionFromXml.getTimeSubmitted().getTime()):null,
																		Boolean.valueOf(submissionFromXml.getSubmitted()).toString(),
																		Boolean.valueOf(submissionFromXml.getGraded()).toString());
		if (submission == null)
		{
			throw new IdUsedException(submissionFromXml.getId());
		}

		// transfer from the XML read submission object to the SubmissionEdit
		((BaseAssignmentSubmissionEdit) submission).set(submissionFromXml);

		((BaseAssignmentSubmissionEdit) submission).setEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_SUBMISSION);

		return submission;
	}

	/**
	 * Get a locked AssignmentSubmission object for editing. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param submissionrReference -
	 *        the reference for the submission.
	 * @return An AssignmentSubmissionEdit object for editing.
	 * @exception IdUnusedException
	 *            if not found, or if not an AssignmentSubmissionEdit object
	 * @exception PermissionException
	 *            if the current user does not have permission to edit this submission.
	 * @exception InUseException
	 *            if the assignment is being edited by another user.
	 */
	public AssignmentSubmissionEdit editSubmission(String submissionReference) throws IdUnusedException, PermissionException,
			InUseException
	{
		String submissionId = submissionId(submissionReference);
		// ignore the cache - get the AssignmentSubmission with a lock from the info store
		AssignmentSubmissionEdit submission = m_submissionStorage.edit(submissionId);
		if (submission == null) throw new InUseException(submissionId);
		
		// pass if with grade or update assignment right
		if (!unlockCheck(SECURE_GRADE_ASSIGNMENT_SUBMISSION, submissionReference) && !unlockCheck(SECURE_UPDATE_ASSIGNMENT, submissionReference))
		{
			boolean notAllowed = true;
			// normal user(not a grader) can only edit his/her own submission
			User currentUser = UserDirectoryService.getCurrentUser(); 
			if (unlockCheck(SECURE_UPDATE_ASSIGNMENT_SUBMISSION, submissionReference))
			{
                            Assignment a = submission.getAssignment();
                            if (a.isGroup()) {
                                String context = a.getContext();
                                Site st = SiteService.getSite(context);
                                try {
                                    notAllowed = 
                                        st.getGroup(submission.getSubmitterId()).getMember(currentUser.getId()) == null;
                                } catch (Throwable _sss) { }
                                    
                            } else {
                                if ( submission.getSubmitterId() != null && submission.getSubmitterId().equals(currentUser.getId()) ) {
                                        // is editing one's own submission
					// then test against extra criteria depend on the status of submission
					try
					{
						if (canSubmit(a.getContext(), a))
						{
							notAllowed = false;
						}
					}
					catch (Exception e)
					{
						M_log.warn(" editSubmission(): cannot get assignment for submission " + submissionReference + e.getMessage());
					}
                                }
                            }
			}
			if (notAllowed)
			{
				// throw PermissionException
				throw new PermissionException(currentUser.getId(), SECURE_UPDATE_ASSIGNMENT, submissionReference);
			}
			
		}

		// check for existance
		if (!m_submissionStorage.check(submissionId))
		{
			throw new IdUnusedException(submissionId);
		}

		((BaseAssignmentSubmissionEdit) submission).setEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_SUBMISSION);

		return submission;

	} // editSubmission

	/**
	 * Commit the changes made to an AssignmentSubmissionEdit object, and release the lock.
	 * 
	 * @param submission
	 *        The AssignmentSubmissionEdit object to commit.
	 */
	public void commitEdit(AssignmentSubmissionEdit submission)
	{
		String submissionRef = submission.getReference();
		
		// check for closed edit
		if (!submission.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn(" commitEdit(): closed AssignmentSubmissionEdit assignment submission id=" + submission.getId() + e.getMessage());
			}
			return;
		}

		// update the properties
		addLiveUpdateProperties(submission.getPropertiesEdit());

		submission.setTimeLastModified(TimeService.newTime());

		// complete the edit
		m_submissionStorage.commit(submission);
		
		// close the edit object
		((BaseAssignmentSubmissionEdit) submission).closeEdit();

		try
		{
			AssignmentSubmission s = getSubmission(submissionRef);
			
			Assignment a = s.getAssignment();
			
			Time returnedTime = s.getTimeReturned();
			Time submittedTime = s.getTimeSubmitted();
			String resubmitNumber = s.getProperties().getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
			// track it
			if (!s.getSubmitted())
			{
				// saving a submission
				EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_SAVE_ASSIGNMENT_SUBMISSION, submissionRef, true));
			}
			else if (returnedTime == null && !s.getReturned() && (submittedTime == null /*grading non-submissions*/
																|| (submittedTime != null && (s.getTimeLastModified().getTime() - submittedTime.getTime()) > 1000*60 /*make sure the last modified time is at least one minute after the submit time*/)))
			{
				if (StringUtils.trimToNull(s.getSubmittedText()) == null && s.getSubmittedAttachments().isEmpty()
					&& StringUtils.trimToNull(s.getGrade()) == null && StringUtils.trimToNull(s.getFeedbackText()) == null && StringUtils.trimToNull(s.getFeedbackComment()) == null && s.getFeedbackAttachments().isEmpty() )
				{
					// auto add submission for those not submitted
					//EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_SUBMISSION, submissionRef, true));
				}
				else
				{
					// graded and saved before releasing it
                    Event event = EventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, submissionRef, true);
                    EventTrackingService.post(event);
                    LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
                            .get("org.sakaiproject.event.api.LearningResourceStoreService");
                    if (null != lrss && StringUtils.isNotEmpty(s.getGrade())) {
                        for (User user : s.getSubmitters()) {
                            lrss.registerStatement(getStatementForAssignmentGraded(lrss.getEventActor(event), event, a, s, user), "assignment");
                        }
                    }
				}
			}
			else if (returnedTime != null && s.getGraded() && (submittedTime == null/*returning non-submissions*/ 
											|| (submittedTime != null && returnedTime.after(submittedTime))/*returning normal submissions*/ 
											|| (submittedTime != null && submittedTime.after(returnedTime) && s.getTimeLastModified().after(submittedTime))/*grading the resubmitted assignment*/))
			{
				// releasing a submitted assignment or releasing grade to an unsubmitted assignment
                Event event = EventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, submissionRef, true);
                EventTrackingService.post(event);
                LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
                        .get("org.sakaiproject.event.api.LearningResourceStoreService");
                if (null != lrss && StringUtils.isNotEmpty(s.getGrade())) {
                    for (User user : s.getSubmitters()) {
                        lrss.registerStatement(getStatementForAssignmentGraded(lrss.getEventActor(event), event, a, s, user), "assignment");
                    }
                }
				
				// if this is releasing grade, depending on the release grade notification setting, send email notification to student
				sendGradeReleaseNotification(s.getGradeReleased(), a.getProperties().getProperty(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE), s.getSubmitters(), s);
				if(resubmitNumber!=null)
					sendGradeReleaseNotification(s.getGradeReleased(), a.getProperties().getProperty(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE), s.getSubmitters(), s);
			}
			else if (submittedTime == null) /*grading non-submission*/
			{
				// releasing a submitted assignment or releasing grade to an unsubmitted assignment
                Event event = EventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, submissionRef, true);
                EventTrackingService.post(event);
                LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
                        .get("org.sakaiproject.event.api.LearningResourceStoreService");
                if (null != lrss) {
                    for (User user : s.getSubmitters()) {
                        lrss.registerStatement(getStatementForUnsubmittedAssignmentGraded(lrss.getEventActor(event), event, a, s, user),
                                "sakai.assignment");
                    }
                }
			}
			else
			{
				// submitting a submission
				EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_SUBMIT_ASSIGNMENT_SUBMISSION, submissionRef, true));
			
				// only doing the notification for real online submissions
				if (a.getContent().getTypeOfSubmission() != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
				{
					// instructor notification
					notificationToInstructors(s, a);
					
					// student notification, whether the student gets email notification once he submits an assignment
					notificationToStudent(s);
				}
			}
				
			
		}
		catch (IdUnusedException e)
		{
			M_log.error(" commitEdit(), submissionId=" + submissionRef, e);
		}
		catch (PermissionException e)
		{
			M_log.error(" commitEdit(), submissionId=" + submissionRef, e);
		}

	} // commitEdit(Submission)
	
	protected void sendGradeReleaseNotification(boolean released, String notificationSetting, User[] allSubmitters, AssignmentSubmission s)
	{
		if (allSubmitters == null) return;
		
		// SAK-19916 need to filter submitters against list of valid users still in site
		Set<User> filteredSubmitters = new HashSet<User>();
		try {
			String siteId = s.getAssignment().getContext();
			Set<String> siteUsers = SiteService.getSite(siteId).getUsers();

			for (int x = 0; x < allSubmitters.length; x++)
			{
				User u = (User) allSubmitters[x];
				String userId = u.getId();
				if (siteUsers.contains(userId)) {
					filteredSubmitters.add(u);
				}
			}
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		User[] submitters = new User[filteredSubmitters.size()];
		filteredSubmitters.toArray(submitters);
		
		if (released && notificationSetting != null && notificationSetting.equals(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_EACH))
		{
			// send email to every submitters
			if (submitters != null)
			{
				// send the message immidiately
				EmailService.sendToUsers(new ArrayList(Arrays.asList(submitters)), getHeaders(null, "releasegrade"),  getNotificationMessage(s, "releasegrade"));
			}
		}
		if (notificationSetting != null && notificationSetting.equals(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_EACH)){
			// send email to every submitters
			if (submitters != null){
				// send the message immidiately
				EmailService.sendToUsers(new ArrayList(Arrays.asList(submitters)), getHeaders(null, "releaseresumbission"),  getNotificationMessage(s, "releaseresumbission"));
			}
		}
	}
	

	/**
	 * send notification to instructor type of users if necessary
	 * @param s
	 * @param a
	 */
	private void notificationToInstructors(AssignmentSubmission s, Assignment a) 
	{
		String notiOption = a.getProperties().getProperty(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE);
		if (notiOption != null && !notiOption.equals(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE))
		{
			// need to send notification email
			String context = s.getContext();
			
			// compare the list of users with the receive.notifications and list of users who can actually grade this assignment
			List receivers = allowReceiveSubmissionNotificationUsers(context);
			List allowGradeAssignmentUsers = allowGradeAssignmentUsers(a.getReference());
			receivers.retainAll(allowGradeAssignmentUsers);
			
			String messageBody = getNotificationMessage(s, "submission");
			
			if (notiOption.equals(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH))
			{
				// send the message immediately
				EmailService.sendToUsers(receivers, getHeaders(null, "submission"), messageBody);
			}
			else if (notiOption.equals(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST))
			{
				// just send plain/text version for now
				String digestMsgBody = getPlainTextNotificationMessage(s, "submission");
				
				// digest the message to each user
				for (Iterator iReceivers = receivers.iterator(); iReceivers.hasNext();)
				{
					User user = (User) iReceivers.next();
					DigestService.digest(user.getId(), getSubject("submission"), digestMsgBody);
				}
			}
		}
	}
	
	/**
	 * get only the plain text of notification message
	 * @param s
	 * @return
	 */
	protected String getPlainTextNotificationMessage(AssignmentSubmission s, String submissionOrReleaseGrade)
	{
		StringBuilder message = new StringBuilder();
		message.append(plainTextContent(s, submissionOrReleaseGrade));
		return message.toString();
	}

	/**
	 * send notification to student/students if necessary
	 * @param s
	 */
	private void notificationToStudent(AssignmentSubmission s) 
	{
		if (m_serverConfigurationService.getBoolean("assignment.submission.confirmation.email", true))
                {
			//send notification
			User[] users = s.getSubmitters();
                        List receivers = new ArrayList();
                        for (int i=0; users != null && i<users.length; i++){
                            if (StringUtils.trimToNull(users[i].getEmail()) != null){
                                receivers.add(users[i]);
                            }
                        }
                        EmailService.sendToUsers(receivers, getHeaders(null, "submission"), getNotificationMessage(s, "submission"));
		}
	}
	
	protected List<String> getHeaders(String receiverEmail, String submissionOrReleaseGrade)
	{
		List<String> rv = new ArrayList<String>();
		
		rv.add("MIME-Version: 1.0");
		rv.add("Content-Type: multipart/alternative; boundary=\""+MULTIPART_BOUNDARY+"\"");
		// set the subject
		rv.add(getSubject(submissionOrReleaseGrade));

		// from
		rv.add(getFrom());
		
		// to
		if (StringUtils.trimToNull(receiverEmail) != null)
		{
			rv.add("To: " + receiverEmail);
		}
		
		return rv;
	}
	
	protected List<String> getReleaseGradeHeaders(String receiverEmail)
	{
		List<String> rv = new ArrayList<String>();
		
		rv.add("MIME-Version: 1.0");
		rv.add("Content-Type: multipart/alternative; boundary=\""+MULTIPART_BOUNDARY+"\"");
		// set the subject
		rv.add(getSubject("releasegrade"));

		// from
		rv.add(getFrom());
		
		// to
		if (StringUtils.trimToNull(receiverEmail) != null)
		{
			rv.add("To: " + receiverEmail);
		}
		
		return rv;
	}
	
	protected String getSubject(String submissionOrReleaseGrade)
	{
		String subject = "";
		if("submission".equals(submissionOrReleaseGrade))
			subject = rb.getString("noti.subject.content");
		else if ("releasegrade".equals(submissionOrReleaseGrade))
			subject = rb.getString("noti.releasegrade.subject.content");
		else
			subject = rb.getString("noti.releaseresubmission.subject.content");
		return "Subject: " + subject ;
	}
	
	protected String getFrom()
	{
		return "From: " + "\"" + m_serverConfigurationService.getString("ui.service", "Sakai") + "\" <"+ m_serverConfigurationService.getString("setup.request","no-reply@"+ m_serverConfigurationService.getServerName()) + ">";
	}
	
	private final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
	private final String BOUNDARY_LINE = "\n\n--"+MULTIPART_BOUNDARY+"\n";
	private final String TERMINATION_LINE = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";
	private final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
	
	/**
	 * Get the message for the email.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the message for the email.
	 */
	protected String getNotificationMessage(AssignmentSubmission s, String submissionOrReleaseGrade)
	{	
		StringBuilder message = new StringBuilder();
		message.append(MIME_ADVISORY);
		message.append(BOUNDARY_LINE);
		message.append(plainTextHeaders());
		message.append(plainTextContent(s, submissionOrReleaseGrade));
		message.append(FormattedText.convertFormattedTextToPlaintext(htmlContentAttachments(s)));
		message.append(BOUNDARY_LINE);
		message.append(htmlHeaders());
		message.append(htmlPreamble(submissionOrReleaseGrade));
		if("submission".equals(submissionOrReleaseGrade))
			message.append(htmlContent(s));
		else if ("releasegrade".equals(submissionOrReleaseGrade))
			message.append(htmlContentReleaseGrade(s));
		else
			message.append(htmlContentReleaseResubmission(s));
		message.append(htmlContentAttachments(s));
		message.append(htmlEnd());
		message.append(TERMINATION_LINE);
		return message.toString();
	}
	
	protected String plainTextHeaders() {
		return "Content-Type: text/plain\n\n";
	}
	
	protected String plainTextContent(AssignmentSubmission s, String submissionOrReleaseGrade) {
		if("submission".equals(submissionOrReleaseGrade))
			return FormattedText.convertFormattedTextToPlaintext(htmlContent(s));
		else if ("releasegrade".equals(submissionOrReleaseGrade))
			return FormattedText.convertFormattedTextToPlaintext(htmlContentReleaseGrade(s));
		else
			return FormattedText.convertFormattedTextToPlaintext(htmlContentReleaseResubmission(s));
	}
	
	protected String htmlHeaders() {
		return "Content-Type: text/html\n\n";
	}
	
	protected String htmlPreamble(String submissionOrReleaseGrade) {
		StringBuilder buf = new StringBuilder();
		buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
		buf.append("    \"http://www.w3.org/TR/html4/loose.dtd\">\n");
		buf.append("<html>\n");
		buf.append("  <head><title>");
		buf.append(getSubject(submissionOrReleaseGrade));
		buf.append("</title></head>\n");
		buf.append("  <body>\n");
		return buf.toString();
	}
	
	protected String htmlEnd() {
		return "\n  </body>\n</html>\n";
	}

	private String htmlContent(AssignmentSubmission s) 
	{	
		Assignment a = s.getAssignment();
		String context = s.getContext();
		boolean isAnon = assignmentUsesAnonymousGrading( a );
		
		String siteTitle = "";
		String siteUrl = "";
		try
		{
			Site site = SiteService.getSite(context);
			siteTitle = site.getTitle();
			siteUrl = site.getUrl();
		}
		catch (Exception ee)
		{
			M_log.warn(" htmlContent(), site id =" + context + " " + ee.getMessage());
		}
		
		StringBuilder buffer = new StringBuilder();
		// site title and id
		buffer.append(rb.getString("noti.site.title") + " " + siteTitle + newline);
		buffer.append(rb.getString("noti.site.url") + " <a href=\""+ siteUrl+ "\">" + siteUrl + "</a>"+ newline);
		// assignment title and due date
		buffer.append(rb.getString("assignment.title") + " " + a.getTitle()+newline);
		buffer.append(rb.getString("noti.assignment.duedate") + " " + a.getDueTime().toStringLocalFull()+newline + newline);
		// submitter name and id
		User[] submitters = s.getSubmitters();
		String submitterNames = "";
		String submitterIds = "";
		for (int i = 0; i<submitters.length; i++)
		{
			User u = (User) submitters[i];
			if (i>0)
			{
				submitterNames = submitterNames.concat("; ");
				submitterIds = submitterIds.concat("; ");
			}
			submitterNames = submitterNames.concat( ( isAnon ? s.getAnonymousSubmissionId() : u.getDisplayName() ) );
			submitterIds = submitterIds.concat(u.getDisplayId());
		}
		buffer.append(rb.getString("noti.student") + " " + submitterNames);
		if (submitterIds.length() != 0 && !isAnon)
		{
			buffer.append("( " + submitterIds + " )");
		}
		buffer.append(newline + newline);
		
		// submit time
		buffer.append(rb.getString("submission.id") + " " + s.getId() + newline);
		
		// submit time 
		buffer.append(rb.getString("noti.submit.time") + " " + s.getTimeSubmitted().toStringLocalFull() + newline + newline);
		
		// submit text
		String text = StringUtils.trimToNull(s.getSubmittedText());
		if ( text != null)
		{
			buffer.append(rb.getString("gen.submittedtext") + newline + newline + Validator.escapeHtmlFormattedText(text) + newline + newline);
		}
		
		// attachment if any
		List attachments = s.getSubmittedAttachments();
		if (attachments != null && attachments.size() >0)
		{
			if (a.getContent().getTypeOfSubmission() == Assignment.SINGLE_ATTACHMENT_SUBMISSION) 
			{
				buffer.append(rb.getString("gen.att.single"));
			} 
			else 
			{
				buffer.append(rb.getString("gen.att"));
			}
			buffer.append(newline).append(newline);
			
			for (int j = 0; j<attachments.size(); j++)
			{
				Reference r = (Reference) attachments.get(j);
				buffer.append(r.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME) + " (" + r.getProperties().getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH)+ ")\n");
				//if this is a archive (zip etc) append the list of files in it
				if (isArchiveFile(r)) {
					buffer.append(getArchiveManifest(r));
				}
			}
		}
		
		return buffer.toString();
	}
	
	/**
	 * get a list of the files in the archive
	 * @param r
	 * @return
	 */
	private Object getArchiveManifest(Reference r) {
		String extension = getFileExtension(r);
		StringBuilder builder = new StringBuilder();
		if (".zip".equals(extension)) {
			ZipContentUtil zipUtil = new ZipContentUtil();
			Map<String, Long> manifest = zipUtil.getZipManifest(r);
			Set<Entry<String, Long>> set = manifest.entrySet();
			Iterator<Entry<String, Long>> it = set.iterator();
			while (it.hasNext()) {
				Entry<String, Long> entry = it.next();
				builder.append(entry.getKey() + " (" + formatFileSize(entry.getValue()) + ")" + newline);
			}
		}
		
		return builder.toString();
	}

	private String formatFileSize(Long bytes) {
		long len = bytes;
		String[] byteString = { "KB", "KB", "MB", "GB" };
		int count = 0;
		long newLen = 0;
		long lenBytesExtra = len;

		while (len > 1024)
		{
			newLen = len / 1024;
			lenBytesExtra = len - (newLen * 1024);
			len = newLen;
			count++;
		}

		if ((lenBytesExtra >= 512) || ((lenBytesExtra > 0) && (newLen == 0)))
		{
			newLen++;
		}

		return Long.toString(newLen) + " " + byteString[count];
	}




	/**
	 * is this an archive type for which we can get a manifest
	 * @param r
	 * @return
	 */
	private boolean isArchiveFile(Reference r) {
		String extension = getFileExtension(r);
		if (".zip".equals(extension)) {
			return true;
		}
		return false;
	}



	private String getFileExtension(Reference r) {
		ResourceProperties resourceProperties = r.getProperties();
		String fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
		if (fileName.indexOf(".")>0) {
			String extension = fileName.substring(fileName.lastIndexOf("."));
			return extension;
		}
		return null;
	}



	private String htmlContentReleaseGrade(AssignmentSubmission s) 
	{
		String newline = "<br />\n";
		
		Assignment a = s.getAssignment();
		
		String context = s.getContext();
		
		String siteTitle = "";
		String siteUrl = "";
		try
		{
			Site site = SiteService.getSite(context);
			siteTitle = site.getTitle();
			siteUrl = site.getUrl();
		}
		catch (Exception ee)
		{
			M_log.warn(" htmlContentReleaseGrade(), site id =" + context + " " + ee.getMessage());
		}
		
		StringBuilder buffer = new StringBuilder();
		// site title and id
		buffer.append(rb.getString("noti.site.title") + " " + siteTitle + newline);
		buffer.append(rb.getString("noti.site.url") + " <a href=\""+ siteUrl+ "\">" + siteUrl + "</a>"+ newline);
		// notification text
		String linkToToolInSite = "<a href=\"" + developerHelperService.getToolViewURL( "sakai.assignment.grades", null, null, null ) + "\">" + siteTitle + "</a>";
		buffer.append(rb.getFormattedMessage("noti.releasegrade.text", new String[]{a.getTitle(), linkToToolInSite}));
		
		return buffer.toString();
	}
	private String htmlContentReleaseResubmission(AssignmentSubmission s){
		String newline = "<br />\n";
		Assignment a = s.getAssignment();
		String context = s.getContext();
			
		String siteTitle = "";
		String siteUrl = "";
		try {
			Site site = SiteService.getSite(context);
			siteTitle = site.getTitle();
			siteUrl = site.getUrl();
		}catch (Exception ee){
			M_log.warn(this + " htmlContentReleaseResubmission(), site id =" + context + " " + ee.getMessage());
		}
	
		StringBuilder buffer = new StringBuilder();
		// site title and id
		buffer.append(rb.getString("noti.site.title") + " " + siteTitle + newline);
		buffer.append(rb.getString("noti.site.url") + " <a href=\""+ siteUrl+ "\">" + siteUrl + "</a>"+ newline);
		// notification text
		//Get the actual person that submitted, for a group submission just get the first person from that group (This is why the array is used)
		String userId = null;
		if (s.getSubmitterIds() != null && s.getSubmitterIds().size() > 0) {
		    userId = s.getSubmitterIds().get(0);
		}

		String linkToToolInSite = "<a href=\"" + developerHelperService.getToolViewURL( "sakai.assignment.grades", null, null, null ) + "\">" + siteTitle + "</a>";
		if (canSubmit(context,a,userId)) {
		    buffer.append(rb.getFormattedMessage("noti.releaseresubmission.text", new String[]{a.getTitle(), linkToToolInSite}));
		}
		else {
		    buffer.append(rb.getFormattedMessage("noti.releaseresubmission.noresubmit.text", new String[]{a.getTitle(), linkToToolInSite}));
		}
	 		
	 	return buffer.toString();
	}
	
	private String htmlContentAttachments(AssignmentSubmission s){
		StringBuffer body = new StringBuffer();
		String newline = "<br />\n";
		
		if (s.getFeedbackAttachments() != null && s.getFeedbackAttachments().size() > 0) {
			body.append(newline).append(newline);
			if (s.getAssignment().getContent().getTypeOfSubmission() == Assignment.SINGLE_ATTACHMENT_SUBMISSION) 
			{
				body.append(rb.getString("gen.att.single"));
			} 
			else 
			{
				body.append(rb.getString("gen.att"));
			}
			body.append(newline);
			
			for (Reference attachment : (List<Reference>)s.getFeedbackAttachments()) {
				String attachmentName = attachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				String attachmentSize = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH);
				body.append("<a href=\"" + attachment.getUrl() + "\">" + attachmentName + " (" + attachmentSize + ")" + "</a>");   
				body.append(newline);
			}
		}
		return body.toString();
	}
	
	
	/**
	 * Cancel the changes made to a AssignmentSubmissionEdit object, and release the lock.
	 * 
	 * @param submission
	 *        The AssignmentSubmissionEdit object to commit.
	 */
	public void cancelEdit(AssignmentSubmissionEdit submission)
	{
		// check for closed edit
		if (!submission.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn(" cancelEdit(): closed AssignmentSubmissionEdit assignment submission id=" + submission.getId() + " " + e.getMessage());
			}
			return;
		}

		// release the edit lock
		m_submissionStorage.cancel(submission);

		// close the edit object
		((BaseAssignmentSubmissionEdit) submission).closeEdit();

	} // cancelEdit(Submission)

	/**
	 * Removes an AssignmentSubmission and all references to it
	 * 
	 * @param submission -
	 *        the AssignmentSubmission to remove.
	 * @throws PermissionException
	 *         if current User does not have permission to do this.
	 */
	public void removeSubmission(AssignmentSubmissionEdit submission) throws PermissionException
	{
		if (submission != null)
		{
			if (!submission.isActiveEdit())
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					M_log.warn(" removeSubmission(): closed AssignmentSubmissionEdit id=" + submission.getId()  + " "  + e.getMessage());
				}
				return;
			}

			// check security
			unlock(SECURE_REMOVE_ASSIGNMENT_SUBMISSION, submission.getReference());

			// complete the edit
			m_submissionStorage.remove(submission);

			// track event
			EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT_SUBMISSION, submission.getReference(),
					true));

			// close the edit object
			((BaseAssignmentSubmissionEdit) submission).closeEdit();

			// remove any realm defined for this resource
			try
			{
				authzGroupService.removeAuthzGroup(authzGroupService.getAuthzGroup(submission.getReference()));
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(" removeSubmission: removing realm for : " + submission.getReference() + " : " + e.getMessage());
			}
			catch (GroupNotDefinedException e)
			{
				M_log.warn(" removeSubmission: cannot find group for submission " + submission.getReference() + " : " + e.getMessage());
			}
		}
	}// removeSubmission

	/**
	 *@inheritDoc
	 */
	public int getSubmissionsSize(String context)
	{
		int size = 0;
		
		List submissions = getSubmissions(context);
		if (submissions != null)
		{
			size = submissions.size();
		}
		return size;
	}
	
	/**
	 * Access all AssignmentSubmission objects - known to us (not from external providers).
	 * 
	 * @return A list of AssignmentSubmission objects.
	 */
	protected List getSubmissions(String context)
	{
		List<AssignmentSubmission> submissions = m_submissionStorage.getAll(context);

		//get all the review scores
		if (contentReviewService != null) {
			try {
				List<ContentReviewItem> reports = contentReviewService.getReportList(null, context);
				if (reports != null && reports.size() > 0) {
					updateSubmissionList(submissions, reports);
				}
			} catch (QueueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SubmissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return submissions;

	} // getAssignmentSubmissions
	
	private void updateSubmissionList(List<AssignmentSubmission> submissions, List<ContentReviewItem> reports) {
		//lets build a map  to avoid multiple searches through the list of reports
		Map<String, ContentReviewItem> reportsMap = new HashMap<String, ContentReviewItem> ();
		for (int i = 0; i < reports.size(); i++) {
			ContentReviewItem item = reports.get(i);
			reportsMap.put(item.getUserId(), item);
		}
		
		for (int i = 0; i < submissions.size(); i++) {
			AssignmentSubmission sub = submissions.get(i);
			String submitterid = sub.getSubmitterId();
			if (reportsMap.containsKey(submitterid)) {
				ContentReviewItem report = reportsMap.get(submitterid);
				AssignmentSubmissionEdit edit;
				try {
					edit = this.editSubmission(sub.getReference());
					edit.setReviewScore(report.getReviewScore());
					edit.setReviewIconUrl(report.getIconUrl());
					edit.setSubmitterId(sub.getSubmitterId());
                    edit.setReviewError(report.getLastError());
					this.commitEdit(edit);
				} catch (IdUnusedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PermissionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InUseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		
		
	}
	

	/**
	 * Access list of all AssignmentContents created by the User.
	 * 
	 * @param owner -
	 *        The User who's AssignmentContents are requested.
	 * @return Iterator over all AssignmentContents owned by this User.
	 */
	public Iterator getAssignmentContents(User owner)
	{
		List retVal = new ArrayList();
		AssignmentContent aContent = null;
		List allContents = getAssignmentContents(owner.getId());

		for (int x = 0; x < allContents.size(); x++)
		{
			aContent = (AssignmentContent) allContents.get(x);
			if (aContent.getCreator().equals(owner.getId()))
			{
				retVal.add(aContent);
			}
		}

		if (retVal.isEmpty())
			return new EmptyIterator();
		else
			return retVal.iterator();

	}// getAssignmentContents(User)

	/**
	 * Access all the Assignments which have the specified AssignmentContent.
	 * 
	 * @param content -
	 *        The particular AssignmentContent.
	 * @return Iterator over all the Assignments with the specified AssignmentContent.
	 */
	public Iterator getAssignments(AssignmentContent content)
	{
		List retVal = new ArrayList();
		String contentReference = null;
		String tempContentReference = null;

		if (content != null)
		{
			contentReference = content.getReference();
			List allAssignments = getAssignments(content.getContext());
			Assignment tempAssignment = null;

			for (int y = 0; y < allAssignments.size(); y++)
			{
				tempAssignment = (Assignment) allAssignments.get(y);
				tempContentReference = tempAssignment.getContentReference();
				if (tempContentReference != null)
				{
					if (tempContentReference.equals(contentReference))
					{
						retVal.add(tempAssignment);
					}
				}
			}
		}

		if (retVal.isEmpty())
			return new EmptyIterator();
		else
			return retVal.iterator();
	}

	/**
	 * Access all the Assignemnts associated with the context
	 * 
	 * @param context -
	 *        Describes the portlet context - generated with DefaultId.getChannel().
	 * @return Iterator over all the Assignments associated with the context and the user.
	 */
	public Iterator getAssignmentsForContext(String context)
	{
		M_log.debug(this + " GET ASSIGNMENTS FOR CONTEXT : CONTEXT : " + context);
		
		return assignmentsForContextAndUser(context, null);

	}
	
	/**
	 * Access all the Assignemnts associated with the context and the user
	 * 
	 * @param context -
	 *        Describes the portlet context - generated with DefaultId.getChannel()
	 * @return Iterator over all the Assignments associated with the context and the user
	 */
	public Iterator getAssignmentsForContext(String context, String userId)
	{
		M_log.debug(this + " GET ASSIGNMENTS FOR CONTEXT : CONTEXT : " + context);
		
		return assignmentsForContextAndUser(context, userId);

	}

	/**
	 * @inheritDoc
	 */
	public Map<Assignment, List<String>> getSubmittableAssignmentsForContext(String context)
	{
		Map<Assignment, List<String>> submittable = new HashMap<Assignment, List<String>>();
		if (!allowGetAssignment(context))
		{
			// no permission to read assignment in context
			return submittable;
		}

		Site site = null;
		try {
			site = SiteService.getSite(context);
		} catch (IdUnusedException e) {
			if (M_log.isDebugEnabled()) {
				M_log.debug("Could not retrieve submittable assignments for nonexistent site: " + context);
			}
		}
		if (site == null)
		{
			return submittable;
		}

		Set<String> siteSubmitterIds = authzGroupService.getUsersIsAllowed(
				SECURE_ADD_ASSIGNMENT_SUBMISSION, Arrays.asList(site.getReference()));
		Map<String, Set<String>> groupIdUserIds = new HashMap<String, Set<String>>();
		for (Group group : site.getGroups()) {
			String groupRef = group.getReference();
			for (Member member : group.getMembers()) {
				if (member.getRole().isAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION)) {
					if (!groupIdUserIds.containsKey(groupRef)) {
						groupIdUserIds.put(groupRef, new HashSet<String>());
					}
					groupIdUserIds.get(groupRef).add(member.getUserId());
				}
			}
		}

		List<Assignment> assignments = (List<Assignment>) getAssignments(context);
		for (Assignment assignment : assignments) {
			Set<String> userIds = new HashSet<String>();
			if (assignment.getAccess() == Assignment.AssignmentAccess.GROUPED) {
				for (String groupRef : (Collection<String>) assignment.getGroups()) {
					if (groupIdUserIds.containsKey(groupRef)) {
						userIds.addAll(groupIdUserIds.get(groupRef));
					}
				}
			} else {
				userIds.addAll(siteSubmitterIds);
			}
			submittable.put(assignment, new ArrayList(userIds));
		}

		return submittable;
	}

	/**
	 * get proper assignments for specified context and user
	 * @param context
	 * @param user
	 * @return
	 */
	private Iterator assignmentsForContextAndUser(String context, String userId) 
	{
		Assignment tempAssignment = null;
		List retVal = new ArrayList();
		List allAssignments = null;

		if (context != null)
		{
			allAssignments = getAssignments(context, userId);
			
			for (int x = 0; x < allAssignments.size(); x++)
			{
				tempAssignment = (Assignment) allAssignments.get(x);

				if ((context.equals(tempAssignment.getContext()))
						|| (context.equals(getGroupNameFromContext(tempAssignment.getContext()))))
				{
					retVal.add(tempAssignment);
				}
			}
		}

		if (retVal.isEmpty())
			return new EmptyIterator();
		else
			return retVal.iterator();
	}

	/**
	 * @inheritDoc
	 */
	public List getListAssignmentsForContext(String context)
	{
		M_log.debug(this + " getListAssignmetsForContext : CONTEXT : " + context);
		Assignment tempAssignment = null;
		List retVal = new ArrayList();

		if (context != null)
		{
			List allAssignments = getAssignments(context);
			for (int x = 0; x < allAssignments.size(); x++)
			{
				tempAssignment = (Assignment) allAssignments.get(x);
				
				if ((context.equals(tempAssignment.getContext()))
						|| (context.equals(getGroupNameFromContext(tempAssignment.getContext()))))
				{
					String deleted = tempAssignment.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
					if (deleted == null || "".equals(deleted))
					{
						// not deleted, show it
						if (tempAssignment.getDraft())
						{
							// who can see the draft assigment
							if (isDraftAssignmentVisible(tempAssignment, context))
							{
								retVal.add(tempAssignment);
							}
						}
						else
						{
							retVal.add(tempAssignment);
						}
					}
				}
			}
		}

		return retVal;

	}
	
	/**
	 * who can see the draft assignment
	 * @param assignment
	 * @param context
	 * @return
	 */
	private boolean isDraftAssignmentVisible(Assignment assignment, String context) 
	{
		return securityService.isSuperUser() // super user can always see it
			|| assignment.getCreator().equals(UserDirectoryService.getCurrentUser().getId()) // the creator can see it
			|| (unlockCheck(SECURE_SHARE_DRAFTS, SiteService.siteReference(context))); // any role user with share draft permission
	}
	
	/**
	 * Access a User's AssignmentSubmission to a particular Assignment.
	 * 
	 * @param assignmentReference
	 *        The reference of the assignment.
	 * @param person -
	 *        The User who's Submission you would like.
	 * @return AssignmentSubmission The user's submission for that Assignment.
	 * @throws IdUnusedException
	 *         if there is no object with this id.
	 * @throws PermissionException
	 *         if the current user is not allowed to access this.
	 */
	public AssignmentSubmission getSubmission(String assignmentReference, User person)
	{
		AssignmentSubmission submission = null;

		String assignmentId = assignmentId(assignmentReference);
		
		if ((assignmentReference != null) && (person != null))
		{
			//First check their personal submission
			submission = m_submissionStorage.get(assignmentId, person.getId());
			if (submission != null && allowGetSubmission(submission.getReference())) {
				return submission;
			}
			try {
				Assignment a = getAssignment(assignmentReference);
				if (a.isGroup()) {
					return getUserGroupSubmissionMap(a, Collections.singletonList(person)).get(person);
				}
			} catch (IdUnusedException | PermissionException e) {
				M_log.debug(e.getMessage());
			}
		}
		
		M_log.debug("No submission found for user {} in assignment {}", person.getId(), assignmentReference);

		return submission;
	}

	/**
	 * Gets a map of users to their submissions for the specified assignment
	 * @param a the assignment in question
	 * @param users the users making up the key set
	 */
	public Map<User, AssignmentSubmission> getUserSubmissionMap(Assignment a, List<User> users)
	{
		Map<User, AssignmentSubmission> userSubmissionMap = new HashMap<>();
		if (a != null && !CollectionUtils.isEmpty(users))
		{
			if (a.isGroup())
			{
				userSubmissionMap.putAll(getUserGroupSubmissionMap(a, users));
			}
			else
			{
				// Get all submissions for these users with a single query
				return m_submissionStorage.getUserSubmissionMap(a, users);
			}
		}
		return userSubmissionMap;
	}

	/**
	 * Gets a map of users to their submissions for the specified group assignment.
	 * @param a the group assignment in question
	 * @param users the users making up the key set
	 */
	private Map<User, AssignmentSubmission> getUserGroupSubmissionMap(Assignment a, List<User> users)
	{
		Map<User, AssignmentSubmission> userSubmissionMap = new HashMap<>();
		if (a == null || !a.isGroup())
		{
			throw new IllegalArgumentException("'a' must be a group assignment");
		}

		try
		{
			Site _site = SiteService.getSite(a.getContext());
			for (User user : users)
			{
				AssignmentSubmission submission = null;
				Collection<Group> groups = (Collection<Group>) _site.getGroupsWithMember(user.getId());
				if (groups != null)
				{
					for (Group _g : groups)
					{
						M_log.debug("Checking submission for group: " + _g.getTitle());
						if(a.getGroups().contains(_g.getReference()))
						{
							submission = getSubmission(a.getReference(), _g.getId());
							if (submission != null && allowGetSubmission(submission.getReference()))
							{
								userSubmissionMap.put(user, submission);
								break;
							}
						}
					}
				}
				else
				{
					M_log.info("Assignment " + a.getId() + " is grouped but " + user.getId() + " is not in any of the site groups");
				}
			}
		}
		catch (IdUnusedException e)
		{
			M_log.error("getUserGroupSubmissionMap invoked with an argument whose 'context' value doesn't match any siteId in the system");
		}

		return userSubmissionMap;
	}

	/**
         * 
	 * Access a Group or User's AssignmentSubmission to a particular Assignment.
	 * 
	 * @param assignmentReference
	 *        The reference of the assignment.
	 * @param submitter -
	 *        The User or Group who's Submission you would like.
	 * @return AssignmentSubmission The user's submission for that Assignment.
	 * @throws IdUnusedException
	 *         if there is no object with this id.
	 * @throws PermissionException
	 *         if the current user is not allowed to access this.
	 */
	public AssignmentSubmission getSubmission(String assignmentReference, String submitter)
	{
		AssignmentSubmission submission = null;

		String assignmentId = assignmentId(assignmentReference);
		
		if ((assignmentReference != null) && (submitter != null))
		{
			submission = m_submissionStorage.get(assignmentId, submitter);
		}

		if (submission != null)
		{
			try
			{
				unlock2(SECURE_ACCESS_ASSIGNMENT_SUBMISSION, SECURE_ACCESS_ASSIGNMENT, submission.getReference());
			}
			catch (PermissionException e)
			{
				M_log.debug(e.getMessage());
				return null;
			}
		}

		return submission;
	}
        
	/**
	 * @inheritDoc
	 */
	public AssignmentSubmission getSubmission(List submissions, User person) 
	{
		AssignmentSubmission retVal = null;
		
		for (int z = 0; z < submissions.size(); z++)
		{
			AssignmentSubmission sub = (AssignmentSubmission) submissions.get(z);
			if (sub != null)
			{
				for (String userId : sub.getSubmitterIds())
				{
						M_log.debug("getSubmission(List, User) comparing aUser id : {} and chosen user id : {}",
								userId, person.getId());
					if (userId.equals(person.getId()))
					{
						M_log.debug("getSubmission(List, User) found a match : return value is {}", sub.getId());
						retVal = sub;
					}
				}
			}
		}

		return retVal;
	}
	
	/**
	 * Get the submissions for an assignment.
	 * 
	 * @param assignment -
	 *        the Assignment who's submissions you would like.
	 * @return Iterator over all the submissions for an Assignment.
	 */
	public List getSubmissions(Assignment assignment)
	{
		List retVal = new ArrayList();

		if (assignment != null)
		{
			retVal = getSubmissions(assignment.getId());
		}
		
		return retVal;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getSubmittedSubmissionsCount(String assignmentRef)
	{
		return m_submissionStorage.getSubmittedSubmissionsCount(assignmentRef);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getUngradedSubmissionsCount(String assignmentRef)
	{
		return m_submissionStorage.getUngradedSubmissionsCount(assignmentRef);
		
	}

	/**
	 * Access the AssignmentSubmission with the specified id.
	 * 
	 * @param submissionReference -
	 *        The reference of the AssignmentSubmission.
	 * @return The AssignmentSubmission corresponding to the id, or null if it does not exist.
	 * @throws IdUnusedException
	 *         if there is no object with this id.
	 * @throws PermissionException
	 *         if the current user is not allowed to access this.
	 */
	public AssignmentSubmission getSubmission(String submissionReference) throws IdUnusedException, PermissionException
	{
		M_log.debug(this + " GET SUBMISSION : REF : " + submissionReference);
		
		// check permission
		unlock2(SECURE_ACCESS_ASSIGNMENT_SUBMISSION, SECURE_ACCESS_ASSIGNMENT, submissionReference);

		AssignmentSubmission submission = null;

		String submissionId = submissionId(submissionReference);

		submission = m_submissionStorage.get(submissionId);

		if (submission == null) throw new IdUnusedException(submissionId);

		// double check the submission submitter information:
		// if current user is not the original submitter and if he doesn't have grading permission, he should not have access to other people's submission.
		String assignmentRef = assignmentReference(submission.getContext(), submission.getAssignmentId());
		if (!allowGradeSubmission(assignmentRef))
		{
			List<String> submitterIds = submission.getSubmitterIds();
			String userId = SessionManager.getCurrentSessionUserId();
			if (!userId.equals(submission.getSubmitterId()) && submitterIds != null && !submitterIds.contains(userId))
			{
				if (M_log.isDebugEnabled()) {
					M_log.debug("getSubmission throwing PermissionException. SubmitterId="+submission.getSubmitterId() + "submitterIds="+StringUtils.join(submitterIds, ","));
				}
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), SECURE_ACCESS_ASSIGNMENT_SUBMISSION, submissionId);
			}
		}
		// track event
		// EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT_SUBMISSION, submission.getReference(), false));

		return submission;

	}// getAssignmentSubmission

	/**
	 * Return the reference root for use in resource references and urls.
	 * 
	 * @return The reference root for use in resource references and urls.
	 */
	protected String getReferenceRoot()
	{
		return REFERENCE_ROOT;
	}

	/**
	 * Update the live properties for an object when modified.
	 */
	protected void addLiveUpdateProperties(ResourcePropertiesEdit props)
	{
		props.addProperty(ResourceProperties.PROP_MODIFIED_BY, SessionManager.getCurrentSessionUserId());

		props.addProperty(ResourceProperties.PROP_MODIFIED_DATE, TimeService.newTime().toString());

	} // addLiveUpdateProperties

	/**
	 * Create the live properties for the object.
	 */
	protected void addLiveProperties(ResourcePropertiesEdit props)
	{
		String current = SessionManager.getCurrentSessionUserId();
		props.addProperty(ResourceProperties.PROP_CREATOR, current);
		props.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = TimeService.newTime().toString();
		props.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		props.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);

	} // addLiveProperties

	/**
	 * check permissions for addAssignment().
	 * 
	 * @param context -
	 *        Describes the portlet context - generated with DefaultId.getChannel()
	 * @return true if the user is allowed to addAssignment(...), false if not.
	 */
	public boolean allowAddGroupAssignment(String context)
	{
		// base the check for SECURE_ADD on the site, any of the site's groups, and the channel
		// if the user can SECURE_ADD anywhere in that mix, they can add an assignment
		// this stack is not the normal azg set for channels, so use a special refernce to get this behavior
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_ASSIGNMENT_GROUPS + Entity.SEPARATOR + "a"
				+ Entity.SEPARATOR + context + Entity.SEPARATOR;

		
		{
			M_log.debug(this + " allowAddGroupAssignment with resource string : " + resourceString);
			M_log.debug("                                    context string : " + context);
		}

		// check security on the channel (throws if not permitted)
		return unlockCheck(SECURE_ADD_ASSIGNMENT, resourceString);

	} // allowAddGroupAssignment

	/**
	 * @inheritDoc
	 */
	public boolean allowReceiveSubmissionNotification(String context)
	{
		String resourceString = getContextReference(context);

		
		{
			M_log.debug(this + " allowReceiveSubmissionNotification with resource string : " + resourceString);
		}

		// checking allow at the site level
		if (unlockCheck(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS, resourceString)) return true;
		
		return false;
	}
	
	/**
	 * @inheritDoc
	 */
	public List allowReceiveSubmissionNotificationUsers(String context)
	{
		String resourceString = getContextReference(context);
		
		{
			M_log.debug(this + " allowReceiveSubmissionNotificationUsers with resource string : " + resourceString);
			M_log.debug("                                   				 	context string : " + context);
		}
		return securityService.unlockUsers(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS, resourceString);

	} // allowAddAssignmentUsers
	
	/**
	 * @inheritDoc
	 */
	public boolean allowAddAssignment(String context)
	{
		String resourceString = getContextReference(context);
		// base the check for SECURE_ADD_ASSIGNMENT on the site and any of the site's groups
		// if the user can SECURE_ADD_ASSIGNMENT anywhere in that mix, they can add an assignment
		// this stack is not the normal azg set for site, so use a special refernce to get this behavior

		
		{
			M_log.debug(this + " allowAddAssignment with resource string : " + resourceString);
		}

		// checking allow at the site level
		if (unlockCheck(SECURE_ADD_ASSIGNMENT, resourceString)) return true;

		// if not, see if the user has any groups to which adds are allowed
		return (!getGroupsAllowAddAssignment(context).isEmpty());
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowAddSiteAssignment(String context)
	{
		// check for assignments that will be site-wide:
		String resourceString = getContextReference(context);

		
		{
			M_log.debug(this + " allowAddSiteAssignment with resource string : " + resourceString);
		}

		// check security on the channel (throws if not permitted)
		return unlockCheck(SECURE_ADD_ASSIGNMENT, resourceString);
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowAllGroups(String context)
	{
		String resourceString = getContextReference(context);

		
		{
			M_log.debug(this + " allowAllGroups with resource string : " + resourceString);
		}

		// checking all.groups
		if (unlockCheck(SECURE_ALL_GROUPS, resourceString)) return true;

		// if not
		return false;
	}
	
	/**
	 * @inheritDoc
	 */
	public Collection getGroupsAllowAddAssignment(String context)
	{
		return getGroupsAllowFunction(SECURE_ADD_ASSIGNMENT, context, null);
	}
	
	/**
	 * @inheritDoc
	 */
	public Collection getGroupsAllowGradeAssignment(String context, String assignmentReference)
	{
		Collection rv = new ArrayList();
		if (allowGradeSubmission(assignmentReference))
		{
			// only if the user is allowed to group at all
			Collection allAllowedGroups = getGroupsAllowFunction(SECURE_GRADE_ASSIGNMENT_SUBMISSION, context, null);
			try
			{
				Assignment a = getAssignment(assignmentReference);
				if (a.getAccess() == Assignment.AssignmentAccess.SITE)
				{
					// for site-scope assignment, return all groups
					rv = allAllowedGroups;
				}
				else
				{
					Collection aGroups = a.getGroups();
					// for grouped assignment, return only those also allowed for grading
					for (Iterator i = allAllowedGroups.iterator(); i.hasNext();)
					{
						Group g = (Group) i.next();
						if (aGroups.contains(g.getReference()))
						{
							rv.add(g);
						}
					}
				}
			}
			catch (Exception e)
			{
				M_log.info(this + " getGroupsAllowGradeAssignment " + e.getMessage() + assignmentReference);
			}
		}
			
		return rv;
	}

	/** 
	 * @inherit
	 */
	public boolean allowGetAssignment(String context)
	{
		String resourceString = getContextReference(context);

		
		{
			M_log.debug(this + " allowGetAssignment with resource string : " + resourceString);
		}

		return unlockCheck(SECURE_ACCESS_ASSIGNMENT, resourceString);
	}

	/**
	 * @inheritDoc
	 */
	public Collection getGroupsAllowGetAssignment(String context)
	{
		return getGroupsAllowFunction(SECURE_ACCESS_ASSIGNMENT, context, null);
	}
	
	// for specified user
	private Collection getGroupsAllowGetAssignment(String context, String userId)
	{
		return getGroupsAllowFunction(SECURE_ACCESS_ASSIGNMENT, context, userId);
	}

	/**
	 * Check permissions for updateing an Assignment.
	 * 
	 * @param assignmentReference -
	 *        The Assignment's reference.
	 * @return True if the current User is allowed to update the Assignment, false if not.
	 */
	public boolean allowUpdateAssignment(String assignmentReference)
	{
		M_log.debug(this + " allowUpdateAssignment with resource string : " + assignmentReference);

		return unlockCheck(SECURE_UPDATE_ASSIGNMENT, assignmentReference);
	}

	/**
	 * Check permissions for removing an Assignment.
	 * 
	 * @return True if the current User is allowed to remove the Assignment, false if not.
	 */
	public boolean allowRemoveAssignment(String assignmentReference)
	{
		M_log.debug(this + " allowRemoveAssignment " + assignmentReference);

		// check security (throws if not permitted)
		return unlockCheck(SECURE_REMOVE_ASSIGNMENT, assignmentReference);
	}

	/**
	 * @inheritDoc
	 */
	public Collection getGroupsAllowRemoveAssignment(String context)
	{
		return getGroupsAllowFunction(SECURE_REMOVE_ASSIGNMENT, context, null);
	}

	/**
	 * Get the groups of this channel's contex-site that the end user has permission to "function" in.
	 * 
	 * @param function
	 *        The function to check
	 */
	protected Collection getGroupsAllowFunction(String function, String context, String userId)
	{	
		Collection rv = new ArrayList();
		try
		{
			// get the site groups
			Site site = SiteService.getSite(context);
			Collection groups = site.getGroups();

			if (securityService.isSuperUser())
			{
				// for super user, return all groups
				return groups;
			}
			else if (userId == null)
			{
				// for current session user
				userId = SessionManager.getCurrentSessionUserId();
			}
			
			// if the user has SECURE_ALL_GROUPS in the context (site), select all site groups
			if (securityService.unlock(userId, SECURE_ALL_GROUPS, SiteService.siteReference(context)) && unlockCheck(function, SiteService.siteReference(context)))
			{
				return groups;
			}

			// otherwise, check the groups for function

			// get a list of the group refs, which are authzGroup ids
			Collection groupRefs = new ArrayList();
			for (Iterator i = groups.iterator(); i.hasNext();)
			{
				Group group = (Group) i.next();
				groupRefs.add(group.getReference());
			}

			// ask the authzGroup service to filter them down based on function
			groupRefs = authzGroupService.getAuthzGroupsIsAllowed(userId,
					function, groupRefs);

			// pick the Group objects from the site's groups to return, those that are in the groupRefs list
			for (Iterator i = groups.iterator(); i.hasNext();)
			{
				Group group = (Group) i.next();
				if (groupRefs.contains(group.getReference()))
				{
					rv.add(group);
				}
			}
		}
		catch (IdUnusedException e)
		{
			M_log.debug(this + " getGroupsAllowFunction idunused :" + context + " : " + e.getMessage());
		}

		return rv;
		
	}

	/** ***********************************************check permissions for AssignmentContent object ******************************************* */
	/**
	 * Check permissions for get AssignmentContent
	 * 
	 * @param contentReference -
	 *        The AssignmentContent reference.
	 * @return True if the current User is allowed to access the AssignmentContent, false if not.
	 */
	public boolean allowGetAssignmentContent(String context)
	{
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "c" + Entity.SEPARATOR + context + Entity.SEPARATOR;

		
		{
			M_log.debug(this + " allowGetAssignmentContent with resource string : " + resourceString);
		}

		// check security (throws if not permitted)
		return unlockCheck(SECURE_ACCESS_ASSIGNMENT_CONTENT, resourceString);
	}

	/**
	 * Check permissions for updating AssignmentContent
	 * 
	 * @param contentReference -
	 *        The AssignmentContent reference.
	 * @return True if the current User is allowed to update the AssignmentContent, false if not.
	 */
	public boolean allowUpdateAssignmentContent(String contentReference)
	{
		
			M_log.debug(this + " allowUpdateAssignmentContent with resource string : " + contentReference);

		// check security (throws if not permitted)
		return unlockCheck(SECURE_UPDATE_ASSIGNMENT_CONTENT, contentReference);
	}

	/**
	 * Check permissions for adding an AssignmentContent.
	 * 
	 * @param context -
	 *        Describes the portlet context - generated with DefaultId.getChannel().
	 * @return True if the current User is allowed to add an AssignmentContent, false if not.
	 */
	public boolean allowAddAssignmentContent(String context)
	{
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "c" + Entity.SEPARATOR + context + Entity.SEPARATOR;
		M_log.debug(this + "allowAddAssignmentContent with resource string : " + resourceString);

		// check security (throws if not permitted)
		if (unlockCheck(SECURE_ADD_ASSIGNMENT_CONTENT, resourceString)) return true;
		
		// if not, see if the user has any groups to which adds are allowed
		return (!getGroupsAllowAddAssignment(context).isEmpty());
	}

	/**
	 * Check permissions for remove the AssignmentContent
	 * 
	 * @param contentReference -
	 *        The AssignmentContent reference.
	 * @return True if the current User is allowed to remove the AssignmentContent, false if not.
	 */
	public boolean allowRemoveAssignmentContent(String contentReference)
	{
		
			M_log.debug(this + " allowRemoveAssignmentContent with referece string : " + contentReference);

		// check security (throws if not permitted)
		return unlockCheck(SECURE_REMOVE_ASSIGNMENT_CONTENT, contentReference);
	}

	/**
	 * Check permissions for add AssignmentSubmission
	 * 
	 * @param context -
	 *        Describes the portlet context - generated with DefaultId.getChannel().
	 * @return True if the current User is allowed to add an AssignmentSubmission, false if not.
	 */
	public boolean allowAddSubmission(String context)
	{
		// check security (throws if not permitted)
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "s" + Entity.SEPARATOR + context + Entity.SEPARATOR;

		M_log.debug(this + " allowAddSubmission with resource string : " + resourceString);

		return unlockCheck(SECURE_ADD_ASSIGNMENT_SUBMISSION, resourceString);
	}
	
	/**
	 * SAK-21525
	 * 
	 * @param context
	 * @param assignment - An Assignment object. Needed for the groups to be checked.
	 * @return
	 */
	public boolean allowAddSubmissionCheckGroups(String context, Assignment assignment)
	{
		// check security (throws if not permitted)
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "s" + Entity.SEPARATOR + context + Entity.SEPARATOR;

		M_log.debug(this + " allowAddSubmission with resource string : " + resourceString);

		return unlockCheckWithGroups(SECURE_ADD_ASSIGNMENT_SUBMISSION, resourceString, assignment);
	}
	
	/**
	 * Get the List of Users who can addSubmission() for this assignment.
	 * 
	 * @param assignmentReference -
	 *        a reference to an assignment
	 * @return the List (User) of users who can addSubmission() for this assignment.
	 */
	public List allowAddSubmissionUsers(String assignmentReference)
	{
		return securityService.unlockUsers(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);

	} // allowAddSubmissionUsers
	
	/**
	 * Get the List of Users who can grade submission for this assignment.
	 * 
	 * @param assignmentReference -
	 *        a reference to an assignment
	 * @return the List (User) of users who can grade submission for this assignment.
	 */
	public List allowGradeAssignmentUsers(String assignmentReference)
	{
		List users = securityService.unlockUsers(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference);
		if (users == null)
		{
			users = new ArrayList();
		}
		
		try
		{
			Assignment a = getAssignment(assignmentReference);
			if (a.getAccess() == Assignment.AssignmentAccess.GROUPED)
			{
				// for grouped assignment, need to include those users that with "all.groups" and "grade assignment" permissions on the site level
				AuthzGroup group = authzGroupService.getAuthzGroup(SiteService.siteReference(a.getContext()));
				if (group != null)
				{
					// get the roles which are allowed for submission but not for all_site control
					Set rolesAllowAllSite = group.getRolesIsAllowed(SECURE_ALL_GROUPS);
					Set rolesAllowGradeAssignment = group.getRolesIsAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION);
					// save all the roles with both "all.groups" and "grade assignment" permissions
					if (rolesAllowAllSite != null)
						rolesAllowAllSite.retainAll(rolesAllowGradeAssignment);
					if (rolesAllowAllSite != null && rolesAllowAllSite.size() > 0)
					{
						for (Iterator iRoles = rolesAllowAllSite.iterator(); iRoles.hasNext(); )
						{
							Set<String> userIds = group.getUsersHasRole((String) iRoles.next());
							if (userIds != null)
							{
								for (Iterator<String> iUserIds = userIds.iterator(); iUserIds.hasNext(); )
								{
									String userId =  iUserIds.next();
									try
									{
										User u = UserDirectoryService.getUser(userId);
										if (!users.contains(u))
										{
											users.add(u);
										}
									}
									catch (Exception ee)
									{
										M_log.warn(" allowGradeAssignmentUsers " + ee.getMessage() + " problem with getting user =" + userId);
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			M_log.warn(" allowGradeAssignmentUsers " + e.getMessage() + " assignmentReference=" + assignmentReference);
		}

		return users;
		
	} // allowGradeAssignmentUsers
	
	/**
	 * @inheritDoc
	 * @param context
	 * @return
	 */
	public List allowAddAnySubmissionUsers(String context)
	{
		List<String> rv = new Vector();
		
		try
		{
			AuthzGroup group = authzGroupService.getAuthzGroup(context);
			
			// get the roles which are allowed for submission but not for all_site control
			Set rolesAllowSubmission = group.getRolesIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION);
			Set rolesAllowAllSite = group.getRolesIsAllowed(SECURE_ALL_GROUPS);
			rolesAllowSubmission.removeAll(rolesAllowAllSite);
			
			for (Iterator iRoles = rolesAllowSubmission.iterator(); iRoles.hasNext(); )
			{
				rv.addAll(group.getUsersHasRole((String) iRoles.next()));
			}
		}
		catch (Exception e)
		{
			M_log.warn(" allowAddAnySubmissionUsers " + e.getMessage() + " context=" + context);
		}
		
		return rv;
		
	}

	/**
	 * Get the List of Users who can add assignment
	 * 
	 * @param assignmentReference -
	 *        a reference to an assignment
	 * @return the List (User) of users who can addSubmission() for this assignment.
	 */
	public List allowAddAssignmentUsers(String context)
	{
		String resourceString = getContextReference(context);
		
		{
			M_log.debug(this + " allowAddAssignmentUsers with resource string : " + resourceString);
			M_log.debug("                                    	context string : " + context);
		}
		return securityService.unlockUsers(SECURE_ADD_ASSIGNMENT, resourceString);

	} // allowAddAssignmentUsers

	/**
	 * Check permissions for accessing a Submission.
	 * 
	 * @param submissionReference -
	 *        The Submission's reference.
	 * @return True if the current User is allowed to get the AssignmentSubmission, false if not.
	 */
	public boolean allowGetSubmission(String submissionReference)
	{
		M_log.debug(this + " allowGetSubmission with resource string : " + submissionReference);

		return unlockCheck2(SECURE_ACCESS_ASSIGNMENT_SUBMISSION, SECURE_ACCESS_ASSIGNMENT, submissionReference);
	}

	/**
	 * Check permissions for updating Submission.
	 * 
	 * @param submissionReference -
	 *        The Submission's reference.
	 * @return True if the current User is allowed to update the AssignmentSubmission, false if not.
	 */
	public boolean allowUpdateSubmission(String submissionReference)
	{
		M_log.debug(this + " allowUpdateSubmission with resource string : " + submissionReference);

		return unlockCheck2(SECURE_UPDATE_ASSIGNMENT_SUBMISSION, SECURE_UPDATE_ASSIGNMENT, submissionReference);
	}

	/**
	 * Check permissions for remove Submission
	 * 
	 * @param submissionReference -
	 *        The Submission's reference.
	 * @return True if the current User is allowed to remove the AssignmentSubmission, false if not.
	 */
	public boolean allowRemoveSubmission(String submissionReference)
	{
		M_log.debug(this + " allowRemoveSubmission with resource string : " + submissionReference);

		// check security (throws if not permitted)
		return unlockCheck(SECURE_REMOVE_ASSIGNMENT_SUBMISSION, submissionReference);
	}

	public boolean allowGradeSubmission(String assignmentReference)
	{
		
		{
			M_log.debug(this + " allowGradeSubmission with resource string : " + assignmentReference);
		}
		return unlockCheck(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference);
	}

	/**
	 * Access the grades spreadsheet for the reference, either for an assignment or all assignments in a context.
	 * 
	 * @param ref
	 *        The reference, either to a specific assignment, or just to an assignment context.
	 * @return The grades spreadsheet bytes.
	 * @throws IdUnusedException
	 *         if there is no object with this id.
	 * @throws PermissionException
	 *         if the current user is not allowed to access this.
	 */
	public byte[] getGradesSpreadsheet(String ref) throws IdUnusedException, PermissionException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (getGradesSpreadsheet(out, ref)) {
			return out.toByteArray();
		}
		return null;
	}

	/**
	 * Access and output the grades spreadsheet for the reference, either for an assignment or all assignments in a context.
	 *
	 * @param out
	 *        The outputStream to stream the grades spreadsheet into.
	 * @param ref
	 *        The reference, either to a specific assignment, or just to an assignment context.
	 * @return Whether the grades spreadsheet is successfully output.
	 * @throws IdUnusedException
	 *         if there is no object with this id.
	 * @throws PermissionException
	 *         if the current user is not allowed to access this.
	 */
	public boolean getGradesSpreadsheet(final OutputStream out, final String ref)
			throws IdUnusedException, PermissionException {
		boolean retVal = false;
		String typeGradesString = REF_TYPE_GRADES + Entity.SEPARATOR;
		String [] parts = ref.substring(ref.indexOf(typeGradesString) + typeGradesString.length()).split(Entity.SEPARATOR);
		String idSite = (parts.length>1) ? parts[1] : parts[0];
		String context = (parts.length>1) ? SiteService.siteGroupReference(idSite, parts[3]) : SiteService.siteReference(idSite);

		// get site title for display purpose
		String siteTitle = "";
		String sheetName = "";
		try
		{
			siteTitle = (parts.length>1)?SiteService.getSite(idSite).getTitle()+" - "+SiteService.getSite(idSite).getGroup((String)parts[3]).getTitle():SiteService.getSite(idSite).getTitle();
			sheetName = (parts.length>1)?SiteService.getSite(idSite).getGroup((String)parts[3]).getTitle():SiteService.getSite(idSite).getTitle();
		}
		catch (Exception e)
		{
			// ignore exception
			M_log.debug(this + ":getGradesSpreadsheet cannot get site context=" + idSite + e.getMessage());
		}
		
		// does current user allowed to grade any assignment?
		boolean allowGradeAny = false;
		List assignmentsList = getListAssignmentsForContext(idSite);
		for (int iAssignment = 0; !allowGradeAny && iAssignment<assignmentsList.size(); iAssignment++)
		{
			if (allowGradeSubmission(((Assignment) assignmentsList.get(iAssignment)).getReference()))
			{
				allowGradeAny = true;
			}
		}
		
		if (!allowGradeAny)
		{
			// not permitted to download the spreadsheet
			return false;
		}
		else
		{
			int rowNum = 0;
			HSSFWorkbook wb = new HSSFWorkbook();
			
			HSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
	
			// Create a row and put some cells in it. Rows are 0 based.
			HSSFRow row = sheet.createRow(rowNum++);
	
			row.createCell(0).setCellValue(rb.getString("download.spreadsheet.title"));
	
			// empty line
			row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue("");
	
			// site title
			row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(rb.getString("download.spreadsheet.site") + siteTitle);
	
			// download time
			row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(
					rb.getString("download.spreadsheet.date") + TimeService.newTime().toStringLocalFull());
	
			// empty line
			row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue("");
	
			HSSFCellStyle style = wb.createCellStyle();
	
			// this is the header row number
			int headerRowNumber = rowNum;
			// set up the header cells
			row = sheet.createRow(rowNum++);
			int cellNum = 0;
			
			// user enterprise id column
			HSSFCell cell = row.createCell(cellNum++);
			cell.setCellStyle(style);
			cell.setCellValue(rb.getString("download.spreadsheet.column.name"));
	
			// user name column
			cell = row.createCell(cellNum++);
			cell.setCellStyle(style);
			cell.setCellValue(rb.getString("download.spreadsheet.column.userid"));
			
			// starting from this row, going to input user data
			Iterator assignments = new SortedIterator(assignmentsList.iterator(), new AssignmentComparator("duedate", "true"));
	
			// site members excluding those who can add assignments
			List members = new ArrayList();
			// hashmap which stores the Excel row number for particular user
			HashMap user_row = new HashMap();
			
			List allowAddAnySubmissionUsers = allowAddAnySubmissionUsers(context);
			for (Iterator iUserIds = new SortedIterator(allowAddAnySubmissionUsers.iterator(), new AssignmentComparator("sortname", "true")); iUserIds.hasNext();)
			{
				String userId = (String) iUserIds.next();
				try
				{
					User u = UserDirectoryService.getUser(userId);
					members.add(u);
					// create the column for user first
					row = sheet.createRow(rowNum);
					// update user_row Hashtable
					user_row.put(u.getId(), Integer.valueOf(rowNum));
					// increase row
					rowNum++;
					// put user displayid and sortname in the first two cells
					cellNum = 0;
					row.createCell(cellNum++).setCellValue(u.getSortName());
					row.createCell(cellNum).setCellValue(u.getDisplayId());
				}
				catch (Exception e)
				{
					M_log.warn(" getGradesSpreadSheet " + e.getMessage() + " userId = " + userId);
				}
			}
				
			int index = 0;
			// the grade data portion starts from the third column, since the first two are used for user's display id and sort name
			while (assignments.hasNext())
			{
				Assignment a = (Assignment) assignments.next();
				
				int assignmentType = a.getContent().getTypeOfGrade();
				
				// for column header, check allow grade permission based on each assignment
				if(!a.getDraft() && allowGradeSubmission(a.getReference()))
				{
					// put in assignment title as the column header
					rowNum = headerRowNumber;
					row = sheet.getRow(rowNum++);
					cellNum = (index + 2);
					cell = row.createCell(cellNum); // since the first two column is taken by student id and name
					cell.setCellStyle(style);
					cell.setCellValue(a.getTitle());
					
					for (int loopNum = 0; loopNum < members.size(); loopNum++)
					{
						// prepopulate the column with the "no submission" string
						row = sheet.getRow(rowNum++);
						cell = row.createCell(cellNum);
						cell.setCellType(1);
						cell.setCellValue(rb.getString("listsub.nosub"));
					}

					// begin to populate the column for this assignment, iterating through student list
					for (Iterator sIterator=getSubmissions(a).iterator(); sIterator.hasNext();)
					{
						AssignmentSubmission submission = (AssignmentSubmission) sIterator.next();
						
						String userId = submission.getSubmitterId();
						
                                                if (a.isGroup()) {                                                     
                                                   
                                                   User[] _users = submission.getSubmitters();
                                                   for (int i=0; _users != null && i < _users.length; i++) {
                                                       
                                                       userId = _users[i].getId();
                                                       
						if (user_row.containsKey(userId))
						{	
							// find right row
							row = sheet.getRow(((Integer)user_row.get(userId)).intValue());
						
							if (submission.getGraded() && submission.getGrade() != null)
							{
								// graded and released
								if (assignmentType == 3)
								{
									try
									{
										// numeric cell type?
										String grade = (StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))!=null)?
												submission.getGradeForUserInGradeBook(userId)!=null?
												submission.getGradeForUserInGradeBook(userId):submission.getGradeForUser(userId):submission.getGradeForUser(userId);
										if(grade == null)
										{
											grade=submission.getGradeDisplay();
										}
										int factor = submission.getAssignment().getContent().getFactor();
										int dec = (int)Math.log10(factor);

										//We get float number no matter the locale it was managed with.
										NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,null);
										float f = nbFormat.parse(grade).floatValue();

										// remove the String-based cell first
										cell = row.getCell(cellNum);
										row.removeCell(cell);
										// add number based cell
										cell=row.createCell(cellNum);
										cell.setCellType(0);
										cell.setCellValue(f);
			
										style = wb.createCellStyle();
										String format ="#,##0.";
										for (int j=0; j<dec; j++) {
											format = format.concat("0");
										}
										style.setDataFormat(wb.createDataFormat().getFormat(format));
										cell.setCellStyle(style);
									}
									catch (Exception e)
									{
										// if the grade is not numeric, let's make it as String type
										// No need to remove the cell and create a new one, as the existing one is String type.
										cell = row.getCell(cellNum);
										cell.setCellType(1);
										cell.setCellValue(submission.getGradeForUser(userId) == null ? submission.getGradeDisplay():
                                                                                    submission.getGradeForUser(userId));
									}
								}
								else
								{
									// String cell type
									cell = row.getCell(cellNum);
									cell.setCellValue(submission.getGradeForUser(userId) == null ? submission.getGradeDisplay():
                                                                                    submission.getGradeForUser(userId));
								}
							}
							else if (submission.getSubmitted() && submission.getTimeSubmitted() != null)
							{
								// submitted, but no grade available yet
								cell = row.getCell(cellNum);
								cell.setCellValue(rb.getString("gen.nograd"));
							}
						} // if
					}
                                                                                                       
				}
                                                else 
                                                {
				
                                                    if (user_row.containsKey(userId))
                                                    {	
							// find right row
							row = sheet.getRow(((Integer)user_row.get(userId)).intValue());
						
							if (submission.getGraded() && submission.getGrade() != null)
							{
								// graded and released
								if (assignmentType == 3)
								{
									try
									{
										// numeric cell type?
										String grade = submission.getGradeDisplay();
										int factor = submission.getAssignment().getContent().getFactor();
										int dec = (int)Math.log10(factor);
			
										//We get float number no matter the locale it was managed with.
										NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,null);
										float f = nbFormat.parse(grade).floatValue();
										
										// remove the String-based cell first
										cell = row.getCell(cellNum);
										row.removeCell(cell);
										// add number based cell
										cell=row.createCell(cellNum);
										cell.setCellType(0);
										cell.setCellValue(f);
			
										style = wb.createCellStyle();
										String format ="#,##0.";
										for (int j=0; j<dec; j++) {
											format = format.concat("0");
										}
										style.setDataFormat(wb.createDataFormat().getFormat(format));
										cell.setCellStyle(style);
									}
									catch (Exception e)
									{
										// if the grade is not numeric, let's make it as String type
										// No need to remove the cell and create a new one, as the existing one is String type. 
										cell = row.getCell(cellNum);
										cell.setCellType(1);
										// Setting grade display instead grade.
										cell.setCellValue(submission.getGradeDisplay());
									}
								}
								else
								{
									// String cell type
									cell = row.getCell(cellNum);
									cell.setCellValue(submission.getGradeDisplay());
								}
							}
							else if (submission.getSubmitted() && submission.getTimeSubmitted() != null)
							{
								// submitted, but no grade available yet
								cell = row.getCell(cellNum);
								cell.setCellValue(rb.getString("gen.nograd"));
							}
                                                    } // if
                                                    
                                                }
					}
				}
				
				index++;
				
			}
			
			// output
			try
			{
				wb.write(out);
				retVal = true;
			}
			catch (IOException e)
			{
				M_log.warn(" getGradesSpreadsheet Can not output the grade spread sheet for reference= " + ref);
			}
			
			return retVal;
		}

	} // getGradesSpreadsheet
	
	@SuppressWarnings("deprecation")
	public Collection<Group> getSubmitterGroupList(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString) {
	    Collection<Group> rv = new ArrayList<Group>();
	    allOrOneGroup = StringUtil.trimToNull(allOrOneGroup);
	    searchString = StringUtil.trimToNull(searchString);
	    boolean bSearchFilterOnly = "true".equalsIgnoreCase(searchFilterOnly);
	    try
	    {
	        Assignment a = getAssignment(aRef);
	        if (a != null)
	        {
	        	Site st = SiteService.getSite(contextString);
	        	if (StringUtils.equals(allOrOneGroup, AssignmentConstants.ALL) || StringUtils.isEmpty(allOrOneGroup))
	        	{
		            if (a.getAccess().equals(Assignment.AssignmentAccess.SITE))
		            {
		                Collection<Group> groupRefs = st.getGroups();
		                for (Iterator gIterator = groupRefs.iterator(); gIterator.hasNext();)
		                {
		                    Group _gg = (Group)gIterator.next();
		                    //if (_gg.getProperties().get(GROUP_SECTION_PROPERTY) == null) {		// NO SECTIONS (this might not be valid test for manually created sections)
		                    rv.add(_gg);
		                    //}
		                }
		            } 
		            else
		            {
		                Collection<String> groupRefs = a.getGroups();
		                for (Iterator gIterator = groupRefs.iterator(); gIterator.hasNext();)
		                {
		                    Group _gg = st.getGroup((String)gIterator.next());		// NO SECTIONS (this might not be valid test for manually created sections)
		                    if (_gg != null) {
		                        rv.add(_gg);
		                    }
		                }
		            }
	        	}
	        	else
	        	{
	        		Group _gg = st.getGroup(allOrOneGroup);
	        		 if (_gg != null) {// && _gg.getProperties().get(GROUP_SECTION_PROPERTY) == null) {
	                        rv.add(_gg);
	                    }
	        	}

	            for (Iterator uIterator = rv.iterator(); uIterator.hasNext();)
	            {
	                Group g = (Group) uIterator.next();
	                AssignmentSubmission uSubmission = getSubmission(aRef, g.getId());
	                if (uSubmission == null)
	                {
	                    if (allowGradeSubmission(a.getReference()))
	                    {
	                        if (a.isGroup()) {
	                            // temporarily allow the user to read and write from assignments (asn.revise permission)
                        		SecurityAdvisor securityAdvisor = new MySecurityAdvisor(
                                        SessionManager.getCurrentSessionUserId(),
                                        new ArrayList<String>(Arrays.asList(SECURE_ADD_ASSIGNMENT_SUBMISSION, SECURE_UPDATE_ASSIGNMENT_SUBMISSION)),
                                        ""/* no submission id yet, pass the empty string to advisor*/);
                        		try {
                        			securityService.pushAdvisor(securityAdvisor);


                        			M_log.debug(this + " getSubmitterGroupList context " + contextString + " for assignment " + a.getId() + " for group " + g.getId());
                        			AssignmentSubmissionEdit s =
                        					addSubmission(contextString, a.getId(), g.getId());
                        			s.setSubmitted(true);
                        			s.setIsUserSubmission(false);
                        			s.setAssignment(a);

                        			// set the resubmission properties
                        			// get the assignment setting for resubmitting
                        			ResourceProperties assignmentProperties = a.getProperties();
                        			String assignmentAllowResubmitNumber = assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
                        			if (assignmentAllowResubmitNumber != null)
                        			{
                        				s.getPropertiesEdit().addProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, assignmentAllowResubmitNumber);

                        				String assignmentAllowResubmitCloseDate = assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
                        				// if assignment's setting of resubmit close time is null, use assignment close time as the close time for resubmit
                        				s.getPropertiesEdit().addProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, assignmentAllowResubmitCloseDate != null?assignmentAllowResubmitCloseDate:String.valueOf(a.getCloseTime().getTime()));
                        			}

                        			commitEdit(s);
                        			// clear the permission
                        		} 
                        		catch (Exception e)
                        		{
                        			M_log.warn("getSubmitterGroupList: exception thrown while creating empty submission for group who has not submitted: " + e.getMessage());
                        		}
                        		finally {
                        			securityService.popAdvisor(securityAdvisor);
                        		}
	                        }
	                    }
	                }
	            }
	        }
	    }
	    catch (IdUnusedException aIdException)
	    {
	        M_log.warn(":getSubmitterGroupList: Assignme id not used: " + aRef + " " + aIdException.getMessage());
	    }

	    catch (PermissionException aPerException)
	    {
	        M_log.warn(":getSubmitterGroupList: Not allowed to get assignment " + aRef + " " + aPerException.getMessage());
	    }

	    return rv;
	}

	/**
	 * {@inheritDoc}}
	 */
	public List<String> getSubmitterIdList(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString) {
		
		List<String> rv = new ArrayList<String>();
		Map<User, AssignmentSubmission> submitterMap = getSubmitterMap(searchFilterOnly, allOrOneGroup, searchString, aRef, contextString);
		for (User u : submitterMap.keySet())
		{
			rv.add(u.getId());
		}
		
		return rv;
	}

	// alternative to getSubmittedIdList which returns full user and submissions, since submitterIdList retrieves them anyway
	public Map<User, AssignmentSubmission> getSubmitterMap(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString)
	{
		Map<User, AssignmentSubmission> rv = new HashMap<User, AssignmentSubmission>();
		List<User> rvUsers;
		allOrOneGroup = StringUtils.trimToNull(allOrOneGroup);
		searchString = StringUtils.trimToNull(searchString);
		
		boolean bSearchFilterOnly = "true".equalsIgnoreCase(searchFilterOnly);
		try
		{
			Assignment a = getAssignment(aRef);
			if (a == null)
			{
				return rv;
			}
			
			// SAK-27824
			if (assignmentUsesAnonymousGrading(a)) {
				bSearchFilterOnly = false;
				searchString = "";
			}
			
			if (bSearchFilterOnly)
			{
				if (allOrOneGroup == null && searchString == null)
				{
					// if the option is set to "Only show user submissions according to Group Filter and Search result"
					// if no group filter and no search string is specified, no user will be shown first by default;
					return rv;
				}
				else 
				{
					List allowAddSubmissionUsers = allowAddSubmissionUsers(aRef);
					if (allOrOneGroup == null && searchString != null) 
					{
						// search is done for all submitters
						rvUsers = getSearchedUsers(searchString, allowAddSubmissionUsers, false);
					}
					else
					{
						// group filter first
						rvUsers = getSelectedGroupUsers(allOrOneGroup, contextString, a, allowAddSubmissionUsers);
						if (searchString != null)
						{
							// then search
							rvUsers = getSearchedUsers(searchString, rvUsers, true);
						}
					}
				}
			}
			else
			{
				List allowAddSubmissionUsers = allowAddSubmissionUsers(aRef);

				// SAK-28055 need to take away those users who have the permissions defined in sakai.properties
				String resourceString = getContextReference(a.getContext());
				String[] permissions = m_serverConfigurationService.getStrings("assignment.submitter.remove.permission");
				if (permissions!=null) {
					for (String permission:permissions) {
						allowAddSubmissionUsers.removeAll(securityService.unlockUsers(permission, resourceString));
					}
				} else {
					allowAddSubmissionUsers.removeAll(securityService.unlockUsers(SECURE_ADD_ASSIGNMENT, resourceString));
				}

				// Step 1: get group if any that is selected
				rvUsers = getSelectedGroupUsers(allOrOneGroup, contextString, a, allowAddSubmissionUsers);

				// Step 2: get all student that meets the search criteria based on previous group users. If search is null or empty string, return all users.
				rvUsers = getSearchedUsers(searchString, rvUsers, true);
			}

			if (!rvUsers.isEmpty())
			{
				List<String> groupRefs = new ArrayList<String>();
				Map<User, AssignmentSubmission> userSubmissionMap = getUserSubmissionMap(a, rvUsers);
				for (Iterator uIterator = rvUsers.iterator(); uIterator.hasNext();)
				{
					User u = (User) uIterator.next();

					AssignmentSubmission uSubmission = userSubmissionMap.get(u);

					if (uSubmission != null)
					{
						rv.put(u, uSubmission);
					}
					// add those users who haven't made any submissions and with submission rights
					else
					{
						//only initiate the group list once
						if (groupRefs.isEmpty())
						{
							if (a.getAccess() == Assignment.AssignmentAccess.SITE)
							{
								// for site range assignment, add the site reference first
								groupRefs.add(SiteService.siteReference(contextString));
							}
							// add all groups inside the site
							Collection groups = getGroupsAllowGradeAssignment(contextString, a.getReference());
							for(Object g : groups)
							{
								if (g instanceof Group)
								{
									groupRefs.add(((Group) g).getReference());
								}
							}
						}
						// construct fake submissions for grading purpose if the user has right for grading
						if (allowGradeSubmission(a.getReference()))
						{
							SecurityAdvisor securityAdvisor = new MySecurityAdvisor(
									SessionManager.getCurrentSessionUserId(), 
									new ArrayList<String>(Arrays.asList(SECURE_ADD_ASSIGNMENT_SUBMISSION, SECURE_UPDATE_ASSIGNMENT_SUBMISSION)),
									groupRefs/* no submission id yet, pass the empty string to advisor*/);
							try
							{
								// temporarily allow the user to read and write from assignments (asn.revise permission)
								securityService.pushAdvisor(securityAdvisor);

								AssignmentSubmissionEdit s = addSubmission(contextString, a.getId(), u.getId());
								if (s != null)
								{
									// Note: If we had s.setSubmitted(false);, this would put it in 'draft mode'
									s.setSubmitted(true);
									/*
									 * SAK-29314 - Since setSubmitted represents whether the submission is in draft mode state, we need another property. So we created isUserSubmission.
									 * This represents whether the submission was geenrated by a user.
									 * We set it to false because these submissions are generated so that the instructor has something to grade;
									 * the user did not in fact submit anything.
									 */
									s.setIsUserSubmission(false);
									s.setAssignment(a);

									// set the resubmission properties
									// get the assignment setting for resubmitting
									ResourceProperties assignmentProperties = a.getProperties();
									String assignmentAllowResubmitNumber = assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
									if (assignmentAllowResubmitNumber != null)
									{
										s.getPropertiesEdit().addProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, assignmentAllowResubmitNumber);

										String assignmentAllowResubmitCloseDate = assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
										// if assignment's setting of resubmit close time is null, use assignment close time as the close time for resubmit
										s.getPropertiesEdit().addProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, assignmentAllowResubmitCloseDate != null?assignmentAllowResubmitCloseDate:String.valueOf(a.getCloseTime().getTime()));
									}

									commitEdit(s);
									rv.put(u, s);
								}
							}
							catch (Exception e)
							{
								M_log.warn("getSubmitterMap: exception thrown while creating empty submission for student who has not submitted: " + e.getMessage());
							}
							finally
							{
								// clear the permission
								securityService.popAdvisor(securityAdvisor);
							}
						}
					}
				}
			}
		}
		catch (IdUnusedException aIdException)
		{
			M_log.warn(":getSubmitterIdList: Assignme id not used: " + aRef + " " + aIdException.getMessage());
		}

		catch (PermissionException aPerException)
		{
			M_log.warn(":getSubmitterIdList: Not allowed to get assignment " + aRef + " " + aPerException.getMessage());
		}
		
		return rv;
	}

	private List<User> getSelectedGroupUsers(String allOrOneGroup, String contextString, Assignment a, List allowAddSubmissionUsers) {
		Collection groups = new ArrayList();
		
		List<User> selectedGroupUsers = new ArrayList<User>();
		if (allOrOneGroup != null && allOrOneGroup.length() > 0)
		{
			// now are we view all sections/groups or just specific one?
			if (allOrOneGroup.equals(AssignmentConstants.ALL))
			{
				if (allowAllGroups(contextString))
				{
					// site range
					try {
						groups.add(SiteService.getSite(contextString));
					} catch (IdUnusedException e) {
						M_log.warn(":getSelectedGroupUsers cannot find site " + " " + contextString + e.getMessage());
					}
				}
				else
				{
					// get all those groups that user is allowed to grade
					groups = getGroupsAllowGradeAssignment(contextString, a.getReference());
				}
			}
			else
			{
				// filter out only those submissions from the selected-group members
				try
				{
					Group group = SiteService.getSite(contextString).getGroup(allOrOneGroup);
					groups.add(group);
				}
				catch (Exception e)
				{
					M_log.warn(":getSelectedGroupUsers " + e.getMessage() + " groupId=" + allOrOneGroup);
				}
			}
			
			for (Iterator iGroup=groups.iterator(); iGroup.hasNext();)
			{
				Object nGroup = iGroup.next();
				String authzGroupRef = (nGroup instanceof Group)? ((Group) nGroup).getReference():((nGroup instanceof Site))?((Site) nGroup).getReference():null;
				if (authzGroupRef != null)
				{
					try
					{
						AuthzGroup group = authzGroupService.getAuthzGroup(authzGroupRef);
						Set grants = group.getUsers();
						for (Iterator iUserIds = grants.iterator(); iUserIds.hasNext();)
						{
							String userId = (String) iUserIds.next();
							
							// don't show user multiple times
							try
							{
								User u = UserDirectoryService.getUser(userId);
								if (u != null && allowAddSubmissionUsers.contains(u))
								{
									if (!selectedGroupUsers.contains(u))
									{
										selectedGroupUsers.add(u);
									}
								}
							}
							catch (UserNotDefinedException uException)
							{
								M_log.warn(":getSelectedGroupUsers " + uException.getMessage() + " userId =" + userId);
							}
						}	
					}
					catch (GroupNotDefinedException gException)
					{
						M_log.warn(":getSelectedGroupUsers " + gException.getMessage() + " authGroupId=" + authzGroupRef);
					}
				}
			}
		}
		return selectedGroupUsers;
	}

	/**
	 * keep the users that match search string in sortname, eid, email field
	 * @param searchString
	 * @param userList
	 * @param retain If true, the original list will be kept if there is no search string specified
	 * @return
	 */
	private List getSearchedUsers(String searchString, List userList, boolean retain) {
		List rv = new ArrayList();
		if (searchString != null && searchString.length() > 0)
		{
			searchString = searchString.toLowerCase();
			for(Iterator iUserList = userList.iterator(); iUserList.hasNext();)
			{
				User u = (User) iUserList.next();
				// search on user sortname, eid, email
				String[] fields = {u.getSortName(), u.getEid(), u.getEmail()};
				List<String> l = new ArrayList(Arrays.asList(fields));
				for (String s : l)
				{
					s = s.toLowerCase();
					if (s != null && s.indexOf(searchString) != -1)
					{
						rv.add(u);
						break;
					}
				}
			}
		}
		else if (retain)
		{
			// retain the original list
			rv = userList;
		}
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void getSubmissionsZip(OutputStream outputStream, String ref) throws IdUnusedException, PermissionException
 	{
		M_log.debug(this + ": getSubmissionsZip reference=" + ref);
		
		getSubmissionsZip(outputStream, ref, null);
 	}

	/**
	 * depends on the query string from ui, determine what to include inside the submission zip
	 * @param outputStream
	 * @param ref
	 * @param queryString
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	protected void getSubmissionsZip(OutputStream out, String ref, String queryString) throws IdUnusedException, PermissionException
 	{
		M_log.debug(this + ": getSubmissionsZip 2 reference=" + ref);
		
		boolean withStudentSubmissionText = false;
		boolean withStudentSubmissionAttachment = false;
		boolean withGradeFile = false;
		String  gradeFileFormat = "csv";
		boolean withFeedbackText = false;
		boolean withFeedbackComment = false;
		boolean withFeedbackAttachment = false;
		
		boolean withoutFolders = false;
		boolean includeNotSubmitted = false;

		String viewString = "";
		String contextString = "";
		String searchString = "";
		String searchFilterOnly = "";
		
		if (queryString != null)
		{
			StringTokenizer queryTokens = new StringTokenizer(queryString, "&");

	        // Parsing the range list
	        while (queryTokens.hasMoreTokens()) {
	            String token = queryTokens.nextToken().trim();
	            
				// check against the content elements selection
				if (token.contains("studentSubmissionText"))
				{
					// should contain student submission text information
					withStudentSubmissionText = true;
				}
				else if (token.contains("studentSubmissionAttachment"))
				{
					// should contain student submission attachment information
					withStudentSubmissionAttachment = true;
				}
				else if (token.contains("gradeFile"))
				{
					// should contain grade file
					withGradeFile = true;	
					if (token.contains("gradeFileFormat=csv"))
					{	
						gradeFileFormat = "csv";
				}
					else if (token.contains("gradeFileFormat=excel"))
					{	
						gradeFileFormat = "excel";
					}
				}
				else if (token.contains("feedbackTexts"))
				{
					// inline text
					withFeedbackText = true;
				}
				else if (token.contains("feedbackComments"))
				{
					// comments  should be available
					withFeedbackComment = true;
				}
				else if (token.contains("feedbackAttachments"))
				{
					// feedback attachment
					withFeedbackAttachment = true;
				}
				else if (token.contains("withoutFolders"))
				{
					// feedback attachment
					withoutFolders = true;
				}
				else if (token.contains("includeNotSubmitted"))
				{
					// include empty submissions
					includeNotSubmitted = true;
				}

				else if (token.contains("contextString"))
				{
					// context
					contextString = token.indexOf("=") != -1 ? token.substring(token.indexOf("=") + 1) : "";
				}
				else if (token.contains("viewString"))
				{
					// view
					viewString = token.indexOf("=") != -1 ? token.substring(token.indexOf("=") + 1) : "";
				}
				else if (token.contains("searchString"))
				{
					// search
					searchString = token.indexOf("=") != -1 ? token.substring(token.indexOf("=") + 1) : "";
				}
				else if (token.contains("searchFilterOnly"))
				{
					// search and group filter only
					searchFilterOnly = token.indexOf("=") != -1 ? token.substring(token.indexOf("=") + 1) : "";
				}
	        }
		}

		byte[] rv = null;
		
		try
		{
			String aRef = assignmentReferenceFromSubmissionsZipReference(ref);
			Assignment a = getAssignment(aRef);
			
			if (a.isGroup()) {
				Collection<Group> submitterGroups = getSubmitterGroupList(searchFilterOnly, viewString.length() == 0 ? AssignmentConstants.ALL:viewString, searchString, aRef, contextString == null ? a.getContext(): contextString);
				if (submitterGroups != null && !submitterGroups.isEmpty())
                                {
					List<GroupSubmission> submissions = new ArrayList<GroupSubmission>();
					for (Iterator<Group> iSubmitterGroupsIterator = submitterGroups.iterator(); iSubmitterGroupsIterator.hasNext();)
                                        {
						Group g = iSubmitterGroupsIterator.next();
						M_log.debug(this + " ZIP GROUP " + g.getTitle() );
						AssignmentSubmission sub = getSubmission(aRef, g.getId());
						M_log.debug(this + " ZIP GROUP " + g.getTitle() + " SUB " + (sub == null ? "null": sub.getId() ));
						if (g != null) {
							GroupSubmission gs = new GroupSubmission(g, sub);
							submissions.add(gs);	
						}
					}
					StringBuilder exceptionMessage = new StringBuilder();

					if (allowGradeSubmission(aRef))
					{
					    zipGroupSubmissions(aRef, a.getTitle(), a.getContent().getTypeOfGradeString(), a.getContent().getTypeOfSubmission(),
					            new SortedIterator(submissions.iterator(), new AssignmentComparator("submitterName", "true")), out, exceptionMessage, withStudentSubmissionText, withStudentSubmissionAttachment, withGradeFile, withFeedbackText, withFeedbackComment, withFeedbackAttachment,gradeFileFormat,includeNotSubmitted);

					    if (exceptionMessage.length() > 0)
					    {
					        // log any error messages

					        M_log.warn(" getSubmissionsZip ref=" + ref + exceptionMessage.toString());
					    }
					}
				}
			}
			else
			{

			//List<String> submitterIds = getSubmitterIdList(searchFilterOnly, viewString.length() == 0 ? AssignmentConstants.ALL:viewString, searchString, aRef, contextString == null? a.getContext():contextString);
			Map<User, AssignmentSubmission> submitters = getSubmitterMap(searchFilterOnly, viewString.length() == 0 ? AssignmentConstants.ALL:viewString, searchString, aRef, contextString == null? a.getContext():contextString); 	
				
			if (!submitters.isEmpty())
			{
				List<AssignmentSubmission> submissions = new ArrayList<AssignmentSubmission>(submitters.values());
	
				StringBuilder exceptionMessage = new StringBuilder();
	
				if (allowGradeSubmission(aRef))
				{
					AssignmentContent content = a.getContent();
					zipSubmissions(aRef, a.getTitle(), content.getTypeOfGradeString(), content.getTypeOfSubmission(), 
							new SortedIterator(submissions.iterator(), new AssignmentComparator("submitterName", "true")), out, exceptionMessage, withStudentSubmissionText, withStudentSubmissionAttachment, withGradeFile, withFeedbackText, withFeedbackComment, withFeedbackAttachment, withoutFolders,gradeFileFormat, includeNotSubmitted);
	
					if (exceptionMessage.length() > 0)
					{
						// log any error messages
						
							M_log.warn(" getSubmissionsZip ref=" + ref + exceptionMessage.toString());
					}
				}

	
			}
		}

		}
		catch (IdUnusedException e)
		{
			
				M_log.debug(this + "getSubmissionsZip -IdUnusedException Unable to get assignment " + ref);
			throw new IdUnusedException(ref);
		}
		catch (PermissionException e)
		{
			M_log.warn(" getSubmissionsZip -PermissionException Not permitted to get assignment " + ref);
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), SECURE_ACCESS_ASSIGNMENT, ref);
		}

	} // getSubmissionsZip
	public String escapeInvalidCharsEntry(String accentedString) {
		String decomposed = Normalizer.normalize(accentedString, Normalizer.Form.NFD);
		String cleanString = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		return cleanString;
	}
	
	protected void zipGroupSubmissions(String assignmentReference, String assignmentTitle, String gradeTypeString, int typeOfSubmission, Iterator submissions, OutputStream outputStream, StringBuilder exceptionMessage, boolean withStudentSubmissionText, boolean withStudentSubmissionAttachment, boolean withGradeFile, boolean withFeedbackText, boolean withFeedbackComment, boolean withFeedbackAttachment,String gradeFileFormat, boolean includeNotSubmitted)
	{
	    ZipOutputStream out = null;
	    try {
	        out = new ZipOutputStream(outputStream);

	        // create the folder structure - named after the assignment's title
	        String root = escapeInvalidCharsEntry(Validator.escapeZipEntry(assignmentTitle)) + Entity.SEPARATOR;

			SpreadsheetExporter.Type type = SpreadsheetExporter.Type.valueOf(gradeFileFormat.toUpperCase());
			SpreadsheetExporter sheet = SpreadsheetExporter.getInstance(type, assignmentTitle, gradeTypeString);

	        String submittedText = "";
	        if (!submissions.hasNext())
	        {
	            exceptionMessage.append("There is no submission yet. ");
	        }

	        // Write the header
			sheet.addHeader("Group", rb.getString("grades.eid"), rb.getString("grades.members"),
					rb.getString("grades.grade"), rb.getString("grades.submissionTime"),rb.getString("grades.late"));

	        // allow add assignment members
	        List allowAddSubmissionUsers = allowAddSubmissionUsers(assignmentReference);

	        // Create the ZIP file
	        String submittersName = "";
	        String caughtException = null;
	        String caughtStackTrace = null;
	        while (submissions.hasNext())
	        {

	            GroupSubmission gs = (GroupSubmission) submissions.next();
	            AssignmentSubmission s = gs.getSubmission(); 

	            M_log.debug( this + " ZIPGROUP " + ( s == null ? "null": s.getId() ));

				//SAK-29314 added a new value where it's by default submitted but is marked when the user submits
	            if ((s.getSubmitted() && s.isUserSubmission())|| includeNotSubmitted)
	            {
	                try
	                {
	                    submittersName = root;

	                    User[] submitters = s.getSubmitters();
	                    String submitterString = gs.getGroup().getTitle() + " (" + gs.getGroup().getId() + ")";
	                    String submittersString = "";
	                    String submitters2String = "";

	                    for (int i = 0; i < submitters.length; i++)
	                    {
	                        if (i > 0)
	                        {
	                            submittersString = submittersString.concat("; ");
	                            submitters2String = submitters2String.concat("; ");
	                        }
	                        String fullName = submitters[i].getSortName();
	                        // in case the user doesn't have first name or last name
	                        if (fullName.indexOf(",") == -1)
	                        {
	                            fullName=fullName.concat(",");
	                        }
	                        submittersString = submittersString.concat(fullName);
	                        submitters2String = submitters2String.concat(submitters[i].getDisplayName());
	                        // add the eid to the end of it to guarantee folder name uniqness
	                        submittersString = submittersString + "(" + submitters[i].getEid() + ")";
	                    }
						String latenessStatus = whenSubmissionMade(s);

						//Adding the row
						sheet.addRow(gs.getGroup().getTitle(), gs.getGroup().getId(), submitters2String,
								s.getGradeDisplay(), s.getTimeSubmittedString(), latenessStatus);

	                    if (StringUtil.trimToNull(submitterString) != null)
	                    {
	                        submittersName = submittersName.concat(StringUtil.trimToNull(submitterString));
	                        submittedText = s.getSubmittedText();

	                        submittersName = submittersName.concat("/");

	                        // record submission timestamp
	                        if (s.getSubmitted() && s.getTimeSubmitted() != null)
	                        {
	                            ZipEntry textEntry = new ZipEntry(submittersName + "timestamp.txt");
	                            out.putNextEntry(textEntry);
	                            byte[] b = (s.getTimeSubmitted().toString()).getBytes();
	                            out.write(b);
	                            textEntry.setSize(b.length);
	                            out.closeEntry();
	                        }

	                        // create the folder structure - named after the submitter's name
	                        if (typeOfSubmission != Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
	                        {
	                            // include student submission text
	                            if (withStudentSubmissionText)
	                            {
	                                // create the text file only when a text submission is allowed
	                                ZipEntry textEntry = new ZipEntry(submittersName + submitterString + "_submissionText" + ZIP_SUBMITTED_TEXT_FILE_TYPE);
	                                out.putNextEntry(textEntry);
	                                byte[] text = submittedText.getBytes();
	                                out.write(text);
	                                textEntry.setSize(text.length);
	                                out.closeEntry();
	                            }

	                            // include student submission feedback text
	                            if (withFeedbackText)
	                            {
	                                // create a feedbackText file into zip
	                                ZipEntry fTextEntry = new ZipEntry(submittersName + "feedbackText.html");
	                                out.putNextEntry(fTextEntry);
	                                byte[] fText = s.getFeedbackText().getBytes();
	                                out.write(fText);
	                                fTextEntry.setSize(fText.length);
	                                out.closeEntry();
	                            }
	                        }

	                        if (typeOfSubmission != Assignment.TEXT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
	                        {
	                            // include student submission attachment
	                            if (withStudentSubmissionAttachment)
	                            {
	                                // create a attachment folder for the submission attachments
	                                String sSubAttachmentFolder = submittersName + rb.getString("stuviewsubm.submissatt") + "/";
	                                ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
	                                out.putNextEntry(sSubAttachmentFolderEntry);
	                                // add all submission attachment into the submission attachment folder
	                                zipAttachments(out, submittersName, sSubAttachmentFolder, s.getSubmittedAttachments());
	                                out.closeEntry();
	                            }
	                        }

	                        if (withFeedbackComment)
	                        {
	                            // the comments.txt file to show instructor's comments
	                            ZipEntry textEntry = new ZipEntry(submittersName + "comments" + ZIP_COMMENT_FILE_TYPE);
	                            out.putNextEntry(textEntry);
	                            byte[] b = FormattedText.encodeUnicode(s.getFeedbackComment()).getBytes();
	                            out.write(b);
	                            textEntry.setSize(b.length);
	                            out.closeEntry();
	                        }

	                        if (withFeedbackAttachment)
	                        {
	                            // create an attachment folder for the feedback attachments
	                            String feedbackSubAttachmentFolder = submittersName + rb.getString("download.feedback.attachment") + "/";
	                            ZipEntry feedbackSubAttachmentFolderEntry = new ZipEntry(feedbackSubAttachmentFolder);
	                            out.putNextEntry(feedbackSubAttachmentFolderEntry);
	                            // add all feedback attachment folder
	                            zipAttachments(out, submittersName, feedbackSubAttachmentFolder, s.getFeedbackAttachments());
	                            out.closeEntry();
	                        }

	                        if (submittersString.trim().length() > 0) {
	                            // the comments.txt file to show instructor's comments
	                            ZipEntry textEntry = new ZipEntry(submittersName + "members" + ZIP_COMMENT_FILE_TYPE);
	                            out.putNextEntry(textEntry);
	                            byte[] b = FormattedText.encodeUnicode(submittersString).getBytes();
	                            out.write(b);
	                            textEntry.setSize(b.length);
	                            out.closeEntry();
	                        }

	                    } // if
	                }
	                catch (Exception e)
	                {
	                    caughtException = e.toString();
	                    if (M_log.isDebugEnabled()) {
	                      caughtStackTrace = ExceptionUtils.getStackTrace(e);
	                    }
	                    break;
	                }
	            } // if the user is still in site

	        } // while -- there is submission

	        if (caughtException == null)
	        {
	            // continue
				if (withGradeFile)
				{
					ZipEntry gradesCSVEntry = new ZipEntry(root + "grades."+ sheet.getFileExtension());
					out.putNextEntry(gradesCSVEntry);
					sheet.write(out);
					out.closeEntry();
				}
	        }
	        else
	        {
	            // log the error
	            exceptionMessage.append(" Exception " + caughtException + " for creating submission zip file for assignment " + "\"" + assignmentTitle + "\"\n");
	            if (M_log.isDebugEnabled()) {
	               exceptionMessage.append(caughtStackTrace);
	            }
	        }
	    }
	    catch (IOException e)
	    {
	        exceptionMessage.append("IOException for creating submission zip file for assignment " + "\"" + assignmentTitle + "\" exception: " + e + "\n");
	    } finally {
	        // Complete the ZIP file
	        if (out != null) {
	            try {
	                out.finish();
	                out.flush();
	            } catch (IOException e) {
	                // tried
	            }
	            try {
	                out.close();
	            } catch (IOException e) {
	                // tried
	            }
	        }
	    }
	}

	protected void zipSubmissions(String assignmentReference, String assignmentTitle, String gradeTypeString, int typeOfSubmission, Iterator submissions, OutputStream outputStream, StringBuilder exceptionMessage, boolean withStudentSubmissionText, boolean withStudentSubmissionAttachment, boolean withGradeFile, boolean withFeedbackText, boolean withFeedbackComment, boolean withFeedbackAttachment, boolean withoutFolders,String gradeFileFormat, boolean includeNotSubmitted)
	{
	    ZipOutputStream out = null;

		try {
			out = new ZipOutputStream(outputStream);

			// create the folder structure - named after the assignment's title
			String root = escapeInvalidCharsEntry(Validator.escapeZipEntry(assignmentTitle)) + Entity.SEPARATOR;

			SpreadsheetExporter.Type type = SpreadsheetExporter.Type.valueOf(gradeFileFormat.toUpperCase());
			SpreadsheetExporter sheet = SpreadsheetExporter.getInstance(type, assignmentTitle, gradeTypeString);

			String submittedText = "";
			if (!submissions.hasNext())
			{
				exceptionMessage.append("There is no submission yet. ");
			}
			
			sheet.addHeader(rb.getString("grades.id"),rb.getString("grades.eid"),rb.getString("grades.lastname"),
					rb.getString("grades.firstname"),rb.getString("grades.grade"),
					rb.getString("grades.submissionTime"),rb.getString("grades.late"));

			// allow add assignment members
			List allowAddSubmissionUsers = allowAddSubmissionUsers(assignmentReference);
			
			// Create the ZIP file
			String submittersName = "";
			String caughtException = null;
			String caughtStackTrace = null;
			while (submissions.hasNext())
			{
				AssignmentSubmission s = (AssignmentSubmission) submissions.next();
				boolean isAnon = assignmentUsesAnonymousGrading( s );
				//SAK-29314 added a new value where it's by default submitted but is marked when the user submits
				if ((s.getSubmitted() && s.isUserSubmission()) || includeNotSubmitted)
				{
					// get the submission user id and see if the user is still in site
					String userId = s.getSubmitterId();
					try
					{
						User u = UserDirectoryService.getUser(userId);
						if (allowAddSubmissionUsers.contains(u))
						{
							submittersName = root;
							
							User[] submitters = s.getSubmitters();
							String submittersString = "";
							for (int i = 0; i < submitters.length; i++)
							{
								if (i > 0)
								{
									submittersString = submittersString.concat("; ");
								}
								String fullName = submitters[i].getSortName();
								// in case the user doesn't have first name or last name
								if (fullName.indexOf(",") == -1)
								{
									fullName=fullName.concat(",");
								}
								submittersString = submittersString.concat(fullName);
								// add the eid to the end of it to guarantee folder name uniqness
								// if user Eid contains non ascii characters, the user internal id will be used
								String userEid = submitters[i].getEid();
								String candidateEid = escapeInvalidCharsEntry(userEid);
								if (candidateEid.equals(userEid)){
									submittersString = submittersString + "(" + candidateEid + ")";
								} else{ 	
									submittersString = submittersString + "(" + submitters[i].getId() + ")";
								}
								submittersString = escapeInvalidCharsEntry(submittersString);
								// Work out if submission is late.
								String latenessStatus = whenSubmissionMade(s);

								String fullAnonId = s.getAnonymousSubmissionId();
								String anonTitle = rb.getString("grading.anonymous.title");

								// SAK-17606
								if (!isAnon)
								{
									sheet.addRow(submitters[i].getDisplayId(), submitters[i].getEid(),
											submitters[i].getLastName(), submitters[i].getFirstName(),
											s.getGradeDisplay(), s.getTimeSubmittedString(), latenessStatus);
								}
								else
								{
									sheet.addRow(fullAnonId, fullAnonId, anonTitle, anonTitle, s.getGradeDisplay(),
											s.getTimeSubmittedString(), latenessStatus);
								}
							}
							
							if (StringUtils.trimToNull(submittersString) != null)
							{
								submittersName = submittersName.concat(StringUtils.trimToNull(submittersString));
								submittedText = s.getSubmittedText();
		
								// SAK-17606
								if (isAnon) {
									submittersName = root + s.getAnonymousSubmissionId();
									submittersString = s.getAnonymousSubmissionId();
								}
		
								if (!withoutFolders)
								{
									submittersName = submittersName.concat("/");
								}
								else
								{
									submittersName = submittersName.concat("_");
								}
									
								// record submission timestamp
								if (!withoutFolders)
								{
									if (s.getSubmitted() && s.getTimeSubmitted() != null)
									{
										ZipEntry textEntry = new ZipEntry(submittersName + "timestamp.txt");
										out.putNextEntry(textEntry);
										byte[] b = (s.getTimeSubmitted().toString()).getBytes();
										out.write(b);
										textEntry.setSize(b.length);
										out.closeEntry();
									}
								}
								
								// create the folder structure - named after the submitter's name
								if (typeOfSubmission != Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
								{
									// include student submission text
									if (withStudentSubmissionText)
									{
										// create the text file only when a text submission is allowed
										String submittersNameString = submittersName + submittersString;
										
										//remove folder name if Download All is without user folders
										if (withoutFolders)
										{
											submittersNameString = submittersName;
										}
										ZipEntry textEntry = new ZipEntry(submittersNameString + "_submissionText" + ZIP_SUBMITTED_TEXT_FILE_TYPE); 
										out.putNextEntry(textEntry);
										byte[] text = submittedText.getBytes();
										out.write(text);
										textEntry.setSize(text.length);
										out.closeEntry();
									}
								
									// include student submission feedback text
									if (withFeedbackText)
									{
									// create a feedbackText file into zip
									ZipEntry fTextEntry = new ZipEntry(submittersName + "feedbackText.html");
									out.putNextEntry(fTextEntry);
									byte[] fText = s.getFeedbackText().getBytes();
									out.write(fText);
									fTextEntry.setSize(fText.length);
									out.closeEntry();
									}
								}
								
								if (typeOfSubmission != Assignment.TEXT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
								{
									// include student submission attachment
									if (withStudentSubmissionAttachment)
									{
										//remove "/" that creates a folder if Download All is without user folders
										String sSubAttachmentFolder = submittersName + rb.getString("stuviewsubm.submissatt");//jh + "/";
										if (!withoutFolders)
										{
											// create a attachment folder for the submission attachments
											sSubAttachmentFolder = submittersName + rb.getString("stuviewsubm.submissatt") + "/";
											sSubAttachmentFolder = escapeInvalidCharsEntry(sSubAttachmentFolder);
											ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
											out.putNextEntry(sSubAttachmentFolderEntry);

										}
										else
										{
											sSubAttachmentFolder = sSubAttachmentFolder + "_";
											//submittersName = submittersName.concat("_");
										}

										// add all submission attachment into the submission attachment folder
										zipAttachments(out, submittersName, sSubAttachmentFolder, s.getSubmittedAttachments());
										out.closeEntry();
									}
								}
								
								if (withFeedbackComment)
								{
									// the comments.txt file to show instructor's comments
									ZipEntry textEntry = new ZipEntry(submittersName + "comments" + ZIP_COMMENT_FILE_TYPE);
									out.putNextEntry(textEntry);
									byte[] b = FormattedText.encodeUnicode(s.getFeedbackComment()).getBytes();
									out.write(b);
									textEntry.setSize(b.length);
									out.closeEntry();
								}
								
								if (withFeedbackAttachment)
								{
									// create an attachment folder for the feedback attachments
									String feedbackSubAttachmentFolder = submittersName + rb.getString("download.feedback.attachment");
									if (!withoutFolders)
									{
										feedbackSubAttachmentFolder = feedbackSubAttachmentFolder + "/";
										ZipEntry feedbackSubAttachmentFolderEntry = new ZipEntry(feedbackSubAttachmentFolder);
										out.putNextEntry(feedbackSubAttachmentFolderEntry);
									}
									else
									{
										submittersName = submittersName.concat("_");
									}
								
									// add all feedback attachment folder
									zipAttachments(out, submittersName, feedbackSubAttachmentFolder, s.getFeedbackAttachments());
									out.closeEntry();
								}
							} // if
						}
					}
					catch (Exception e)
					{
						caughtException = e.toString();
						if (M_log.isDebugEnabled()) {
							caughtStackTrace = ExceptionUtils.getStackTrace(e);
						}
						break;
					}
				} // if the user is still in site

			} // while -- there is submission

			if (caughtException == null)
			{
				// continue
				if (withGradeFile)
				{
					ZipEntry gradesCSVEntry = new ZipEntry(root + "grades."+ sheet.getFileExtension());
					out.putNextEntry(gradesCSVEntry);
					sheet.write(out);
					out.closeEntry();
				}
			}
			else
			{
				// log the error
				exceptionMessage.append(" Exception " + caughtException + " for creating submission zip file for assignment " + "\"" + assignmentTitle + "\"\n");
				if (M_log.isDebugEnabled()) {
					exceptionMessage.append(caughtStackTrace);
				}
			}
		}
		catch (IOException e)
		{
			exceptionMessage.append("IOException for creating submission zip file for assignment " + "\"" + assignmentTitle + "\" exception: " + e + "\n");
		} finally {
		    // Complete the ZIP file
		    if (out != null) {
		        try {
		            out.finish();
		            out.flush();
		        } catch (IOException e) {
		            // tried
		        }
		        try {
		            out.close();
		        } catch (IOException e) {
		            // tried
		        }
		    }
		}
	}

	/**
	 * Just check to see if a submission is late.
	 * @param s The assignment submission
	 * @return The resource bundle string.
	 */
	private String whenSubmissionMade(AssignmentSubmission s) {
		Time dueTime = s.getAssignment().getDueTime();
		Time submittedTime = s.getTimeSubmitted();
		String latenessStatus;
		if (submittedTime == null) {
			latenessStatus = rb.getString("grades.lateness.unknown");
		} else if(dueTime != null && submittedTime.after(dueTime)) {
			latenessStatus = rb.getString("grades.lateness.late");
		} else {
			latenessStatus = rb.getString("grades.lateness.ontime");
		}
		return latenessStatus;
	}

	/*
	 * SAK-17606 - If the assignment uses anonymous grading returns true, else false
	 * 
	 * Params: AssignmentSubmission s
	 */
	private boolean assignmentUsesAnonymousGrading(AssignmentSubmission s) {
		return assignmentUsesAnonymousGrading(s.getAssignment());
	}

	/*
	 * If the assignment uses anonymous grading returns true, else false
	 * 
	 * SAK-27824
	 * 
	 * Params: Assignment a
	 */
	@Override
	public boolean assignmentUsesAnonymousGrading(Assignment a) {
		ResourceProperties properties = a.getProperties();
			try {
					return properties.getBooleanProperty(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING);
			}
			catch (EntityPropertyNotDefinedException e) {
					M_log.debug("Entity Property " + NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING + " not defined " + e.getMessage());
			}
			catch (EntityPropertyTypeException e) {
					M_log.debug("Entity Property " + NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING + " type not defined " + e.getMessage());
			}
			return false;
	}

	private void zipAttachments(ZipOutputStream out, String submittersName, String sSubAttachmentFolder, List attachments) {
		int attachedUrlCount = 0;
		InputStream content = null;
		HashMap<String, Integer> done = new HashMap<String, Integer> ();
		for (int j = 0; j < attachments.size(); j++)
		{
			Reference r = (Reference) attachments.get(j);
			try
			{
				ContentResource resource = m_contentHostingService.getResource(r.getId());

				String contentType = resource.getContentType();
				
				ResourceProperties props = r.getProperties();
				String displayName = props.getPropertyFormatted(props.getNamePropDisplayName());
				displayName = escapeInvalidCharsEntry(displayName);

				// for URL content type, encode a redirect to the body URL
				if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL))
				{
					displayName = "attached_URL_" + attachedUrlCount;
					attachedUrlCount++;
				}

				// buffered stream input
				content = resource.streamContent();
				byte data[] = new byte[1024 * 10];
				BufferedInputStream bContent = null;
				try
				{
					bContent = new BufferedInputStream(content, data.length);
					
					String candidateName = sSubAttachmentFolder + displayName;
					String realName = null;
					Integer already = done.get(candidateName);
					if (already == null) {
					    realName = candidateName;
					    done.put(candidateName, 1);
					} else {
						String fileName = FilenameUtils.removeExtension(candidateName);
						String fileExt = FilenameUtils.getExtension(candidateName);
						if(!"".equals(fileExt.trim())){
							fileExt = "." + fileExt;
						}
					    realName = fileName + "+" + already + fileExt;
					    done.put(candidateName, already + 1);
					}

					ZipEntry attachmentEntry = new ZipEntry(realName);
					out.putNextEntry(attachmentEntry);
					int bCount = -1;
					while ((bCount = bContent.read(data, 0, data.length)) != -1) 
					{
						out.write(data, 0, bCount);
					}
					
					try
					{
						out.closeEntry(); // The zip entry need to be closed
					}
					catch (IOException ioException)
					{
						M_log.warn(":zipAttachments: problem closing zip entry " + ioException);
					}
				}
				catch (IllegalArgumentException iException)
				{
					M_log.warn(":zipAttachments: problem creating BufferedInputStream with content and length " + data.length + iException);
				}
				finally
				{
					if (bContent != null)
					{
						try
						{
							bContent.close(); // The BufferedInputStream needs to be closed
						}
						catch (IOException ioException)
						{
							M_log.warn(":zipAttachments: problem closing FileChannel " + ioException);
						}
					}
				}
			}
			catch (PermissionException e)
			{
				M_log.warn(" zipAttachments--PermissionException submittersName="
						+ submittersName + " attachment reference=" + r);
			}
			catch (IdUnusedException e)
			{
				M_log.warn(" zipAttachments--IdUnusedException submittersName="
						+ submittersName + " attachment reference=" + r);
			}
			catch (TypeException e)
			{
				M_log.warn(" zipAttachments--TypeException: submittersName="
						+ submittersName + " attachment reference=" + r);
			}
			catch (IOException e)
			{
				M_log.warn(" zipAttachments--IOException: Problem in creating the attachment file: submittersName="
								+ submittersName + " attachment reference=" + r + " error " + e);
			}
			catch (ServerOverloadException e)
			{
				M_log.warn(" zipAttachments--ServerOverloadException: submittersName="
						+ submittersName + " attachment reference=" + r);
			}
			finally
			{
				if (content != null)
				{
					try
					{
						content.close(); // The input stream needs to be closed
					}
					catch (IOException ioException)
					{
						M_log.warn(":zipAttachments: problem closing Inputstream content " + ioException);
					}
				}
			}
		} // for
	}

	/**
	 * Get the string to form an assignment grade spreadsheet
	 * 
	 * @param context
	 *        The assignment context String
	 * @param assignmentId
	 *        The id for the assignment object; when null, indicates all assignment in that context
	 */
	public String gradesSpreadsheetReference(String context, String assignmentId)
	{
		// based on all assignment in that context
		String s = REFERENCE_ROOT + Entity.SEPARATOR + REF_TYPE_GRADES + Entity.SEPARATOR + context;
		if (assignmentId != null)
		{
			// based on the specified assignment only
			s = s.concat(Entity.SEPARATOR + assignmentId);
		}

		return s;

	} // gradesSpreadsheetReference

	/**
	 * Get the string to form an assignment submissions zip file
	 * 
	 * @param context
	 *        The assignment context String
	 * @param assignmentReference
	 *        The reference for the assignment object;
	 */
	public String submissionsZipReference(String context, String assignmentReference)
	{
		// based on the specified assignment
		return REFERENCE_ROOT + Entity.SEPARATOR + REF_TYPE_SUBMISSIONS + Entity.SEPARATOR + context + Entity.SEPARATOR
				+ assignmentReference;

	} // submissionsZipReference

	/**
	 * Decode the submissionsZipReference string to get the assignment reference String
	 * 
	 * @param sReference
	 *        The submissionZipReference String
	 * @return The assignment reference String
	 */
	private String assignmentReferenceFromSubmissionsZipReference(String sReference)
	{
		// remove the String part relating to submissions zip reference
		if (sReference.indexOf(Entity.SEPARATOR +"site") == -1)
		{
			return sReference.substring(sReference.lastIndexOf(Entity.SEPARATOR + "assignment"));
		}
		else
		{
			return sReference.substring(sReference.lastIndexOf(Entity.SEPARATOR + "assignment"), sReference.indexOf(Entity.SEPARATOR +"site"));
		}

	} // assignmentReferenceFromSubmissionsZipReference
	
	/**
	 * Decode the submissionsZipReference string to get the group reference String
	 * 
	 * @param sReference
	 *        The submissionZipReference String
	 * @return The group reference String
	 */
	private String groupReferenceFromSubmissionsZipReference(String sReference)
	{
		// remove the String part relating to submissions zip reference
		if (sReference.indexOf(Entity.SEPARATOR +"site") != -1)
		{
			return sReference.substring(sReference.lastIndexOf(Entity.SEPARATOR + "site"));
		}
		else
		{
			return null;
		}

	} // assignmentReferenceFromSubmissionsZipReference

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ResourceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "assignment";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
					EntityAccessOverloadException, EntityCopyrightException
			{
				if (SessionManager.getCurrentSessionUserId() == null)
				{
					// fail the request, user not logged in yet.
				}
				else
				{
					try
					{
						if (REF_TYPE_SUBMISSIONS.equals(ref.getSubType()))
						{
							String queryString = req.getQueryString();
							res.setContentType("application/zip");
							res.setHeader("Content-Disposition", "attachment; filename = bulk_download.zip");
							 
							OutputStream out = null;
							try
							{
							    out = res.getOutputStream();
							    
							    // get the submissions zip blob
							    getSubmissionsZip(out, ref.getReference(), queryString);
							    
							}
							catch (Throwable ignore)
							{
							    M_log.error(this + " getHttpAccess handleAccess " + ignore.getMessage() + " ref=" + ref.getReference());
							}
							finally
							{
							    if (out != null)
							    {
							        try
							        {
									    out.flush();
									    out.close();
							        }
							        catch (Throwable ignore)
							        {
							        	M_log.warn(": handleAccess 1 " + ignore.getMessage());
							        }
							    }
							}
						}
	
						else if (REF_TYPE_GRADES.equals(ref.getSubType()))
						{
								res.setContentType("application/vnd.ms-excel");
								res.setHeader("Content-Disposition", "attachment; filename = export_grades_file.xls");
	
								OutputStream out = null;
								try
								{
									out = res.getOutputStream();
									getGradesSpreadsheet(out, ref.getReference());
									out.flush();
									out.close();
								}
								catch (Throwable ignore)
								{
									M_log.warn(": handleAccess 2 " + ignore.getMessage());
								}
								finally
								{
									if (out != null)
									{
										try
										{
											out.close();
										}
										catch (Throwable ignore)
										{
											M_log.warn(": handleAccess 3 " + ignore.getMessage());
										}
									}
								}
						}
						else
						{
							 M_log.warn("handleAccess: throw IdUnusedException " + ref.getReference());
							throw new IdUnusedException(ref.getReference());
						}
					}
					catch (Throwable t)
					{
						 M_log.warn(" HandleAccess: caught exception " + t.toString() + " and rethrow it!");
						throw new EntityNotDefinedException(ref.getReference());
					}
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String id = null;
			String subType = null;
			String container = null;
			String context = null;

			// Note: StringUtils.split would not produce the following first null part
			// Still use StringUtil here.
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);
			// we will get null, assignment, [a|c|s|grades|submissions], context, [auid], id

			if (parts.length > 2)
			{
				subType = parts[2];

				if (parts.length > 3)
				{
					// context is the container
					context = parts[3];

					// submissions have the assignment unique id as a container
					if ("s".equals(subType))
					{
						if (parts.length > 5)
						{
							container = parts[4];
							id = parts[5];
						}
					}

					// others don't
					else
					{
						if (parts.length > 4)
						{
							id = parts[4];
						}
					}
				}
			}

			ref.set(APPLICATION_ID, subType, id, container, context);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		Entity rv = null;

		try
		{
			// is it an AssignmentContent object
			if (REF_TYPE_CONTENT.equals(ref.getSubType()))
			{
				rv = getAssignmentContent(ref.getReference());
			}
			// is it an Assignment object
			else if (REF_TYPE_ASSIGNMENT.equals(ref.getSubType()))
			{
				rv = getAssignment(ref.getReference());
			}
			// is it an AssignmentSubmission object
			else if (REF_TYPE_SUBMISSION.equals(ref.getSubType()))
			{
				rv = getSubmission(ref.getReference());
			}
			else
				M_log.warn("getEntity(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn("getEntity(): " + e + " ref=" + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getEntity(): " + e + " ref=" + ref.getReference());
		}
		catch (NullPointerException e)
		{
			M_log.warn("getEntity(): " + e + " ref=" + ref.getReference());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		Collection rv = new ArrayList();

		// for AssignmentService assignments:
		// if access set to SITE, use the assignment and site authzGroups.
		// if access set to GROUPED, use the assignment, and the groups, but not the site authzGroups.
		// if the user has SECURE_ALL_GROUPS in the context, ignore GROUPED access and treat as if SITE

		try
		{
			// for assignment and content
			if (REF_TYPE_ASSIGNMENT.equals(ref.getSubType()) || REF_TYPE_CONTENT.equals(ref.getSubType()))
			{
				// assignment
				rv.add(ref.getReference());
				
				boolean grouped = false;
				Collection groups = null;

				// check SECURE_ALL_GROUPS - if not, check if the assignment has groups or not
				// TODO: the last param needs to be a ContextService.getRef(ref.getContext())... or a ref.getContextAuthzGroup() -ggolden
				if ((userId == null) || ((!securityService.isSuperUser(userId)) && (!securityService.unlock(userId, SECURE_ALL_GROUPS, SiteService.siteReference(ref.getContext())))))
				{
					// get the channel to get the message to get group information
					// TODO: check for efficiency, cache and thread local caching usage -ggolden
					if (ref.getId() != null)
					{
						Assignment a = null;
						if(REF_TYPE_ASSIGNMENT.equals(ref.getSubType())){
							a = findAssignment(ref.getReference());
						} else if(REF_TYPE_CONTENT.equals(ref.getSubType())){
							a = findAssignmentFromContent(ref.getContext(), ref.getReference());
						}
						if (a != null)
						{
							grouped = Assignment.AssignmentAccess.GROUPED == a.getAccess();
							groups = a.getGroups();
						}
					}
				}

				if (grouped)
				{
					// groups
					rv.addAll(groups);
				}

				// not grouped
				else
				{
					// site
					ref.addSiteContextAuthzGroup(rv);
				}
			}
			else
			{
				rv.add(ref.getReference());
				
				// for submission, use site security setting
				ref.addSiteContextAuthzGroup(rv);
			}
		}
		catch (Throwable e)
		{
			M_log.warn(" getEntityAuthzGroups(): " + e.getMessage() + " ref=" + ref.getReference());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		String rv = null;

		try
		{
			// is it an AssignmentContent object
			if (REF_TYPE_CONTENT.equals(ref.getSubType()))
			{
				AssignmentContent c = getAssignmentContent(ref.getReference());
				rv = c.getUrl();
			}
			// is it an Assignment object
			else if (REF_TYPE_ASSIGNMENT.equals(ref.getSubType()))
			{
				Assignment a = getAssignment(ref.getReference());
				rv = a.getUrl();
			}
			// is it an AssignmentSubmission object
			else if (REF_TYPE_SUBMISSION.equals(ref.getSubType()))
			{
				AssignmentSubmission s = getSubmission(ref.getReference());
				rv = s.getUrl();
			}
			else
				M_log.warn(" getEntityUrl(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn("getEntityUrl(): " + e + " ref=" + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getEntityUrl(): " + e + " ref=" + ref.getReference());
		}
		catch (NullPointerException e)
		{
			M_log.warn("getEntityUrl(): " + e + " ref=" + ref.getReference());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		// String assignRef = assignmentReference(siteId, SiteService.MAIN_CONTAINER);
		results.append("archiving " + getLabel() + " context " + Entity.SEPARATOR + siteId + Entity.SEPARATOR
				+ SiteService.MAIN_CONTAINER + ".\n");

		// start with an element with our very own (service) name
		Element element = doc.createElement(AssignmentService.class.getName());
		((Element) stack.peek()).appendChild(element);
		stack.push(element);

		Iterator assignmentsIterator = getAssignmentsForContext(siteId);

		while (assignmentsIterator.hasNext())
		{
			Assignment assignment = (Assignment) assignmentsIterator.next();

			// archive this assignment
			Element el = assignment.toXml(doc, stack);
			element.appendChild(el);

			// in order to make the assignment.xml have a better structure
			// the content id attribute removed from the assignment node
			// the content will be a child of assignment node
			el.removeAttribute("assignmentcontent");

			// then archive the related content
			AssignmentContent content = (AssignmentContent) assignment.getContent();
			if (content != null)
			{
				Element contentEl = content.toXml(doc, stack);

				// assignment node has already kept the context info
				contentEl.removeAttribute("context");

				// collect attachments
				List atts = content.getAttachments();

				for (int i = 0; i < atts.size(); i++)
				{
					Reference ref = (Reference) atts.get(i);
					// if it's in the attachment area, and not already in the list
					if ((ref.getReference().startsWith("/content/attachment/")) && (!attachments.contains(ref)))
					{
						attachments.add(ref);
					}

					// in order to make assignment.xml has the consistent format with the other xml files
					// move the attachments to be the children of the content, instead of the attributes
					String attributeString = "attachment" + i;
					String attRelUrl = contentEl.getAttribute(attributeString);
					contentEl.removeAttribute(attributeString);
					Element attNode = doc.createElement("attachment");
					attNode.setAttribute("relative-url", attRelUrl);
					contentEl.appendChild(attNode);

				} // for

				// make the content a childnode of the assignment node
				el.appendChild(contentEl);

				Iterator submissionsIterator = getSubmissions(assignment).iterator();
				while (submissionsIterator.hasNext())
				{
					AssignmentSubmission submission = (AssignmentSubmission) submissionsIterator.next();

					// archive this assignment
					Element submissionEl = submission.toXml(doc, stack);
					el.appendChild(submissionEl);

				}
			} // if
		} // while

		stack.pop();

		return results.toString();

	} // archive

	/**
	 * Replace the WT user id with the new qualified id
	 * 
	 * @param el
	 *        The XML element holding the perproties
	 * @param useIdTrans
	 *        The HashMap to track old WT id to new CTools id
	 */
	protected void WTUserIdTrans(Element el, Map userIdTrans)
	{
		NodeList children4 = el.getChildNodes();
		int length4 = children4.getLength();
		for (int i4 = 0; i4 < length4; i4++)
		{
			Node child4 = children4.item(i4);
			if (child4.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element4 = (Element) child4;
				if (element4.getTagName().equals("property"))
				{
					String creatorId = "";
					String modifierId = "";
					if (element4.hasAttribute("CHEF:creator"))
					{
						if ("BASE64".equalsIgnoreCase(element4.getAttribute("enc")))
						{
							creatorId = Xml.decodeAttribute(element4, "CHEF:creator");
						}
						else
						{
							creatorId = element4.getAttribute("CHEF:creator");
						}
						String newCreatorId = (String) userIdTrans.get(creatorId);
						if (newCreatorId != null)
						{
							Xml.encodeAttribute(element4, "CHEF:creator", newCreatorId);
							element4.setAttribute("enc", "BASE64");
						}
					}
					else if (element4.hasAttribute("CHEF:modifiedby"))
					{
						if ("BASE64".equalsIgnoreCase(element4.getAttribute("enc")))
						{
							modifierId = Xml.decodeAttribute(element4, "CHEF:modifiedby");
						}
						else
						{
							modifierId = element4.getAttribute("CHEF:modifiedby");
						}
						String newModifierId = (String) userIdTrans.get(modifierId);
						if (newModifierId != null)
						{
							Xml.encodeAttribute(element4, "CHEF:modifiedby", newModifierId);
							element4.setAttribute("enc", "BASE64");
						}
					}
				}
			}
		}

	} // WTUserIdTrans

	/**
	 * {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		int count = 0;

		try
		{
			// pass the DOM to get new assignment ids, and adjust attachments
			NodeList children2 = root.getChildNodes();

			int length2 = children2.getLength();
			for (int i2 = 0; i2 < length2; i2++)
			{
				Node child2 = children2.item(i2);
				if (child2.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element2 = (Element) child2;

					if (element2.getTagName().equals("assignment"))
					{
						// a flag showing if continuing merging the assignment
						boolean goAhead = true;
						AssignmentContentEdit contentEdit = null;

						// element2 now - assignment node
						// adjust the id of this assignment
						// String newId = IdManager.createUuid();
						element2.setAttribute("id", IdManager.createUuid());
						element2.setAttribute("context", siteId);

						// cloneNode(false) - no children cloned
						Element el2clone = (Element) element2.cloneNode(false);

						// traverse this assignment node first to check if the person who last modified, has the right role.
						// if no right role, mark the flag goAhead to be false.
						NodeList children3 = element2.getChildNodes();
						int length3 = children3.getLength();
						for (int i3 = 0; i3 < length3; i3++)
						{
							Node child3 = children3.item(i3);
							if (child3.getNodeType() == Node.ELEMENT_NODE)
							{
								Element element3 = (Element) child3;

								// add the properties childnode to the clone of assignment node
								if (element3.getTagName().equals("properties"))
								{
									NodeList children6 = element3.getChildNodes();
									int length6 = children6.getLength();
									for (int i6 = 0; i6 < length6; i6++)
									{
										Node child6 = children6.item(i6);
										if (child6.getNodeType() == Node.ELEMENT_NODE)
										{
											Element element6 = (Element) child6;

											if (element6.getTagName().equals("property"))
											{
												if (element6.getAttribute("name").equalsIgnoreCase("CHEF:modifiedby"))
												{
													if ("BASE64".equalsIgnoreCase(element6.getAttribute("enc")))
													{
														String creatorId = Xml.decodeAttribute(element6, "value");
														if (!userListAllowImport.contains(creatorId)) goAhead = false;
													}
													else
													{
														String creatorId = element6.getAttribute("value");
														if (!userListAllowImport.contains(creatorId)) goAhead = false;
													}
												}
											}
										}
									}
								}
							}
						} // for

						// then, go ahead to merge the content and assignment
						if (goAhead)
						{
							for (int i3 = 0; i3 < length3; i3++)
							{
								Node child3 = children3.item(i3);
								if (child3.getNodeType() == Node.ELEMENT_NODE)
								{
									Element element3 = (Element) child3;

									// add the properties childnode to the clone of assignment node
									if (element3.getTagName().equals("properties"))
									{
										// add the properties childnode to the clone of assignment node
										el2clone.appendChild(element3.cloneNode(true));
									}
									else if (element3.getTagName().equals("content"))
									{
										// element3 now- content node
										// adjust the id of this content
										String newContentId = IdManager.createUuid();
										element3.setAttribute("id", newContentId);
										element3.setAttribute("context", siteId);

										// clone the content node without the children of <properties>
										Element el3clone = (Element) element3.cloneNode(false);

										// update the assignmentcontent id in assignment node
										String assignContentId = "/assignment/c/" + siteId + "/" + newContentId;
										el2clone.setAttribute("assignmentcontent", assignContentId);

										// for content node, process the attachment or properties kids
										NodeList children5 = element3.getChildNodes();
										int length5 = children5.getLength();
										int attCount = 0;
										for (int i5 = 0; i5 < length5; i5++)
										{
											Node child5 = children5.item(i5);
											if (child5.getNodeType() == Node.ELEMENT_NODE)
											{
												Element element5 = (Element) child5;

												// for the node of "properties"
												if (element5.getTagName().equals("properties"))
												{
													// for the file from WT, preform userId translation when needed
													if (!userIdTrans.isEmpty())
													{
														WTUserIdTrans(element3, userIdTrans);
													}
												} // for the node of properties
												el3clone.appendChild(element5.cloneNode(true));

												// for "attachment" children
												if (element5.getTagName().equals("attachment"))
												{
													// map the attachment area folder name
													// filter out the invalid characters in the attachment id
													// map the attachment area folder name
													String oldUrl = element5.getAttribute("relative-url");
													if (oldUrl.startsWith("/content/attachment/" + fromSiteId + "/"))
													{
														String newUrl = "/content/attachment/" + siteId + oldUrl.substring(("/content/attachment" + fromSiteId).length());
														element5.setAttribute("relative-url", Validator.escapeQuestionMark(newUrl));
														
														// transfer attachment, replace the context string and add new attachment if necessary
														newUrl = transferAttachment(fromSiteId, siteId, null, oldUrl.substring("/content".length()));
														element5.setAttribute("relative-url", Validator.escapeQuestionMark(newUrl));
														
														newUrl = (String) attachmentNames.get(oldUrl);
														if (newUrl != null)
														{
															if (newUrl.startsWith("/attachment/"))
																newUrl = "/content".concat(newUrl);

															element5.setAttribute("relative-url", Validator
																	.escapeQuestionMark(newUrl));
														}
													}

													// map any references to this site to the new site id
													else if (oldUrl.startsWith("/content/group/" + fromSiteId + "/"))
													{
														String newUrl = "/content/group/" + siteId
																+ oldUrl.substring(("/content/group/" + fromSiteId).length());
														element5.setAttribute("relative-url", Validator.escapeQuestionMark(newUrl));
													}
													// put the attachment back to the attribute field of content
													// to satisfy the input need of mergeAssignmentContent
													String attachmentString = "attachment" + attCount;
													el3clone.setAttribute(attachmentString, element5.getAttribute("relative-url"));
													attCount++; 

												} // if
											} // if
										} // for

										// create a newassignment content
										contentEdit = mergeAssignmentContent(el3clone);
										commitEdit(contentEdit);
									}
								}
							} // for

 							// when importing, refer to property to determine draft status
							if ("false".equalsIgnoreCase(m_serverConfigurationService.getString("import.importAsDraft")))
							{
								String draftAttribute = el2clone.getAttribute("draft");
								if (draftAttribute.equalsIgnoreCase("true") || draftAttribute.equalsIgnoreCase("false"))
									el2clone.setAttribute("draft", draftAttribute);
								else
									el2clone.setAttribute("draft", "true");
							}
							else
							{
								el2clone.setAttribute("draft", "true");
							}

							// merge in this assignment
							AssignmentEdit edit = mergeAssignment(el2clone);
							edit.setContent(contentEdit);
							commitEdit(edit);

							count++;
						} // if goAhead
					} // if
				} // if
			} // for
		}
		catch (Exception any)
		{
			M_log.warn(" merge(): exception: " + any.getMessage() + " siteId=" + siteId + " from site id=" + fromSiteId);
		}

		results.append("merging assignment " + siteId + " (" + count + ") assignments.\n");
		return results.toString();

	} // merge

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.assignment", "sakai.assignment.grades" };
		return toolIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List resourceIds){
		transferCopyEntitiesRefMigrator(fromContext, toContext, resourceIds);
	}


	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List resourceIds)
	{
		Map<String, String> transversalMap = new HashMap<String, String>();
		// import Assignment objects
		Iterator oAssignments = getAssignmentsForContext(fromContext);
		while (oAssignments.hasNext())
		{
			Assignment oAssignment = (Assignment) oAssignments.next();
			String oAssignmentId = oAssignment.getId();

			boolean toBeImported = true;
			if (resourceIds != null && resourceIds.size() > 0)
			{
				// if there is a list for import assignments, only import those assignments and relative submissions
				toBeImported = false;
				for (int m = 0; m < resourceIds.size() && !toBeImported; m++)
				{
					if (((String) resourceIds.get(m)).equals(oAssignmentId))
					{
						toBeImported = true;
					}
				}
			}

			if (toBeImported)
			{
				AssignmentEdit nAssignment = null;
				AssignmentContentEdit nContent = null;

				if (!m_assignmentStorage.check(oAssignmentId))
				{

				}
				else
				{
					try
					{
						// add new Assignment content
						String oContentReference = oAssignment.getContentReference();
						String oContentId = contentId(oContentReference);
						if (!m_contentStorage.check(oContentId))
							throw new IdUnusedException(oContentId);
						else
						{
							AssignmentContent oContent = getAssignmentContent(oContentReference);
							nContent = addAssignmentContent(toContext);
							// attributes
							nContent.setAllowAttachments(oContent.getAllowAttachments());
							nContent.setContext(toContext);
							nContent.setGroupProject(oContent.getGroupProject());
							nContent.setHonorPledge(oContent.getHonorPledge());
							nContent.setHideDueDate(oContent.getHideDueDate());
							nContent.setIndividuallyGraded(oContent.individuallyGraded());
							// replace all occurrence of old context with new context inside instruction text
							String instructions = oContent.getInstructions();
							if (instructions.indexOf(fromContext) != -1)
							{
								instructions = instructions.replaceAll(fromContext, toContext);
							}
							nContent.setInstructions(instructions);
							nContent.setMaxGradePoint(oContent.getMaxGradePoint());
							nContent.setFactor(oContent.getFactor());
							nContent.setReleaseGrades(oContent.releaseGrades());
							nContent.setTimeLastModified(oContent.getTimeLastModified());
							nContent.setTitle(oContent.getTitle());
							nContent.setTypeOfGrade(oContent.getTypeOfGrade());
							nContent.setTypeOfSubmission(oContent.getTypeOfSubmission());
							// review service
							nContent.setAllowReviewService(oContent.getAllowReviewService());
							// properties
							ResourcePropertiesEdit p = nContent.getPropertiesEdit();
							p.clear();
							p.addAll(oContent.getProperties());
							// update live properties
							addLiveProperties(p);
							// attachment
							List oAttachments = oContent.getAttachments();
							List nAttachments = m_entityManager.newReferenceList();
							for (int n = 0; n < oAttachments.size(); n++)
							{
								Reference oAttachmentRef = (Reference) oAttachments.get(n);
								String oAttachmentId = ((Reference) oAttachments.get(n)).getId();
								if (oAttachmentId.indexOf(fromContext) != -1)
								{
									// transfer attachment, replace the context string and add new attachment if necessary
									transferAttachment(fromContext, toContext, nAttachments, oAttachmentId);
								}
								else
								{
									nAttachments.add(oAttachmentRef);
								}
							}
							nContent.replaceAttachments(nAttachments);
							// complete the edit
							m_contentStorage.commit(nContent);
							((BaseAssignmentContentEdit) nContent).closeEdit();
						}
					}
					catch (Exception e)
					{
						if (M_log.isWarnEnabled()) M_log.warn(" transferCopyEntities " + e.toString()  + " oAssignmentId=" + oAssignmentId);
					}

					if (nContent != null)
					{
						try
						{
							// add new assignment
							nAssignment = addAssignment(toContext);
							// attribute
							nAssignment.setCloseTime(oAssignment.getCloseTime());
							nAssignment.setContentReference(nContent.getReference());
							nAssignment.setContext(toContext);
							
 							// when importing, refer to property to determine draft status
							if ("false".equalsIgnoreCase(m_serverConfigurationService.getString("import.importAsDraft")))
							{
								nAssignment.setDraft(oAssignment.getDraft());
							}
							else
							{
								nAssignment.setDraft(true);
							}
							
							nAssignment.setGroup(oAssignment.isGroup());
							nAssignment.setDropDeadTime(oAssignment.getDropDeadTime());
							nAssignment.setDueTime(oAssignment.getDueTime());
							nAssignment.setOpenTime(oAssignment.getOpenTime());
							nAssignment.setSection(oAssignment.getSection());
							nAssignment.setTitle(oAssignment.getTitle());
							nAssignment.setPosition_order(oAssignment.getPosition_order());
							
							nAssignment.setAllowPeerAssessment(oAssignment.getAllowPeerAssessment());
							nAssignment.setPeerAssessmentAnonEval(oAssignment.getPeerAssessmentAnonEval());
							nAssignment.setPeerAssessmentInstructions(oAssignment.getPeerAssessmentInstructions());
							nAssignment.setPeerAssessmentNumReviews(oAssignment.getPeerAssessmentNumReviews());
							nAssignment.setPeerAssessmentStudentViewReviews(oAssignment.getPeerAssessmentStudentViewReviews());
							nAssignment.setPeerAssessmentPeriod(oAssignment.getPeerAssessmentPeriod());
							if(nAssignment.getPeerAssessmentPeriod() == null && nAssignment.getCloseTime() != null){
								// set the peer period time to be 10 mins after accept until date
								GregorianCalendar c = new GregorianCalendar();
								c.setTimeInMillis(nAssignment.getCloseTime().getTime());
								c.add(GregorianCalendar.MINUTE, 10);
								nAssignment.setPeerAssessmentPeriod(TimeService.newTime(c.getTimeInMillis()));
							}
							// properties
							ResourcePropertiesEdit p = nAssignment.getPropertiesEdit();
							p.clear();
							p.addAll(oAssignment.getProperties());
							
							// one more touch on the gradebook-integration link	 
							String associatedGradebookAssignment = StringUtils.trimToNull(p.getProperty(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
							if (associatedGradebookAssignment != null) {
								// see if the old assignment's associated gradebook item is an internal gradebook entry or externally defined
								boolean isExternalAssignmentDefined = m_gradebookExternalAssessmentService.isExternalAssignmentDefined(oAssignment.getContent().getContext(), associatedGradebookAssignment);
								if (isExternalAssignmentDefined)
								{
									// if this is an external defined (came from assignment)
									// mark the link as "add to gradebook" for the new imported assignment, since the assignment is still of draft state
									//later when user posts the assignment, the corresponding assignment will be created in gradebook.	 
									p.removeProperty(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);	 
									p.addProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ADD);
							    }
							}

							// remove the link btw assignment and announcement item. One can announce the open date afterwards
							p.removeProperty(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
							p.removeProperty("new_assignment_open_date_announced");
							p.removeProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID);
							
							// remove the link btw assignment and calendar item. One can add the due date to calendar afterwards
							p.removeProperty(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
							p.removeProperty("new_assignment_due_date_scheduled");
							p.removeProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
							
							// update live properties
							addLiveProperties(p);
							// complete the edit
							m_assignmentStorage.commit(nAssignment);
							((BaseAssignmentEdit) nAssignment).closeEdit();
							
							transversalMap.put("assignment/" + oAssignment.getId(), "assignment/" + nAssignment.getId());
							M_log.info("old assignment id:"+oAssignment.getId()+" - new assignment id:"+nAssignment.getId());
							
							try {
								if (m_taggingManager.isTaggable()) {
									for (TaggingProvider provider : m_taggingManager
											.getProviders()) {
										provider
												.transferCopyTags(
														m_assignmentActivityProducer
																.getActivity(oAssignment),
														m_assignmentActivityProducer
																.getActivity(nAssignment));
									}
								}
							} catch (PermissionException pe) {
								M_log.error(this + " transferCopyEntities " + pe.toString()  + " oAssignmentId=" + oAssignment.getId() + " nAssignmentId=" + nAssignment.getId());
							}
						}
						catch (Exception ee)
						{
							M_log.error(this + " transferCopyEntities " + ee.toString() + " oAssignmentId=" + oAssignment.getId() + " nAssignmentId=" + nAssignment.getId());
						}
					}
				} // if-else
				//Import supplementary items if they are present in the assignment to be imported
				AssignmentSupplementItemService assignmentSupplementItemService =
						(AssignmentSupplementItemService) ComponentManager.get("org.sakaiproject.assignment.api.model.AssignmentSupplementItemService");
				// Model Answer
				AssignmentModelAnswerItem oModelAnswerItem = assignmentSupplementItemService.getModelAnswer(oAssignmentId);
				if (oModelAnswerItem != null) {
					AssignmentModelAnswerItem nModelAnswerItem = assignmentSupplementItemService.newModelAnswer();
					assignmentSupplementItemService.saveModelAnswer(nModelAnswerItem);
					nModelAnswerItem.setAssignmentId(nAssignment.getId());
					nModelAnswerItem.setText(oModelAnswerItem.getText());
					nModelAnswerItem.setShowTo(oModelAnswerItem.getShowTo());
					Set oAttachments = oModelAnswerItem.getAttachmentSet();
					Set<AssignmentSupplementItemAttachment> nAttachments = new HashSet<AssignmentSupplementItemAttachment>();
					for (Iterator iter = oAttachments.iterator(); iter.hasNext();) {
						AssignmentSupplementItemAttachment a = (AssignmentSupplementItemAttachment) iter.next();
						AssignmentSupplementItemAttachment nAttach = assignmentSupplementItemService.newAttachment();
						// New attachment creation
						String nAttachId = transferAttachment(fromContext, toContext, null, a.getAttachmentId().replaceFirst("/content", ""));
						if (StringUtils.isNotEmpty(nAttachId)) {
							nAttach.setAssignmentSupplementItemWithAttachment(nModelAnswerItem);
							nAttach.setAttachmentId(nAttachId);
							assignmentSupplementItemService.saveAttachment(nAttach);
							nAttachments.add(nAttach);
						}
					}
					nModelAnswerItem.setAttachmentSet(nAttachments);
					assignmentSupplementItemService.saveModelAnswer(nModelAnswerItem);
				}
				// Private Note
				AssignmentNoteItem oNoteItem = assignmentSupplementItemService.getNoteItem(oAssignmentId);
				if (oNoteItem != null) {
					AssignmentNoteItem nNoteItem = assignmentSupplementItemService.newNoteItem();
					//assignmentSupplementItemService.saveNoteItem(nNoteItem);
					nNoteItem.setAssignmentId(nAssignment.getId());
					nNoteItem.setNote(oNoteItem.getNote());
					nNoteItem.setShareWith(oNoteItem.getShareWith());
					nNoteItem.setCreatorId(UserDirectoryService.getCurrentUser().getId());
					assignmentSupplementItemService.saveNoteItem(nNoteItem);
				}
				// All Purpose 
				AssignmentAllPurposeItem oAllPurposeItem = assignmentSupplementItemService.getAllPurposeItem(oAssignmentId);
				if (oAllPurposeItem != null) {
					AssignmentAllPurposeItem nAllPurposeItem = assignmentSupplementItemService.newAllPurposeItem();
					assignmentSupplementItemService.saveAllPurposeItem(nAllPurposeItem);
					nAllPurposeItem.setAssignmentId(nAssignment.getId());
					nAllPurposeItem.setTitle(oAllPurposeItem.getTitle());
					nAllPurposeItem.setText(oAllPurposeItem.getText());
					nAllPurposeItem.setHide(oAllPurposeItem.getHide());
					nAllPurposeItem.setReleaseDate(null);
					nAllPurposeItem.setRetractDate(null);
					Set oAttachments = oAllPurposeItem.getAttachmentSet();
					Set<AssignmentSupplementItemAttachment> nAttachments = new HashSet<AssignmentSupplementItemAttachment>();
					for (Iterator iter = oAttachments.iterator(); iter.hasNext();) {
						AssignmentSupplementItemAttachment a = (AssignmentSupplementItemAttachment) iter.next();
						AssignmentSupplementItemAttachment nAttach = assignmentSupplementItemService.newAttachment();
						// New attachment creation
						String nAttachId = transferAttachment(fromContext, toContext, null, a.getAttachmentId().replaceFirst("/content", ""));
						if (StringUtils.isNotEmpty(nAttachId)) {
							nAttach.setAssignmentSupplementItemWithAttachment(nAllPurposeItem);
							nAttach.setAttachmentId(nAttachId);
							assignmentSupplementItemService.saveAttachment(nAttach);
							nAttachments.add(nAttach);
						}
					}
					nAllPurposeItem.setAttachmentSet(nAttachments);
					assignmentSupplementItemService.cleanAllPurposeItemAccess(nAllPurposeItem);
					Set<AssignmentAllPurposeItemAccess> accessSet = new HashSet<AssignmentAllPurposeItemAccess>();
					AssignmentAllPurposeItemAccess access = assignmentSupplementItemService.newAllPurposeItemAccess();
					access.setAccess(UserDirectoryService.getCurrentUser().getId());
					access.setAssignmentAllPurposeItem(nAllPurposeItem);
					assignmentSupplementItemService.saveAllPurposeItemAccess(access);
					accessSet.add(access);
					nAllPurposeItem.setAccessSet(accessSet);
					assignmentSupplementItemService.saveAllPurposeItem(nAllPurposeItem);
				}
			} // if
		} // for
		return transversalMap;
	} // importResources

	/**
	 * manipulate the transfered attachment
	 * @param fromContext
	 * @param toContext
	 * @param nAttachments
	 * @param oAttachmentId
	 * @return the new reference
	 */

	private String transferAttachment(String fromContext, String toContext,
			List nAttachments, String oAttachmentId) 
	{
		String rv = "";
		
		// replace old site id with new site id in attachments
		String nAttachmentId = oAttachmentId.replaceAll(fromContext, toContext);
		try
		{
			ContentResource attachment = m_contentHostingService.getResource(nAttachmentId);
			if (nAttachments != null)
			{
				nAttachments.add(m_entityManager.newReference(attachment.getReference()));
			}
			rv = attachment.getReference();
		}
		catch (IdUnusedException e)
		{
			try
			{
				ContentResource oAttachment = m_contentHostingService.getResource(oAttachmentId);
				try
				{
					if (m_contentHostingService.isAttachmentResource(nAttachmentId))
					{
						// add the new resource into attachment collection area
						ContentResource attachment = m_contentHostingService.addAttachmentResource(
								Validator.escapeResourceName(oAttachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME)), 
								toContext, 
								ToolManager.getTool("sakai.assignment.grades").getTitle(), 
								oAttachment.getContentType(), 
								oAttachment.getContent(), 
								oAttachment.getProperties());
						rv = attachment.getReference();
						// add to attachment list
						if (nAttachments != null)
						{
							nAttachments.add(m_entityManager.newReference(rv));
						}
					}
					else
					{
						// add the new resource into resource area
						ContentResource attachment = m_contentHostingService.addResource(
								Validator.escapeResourceName(oAttachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME)),
								toContext,
								1, 
								oAttachment.getContentType(), 
								oAttachment.getContent(), 
								oAttachment.getProperties(), 
								NotificationService.NOTI_NONE);
						rv = attachment.getReference();
						// add to attachment list
						if (nAttachments != null)
						{
							nAttachments.add(m_entityManager.newReference(rv));
						}
					}
				}
				catch (Exception eeAny)
				{
					// if the new resource cannot be added
					M_log.warn(" transferCopyEntities: cannot add new attachment with id=" + nAttachmentId + " " + eeAny.getMessage());
				}
			}
			catch (Exception eAny)
			{
				// if cannot find the original attachment, do nothing.
				M_log.warn(" transferCopyEntities: cannot find the original attachment with id=" + oAttachmentId + " " + eAny.getMessage());
			}
		}
		catch (Exception any)
		{
			M_log.warn(" transferCopyEntities" + any.getMessage() + " oAttachmentId=" + oAttachmentId + " nAttachmentId=" + nAttachmentId);
		}
		
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		String rv = "Assignment: " + ref.getReference();
		
		try
		{
			// is it an AssignmentContent object
			if (REF_TYPE_CONTENT.equals(ref.getSubType()))
			{
				AssignmentContent c = getAssignmentContent(ref.getReference());
				rv = "AssignmentContent: " + c.getId() + " (" + c.getContext() + ")";
			}
			// is it an Assignment object
			else if (REF_TYPE_ASSIGNMENT.equals(ref.getSubType()))
			{
				Assignment a = getAssignment(ref.getReference());
				rv = "Assignment: " + a.getId() + " (" + a.getContext() + ")";
			}
			// is it an AssignmentSubmission object
			else if (REF_TYPE_SUBMISSION.equals(ref.getSubType()))
			{
				AssignmentSubmission s = getSubmission(ref.getReference());
				rv = "AssignmentSubmission: " + s.getId() + " (" + s.getContext() + ")";
			}
			else
				M_log.warn(" getEntityDescription(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn(" getEntityDescription(): " + e.getMessage() + " ref=" + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn(" getEntityDescription(): " + e.getMessage() + " ref=" + ref.getReference());
		}
		catch (NullPointerException e)
		{
			M_log.warn(" getEntityDescription(): " + e.getMessage() + " ref=" + ref.getReference());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		ResourceProperties rv = null;

		try
		{
			// is it an AssignmentContent object
			if (REF_TYPE_CONTENT.equals(ref.getSubType()))
			{
				AssignmentContent c = getAssignmentContent(ref.getReference());
				rv = c.getProperties();
			}
			// is it an Assignment object
			else if (REF_TYPE_ASSIGNMENT.equals(ref.getSubType()))
			{
				Assignment a = getAssignment(ref.getReference());
				rv = a.getProperties();
			}
			// is it an AssignmentSubmission object
			else if (REF_TYPE_SUBMISSION.equals(ref.getSubType()))
			{
				AssignmentSubmission s = getSubmission(ref.getReference());
				rv = s.getProperties();
			}
			else
				M_log.warn(" getEntityResourceProperties: unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn(" getEntityResourceProperties(): " + e.getMessage() + " ref=" + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn(" getEntityResourceProperties(): " + e.getMessage() + " ref=" + ref.getReference());
		}
		catch (NullPointerException e)
		{
			M_log.warn(" getEntityResourceProperties(): " + e.getMessage() + " ref=" + ref.getReference());
		}

		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean canSubmit(String context, Assignment a) {
	    return canSubmit (context,a,null);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canSubmit(String context, Assignment a, String userId)
	{
		// submissions are never allowed to non-electronic assignments
		if (a.getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
		{
			return false;
		}

		// return false if not allowed to submit at all
		if (!allowAddSubmissionCheckGroups(context, a) && !allowAddAssignment(context) /*SAK-25555 return true if user is allowed to add assignment*/) return false;
		
		//If userId is not defined look it up
		if (userId == null) {
		    userId = SessionManager.getCurrentSessionUserId();
		}

		// if user can submit to this assignment
		List visibleAssignments = assignments(context, userId);
		if (visibleAssignments == null || !visibleAssignments.contains(a)) return false;
		
		try
		{
			// get user
			User u = UserDirectoryService.getUser(userId);
			
			Time currentTime = TimeService.newTime();
			
			// return false if the assignment is draft or is not open yet
			Time openTime = a.getOpenTime();
			if (a.getDraft() || (openTime != null && openTime.after(currentTime)))
			{
				return false;
			}
			
			// return false if the current time has passed the assignment close time
			Time closeTime = a.getCloseTime();
			
			// get user's submission
			AssignmentSubmission submission = null;
			
			submission = getSubmission(a.getReference(), u);
			
			// check for allow resubmission or not first
			// return true if resubmission is allowed and current time is before resubmission close time
			// get the resubmit settings from submission object first
			String allowResubmitNumString = submission != null?submission.getProperties().getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER):null;
			if (allowResubmitNumString != null  && submission.getTimeSubmitted() != null && this.hasBeenSubmitted(submission))
			{
				try
				{
					int allowResubmitNumber = Integer.parseInt(allowResubmitNumString);
					String allowResubmitCloseTime = submission != null ? (String) submission.getProperties().getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME):null;
					Time resubmitCloseTime = null;
					
					if (allowResubmitCloseTime != null)
					{
						// see if a resubmission close time is set on submission level
						resubmitCloseTime = TimeService.newTime(Long.parseLong(allowResubmitCloseTime));
					}
					else
					{
						// otherwise, use assignment close time as the resubmission close time
						resubmitCloseTime = a.getCloseTime();
					}
					return (allowResubmitNumber > 0 /* additional submission number allowed */ || allowResubmitNumber == -1 /* unlimited submission times allowed */) && resubmitCloseTime != null && currentTime.before(resubmitCloseTime);
				}
				catch (NumberFormatException e)
				{
					M_log.warn(" canSubmit(String, Assignment) " + e.getMessage() + " allowResubmitNumString=" + allowResubmitNumString);
				}
			}
			
			if (submission == null || (submission != null && submission.getTimeSubmitted() == null))
			{
				// if there is no submission yet
				if (closeTime != null && currentTime.after(closeTime))
				{
					return false;
				}
				else
				{
					return true;
				}
			}
			else
			{
				if (!submission.getSubmitted() && !(closeTime != null && currentTime.after(closeTime)))
				{
					// return true for drafted submissions
					return true;
				}
				else
					return false;
			}
		}
		catch (UserNotDefinedException e)
		{
			// cannot find user
			M_log.warn(" canSubmit(String, Assignment) " + e.getMessage() + " assignment ref=" + a.getReference());
			return false;
		}
	}
	
	public Integer getScaleFactor() {		
		Integer decimals = m_serverConfigurationService.getInt("assignment.grading.decimals", AssignmentConstants.DEFAULT_DECIMAL_POINT);
		
		return (int)Math.pow(10.0, decimals);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Assignment Implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAssignment implements Assignment
	{
		protected ResourcePropertiesEdit m_properties;

		protected String m_id;

		protected String m_assignmentContent;

		protected String m_title;

		protected String m_context;

		protected String m_section;

		protected Time m_visibleTime;

		protected Time m_openTime;

		protected Time m_dueTime;

		protected Time m_closeTime;

		protected Time m_dropDeadTime;

		protected List m_authors;

		protected boolean m_draft;

		protected boolean m_hideDueDate;
		
                protected boolean m_group;
                
		protected int m_position_order;

		/** The Collection of groups (authorization group id strings). */
		protected Collection m_groups = new ArrayList();

		/** The assignment access. */
		protected AssignmentAccess m_access = AssignmentAccess.SITE;
		
		protected boolean m_allowPeerAssessment;

		protected Time m_peerAssessmentPeriodTime;

		protected boolean m_peerAssessmentAnonEval;

		protected boolean m_peerAssessmentStudentViewReviews;

		protected int m_peerAssessmentNumReviews;

		protected String m_peerAssessmentInstructions;

		/**
		 * constructor
		 */
		public BaseAssignment()
		{
			m_properties = new BaseResourcePropertiesEdit();
		}// constructor
		
		/**
		 * Copy constructor
		 */
		public BaseAssignment(Assignment assignment)
		{
			setAll(assignment);
		}// copy constructor

		/**
		 * Constructor used in addAssignment
		 */
		public BaseAssignment(String id, String context)
		{
			m_properties = new BaseResourcePropertiesEdit();
			addLiveProperties(m_properties);
			m_id = id;
			m_assignmentContent = "";
			m_title = "";
			m_context = context;
			m_section = "";
			m_authors = new ArrayList();
			m_draft = true;
			m_hideDueDate = false;
			m_groups = new ArrayList();
			m_position_order = 0;
			m_allowPeerAssessment = false;
			m_peerAssessmentPeriodTime = null;
			m_peerAssessmentAnonEval = false;
			m_peerAssessmentStudentViewReviews = false;
			m_peerAssessmentNumReviews = 0;
			m_peerAssessmentInstructions = null;
		}

		/**
		 * Reads the Assignment's attribute values from xml.
		 * 
		 * @param s -
		 *        Data structure holding the xml info.
		 */
		public BaseAssignment(Element el)
		{
			M_log.debug(" BASE ASSIGNMENT : ENTERING STORAGE CONSTRUCTOR");

			m_properties = new BaseResourcePropertiesEdit();

			int numAttributes = 0;
			String intString = null;
			String attributeString = null;
			String tempString = null;

			m_id = el.getAttribute("id");
			
				M_log.debug(" BASE ASSIGNMENT : STORAGE CONSTRUCTOR : ASSIGNMENT ID : " + m_id);
			m_title = el.getAttribute("title");
			m_section = el.getAttribute("section");
			m_draft = getBool(el.getAttribute("draft"));
			m_hideDueDate = getBool(el.getAttribute("hideduedate"));
			
			m_group = getBool(el.getAttribute("group"));

				M_log.debug(" BASE ASSIGNMENT : STORAGE CONSTRUCTOR : READ THROUGH REG ATTS");

			m_assignmentContent = el.getAttribute("assignmentcontent");
			
				M_log.debug(" BASE ASSIGNMENT : STORAGE CONSTRUCTOR : CONTENT ID : "
						+ m_assignmentContent);

			m_openTime = getTimeObject(el.getAttribute("opendate"));
			m_dueTime = getTimeObject(el.getAttribute("duedate"));
			m_visibleTime = getTimeObject(el.getAttribute("visibledate"));
			m_dropDeadTime = getTimeObject(el.getAttribute("dropdeaddate"));
			m_closeTime = getTimeObject(el.getAttribute("closedate"));
			m_context = el.getAttribute("context");
			m_position_order = 0; // prevents null pointer if there is no position_order defined as well as helps with the sorting
			try
			{
				m_position_order = Long.valueOf(el.getAttribute("position_order")).intValue();
			}
			catch (Exception e)
			{
				M_log.warn(": BaseAssignment(Element) " + e.getMessage());
			}
			m_allowPeerAssessment = getBool(el.getAttribute("allowpeerassessment"));
			m_peerAssessmentPeriodTime = getTimeObject(el.getAttribute("peerassessmentperiodtime"));
			m_peerAssessmentAnonEval = getBool(el.getAttribute("peerassessmentanoneval"));
			m_peerAssessmentStudentViewReviews = getBool(el.getAttribute("peerassessmentstudentviewreviews"));
			String numReviews = el.getAttribute("peerassessmentnumreviews");
			m_peerAssessmentNumReviews = 0;
			if(numReviews != null && !"".equals(numReviews)){
				try{
					m_peerAssessmentNumReviews = Integer.parseInt(numReviews);
				}catch(Exception e){}
			}
			m_peerAssessmentInstructions = el.getAttribute("peerassessmentinstructions");


			// READ THE AUTHORS
			m_authors = new ArrayList();
			intString = el.getAttribute("numberofauthors");
			
				M_log.debug(" BASE ASSIGNMENT : STORAGE CONSTRUCTOR : number of authors : " + intString);
			try
			{
				numAttributes = Integer.parseInt(intString);

				for (int x = 0; x < numAttributes; x++)
				{
					
						M_log.debug(" BASE ASSIGNMENT : STORAGE CONSTRUCTOR : reading author # " + x);
					attributeString = "author" + x;
					tempString = el.getAttribute(attributeString);

					if (tempString != null)
					{
						
							M_log.debug(" BASE ASSIGNMENT : STORAGE CONSTRUCTOR : adding author # " + x
									+ " id :  " + tempString);
						m_authors.add(tempString);
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(" BASE ASSIGNMENT : STORAGE CONSTRUCTOR : Exception reading authors : " + e);
			}

			// READ THE PROPERTIES AND INSTRUCTIONS
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
				}

				// look for an group
				else if (element.getTagName().equals("group"))
				{
					m_groups.add(element.getAttribute("authzGroup"));
				}
			}

			// extract access
			AssignmentAccess access = AssignmentAccess.fromString(el.getAttribute("access"));
			if (access != null)
			{
				m_access = access;
			}

			M_log.debug(" BASE ASSIGNMENT : LEAVING STORAGE CONSTRUCTOR");

		}// storage constructor

		/**
		 * @param services
		 * @return
		 */
		public ContentHandler getContentHandler(Map<String, Object> services)
		{
			final Entity thisEntity = this;
			return new DefaultEntityHandler()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.sakaiproject.util.DefaultEntityHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
				{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if ("assignment".equals(qName) && entity == null)
						{
							m_id = attributes.getValue("id");
							m_properties = new BaseResourcePropertiesEdit();
							
							int numAttributes = 0;
							String intString = null;
							String attributeString = null;
							String tempString = null;

							m_title = attributes.getValue("title");
							m_section = attributes.getValue("section");
							m_draft = getBool(attributes.getValue("draft"));
							m_hideDueDate = getBool(attributes.getValue("hideduedate"));
							
                                                        m_group = getBool(attributes.getValue("group"));
                                                        
								M_log.debug(this + " getContentHandler: READ THROUGH REG ATTS");

							m_assignmentContent = attributes.getValue("assignmentcontent");
							
								M_log.debug(this + " getContentHandler: STORAGE CONSTRUCTOR : CONTENT ID : "
										+ m_assignmentContent);

							m_openTime = getTimeObject(attributes.getValue("opendate"));
							m_dueTime = getTimeObject(attributes.getValue("duedate"));
							m_visibleTime = getTimeObject(attributes.getValue("visibledate"));
							m_dropDeadTime = getTimeObject(attributes.getValue("dropdeaddate"));
							m_closeTime = getTimeObject(attributes.getValue("closedate"));
							m_context = attributes.getValue("context");
							try
							{
								m_position_order = NumberUtils.toInt(attributes.getValue("position_order"));
							}
							catch (Exception e)
							{
								m_position_order = 0; // prevents null pointer if there is no position_order defined as well as helps with the sorting
							}
							
							m_allowPeerAssessment = getBool(attributes.getValue("allowpeerassessment"));
							m_peerAssessmentPeriodTime = getTimeObject(attributes.getValue("peerassessmentperiodtime"));
							m_peerAssessmentAnonEval = getBool(attributes.getValue("peerassessmentanoneval"));
							m_peerAssessmentStudentViewReviews = getBool(attributes.getValue("peerassessmentstudentviewreviews"));
							String numReviews = attributes.getValue("peerassessmentnumreviews");
							m_peerAssessmentNumReviews = 0;
							if(numReviews != null && !"".equals(numReviews)){
								try{
									m_peerAssessmentNumReviews = Integer.parseInt(numReviews);
								}catch(Exception e){}
							}
							m_peerAssessmentInstructions = attributes.getValue("peerassessmentinstructions");


							// READ THE AUTHORS
							m_authors = new ArrayList();
							intString = attributes.getValue("numberofauthors");
							try
							{
								numAttributes = Integer.parseInt(intString);

								for (int x = 0; x < numAttributes; x++)
								{
									attributeString = "author" + x;
									tempString = attributes.getValue(attributeString);

									if (tempString != null)
									{
										m_authors.add(tempString);
									}
								}
							}
							catch (Exception e)
							{
								M_log.warn(" BASE ASSIGNMENT getContentHandler startElement : Exception reading authors : " + e.toString());
							}

							// extract access
							AssignmentAccess access = AssignmentAccess.fromString(attributes.getValue("access"));
							if (access != null)
							{
								m_access = access;
							}
							
							entity = thisEntity;
						}
						else if (GROUP_LIST.equals(qName))
						{
							String groupRef = attributes.getValue(GROUP_NAME);
							if (groupRef != null)
							{
								m_groups.add(groupRef);
							}
						}
						else
						{
							M_log.debug(this + " BaseAssignment getContentHandler Unexpected Element " + qName);
						}

					}
				}
			};
		}
		
		/**
		 * Takes the Assignment's attribute values and puts them into the xml document.
		 * 
		 * @param s -
		 *        Data structure holding the object to be stored.
		 * @param doc -
		 *        The xml document.
		 */
		public Element toXml(Document doc, Stack stack)
		{
			M_log.debug(this + " BASE ASSIGNMENT : ENTERING TOXML");

			Element assignment = doc.createElement("assignment");

			if (stack.isEmpty())
			{
				doc.appendChild(assignment);
			}
			else
			{
				((Element) stack.peek()).appendChild(assignment);
			}
			stack.push(assignment);

			// SET ASSIGNMENT ATTRIBUTES
			String numItemsString = null;
			String attributeString = null;
			String itemString = null;
			
			// SAK-13408 -The XML implementation in Websphere throws an LSException if the
			// attribute is null, while in Tomcat it assumes an empty string. The following
			// sets the attribute to an empty string if the value is null.
			assignment.setAttribute("id", m_id == null ? "" : m_id);
			assignment.setAttribute("title", m_title == null ? "" : m_title);
			assignment.setAttribute("section", m_section == null ? "" : m_section);
			assignment.setAttribute("context", m_context == null ? "" : m_context);
			assignment.setAttribute("assignmentcontent", m_assignmentContent == null ? "" : m_assignmentContent);
			assignment.setAttribute("draft", getBoolString(m_draft));
                        assignment.setAttribute("group", getBoolString(m_group));
			assignment.setAttribute("hideduedate", getBoolString(m_hideDueDate));
			assignment.setAttribute("opendate", getTimeString(m_openTime));
			assignment.setAttribute("duedate", getTimeString(m_dueTime));
			assignment.setAttribute("visibledate", getTimeString(m_visibleTime));
			assignment.setAttribute("dropdeaddate", getTimeString(m_dropDeadTime));
			assignment.setAttribute("closedate", getTimeString(m_closeTime));
			assignment.setAttribute("position_order", Long.valueOf(m_position_order).toString().trim());
			assignment.setAttribute("allowpeerassessment", getBoolString(m_allowPeerAssessment));
			assignment.setAttribute("peerassessmentperiodtime", getTimeString(m_peerAssessmentPeriodTime));
			assignment.setAttribute("peerassessmentanoneval", getBoolString(m_peerAssessmentAnonEval));
			assignment.setAttribute("peerassessmentstudentviewreviews", getBoolString(m_peerAssessmentStudentViewReviews));
			assignment.setAttribute("peerassessmentnumreviews", "" + m_peerAssessmentNumReviews);
			assignment.setAttribute("peerassessmentinstructions", m_peerAssessmentInstructions);

			
				M_log.debug(this + " BASE ASSIGNMENT : TOXML : saved regular properties");

			// SAVE THE AUTHORS
			numItemsString = "" + m_authors.size();
			
				M_log.debug(this + " BASE ASSIGNMENT : TOXML : saving " + numItemsString + " authors");

			assignment.setAttribute("numberofauthors", numItemsString);
			for (int x = 0; x < m_authors.size(); x++)
			{
				attributeString = "author" + x;
				itemString = (String) m_authors.get(x);
				if (itemString != null)
				{
					assignment.setAttribute(attributeString, itemString);
					
						M_log.debug(this + " BASE ASSIGNMENT : TOXML : saving author : " + itemString);
				}
			}

			// add groups
			if ((m_groups != null) && (m_groups.size() > 0))
			{
				for (Iterator i = m_groups.iterator(); i.hasNext();)
				{
					String group = (String) i.next();
					Element sect = doc.createElement("group");
					assignment.appendChild(sect);
					sect.setAttribute("authzGroup", group);
				}
			}

			// add access
			assignment.setAttribute("access", m_access.toString());

			// SAVE THE PROPERTIES
			m_properties.toXml(doc, stack);
			M_log.debug(this + " BASE ASSIGNMENT : TOXML : SAVED PROPERTIES");
			stack.pop();

			M_log.debug("ASSIGNMENT : BASE ASSIGNMENT : LEAVING TOXML");

			return assignment;

		}// toXml

		protected void setAll(Assignment assignment)
		{
			if (assignment != null)
			{
				m_id = assignment.getId();
				m_assignmentContent = assignment.getContentReference();
				m_authors = assignment.getAuthors();
				m_title = assignment.getTitle();
				m_context = assignment.getContext();
				m_section = assignment.getSection();
				m_openTime = assignment.getOpenTime();
				m_dueTime = assignment.getDueTime();
				m_visibleTime = assignment.getVisibleTime();
				m_closeTime = assignment.getCloseTime();
				m_dropDeadTime = assignment.getDropDeadTime();
				m_draft = assignment.getDraft();
                                m_group = assignment.isGroup();
				m_position_order = 0;
				try
				{
					m_position_order = assignment.getPosition_order();
				}
				catch (Exception e)
				{
					M_log.warn(": setAll(Assignment) get position order " + e.getMessage());
				}
				m_properties = new BaseResourcePropertiesEdit();
				m_properties.addAll(assignment.getProperties());
				m_groups = assignment.getGroups();
				m_access = assignment.getAccess();
				m_allowPeerAssessment = assignment.getAllowPeerAssessment();
				m_peerAssessmentPeriodTime = assignment.getPeerAssessmentPeriod();
				m_peerAssessmentAnonEval = assignment.getPeerAssessmentAnonEval();
				m_peerAssessmentStudentViewReviews = assignment.getPeerAssessmentStudentViewReviews();
				m_peerAssessmentNumReviews = assignment.getPeerAssessmentNumReviews();
				m_peerAssessmentInstructions = assignment.getPeerAssessmentInstructions();

			}
		}

		public String getId()
		{
			return m_id;
		}

		/**
		 * Access the URL which can be used to access the resource.
		 * 
		 * @return The URL which can be used to access the resource.
		 */
		public String getUrl()
		{
			return getAccessPoint(false) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + m_context + Entity.SEPARATOR + m_id;

		} // getUrl

		/**
		 * Access the internal reference which can be used to access the resource from within the system.
		 * 
		 * @return The the internal reference which can be used to access the resource from within the system.
		 */
		public String getReference()
		{
			return assignmentReference(m_context, m_id);

		} // getReference

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * Access the resource's properties.
		 * 
		 * @return The resource's properties.
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;
		}

		/**
		 * Access the list of authors.
		 * 
		 * @return FlexStringArray of user ids.
		 */
		public List getAuthors()
		{
			return m_authors;
		}

		/**
		 * Add an author to the author list.
		 * 
		 * @param author -
		 *        The User to add to the author list.
		 */
		public void addAuthor(User author)
		{
			if (author != null) m_authors.add(author.getId());
		}

		/**
		 * Remove an author from the author list.
		 * 
		 * @param author -
		 *        the User to remove from the author list.
		 */
		public void removeAuthor(User author)
		{
			if (author != null) m_authors.remove(author.getId());
		}

		/**
		 * Access the creator of this object.
		 * 
		 * @return String The creator's user id.
		 */
		public String getCreator()
		{
			return m_properties.getProperty(ResourceProperties.PROP_CREATOR);
		}

		/**
		 * Access the person of last modificaiton
		 * 
		 * @return the User's Id
		 */
		public String getAuthorLastModified()
		{
			return m_properties.getProperty(ResourceProperties.PROP_MODIFIED_BY);
		}

		/**
		 * Access the title.
		 * 
		 * @return The Assignment's title.
		 */
		public String getTitle()
		{
			return m_title;
		}
		
		public Time getPeerAssessmentPeriod()
		{
			return m_peerAssessmentPeriodTime;
		}

		public boolean getPeerAssessmentAnonEval(){
			return m_peerAssessmentAnonEval;
		}

		public boolean getPeerAssessmentStudentViewReviews(){
			return m_peerAssessmentStudentViewReviews;
		}

		public int getPeerAssessmentNumReviews(){
			return m_peerAssessmentNumReviews;
		}

		public String getPeerAssessmentInstructions(){
			return m_peerAssessmentInstructions;
		}

		public boolean getAllowPeerAssessment()
		{
			return m_allowPeerAssessment;
		}

		/**
		 * peer assessment is set for this assignment and the current time 
		 * falls between the assignment close time and the peer asseessment period time
		 * @return
		 */
		public boolean isPeerAssessmentOpen(){
			if(getAllowPeerAssessment()){
				Time now = TimeService.newTime();
				return now.before(getPeerAssessmentPeriod()) && now.after(getCloseTime());
			}else{
				return false;
			}
		}
		
		/**
		 * peer assessment is set for this assignment but the close time hasn't passed
		 * @return
		 */
		public boolean isPeerAssessmentPending(){
			if(getAllowPeerAssessment()){
				Time now = TimeService.newTime();
				return now.before(getCloseTime());
			}else{
				return false;
			}
		}
		/**
		 * peer assessment is set for this assignment but the current time is passed 
		 * the peer assessment period
		 * @return
		 */
		public boolean isPeerAssessmentClosed(){
			if(getAllowPeerAssessment()){
				Time now = TimeService.newTime();
				return now.after(getPeerAssessmentPeriod());
			}else{
				return false;
			}
		}
		
		/**
		 * @inheritDoc
		 */
		public String getStatus()
		{
			Time currentTime = TimeService.newTime();
			
			if (this.getDraft())
				return rb.getString("gen.dra1");
			else if (this.getOpenTime().after(currentTime))
				return rb.getString("gen.notope");
			else if (this.getDueTime().after(currentTime))
				return rb.getString("gen.open");
			else if ((this.getCloseTime() != null) && (this.getCloseTime().before(currentTime)))
				return rb.getString("gen.closed");
			else
				return rb.getString("gen.due");
		}

		/**
		 * Access the time that this object was created.
		 * 
		 * @return The Time object representing the time of creation.
		 */
		public Time getTimeCreated()
		{
			try
			{
				return m_properties.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
			}
			catch (EntityPropertyNotDefinedException e)
			{
				M_log.warn(":getTimeCreated() no time property defined " + e.getMessage());
			}
			catch (EntityPropertyTypeException e)
			{
				M_log.warn(":getTimeCreated() no time property defined " + e.getMessage());
			}
			return null;
		}

		/**
		 * Access the time of last modificaiton.
		 * 
		 * @return The Time of last modification.
		 */
		public Time getTimeLastModified()
		{
			try
			{
				return m_properties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
			}
			catch (EntityPropertyNotDefinedException e)
			{
				M_log.warn(":getTimeLastModified() no time property defined " + e.getMessage());
			}
			catch (EntityPropertyTypeException e)
			{
				M_log.warn(":getTimeLastModified() no time property defined " + e.getMessage());
			}
			return null;
		}

		/**
		 * Access the AssignmentContent of this Assignment.
		 * 
		 * @return The Assignment's AssignmentContent.
		 */
		public AssignmentContent getContent()
		{
			AssignmentContent retVal = null;
			if (m_assignmentContent != null)
			{
				try
				{
					retVal = getAssignmentContent(m_assignmentContent);
				}
				catch (Exception e)
				{
					M_log.warn(":getContent() " + e.getMessage());
				}
			}

			return retVal;
		}

		/**
		 * Access the reference of the AssignmentContent of this Assignment.
		 * 
		 * @return The Assignment's reference.
		 */
		public String getContentReference()
		{
			return m_assignmentContent;
		}

		/**
		 * Access the id of the Assignment's group.
		 * 
		 * @return The id of the group for which this Assignment is designed.
		 */
		public String getContext()
		{
			return m_context;
		}

		/**
		 * Access the section info
		 * 
		 * @return The section String
		 */
		public String getSection()
		{
			return m_section;
		}

		/**
		 * Access the first time at which the assignment can be viewed; may be null.
		 * 
		 * @return The Time at which the assignment is due, or null if unspecified.
		 */
		public Time getOpenTime()
		{
			return m_openTime;
		}

	  /**
		* @inheritDoc
		*/
		public String getOpenTimeString()
		{
			if ( m_openTime == null )
				return "";
			else
				return m_openTime.toStringLocalFull();
		}

		/**
		 * Access the time at which the assignment is due; may be null.
		 * 
		 * @return The Time at which the Assignment is due, or null if unspecified.
		 */
		public Time getDueTime()
		{
			return m_dueTime;
		}

	  /**
		 * Access the time at which the assignment is visible; may be null.
		 *
		 * @return The Time at which the Assignment is visible, or null if unspecified.
		 */
		public Time getVisibleTime()
		{
			return m_visibleTime;
		}

	  /**
		* @inheritDoc
		*/
		public String getDueTimeString()
		{
			if ( m_dueTime == null )
				return "";
			else
				return m_dueTime.toStringLocalFull();
		}

		public String getVisibleTimeString()
		{
			if ( m_visibleTime == null )
				return "";
			else
				return m_visibleTime.toStringLocalFull();
		}

		/**
		 * Access the drop dead time after which responses to this assignment are considered late; may be null.
		 * 
		 * @return The Time object representing the drop dead time, or null if unspecified.
		 */
		public Time getDropDeadTime()
		{
			return m_dropDeadTime;
		}

	  /**
		* @inheritDoc
		*/
		public String getDropDeadTimeString()
		{
			if ( m_dropDeadTime == null )
				return "";
			else
				return m_dropDeadTime.toStringLocalFull();
		}

		/**
		 * Access the close time after which this assignment can no longer be viewed, and after which submissions will not be accepted. May be null.
		 * 
		 * @return The Time after which the Assignment is closed, or null if unspecified.
		 */
		public Time getCloseTime()
		{
			if (m_closeTime == null)
			{
				m_closeTime = m_dueTime;
			}
			return m_closeTime;
		}

	  /**
		* @inheritDoc
		*/
		public String getCloseTimeString()
		{
			if ( m_closeTime == null )
				return "";
			else
				return m_closeTime.toStringLocalFull();
		}

		/**
		 * Get whether this is a draft or final copy.
		 * 
		 * @return True if this is a draft, false if it is a final copy.
		 */
		public boolean getDraft()
		{
			return m_draft;
		}
	
		public boolean getHideDueDate()
		{
			return m_hideDueDate;
		}
	
                public boolean isGroup()
                {
                        return m_group;
                }
                
		/**
		 * Access the position order.
		 * 
		 * @return The Assignment's positionorder.
		 */
		public int getPosition_order()
		{
			return m_position_order;
		}

		/**
		 * @inheritDoc
		 */
		public Collection getGroups()
		{
			return new ArrayList(m_groups);
		}

		/**
		 * @inheritDoc
		 */
		public AssignmentAccess getAccess()
		{
			return m_access;
		}

		/**
		 * Are these objects equal? If they are both Assignment objects, and they have matching id's, they are.
		 * 
		 * @return true if they are equal, false if not.
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Assignment)) return false;
			return ((Assignment) obj).getId().equals(getId());

		} // equals

		/**
		 * Make a hash code that reflects the equals() logic as well. We want two objects, even if different instances, if they have the same id to hash the same.
		 */
		public int hashCode()
		{
			return getId().hashCode();

		} // hashCode

		/**
		 * Compare this object with the specified object for order.
		 * 
		 * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
		 */
		public int compareTo(Object obj)
		{
			if (!(obj instanceof Assignment)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// start the compare by comparing their sort names
			int compare = getTitle().compareTo(((Assignment) obj).getTitle());

			// if these are the same
			if (compare == 0)
			{
				// sort based on (unique) id
				compare = getId().compareTo(((Assignment) obj).getId());
			}

			return compare;

		} // compareTo

	} // BaseAssignment

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * <p>
	 * BaseAssignmentEdit is an implementation of the CHEF AssignmentEdit object.
	 * </p>
	 * 
	 * @author University of Michigan, CHEF Software Development Team
	 */
	public class BaseAssignmentEdit extends BaseAssignment implements AssignmentEdit, SessionBindingListener
	{
		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/**
		 * Construct from another Assignment object.
		 * 
		 * @param Assignment
		 *        The Assignment object to use for values.
		 */
		public BaseAssignmentEdit(Assignment assignment)
		{
			super(assignment);

		} // BaseAssignmentEdit

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The assignment id.
		 */
		public BaseAssignmentEdit(String id, String context)
		{
			super(id, context);

		} // BaseAssignmentEdit

		/**
		 * Construct from information in XML.
		 * 
		 * @param el
		 *        The XML DOM Element definining the Assignment.
		 */
		public BaseAssignmentEdit(Element el)
		{
			super(el);

		} // BaseAssignmentEdit

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}

		} // finalize

		/**
		 * Set the title.
		 * 
		 * @param title -
		 *        The Assignment's title.
		 */
		public void setTitle(String title)
		{
			m_title = title;
		}

		public void setPeerAssessmentPeriod(Time time)
		{
			m_peerAssessmentPeriodTime = time;
		}

		public void setAllowPeerAssessment(boolean allow)
		{
			m_allowPeerAssessment = allow;
		}

		public void setPeerAssessmentAnonEval(boolean anonEval){
			m_peerAssessmentAnonEval = anonEval;
		}

		public void setPeerAssessmentStudentViewReviews(boolean studentViewReviews){
			m_peerAssessmentStudentViewReviews = studentViewReviews;
		}

		public void setPeerAssessmentNumReviews(int numReviews){
			m_peerAssessmentNumReviews = numReviews;
		}

		public void setPeerAssessmentInstructions(String instructions){
			m_peerAssessmentInstructions = instructions;
		}

		/**
		 * Set the reference of the AssignmentContent of this Assignment.
		 * 
		 * @param String -
		 *        the reference of the AssignmentContent.
		 */
		public void setContentReference(String contentReference)
		{
			if (contentReference != null) m_assignmentContent = contentReference;
		}

		/**
		 * Set the AssignmentContent of this Assignment.
		 * 
		 * @param content -
		 *        the Assignment's AssignmentContent.
		 */
		public void setContent(AssignmentContent content)
		{
			if (content != null) m_assignmentContent = content.getReference();
		}

		/**
		 * Set the context at the time of creation.
		 * 
		 * @param context -
		 *        the context string.
		 */
		public void setContext(String context)
		{
			m_context = context;
		}

		/**
		 * Set the section info
		 * 
		 * @param sectionId -
		 *        The section id
		 */
		public void setSection(String sectionId)
		{
			m_section = sectionId;
		}

		/**
		 * Set the first time at which the assignment can be viewed; may be null.
		 * 
		 * @param opentime -
		 *        The Time at which the Assignment opens.
		 */
		public void setOpenTime(Time opentime)
		{
			m_openTime = opentime;
		}

		/**
		 * Set the time at which the assignment is due; may be null.
		 * 
		 * @param dueTime -
		 *        The Time at which the Assignment is due.
		 */
		public void setDueTime(Time duetime)
		{
			m_dueTime = duetime;
		}

		/**
		 * Set the time at which the assignment is visible; may be null.
		*
		 * @param visibleTime -
		 * 	The Time at which the Assignment is visible
		 */
		public void setVisibleTime(Time visibletime)
		{
			m_visibleTime = visibletime;
		}

		/**
		 * Set the drop dead time after which responses to this assignment are considered late; may be null.
		 * 
		 * @param dropdeadtime -
		 *        The Time object representing the drop dead time.
		 */
		public void setDropDeadTime(Time dropdeadtime)
		{
			m_dropDeadTime = dropdeadtime;
		}

		/**
		 * Set the time after which this assignment can no longer be viewed, and after which submissions will not be accepted. May be null.
		 * 
		 * @param closetime -
		 *        The Time after which the Assignment is closed, or null if unspecified.
		 */
		public void setCloseTime(Time closetime)
		{
			m_closeTime = closetime;
		}

		/**
		 * Set whether this is a draft or final copy.
		 * 
		 * @param draft -
		 *        true if this is a draft, false if it is a final copy.
		 */
		public void setDraft(boolean draft)
		{
			m_draft = draft;
		}

        public void setHideDueDate (boolean hide)
        {
            m_hideDueDate = hide;
        }
		
                public void setGroup(boolean group) {
                        m_group = group;
                }
                
		/**
		 * Set the position order field for the an assignment.
		 * 
		 * @param position_order - 
		 *        The position order.
		 */
		public void setPosition_order(int position_order)
		{
			m_position_order = position_order;
		}

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The user object to take values from.
		 */
		protected void set(Assignment assignment)
		{
			setAll(assignment);

		} // set

		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * Access the resource's properties for modification
		 * 
		 * @return The resource's properties.
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;

		} // getPropertiesEdit

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;

		} // activate

		/**
		 * Check to see if the edit is still active, or has already been closed.
		 * 
		 * @return true if the edit is active, false if it's been closed.
		 */
		public boolean isActiveEdit()
		{
			return m_active;

		} // isActiveEdit

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;

		} // closeEdit

		/******************************************************************************************************************************************************************************************************************************************************
		 * Group awareness implementation
		 *****************************************************************************************************************************************************************************************************************************************************/
		/**
		 * @inheritDoc
		 */
		public void setAccess(AssignmentAccess access)
		{
			m_access = access;
		}

		/**
		 * @inheritDoc
		 */
		public void setGroupAccess(Collection groups) throws PermissionException
		{	
			// convenience (and what else are we going to do?)
			if ((groups == null) || (groups.size() == 0))
			{
				clearGroupAccess();
				return;
			}
			
			// is there any change?  If we are already grouped, and the group list is the same, ignore the call
			if ((m_access == AssignmentAccess.GROUPED) && (EntityCollections.isEqualEntityRefsToEntities(m_groups, groups))) return;
			
			// there should not be a case where there's no context
			if (m_context == null)
			{
				M_log.warn(" setGroupAccess() called with null context: " + getReference());
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), "access:site", getReference());
			}

			// isolate any groups that would be removed or added
			Collection addedGroups = new ArrayList();
			Collection removedGroups = new ArrayList();
			EntityCollections.computeAddedRemovedEntityRefsFromNewEntitiesOldRefs(addedGroups, removedGroups, groups, m_groups);

			// verify that the user has permission to remove
			if (removedGroups.size() > 0)
			{
				// the Group objects the user has remove permission
				Collection allowedGroups = getGroupsAllowRemoveAssignment(m_context);

				for (Iterator i = removedGroups.iterator(); i.hasNext();)
				{
					String ref = (String) i.next();

					// is ref a group the user can remove from?
					if (!EntityCollections.entityCollectionContainsRefString(allowedGroups, ref))
					{
						throw new PermissionException(SessionManager.getCurrentSessionUserId(), "access:group:remove", ref);
					}
				}
			}
			
			// verify that the user has permission to add in those contexts
			if (addedGroups.size() > 0)
			{
				// the Group objects the user has add permission
				Collection allowedGroups = getGroupsAllowAddAssignment(m_context);

				for (Iterator i = addedGroups.iterator(); i.hasNext();)
				{
					String ref = (String) i.next();

					// is ref a group the user can remove from?
					if (!EntityCollections.entityCollectionContainsRefString(allowedGroups, ref))
					{
						throw new PermissionException(SessionManager.getCurrentSessionUserId(), "access:group:add", ref);
					}
				}
			}
			
			// we are clear to perform this
			m_access = AssignmentAccess.GROUPED;
			EntityCollections.setEntityRefsFromEntities(m_groups, groups);
		}

		/**
		 * @inheritDoc
		 */
		public void clearGroupAccess() throws PermissionException
		{			
			// is there any change?  If we are already site, ignore the call
			if (m_access == AssignmentAccess.SITE)
			{
				m_groups.clear();
				return;
			}

			if (m_context == null)
			{
				// there should not be a case where there's no context
				M_log.warn(" clearGroupAccess() called with null context. " + getReference());
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), "access:site", getReference());
			}
			else
			{
				// verify that the user has permission to add in the site context
				if (!allowAddAssignment(m_context))
				{
					throw new PermissionException(SessionManager.getCurrentSessionUserId(), "access:site", getReference());
				}
			}

			// we are clear to perform this
			m_access = AssignmentAccess.SITE;
			m_groups.clear();
			
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			M_log.debug(this + " BaseAssignmentEdit valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}

		} // valueUnbound

	} // BaseAssignmentEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentContent Implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAssignmentContent implements AssignmentContent
	{
		protected ResourcePropertiesEdit m_properties;

		protected String m_id;

		protected String m_context;

		protected List m_attachments;

		protected List m_authors;

		protected String m_title;

		protected String m_instructions;

		protected int m_honorPledge;

		protected int m_typeOfSubmission;

		protected int m_typeOfGrade;

		protected int m_maxGradePoint;
		
		protected int m_factor;

		protected boolean m_groupProject;

		protected boolean m_individuallyGraded;

		protected boolean m_releaseGrades;

		protected boolean m_hideDueDate;
		
		protected boolean m_allowAttachments;

		protected boolean m_allowReviewService;
		
		protected boolean m_allowStudentViewReport;

		String m_submitReviewRepo;
		String m_generateOriginalityReport;
		boolean m_checkTurnitin = true;
		boolean m_checkInternet = true;
		boolean m_checkPublications = true;
		boolean m_checkInstitution = true;
		boolean m_excludeBibliographic = true;
		boolean m_excludeQuoted = true;
		int m_excludeType = 0;
		int m_excludeValue = 1;
		
		protected Time m_timeCreated;

		protected Time m_timeLastModified;
		
		/**
		 * constructor
		 */
		public BaseAssignmentContent()
		{
			m_properties = new BaseResourcePropertiesEdit();
		}// constructor

		/**
		 * Copy constructor.
		 */
		public BaseAssignmentContent(AssignmentContent content)
		{
			setAll(content);
		}

		/**
		 * Constructor used in addAssignmentContent.
		 */
		public BaseAssignmentContent(String id, String context)
		{
			m_id = id;
			m_context = context;
			m_properties = new BaseResourcePropertiesEdit();
			addLiveProperties(m_properties);
			m_authors = new ArrayList();
			m_attachments = m_entityManager.newReferenceList();
			m_title = "";
			m_instructions = "";
			m_honorPledge = Assignment.HONOR_PLEDGE_NOT_SET;
			m_typeOfSubmission = Assignment.ASSIGNMENT_SUBMISSION_TYPE_NOT_SET;
			m_typeOfGrade = Assignment.GRADE_TYPE_NOT_SET;
			m_maxGradePoint = 0;
			m_factor = getScaleFactor();
			m_timeCreated = TimeService.newTime();
			m_timeLastModified = TimeService.newTime();
		}

		/**
		 * Reads the AssignmentContent's attribute values from xml.
		 * 
		 * @param s -
		 *        Data structure holding the xml info.
		 */
		public BaseAssignmentContent(Element el)
		{
			int numAttributes = 0;
			String intString = null;
			String attributeString = null;
			String tempString = null;
			Reference tempReference = null;
			M_log.debug(" BaseAssignmentContent : Entering read");

			m_id = el.getAttribute("id");
			m_context = el.getAttribute("context");
			m_title = el.getAttribute("title");
			m_groupProject = getBool(el.getAttribute("groupproject"));
			m_individuallyGraded = getBool(el.getAttribute("indivgraded"));
			m_releaseGrades = getBool(el.getAttribute("releasegrades"));
			m_allowAttachments = getBool(el.getAttribute("allowattach"));
			m_hideDueDate = getBool(el.getAttribute("hideduedate"));
			m_allowReviewService = getBool(el.getAttribute("allowreview"));
			m_allowStudentViewReport = getBool(el.getAttribute("allowstudentview"));
			m_submitReviewRepo = el.getAttribute("submitReviewRepo");
			m_generateOriginalityReport = el.getAttribute("generateOriginalityReport");
			m_checkTurnitin = getBool(el.getAttribute("checkTurnitin"));
			m_checkInternet = getBool(el.getAttribute("checkInternet"));
			m_checkPublications = getBool(el.getAttribute("checkPublications"));
			m_checkInstitution = getBool(el.getAttribute("checkInstitution"));
			m_excludeBibliographic = getBool(el.getAttribute("excludeBibliographic"));
			m_excludeQuoted = getBool(el.getAttribute("excludeQuoted"));
			String excludeTypeStr = el.getAttribute("excludeType");
			try{
				m_excludeType = Integer.parseInt(excludeTypeStr);
				if(m_excludeType != 0 && m_excludeType != 1 && m_excludeType != 2){
					m_excludeType = 0;
				}
			}catch (Exception e) {
				m_excludeType = 0;
			}
			String excludeValueStr = el.getAttribute("excludeValue");
			try{
				m_excludeValue = Integer.parseInt(excludeValueStr);
				if(m_excludeValue < 0 || m_excludeValue > 100){
					m_excludeValue = 1;
				}
			}catch (Exception e) {
				m_excludeValue = 1;
			}
			m_timeCreated = getTimeObject(el.getAttribute("datecreated"));
			m_timeLastModified = getTimeObject(el.getAttribute("lastmod"));

			m_instructions = FormattedText.decodeFormattedTextAttribute(el, "instructions");

			try
			{
				m_honorPledge = Integer.parseInt(el.getAttribute("honorpledge"));
			}
			catch (Exception e)
			{
				M_log.warn(" BaseAssignmentContent Exception parsing honor pledge int from xml file string : " + e);
			}

			try
			{
				m_typeOfSubmission = Integer.parseInt(el.getAttribute("submissiontype"));
			}
			catch (Exception e)
			{
				M_log.warn(" BaseAssignmentContent Exception parsing submission type int from xml file string : " + e);
			}

			try
			{
				m_typeOfGrade = Integer.parseInt(el.getAttribute("typeofgrade"));
			}
			catch (Exception e)
			{
				M_log.warn(" BaseAssignmentContent Exception parsing grade type int from xml file string : " + e);
			}

			try
			{
				String factor = StringUtils.trimToNull(el.getAttribute("scaled_factor"));
				if (factor == null)
				{
					factor = String.valueOf(AssignmentConstants.DEFAULT_SCALED_FACTOR);
				}
				m_factor = Integer.valueOf(factor);
				// %%%zqian
				// read the scaled max grade point first; if there is none, get the old max grade value and multiple by 10
				String maxGradePoint = StringUtils.trimToNull(el.getAttribute("scaled_maxgradepoint"));
				if (maxGradePoint == null)
				{
					maxGradePoint = StringUtils.trimToNull(el.getAttribute("maxgradepoint"));
					if (maxGradePoint != null)
					{
						maxGradePoint = maxGradePoint + factor.substring(1);
					}
				}
				if (maxGradePoint != null)
				{
					m_maxGradePoint = Integer.parseInt(maxGradePoint);
				}
			}
			catch (Exception e)
			{
				M_log.warn(" BaseAssignmentContent Exception parsing maxgradepoint int from xml file string : " + e);
			}

			// READ THE AUTHORS
			m_authors = new ArrayList();
			intString = el.getAttribute("numberofauthors");
			try
			{
				numAttributes = Integer.parseInt(intString);

				for (int x = 0; x < numAttributes; x++)
				{
					attributeString = "author" + x;
					tempString = el.getAttribute(attributeString);
					if (tempString != null) m_authors.add(tempString);
				}
			}
			catch (Exception e)
			{
				M_log.warn(" BaseAssignmentContent: Exception reading authors : " + e);
			}

			// READ THE ATTACHMENTS
			m_attachments = m_entityManager.newReferenceList();
			M_log.debug(" BaseAssignmentContent: Reading attachments : ");
			intString = el.getAttribute("numberofattachments");
			M_log.debug(" BaseAssignmentContent: num attachments : " + intString);
			try
			{
				numAttributes = Integer.parseInt(intString);

				for (int x = 0; x < numAttributes; x++)
				{
					attributeString = "attachment" + x;
					tempString = el.getAttribute(attributeString);
					if (tempString != null)
					{
						tempReference = m_entityManager.newReference(tempString);
						m_attachments.add(tempReference);
						M_log.debug(" BaseAssignmentContent: " + attributeString + " : " + tempString);
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(" BaseAssignmentContent: Exception reading attachments : " + e);
			}

			// READ THE PROPERTIES
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
				}
				// old style of encoding
				else if (element.getTagName().equals("instructions-html") || element.getTagName().equals("instructions-formatted")
						|| element.getTagName().equals("instructions"))
				{
					if ((element.getChildNodes() != null) && (element.getChildNodes().item(0) != null))
					{
						m_instructions = element.getChildNodes().item(0).getNodeValue();
						if (element.getTagName().equals("instructions"))
							m_instructions = FormattedText.convertPlaintextToFormattedText(m_instructions);
						if (element.getTagName().equals("instructions-formatted"))
							m_instructions = FormattedText.convertOldFormattedText(m_instructions);
						
							M_log.debug(" BaseAssignmentContent(Element): instructions : " + m_instructions);
					}
					if (m_instructions == null)
					{
						m_instructions = "";
					}
				}
			}

			M_log.debug(" BaseAssignmentContent(Element): LEAVING STORAGE CONSTRUTOR");

		}// storage constructor
		
		/**
		 * @param services
		 * @return
		 */
		public ContentHandler getContentHandler(Map<String, Object> services)
		{
			final Entity thisEntity = this;
			return new DefaultEntityHandler()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.sakaiproject.util.DefaultEntityHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
				{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if ("content".equals(qName) && entity == null)
						{
							int numAttributes = 0;
							String intString = null;
							String attributeString = null;
							String tempString = null;
							Reference tempReference = null;

							m_id = attributes.getValue("id");
							m_context = attributes.getValue("context");
							m_title = attributes.getValue("title");
							m_groupProject = getBool(attributes.getValue("groupproject"));
							m_individuallyGraded = getBool(attributes.getValue("indivgraded"));
							m_releaseGrades = getBool(attributes.getValue("releasegrades"));
							m_allowAttachments = getBool(attributes.getValue("allowattach"));
							m_hideDueDate = getBool(attributes.getValue("hideduedate"));
							m_allowReviewService = getBool(attributes.getValue("allowreview"));
							m_allowStudentViewReport = getBool(attributes.getValue("allowstudentview"));
							m_submitReviewRepo = attributes.getValue("submitReviewRepo");
							m_generateOriginalityReport = attributes.getValue("generateOriginalityReport");
							m_checkTurnitin = getBool(attributes.getValue("checkTurnitin"));
							m_checkInternet = getBool(attributes.getValue("checkInternet"));
							m_checkPublications = getBool(attributes.getValue("checkPublications"));
							m_checkInstitution = getBool(attributes.getValue("checkInstitution"));
							m_excludeBibliographic = getBool(attributes.getValue("excludeBibliographic"));
							m_excludeQuoted = getBool(attributes.getValue("excludeQuoted"));
							String excludeTypeStr = attributes.getValue("excludeType");
							try{
								m_excludeType = Integer.parseInt(excludeTypeStr);
								if(m_excludeType != 0 && m_excludeType != 1 && m_excludeType != 2){
									m_excludeType = 0;
								}
							}catch (Exception e) {
								m_excludeType = 0;
							}
							String excludeValueStr = attributes.getValue("excludeValue");
							try{
								m_excludeValue = Integer.parseInt(excludeValueStr);
								if(m_excludeValue < 0 || m_excludeValue > 100){
									m_excludeValue = 1;
								}
							}catch (Exception e) {
								m_excludeValue = 1;
							}
							
							m_timeCreated = getTimeObject(attributes.getValue("datecreated"));
							m_timeLastModified = getTimeObject(attributes.getValue("lastmod"));

							m_instructions = formattedTextDecodeFormattedTextAttribute(attributes, "instructions");

							try
							{
								m_honorPledge = Integer.parseInt(attributes.getValue("honorpledge"));
							}
							catch (Exception e)
							{
								M_log.warn(" getContentHandler startElement Exception parsing honor pledge int from xml file string : " + e);
							}

							try
							{
								m_typeOfSubmission = Integer.parseInt(attributes.getValue("submissiontype"));
							}
							catch (Exception e)
							{
								M_log.warn(" getContentHandler startElement Exception parsing submission type int from xml file string : " + e);
							}

							try
							{
								m_typeOfGrade = Integer.parseInt(attributes.getValue("typeofgrade"));
							}
							catch (Exception e)
							{
								M_log.warn(" getContentHandler startElement Exception parsing grade type int from xml file string : " + e);
							}

							try
							{
								String factor = StringUtils.trimToNull(attributes.getValue("scaled_factor"));
								if (factor == null)
								{
									factor = String.valueOf(AssignmentConstants.DEFAULT_SCALED_FACTOR);
								}
								m_factor = Integer.parseInt(factor);
								// %%%zqian
								// read the scaled max grade point first; if there is none, get the old max grade value and multiple by "factor"
								String maxGradePoint = StringUtils.trimToNull(attributes.getValue("scaled_maxgradepoint"));
								if (maxGradePoint == null)
								{
									maxGradePoint = StringUtils.trimToNull(attributes.getValue("maxgradepoint"));
									if (maxGradePoint != null)
									{
										maxGradePoint = maxGradePoint + factor.substring(1);
									}
								}
								m_maxGradePoint = maxGradePoint != null ? Integer.parseInt(maxGradePoint) : m_maxGradePoint;
							}
							catch (Exception e)
							{
								M_log.warn(" getContentHandler startElement Exception parsing maxgradepoint int from xml file string : " + e);
							}

							// READ THE AUTHORS
							m_authors = new ArrayList();
							intString = attributes.getValue("numberofauthors");
							try
							{
								numAttributes = Integer.parseInt(intString);

								for (int x = 0; x < numAttributes; x++)
								{
									attributeString = "author" + x;
									tempString = attributes.getValue(attributeString);
									if (tempString != null) m_authors.add(tempString);
								}
							}
							catch (Exception e)
							{
								M_log.warn(" getContentHandler startElement Exception reading authors : " + e);
							}

							// READ THE ATTACHMENTS
							m_attachments = m_entityManager.newReferenceList();
							intString = attributes.getValue("numberofattachments");
							try
							{
								numAttributes = Integer.parseInt(intString);

								for (int x = 0; x < numAttributes; x++)
								{
									attributeString = "attachment" + x;
									tempString = attributes.getValue(attributeString);
									if (tempString != null)
									{
										tempReference = m_entityManager.newReference(tempString);
										m_attachments.add(tempReference);
									}
								}
							}
							catch (Exception e)
							{
								M_log.warn(" getContentHandler startElement DbCachedContent : Exception reading attachments : " + e);
							}
							
							entity = thisEntity;
						}
						else
						{
							M_log.warn(" getContentHandler startElement Unexpected Element " + qName);
						}
					}
				}
			};
		}
		
		/**
		 * Takes the AssignmentContent's attribute values and puts them into the xml document.
		 * 
		 * @param s -
		 *        Data structure holding the object to be stored.
		 * @param doc -
		 *        The xml document.
		 */
		public Element toXml(Document doc, Stack stack)
		{
			M_log.debug(this + " BASE ASSIGNMENT : ENTERING TOXML");

			Element content = doc.createElement("content");

			if (stack.isEmpty())
			{
				doc.appendChild(content);
			}
			else
			{
				((Element) stack.peek()).appendChild(content);
			}
			stack.push(content);

			String numItemsString = null;
			String attributeString = null;
			String itemString = null;
			Reference tempReference = null;

			// SAK-13408 -The XML implementation in Websphere throws an LSException if the
			// attribute is null, while in Tomcat it assumes an empty string. The following
			// sets the attribute to an empty string if the value is null.
			content.setAttribute("id", m_id == null ? "" : m_id);
			content.setAttribute("context", m_context == null ? "" : m_context);
			content.setAttribute("title", m_title == null ? "" : m_title);
			content.setAttribute("groupproject", getBoolString(m_groupProject));
			content.setAttribute("indivgraded", getBoolString(m_individuallyGraded));
			content.setAttribute("releasegrades", getBoolString(m_releaseGrades));
			content.setAttribute("allowattach", getBoolString(m_allowAttachments));
			content.setAttribute("hideduedate", getBoolString(m_hideDueDate));
		
			content.setAttribute("allowreview", getBoolString(m_allowReviewService));
			content.setAttribute("allowstudentview", getBoolString(m_allowStudentViewReport));
			content.setAttribute("submitReviewRepo", m_submitReviewRepo);
			content.setAttribute("generateOriginalityReport", m_generateOriginalityReport);
			content.setAttribute("checkTurnitin", getBoolString(m_checkTurnitin));
			content.setAttribute("checkInternet", getBoolString(m_checkInternet));
			content.setAttribute("checkPublications", getBoolString(m_checkPublications));
			content.setAttribute("checkInstitution", getBoolString(m_checkInstitution));
			content.setAttribute("excludeBibliographic", getBoolString(m_excludeBibliographic));
			content.setAttribute("excludeQuoted", getBoolString(m_excludeQuoted));
			content.setAttribute("excludeType", Integer.toString(m_excludeType));
			content.setAttribute("excludeValue", Integer.toString(m_excludeValue));
			
			content.setAttribute("honorpledge", String.valueOf(m_honorPledge));
			content.setAttribute("submissiontype", String.valueOf(m_typeOfSubmission));
			content.setAttribute("typeofgrade", String.valueOf(m_typeOfGrade));
			content.setAttribute("scaled_maxgradepoint", String.valueOf(m_maxGradePoint));
			content.setAttribute("scaled_factor", String.valueOf(m_factor));
			content.setAttribute("datecreated", getTimeString(m_timeCreated));
			content.setAttribute("lastmod", getTimeString(m_timeLastModified));

			M_log.debug(this + " BASE CONTENT : TOXML : SAVED REGULAR PROPERTIES");

			// SAVE THE AUTHORS
			numItemsString = "" + m_authors.size();
			content.setAttribute("numberofauthors", numItemsString);
			for (int x = 0; x < m_authors.size(); x++)
			{
				attributeString = "author" + x;
				itemString = (String) m_authors.get(x);
				if (itemString != null) content.setAttribute(attributeString, itemString);
			}

			M_log.debug(this + " BASE CONTENT : TOXML : SAVED AUTHORS");

			// SAVE THE ATTACHMENTS
			numItemsString = "" + m_attachments.size();
			content.setAttribute("numberofattachments", numItemsString);
			for (int x = 0; x < m_attachments.size(); x++)
			{
				attributeString = "attachment" + x;
				tempReference = (Reference) m_attachments.get(x);
				itemString = tempReference.getReference();
				if (itemString != null) content.setAttribute(attributeString, itemString);
			}

			// SAVE THE PROPERTIES
			m_properties.toXml(doc, stack);

			M_log.debug(this + " BASE CONTENT : TOXML : SAVED REGULAR PROPERTIES");

			stack.pop();

			// SAVE THE INSTRUCTIONS
			FormattedText.encodeFormattedTextAttribute(content, "instructions", m_instructions);

			return content;

		}// toXml

		protected void setAll(AssignmentContent content)
		{
			if (content != null)
			{
				m_id = content.getId();
				m_context = content.getContext();
				m_authors = content.getAuthors();
				m_attachments = content.getAttachments();
				m_title = content.getTitle();
				m_instructions = content.getInstructions();
				m_honorPledge = content.getHonorPledge();
				m_typeOfSubmission = content.getTypeOfSubmission();
				m_typeOfGrade = content.getTypeOfGrade();
				m_maxGradePoint = content.getMaxGradePoint();
				m_factor = content.getFactor();
				m_groupProject = content.getGroupProject();
				m_individuallyGraded = content.individuallyGraded();
				m_releaseGrades = content.releaseGrades();
				m_allowAttachments = content.getAllowAttachments();
				m_hideDueDate = content.getHideDueDate();
				//Uct
				m_allowReviewService = content.getAllowReviewService();
				m_allowStudentViewReport = content.getAllowStudentViewReport();
				m_submitReviewRepo = content.getSubmitReviewRepo();
				m_generateOriginalityReport = content.getGenerateOriginalityReport();
				m_checkTurnitin = content.isCheckTurnitin();
				m_checkInternet = content.isCheckInternet();
				m_checkPublications = content.isCheckPublications();
				m_checkInstitution = content.isCheckInstitution();
				m_excludeBibliographic = content.isExcludeBibliographic();
				m_excludeQuoted = content.isExcludeQuoted();
				m_excludeType = content.getExcludeType();
				m_excludeValue = content.getExcludeValue();
				m_timeCreated = content.getTimeCreated();
				m_timeLastModified = content.getTimeLastModified();
				m_properties = new BaseResourcePropertiesEdit();
				m_properties.addAll(content.getProperties());
			}
		}

		public String getId()
		{
			return m_id;
		}

		/**
		 * Access the URL which can be used to access the resource.
		 * 
		 * @return The URL which can be used to access the resource.
		 */
		public String getUrl()
		{
			return getAccessPoint(false) + Entity.SEPARATOR + "c" + Entity.SEPARATOR + m_context + Entity.SEPARATOR + m_id;

		} // getUrl

		/**
		 * Access the internal reference which can be used to access the resource from within the system.
		 * 
		 * @return The the internal reference which can be used to access the resource from within the system.
		 */
		public String getReference()
		{
			return contentReference(m_context, m_id);

		} // getReference

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * Access the resource's properties.
		 * 
		 * @return The resource's properties.
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * AttachmentContainer Implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * Access the attachments.
		 * 
		 * @return The set of attachments (a ReferenceVector containing Reference objects) (may be empty).
		 */
		public List getAttachments()
		{
			return m_attachments;
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * AssignmentContent Implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * Access the AssignmentContent's context at the time of creation.
		 * 
		 * @return String - the context string.
		 */
		public String getContext()
		{
			return m_context;
		}

		/**
		 * Access the list of authors.
		 * 
		 * @return FlexStringArray of user ids.
		 */
		public List getAuthors()
		{
			return m_authors;
		}

		/**
		 * Access the creator of this object.
		 * 
		 * @return The User object representing the creator.
		 */
		public String getCreator()
		{
			return m_properties.getProperty(ResourceProperties.PROP_CREATOR);
		}

		/**
		 * Access the person of last modificaiton
		 * 
		 * @return the User
		 */
		public String getAuthorLastModified()
		{
			return m_properties.getProperty(ResourceProperties.PROP_MODIFIED_BY);
		}

		/**
		 * Access the title.
		 * 
		 * @return The Assignment's title.
		 */
		public String getTitle()
		{
			return m_title;
		}

		/**
		 * Access the instructions.
		 * 
		 * @return The Assignment Content's instructions.
		 */
		public String getInstructions()
		{
			return m_instructions;
		}

		/**
		 * Access a string describing the type of submission.
		 * @return Description of the type of submission.
		 */
		public String getTypeOfSubmissionString(){
			
			String retVal = null;

			switch (m_typeOfSubmission)
			{
				case 1:
					retVal = rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_INLINE_PROP);
					break;

				case 2:
					retVal = rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_ATTACHMENTS_ONLY_PROP);
					break;

				case 3:
					retVal = rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_INLINE_AND_ATTACHMENTS_PROP);
					break;

				case 4:
					retVal = rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_NON_ELECTRONIC_PROP);
					break;

				case 5:
					retVal = rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_SINGLE_ATTACHMENT_PROP);
					break;

				default:
					retVal = rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_UNKNOWN_PROP);
					break;
			}

			return retVal;
		}
		
		/**
		 * Get the type of valid submission.
		 * 
		 * @return int - Type of Submission.
		 */
		public int getTypeOfSubmission()
		{
			return m_typeOfSubmission;
		}

	    /**
	     * Access a string describing the type of grade.
	     * @deprecated Use getTypeOfGradeString() instead.
	     * @param gradeType -
	     *        The integer representing the type of grade.
	     * @return Description of the type of grade.
	     */
	    public String getTypeOfGradeString(int gradeType){
	    	return getTypeOfGradeString();
	    }
	    
		/**
		 * Access a string describing the type of grade.
		 * 
		 * @return Description of the type of grade.
		 */
		public String getTypeOfGradeString(){
			
			String retVal = null;

			switch (m_typeOfGrade)
			{
				case 1:
					retVal = rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_NOGRADE_PROP);
					break;

				case 2:
					retVal = rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_LETTER_PROP);
					break;

				case 3:
					retVal = rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_POINTS_PROP);
					break;

				case 4:
					retVal = rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_PASS_FAIL_PROP);
					break;

				case 5:
					retVal = rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_CHECK_PROP);
					break;

				default:
					retVal = rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_UNKNOWN_PROP);
					break;
			}

			return retVal;
		}

		/**
		 * Get the grade type.
		 * 
		 * @return gradeType - The type of grade.
		 */
		public int getTypeOfGrade()
		{
			return m_typeOfGrade;
		}

		/**
		 * Get the maximum grade for grade type = SCORE_GRADE_TYPE(3)
		 * 
		 * @return The maximum grade score.
		 */
		public int getMaxGradePoint()
		{
			return m_maxGradePoint;
		}
		
		public int getFactor() 
		{
			return m_factor;
		}

		/**
		 * Get the maximum grade for grade type = SCORE_GRADE_TYPE(3) Formated to show "factor" decimal places
		 * 
		 * @return The maximum grade score.
		 */
		public String getMaxGradePointDisplay()
		{
			// get the number of decimals
			int factor = getFactor();
			// formated to show factor decimal places, for example, 1000 to 100.0
			// get localized number format
			NumberFormat nbFormat = FormattedText.getNumberFormat((int)Math.log10(factor),(int)Math.log10(factor),false);				
			// show grade in localized number format
			Double dblGrade = new Double(m_maxGradePoint/(double)factor);
			String decimal_maxGradePoint = nbFormat.format(dblGrade);
			
			return decimal_maxGradePoint;
		}

		/**
		 * Get whether this project can be a group project.
		 * 
		 * @return True if this can be a group project, false otherwise.
		 */
		public boolean getGroupProject()
		{
			return m_groupProject;
		}

		/**
		 * Get whether group projects should be individually graded.
		 * 
		 * @return individGraded - true if projects are individually graded, false if grades are given to the group.
		 */
		public boolean individuallyGraded()
		{
			return m_individuallyGraded;
		}

		/**
		 * Gets whether grades can be released once submissions are graded.
		 * 
		 * @return true if grades can be released once submission are graded, false if they must be released manually.
		 */
		public boolean releaseGrades()
		{
			return m_releaseGrades;
		}

		/**
		 * Get the Honor Pledge type; values are NONE and ENGINEERING_HONOR_PLEDGE.
		 * 
		 * @return the Honor Pledge value.
		 */
		public int getHonorPledge()
		{
			return m_honorPledge;
		}

		/**
		 * Does this Assignment allow attachments?
		 * 
		 * @return true if the Assignment allows attachments, false otherwise?
		 */
		public boolean getAllowAttachments()
		{
			return m_allowAttachments;
		}
	
		/**
		 * Does this Assignment have a hidden due date
		 * 
		 * @return true if the Assignment due date hidden, false otherwise?
		 */
		public boolean getHideDueDate()
		{
			return m_hideDueDate;
		}
	
		/**
		 * Does this Assignment allow review service?
		 * 
		 * @return true if the Assignment allows review service, false otherwise?
		 */
		public boolean getAllowReviewService()
		{
			return m_allowReviewService;
		}
		
		public boolean getAllowStudentViewReport() {
			return m_allowStudentViewReport;
		}
		
		
		/**
		 * Access the time that this object was created.
		 * 
		 * @return The Time object representing the time of creation.
		 */
		public Time getTimeCreated()
		{
			return m_timeCreated;
		}

		/**
		 * Access the time of last modificaiton.
		 * 
		 * @return The Time of last modification.
		 */
		public Time getTimeLastModified()
		{
			return m_timeLastModified;
		}

		/**
		 * Is this AssignmentContent selected for use by an Assignment ?
		 */
		public boolean inUse()
		{
			boolean retVal = false;
			Assignment assignment = null;
			List allAssignments = getAssignments(m_context);
			for (int x = 0; x < allAssignments.size(); x++)
			{
				assignment = (Assignment) allAssignments.get(x);
				if (assignment.getContentReference().equals(getReference())) return true;
			}

			return retVal;
		}

		/**
		 * Are these objects equal? If they are both AssignmentContent objects, and they have matching id's, they are.
		 * 
		 * @return true if they are equal, false if not.
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof AssignmentContent)) return false;
			return ((AssignmentContent) obj).getId().equals(getId());

		} // equals

		/**
		 * Make a hash code that reflects the equals() logic as well. We want two objects, even if different instances, if they have the same id to hash the same.
		 */
		public int hashCode()
		{
			return getId().hashCode();

		} // hashCode

		/**
		 * Compare this object with the specified object for order.
		 * 
		 * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
		 */
		public int compareTo(Object obj)
		{
			if (!(obj instanceof AssignmentContent)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// start the compare by comparing their sort names
			int compare = getTitle().compareTo(((AssignmentContent) obj).getTitle());

			// if these are the same
			if (compare == 0)
			{
				// sort based on (unique) id
				compare = getId().compareTo(((AssignmentContent) obj).getId());
			}

			return compare;

		} // compareTo

		public String getSubmitReviewRepo() {
			return m_submitReviewRepo;
		}

		public void setSubmitReviewRepo(String m_submitReviewRepo) {
			this.m_submitReviewRepo = m_submitReviewRepo;
		}

		public String getGenerateOriginalityReport() {
			return m_generateOriginalityReport;
		}

		public void setGenerateOriginalityReport(String m_generateOriginalityReport) {
			this.m_generateOriginalityReport = m_generateOriginalityReport;
		}

		public boolean isCheckTurnitin() {
			return m_checkTurnitin;
		}

		public void setCheckTurnitin(boolean m_checkTurnitin) {
			this.m_checkTurnitin = m_checkTurnitin;
		}

		public boolean isCheckInternet() {
			return m_checkInternet;
		}

		public void setCheckInternet(boolean m_checkInternet) {
			this.m_checkInternet = m_checkInternet;
		}

		public boolean isCheckPublications() {
			return m_checkPublications;
		}

		public void setCheckPublications(boolean m_checkPublications) {
			this.m_checkPublications = m_checkPublications;
		}

		public boolean isCheckInstitution() {
			return m_checkInstitution;
		}

		public void setCheckInstitution(boolean m_checkInstitution) {
			this.m_checkInstitution = m_checkInstitution;
		}
		
		public boolean isExcludeBibliographic() {
			return m_excludeBibliographic;
		}

		public void setExcludeBibliographic(boolean m_excludeBibliographic) {
			this.m_excludeBibliographic = m_excludeBibliographic;
		}
		
		public boolean isExcludeQuoted() {
			return m_excludeQuoted;
		}

		public void setExcludeQuoted(boolean m_excludeQuoted) {
			this.m_excludeQuoted = m_excludeQuoted;
		}
		
		public int getExcludeType(){
			return m_excludeType;
		}
		
		public void setExcludeType(int m_excludeType){
			this.m_excludeType = m_excludeType;
		}
		
		public int getExcludeValue(){
			return m_excludeValue;
		}
		
		public void setExcludeValue(int m_excludeValue){
			this.m_excludeValue = m_excludeValue;
		}

	}// BaseAssignmentContent

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentContentEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * <p>
	 * BaseAssignmentContentEdit is an implementation of the CHEF AssignmentContentEdit object.
	 * </p>
	 * 
	 * @author University of Michigan, CHEF Software Development Team
	 */
	public class BaseAssignmentContentEdit extends BaseAssignmentContent implements AttachmentContainer, AssignmentContentEdit,
			SessionBindingListener
	{
		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/**
		 * Construct from another AssignmentContent object.
		 * 
		 * @param AssignmentContent
		 *        The AssignmentContent object to use for values.
		 */
		public BaseAssignmentContentEdit(AssignmentContent assignmentContent)
		{
			super(assignmentContent);

		} // BaseAssignmentContentEdit

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The AssignmentContent id.
		 */
		public BaseAssignmentContentEdit(String id, String context)
		{
			super(id, context);

		} // BaseAssignmentContentEdit

		/**
		 * Construct from information in XML.
		 * 
		 * @param el
		 *        The XML DOM Element definining the AssignmentContent.
		 */
		public BaseAssignmentContentEdit(Element el)
		{
			super(el);

		} // BaseAssignmentContentEdit

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}

		} // finalize

		/******************************************************************************************************************************************************************************************************************************************************
		 * AttachmentContainer Implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * Add an attachment.
		 * 
		 * @param ref -
		 *        The attachment Reference.
		 */
		public void addAttachment(Reference ref)
		{
			if (ref != null) m_attachments.add(ref);
		}

		/**
		 * Remove an attachment.
		 * 
		 * @param ref -
		 *        The attachment Reference to remove (the one removed will equal this, they need not be ==).
		 */
		public void removeAttachment(Reference ref)
		{
			if (ref != null) m_attachments.remove(ref);
		}

		/**
		 * Replace the attachment set.
		 * 
		 * @param attachments -
		 *        A ReferenceVector that will become the new set of attachments.
		 */
		public void replaceAttachments(List attachments)
		{
			m_attachments = attachments;
		}

		/**
		 * Clear all attachments.
		 */
		public void clearAttachments()
		{
			m_attachments.clear();
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * AssignmentContentEdit Implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * Set the title.
		 * 
		 * @param title -
		 *        The Assignment's title.
		 */
		public void setTitle(String title)
		{
			m_title = title;
		}

		/**
		 * Set the instructions.
		 * 
		 * @param instructions -
		 *        The Assignment's instructions.
		 */
		public void setInstructions(String instructions)
		{
			m_instructions = instructions;
		}

		/**
		 * Set the context at the time of creation.
		 * 
		 * @param context -
		 *        the context string.
		 */
		public void setContext(String context)
		{
			m_context = context;
		}

		/**
		 * Set the type of valid submission.
		 * 
		 * @param int -
		 *        Type of Submission.
		 */
		public void setTypeOfSubmission(int type)
		{
			m_typeOfSubmission = type;
		}

		/**
		 * Set the grade type.
		 * 
		 * @param gradeType -
		 *        The type of grade.
		 */
		public void setTypeOfGrade(int gradeType)
		{
			m_typeOfGrade = gradeType;
		}

		/**
		 * Set the maximum grade for grade type = SCORE_GRADE_TYPE(3)
		 * 
		 * @param maxPoints -
		 *        The maximum grade score.
		 */
		public void setMaxGradePoint(int maxPoints)
		{
			m_maxGradePoint = maxPoints;
		}
		
		public void setFactor(int factor)
		{
			m_factor = factor;
		}

		/**
		 * Set whether this project can be a group project.
		 * 
		 * @param groupProject -
		 *        True if this can be a group project, false otherwise.
		 */
		public void setGroupProject(boolean groupProject)
		{
			m_groupProject = groupProject;
		}

		/**
		 * Set whether group projects should be individually graded.
		 * 
		 * @param individGraded -
		 *        true if projects are individually graded, false if grades are given to the group.
		 */
		public void setIndividuallyGraded(boolean individGraded)
		{
			m_individuallyGraded = individGraded;
		}

		/**
		 * Sets whether grades can be released once submissions are graded.
		 * 
		 * @param release -
		 *        true if grades can be released once submission are graded, false if they must be released manually.
		 */
		public void setReleaseGrades(boolean release)
		{
			m_releaseGrades = release;
		}

		public void setHideDueDate(boolean hide)
		{	
			m_hideDueDate = hide;
		}

		/**
		 * Set the Honor Pledge type; values are NONE and ENGINEERING_HONOR_PLEDGE.
		 * 
		 * @param pledgeType -
		 *        the Honor Pledge value.
		 */
		public void setHonorPledge(int pledgeType)
		{
			m_honorPledge = pledgeType;
		}

		
		/**
		 * Does this Assignment allow using the review service?
		 * 
		 * @param allow -
		 *        true if the Assignment allows review service, false otherwise?
		 */
		public void setAllowReviewService(boolean allow)
		{
			m_allowReviewService = allow;
		}

		/**
		 * Does this Assignment allow students to view the report?
		 * 
		 * @param allow -
		 *        true if the Assignment allows students to view the report, false otherwise?
		 */
		public void setAllowStudentViewReport(boolean allow) {
			m_allowStudentViewReport = allow;
		}
		
		/**
		 * Does this Assignment allow attachments?
		 * 
		 * @param allow -
		 *        true if the Assignment allows attachments, false otherwise?
		 */
		public void setAllowAttachments(boolean allow)
		{
			m_allowAttachments = allow;
		}

		/**
		 * Add an author to the author list.
		 * 
		 * @param author -
		 *        The User to add to the author list.
		 */
		public void addAuthor(User author)
		{
			if (author != null) m_authors.add(author.getId());
		}

		/**
		 * Remove an author from the author list.
		 * 
		 * @param author -
		 *        the User to remove from the author list.
		 */
		public void removeAuthor(User author)
		{
			if (author != null) m_authors.remove(author.getId());
		}

		/**
		 * Set the time last modified.
		 * 
		 * @param lastmod -
		 *        The Time at which the Content was last modified.
		 */
		public void setTimeLastModified(Time lastmod)
		{
			if (lastmod != null) m_timeLastModified = lastmod;
		}

		/**
		 * Take all values from this object.
		 * 
		 * @param AssignmentContent
		 *        The AssignmentContent object to take values from.
		 */
		protected void set(AssignmentContent assignmentContent)
		{
			setAll(assignmentContent);

		} // set

		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * Access the resource's properties for modification
		 * 
		 * @return The resource's properties.
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;

		} // getPropertiesEdit

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;

		} // activate

		/**
		 * Check to see if the edit is still active, or has already been closed.
		 * 
		 * @return true if the edit is active, false if it's been closed.
		 */
		public boolean isActiveEdit()
		{
			return m_active;

		} // isActiveEdit

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;

		} // closeEdit

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			M_log.debug(" BaseAssignmentContent valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}

		} // valueUnbound

	} // BaseAssignmentContentEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentSubmission implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAssignmentSubmission implements AssignmentSubmission
	{
		protected final String STATUS_DRAFT = "Drafted";

		protected final String STATUS_SUBMITTED = "Submitted";

		protected final String STATUS_RETURNED = "Returned";

		protected final String STATUS_GRADED = "Graded";

		protected ResourcePropertiesEdit m_properties;

		protected String m_id;

		protected String m_assignment;

		protected String m_context;

		protected List<String> m_submitters;

                protected String m_submitterId;

                protected List m_submissionLog;

		protected List m_grades;
                
		protected Time m_timeSubmitted;

		protected Time m_timeReturned;

		protected Time m_timeLastModified;

		protected List m_submittedAttachments;

		protected List m_feedbackAttachments;

		protected String m_submittedText;

		protected String m_feedbackComment;

		protected String m_feedbackText;

		protected String m_grade;
		
		protected int m_factor;

		protected boolean m_submitted;

		protected boolean m_returned;

		protected boolean m_graded;
		
		protected String m_gradedBy;

		protected boolean m_gradeReleased;

		protected boolean m_honorPledgeFlag;

		// SAK-17606
		protected String m_anonymousSubmissionId;

		protected boolean m_hideDueDate;

		
		//The score given by the review service
		protected Integer m_reviewScore;
		// The report given by the content review service
		protected String m_reviewReport;
		// The status of the review service
		protected String m_reviewStatus;
		
		protected String m_reviewIconUrl;

        protected String m_reviewError;
		
		// SAK-29314
		protected boolean m_isUserSubmission;
		
		protected Assignment m_asn;
		/*
		 * Helper method to add elements or attributes to a list
		 * @param attributeName Name of the attribute or element value to add
		 * @param list The list to add to add elements to
		 * @param attributes A object of Element or Attributes that will be used as a source
		 * @param dereference Whether or not it needs to be created as a reference from entitybroker
		 */
		protected void addElementsToList(String attributeName, List list, Object attributes, boolean dereference) {
			int x=0;
			String tempString = null;
			//Can handle either values coming as an Element or Attributes
			if (attributes instanceof Element) {
				tempString = ((Element) attributes).getAttribute(attributeName+x);
			}
			else if (attributes instanceof Attributes) {
				tempString = ((Attributes) attributes).getValue(attributeName+x);
			}
			tempString = StringUtils.trimToNull(tempString);
			while (tempString != null)
			{
				Reference tempReference;
				if (dereference==true) {
					tempReference = m_entityManager.newReference(tempString);
					list.add(tempReference);
				}
				else {
					list.add(tempString);
				}
				x++;
				if (attributes instanceof Element) {
					tempString = ((Element) attributes).getAttribute(attributeName+x);
				}
				else if (attributes instanceof Attributes) {
					tempString = ((Attributes) attributes).getValue(attributeName+x);
				}
				tempString = StringUtils.trimToNull(tempString);
			} 
		}
		
		// return the variables
		// Get new values from review service if defaults
		public int getReviewScore() {
			// Code to get updated score if default
			M_log.debug(this + " getReviewScore for submission " + this.getId() + " and review service is: " + (this.getAssignment().getContent().getAllowReviewService()));
			if (!this.getAssignment().getContent().getAllowReviewService()) {
				M_log.debug(this + " getReviewScore Content review is not enabled for this assignment");
				return -2;
			}

			if (m_submittedAttachments.isEmpty()) {
				M_log.debug(this + " getReviewScore No attachments submitted.");
				return -2;
			}
			else
			{
				//we may have already retrived this one
				if (m_reviewScore != null && m_reviewScore > -1) {
					M_log.debug("returning stored value of " + m_reviewScore);
					return m_reviewScore.intValue();
				}

				ContentResource cr = getFirstAcceptableAttachement();
				if (cr == null )
				{
					M_log.debug(this + " getReviewScore No suitable attachments found in list");
					return -2;
				}



				try {
					//we need to find the first attachment the CR will accept
					String contentId = cr.getId();
					M_log.debug(this + " getReviewScore checking for score for content: " + contentId);

                    Long status = contentReviewService.getReviewStatus(contentId);
                    if (status != null && (status.equals(ContentReviewItem.NOT_SUBMITTED_CODE) || status.equals(ContentReviewItem.SUBMITTED_AWAITING_REPORT_CODE)))  {
                        M_log.debug(this + " getReviewStatus returned a status of: " + status);
                        return -2;
                    }

					int score = contentReviewService.getReviewScore(contentId, getAssignment().getReference(), getContentReviewSubmitterId(cr));
					m_reviewScore = score;
					M_log.debug(this + " getReviewScore CR returned a score of: " + score);
					return score;
						
				} 
				catch (QueueException cie) {
					//should we add the item
					try {
						
							M_log.debug(this + " getReviewScore Item is not in queue we will try add it");
                                                        try {
								contentReviewService.queueContent(getContentReviewSubmitterId(cr), this.getContext(), getAssignment().getReference(), Arrays.asList(cr));
							}
							catch (QueueException qe) {
								M_log.warn(" getReviewScore Unable to queue content with content review Service: " + qe.getMessage());
							}
								
							
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					return -1;
					
				}
				catch (Exception e) {
					M_log.warn(this + " getReviewScore " + e.getMessage());
					return -1;
				}
					
			}
	
			
		}

		/**
		 * SAK-26322 - Essentially the same as getReviewScore() only it acts upon the ContentResource parameter rather than that which is returned by firstAcceptableAttachment().
		 * TODO: consider deleting getReviewScore(). If not possible, then refactor to eliminate code duplication
		 */
		private int getReviewScore(ContentResource cr)
		{
			M_log.debug(this + " getReviewScore(ContentResource) for submission " + this.getId() + " and review service is: " + (this.getAssignment().getContent().getAllowReviewService()));

			//null check, allow review service check
			if (cr == null)
			{
				M_log.debug(this + " getReviewScore(ContentResource) called with cr == null");
				return -2;
			}

			if (!this.getAssignment().getContent().getAllowReviewService())
			{
				M_log.debug(this + " getReviewScore(ContentResource) Content review is not enabled for this assignment");
				return -2;
			}

			//get the status from the content review service, if it's in a valid status, get the score)
			try
			{
				String contentId = cr.getId();
				M_log.debug(this + " getReviewScore(ContentResource) checking for score for content: " + contentId);

				Long status = contentReviewService.getReviewStatus(contentId);
				if (status != null && (status.equals(ContentReviewItem.NOT_SUBMITTED_CODE) || status.equals(ContentReviewItem.SUBMITTED_AWAITING_REPORT_CODE)))
				{
					M_log.debug(this + " getReviewStatus returned a state of: " + status);
					return -2;
				}

				int score = contentReviewService.getReviewScore(contentId, getAssignment().getReference(), getContentReviewSubmitterId(cr));
				// TODO: delete the following line if there will be no repercussions:
				m_reviewScore = score;
				M_log.debug(this + " getReviewScore(ContentResource) CR returned a score of: " + score);
				return score;
			}
			catch (QueueException cie)
			{
				//should we add the item
				try
				{
					M_log.debug(" getReviewScore(ContentResource) Item is not in queue we will try to add it");
					try
					{
						contentReviewService.queueContent(getContentReviewSubmitterId(cr), this.getContext(), getAssignment().getReference(), Arrays.asList(cr));
					}
					catch (QueueException qe)
					{
						M_log.warn(" getReviewScore(ContentResource) Unable to queue content with content review service: " + qe.getMessage());
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return -1;
			}
			catch (Exception e)
			{
				M_log.warn(this + " getReviewScore " + e.getMessage());
				return -1;
			}
		}
		
		public String getContentReviewSubmitterId(ContentResource cr){
			//Group submissions store the group ID as the submitterId, so find an actual user ID
			String userId = null;
			if(cr != null && getAssignment().isGroup() && cr.getProperties() != null
					&& StringUtils.isNotEmpty(cr.getProperties().getProperty(ResourceProperties.PROP_CREATOR))){
				//this isn't the best solution since the instructor could have submitted on behalf of the group, resulting in getting the instructors ID
				userId = cr.getProperties().getProperty(ResourceProperties.PROP_CREATOR);
			}else{						
				userId = this.getSubmitterId();
			}
			return userId;
		}

		public String getReviewReport() {
//			 Code to get updated report if default
			if (m_submittedAttachments.isEmpty()) { 
				M_log.debug(this.getId() + " getReviewReport No attachments submitted."); 
				return "Error";
			}
			else
			{
				try {
					ContentResource cr = getFirstAcceptableAttachement();
					if (cr == null )
					{
						M_log.debug(this + " getReviewReport No suitable attachments found in list");
						return "error";
					}
					
					String contentId = cr.getId();
					
					if (allowGradeSubmission(getReference()))
						return contentReviewService.getReviewReportInstructor(contentId, getAssignment().getReference(), UserDirectoryService.getCurrentUser().getId());
					else
						return contentReviewService.getReviewReportStudent(contentId, getAssignment().getReference(), UserDirectoryService.getCurrentUser().getId());
					
				} catch (Exception e) {
					M_log.warn(":getReviewReport() " + e.getMessage());
					return "Error";
				}
					
			}
			
		}

		/**
		 * SAK-26322 - Essentially the same as getReviewReport(), only it acts upon the ContentResource that is passed rather than that which is returned from getFirstAcceptableAttachment()
		 * TODO: consider removing getReviewReport(). If this is not possible, eliminate code duplication
		 */
		private String getReviewReport(ContentResource cr)
		{
			if (cr == null)
			{
				M_log.debug(this.getId() + " getReviewReport(ContentResource) called with cr == null");
				return "Error";
			}

			try
			{
				String contentId = cr.getId();
				if (allowGradeSubmission(getReference()))
				{
					return contentReviewService.getReviewReportInstructor(contentId, getAssignment().getReference(), UserDirectoryService.getCurrentUser().getId());
				}
				else
				{
					return contentReviewService.getReviewReportStudent(contentId, getAssignment().getReference(), UserDirectoryService.getCurrentUser().getId());
				}
			}
			catch (Exception e)
			{
				M_log.warn(":getReviewReport(ContentResource) " + e.getMessage());
				return "Error";
			}
		}
		
		//TODO: delete this and all calling methods if there are no repercussions
		private ContentResource getFirstAcceptableAttachement() {
			String contentId = null;
			try {
			for( int i =0; i < m_submittedAttachments.size();i++ ) {
				Reference ref = (Reference)m_submittedAttachments.get(i);
				ContentResource contentResource = (ContentResource)ref.getEntity();
				if (contentReviewService.isAcceptableContent(contentResource)) {
					return (ContentResource)contentResource;
				}
			}
			}
			catch (Exception e) {
				M_log.warn(":getFirstAcceptableAttachment() " + e.getMessage());
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * SAK-26322 - Gets all attachments in m_submittedAttachments that are acceptable to the content review service
		 */
		private List<ContentResource> getAllAcceptableAttachments()
		{
			List<ContentResource> attachments = new ArrayList<ContentResource>();
			for (int i = 0; i< m_submittedAttachments.size(); i++)
			{
				try
				{
					Reference ref = (Reference)m_submittedAttachments.get(i);
					ContentResource contentResource = (ContentResource)ref.getEntity();
					if (contentReviewService.isAcceptableContent(contentResource))
					{
						attachments.add((ContentResource)contentResource);
					}
				}
				catch (Exception e)
				{
					M_log.warn(":getAllAcceptableAttachments() " + e.getMessage());
					e.printStackTrace();
				}
			}
			return attachments;
		}
		
		public String getReviewStatus() {
			return m_reviewStatus;
		}

        public String getReviewError() {
        //	Code to get  error report
            if (m_submittedAttachments.isEmpty()) {
                M_log.debug(this.getId() + " getReviewError No attachments submitted.");
                return null;
            }
            else
            {
                try {
                    ContentResource cr = getFirstAcceptableAttachement();
                    if (cr == null )
                    {
                        M_log.debug(this + " getReviewError No suitable attachments found in list");
                        return null;
                    }

                    String contentId = cr.getId();

                    // This should use getLocalizedReviewErrorMessage(contentId)
                    // to get a i18n message of the error
                    Long status = contentReviewService.getReviewStatus(contentId);
                    String errorMessage = null; 
                    
                    if (status != null) {
                        if (status.equals(ContentReviewItem.REPORT_ERROR_NO_RETRY_CODE)) {
                            errorMessage = rb.getString("content_review.error.REPORT_ERROR_NO_RETRY_CODE");
                        } else if (status.equals(ContentReviewItem.REPORT_ERROR_RETRY_CODE)) {
                            errorMessage = rb.getString("content_review.error.REPORT_ERROR_RETRY_CODE");
                        } else if (status.equals(ContentReviewItem.SUBMISSION_ERROR_NO_RETRY_CODE)) {
                            errorMessage = rb.getString("content_review.error.SUBMISSION_ERROR_NO_RETRY_CODE");
                        } else if (status.equals(ContentReviewItem.SUBMISSION_ERROR_RETRY_CODE)) {
                            errorMessage = rb.getString("content_review.error.SUBMISSION_ERROR_RETRY_CODE");
                        } else if (status.equals(ContentReviewItem.SUBMISSION_ERROR_RETRY_EXCEEDED)) {
                            errorMessage = rb.getString("content_review.error.SUBMISSION_ERROR_RETRY_EXCEEDED_CODE");
                        } else if (status.equals(ContentReviewItem.SUBMISSION_ERROR_USER_DETAILS_CODE)) {
                            errorMessage = rb.getString("content_review.error.SUBMISSION_ERROR_USER_DETAILS_CODE");
                        } else if (ContentReviewItem.SUBMITTED_AWAITING_REPORT_CODE.equals(status)
                                || ContentReviewItem.NOT_SUBMITTED_CODE.equals(status)) {
                        	errorMessage = rb.getString("content_review.pending.info");
                        }
                    }
                    
                    if (errorMessage == null) {
                        errorMessage = rb.getString("content_review.error");
                    }
                    
                    return errorMessage;
                } catch (Exception e) {
                    //e.printStackTrace();
                    M_log.warn(this + ":getReviewError() " + e.getMessage());
                    return null;
                }

            }
        }

		/**
		 * SAK-26322 - Essentially the same as getReviewError(), only it acts upon the ContentResource that is passed rather than that which is returned from getFirstAcceptableAttachment()
		 */
		private String getReviewError(ContentResource cr)
		{
			if (cr == null)
			{
				M_log.debug(this.getId() + " getReviewReport(ContentResource) called with cr == null");
				return null;
			}
			try
			{
				String contentId = cr.getId();
				//This should use getLocalizedReviewErrorMesage(contentId)
				//to get a i18n message of the error
				Long status = contentReviewService.getReviewStatus(contentId);
				String errorMessage = null;

				// TODO: we can remove this null check if we use yoda statements below
				if (status != null)
				{
					if (status.equals(ContentReviewItem.REPORT_ERROR_NO_RETRY_CODE))
					{
						errorMessage = rb.getString("content_review.error.REPORT_ERROR_NO_RETRY_CODE");
					}
					else if (status.equals(ContentReviewItem.REPORT_ERROR_RETRY_CODE))
					{
						errorMessage = rb.getString("content_review.error.REPORT_ERROR_RETRY_CODE");
					}
					else if (status.equals(ContentReviewItem.SUBMISSION_ERROR_NO_RETRY_CODE))
					{
						errorMessage = rb.getString("content_review.error.SUBMISSION_ERROR_NO_RETRY_CODE");
					}
					else if (status.equals(ContentReviewItem.SUBMISSION_ERROR_RETRY_CODE))
					{
						errorMessage = rb.getString("content_review.error.SUBMISSION_ERROR_RETRY_CODE");
					}
					else if (status.equals(ContentReviewItem.SUBMISSION_ERROR_RETRY_EXCEEDED))
					{
						errorMessage = rb.getString("content_review.error.SUBMISSION_ERROR_RETRY_EXCEEDED_CODE");
					}
					else if (status.equals(ContentReviewItem.SUBMISSION_ERROR_USER_DETAILS_CODE))
					{
						errorMessage = rb.getString("content_review.error.SUBMISSION_ERROR_USER_DETAILS_CODE");
					}
					else if (ContentReviewItem.SUBMITTED_AWAITING_REPORT_CODE.equals(status) || ContentReviewItem.NOT_SUBMITTED_CODE.equals(status))
					{
						errorMessage = rb.getString("content_review.pending.info");
					}
				}

				if (errorMessage == null)
				{
					errorMessage = rb.getString("content_review.error");
				}

				return errorMessage;
			}
			catch (Exception e)
			{
				M_log.warn(this + ":getReviewError(ContentResource) " + e.getMessage());
				return null;
			}
		}


		public String getReviewIconUrl() {
			if (m_reviewIconUrl == null )
				m_reviewIconUrl = contentReviewService.getIconUrlforScore(Long.valueOf(this.getReviewScore()));
				
			return m_reviewIconUrl;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public List<ContentReviewResult> getContentReviewResults()
		{
			ArrayList<ContentReviewResult> reviewResults = new ArrayList<ContentReviewResult>();

			//get all the attachments for this submission and populate the reviewResults
			List<ContentResource> contentResources = getAllAcceptableAttachments();
			Iterator<ContentResource> itContentResources = contentResources.iterator();
			while (itContentResources.hasNext())
			{
				ContentResource cr = itContentResources.next();
				ContentReviewResult reviewResult = new ContentReviewResult();

				reviewResult.setContentResource(cr);
				int reviewScore = getReviewScore(cr);
				reviewResult.setReviewScore(reviewScore);
				reviewResult.setReviewReport(getReviewReport(cr));
				//skip review status, it's unused
				String iconUrl = contentReviewService.getIconUrlforScore(Long.valueOf(reviewScore));
				reviewResult.setReviewIconURL(iconUrl);
				reviewResult.setReviewError(getReviewError(cr));
				
				ContentReviewItem cri = findReportByContentId(cr.getId());
				reviewResult.setContentReviewItem(cri);

				if ("true".equals(cr.getProperties().getProperty(PROP_INLINE_SUBMISSION)))
				{
					reviewResults.add(0, reviewResult);
				}
				else
				{
					reviewResults.add(reviewResult);
				}
			}
			return reviewResults;
		}
		
		/**
		 * Gets a report from contentReviewService given its contentId
		 * This function should be provided by content-review in Sakai 12+,
		 * meanwhile in Sakai 11.x we need to get the full report list and iterate over it.
		 */
		private ContentReviewItem findReportByContentId(String contentId){
			String siteId = this.m_context;
			if(StringUtils.isBlank(contentId)){
				return null;
			}
			try{
				List<ContentReviewItem> reports = contentReviewService.getReportList(siteId);
				
				for(ContentReviewItem item : reports) {
					if(StringUtils.isNotBlank(item.getContentId()) && contentId.equals(item.getContentId())){
						return item;
					}
				}
			}catch(Exception e){
				M_log.error("Error getting reports list for site "+siteId, e);
			}
			return null;
		}
		
		/**
		 * constructor
		 */
		public BaseAssignmentSubmission()
		{
			m_properties = new BaseResourcePropertiesEdit();
		}// constructor
		
		/**
		 * Copy constructor.
		 */
		public BaseAssignmentSubmission(AssignmentSubmission submission)
		{
			setAll(submission);
		}

		/**
		 * Constructor used by addSubmission.
		 */
		public BaseAssignmentSubmission(String id, String assignId, String submitterId, String submitTime, String submitted, String graded)
		{
			
			// must set initial review status
			m_reviewStatus = "";
			m_reviewScore = -1;
			m_reviewReport = "Not available yet";
            m_reviewError = "";
			
			m_id = id;
			m_assignment = assignId;
			m_properties = new BaseResourcePropertiesEdit();
			addLiveProperties(m_properties);
			m_submitters = new ArrayList<String>();
			m_submissionLog = new ArrayList();
			m_grades = new ArrayList();
                        m_feedbackAttachments = m_entityManager.newReferenceList();
			m_submittedAttachments = m_entityManager.newReferenceList();
			m_submitted = false;
			m_returned = false;
			m_graded = false;
			m_gradedBy = null;
			m_gradeReleased = false;
			m_submittedText = "";
			m_feedbackComment = "";
			m_feedbackText = "";
			m_grade = "";
			m_timeLastModified = TimeService.newTime();
			
			// SAK-29314
			m_isUserSubmission = true;
			
                        m_submitterId = submitterId;

			if (submitterId == null)
			{
				String currentUser = SessionManager.getCurrentSessionUserId();
				if (currentUser == null) currentUser = "";
				m_submitters.add(currentUser);
                                m_submitterId = currentUser;
			}
			else
			{
				m_submitters.add(submitterId);
			}
			
			if (submitted != null)
			{
				m_submitted = Boolean.valueOf(submitted).booleanValue();
			}
			
			if (graded != null)
			{
				m_graded = Boolean.valueOf(graded).booleanValue();
			}
		}

		
		// todo work out what this does
		/**
		 * Reads the AssignmentSubmission's attribute values from xml.
		 * 
		 * @param s -
		 *        Data structure holding the xml info.
		 */
		public BaseAssignmentSubmission(Element el)
		{
			String tempString = null;
			Reference tempReference = null;

			M_log.debug(" BaseAssigmentSubmission : ENTERING STORAGE CONSTRUCTOR");

			m_id = el.getAttribute("id");
			m_context = el.getAttribute("context");

			String factor = StringUtils.trimToNull(el.getAttribute("scaled_factor"));
			if (factor == null) {
				factor = String.valueOf(AssignmentConstants.DEFAULT_SCALED_FACTOR);
			}
			m_factor = Integer.parseInt(factor);
			// %%%zqian
			// read the scaled grade point first; if there is none, get the old grade value
			String grade = StringUtils.trimToNull(el.getAttribute("scaled_grade"));
			if (grade == null)
			{
				grade = StringUtils.trimToNull(el.getAttribute("grade"));
				if (grade != null)
				{
					try
					{
						Integer.parseInt(grade);
						// for the grades in points, multiple those by factor
						grade = grade + factor.substring(1);
					}
					catch (Exception e)
					{
						M_log.warn(":BaseAssignmentSubmission(Element el) " + e.getMessage());
					}
				}
			}
			m_grade = grade;

			m_assignment = el.getAttribute("assignment");

			m_timeSubmitted = getTimeObject(el.getAttribute("datesubmitted"));
			m_timeReturned = getTimeObject(el.getAttribute("datereturned"));
			m_assignment = el.getAttribute("assignment");
			m_timeLastModified = getTimeObject(el.getAttribute("lastmod"));

			m_submitted = getBool(el.getAttribute("submitted"));
			m_returned = getBool(el.getAttribute("returned"));
			m_graded = getBool(el.getAttribute("graded"));
			m_gradedBy = el.getAttribute("gradedBy");
			m_gradeReleased = getBool(el.getAttribute("gradereleased"));
			m_honorPledgeFlag = getBool(el.getAttribute("pledgeflag"));
			m_hideDueDate = getBool(el.getAttribute("hideduedate"));

			m_submittedText = FormattedText.decodeFormattedTextAttribute(el, "submittedtext");
			m_feedbackComment = FormattedText.decodeFormattedTextAttribute(el, "feedbackcomment");
			m_feedbackText = FormattedText.decodeFormattedTextAttribute(el, "feedbacktext");

			// SAK-17606
			m_anonymousSubmissionId = el.getAttribute("anonymousSubmissionId");

			m_submitterId = el.getAttribute("submitterid");

			m_submissionLog = new ArrayList();
			m_grades = new ArrayList();
			m_submitters = new ArrayList<String>();
			m_submittedAttachments = m_entityManager.newReferenceList();
			m_feedbackAttachments = m_entityManager.newReferenceList();

			addElementsToList("log",m_submissionLog,el,false);
			addElementsToList("grade",m_grades,el,false);
			addElementsToList("submitter",m_submitters,el,false);
			// for backward compatibility of assignments without submitter ids
			if (m_submitterId == null && m_submitters.size() > 0) {
				m_submitterId = (String) m_submitters.get(0);
			}
			addElementsToList("feedbackattachment",m_feedbackAttachments,el,true);
			addElementsToList("submittedattachment",m_submittedAttachments,el,true);

			/* SAK-30644 - handle legacy submissions with no 'isUserSubmission' attribute gracefully.
				You must ensure that both m_submittedText and m_sumbittedAttachments have 
				been set prior to calling this method. If they are not set, this algorithm
				will likely return false negatives.
			*/
			getIsUserSubmission( el.getAttribute( SUBMISSION_ATTR_IS_USER_SUB ) );

			// READ THE PROPERTIES, SUBMITTED TEXT, FEEDBACK COMMENT, FEEDBACK TEXT
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
				}
				// old style encoding
				else if (element.getTagName().equals("submittedtext"))
				{
					if ((element.getChildNodes() != null) && (element.getChildNodes().item(0) != null))
					{
						m_submittedText = element.getChildNodes().item(0).getNodeValue();
						
							M_log.debug(" BaseAssignmentSubmission: CONSTRUCTOR : submittedtext : " + m_submittedText);
					}
					if (m_submittedText == null)
					{
						m_submittedText = "";
					}
				}
				// old style encoding
				else if (element.getTagName().equals("feedbackcomment"))
				{
					if ((element.getChildNodes() != null) && (element.getChildNodes().item(0) != null))
					{
						m_feedbackComment = element.getChildNodes().item(0).getNodeValue();
						
							M_log.debug(" BaseAssignmentSubmission: CONSTRUCTOR : feedbackcomment : "
									+ m_feedbackComment);
					}
					if (m_feedbackComment == null)
					{
						m_feedbackComment = "";
					}
				}
				// old style encoding
				else if (element.getTagName().equals("feedbacktext"))
				{
					if ((element.getChildNodes() != null) && (element.getChildNodes().item(0) != null))
					{
						m_feedbackText = element.getChildNodes().item(0).getNodeValue();
						
							M_log.debug(" BaseAssignmentSubmission: CONSTRUCTOR : FEEDBACK TEXT : " + m_feedbackText);
					}
					if (m_feedbackText == null)
					{
						m_feedbackText = "";
					}
				}
			}

		
			m_reviewScore = -1;
			m_reviewReport = "no report available";
			m_reviewStatus = "";
            m_reviewError = "";
			
			//get the review Status from ContentReview rather than using old ones
			if (contentReviewService != null) {
				m_reviewStatus = this.getReviewStatus();
				m_reviewScore  = this.getReviewScore();
                m_reviewError = this.getReviewError();
			}
			
			
			
			M_log.debug(" BaseAssignmentSubmission: LEAVING STORAGE CONSTRUCTOR");

		}// storage constructor

		/**
		 * Handle legacy submissions with no 'isUserSubmission' attribute gracefully.
		 * You must ensure that both m_submittedText and m_sumbittedAttachments have 
		 * been set prior to calling this method. If they are not set, this algorithm
		 * will likely return false negatives.
		 * 
		 * @see SAK-30644
		 */
		private void getIsUserSubmission( String isUserSubmission )
		{
			if( StringUtils.isBlank( isUserSubmission ) )
			{
				// Initialize the list if it's null, to avoid NPE's in check below
				if( m_submittedAttachments == null )
				{
					m_submittedAttachments = m_entityManager.newReferenceList();
				}

				// If there is submitted text, attachments, or if the type is 'non-electronic', this is considered an actual user submission
				m_isUserSubmission = StringUtils.isNotBlank( m_submittedText ) || 
									 getAssignment().getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION ||
									 !m_submittedAttachments.isEmpty();
			}
			else
			{
				m_isUserSubmission = getBool( isUserSubmission );
			}
		}

		/**
		 * @param services
		 * @return
		 */
		public ContentHandler getContentHandler(Map<String, Object> services)
		{
			final Entity thisEntity = this;
			return new DefaultEntityHandler()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.sakaiproject.util.DefaultEntityHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
				{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if ("submission".equals(qName) && entity == null)
						{
							m_reviewScore = -1;
							m_reviewReport = "no report available";
							m_reviewStatus = "";
							m_reviewError = "";
							String intString = null;
							String attributeString = null;
							String tempString = null;
							Reference tempReference = null;

							m_id = attributes.getValue("id");
							// M_log.info(this + " BASE SUBMISSION : CONSTRUCTOR : m_id : " + m_id);
							m_context = attributes.getValue("context");
							// M_log.info(this + " BASE SUBMISSION : CONSTRUCTOR : m_context : " + m_context);

							String factor = StringUtils.trimToNull(attributes.getValue("scaled_factor"));
							if (factor == null) {
								factor = String.valueOf(AssignmentConstants.DEFAULT_SCALED_FACTOR);
							}
							// %%%zqian
							// read the scaled grade point first; if there is none, get the old grade value
							String grade = StringUtils.trimToNull(attributes.getValue("scaled_grade"));
							if (grade == null)
							{
								grade = StringUtils.trimToNull(attributes.getValue("grade"));
								if (grade != null)
								{
									try
									{
										Integer.parseInt(grade);
										// for the grades in points, multiple those by factor
										grade = grade + factor.substring(1);
									}
									catch (Exception e)
									{
										M_log.warn(":BaseAssignmentSubmission:getContentHanler:DefaultEnityHandler " + e.getMessage());
									}
								}
							}
							m_grade = grade;

							m_assignment = attributes.getValue("assignment");

							m_timeSubmitted = getTimeObject(attributes.getValue("datesubmitted"));
							m_timeReturned = getTimeObject(attributes.getValue("datereturned"));
							m_assignment = attributes.getValue("assignment");
							m_timeLastModified = getTimeObject(attributes.getValue("lastmod"));

							m_submitted = getBool(attributes.getValue("submitted"));
							m_returned = getBool(attributes.getValue("returned"));
							m_graded = getBool(attributes.getValue("graded"));
							m_gradedBy = attributes.getValue("gradedBy");
							m_gradeReleased = getBool(attributes.getValue("gradereleased"));
							m_honorPledgeFlag = getBool(attributes.getValue("pledgeflag"));
							m_hideDueDate = getBool(attributes.getValue("hideduedate"));

							m_submittedText = formattedTextDecodeFormattedTextAttribute(attributes, "submittedtext");
							m_feedbackComment = formattedTextDecodeFormattedTextAttribute(attributes, "feedbackcomment");
							m_feedbackText = formattedTextDecodeFormattedTextAttribute(attributes, "feedbacktext");

							// SAK-17606
							m_anonymousSubmissionId = m_id.substring(27)+" (" + rb.getString("grading.anonymous.title")  + ")";

							m_submitterId = attributes.getValue("submitterid");

							m_submissionLog = new ArrayList();
							m_grades = new ArrayList();
							m_submitters = new ArrayList();
							m_feedbackAttachments = m_entityManager.newReferenceList();
							m_submittedAttachments = m_entityManager.newReferenceList();

							addElementsToList("log",m_submissionLog,attributes,false);
							addElementsToList("grade",m_grades,attributes,false);
							addElementsToList("submitter",m_submitters,attributes,false);
							// for backward compatibility of assignments without submitter ids
							if (m_submitterId == null && m_submitters.size() > 0) {
								m_submitterId = (String) m_submitters.get(0);
							}
							addElementsToList("feedbackattachment",m_feedbackAttachments,attributes,true);
							addElementsToList("submittedattachment",m_submittedAttachments,attributes,true);

							/* SAK-30644 - handle legacy submissions with no 'isUserSubmission' attribute gracefully.
								You must ensure that both m_submittedText and m_sumbittedAttachments have 
								been set prior to calling this method. If they are not set, this algorithm
								will likely return false negatives.
							*/
							getIsUserSubmission( attributes.getValue( SUBMISSION_ATTR_IS_USER_SUB ) );

							entity = thisEntity;
						}
					}
				}
			};
		}

		
		/**
		 * Takes the AssignmentContent's attribute values and puts them into the xml document.
		 * 
		 * @param s -
		 *        Data structure holding the object to be stored.
		 * @param doc -
		 *        The xml document.
		 */
		public Element toXml(Document doc, Stack stack)
		{
		    if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission : ENTERING TOXML");

			Element submission = doc.createElement("submission");
			if (stack.isEmpty())
			{
				doc.appendChild(submission);
			}
			else
			{
				((Element) stack.peek()).appendChild(submission);
			}

			stack.push(submission);

			String numItemsString = null;
			String attributeString = null;
			String itemString = null;
			Reference tempReference = null;

			// SAK-13408 -The XML implementation in Websphere throws an LSException if the
			// attribute is null, while in Tomcat it assumes an empty string. The following
			// sets the attribute to an empty string if the value is null. 			
			submission.setAttribute("id", m_id == null ? "" : m_id);
			submission.setAttribute("context", m_context == null ? "" : m_context);
			submission.setAttribute("scaled_grade", m_grade == null ? "" : m_grade);
			submission.setAttribute("scaled_factor", String.valueOf(m_factor));
			submission.setAttribute("assignment", m_assignment == null ? "" : m_assignment);

			submission.setAttribute("datesubmitted", getTimeString(m_timeSubmitted));
			submission.setAttribute("datereturned", getTimeString(m_timeReturned));
			submission.setAttribute("lastmod", getTimeString(m_timeLastModified));
			submission.setAttribute("submitted", getBoolString(m_submitted));
			submission.setAttribute("returned", getBoolString(m_returned));
			submission.setAttribute("graded", getBoolString(m_graded));
			submission.setAttribute("gradedBy", m_gradedBy == null ? "" : m_gradedBy);
			submission.setAttribute("gradereleased", getBoolString(m_gradeReleased));
			submission.setAttribute("pledgeflag", getBoolString(m_honorPledgeFlag));
			submission.setAttribute("hideduedate", getBoolString(m_hideDueDate));

			// SAK-17606
			submission.setAttribute("anonymousSubmissionId", m_anonymousSubmissionId);

			// SAK-29314
			submission.setAttribute(SUBMISSION_ATTR_IS_USER_SUB, getBoolString(m_isUserSubmission));

			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: SAVED REGULAR PROPERTIES");

			submission.setAttribute("submitterid", m_submitterId == null ? "": m_submitterId);

			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: SAVED SUBMITTER ID : " + m_submitterId);

			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: # logs " + m_submissionLog.size());
			for (int x = 0; x < m_submissionLog.size(); x++)
			{
			    attributeString = "log" + x;
			    itemString = (String) m_submissionLog.get(x);
			    if (itemString != null) {
			        submission.setAttribute(attributeString, itemString);
			    }
			}
			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: # grades " + m_grades.size());
			for (int x = 0; x < m_grades.size(); x++)
			{
			    attributeString = "grade" + x;
			    itemString = (String) m_grades.get(x);
			    if (itemString != null) {
			        submission.setAttribute(attributeString, itemString);
			    }
			}
			// SAVE THE SUBMITTERS
			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: # submitters " + m_submitters.size());
			for (int x = 0; x < m_submitters.size(); x++)
			{
				attributeString = "submitter" + x;
				itemString = (String) m_submitters.get(x);
				if (itemString != null) {
				    submission.setAttribute(attributeString, itemString);
				}
			}

			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: SAVED SUBMITTERS");

			// SAVE THE FEEDBACK ATTACHMENTS
			if (M_log.isDebugEnabled()) M_log.debug("DB : DbCachedStorage : DbCachedAssignmentSubmission : entering fb attach loop : size : "
						+ m_feedbackAttachments.size());
			for (int x = 0; x < m_feedbackAttachments.size(); x++)
			{
				attributeString = "feedbackattachment" + x;
				tempReference = (Reference) m_feedbackAttachments.get(x);
				itemString = tempReference.getReference();
				if (itemString != null) {
				    submission.setAttribute(attributeString, itemString);
				}
			}

			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: SAVED FEEDBACK ATTACHMENTS");

			// SAVE THE SUBMITTED ATTACHMENTS
			for (int x = 0; x < m_submittedAttachments.size(); x++)
			{
				attributeString = "submittedattachment" + x;
				tempReference = (Reference) m_submittedAttachments.get(x);
				itemString = tempReference.getReference();
				if (itemString != null) {
				    submission.setAttribute(attributeString, itemString);
				}
			}

			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: SAVED SUBMITTED ATTACHMENTS");

			// SAVE THE PROPERTIES
			m_properties.toXml(doc, stack);
			stack.pop();

			FormattedText.encodeFormattedTextAttribute(submission, "submittedtext", m_submittedText);
			FormattedText.encodeFormattedTextAttribute(submission, "feedbackcomment", m_feedbackComment);
			FormattedText.encodeFormattedTextAttribute(submission, "feedbacktext", m_feedbackText);

			if (M_log.isDebugEnabled()) M_log.debug(this + " BaseAssignmentSubmission: LEAVING TOXML");

			return submission;

		}// toXml

		
		protected void setAll(AssignmentSubmission submission)
		{
			
			if (contentReviewService != null) {
				m_reviewScore = submission.getReviewScore();
				// The report given by the content review service
				m_reviewReport = submission.getReviewReport();
				// The status of the review service
				m_reviewStatus = submission.getReviewStatus();
				// Error msg, if any from review service
				m_reviewError = submission.getReviewError();
			}
			
			m_id = submission.getId();
			m_context = submission.getContext();
			m_assignment = submission.getAssignmentId();
			m_grade = submission.getGrade();
			m_submitters = submission.getSubmitterIds();
			m_submitted = submission.getSubmitted();
			m_timeSubmitted = submission.getTimeSubmitted();
			m_timeReturned = submission.getTimeReturned();
			m_timeLastModified = submission.getTimeLastModified();
			m_submittedAttachments = submission.getSubmittedAttachments();
			m_feedbackAttachments = submission.getFeedbackAttachments();
			m_submittedText = submission.getSubmittedText();
			m_submitterId = submission.getSubmitterId();
			m_submissionLog = submission.getSubmissionLog();
			m_grades = submission.getGrades();
			m_feedbackComment = submission.getFeedbackComment();
			m_feedbackText = submission.getFeedbackText();
			m_returned = submission.getReturned();
			m_graded = submission.getGraded();
			m_gradedBy = submission.getGradedBy();
			m_gradeReleased = submission.getGradeReleased();
			m_honorPledgeFlag = submission.getHonorPledgeFlag();
			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(submission.getProperties());

			// SAK-17606
			m_anonymousSubmissionId = submission.getAnonymousSubmissionId();

			// SAK-29314
			m_isUserSubmission = submission.isUserSubmission();
		}

		/**
		 * Access the URL which can be used to access the resource.
		 * 
		 * @return The URL which can be used to access the resource.
		 */
		public String getUrl()
		{
			return getAccessPoint(false) + Entity.SEPARATOR + "s" + Entity.SEPARATOR + m_context + Entity.SEPARATOR + m_id;

		} // getUrl

		/**
		 * Access the internal reference which can be used to access the resource from within the system.
		 * 
		 * @return The the internal reference which can be used to access the resource from within the system.
		 */
		public String getReference()
		{
			return submissionReference(m_context, m_id, m_assignment);

		} // getReference

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * Access the id of the resource.
		 * 
		 * @return The id.
		 */
		public String getId()
		{
			return m_id;
		}

		/**
		 * Access the resource's properties.
		 * 
		 * @return The resource's properties.
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * AssignmentSubmission implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * Access the AssignmentSubmission's context at the time of creation.
		 * 
		 * @return String - the context string.
		 */
		public String getContext()
		{
			return m_context;
		}

		/**
		 * Access the Assignment for this Submission
		 * 
		 * @return the Assignment
		 */
		public Assignment getAssignment()
		{
			if (m_asn == null && m_assignment != null) // lazy load assignment as needed, store for future
			{
				m_asn = m_assignmentStorage.get(m_assignment);
			}
			
			// track event
			//EventTrackingService.post(EventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT, retVal.getReference(), false));

			return m_asn;
		}
		
		/**
		 * call this method to store the assignment object to avoid costly lookup by assignment id later
		 * will do nothing if assignment ids don't match
		 */
		public void setAssignment(Assignment value)
		{
			if (m_assignment != null && value != null && m_assignment.equals(value.getId()))
			{
				m_asn = value;
			}
		}

		/**
		 * Access the Id for the Assignment for this Submission
		 * 
		 * @return String - the Assignment Id
		 */
		public String getAssignmentId()
		{
			return m_assignment;
		}

		/**
		 * Get whether this is a final submission.
		 * 
		 * SAK-30174 To non-electronic assignement, submissions is always submitted.
		 * @return True if a final submission, false if still a draft.
		 */
		public boolean getSubmitted()
		{
			return m_submitted || getAssignment().getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION;
		}

		public String getSubmitterId() {
		    return m_submitterId;
		}
		public List getSubmissionLog() {
		    return m_submissionLog;
		}
		public List getGrades() {
		    return m_grades;
		}
		public String getGradeForUser(String id) {
		    if (m_grades != null) {
		        Iterator<String> _it = m_grades.iterator();
		        while (_it.hasNext()) {
		            String _s = _it.next();
		            if (_s.startsWith(id + "::")) {
		                //return _s.endsWith("null") ? null: _s.substring(_s.indexOf("::") + 2);
		            	if(_s.endsWith("null"))
		            	{
		            		return null;
		            	}
		            	else
		            	{
		            		String grade=_s.substring(_s.indexOf("::") + 2);
		            		if (grade != null && grade.length() > 0 && !"0".equals(grade))
		    				{
		    					int factor = getAssignment().getContent().getFactor();
		    					int dec = (int)Math.log10(factor);
		    					String decSeparator = FormattedText.getDecimalSeparator();
		    					String decimalGradePoint = "";
		    					try
		    					{
		    						Integer.parseInt(grade);
		    						// if point grade, display the grade with factor decimal place
		    						int length = grade.length();
		    						if (length > dec) {
		    							decimalGradePoint = grade.substring(0, grade.length() - dec) + decSeparator + grade.substring(grade.length() - dec);
		    						}
		    						else {
		    							String newGrade = "0".concat(decSeparator);
		    							for (int i = length; i < dec; i++) {
		    								newGrade = newGrade.concat("0");
		    							}
		    							decimalGradePoint = newGrade.concat(grade);
		    						}
		    					}
		    					catch (NumberFormatException e) {
		    						try {
		    							Float.parseFloat(grade);
		    							decimalGradePoint = grade;
		    						}
		    						catch (Exception e1) {
		    							return grade;
		    						}
		    					}
		    					// get localized number format
		    					NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,false);
		    					DecimalFormat dcformat = (DecimalFormat) nbFormat;
		    					// show grade in localized number format
		    					try {
		    						Double dblGrade = dcformat.parse(decimalGradePoint).doubleValue();
		    						decimalGradePoint = nbFormat.format(dblGrade);
		    					}
		    					catch (Exception e) {
		    						return grade;
		    					}
		    					return decimalGradePoint;
		    				}
		    				else
		    				{
		    					return StringUtils.trimToEmpty(grade);
		    				}
		            	}
		            }
		        }
		    }
		    return null;
		}

		/**
		 * 
		 * @return Array of User objects.
		 */
		public User[] getSubmitters() {
			List<User> retVal = new ArrayList();
			for (String userId : getSubmitterIds()) {
				try {
					retVal.add(UserDirectoryService.getUser(userId));
				} catch (Exception e) {
					M_log.warn(" BaseAssignmentSubmission getSubmitters" + e.getMessage() + userId);
				}
			}
			// compare users on sortname
			java.util.Collections.sort(retVal, new UserComparator());

			// get the User[] array
			int size = retVal.size();
			User[] rv = new User[size];
			for(int k = 0; k<size; k++)
			{
				rv[k] = (User) retVal.get(k);
			}
			
			return rv;
		}

		/**
		 * Access the list of Users who submitted this response to the Assignment.
		 * 
		 * @return FlexStringArray of user ids.
		 */
		public List<String> getSubmitterIds()
		{
		    Assignment a = getAssignment();
		    if (a.isGroup()) {
		        try {
		            Site site = SiteService.getSite(a.getContext());
		            Group _g = site.getGroup(m_submitterId);
		            if (_g !=null) {
		                return getSubmitterIdList("false", _g.getId(), null, a.getReference(), a.getContext());
		            }
		    	} catch (IdUnusedException _iue) {
		            return null;
		        }
		    } else { 
		        return m_submitters;
		    }
		    return new ArrayList();
		}

		/**
		 * {@inheritDoc}
		 */
		public String getSubmitterIdString ()
		{
			String rv = "";
			if (m_submitters != null)
			{
				for (int j = 0; j < m_submitters.size(); j++)
				{
					rv = rv.concat((String) m_submitters.get(j));
				}
			}
			return rv;
		}
		
		/**
		 * Set the time at which this response was submitted; null signifies the response is unsubmitted.
		 * 
		 * @return Time of submission.
		 */
		public Time getTimeSubmitted()
		{
			return m_timeSubmitted;
		}

		/**
		 * @inheritDoc
		 */
		public String getTimeSubmittedString()
		{
			if ( m_timeSubmitted == null )
				return "";
			else
				return m_timeSubmitted.toStringLocalFull();
		}

		/**
		 * Get whether the grade has been released.
		 * 
		 * @return True if the Submissions's grade has been released, false otherwise.
		 */
		public boolean getGradeReleased()
		{
			return m_gradeReleased;
		}

		/**
		 * Access the grade recieved.
		 * 
		 * @return The Submission's grade..
		 */
		public String getGrade()
		{
			return getGrade(true);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String getGrade(boolean overrideWithGradebookValue)
		{
			String rv = m_grade;
			
			if (!overrideWithGradebookValue)
			{
				// use assignment submission grade
				return m_grade;
			}
			else
			{
				String gradeGB=this.getGradeForUserInGradeBook(null);
				if(gradeGB!=null)
				{
					if(!gradeGB.equals(""))
					{
						rv=gradeGB;
					}
					else
					{
						rv=null;
					}
				}
			}
			return rv;
		}

		/**
		 * Access the grade recieved.
		 * 
		 * @return The Submission's grade..
		 */
		public String getGradeDisplay()
		{
			Assignment m = getAssignment();
			return getGradeDisplay(m.getContent().getTypeOfGrade());
		}
		
		public String getGradeDisplay(int typeOfGrade)
		{	
			String grade = getGrade();
			if (typeOfGrade == Assignment.SCORE_GRADE_TYPE)
			{
				if (grade != null && grade.length() > 0 && !"0".equals(grade))
				{
					int factor = getAssignment().getContent().getFactor();
					int dec = (int)Math.log10(factor);
					String decSeparator = FormattedText.getDecimalSeparator();
					String decimalGradePoint = "";
					try
					{
						Integer.parseInt(grade);
						// if point grade, display the grade with factor decimal place
						int length = grade.length();
						if (length > dec) {
							decimalGradePoint = grade.substring(0, grade.length() - dec) + decSeparator + grade.substring(grade.length() - dec);
						}
						else {
							String newGrade = "0".concat(decSeparator);
							for (int i = length; i < dec; i++) {
								newGrade = newGrade.concat("0");
							}
							decimalGradePoint = newGrade.concat(grade);
						}
					}
					catch (NumberFormatException e) {
						try {
							Float.parseFloat(grade);
							decimalGradePoint = grade;
						}
						catch (Exception e1) {
							return grade;
						}
					}
					// get localized number format
					NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,false);
					DecimalFormat dcformat = (DecimalFormat) nbFormat;
					// show grade in localized number format
					try {
						Double dblGrade = dcformat.parse(decimalGradePoint).doubleValue();
						decimalGradePoint = nbFormat.format(dblGrade);
					}
					catch (Exception e) {
						return grade;
					}
					return decimalGradePoint;
				}
				else
				{
					return StringUtils.trimToEmpty(grade);
				}
			}
			else if (typeOfGrade == Assignment.UNGRADED_GRADE_TYPE) {
				String ret = "";
				if (grade != null) {
					if (grade.equalsIgnoreCase("gen.nograd")) ret = rb.getString("gen.nograd");
				}
				return ret;
			}
			else if (typeOfGrade == Assignment.PASS_FAIL_GRADE_TYPE) {
				String ret = rb.getString("ungra");
				if (grade != null) {
					if (grade.equalsIgnoreCase("Pass")) ret = rb.getString("pass");
					else if (grade.equalsIgnoreCase("Fail")) ret = rb.getString("fail");
				}
				return ret;
			}
			else if (typeOfGrade == Assignment.CHECK_GRADE_TYPE) {
				String ret = rb.getString("ungra");
				if (grade != null) {
					if (grade.equalsIgnoreCase("Checked")) ret = rb.getString("gen.checked");
				}
				return ret;
			}
			else
			{
				if (grade != null && grade.length() > 0)
				{
					return StringUtils.trimToEmpty(grade);
				}
				else
				{
					// return "ungraded" in stead
					return rb.getString("ungra");
				}
			}
		}



		public String getGradeForUserInGradeBook(String userId)
		{
			String rv =null;
			if (userId == null)
			{
				userId = m_submitterId;
			}
			Assignment m = getAssignment();
			String gAssignmentName = StringUtils.trimToEmpty(m.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
			String gradebookUid = m.getContext();
			org.sakaiproject.service.gradebook.shared.Assignment gradebookAssignment = m_gradebookService.getAssignment(gradebookUid, gAssignmentName);
			if( gradebookAssignment != null )
			{
				final GradeDefinition def = m_gradebookService.getGradeDefinitionForStudentForItem(gradebookUid, gradebookAssignment.getId() , userId);
				String gString=def.getGrade();
				try
				{
					if (gString != null)
					{
						String decSeparator = FormattedText.getDecimalSeparator();
						rv = StringUtils.replace(gString, (",".equals(decSeparator)?".":","), decSeparator);
						NumberFormat nbFormat = FormattedText.getNumberFormat((int)Math.log10(m.getContent().getFactor()),(int)Math.log10(m.getContent().getFactor()),false);
						DecimalFormat dcformat = (DecimalFormat) nbFormat;
						Double dblGrade = dcformat.parse(rv).doubleValue();
						rv = nbFormat.format(dblGrade);
					}
					
					/*
					 * SAK-32201 - We need to know if 'userId' is an id of a Group because
					 * there is'nt a correspondence between the general grade of a group
					 * in an assignment with Group Submission and any grade from Gradebook so always is null.
					 */
					
					else if(m_gradeReleased)
					{
						Site site = SiteService.getSite(m.getContext());
						if (site.getGroup(userId)==null)
						{
							rv="";
						}
					}
				}
				catch (Exception e)
				{
					M_log.warn(" BaseAssignmentSubmission getGradeFromGradeBook  "+ e.getMessage());
				}
			}
			return rv;
		}

		/**
		 * Get the time of last modification;
		 * 
		 * @return The time of last modification.
		 */
		public Time getTimeLastModified()
		{
			return m_timeLastModified;
		}

		/**
		 * Text submitted in response to the Assignment.
		 * 
		 * @return The text of the submission.
		 */
		public String getSubmittedText()
		{
			return m_submittedText;
		}

		/**
		 * Access the list of attachments to this response to the Assignment.
		 * 
		 * @return ReferenceVector of the list of attachments as Reference objects;
		 */
		public List getSubmittedAttachments()
		{
			return m_submittedAttachments;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public List getVisibleSubmittedAttachments()
		{
			List visibleAttachments = new ArrayList();
			if (m_submittedAttachments != null)
			{
				Iterator itAttachments = m_submittedAttachments.iterator();
				while (itAttachments.hasNext())
				{
					Reference attachment = (Reference) itAttachments.next();
					ResourceProperties props = attachment.getProperties();
					if (!"true".equals(props.getProperty(PROP_INLINE_SUBMISSION)))
					{
						visibleAttachments.add(attachment);
					}
				}
			}
			return visibleAttachments;
		}

		/**
		 * Get the general comments by the grader
		 * 
		 * @return The text of the grader's comments; may be null.
		 */
		public String getFeedbackComment()
		{	
			return m_feedbackComment;
		}

		/**
		 * Access the text part of the instructors feedback; usually an annotated copy of the submittedText
		 * 
		 * @return The text of the grader's feedback.
		 */
		public String getFeedbackText()
		{
			return m_feedbackText;
		}

		/**
		 * Access the formatted text part of the instructors feedback; usually an annotated copy of the submittedText
		 * 
		 * @return The formatted text of the grader's feedback.
		 */
		public String getFeedbackFormattedText()
		{
			if (m_feedbackText == null || m_feedbackText.length() == 0) 
				return m_feedbackText;

			String value = fixAssignmentFeedback(m_feedbackText);

			StringBuffer buf = new StringBuffer(value);
			int pos = -1;

			while ((pos = buf.indexOf("{{")) != -1)
			{
				buf.replace(pos, pos + "{{".length(), "<span class='highlight'>");
			}

			while ((pos = buf.indexOf("}}")) != -1)
			{
				buf.replace(pos, pos + "}}".length(), "</span>");
			}

			return FormattedText.escapeHtmlFormattedText(buf.toString());
		}

		/**
		 * Apply the fix to pre 1.1.05 assignments submissions feedback.
		 */
		private String fixAssignmentFeedback(String value)
		{
			if (value == null || value.length() == 0) return value;
			
			StringBuffer buf = new StringBuffer(value);
			int pos = -1;
			
			// <br/> -> \n
			while ((pos = buf.indexOf("<br/>")) != -1)
			{
				buf.replace(pos, pos + "<br/>".length(), "\n");
			}
			
			// <span class='chefAlert'>( -> {{
			while ((pos = buf.indexOf("<span class='chefAlert'>(")) != -1)
			{
				buf.replace(pos, pos + "<span class='chefAlert'>(".length(), "{{");
			}
			
			// )</span> -> }}
			while ((pos = buf.indexOf(")</span>")) != -1)
			{
				buf.replace(pos, pos + ")</span>".length(), "}}");
			}
			
			while ((pos = buf.indexOf("<ins>")) != -1)
			{
				buf.replace(pos, pos + "<ins>".length(), "{{");
			}
			
			while ((pos = buf.indexOf("</ins>")) != -1)
			{
				buf.replace(pos, pos + "</ins>".length(), "}}");
			}
			
			return buf.toString();
			
		} // fixAssignmentFeedback

		/**
		 * Access the list of attachments returned to the students in the process of grading this assignment; usually a modified or annotated version of the attachment submitted.
		 * 
		 * @return ReferenceVector of the Resource objects pointing to the attachments.
		 */
		public List getFeedbackAttachments()
		{
			return m_feedbackAttachments;
		}

		/**
		 * Get whether this Submission was rejected by the grader.
		 * 
		 * @return True if this response was rejected by the grader, false otherwise.
		 */
		public boolean getReturned()
		{
			return m_returned;
		}

		/**
		 * Get whether this Submission has been graded.
		 * 
		 * @return True if the submission has been graded, false otherwise.
		 */
		public boolean getGraded()
		{
			return m_graded;
		}
		
		/**
		 * Get the grader id (used to determine auto or instructor grading)
		 */
		public String getGradedBy(){
			return m_gradedBy;
		}

		/**
		 * Get the time on which the graded submission was returned; null means the response is not yet graded.
		 * 
		 * @return the time (may be null)
		 */
		public Time getTimeReturned()
		{
			return m_timeReturned;
		}

		/**
		 * Access the checked status of the honor pledge flag.
		 * 
		 * @return True if the honor pledge is checked, false otherwise.
		 */
		public boolean getHonorPledgeFlag()
		{
			return m_honorPledgeFlag;
		}

		/**
		 * Returns the status of the submission : Not Started, submitted, returned or graded.
		 * 
		 * @return The Submission's status.
		 */
		public String getStatus()
		{
			Assignment assignment = getAssignment();
			boolean allowGrade = assignment != null ? allowGradeSubmission(assignment.getReference()):false;
			String retVal = "";
			
			Time submitTime = getTimeSubmitted();
			Time returnTime = getTimeReturned();
			Time lastModTime = getTimeLastModified();
		
			if (getSubmitted() || (!getSubmitted() && allowGrade))
			{
				if (submitTime != null)
				{
					if (getReturned())
					{
						if (returnTime != null && returnTime.before(submitTime))
						{
							if (!getGraded())
							{
								retVal = rb.getString("gen.resub") + " " + submitTime.toStringLocalFull();
								if (submitTime.after(getAssignment().getDueTime()))
									retVal = retVal + rb.getString("gen.late2");
							}
									
							else
								retVal = rb.getString("gen.returned");
						}
						else
							retVal = rb.getString("gen.returned");
					}
					else if (getGraded() && allowGrade)
					{
							retVal = getGradeOrComment();
					}
					else 
					{
						if (allowGrade)
						{
							// ungraded submission
							retVal = rb.getString("ungra");
						}
						else
						{
							// submitted
							retVal = rb.getString("gen.subm4");
							
							if(submitTime != null)
							{
								retVal = rb.getString("gen.subm4") + " " + submitTime.toStringLocalFull();
							}
						}
					}
				}
				else
				{
					if (getReturned())
					{
						// instructor can return grading to non-submitted user
						retVal = rb.getString("gen.returned");
					}
					else if (getGraded() && allowGrade)
					{
						// instructor can grade non-submitted ones
						retVal = getGradeOrComment();
					}
					else
					{
						if (allowGrade)
						{
							// show "no submission" to graders
							retVal = rb.getString("listsub.nosub");
						}
						else
						{
							// show "not started" to students
							retVal = rb.getString("gen.notsta");
						}
					}
				}
			}
			else
			{
				if (getGraded())
				{
					if (getReturned())
					{
						if (lastModTime != null && returnTime != null && lastModTime.after(TimeService.newTime(returnTime.getTime() + 1000 * 10)) && !allowGrade)
						{
							// working on a returned submission now
							retVal = rb.getString("gen.dra2") + " " + rb.getString("gen.inpro");
						}
						else
						{
							// not submitted submmission has been graded and returned
							retVal = rb.getString("gen.returned");
						}
					}
					else if (allowGrade){
						// grade saved but not release yet, show this to graders
						retVal = getGradeOrComment();
					}else{
						// submission saved, not submitted.
						retVal = rb.getString("gen.dra2") + " " + rb.getString("gen.inpro");
					}
				}
				else	
				{
					if (allowGrade)
						retVal = rb.getString("ungra");
					else
						// submission saved, not submitted.
						retVal = rb.getString("gen.dra2") + " " + rb.getString("gen.inpro");
				}
			}

			return retVal;
		}

		private String getGradeOrComment() {
			String retVal;
			if (getGrade() != null && getGrade().length() > 0)
				retVal = rb.getString("grad3");
			else
				retVal = rb.getString("gen.commented");
			return retVal;
		}

		/**
		 * Are these objects equal? If they are both AssignmentSubmission objects, and they have matching id's, they are.
		 * 
		 * @return true if they are equal, false if not.
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof AssignmentSubmission)) return false;
			return ((AssignmentSubmission) obj).getId().equals(getId());

		} // equals

		/**
		 * Make a hash code that reflects the equals() logic as well. We want two objects, even if different instances, if they have the same id to hash the same.
		 */
		public int hashCode()
		{
			return getId().hashCode();

		} // hashCode

		/**
		 * Compare this object with the specified object for order.
		 * 
		 * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
		 */
		public int compareTo(Object obj)
		{
			if (!(obj instanceof AssignmentSubmission)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// start the compare by comparing their sort names
			int compare = getTimeSubmitted().toString().compareTo(((AssignmentSubmission) obj).getTimeSubmitted().toString());

			// if these are the same
			if (compare == 0)
			{
				// sort based on (unique) id
				compare = getId().compareTo(((AssignmentSubmission) obj).getId());
			}

			return compare;

		} // compareTo
		
		/**
		 * {@inheritDoc}
		 */
		public int getResubmissionNum()
		{
			String numString = StringUtils.trimToNull(m_properties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER));
			return numString != null?Integer.valueOf(numString).intValue():0;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public Time getCloseTime()
		{
			String closeTimeString = StringUtils.trimToNull(m_properties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME));
			if (closeTimeString != null && getResubmissionNum() != 0)
			{
				// return the close time if it is set
				return TimeService.newTime(Long.parseLong(closeTimeString));
			}
			else
			{
				// else use the assignment close time setting
				Assignment a = getAssignment();
				return a!=null?a.getCloseTime():null;	
			}
		}
		
		/**
		 * SAK-17606 - Method to return a speacialized string for anonymous grading.
		 * @return
		 */
		public String getAnonymousSubmissionId() {
				String anonTitle = rb.getString("grading.anonymous.title") ;
				return this.getId().substring(27) + " (" + anonTitle + ")";
		}

		/**
		 * SAK-29314 - Determines whether this submission was submitted by a user or by the system
		 */
		public boolean isUserSubmission()
		{
			return m_isUserSubmission;
		}
		
	} // AssignmentSubmission
	
	/***************************************************************************
	 * AssignmentSubmissionEdit implementation
	 **************************************************************************/

	/**
	 * <p>
	 * BaseAssignmentSubmissionEdit is an implementation of the CHEF AssignmentSubmissionEdit object.
	 * </p>
	 * 
	 * @author University of Michigan, CHEF Software Development Team
	 */
	public class BaseAssignmentSubmissionEdit extends BaseAssignmentSubmission implements AssignmentSubmissionEdit,
			SessionBindingListener
	{
		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/**
		 * Construct from another AssignmentSubmission object.
		 * 
		 * @param AssignmentSubmission
		 *        The AssignmentSubmission object to use for values.
		 */
		public BaseAssignmentSubmissionEdit(AssignmentSubmission assignmentSubmission)
		{
			super(assignmentSubmission);

		} // BaseAssignmentSubmissionEdit

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The AssignmentSubmission id.
		 */
		public BaseAssignmentSubmissionEdit(String id, String assignmentId, String submitterId, String submitTime, String submitted, String graded)
		{
			super(id, assignmentId, submitterId, submitTime, submitted, graded);

		} // BaseAssignmentSubmissionEdit

		/**
		 * Construct from information in XML.
		 * 
		 * @param el
		 *        The XML DOM Element definining the AssignmentSubmission.
		 */
		public BaseAssignmentSubmissionEdit(Element el)
		{
			super(el);

		} // BaseAssignmentSubmissionEdit

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}

		} // finalize

		/**
		 * Set the context at the time of creation.
		 * 
		 * @param context -
		 *        the context string.
		 */
		public void setContext(String context)
		{
			m_context = context;
		}

		/**
		 * Set the Assignment for this Submission
		 * 
		 * @param assignment -
		 *        the Assignment
		 */
		public void setAssignment(Assignment assignment)
		{
			if (assignment != null)
			{
				m_assignment = assignment.getId();
			}
			else
				m_assignment = "";
		}

		/**
		 * Set whether this is a final submission.
		 * 
		 * @param submitted -
		 *        True if a final submission, false if still a draft.
		 */
		public void setSubmitted(boolean submitted)
		{
			m_submitted = submitted;
		}

		/**
		 * Add a User to the submitters list.
		 * 
		 * @param submitter -
		 *        the User to add.
		 */
		public void addSubmitter(User submitter)
		{
			if (submitter != null) m_submitters.add(submitter.getId());
		}

                public void setSubmitterId(String id) {
                    m_submitterId = id;
                }
                public void addSubmissionLogEntry(String entry) {
                    if (m_submissionLog != null) m_submissionLog.add(entry);
                }

                public void addGradeForUser(String uid, String grade) {
                    if (m_grades != null)
                    {
                        Iterator<String> _it = m_grades.iterator();
                        while (_it.hasNext()) {
                            String _val = _it.next();
                            if (_val.startsWith(uid + "::")) {
                                m_grades.remove(_val);
                                break;
                            }
                        }
                        if (grade != null && !(grade.equals("null"))) {
                            m_grades.add(uid + "::" + grade);
                        }
                    }
                }
                
		/**
		 * Remove an User from the submitter list
		 * 
		 * @param submitter -
		 *        the User to remove.
		 */
		public void removeSubmitter(User submitter)
		{
			if (submitter != null) m_submitters.remove(submitter.getId());
		}

		/**
		 * Remove all user from the submitter list
		 */
		public void clearSubmitters()
		{
			m_submitters.clear();
		}

		/**
		 * Set the time at which this response was submitted; setting it to null signifies the response is unsubmitted.
		 * 
		 * @param timeSubmitted -
		 *        Time of submission.
		 */
		public void setTimeSubmitted(Time value)
		{
			m_timeSubmitted = value;
		}

		/**
		 * Set whether the grade has been released.
		 * 
		 * @param released -
		 *        True if the Submissions's grade has been released, false otherwise.
		 */
		public void setGradeReleased(boolean released)
		{
			m_gradeReleased = released;
		}

		/**
		 * Sets the grade for the Submisssion.
		 * 
		 * @param grade -
		 *        The Submission's grade.
		 */
		public void setGrade(String grade)
		{
			m_grade = grade;
		}

		/**
		 * Text submitted in response to the Assignment.
		 * 
		 * @param submissionText -
		 *        The text of the submission.
		 */
		public void setSubmittedText(String value)
		{
			m_submittedText = value;
		}

		/**
		 * Add an attachment to the list of submitted attachments.
		 * 
		 * @param attachment -
		 *        The Reference object pointing to the attachment.
		 */
		public void addSubmittedAttachment(Reference attachment)
		{
			if (attachment != null) m_submittedAttachments.add(attachment);
		}

		/**
		 * Remove an attachment from the list of submitted attachments
		 * 
		 * @param attachment -
		 *        The Reference object pointing to the attachment.
		 */
		public void removeSubmittedAttachment(Reference attachment)
		{
			if (attachment != null) m_submittedAttachments.remove(attachment);
		}

		/**
		 * Remove all submitted attachments.
		 */
		public void clearSubmittedAttachments()
		{
			m_submittedAttachments.clear();
		}

		/**
		 * Set the general comments by the grader.
		 * 
		 * @param comment -
		 *        the text of the grader's comments; may be null.
		 */
		public void setFeedbackComment(String value)
		{
			m_feedbackComment = value;
		}

		/**
		 * Set the text part of the instructors feedback; usually an annotated copy of the submittedText
		 * 
		 * @param feedback -
		 *        The text of the grader's feedback.
		 */
		public void setFeedbackText(String value)
		{
			m_feedbackText = value;
		}

		/**
		 * Add an attachment to the list of feedback attachments.
		 * 
		 * @param attachment -
		 *        The Resource object pointing to the attachment.
		 */
		public void addFeedbackAttachment(Reference attachment)
		{
			if (attachment != null) m_feedbackAttachments.add(attachment);
		}

		/**
		 * Remove an attachment from the list of feedback attachments.
		 * 
		 * @param attachment -
		 *        The Resource pointing to the attachment to remove.
		 */
		public void removeFeedbackAttachment(Reference attachment)
		{
			if (attachment != null) m_feedbackAttachments.remove(attachment);
		}

		/**
		 * Remove all feedback attachments.
		 */
		public void clearFeedbackAttachments()
		{
			m_feedbackAttachments.clear();
		}

		/**
		 * Set whether this Submission was rejected by the grader.
		 * 
		 * @param returned -
		 *        true if this response was rejected by the grader, false otherwise.
		 */
		public void setReturned(boolean value)
		{
			m_returned = value;
		}

		/**
		 * Set whether this Submission has been graded.
		 * 
		 * @param graded -
		 *        true if the submission has been graded, false otherwise.
		 */
		public void setGraded(boolean value)
		{
			m_graded = value;
		}
		
		/**
		 * set the grader id (used to distinguish between auto and instructor grading)
		 */
		public void setGradedBy(String gradedBy){
			m_gradedBy = gradedBy;
		}

		/**
		 * Set the time at which the graded Submission was returned; setting it to null means it is not yet graded.
		 * 
		 * @param timeReturned -
		 *        The time at which the graded Submission was returned.
		 */
		public void setTimeReturned(Time timeReturned)
		{
			m_timeReturned = timeReturned;
		}

		/**
		 * Set the checked status of the honor pledge flag.
		 * 
		 * @param honorPledgeFlag -
		 *        True if the honor pledge is checked, false otherwise.
		 */
		public void setHonorPledgeFlag(boolean honorPledgeFlag)
		{
			m_honorPledgeFlag = honorPledgeFlag;
		}

		/**
		 * Set the time last modified.
		 * 
		 * @param lastmod -
		 *        The Time at which the Assignment was last modified.
		 */
		public void setTimeLastModified(Time lastmod)
		{
			if (lastmod != null) m_timeLastModified = lastmod;
		}
		
		
		
		public void postAttachment(List attachments){
			//Send the attachment to the review service

			try {
				//SAK-26322
				List<ContentResource> resources = getAllAcceptableAttachments(attachments);
				Assignment ass = this.getAssignment();			
				if (ass != null)
				{
					//Group submissions store the group ID as the submitterId, so find an actual user ID
					String userId = null;
					if(getAssignment().isGroup()){
						//first first user id from an attachment
						for(ContentResource cr : resources){
							userId = this.getContentReviewSubmitterId(cr);
							if(userId != null){
								break;
							}
						}
					}else{						
						userId = this.getContentReviewSubmitterId(null);
					}
					contentReviewService.queueContent(userId, this.getContext(), ass.getReference(), resources);
				}
				else
				{
					// error, assignment couldn't be found. Logger the error
					M_log.debug(this + " BaseAssignmentSubmissionEdit postAttachment: Unable to find assignment associated with submission id= " + this.m_id + " and assignment id=" + this.m_assignment);
				}
			}
			catch (QueueException qe)
			{
				M_log.warn(" BaseAssignmentSubmissionEdit postAttachment: Unable to add content to Content Review queue: " + qe.getMessage());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		private ContentResource getFirstAcceptableAttachement(List attachments) {
			
			for( int i =0; i < attachments.size();i++ ) { 
				Reference attachment = (Reference)attachments.get(i);
				try {
					ContentResource res = m_contentHostingService.getResource(attachment.getId());
					if (contentReviewService.isAcceptableContent(res)) {
						return res;
					}
				} catch (PermissionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					M_log.warn(":geFirstAcceptableAttachment " + e.getMessage());
				} catch (IdUnusedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					M_log.warn(":geFirstAcceptableAttachment " + e.getMessage());
				} catch (TypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					M_log.warn(":geFirstAcceptableAttachment " + e.getMessage());
				}

				
			}
			return null;
		}

		private List<ContentResource> getAllAcceptableAttachments(List attachments)
		{
			List<ContentResource> resources = new ArrayList<ContentResource>();
			for (int i = 0; i < attachments.size(); i++)
			{
				Reference attachment = (Reference) attachments.get(i);
				try
				{
					ContentResource res = m_contentHostingService.getResource(attachment.getId());
					if (contentReviewService.isAcceptableContent(res))
					{
						resources.add(res);
					}
				}
				catch (PermissionException e)
				{
					e.printStackTrace();
					M_log.warn(":getAllAcceptableAttachments " + e.getMessage());
				}
				catch (IdUnusedException e)
				{
					e.printStackTrace();
					M_log.warn(":getAllAcceptableAttachments " + e.getMessage());
				}
				catch (TypeException e)
				{
					e.printStackTrace();
					M_log.warn(":getAllAcceptableAttachments " + e.getMessage());
				}
			}

			return resources;
		}
		
		/**
		 * Take all values from this object.
		 * 
		 * @param AssignmentSubmission
		 *        The AssignmentSubmission object to take values from.
		 */
		protected void set(AssignmentSubmission assignmentSubmission)
		{
			setAll(assignmentSubmission);

		} // set

		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * Access the resource's properties for modification
		 * 
		 * @return The resource's properties.
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;

		} // getPropertiesEdit

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;

		} // activate

		/**
		 * Check to see if the edit is still active, or has already been closed.
		 * 
		 * @return true if the edit is active, false if it's been closed.
		 */
		public boolean isActiveEdit()
		{
			return m_active;

		} // isActiveEdit

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;

		} // closeEdit

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			M_log.debug(this + " BaseAssignmentSubmissionEdit valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}

		} // valueUnbound

		public void setReviewScore(int score) {
			this.m_reviewScore = score;
			
		}

		public void setReviewIconUrl(String url) {
			this.m_reviewIconUrl = url;
			
		}

		public void setReviewStatus(String status) {
			this.m_reviewStatus = status;
		
			
		}

        public void setReviewError(String error) {
            this.m_reviewError = error;
        }

		// SAK-29314
		public void setIsUserSubmission(boolean isUserSubmission)
		{
			this.m_isUserSubmission = isUserSubmission;
		}

	} // BaseAssignmentSubmissionEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Assignment Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface AssignmentStorage
	{
		/**
		 * Open.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if an Assignment by this id exists.
		 * 
		 * @param id
		 *        The assignment id.
		 * @return true if an Assignment by this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the Assignment with this id, or null if not found.
		 * 
		 * @param id
		 *        The Assignment id.
		 * @return The Assignment with this id, or null if not found.
		 */
		public Assignment get(String id);

		/**
		 * Get all Assignments.
		 * 
		 * @return The list of all Assignments.
		 */
		public List getAll(String context);

		/**
		 * Add a new Assignment with this id.
		 * 
		 * @param id
		 *        The Assignment id.
		 * @param context
		 *        The context.
		 * @return The locked Assignment object with this id, or null if the id is in use.
		 */
		public AssignmentEdit put(String id, String context);

		/**
		 * Get a lock on the Assignment with this id, or null if a lock cannot be gotten.
		 * 
		 * @param id
		 *        The Assignment id.
		 * @return The locked Assignment with this id, or null if this records cannot be locked.
		 */
		public AssignmentEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 * 
		 * @param Assignment
		 *        The Assignment to commit.
		 */
		public void commit(AssignmentEdit assignment);

		/**
		 * Cancel the changes and release the lock.
		 * 
		 * @param Assignment
		 *        The Assignment to commit.
		 */
		public void cancel(AssignmentEdit assignment);

		/**
		 * Remove this Assignment.
		 * 
		 * @param Assignment
		 *        The Assignment to remove.
		 */
		public void remove(AssignmentEdit assignment);

	} // AssignmentStorage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentContent Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface AssignmentContentStorage
	{
		/**
		 * Open.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if a AssignmentContent by this id exists.
		 * 
		 * @param id
		 *        The AssignmentContent id.
		 * @return true if a AssignmentContent by this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the AssignmentContent with this id, or null if not found.
		 * 
		 * @param id
		 *        The AssignmentContent id.
		 * @return The AssignmentContent with this id, or null if not found.
		 */
		public AssignmentContent get(String id);

		/**
		 * Get all AssignmentContents.
		 * 
		 * @return The list of all AssignmentContents.
		 */
		public List getAll(String context);

		/**
		 * Add a new AssignmentContent with this id.
		 * 
		 * @param id
		 *        The AssignmentContent id.
		 * @param context
		 *        The context.
		 * @return The locked AssignmentContent object with this id, or null if the id is in use.
		 */
		public AssignmentContentEdit put(String id, String context);

		/**
		 * Get a lock on the AssignmentContent with this id, or null if a lock cannot be gotten.
		 * 
		 * @param id
		 *        The AssignmentContent id.
		 * @return The locked AssignmentContent with this id, or null if this records cannot be locked.
		 */
		public AssignmentContentEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 * 
		 * @param AssignmentContent
		 *        The AssignmentContent to commit.
		 */
		public void commit(AssignmentContentEdit content);

		/**
		 * Cancel the changes and release the lock.
		 * 
		 * @param AssignmentContent
		 *        The AssignmentContent to commit.
		 */
		public void cancel(AssignmentContentEdit content);

		/**
		 * Remove this AssignmentContent.
		 * 
		 * @param AssignmentContent
		 *        The AssignmentContent to remove.
		 */
		public void remove(AssignmentContentEdit content);

	} // AssignmentContentStorage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentSubmission Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface AssignmentSubmissionStorage
	{
		/**
		 * Open.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if a AssignmentSubmission by this id exists.
		 * 
		 * @param id
		 *        The AssignmentSubmission id.
		 * @return true if a AssignmentSubmission by this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the AssignmentSubmission with this id, or null if not found.
		 * 
		 * @param id
		 *        The AssignmentSubmission id.
		 * @return The AssignmentSubmission with this id, or null if not found.
		 */
		public AssignmentSubmission get(String id);
		
		/**
		 * Get the AssignmentSubmission with this assignment id and user id.
		 * 
		 * @param assignmentId
		 *        The Assignment id.
		 * @param userId
		 * 		  The user id
		 * @return The AssignmentSubmission with this id, or null if not found.
		 */
		public AssignmentSubmission get(String assignmentId, String userId);

		/**
		 * Gets a map of users to their corresponding submission on this non-group assignment for the specified users.
		 * NB: This method does not support gorup assignments - it's intended for perfromance in retrieving submissions for non-group assignments (e. where 1 submission has 1 submitterId)
		 */
		public Map<User, AssignmentSubmission> getUserSubmissionMap(Assignment assignment, List<User> users);

		/**
		 * Get the number of submissions which has been submitted.
		 * 
		 * @param assignmentId -
		 *        the id of Assignment who's submissions you would like.
		 * @return List over all the submissions for an Assignment.
		 */
		public int getSubmittedSubmissionsCount(String assignmentId);
		
		/**
		 * Get the number of submissions which has not been submitted and graded.
		 * 
		 * @param assignment -
		 *        the Assignment who's submissions you would like.
		 * @return List over all the submissions for an Assignment.
		 */
		public int getUngradedSubmissionsCount(String assignmentId);

		/**
		 * Get all AssignmentSubmissions.
		 * 
		 * @return The list of all AssignmentSubmissions.
		 */
		public List getAll(String context);

		/**
		 * Add a new AssignmentSubmission with this id.
		 * 
		 * @param id
		 *        The AssignmentSubmission id.
		 * @param context
		 *        The context.
		 * @return The locked AssignmentSubmission object with this id, or null if the id is in use.
		 */
		public AssignmentSubmissionEdit put(String id, String assignmentId, String submitterId, String submitTime, String submitted, String graded);

		/**
		 * Get a lock on the AssignmentSubmission with this id, or null if a lock cannot be gotten.
		 * 
		 * @param id
		 *        The AssignmentSubmission id.
		 * @return The locked AssignmentSubmission with this id, or null if this records cannot be locked.
		 */
		public AssignmentSubmissionEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 * 
		 * @param AssignmentSubmission
		 *        The AssignmentSubmission to commit.
		 */
		public void commit(AssignmentSubmissionEdit submission);

		/**
		 * Cancel the changes and release the lock.
		 * 
		 * @param AssignmentSubmission
		 *        The AssignmentSubmission to commit.
		 */
		public void cancel(AssignmentSubmissionEdit submission);

		/**
		 * Remove this AssignmentSubmission.
		 * 
		 * @param AssignmentSubmission
		 *        The AssignmentSubmission to remove.
		 */
		public void remove(AssignmentSubmissionEdit submission);

	} // AssignmentSubmissionStorage

	/**
	 * Utility function which returns the string representation of the long value of the time object.
	 * 
	 * @param t -
	 *        the Time object.
	 * @return A String representation of the long value of the time object.
	 */
	protected String getTimeString(Time t)
	{
		String retVal = "";
		if (t != null) retVal = t.toString();
		return retVal;
	}

	/**
	 * Utility function which returns a string from a boolean value.
	 * 
	 * @param b -
	 *        the boolean value.
	 * @return - "True" if the input value is true, "false" otherwise.
	 */
	protected String getBoolString(boolean b)
	{
		if (b)
			return "true";
		else
			return "false";
	}

	/**
	 * Utility function which returns a boolean value from a string.
	 * 
	 * @param s -
	 *        The input string.
	 * @return the boolean true if the input string is "true", false otherwise.
	 */
	protected boolean getBool(String s)
	{
		boolean retVal = false;
		if (s != null)
		{
			if (s.equalsIgnoreCase("true")) retVal = true;
		}
		return retVal;
	}

	/**
	 * Utility function which converts a string into a chef time object.
	 * 
	 * @param timeString -
	 *        String version of a time in long format, representing the standard ms since the epoch, Jan 1, 1970 00:00:00.
	 * @return A chef Time object.
	 */
	protected Time getTimeObject(String timeString)
	{
		Time aTime = null;
		timeString = StringUtils.trimToNull(timeString);
		if (timeString != null)
		{
			try
			{
				aTime = TimeService.newTimeGmt(timeString);
			}
			catch (Exception e)
			{
				M_log.warn(":geTimeObject " + e.getMessage());
				try
				{
					long longTime = Long.parseLong(timeString);
					aTime = TimeService.newTime(longTime);
				}
				catch (Exception ee)
				{
					M_log.warn(" getTimeObject Base Exception creating time object from xml file : " + ee.getMessage() + " timeString=" + timeString);
				}
			}
		}
		return aTime;
	}

	protected String getGroupNameFromContext(String context)
	{
		String retVal = "";

		if (context != null)
		{
			int index = context.indexOf("group-");
			if (index != -1)
			{
				String[] parts = StringUtil.splitFirst(context, "-");
				if (parts.length > 1)
				{
					retVal = parts[1];
				}
			}
			else
			{
				retVal = context;
			}
		}

		return retVal;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementations (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentStorageUser implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class AssignmentStorageUser implements SingleStorageUser, SAXEntityReader
	{
		private Map<String,Object> m_services;

		/**
		 * Construct a new resource given just an id.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param id
		 *        The id for the new object.
		 * @param others
		 *        (options) array of objects to load into the Resource's fields.
		 * @return The new resource.
		 */
		public Entity newResource(Entity container, String id, Object[] others)
		{
			return new BaseAssignment(id, (String) others[0]);
		}

		/**
		 * Construct a new resource, from an XML element.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param element
		 *        The XML.
		 * @return The new resource from the XML.
		 */
		public Entity newResource(Entity container, Element element)
		{
			return new BaseAssignment(element);
		}

		/**
		 * Construct a new resource from another resource of the same type.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param other
		 *        The other resource.
		 * @return The new resource as a copy of the other.
		 */
		public Entity newResource(Entity container, Entity other)
		{
			return new BaseAssignment((Assignment) other);
		}

		/**
		 * Construct a new resource given just an id.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param id
		 *        The id for the new object.
		 * @param others
		 *        (options) array of objects to load into the Resource's fields.
		 * @return The new resource.
		 */
		public Edit newResourceEdit(Entity container, String id, Object[] others)
		{
			BaseAssignmentEdit e = new BaseAssignmentEdit(id, (String) others[0]);
			e.activate();
			return e;
		}

		/**
		 * Construct a new resource, from an XML element.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param element
		 *        The XML.
		 * @return The new resource from the XML.
		 */
		public Edit newResourceEdit(Entity container, Element element)
		{
			BaseAssignmentEdit e = new BaseAssignmentEdit(element);
			e.activate();
			return e;
		}

		/**
		 * Construct a new resource from another resource of the same type.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param other
		 *        The other resource.
		 * @return The new resource as a copy of the other.
		 */
		public Edit newResourceEdit(Entity container, Entity other)
		{
			BaseAssignmentEdit e = new BaseAssignmentEdit((Assignment) other);
			e.activate();
			return e;
		}

		/**
		 * Collect the fields that need to be stored outside the XML (for the resource).
		 * 
		 * @return An array of field values to store in the record outside the XML (for the resource).
		 */
		public Object[] storageFields(Entity r)
		{
			Object rv[] = new Object[1];
			rv[0] = ((Assignment) r).getContext();
			return rv;
		}

		/***********************************************************************
		 * SAXEntityReader
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getDefaultHandler(java.util.Map)
		 */
		public DefaultEntityHandler getDefaultHandler(final Map<String, Object> services)
		{
			return new DefaultEntityHandler()
			{

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
				{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if (entity == null)
						{
							if ("assignment".equals(qName))
							{
								BaseAssignment ba = new BaseAssignment();
								entity = ba;
								setContentHandler(ba.getContentHandler(services), uri,
										localName, qName, attributes);
							}
							else
							{
								M_log.warn(" AssignmentStorageUser getDefaultHandler startElement Unexpected Element in XML [" + qName + "]");
							}

						}
					}
				}

			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getServices()
		 */
		public Map<String, Object> getServices()
		{
			if (m_services == null)
			{
				m_services = new HashMap<String, Object>();
			}
			return m_services;
		}

	}// AssignmentStorageUser

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentContentStorageUser implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class AssignmentContentStorageUser implements SingleStorageUser, SAXEntityReader
	{
		private Map<String,Object> m_services;

		/**
		 * Construct a new resource given just an id.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param id
		 *        The id for the new object.
		 * @param others
		 *        (options) array of objects to load into the Resource's fields.
		 * @return The new resource.
		 */
		public Entity newResource(Entity container, String id, Object[] others)
		{
			return new BaseAssignmentContent(id, (String) others[0]);
		}

		/**
		 * Construct a new resource, from an XML element.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param element
		 *        The XML.
		 * @return The new resource from the XML.
		 */
		public Entity newResource(Entity container, Element element)
		{
			return new BaseAssignmentContent(element);
		}

		/**
		 * Construct a new resource from another resource of the same type.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param other
		 *        The other resource.
		 * @return The new resource as a copy of the other.
		 */
		public Entity newResource(Entity container, Entity other)
		{
			return new BaseAssignmentContent((AssignmentContent) other);
		}

		/**
		 * Construct a new rsource given just an id.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param id
		 *        The id for the new object.
		 * @param others
		 *        (options) array of objects to load into the Resource's fields.
		 * @return The new resource.
		 */
		public Edit newResourceEdit(Entity container, String id, Object[] others)
		{
			BaseAssignmentContentEdit e = new BaseAssignmentContentEdit(id, (String) others[0]);
			e.activate();
			return e;
		}

		/**
		 * Construct a new resource, from an XML element.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param element
		 *        The XML.
		 * @return The new resource from the XML.
		 */
		public Edit newResourceEdit(Entity container, Element element)
		{
			BaseAssignmentContentEdit e = new BaseAssignmentContentEdit(element);
			e.activate();
			return e;
		}

		/**
		 * Construct a new resource from another resource of the same type.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param other
		 *        The other resource.
		 * @return The new resource as a copy of the other.
		 */
		public Edit newResourceEdit(Entity container, Entity other)
		{
			BaseAssignmentContentEdit e = new BaseAssignmentContentEdit((AssignmentContent) other);
			e.activate();
			return e;
		}

		/**
		 * Collect the fields that need to be stored outside the XML (for the resource).
		 * 
		 * @return An array of field values to store in the record outside the XML (for the resource).
		 */
		public Object[] storageFields(Entity r)
		{
			Object rv[] = new Object[1];
			rv[0] = ((AssignmentContent) r).getCreator();
			return rv;
		}

		/***********************************************************************
		 * SAXEntityReader
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getDefaultHandler(java.util.Map)
		 */
		public DefaultEntityHandler getDefaultHandler(final Map<String, Object> services)
		{
			return new DefaultEntityHandler()
			{

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
				{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if (entity == null)
						{
							if ("content".equals(qName))
							{
								BaseAssignmentContent bac = new BaseAssignmentContent();
								entity = bac;
								setContentHandler(bac.getContentHandler(services), uri,
										localName, qName, attributes);
							}
							else
							{
								M_log.warn(" AssignmentContentStorageUser getDefaultEntityHandler startElement Unexpected Element in XML [" + qName + "]");
							}

						}
					}
				}

			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getServices()
		 */
		public Map<String, Object> getServices()
		{
			if (m_services == null)
			{
				m_services = new HashMap<String, Object>();
			}
			return m_services;
		}

	}// ContentStorageUser

	/**********************************************************************************************************************************************************************************************************************************************************
	 * SubmissionStorageUser implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class AssignmentSubmissionStorageUser implements SingleStorageUser, SAXEntityReader
	{
		private Map<String,Object> m_services;

		/**
		 * Construct a new resource given just an id.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param id
		 *        The id for the new object.
		 * @param others
		 *        (options) array of objects to load into the Resource's fields.
		 * @return The new resource.
		 */
		public Entity newResource(Entity container, String id, Object[] others)
		{
			return new BaseAssignmentSubmission(id, (String) others[0], (String) others[1], (String) others[2], (String) others[3], (String) others[4]);
		}

		/**
		 * Construct a new resource, from an XML element.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param element
		 *        The XML.
		 * @return The new resource from the XML.
		 */
		public Entity newResource(Entity container, Element element)
		{
			return new BaseAssignmentSubmission(element);
		}

		/**
		 * Construct a new resource from another resource of the same type.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param other
		 *        The other resource.
		 * @return The new resource as a copy of the other.
		 */
		public Entity newResource(Entity container, Entity other)
		{
			return new BaseAssignmentSubmission((AssignmentSubmission) other);
		}

		/**
		 * Construct a new rsource given just an id.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param id
		 *        The id for the new object.
		 * @param others
		 *        (options) array of objects to load into the Resource's fields.
		 * @return The new resource.
		 */
		public Edit newResourceEdit(Entity container, String id, Object[] others)
		{
			BaseAssignmentSubmissionEdit e = new BaseAssignmentSubmissionEdit(id, (String) others[0], (String) others[1], (String) others[2], (String) others[3], (String) others[4]);
			e.activate();
			return e;
		}

		/**
		 * Construct a new resource, from an XML element.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param element
		 *        The XML.
		 * @return The new resource from the XML.
		 */
		public Edit newResourceEdit(Entity container, Element element)
		{
			BaseAssignmentSubmissionEdit e = new BaseAssignmentSubmissionEdit(element);
			e.activate();
			return e;
		}

		/**
		 * Construct a new resource from another resource of the same type.
		 * 
		 * @param container
		 *        The Resource that is the container for the new resource (may be null).
		 * @param other
		 *        The other resource.
		 * @return The new resource as a copy of the other.
		 */
		public Edit newResourceEdit(Entity container, Entity other)
		{
			BaseAssignmentSubmissionEdit e = new BaseAssignmentSubmissionEdit((AssignmentSubmission) other);
			e.activate();
			return e;
		}

		/**
		 * Collect the fields that need to be stored outside the XML (for the resource).
		 * 
		 * @return An array of field values to store in the record outside the XML (for the resource).
		 */
		public Object[] storageFields(Entity r)
		{
			/*"context", "SUBMITTER_ID", "SUBMIT_TIME", "SUBMITTED", "GRADED"*/
			Object rv[] = new Object[5];
			rv[0] = ((AssignmentSubmission) r).getAssignmentId();
			
                        rv[1] = ((AssignmentSubmission) r).getSubmitterId();
			
			Time submitTime = ((AssignmentSubmission) r).getTimeSubmitted();
			rv[2] = (submitTime != null)?String.valueOf(submitTime.getTime()):null;
			
			rv[3] = Boolean.valueOf(((AssignmentSubmission) r).getSubmitted()).toString();
			
			rv[4] = Boolean.valueOf(((AssignmentSubmission) r).getGraded()).toString();
			
			return rv;
		}

		/***********************************************************************
		 * SAXEntityReader
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getDefaultHandler(java.util.Map)
		 */
		public DefaultEntityHandler getDefaultHandler(final Map<String, Object> services)
		{
			return new DefaultEntityHandler()
			{

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
				{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if (entity == null)
						{
							if ("submission".equals(qName))
							{
								BaseAssignmentSubmission bas = new BaseAssignmentSubmission();
								entity = bas;
								setContentHandler(bas.getContentHandler(services), uri,
										localName, qName, attributes);
							}
							else
							{
								M_log.warn(" AssignmentSubmissionStorageUser getDefaultHandler startElement: Unexpected Element in XML [" + qName + "]");
							}

						}
					}
				}

			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getServices()
		 */
		public Map<String, Object> getServices()
		{
			if (m_services == null)
			{
				m_services = new HashMap<String, Object>();
			}
			return m_services;
		}

	}// SubmissionStorageUser


	private class UserComparator implements Comparator
        {
            public UserComparator() {}
            
            public int compare(Object o1, Object o2) {
                User _u1 = (User)o1;
                User _u2 = (User)o2;
                return _u1.compareTo(_u2);
            }
        }

	/**
	 * the AssignmentComparator clas
	 */
	static class AssignmentComparator implements Comparator
	{	
		Collator collator = null;
		
		/**
		 * the criteria
		 */
		String m_criteria = null;

		/**
		 * the criteria
		 */
		String m_asc = null;

		/**
		 * is group submission
		 */
		boolean m_group_submission = false;

		/**
		 * constructor
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. TRUE_STRING if ascending; "false" otherwise.
		 */
		public AssignmentComparator(String criteria, String asc)
		{
			this(criteria, asc, false);
		} // constructor
		public AssignmentComparator(String criteria, String asc, boolean group)
		{
			m_criteria = criteria;
			m_asc = asc;
			m_group_submission = group;
			try
			{
				collator= new RuleBasedCollator(((RuleBasedCollator)Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
			}
			catch (ParseException e)
			{
				// error with init RuleBasedCollator with rules
				// use the default Collator
				collator = Collator.getInstance();
				M_log.warn(this + " AssignmentComparator cannot init RuleBasedCollator. Will use the default Collator instead. " + e);
			}
		}

		/**
		 * implementing the compare function
		 * 
		 * @param o1
		 *        The first object
		 * @param o2
		 *        The second object
		 * @return The compare result. 1 is o1 < o2; -1 otherwise
		 */
		public int compare(Object o1, Object o2)
		{
			int result = -1;

			/************** for sorting submissions ********************/
			if ("submitterName".equals(m_criteria))
			{
				String name1 = getSubmitterSortname(o1);
				String name2 = getSubmitterSortname(o2);
				result = compareString(name1,name2);
			}
			/** *********** for sorting assignments ****************** */
			else if ("duedate".equals(m_criteria))
			{
				// sorted by the assignment due date
				Time t1 = ((Assignment) o1).getDueTime();
				Time t2 = ((Assignment) o2).getDueTime();

				if (t1 == null)
				{
					result = -1;
				}
				else if (t2 == null)
				{
					result = 1;
				}
				else if (t1.before(t2))
				{
					result = -1;
				}
				else
				{
					result = 1;
				}
			}
			else if ("sortname".equals(m_criteria))
			{
				// sorted by the user's display name
				String s1 = null;
				String userId1 = (String) o1;
				if (userId1 != null)
				{
					try
					{
						User u1 = UserDirectoryService.getUser(userId1);
						s1 = u1!=null?u1.getSortName():null;
					}
					catch (Exception e)
					{
						M_log.warn(" AssignmentComparator.compare " + e.getMessage() + " id=" + userId1);
					}
				}
					
				String s2 = null;
				String userId2 = (String) o2;
				if (userId2 != null)
				{
					try
					{
						User u2 = UserDirectoryService.getUser(userId2);
						s2 = u2!=null?u2.getSortName():null;
					}
					catch (Exception e)
					{
						M_log.warn(" AssignmentComparator.compare " + e.getMessage() + " id=" + userId2);
					}
				}

				result = compareString(s1,s2);
			}
			
			// sort ascending or descending
			if (m_asc.equals(Boolean.FALSE.toString()))
			{
				result = -result;
			}
			return result;
		}

		/**
		 * get the submitter sortname String for the AssignmentSubmission object
		 * @param o2
		 * @return
		 */
		private String getSubmitterSortname(Object o2) {
			String rv = "";
			if (o2 instanceof AssignmentSubmission)
			{
				// get Assignment
				AssignmentSubmission _submission =(AssignmentSubmission) o2;
				if (_submission.getAssignment().isGroup()) {
					// get the Group
					try {
						Site _site = SiteService.getSite( _submission.getAssignment().getContext() );
						rv = _site.getGroup(_submission.getSubmitterId()).getTitle();
					} catch (Throwable _dfd) { }			
				} else {	
				User[] users2 = ((AssignmentSubmission) o2).getSubmitters();
				if (users2 != null)
				{
					StringBuffer users2Buffer = new StringBuffer();
					for (int i = 0; i < users2.length; i++)
					{
						users2Buffer.append(users2[i].getSortName() + " ");
					}
					rv = users2Buffer.toString();
				}
			}
			}
			return rv;
		}
		
		private int compareString(String s1, String s2) 
		{
			int result;
			if (s1 == null && s2 == null) {
				result = 0;
			} else if (s2 == null) {
				result = 1;
			} else if (s1 == null) {
				result = -1;
			} else {
				result = collator.compare(s1.toLowerCase(), s2.toLowerCase());
			}
			return result;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void updateEntityReferences(String toContext, Map<String, String> transversalMap){
		if(transversalMap != null && transversalMap.size() > 0){
			Set<Entry<String, String>> entrySet = (Set<Entry<String, String>>) transversalMap.entrySet();
			
			String toSiteId = toContext;
			Iterator assignmentsIter = getAssignmentsForContext(toSiteId);
			while (assignmentsIter.hasNext())
			{
				Assignment assignment = (Assignment) assignmentsIter.next();
				String assignmentId = assignment.getId();
				try 
				{
					String msgBody = assignment.getContent().getInstructions();
					StringBuffer msgBodyPreMigrate = new StringBuffer(msgBody);
					msgBody = LinkMigrationHelper.migrateAllLinks(entrySet, msgBody);
					SecurityAdvisor securityAdvisor = new MySecurityAdvisor(SessionManager.getCurrentSessionUserId(), 
							new ArrayList<String>(Arrays.asList(SECURE_UPDATE_ASSIGNMENT_CONTENT)),
							assignment.getContentReference());
						try
						{
						if(!msgBody.equals(msgBodyPreMigrate.toString())){
							// add permission to update assignment content
							securityService.pushAdvisor(securityAdvisor);
							
							AssignmentContentEdit cEdit = editAssignmentContent(assignment.getContentReference());
							cEdit.setInstructions(msgBody);
							commitEdit(cEdit);
						}
					}
						catch (Exception e)
						{
							// exception
							M_log.warn("UpdateEntityReference: cannot get assignment content for " + assignment.getId() + e.getMessage());
						}
						finally
						{
							// remove advisor
							securityService.popAdvisor(securityAdvisor);
						}
					}					
				catch(Exception ee)
				{
					M_log.warn("UpdateEntityReference: remove Assignment and all references for " + assignment.getId() + ee.getMessage());
				}
			}
		}
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup){
		transferCopyEntitiesRefMigrator(fromContext, toContext, ids, cleanup);
	}

	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		Map<String, String> transversalMap = new HashMap<String, String>();
		
		try
		{
			if(cleanup == true)
			{
				String toSiteId = toContext;
				Iterator assignmentsIter = getAssignmentsForContext(toSiteId);
				while (assignmentsIter.hasNext())
				{
					Assignment assignment = (Assignment) assignmentsIter.next();
					String assignmentId = assignment.getId();
					
					SecurityAdvisor securityAdvisor = new MySecurityAdvisor(SessionManager.getCurrentSessionUserId(), 
							new ArrayList<String>(Arrays.asList(SECURE_UPDATE_ASSIGNMENT, SECURE_REMOVE_ASSIGNMENT)),
							assignmentId);
					try 
					{
						// advisor to allow edit and remove assignment
						securityService.pushAdvisor(securityAdvisor);
						
						AssignmentEdit aEdit = editAssignment(assignmentId);
						
						// remove this assignment with all its associated items
						removeAssignmentAndAllReferences(aEdit);
					}
					catch(Exception ee)
					{
						M_log.warn(":transferCopyEntities: remove Assignment and all references for " + assignment.getId() + ee.getMessage());
					}
					finally
					{
						// remove SecurityAdvisor
						securityService.popAdvisor(securityAdvisor);
					}
				}
			}
			transversalMap.putAll(transferCopyEntitiesRefMigrator(fromContext, toContext, ids));
		}
		catch (Exception e)
		{
			M_log.info(this + "transferCopyEntities: End removing Assignmentt data" + e.getMessage());
		}
		
		return transversalMap;
	}

	/**
	 * This is to mimic the FormattedText.decodeFormattedTextAttribute but use SAX serialization instead
	 * @return
	 */
	protected String formattedTextDecodeFormattedTextAttribute(Attributes attributes, String baseAttributeName)
	{
		String ret;

		// first check if an HTML-encoded attribute exists, for example "foo-html", and use it if available
		ret = StringUtils.trimToNull(xmlDecodeAttribute(attributes, baseAttributeName + "-html"));
		if (ret != null) return ret;

		// next try the older kind of formatted text like "foo-formatted", and convert it if found
		ret = StringUtils.trimToNull(xmlDecodeAttribute(attributes, baseAttributeName + "-formatted"));
		ret = FormattedText.convertOldFormattedText(ret);
		if (ret != null) return ret;

		// next try just a plaintext attribute and convert the plaintext to formatted text if found
		// convert from old plaintext instructions to new formatted text instruction
		ret = xmlDecodeAttribute(attributes, baseAttributeName);
		ret = FormattedText.convertPlaintextToFormattedText(ret);
		return ret;
	}
	
	/**
	 * this is to mimic the Xml.decodeAttribute
	 * @param el
	 * @param tag
	 * @return
	 */
	protected String xmlDecodeAttribute(Attributes attributes, String tag)
	{
		String charset = StringUtils.trimToNull(attributes.getValue("charset"));
		if (charset == null) charset = "UTF-8";

		String body = StringUtils.trimToNull(attributes.getValue(tag));
		if (body != null)
		{
			try {
	            byte[] decoded = Base64.decodeBase64(body); // UTF-8 by default
	            body = org.apache.commons.codec.binary.StringUtils.newString(decoded, charset);
			} catch (IllegalStateException e) {
				M_log.warn(" XmlDecodeAttribute: " + e.getMessage() + " tag=" + tag);
			}
		}

		if (body == null) body = "";

		return body;
	}
	
    /**
     * construct the right path for context string, used for permission checkings
     * @param context
     * @return
     */
    public static String getContextReference(String context) 
    {   
            String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context + Entity.SEPARATOR;
            return resourceString;
    }

        /**
         * the GroupSubmission clas
         */
        public class GroupSubmission
        {

                /**
                *  the Group object
                */
                Group m_group = null;

                /**
                 * the AssignmentSubmission object
                 */
                AssignmentSubmission m_submission = null;


                public GroupSubmission(Group g, AssignmentSubmission s)
                {
                        m_group = g;
                        m_submission = s;
                }

                /**
                 * Returns the AssignmentSubmission object
                 */
                public AssignmentSubmission getSubmission()
                {
                        return m_submission;
                }

                public Group getGroup()
                {
                        return m_group;
                }
        }

    private LRS_Statement getStatementForAssignmentGraded(LRS_Actor instructor, Event event, Assignment a, AssignmentSubmission s,
            User studentUser) {
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(m_serverConfigurationService.getPortalUrl() + event.getResource(),
                "received-grade-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User received a grade");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User received a grade for their assginment: " + a.getTitle() + "; Submission #: " + s.getResubmissionNum());
        lrsObject.setDescription(descMap);
        LRS_Actor student = new LRS_Actor(studentUser.getEmail());
        student.setName(studentUser.getDisplayName());
        LRS_Context context = new LRS_Context(instructor);
        context.setActivity("other", "assignment");
        LRS_Statement statement = new LRS_Statement(student, verb, lrsObject, getLRS_Result(a, s, true), context);
        return statement;
    }

    private LRS_Result getLRS_Result(Assignment a, AssignmentSubmission s, boolean completed) {
        LRS_Result result = null;
        AssignmentContent content = a.getContent();
		String decSeparator = FormattedText.getDecimalSeparator();
		// gradeDisplay ready to conversion to Float
		String gradeDisplay = StringUtils.replace(s.getGradeDisplay(), decSeparator, ".");
        if (3 == content.getTypeOfGrade() && NumberUtils.isNumber(gradeDisplay)) { // Points
    		String maxGradePointDisplay = StringUtils.replace(content.getMaxGradePointDisplay(), decSeparator, ".");
            result = new LRS_Result(new Float(gradeDisplay), new Float(0.0), new Float(maxGradePointDisplay), null);
            result.setCompletion(completed);
        } else {
            result = new LRS_Result(completed);
            result.setGrade(s.getGradeDisplay());
        }
        return result;
    }

    private LRS_Statement getStatementForUnsubmittedAssignmentGraded(LRS_Actor instructor, Event event, Assignment a,
            AssignmentSubmission s, User studentUser) {
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(m_serverConfigurationService.getAccessUrl() + event.getResource(), "received-grade-unsubmitted-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User received a grade");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User received a grade for an unsubmitted assginment: " + a.getTitle());
        lrsObject.setDescription(descMap);
        LRS_Actor student = new LRS_Actor(studentUser.getEmail());
        student.setName(studentUser.getDisplayName());
        LRS_Context context = new LRS_Context(instructor);
        context.setActivity("other", "assignment");
        LRS_Statement statement = new LRS_Statement(student, verb, lrsObject, getLRS_Result(a, s, false), context);
        return statement;
    }
    
    public boolean hasBeenSubmitted(AssignmentSubmission submission)
	{
		try
		{
			List submissionLog=submission.getSubmissionLog();
			
			//Special case for old submissions prior to Sakai 10 where the submission log did not exist. Just return true for backward compatibility.
			if (submissionLog == null || submissionLog.size() == 0) {
				return true;
			}
			for (int x = 0; x < submissionLog.size(); x++)
			{
			    String itemString = (String) submissionLog.get(x);
			    if(itemString.contains("submitted"))
			    {
			    	return true;
			    }
			}
		}
		catch (Exception e)
		{
			M_log.warn(" hasBeenSubmitted(submission) " + e.getMessage());
			return false;
		}
		return false;
	}

	public String getDeepLink(String context, String assignmentId) throws Exception {

		Assignment a = getAssignment(assignmentId);

		boolean allowReadAssignment = allowGetAssignment(context);
		boolean allowAddAssignment = allowAddAssignment(context);
		boolean allowSubmitAssignment = allowAddSubmission(context);

		return getDeepLinkWithPermissions(context, assignmentId
											, allowReadAssignment, allowAddAssignment, allowSubmitAssignment);
	}

	public String getDeepLinkWithPermissions(String context, String assignmentId, boolean allowReadAssignment
					, boolean allowAddAssignment, boolean allowSubmitAssignment) throws Exception {

		Assignment a = getAssignment(assignmentId);

		String assignmentContext = a.getContext(); // assignment context
		if (allowReadAssignment
				&& a.getOpenTime().before(TimeService.newTime())) {
			// this checks if we want to display an assignment link
			try {
				Site site = SiteService.getSite(assignmentContext);
				// site id
				ToolConfiguration fromTool = site
						.getToolForCommonId("sakai.assignment.grades");
				// Three different urls to be rendered depending on the
				// user's permission
				if (allowAddAssignment) {
					return m_serverConfigurationService.getPortalUrl()
												+ "/directtool/"
												+ fromTool.getId()
												+ "?assignmentId=" + assignmentId + "&assignmentReference="
												+ a.getReference()
												+ "&panel=Main&sakai_action=doView_assignment";
				} else if (allowSubmitAssignment) {
					return m_serverConfigurationService.getPortalUrl()
											+ "/directtool/"
											+ fromTool.getId()
											+ "?assignmentId=" + assignmentId + "&assignmentReference="
											+ a.getReference()
											+ "&panel=Main&sakai_action=doView_submission";
				} else {
					// user can read the assignment, but not submit, so
					// render the appropriate url
					return m_serverConfigurationService.getPortalUrl()
											+ "/directtool/"
											+ fromTool.getId()
											+ "?assignmentId=" + assignmentId + "&assignmentReference="
											+ a.getReference()
											+ "&panel=Main&sakai_action=doView_assignment_as_student";
				}
			} catch (IdUnusedException e) {
				// No site found
				throw new IdUnusedException(
						"No site found while creating assignment url");
			}
		}
		return "";
	}
} // BaseAssignmentService

