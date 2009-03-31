/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.assignment.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.model.ContentReviewItem;
import org.sakaiproject.contentreview.service.ContentReviewService;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentContentEdit;
import org.sakaiproject.assignment.api.AssignmentContentNotEmptyException;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentSubmissionEdit;
import org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.message.api.MessageService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.email.cover.EmailService;
import org.sakaiproject.email.cover.DigestService;
import org.sakaiproject.entity.api.AttachmentContainer;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.Blob;
import org.sakaiproject.util.DefaultEntityHandler;
import org.sakaiproject.util.SAXEntityReader;
import org.sakaiproject.util.EmptyIterator;
import org.sakaiproject.util.EntityCollections;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.StorageUser;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.commonscodec.CommonsCodecBase64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * <p>
 * BaseAssignmentService is the abstract service class for Assignments.
 * </p>
 * <p>
 * The Concrete Service classes extending this are the XmlFile and DbCached storage classes.
 * </p>
 */
public abstract class BaseAssignmentService implements AssignmentService, EntityTransferrer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseAssignmentService.class);

	/** the resource bundle */
	private static ResourceLoader rb = new ResourceLoader("assignment");

	/** A Storage object for persistent storage of Assignments. */
	protected AssignmentStorage m_assignmentStorage = null;

	/** A Storage object for persistent storage of Assignments. */
	protected AssignmentContentStorage m_contentStorage = null;

	/** A Storage object for persistent storage of Assignments. */
	protected AssignmentSubmissionStorage m_submissionStorage = null;

	/** A Cache for this service - Assignments keyed by reference. */
	protected Cache m_assignmentCache = null;

	/** A Cache for this service - AssignmentContents keyed by reference. */
	protected Cache m_contentCache = null;

	/** A Cache for this service - AssignmentSubmissions keyed by reference. */
	protected Cache m_submissionCache = null;

	/** The access point URL. */
	protected String m_relativeAccessPoint = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EVENT STRINGS
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Event for adding an assignment. */
	public static final String EVENT_ADD_ASSIGNMENT = "asn.new.assignment";

	/** Event for adding an assignment. */
	public static final String EVENT_ADD_ASSIGNMENT_CONTENT = "asn.new.assignmentcontent";

	/** Event for adding an assignment submission. */
	public static final String EVENT_ADD_ASSIGNMENT_SUBMISSION = "asn.new.submission";

	/** Event for removing an assignment. */
	public static final String EVENT_REMOVE_ASSIGNMENT = "asn.delete.assignment";

	/** Event for removing an assignment content. */
	public static final String EVENT_REMOVE_ASSIGNMENT_CONTENT = "asn.delete.assignmentcontent";

	/** Event for removing an assignment submission. */
	public static final String EVENT_REMOVE_ASSIGNMENT_SUBMISSION = "asn.delete.submission";

	/** Event for accessing an assignment. */
	public static final String EVENT_ACCESS_ASSIGNMENT = "asn.read.assignment";

	/** Event for accessing an assignment content. */
	public static final String EVENT_ACCESS_ASSIGNMENT_CONTENT = "asn.read.assignmentcontent";

	/** Event for accessing an assignment submission. */
	public static final String EVENT_ACCESS_ASSIGNMENT_SUBMISSION = "asn.read.submission";

	/** Event for updating an assignment. */
	public static final String EVENT_UPDATE_ASSIGNMENT = "asn.revise.assignment";

	/** Event for updating an assignment content. */
	public static final String EVENT_UPDATE_ASSIGNMENT_CONTENT = "asn.revise.assignmentcontent";

	/** Event for updating an assignment submission. */
	public static final String EVENT_UPDATE_ASSIGNMENT_SUBMISSION = "asn.revise.submission";

	/** Event for saving an assignment submission. */
	public static final String EVENT_SAVE_ASSIGNMENT_SUBMISSION = "asn.save.submission";

	/** Event for submitting an assignment submission. */
	public static final String EVENT_SUBMIT_ASSIGNMENT_SUBMISSION = "asn.submit.submission";
	
	/** Event for grading an assignment submission. */
	public static final String EVENT_GRADE_ASSIGNMENT_SUBMISSION = "asn.grade.submission";
	
	private static final String NEW_ASSIGNMENT_DUE_DATE_SCHEDULED = "new_assignment_due_date_scheduled";

	protected static final String GROUP_LIST = "group";

	protected static final String GROUP_NAME = "authzGroup";
	
	// the file types for zip download
	protected static final String ZIP_COMMENT_FILE_TYPE = ".txt";
	protected static final String ZIP_SUBMITTED_TEXT_FILE_TYPE = ".html";

//	spring service injection
	
	
	protected ContentReviewService contentReviewService;
	public void setContentReviewService(ContentReviewService contentReviewService) {
		this.contentReviewService = contentReviewService;
	}
	

	
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
	protected String getAccessPoint(boolean relative)
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
		if (!SecurityService.unlock(lock, resource))
		{
			return false;
		}

		return true;

	}// unlockCheck

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
		if (SecurityService.unlock(lock1, resource)) return true;

		// if the second is different, check that
		if ((lock1 != lock2) && (SecurityService.unlock(lock2, resource))) return true;

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

	/** Configuration: cache, or not. */
	protected boolean m_caching = false;

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param path
	 *        The storage path.
	 */
	public void setCaching(String value)
	{
		m_caching = new Boolean(value).booleanValue();
	}

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
	protected ServerConfigurationService m_serverConfigurationService = null;

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

		// make the cache
		if (m_caching)
		{
			m_assignmentCache = m_memoryService
					.newCache(
							"org.sakaiproject.assignment.api.AssignmentService.assignmentCache",
							new AssignmentCacheRefresher(),
							assignmentReference(null, ""));
			m_contentCache = m_memoryService
					.newCache(
							"org.sakaiproject.assignment.api.AssignmentService.contentCache",
							new AssignmentContentCacheRefresher(),
							contentReference(null, ""));
			m_submissionCache = m_memoryService
					.newCache(
							"org.sakaiproject.assignment.api.AssignmentService.submissionCache",
							new AssignmentSubmissionCacheRefresher(),
							submissionReference(null, "", ""));
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
		if (m_caching)
		{
			if (m_assignmentCache != null)
			{
				m_assignmentCache.destroy();
				m_assignmentCache = null;
			}
			if (m_contentCache != null)
			{
				m_contentCache.destroy();
				m_contentCache = null;
			}
			if (m_submissionCache != null)
			{
				m_submissionCache.destroy();
				m_submissionCache = null;
			}
		}

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
		((BaseAssignmentEdit) assignment).setEvent(EVENT_ADD_ASSIGNMENT);

		
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

		((BaseAssignmentEdit) assignment).setEvent(EVENT_ADD_ASSIGNMENT);

		ResourcePropertiesEdit propertyEdit = (BaseResourcePropertiesEdit)assignment.getProperties();
		try
		{
			Time createTime = propertyEdit.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
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
				retVal.setTitle(existingAssignment.getTitle() + " - Copy");
				retVal.setSection(existingAssignment.getSection());
				retVal.setOpenTime(existingAssignment.getOpenTime());
				retVal.setDueTime(existingAssignment.getDueTime());
				retVal.setDropDeadTime(existingAssignment.getDropDeadTime());
				retVal.setCloseTime(existingAssignment.getCloseTime());
				retVal.setDraft(true);
				ResourcePropertiesEdit pEdit = (BaseResourcePropertiesEdit) retVal.getProperties();
				pEdit.addAll(existingAssignment.getProperties());
				addLiveProperties(pEdit);
			}
		}

		
			M_log.debug(this + " ADD DUPLICATE ASSIGNMENT : LEAVING ADD DUPLICATE ASSIGNMENT WITH ID : "
					+ retVal.getId());

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
		
		if (assignment == null) throw new IdUnusedException(assignmentReference);

		// track event
		//EventTrackingService.post(EventTrackingService.newEvent(EVENT_ACCESS_ASSIGNMENT, assignment.getReference(), false));

		return assignment;

	}// getAssignment
	
	protected Assignment findAssignment(String assignmentReference)
	{
		Assignment assignment = null;

		String assignmentId = assignmentId(assignmentReference);

		if ((m_caching) && (m_assignmentCache != null) && (!m_assignmentCache.disabled()))
		{
			// if we have it in the cache, use it
			if (m_assignmentCache.containsKey(assignmentReference))
				assignment = (Assignment) m_assignmentCache.get(assignmentReference);
			if ( assignment == null ) //SAK-12447 cache.get can return null on expired
			{
				assignment = m_assignmentStorage.get(assignmentId);

				// cache the result
				m_assignmentCache.put(assignmentReference, assignment);
			}
		}
		else
		{
			assignment = m_assignmentStorage.get(assignmentId);
		}
		
		return assignment;
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
		if (userId == null)
		{
			userId = SessionManager.getCurrentSessionUserId();
		}
		List assignments = new Vector();

		if ((m_caching) && (m_assignmentCache != null) && (!m_assignmentCache.disabled()))
		{
			// if the cache is complete, use it
			if (m_assignmentCache.isComplete())
			{
				assignments = m_assignmentCache.getAll();
				// TODO: filter by context
			}

			// otherwise get all the assignments from storage
			else
			{
				// Note: while we are getting from storage, storage might change. These can be processed
				// after we get the storage entries, and put them in the cache, and mark the cache complete.
				// -ggolden
				synchronized (m_assignmentCache)
				{
					// if we were waiting and it's now complete...
					if (m_assignmentCache.isComplete())
					{
						assignments = m_assignmentCache.getAll();
						return assignments;
					}

					// save up any events to the cache until we get past this load
					m_assignmentCache.holdEvents();

					assignments = m_assignmentStorage.getAll(context);

					// update the cache, and mark it complete
					for (int i = 0; i < assignments.size(); i++)
					{
						Assignment assignment = (Assignment) assignments.get(i);
						m_assignmentCache.put(assignment.getReference(), assignment);
					}

					m_assignmentCache.setComplete();
					// TODO: not reall, just for context

					// now we are complete, process any cached events
					m_assignmentCache.processEvents();
				}
			}
		}

		else
		{
			// // if we have done this already in this thread, use that
			// assignments = (List) CurrentService.getInThread(context+".assignment.assignments");
			// if (assignments == null)
			// {
			assignments = m_assignmentStorage.getAll(context);
			//				
			// // "cache" the assignments in the current service in case they are needed again in this thread...
			// if (assignments != null)
			// {
			// CurrentService.setInThread(context+".assignment.assignments", assignments);
			// }
			// }
		}

		List rv = new Vector();
		
		// check for the allowed groups of the current end use if we need it, and only once
		Collection allowedGroups = null;
		Site site = null;
		try
		{
			site = SiteService.getSite(context);
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + " assignments(String, String) " + e.getMessage() + " context=" + context);
		}
		
		for (int x = 0; x < assignments.size(); x++)
		{
			Assignment tempAssignment = (Assignment) assignments.get(x);
			if (tempAssignment.getAccess() == Assignment.AssignmentAccess.GROUPED)
			{
				
				// Can at least one of the designated groups been found
				boolean groupFound = false;
				
				// if grouped, check that the end user has get access to any of this assignment's groups; reject if not

				// check the assignment's groups to the allowed (get) groups for the current user
				Collection asgGroups = tempAssignment.getGroups();

				for (Iterator iAsgGroups=asgGroups.iterator(); site!=null && !groupFound && iAsgGroups.hasNext();)
				{
					String groupId = (String) iAsgGroups.next();
					try
					{
						if (site.getGroup(groupId) != null)
						{
							groupFound = true;
						}
					}
					catch (Exception ee)
					{
						M_log.warn(this + " assignments(String, String) " + ee.getMessage() + " groupId = " + groupId);
					}
					
				}
				
				if (!groupFound)
				{
					// if none of the group exists, mark the assignment as draft and list it
					String assignmentId = tempAssignment.getId();
					try
					{
						AssignmentEdit aEdit = editAssignment(assignmentReference(context, assignmentId));
						aEdit.setDraft(true);
						commitEdit(aEdit);
						rv.add(getAssignment(assignmentId));
					}
					catch (Exception e)
					{
						M_log.warn(this + " assignments(String, String) " + e.getMessage() + " assignment id =" + assignmentId);
						continue;
					}
				}
				else
				{
					// we need the allowed groups, so get it if we have not done so yet
					if (allowedGroups == null)
					{
						allowedGroups = getGroupsAllowGetAssignment(context, userId);
					}
					
					// reject if there is no intersection
					if (!isIntersectionGroupRefsToGroups(asgGroups, allowedGroups)) continue;
					
					rv.add(tempAssignment);
				}
			}
			else
			{
				/// if not reject, add it
				rv.add(tempAssignment);
			}
		}

		return rv;
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

		((BaseAssignmentEdit) assignmentEdit).setEvent(EVENT_UPDATE_ASSIGNMENT);

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
				M_log.warn(this + " commitEdit(): closed AssignmentEdit " + e.getMessage() + " assignment id=" + assignment.getId());
			}
			return;
		}

		// update the properties
		addLiveUpdateProperties(assignment.getPropertiesEdit());

		// complete the edit
		m_assignmentStorage.commit(assignment);

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
				M_log.warn(this + " cancelEdit(): closed AssignmentEdit " + e.getMessage() + " assignment id=" + assignment.getId());
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
					M_log.warn(this + " removeAssignment(): closed AssignmentEdit" + e.getMessage() + " assignment id=" + assignment.getId());
				}
				return;
			}

			// CHECK PERMISSION
			unlock(SECURE_REMOVE_ASSIGNMENT, assignment.getReference());

			// complete the edit
			m_assignmentStorage.remove(assignment);

			// track event
			EventTrackingService.post(EventTrackingService.newEvent(EVENT_REMOVE_ASSIGNMENT, assignment.getReference(), true));

			// close the edit object
			((BaseAssignmentEdit) assignment).closeEdit();

			// remove any realm defined for this resource
			try
			{
				AuthzGroupService.removeAuthzGroup(assignment.getReference());
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(this + " removeAssignment: removing realm for assignment reference=" + assignment.getReference() + " : " + e.getMessage());
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
					M_log.warn(this + " removeAssignmentAndAllReferences(): closed AssignmentEdit" + e.getMessage() + " assignment id=" + assignment.getId());
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
						M_log.warn(this + "removeAssignmentAndAllReference: User does not have permission to remove submission " + sReference + " for assignment: " + assignment.getId()  + e.getMessage());
					}
					catch (InUseException e) 
					{
						M_log.warn(this + "removeAssignmentAndAllReference: submission " + sReference + " for assignment: " + assignment.getId() + " is in use. " + e.getMessage());
					}catch (IdUnusedException e) 
					{
						M_log.warn(this + "removeAssignmentAndAllReference: submission " + sReference + " for assignment: " + assignment.getId() + " does not exist. " + e.getMessage());
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
				M_log.warn(this + " removeAssignmentAndAllReferences(): cannot remove non-empty AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
			}
			catch (PermissionException e)
			{
				M_log.warn(this + " removeAssignmentAndAllReferences(): not allowed to remove AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
			}
			catch (InUseException e)
			{
				M_log.warn(this + " removeAssignmentAndAllReferences(): AssignmentContent object for assignment = " + assignment.getId() + " is in used. " + e.getMessage());
			}
			catch (IdUnusedException e)
			{
				M_log.warn(this + " removeAssignmentAndAllReferences(): cannot find AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
			}
			
			// 7. remove assignment
			m_assignmentStorage.remove(assignment);

			// close the edit object
			((BaseAssignmentEdit) assignment).closeEdit();

			// 8. remove any realm defined for this resource
			try
			{
				AuthzGroupService.removeAuthzGroup(assignment.getReference());
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(this + " removeAssignment: removing realm for assignment reference=" + assignment.getReference() + " : " + e.getMessage());
			}
			
			// track event
			EventTrackingService.post(EventTrackingService.newEvent(EVENT_REMOVE_ASSIGNMENT, assignment.getReference(), true));

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
		    M_log.warn(this + "removeAssociatedTaggingItem: User does not have permission to remove tags for assignment: " + assignment.getId() + " via transferCopyEntities");
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
		        M_log.warn(this + "getCalendar: No calendar found for site: " + contextId);
		        calendar = null;
		    }
		    catch (PermissionException e)
		    {
		        M_log.warn(this + "getCalendar: The current user does not have permission to access " +
		                "the calendar for context: " + contextId, e);
		    }
		    catch (Exception ex)
		    {
		        M_log.warn(this + "getCalendar: Unknown exception occurred retrieving calendar for site: " + contextId, ex);
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
	                    M_log.warn(this + ":removeCalendarEvent " + ee.getMessage());
	                }
	                catch (PermissionException ee)
	                {
	                    M_log.warn(this + ":removeCalendarEvent " + ee.getMessage());
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
	                    M_log.warn(this + ":removeCalendarEvent not allowed to remove calendar event for assignment = " + aEdit.getTitle() + ". ");
	                }
	                catch (InUseException ee)
	                {
	                    M_log.warn(this + ":removeCalendarEvent someone else is editing calendar event for assignment = " + aEdit.getTitle() + ". ");
	                }
	                catch (IdUnusedException ee)
	                {
	                    M_log.warn(this + ":removeCalendarEvent calendar event are in use for assignment = " + aEdit.getTitle() + " and event =" + event.getId());
	                }
	            }
	        }
	    }
	}


	private AnnouncementChannel getAnnouncementChannel(String contextId)
	{
	    AnnouncementService aService = org.sakaiproject.announcement.cover.AnnouncementService.getInstance();
	    AnnouncementChannel channel = null;
	    String channelId = m_serverConfigurationService.getString("channel", null);
	    if (channelId == null)
	    {
	        channelId = m_announcementService.channelReference(contextId, SiteService.MAIN_CONTAINER);
	        try
	        {
	            channel = aService.getAnnouncementChannel(channelId);
	        }
	        catch (IdUnusedException e)
	        {
	            M_log.warn(this + "getAnnouncement:No announcement channel found");
	            channel = null;
	        }
	        catch (PermissionException e)
	        {
	            M_log.warn(this + "getAnnouncement:Current user not authorized to deleted annc associated " +
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
	        String openDateAnnounced = StringUtil.trimToNull(pEdit.getProperty("new_assignment_open_date_announced"));
	        String openDateAnnouncementId = StringUtil.trimToNull(pEdit.getProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
	        if (openDateAnnounced != null && openDateAnnouncementId != null)
	        {
	            try
	            {
	                channel.removeMessage(openDateAnnouncementId);
	            }
	            catch (PermissionException e)
	            {
	                M_log.warn(this + ":removeAnnouncement " + e.getMessage());
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
		((BaseAssignmentContentEdit) content).setEvent(EVENT_ADD_ASSIGNMENT_CONTENT);

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

		((BaseAssignmentContentEdit) content).setEvent(EVENT_ADD_ASSIGNMENT_CONTENT);

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
				retVal.setTitle(existingContent.getTitle() + " - Copy");
				retVal.setInstructions(existingContent.getInstructions());
				retVal.setHonorPledge(existingContent.getHonorPledge());
				retVal.setTypeOfSubmission(existingContent.getTypeOfSubmission());
				retVal.setTypeOfGrade(existingContent.getTypeOfGrade());
				retVal.setMaxGradePoint(existingContent.getMaxGradePoint());
				retVal.setGroupProject(existingContent.getGroupProject());
				retVal.setIndividuallyGraded(existingContent.individuallyGraded());
				retVal.setReleaseGrades(existingContent.releaseGrades());
				retVal.setAllowAttachments(existingContent.getAllowAttachments());

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
								
									M_log.warn(this + " LEAVING ADD DUPLICATE CONTENT : " + e.toString());
							}	
						}
					}
				}
				
				ResourcePropertiesEdit pEdit = (BaseResourcePropertiesEdit) retVal.getPropertiesEdit();
				pEdit.addAll(existingContent.getProperties());
				addLiveProperties(pEdit);
			}
		}

		
			M_log.debug(this + " LEAVING ADD DUPLICATE CONTENT WITH ID : " + retVal.getId());

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

		if ((m_caching) && (m_contentCache != null) && (!m_contentCache.disabled()))
		{
			if (m_contentCache.containsKey(contentReference))
				content = (AssignmentContent) m_contentCache.get(contentReference);
			if ( content == null ) //SAK-12447 cache.get can return null on expired
			{
				content = m_contentStorage.get(contentId);

				// cache the result
				m_contentCache.put(contentReference, content);
			}
		}

		else
		{
			// // if we have done this already in this thread, use that
			// content = (AssignmentContent) CurrentService.getInThread(contentId+".assignment.content");
			// if (content == null)
			// {
			content = m_contentStorage.get(contentId);
			//				
			// // "cache" the content in the current service in case they are needed again in this thread...
			// if (content != null)
			// {
			// CurrentService.setInThread(contentId+".assignment.content", contentId);
			// }
			// }
		}

		if (content == null) throw new IdUnusedException(contentId);

		M_log.debug(this + " GOT ASSIGNMENT CONTENT : ID : " + content.getId());

		// track event
		// EventTrackingService.post(EventTrackingService.newEvent(EVENT_ACCESS_ASSIGNMENT_CONTENT, content.getReference(), false));

		return content;

	}// getAssignmentContent

	/**
	 * Access all AssignmentContent objects - known to us (not from external providers).
	 * 
	 * @return A list of AssignmentContent objects.
	 */
	protected List getAssignmentContents(String context)
	{
		List contents = new Vector();

		if ((m_caching) && (m_contentCache != null) && (!m_contentCache.disabled()))
		{
			// if the cache is complete, use it
			if (m_contentCache.isComplete())
			{
				contents = m_contentCache.getAll();
				// TODO: filter by context
			}

			// otherwise get all the contents from storage
			else
			{
				// Note: while we are getting from storage, storage might change. These can be processed
				// after we get the storage entries, and put them in the cache, and mark the cache complete.
				// -ggolden
				synchronized (m_contentCache)
				{
					// if we were waiting and it's now complete...
					if (m_contentCache.isComplete())
					{
						contents = m_contentCache.getAll();
						return contents;
					}

					// save up any events to the cache until we get past this load
					m_contentCache.holdEvents();

					contents = m_contentStorage.getAll(context);

					// update the cache, and mark it complete
					for (int i = 0; i < contents.size(); i++)
					{
						AssignmentContent content = (AssignmentContent) contents.get(i);
						m_contentCache.put(content.getReference(), content);
					}

					m_contentCache.setComplete();
					// TODO: not really, just for context

					// now we are complete, process any cached events
					m_contentCache.processEvents();
				}
			}
		}

		else
		{
			// // if we have done this already in this thread, use that
			// contents = (List) CurrentService.getInThread(context+".assignment.contents");
			// if (contents == null)
			// {
			contents = m_contentStorage.getAll(context);
			//
			// // "cache" the contents in the current service in case they are needed again in this thread...
			// if (contents != null)
			// {
			// CurrentService.setInThread(context+".assignment.contents", contents);
			// }
			// }
		}

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

		((BaseAssignmentContentEdit) content).setEvent(EVENT_UPDATE_ASSIGNMENT_CONTENT);

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
				M_log.warn(this + " commitEdit(): closed AssignmentContentEdit " + e + " content id=" + content.getId());
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
				M_log.warn(this + " cancelEdit(): closed AssignmentContentEdit " + e.getMessage() + " assignment content id=" + content.getId());
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
					M_log.warn(this + " removeAssignmentContent(): closed AssignmentContentEdit " + e.getMessage() + " assignment content id=" + content.getId());
				}
				return;
			}

			// CHECK SECURITY
			unlock(SECURE_REMOVE_ASSIGNMENT_CONTENT, content.getReference());

			// complete the edit
			m_contentStorage.remove(content);

			// track event
			EventTrackingService.post(EventTrackingService.newEvent(EVENT_REMOVE_ASSIGNMENT_CONTENT, content.getReference(),
					true));

			// close the edit object
			((BaseAssignmentContentEdit) content).closeEdit();

			// remove any realm defined for this resource
			try
			{
				AuthzGroupService.removeAuthzGroup(AuthzGroupService.getAuthzGroup(content.getReference()));
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(this + " removeAssignmentContent: removing realm for assignment content reference=" + content.getReference() + " : " + e);
			}
			catch (GroupNotDefinedException e)
			{
				M_log.warn(this + " removeAssignmentContent " + content.getReference() + e.getMessage());
			}
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

		unlock(SECURE_ADD_ASSIGNMENT_SUBMISSION, key);

		M_log.debug(this + " ADD SUBMISSION : UNLOCKED");

		// storage
		AssignmentSubmissionEdit submission = m_submissionStorage.put(submissionId, assignmentId, submitterId, null, null, null);

		submission.setContext(context);
		
		
			M_log.debug(this + " LEAVING ADD SUBMISSION : REF : " + submission.getReference());

		// event for tracking
		((BaseAssignmentSubmissionEdit) submission).setEvent(EVENT_ADD_ASSIGNMENT_SUBMISSION);

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

		((BaseAssignmentSubmissionEdit) submission).setEvent(EVENT_ADD_ASSIGNMENT_SUBMISSION);

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
				User[] submitters = submission.getSubmitters();
				if (submitters != null && submitters.length == 1 && submitters[0].equals(currentUser))
				{
					// is editing one's own submission
					// then test against extra criteria depend on the status of submission
					try
					{
						Assignment a = submission.getAssignment();
						if (canSubmit(a.getContext(), a))
						{
							notAllowed = false;
						}
					}
					catch (Exception e)
					{
						M_log.warn(this + " editSubmission(): cannot get assignment for submission " + submissionReference + e.getMessage());
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

		((BaseAssignmentSubmissionEdit) submission).setEvent(EVENT_UPDATE_ASSIGNMENT_SUBMISSION);

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
				M_log.warn(this + " commitEdit(): closed AssignmentSubmissionEdit assignment submission id=" + submission.getId() + e.getMessage());
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
			
			// track it
			if (!s.getSubmitted())
			{
				// saving a submission
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_SAVE_ASSIGNMENT_SUBMISSION, submissionRef, true));
			}
			else if (returnedTime == null && !s.getReturned() && (submittedTime == null /*grading non-submissions*/
																|| (submittedTime != null && (s.getTimeLastModified().getTime() - submittedTime.getTime()) > 1000*60 /*make sure the last modified time is at least one minute after the submit time*/)))
			{
				// graded and saved before releasing it
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_GRADE_ASSIGNMENT_SUBMISSION, submissionRef, true));
			}
			else if (returnedTime != null && s.getGraded() && (submittedTime == null/*returning non-submissions*/ 
											|| (submittedTime != null && returnedTime.after(submittedTime))/*returning normal submissions*/ 
											|| (submittedTime != null && submittedTime.after(returnedTime) && s.getTimeLastModified().after(submittedTime))/*grading the resubmitted assignment*/))
			{
				// releasing a submitted assignment or releasing grade to an unsubmitted assignment
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_GRADE_ASSIGNMENT_SUBMISSION, submissionRef, true));
			}
			else if (submittedTime == null) /*grading non-submission*/
			{
				// releasing a submitted assignment or releasing grade to an unsubmitted assignment
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_GRADE_ASSIGNMENT_SUBMISSION, submissionRef, true));
			}
			else
			{
				// submitting a submission
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_SUBMIT_ASSIGNMENT_SUBMISSION, submissionRef, true));
			
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
			M_log.warn(this + " commitEdit(), submissionId=" + submissionRef, e);
		}
		catch (PermissionException e)
		{
			M_log.warn(this + " commitEdit(), submissionId=" + submissionRef, e);
		}

	} // commitEdit(Submission)

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
			
			String submitterId = s.getSubmitterIdString();
			
			// filter out users who's not able to grade this submission
			List finalReceivers = new Vector();
			
			HashSet receiverSet = new HashSet();
			if (a.getAccess().equals(Assignment.AssignmentAccess.GROUPED))
			{
				Collection groups = a.getGroups();
				for (Iterator gIterator = groups.iterator(); gIterator.hasNext();)
				{
					String g = (String) gIterator.next();
					try
					{
						AuthzGroup aGroup = AuthzGroupService.getAuthzGroup(g);
						if (aGroup.isAllowed(submitterId, AssignmentService.SECURE_ADD_ASSIGNMENT_SUBMISSION))
						{
							for (Iterator rIterator = receivers.iterator(); rIterator.hasNext();)
							{
								User rUser = (User) rIterator.next();
								String rUserId = rUser.getId();
								if (!receiverSet.contains(rUserId) && aGroup.isAllowed(rUserId, AssignmentService.SECURE_GRADE_ASSIGNMENT_SUBMISSION))
								{
									finalReceivers.add(rUser);
									receiverSet.add(rUserId);
								}
							}
						}
					}
					catch (Exception e)
					{
						M_log.warn(this + " notificationToInstructors, group id =" + g + " " + e.getMessage());
					}
				}
			}
			else
			{
				finalReceivers.addAll(receivers);
			}
			
			String messageBody = getNotificationMessage(s);
			
			if (notiOption.equals(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH))
			{
				// send the message immidiately
				EmailService.sendToUsers(finalReceivers, getHeaders(null), messageBody);
			}
			else if (notiOption.equals(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST))
			{
				// just send plain/text version for now
				String digestMsgBody = getPlainTextNotificationMessage(s);
				
				// digest the message to each user
				for (Iterator iReceivers = finalReceivers.iterator(); iReceivers.hasNext();)
				{
					User user = (User) iReceivers.next();
					DigestService.digest(user.getId(), getSubject(), digestMsgBody);
				}
			}
		}
	}
	
	/**
	 * get only the plain text of notification message
	 * @param s
	 * @return
	 */
	protected String getPlainTextNotificationMessage(AssignmentSubmission s)
	{
		StringBuilder message = new StringBuilder();
		message.append(plainTextContent(s));
		return message.toString();
	}

	/**
	 * send notification to student if necessary
	 * @param s
	 */
	private void notificationToStudent(AssignmentSubmission s) 
	{
		if (m_serverConfigurationService.getBoolean("assignment.submission.confirmation.email", true))
		{
			//send notification
			User u = UserDirectoryService.getCurrentUser();
			
			if (StringUtil.trimToNull(u.getEmail()) != null)
			{
				List receivers = new Vector();
				receivers.add(u);
				
				EmailService.sendToUsers(receivers, getHeaders(u.getEmail()), getNotificationMessage(s));
			}
		}
	}
	
	protected List<String> getHeaders(String receiverEmail)
	{
		List<String> rv = new Vector<String>();
		
		rv.add("MIME-Version: 1.0");
		rv.add("Content-Type: multipart/alternative; boundary=\""+MULTIPART_BOUNDARY+"\"");
		// set the subject
		rv.add(getSubject());

		// from
		rv.add(getFrom());
		
		// to
		if (StringUtil.trimToNull(receiverEmail) != null)
		{
			rv.add("To: " + receiverEmail);
		}
		
		return rv;
	}
	
	protected String getSubject()
	{
		return rb.getString("noti.subject.label") + " " + rb.getString("noti.subject.content");
	}
	
	protected String getFrom()
	{
		return "From: " + "\"" + m_serverConfigurationService.getString("ui.service", "Sakai") + "\"<no-reply@"+ m_serverConfigurationService.getServerName() + ">";
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
	protected String getNotificationMessage(AssignmentSubmission s)
	{	
		StringBuilder message = new StringBuilder();
		message.append(MIME_ADVISORY);
		message.append(BOUNDARY_LINE);
		message.append(plainTextHeaders());
		message.append(plainTextContent(s));
		message.append(BOUNDARY_LINE);
		message.append(htmlHeaders());
		message.append(htmlPreamble());
		message.append(htmlContent(s));
		message.append(htmlEnd());
		message.append(TERMINATION_LINE);
		return message.toString();
	}
	
	protected String plainTextHeaders() {
		return "Content-Type: text/plain\n\n";
	}
	
	protected String plainTextContent(AssignmentSubmission s) {
		return FormattedText.convertFormattedTextToPlaintext(htmlContent(s));
	}
	
	protected String htmlHeaders() {
		return "Content-Type: text/html\n\n";
	}
	
	protected String htmlPreamble() {
		StringBuilder buf = new StringBuilder();
		buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
		buf.append("    \"http://www.w3.org/TR/html4/loose.dtd\">\n");
		buf.append("<html>\n");
		buf.append("  <head><title>");
		buf.append(getSubject());
		buf.append("</title></head>\n");
		buf.append("  <body>\n");
		return buf.toString();
	}
	
	protected String htmlEnd() {
		return "\n  </body>\n</html>\n";
	}

	private String htmlContent(AssignmentSubmission s) 
	{
		String newline = "<br />\n";
		
		Assignment a = s.getAssignment();
		
		String context = s.getContext();
		
		String siteTitle = "";
		String siteId = "";
		try
		{
			Site site = SiteService.getSite(context);
			siteTitle = site.getTitle();
			siteId = site.getId();
		}
		catch (Exception ee)
		{
			M_log.warn(this + " htmlContent(), site id =" + context + " " + ee.getMessage());
		}
		
		StringBuilder buffer = new StringBuilder();
		// site title and id
		buffer.append(rb.getString("noti.site.title") + " " + siteTitle + newline);
		buffer.append(rb.getString("noti.site.id") + " " + siteId +newline + newline);
		// assignment title and due date
		buffer.append(rb.getString("noti.assignment") + " " + a.getTitle()+newline);
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
			submitterNames = submitterNames.concat(u.getDisplayName());
			submitterIds = submitterIds.concat(u.getDisplayId());
		}
		buffer.append(rb.getString("noti.student") + " " + submitterNames);
		if (submitterIds.length() != 0)
		{
			buffer.append("( " + submitterIds + " )");
		}
		buffer.append(newline + newline);
		
		// submit time
		buffer.append(rb.getString("noti.submit.id") + " " + s.getId() + newline);
		
		// submit time 
		buffer.append(rb.getString("noti.submit.time") + " " + s.getTimeSubmitted().toStringLocalFull() + newline + newline);
		
		// submit text
		String text = StringUtil.trimToNull(s.getSubmittedText());
		if ( text != null)
		{
			buffer.append(rb.getString("noti.submit.text") + newline + newline + Validator.escapeHtmlFormattedText(text) + newline + newline);
		}
		
		// attachment if any
		List attachments = s.getSubmittedAttachments();
		if (attachments != null && attachments.size() >0)
		{
			buffer.append(rb.getString("noti.submit.attachments") + newline + newline);
			for (int j = 0; j<attachments.size(); j++)
			{
				Reference r = (Reference) attachments.get(j);
				buffer.append(r.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME) + "(" + r.getProperties().getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH)+ ")\n");
			}
		}
		
		return buffer.toString();
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
				M_log.warn(this + " cancelEdit(): closed AssignmentSubmissionEdit assignment submission id=" + submission.getId() + " " + e.getMessage());
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
					M_log.warn(this + " removeSubmission(): closed AssignmentSubmissionEdit id=" + submission.getId()  + " "  + e.getMessage());
				}
				return;
			}

			// check security
			unlock(SECURE_REMOVE_ASSIGNMENT_SUBMISSION, submission.getReference());

			// complete the edit
			m_submissionStorage.remove(submission);

			// track event
			EventTrackingService.post(EventTrackingService.newEvent(EVENT_REMOVE_ASSIGNMENT_SUBMISSION, submission.getReference(),
					true));

			// close the edit object
			((BaseAssignmentSubmissionEdit) submission).closeEdit();

			// remove any realm defined for this resource
			try
			{
				AuthzGroupService.removeAuthzGroup(AuthzGroupService.getAuthzGroup(submission.getReference()));
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(this + " removeSubmission: removing realm for : " + submission.getReference() + " : " + e.getMessage());
			}
			catch (GroupNotDefinedException e)
			{
				M_log.warn(this + " removeSubmission: cannot find group for submission " + submission.getReference() + " : " + e.getMessage());
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
		List<AssignmentSubmission> submissions = new Vector<AssignmentSubmission>();

		if ((m_caching) && (m_submissionCache != null) && (!m_submissionCache.disabled()))
		{
			// if the cache is complete, use it
			if (m_submissionCache.isComplete())
			{
				submissions = m_submissionCache.getAll();
				// TODO: filter by context
			}

			// otherwise get all the submissions from storage
			else
			{
				// Note: while we are getting from storage, storage might change. These can be processed
				// after we get the storage entries, and put them in the cache, and mark the cache complete.
				// -ggolden
				synchronized (m_submissionCache)
				{
					// if we were waiting and it's now complete...
					if (m_submissionCache.isComplete())
					{
						submissions = m_submissionCache.getAll();
						return submissions;
					}

					// save up any events to the cache until we get past this load
					m_submissionCache.holdEvents();

					submissions = m_submissionStorage.getAll(context);

					// update the cache, and mark it complete
					for (int i = 0; i < submissions.size(); i++)
					{
						AssignmentSubmission submission = (AssignmentSubmission) submissions.get(i);
						m_submissionCache.put(submission.getReference(), submission);
					}

					m_submissionCache.setComplete();
					// TODO: not really! just for context

					// now we are complete, process any cached events
					m_submissionCache.processEvents();
				}
			}
		}

		else
		{
			// // if we have done this already in this thread, use that
			// submissions = (List) CurrentService.getInThread(context+".assignment.submissions");
			// if (submissions == null)
			// {
			submissions = m_submissionStorage.getAll(context);
			//
			// // "cache" the submissions in the current service in case they are needed again in this thread...
			// if (submissions != null)
			// {
			// CurrentService.setInThread(context+".assignment.submissions", submissions);
			// }
			// }
		}
		
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
			String submitterid = (String)sub.getSubmitterIds().get(0);
			if (reportsMap.containsKey(submitterid)) {
				ContentReviewItem report = reportsMap.get(submitterid);
				AssignmentSubmissionEdit edit;
				try {
					edit = this.editSubmission(sub.getReference());
					edit.setReviewScore(report.getReviewScore());
					edit.setReviewIconUrl(report.getIconUrl());
					edit.setReviewStatus(report.getStatus().toString());
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
		Vector retVal = new Vector();
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
		Vector retVal = new Vector();
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
	 * get proper assignments for specified context and user
	 * @param context
	 * @param user
	 * @return
	 */
	private Iterator assignmentsForContextAndUser(String context, String userId) 
	{
		Assignment tempAssignment = null;
		Vector retVal = new Vector();
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
		Vector retVal = new Vector();
		List allAssignments = new Vector();

		if (context != null)
		{
			allAssignments = getAssignments(context);
			for (int x = 0; x < allAssignments.size(); x++)
			{
				tempAssignment = (Assignment) allAssignments.get(x);
				
				if ((context.equals(tempAssignment.getContext()))
						|| (context.equals(getGroupNameFromContext(tempAssignment.getContext()))))
				{
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
		return SecurityService.isSuperUser() // super user can always see it
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
			submission = m_submissionStorage.get(assignmentId, person.getId());
		}

		if (submission != null)
		{
			try
			{
				unlock2(SECURE_ACCESS_ASSIGNMENT_SUBMISSION, SECURE_ACCESS_ASSIGNMENT, submission.getReference());
			}
			catch (PermissionException e)
			{
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
				List submitters = sub.getSubmitterIds();
				for (int a = 0; a < submitters.size(); a++)
				{
					String aUserId = (String) submitters.get(a);
					
						M_log.debug(this + " getSubmission(List, User) comparing aUser id : " + aUserId + " and chosen user id : "
								+ person.getId());
					if (aUserId.equals(person.getId()))
					{
						
							M_log.debug(this + " getSubmission(List, User) found a match : return value is " + sub.getId());
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
		List retVal = new Vector();

		if (assignment != null)
		{
			retVal = getSubmissions(assignment.getId());
		}
		
		return retVal;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getSubmittedSubmissionsCount(String assignmentId)
	{
		return m_submissionStorage.getSubmittedSubmissionsCount(assignmentId);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getUngradedSubmissionsCount(String assignmentId)
	{
		return m_submissionStorage.getUngradedSubmissionsCount(assignmentId);
		
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

		if ((m_caching) && (m_submissionCache != null) && (!m_submissionCache.disabled()))
		{
			// if we have it in the cache, use it
			if (m_submissionCache.containsKey(submissionReference))
				submission = (AssignmentSubmission) m_submissionCache.get(submissionReference);
			if ( submission == null ) //SAK-12447 cache.get can return null on expired
 			{
				submission = m_submissionStorage.get(submissionId);

				// cache the result
				m_submissionCache.put(submissionReference, submission);
			}
		}

		else
		{
			// // if we have done this already in this thread, use that
			// submission = (AssignmentSubmission) CurrentService.getInThread(submissionId+".assignment.submission");
			// if (submission == null)
			// {
			submission = m_submissionStorage.get(submissionId);
			//				
			// // "cache" the submission in the current service in case they are needed again in this thread...
			// if (submission != null)
			// {
			// CurrentService.setInThread(submissionId+".assignment.submission", submission);
			// }
			// }
		}

		if (submission == null) throw new IdUnusedException(submissionId);

		// track event
		// EventTrackingService.post(EventTrackingService.newEvent(EVENT_ACCESS_ASSIGNMENT_SUBMISSION, submission.getReference(), false));

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
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context + Entity.SEPARATOR;

		
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
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context + Entity.SEPARATOR;
		
		{
			M_log.debug(this + " allowReceiveSubmissionNotificationUsers with resource string : " + resourceString);
			M_log.debug("                                   				 	context string : " + context);
		}
		return SecurityService.unlockUsers(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS, resourceString);

	} // allowAddAssignmentUsers
	
	/**
	 * @inheritDoc
	 */
	public boolean allowAddAssignment(String context)
	{
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context + Entity.SEPARATOR;
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
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context  + Entity.SEPARATOR;

		
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
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context + Entity.SEPARATOR;

		
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
		Collection rv = new Vector();
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
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context + Entity.SEPARATOR;

		
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
		Collection rv = new Vector();
		try
		{
			// get the site groups
			Site site = SiteService.getSite(context);
			Collection groups = site.getGroups();

			if (SecurityService.isSuperUser())
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
			if (AuthzGroupService.isAllowed(userId, SECURE_ALL_GROUPS, SiteService.siteReference(context)) && unlockCheck(function, SiteService.siteReference(context)))
			{
				return groups;
			}

			// otherwise, check the groups for function

			// get a list of the group refs, which are authzGroup ids
			Collection groupRefs = new Vector();
			for (Iterator i = groups.iterator(); i.hasNext();)
			{
				Group group = (Group) i.next();
				groupRefs.add(group.getReference());
			}

			// ask the authzGroup service to filter them down based on function
			groupRefs = AuthzGroupService.getAuthzGroupsIsAllowed(userId,
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
	 * Get the List of Users who can addSubmission() for this assignment.
	 * 
	 * @param assignmentReference -
	 *        a reference to an assignment
	 * @return the List (User) of users who can addSubmission() for this assignment.
	 */
	public List allowAddSubmissionUsers(String assignmentReference)
	{
		return SecurityService.unlockUsers(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);

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
		return SecurityService.unlockUsers(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference);

	} // allowGradeAssignmentUsers
	
	/**
	 * @inheritDoc
	 * @param context
	 * @return
	 */
	public List allowAddAnySubmissionUsers(String context)
	{
		List rv = new Vector();
		
		try
		{
			AuthzGroup group = AuthzGroupService.getAuthzGroup(SiteService.siteReference(context));
			
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
			M_log.warn(this + " allowAddAnySubmissionUsers " + e.getMessage() + " context=" + context);
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
		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a" + Entity.SEPARATOR + context + Entity.SEPARATOR;
		
		{
			M_log.debug(this + " allowAddAssignmentUsers with resource string : " + resourceString);
			M_log.debug("                                    	context string : " + context);
		}
		return SecurityService.unlockUsers(SECURE_ADD_ASSIGNMENT, resourceString);

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
		String typeGradesString = new String(REF_TYPE_GRADES + Entity.SEPARATOR);
		String context = ref.substring(ref.indexOf(typeGradesString) + typeGradesString.length());

		// get site title for display purpose
		String siteTitle = "";
		try
		{
			Site s = SiteService.getSite(context);
			siteTitle = s.getTitle();
		}
		catch (Exception e)
		{
			// ignore exception
			M_log.debug(this + ":getGradesSpreadsheet cannot get site context=" + context + e.getMessage());
		}
		
		// does current user allowed to grade any assignment?
		boolean allowGradeAny = false;
		List assignmentsList = getListAssignmentsForContext(context);
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
			return null;
		}
		else
		{
			short rowNum = 0;
			HSSFWorkbook wb = new HSSFWorkbook();
			
			// a tab title in a workbook have a maximum length of 31 chars.
			// otherwise an Exception will been thrown "Sheet name cannot be blank, greater than 31 chars, or contain any of /\*?[]"
			// we truncate it if it's too long
			String sheetTitle = siteTitle;
			int siteTitleLength = sheetTitle.length();
			if (siteTitleLength > 31) {
			    M_log.info(this + " Site title is too long (" + siteTitleLength + " chars) truncating it down to 31 chars!");
			    sheetTitle = sheetTitle.substring(0, 31);
			}
			HSSFSheet sheet = wb.createSheet(Validator.escapeZipEntry(sheetTitle));
	
			// Create a row and put some cells in it. Rows are 0 based.
			HSSFRow row = sheet.createRow(rowNum++);
	
			row.createCell((short) 0).setCellValue(rb.getString("download.spreadsheet.title"));
	
			// empty line
			row = sheet.createRow(rowNum++);
			row.createCell((short) 0).setCellValue("");
	
			// site title
			row = sheet.createRow(rowNum++);
			row.createCell((short) 0).setCellValue(rb.getString("download.spreadsheet.site") + siteTitle);
	
			// download time
			row = sheet.createRow(rowNum++);
			row.createCell((short) 0).setCellValue(
					rb.getString("download.spreadsheet.date") + TimeService.newTime().toStringLocalFull());
	
			// empty line
			row = sheet.createRow(rowNum++);
			row.createCell((short) 0).setCellValue("");
	
			HSSFCellStyle style = wb.createCellStyle();
	
			// this is the header row number
			short headerRowNumber = rowNum;
			// set up the header cells
			row = sheet.createRow(rowNum++);
			short cellNum = 0;
			
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
			List members = new Vector();
			// hashtable which stores the Excel row number for particular user
			Hashtable user_row = new Hashtable();
			
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
					user_row.put(u.getId(), new Integer(rowNum));
					// increase row
					rowNum++;
					// put user displayid and sortname in the first two cells
					cellNum = 0;
					row.createCell(cellNum++).setCellValue(u.getSortName());
					row.createCell(cellNum).setCellValue(u.getDisplayId());
				}
				catch (Exception e)
				{
					M_log.warn(this + " getGradesSpreadSheet " + e.getMessage() + " userId = " + userId);
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
					cellNum = (short) (index + 2);
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
						
						String userId = (String) submission.getSubmitterIds().get(0);
						
						if (user_row.containsKey(userId))
						{	
							// find right row
							row = sheet.getRow(((Integer)user_row.get(userId)).intValue());
						
							if (submission.getGraded() && submission.getGradeReleased() && submission.getGrade() != null)
							{
								// graded and released
								if (assignmentType == 3)
								{
									try
									{
										// numeric cell type?
										String grade = submission.getGradeDisplay();
										Float.parseFloat(grade);
			
										// remove the String-based cell first
										cell = row.getCell(cellNum);
										row.removeCell(cell);
										// add number based cell
										cell=row.createCell(cellNum);
										cell.setCellType(0);
										cell.setCellValue(Float.parseFloat(grade));
			
										style = wb.createCellStyle();
										style.setDataFormat(wb.createDataFormat().getFormat("#,##0.0"));
										cell.setCellStyle(style);
									}
									catch (Exception e)
									{
										// if the grade is not numeric, let's make it as String type
										row.removeCell(cell);
										cell=row.createCell(cellNum);
										cell.setCellType(1);
										cell.setCellValue(submission.getGrade());
									}
								}
								else
								{
									// String cell type
									cell = row.getCell(cellNum);
									cell.setCellValue(submission.getGrade());
								}
							}
							else
							{
								// no grade available yet
								cell = row.getCell(cellNum);
								cell.setCellValue("");
							}
						} // if
					}
				}
				
				index++;
				
			}
			
			// output
			Blob b = new Blob();
			try
			{
				wb.write(b.outputStream());
			}
			catch (IOException e)
			{
				M_log.warn(this + " getGradesSpreadsheet Can not output the grade spread sheet for reference= " + ref);
			}
			
			return b.getBytes();
		}

	} // getGradesSpreadsheet
	
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
		boolean withFeedbackText = false;
		boolean withFeedbackComment = false;
		boolean withFeedbackAttachment = false;
		
		if (queryString != null)
		{
				
			// check against the content elements selection
			if (queryString.contains("studentSubmissionText"))
			{
				// should contain student submission text information
				withStudentSubmissionText = true;
			}
			if (queryString.contains("studentSubmissionAttachment"))
			{
				// should contain student submission attachment information
				withStudentSubmissionAttachment = true;
			}
			if (queryString.contains("gradeFile"))
			{
				// should contain grade file
				withGradeFile = true;	
			}
			if (queryString.contains("feedbackTexts"))
			{
				// inline text
				withFeedbackText = true;
			}
			if (queryString.contains("feedbackComments"))
			{
				// comments  should be available
				withFeedbackComment = true;
			}
			if (queryString.contains("feedbackAttachments"))
			{
				// feedback attachment
				withFeedbackAttachment = true;
			}
		}

		byte[] rv = null;

		try
		{
			Assignment a = getAssignment(assignmentReferenceFromSubmissionsZipReference(ref));
			String contextString = a.getContext();
			String groupReference = groupReferenceFromSubmissionsZipReference(ref);
			List allSubmissions = getSubmissions(a);
			List submissions = new Vector();
			
			// group or site
			String authzGroupId = "";
			if (groupReference == null)
			{
				// view all groups
				if (allowAllGroups(contextString))
				{
					// if have site level control
					submissions = allSubmissions;
				}
				else
				{
					// iterate through all allowed-grade-group
					Collection gCollection = getGroupsAllowGradeAssignment(contextString, a.getReference());
					// prevent multiple entries
					HashSet userIdSet = new HashSet();
					for (Iterator iGCollection = gCollection.iterator(); iGCollection.hasNext();)
					{
						Group g = (Group) iGCollection.next();
						String gReference = g.getReference();
						try
						{
							AuthzGroup group = AuthzGroupService.getAuthzGroup(gReference);
							Set grants = group.getUsers();
							for (int i = 0; i<allSubmissions.size();i++)
							{
								// see if the submitters is in the group
								AssignmentSubmission s = (AssignmentSubmission) allSubmissions.get(i);
								String submitterId = s.getSubmitterIdString();
								if (!userIdSet.contains(submitterId) && grants.contains(submitterId))
								{
									submissions.add(s);
									userIdSet.add(submitterId);
								}
							}
						}
						catch (Exception ee)
						{
							M_log.info(this + " getSubmissionsZip " + ee.getMessage() + " group reference=" + gReference);
						}
					}
					
				}
			}
			else
			{
				// just one group
				try
				{
					AuthzGroup group = AuthzGroupService.getAuthzGroup(groupReference);
					Set grants = group.getUsers();
					for (int i = 0; i<allSubmissions.size();i++)
					{
						// see if the submitters is in the group
						AssignmentSubmission s = (AssignmentSubmission) allSubmissions.get(i);
						if (grants.contains(s.getSubmitterIdString()))
						{
							submissions.add(s);
						}
					}
				}
				catch (Exception ee)
				{
					M_log.info(this +  " getSubmissionsZip " + ee.getMessage() + " group reference=" + groupReference);
				}
				
			}

			StringBuilder exceptionMessage = new StringBuilder();

			if (allowGradeSubmission(a.getReference()))
			{
				zipSubmissions(a.getReference(), a.getTitle(), a.getContent().getTypeOfGradeString(a.getContent().getTypeOfGrade()), a.getContent().getTypeOfSubmission(), submissions.iterator(), out, exceptionMessage, withStudentSubmissionText, withStudentSubmissionAttachment, withGradeFile, withFeedbackText, withFeedbackComment, withFeedbackAttachment);

				if (exceptionMessage.length() > 0)
				{
					// log any error messages
					
						M_log.warn(this + " getSubmissionsZip ref=" + ref + exceptionMessage.toString());
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
			M_log.warn(this + " getSubmissionsZip -PermissionException Not permitted to get assignment " + ref);
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), SECURE_ACCESS_ASSIGNMENT, ref);
		}

	} // getSubmissionsZip

	protected void zipSubmissions(String assignmentReference, String assignmentTitle, String gradeTypeString, int typeOfSubmission, Iterator submissions, OutputStream outputStream, StringBuilder exceptionMessage, boolean withStudentSubmissionText, boolean withStudentSubmissionAttachment, boolean withGradeFile, boolean withFeedbackText, boolean withFeedbackComment, boolean withFeedbackAttachment) 
	{
	    ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(outputStream);

			// create the folder structor - named after the assignment's title
			String root = Validator.escapeZipEntry(assignmentTitle) + Entity.SEPARATOR;

			String submittedText = "";
			if (!submissions.hasNext())
			{
				exceptionMessage.append("There is no submission yet. ");
			}
			
			// the buffer used to store grade information
			StringBuilder gradesBuffer = new StringBuilder(assignmentTitle + "," + gradeTypeString + "\n\n");
			gradesBuffer.append(rb.getString("grades.id") + "," + rb.getString("grades.eid") + "," + rb.getString("grades.lastname") + "," + rb.getString("grades.firstname") + "," + rb.getString("grades.grade") + "\n");

			// allow add assignment members
			List allowAddSubmissionUsers = allowAddSubmissionUsers(assignmentReference);
			
			// Create the ZIP file
			String submittersName = "";
			int count = 1;
			while (submissions.hasNext())
			{
				AssignmentSubmission s = (AssignmentSubmission) submissions.next();
				
				if (s.getSubmitted())
				{
					// get the submission user id and see if the user is still in site
					String userId = (String) s.getSubmitterIds().get(0);
					try
					{
						User u = UserDirectoryService.getUser(userId);
						if (allowAddSubmissionUsers.contains(u))
						{
							count = 1;
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
								submittersString = submittersString + "(" + submitters[i].getEid() + ")";
								gradesBuffer.append(submitters[i].getDisplayId() + "," + submitters[i].getEid() + "," + fullName + "," + s.getGradeDisplay() + "\n");
							}
							
							if (StringUtil.trimToNull(submittersString) != null)
							{
								submittersName = submittersName.concat(StringUtil.trimToNull(submittersString));
								submittedText = s.getSubmittedText();
		
								boolean added = false;
								while (!added)
								{
									try
									{
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
												ZipEntry textEntry = new ZipEntry(submittersName + submittersString + "_submissionText" + ZIP_SUBMITTED_TEXT_FILE_TYPE);
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
												String sSubAttachmentFolder = submittersName + rb.getString("download.submission.attachment") + "/";
												ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
												out.putNextEntry(sSubAttachmentFolderEntry);
												out.closeEntry();
												// add all submission attachment into the submission attachment folder
												zipAttachments(out, submittersName, sSubAttachmentFolder, s.getSubmittedAttachments());
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
											out.closeEntry();
											// add all feedback attachment folder
											zipAttachments(out, submittersName, feedbackSubAttachmentFolder, s.getFeedbackAttachments());
										}
		
										added = true;
									}
									catch (IOException e)
									{
										exceptionMessage.append("Can not establish the IO to create zip file for user "
												+ submittersName);
										M_log.warn(this + " zipSubmissions --IOException unable to create the zip file for user"
												+ submittersName);
										submittersName = submittersName.substring(0, submittersName.length() - 1) + "_" + count++;
									}
								}	//while
							} // if
						}
					}
					catch (Exception e)
					{
						M_log.warn(this + " zipSubmissions " + e.getMessage() + " userId = " + userId);
					}
				} // if the user is still in site

			} // while -- there is submission

			if (withGradeFile)
			{
				// create a grades.csv file into zip
				ZipEntry gradesCSVEntry = new ZipEntry(root + "grades.csv");
				out.putNextEntry(gradesCSVEntry);
				byte[] grades = gradesBuffer.toString().getBytes();
				out.write(grades);
				gradesCSVEntry.setSize(grades.length);
				out.closeEntry();
			}
		}
		catch (IOException e)
		{
			exceptionMessage.append("Can not establish the IO to create zip file. ");
			M_log.warn(this + " zipSubmissions IOException unable to create the zip file for assignment "
					+ assignmentTitle);
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



	private void zipAttachments(ZipOutputStream out, String submittersName, String sSubAttachmentFolder, List attachments) {
		int attachedUrlCount = 0;
		for (int j = 0; j < attachments.size(); j++)
		{
			Reference r = (Reference) attachments.get(j);
			try
			{
				ContentResource resource = m_contentHostingService.getResource(r.getId());

				String contentType = resource.getContentType();
				
				ResourceProperties props = r.getProperties();
				String displayName = props.getPropertyFormatted(props.getNamePropDisplayName());

				// for URL content type, encode a redirect to the body URL
				if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL))
				{
					displayName = "attached_URL_" + attachedUrlCount;
					attachedUrlCount++;
				}

				// buffered stream input
				InputStream content = resource.streamContent();
				byte data[] = new byte[1024 * 10];
				BufferedInputStream bContent = new BufferedInputStream(content, data.length);
				
				ZipEntry attachmentEntry = new ZipEntry(sSubAttachmentFolder + displayName);
				out.putNextEntry(attachmentEntry);
				int bCount = -1;
				while ((bCount = bContent.read(data, 0, data.length)) != -1) 
				{
					out.write(data, 0, bCount);
				}
				out.closeEntry();
				content.close();
			}
			catch (PermissionException e)
			{
				M_log.warn(this + " zipAttachments--PermissionException submittersName="
						+ submittersName + " attachment reference=" + r);
			}
			catch (IdUnusedException e)
			{
				M_log.warn(this + " zipAttachments--IdUnusedException submittersName="
						+ submittersName + " attachment reference=" + r);
			}
			catch (TypeException e)
			{
				M_log.warn(this + " zipAttachments--TypeException: submittersName="
						+ submittersName + " attachment reference=" + r);
			}
			catch (IOException e)
			{
				M_log.warn(this + " zipAttachments--IOException: Problem in creating the attachment file: submittersName="
								+ submittersName + " attachment reference=" + r);
			}
			catch (ServerOverloadException e)
			{
				M_log.warn(this + " zipAttachments--ServerOverloadException: submittersName="
						+ submittersName + " attachment reference=" + r);
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
							        	M_log.warn(this + ": handleAccess 1 " + ignore.getMessage());
							        }
							    }
							}
						}
	
						else if (REF_TYPE_GRADES.equals(ref.getSubType()))
						{
							// get the grades spreadsheet blob
							byte[] spreadsheet = getGradesSpreadsheet(ref.getReference());
	
							if (spreadsheet != null)
							{
								res.setContentType("application/vnd.ms-excel");
								res.setHeader("Content-Disposition", "attachment; filename = export_grades_file.xls");
	
								OutputStream out = null;
								try
								{
									out = res.getOutputStream();
									out.write(spreadsheet);
									out.flush();
									out.close();
								}
								catch (Throwable ignore)
								{
									M_log.warn(this + ": handleAccess 2 " + ignore.getMessage());
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
											M_log.warn(this + ": handleAccess 3 " + ignore.getMessage());
										}
									}
								}
							}
						}
						else
						{
							 M_log.warn(this + "handleAccess: throw IdUnusedException " + ref.getReference());
							throw new IdUnusedException(ref.getReference());
						}
					}
					catch (Throwable t)
					{
						 M_log.warn(this + " HandleAccess: caught exception " + t.toString() + " and rethrow it!");
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
				M_log.warn(this + "getEntity(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn(this + "getEntity(): " + e + " ref=" + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + "getEntity(): " + e + " ref=" + ref.getReference());
		}
		catch (NullPointerException e)
		{
			M_log.warn(this + "getEntity(): " + e + " ref=" + ref.getReference());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		Collection rv = new Vector();

		// for AssignmentService assignments:
		// if access set to SITE, use the assignment and site authzGroups.
		// if access set to GROUPED, use the assignment, and the groups, but not the site authzGroups.
		// if the user has SECURE_ALL_GROUPS in the context, ignore GROUPED access and treat as if SITE

		try
		{
			// for assignment
			if (REF_TYPE_ASSIGNMENT.equals(ref.getSubType()))
			{
				// assignment
				rv.add(ref.getReference());
				
				boolean grouped = false;
				Collection groups = null;

				// check SECURE_ALL_GROUPS - if not, check if the assignment has groups or not
				// TODO: the last param needs to be a ContextService.getRef(ref.getContext())... or a ref.getContextAuthzGroup() -ggolden
				if ((userId == null) || ((!SecurityService.isSuperUser(userId)) && (!AuthzGroupService.isAllowed(userId, SECURE_ALL_GROUPS, SiteService.siteReference(ref.getContext())))))
				{
					// get the channel to get the message to get group information
					// TODO: check for efficiency, cache and thread local caching usage -ggolden
					if (ref.getId() != null)
					{
						Assignment a = findAssignment(ref.getReference());
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
				
				// for content and submission, use site security setting
				ref.addSiteContextAuthzGroup(rv);
			}
		}
		catch (Throwable e)
		{
			M_log.warn(this + " getEntityAuthzGroups(): " + e.getMessage() + " ref=" + ref.getReference());
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
				M_log.warn(this + " getEntityUrl(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn(this + "getEntityUrl(): " + e + " ref=" + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + "getEntityUrl(): " + e + " ref=" + ref.getReference());
		}
		catch (NullPointerException e)
		{
			M_log.warn(this + "getEntityUrl(): " + e + " ref=" + ref.getReference());
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
							Xml.encodeAttribute(element4, "CHEF:creator", newModifierId);
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
			M_log.warn(this + " merge(): exception: " + any.getMessage() + " siteId=" + siteId + " from site id=" + fromSiteId);
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
	public void transferCopyEntities(String fromContext, String toContext, List resourceIds)
	{
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
							nContent.setIndividuallyGraded(oContent.individuallyGraded());
							nContent.setInstructions(oContent.getInstructions());
							nContent.setMaxGradePoint(oContent.getMaxGradePoint());
							nContent.setReleaseGrades(oContent.releaseGrades());
							nContent.setTimeLastModified(oContent.getTimeLastModified());
							nContent.setTitle(oContent.getTitle());
							nContent.setTypeOfGrade(oContent.getTypeOfGrade());
							nContent.setTypeOfSubmission(oContent.getTypeOfSubmission());
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
						if (M_log.isWarnEnabled()) M_log.warn(this + " transferCopyEntities " + e.toString()  + " oAssignmentId=" + oAssignmentId);
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
							
							nAssignment.setDropDeadTime(oAssignment.getDropDeadTime());
							nAssignment.setDueTime(oAssignment.getDueTime());
							nAssignment.setOpenTime(oAssignment.getOpenTime());
							nAssignment.setSection(oAssignment.getSection());
							nAssignment.setTitle(oAssignment.getTitle());
							nAssignment.setPosition_order(oAssignment.getPosition_order());
							// properties
							ResourcePropertiesEdit p = nAssignment.getPropertiesEdit();
							p.clear();
							p.addAll(oAssignment.getProperties());
							
							// one more touch on the gradebook-integration link
							if (StringUtil.trimToNull(p.getProperty(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT)) != null)
							{
								// assignments are imported as drafts;
								// mark the integration with "add" for now, later when user posts the assignment, the corresponding assignment will be created in gradebook.
								p.removeProperty(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
								p.addProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ADD);
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
			} // if
		} // for
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
					M_log.warn(this + " transferCopyEntities: cannot add new attachment with id=" + nAttachmentId + " " + eeAny.getMessage());
				}
			}
			catch (Exception eAny)
			{
				// if cannot find the original attachment, do nothing.
				M_log.warn(this + " transferCopyEntities: cannot find the original attachment with id=" + oAttachmentId + " " + eAny.getMessage());
			}
		}
		catch (Exception any)
		{
			M_log.warn(this + " transferCopyEntities" + any.getMessage() + " oAttachmentId=" + oAttachmentId + " nAttachmentId=" + nAttachmentId);
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
				M_log.warn(this + " getEntityDescription(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn(this + " getEntityDescription(): " + e.getMessage() + " ref=" + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + " getEntityDescription(): " + e.getMessage() + " ref=" + ref.getReference());
		}
		catch (NullPointerException e)
		{
			M_log.warn(this + " getEntityDescription(): " + e.getMessage() + " ref=" + ref.getReference());
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
				M_log.warn(this + " getEntityResourceProperties: unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn(this + " getEntityResourceProperties(): " + e.getMessage() + " ref=" + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + " getEntityResourceProperties(): " + e.getMessage() + " ref=" + ref.getReference());
		}
		catch (NullPointerException e)
		{
			M_log.warn(this + " getEntityResourceProperties(): " + e.getMessage() + " ref=" + ref.getReference());
		}

		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean canSubmit(String context, Assignment a)
	{
		// return false if not allowed to submit at all
		if (!allowAddSubmission(context)) return false;
		
		String userId = SessionManager.getCurrentSessionUserId();
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
			if (submission != null)
			{
				closeTime = submission.getCloseTime();
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
				{
					// return true if resubmission is allowed and current time is before resubmission close time
					// get the resubmit settings from submission object first
					int allowResubmitNumber = submission.getResubmissionNum();
					Time resubmitCloseTime = null;
					if (allowResubmitNumber != 0)
					{
						ResourceProperties submissionProperties = submission.getProperties();
						String property = (String) submissionProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
						resubmitCloseTime = submissionProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME) != null? TimeService.newTime(submissionProperties.getLongProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME)):null;
					}
					else
					{
						// if no setting on the submission object level, get it from the assignment level next
						ResourceProperties assignmentProperties = a.getProperties();
						try
						{
							allowResubmitNumber = assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null? Integer.parseInt((String) assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER)):0;
							if (allowResubmitNumber != 0)
							{
								resubmitCloseTime = assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME) != null? assignmentProperties.getTimeProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME):a.getCloseTime();
							}
						}
						catch (Exception e)
						{
							M_log.warn(this + "canSubmit: exception of get integer value for resubmit number: assignment id = " + a.getId() + " submission id = " + submission.getId() + " assignment resubmission number = " + assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) + e.getMessage());
						}
					}
					return allowResubmitNumber != 0 && resubmitCloseTime != null && currentTime.before(resubmitCloseTime);
				}
			}
		}
		catch (UserNotDefinedException e)
		{
			// cannot find user
			M_log.warn(this + " canSubmit(String, Assignment) " + e.getMessage() + " assignment ref=" + a.getReference());
			return false;
		} catch (EntityPropertyNotDefinedException e) {
			// Property not defined
			M_log.warn(this + " canSubmit(String, Assignment) " + e.getMessage() + " assignment ref=" + a.getReference());
			return false;
		} catch (EntityPropertyTypeException e) {
			// entity property type exception
			M_log.warn(this + " canSubmit(String, Assignment) " + e.getMessage() + " assignment ref=" + a.getReference());
			return false;
		}
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

		protected Time m_openTime;

		protected Time m_dueTime;

		protected Time m_closeTime;

		protected Time m_dropDeadTime;

		protected List m_authors;

		protected boolean m_draft;
		
		protected int m_position_order;

		/** The Collection of groups (authorization group id strings). */
		protected Collection m_groups = new Vector();

		/** The assignment access. */
		protected AssignmentAccess m_access = AssignmentAccess.SITE;

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
			m_authors = new Vector();
			m_draft = true;
			m_groups = new Vector();
			m_position_order = 0;
		}

		/**
		 * Reads the Assignment's attribute values from xml.
		 * 
		 * @param s -
		 *        Data structure holding the xml info.
		 */
		public BaseAssignment(Element el)
		{
			M_log.debug(this + " BASE ASSIGNMENT : ENTERING STORAGE CONSTRUCTOR");

			m_properties = new BaseResourcePropertiesEdit();

			int numAttributes = 0;
			String intString = null;
			String attributeString = null;
			String tempString = null;

			m_id = el.getAttribute("id");
			
				M_log.debug(this + " BASE ASSIGNMENT : STORAGE CONSTRUCTOR : ASSIGNMENT ID : " + m_id);
			m_title = el.getAttribute("title");
			m_section = el.getAttribute("section");
			m_draft = getBool(el.getAttribute("draft"));
			
				M_log.debug(this + " BASE ASSIGNMENT : STORAGE CONSTRUCTOR : READ THROUGH REG ATTS");

			m_assignmentContent = el.getAttribute("assignmentcontent");
			
				M_log.debug(this + " BASE ASSIGNMENT : STORAGE CONSTRUCTOR : CONTENT ID : "
						+ m_assignmentContent);

			m_openTime = getTimeObject(el.getAttribute("opendate"));
			m_dueTime = getTimeObject(el.getAttribute("duedate"));
			m_dropDeadTime = getTimeObject(el.getAttribute("dropdeaddate"));
			m_closeTime = getTimeObject(el.getAttribute("closedate"));
			m_context = el.getAttribute("context");
			m_position_order = 0; // prevents null pointer if there is no position_order defined as well as helps with the sorting
			try
			{
				m_position_order = new Long(el.getAttribute("position_order")).intValue();
			}
			catch (Exception e)
			{
				M_log.warn(this + ": BaseAssignment(Element) " + e.getMessage());
			}

			// READ THE AUTHORS
			m_authors = new Vector();
			intString = el.getAttribute("numberofauthors");
			
				M_log.debug(this + " BASE ASSIGNMENT : STORAGE CONSTRUCTOR : number of authors : " + intString);
			try
			{
				numAttributes = Integer.parseInt(intString);

				for (int x = 0; x < numAttributes; x++)
				{
					
						M_log.debug(this + " BASE ASSIGNMENT : STORAGE CONSTRUCTOR : reading author # " + x);
					attributeString = "author" + x;
					tempString = el.getAttribute(attributeString);

					if (tempString != null)
					{
						
							M_log.debug(this + " BASE ASSIGNMENT : STORAGE CONSTRUCTOR : adding author # " + x
									+ " id :  " + tempString);
						m_authors.add(tempString);
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(this + " BASE ASSIGNMENT : STORAGE CONSTRUCTOR : Exception reading authors : " + e);
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

			M_log.debug(this + " BASE ASSIGNMENT : LEAVING STORAGE CONSTRUCTOR");

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
							
								M_log.debug(this + " getContentHandler: READ THROUGH REG ATTS");

							m_assignmentContent = attributes.getValue("assignmentcontent");
							
								M_log.debug(this + " getContentHandler: STORAGE CONSTRUCTOR : CONTENT ID : "
										+ m_assignmentContent);

							m_openTime = getTimeObject(attributes.getValue("opendate"));
							m_dueTime = getTimeObject(attributes.getValue("duedate"));
							m_dropDeadTime = getTimeObject(attributes.getValue("dropdeaddate"));
							m_closeTime = getTimeObject(attributes.getValue("closedate"));
							m_context = attributes.getValue("context");
							m_position_order = 0; // prevents null pointer if there is no position_order defined as well as helps with the sorting
							try
							{
								m_position_order = new Long(attributes.getValue("position_order")).intValue();
							}
							catch (Exception e)
							{
								M_log.warn(this + ":getContentHandler:DefaultEntityHandler Long data parse problem " + attributes.getValue("position_order") + e.getMessage());
							}

							// READ THE AUTHORS
							m_authors = new Vector();
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
								M_log.warn(this + " BASE ASSIGNMENT getContentHandler startElement : Exception reading authors : " + e.toString());
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
			assignment.setAttribute("opendate", getTimeString(m_openTime));
			assignment.setAttribute("duedate", getTimeString(m_dueTime));
			assignment.setAttribute("dropdeaddate", getTimeString(m_dropDeadTime));
			assignment.setAttribute("closedate", getTimeString(m_closeTime));
			assignment.setAttribute("position_order", new Long(m_position_order).toString().trim());

			
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
				m_closeTime = assignment.getCloseTime();
				m_dropDeadTime = assignment.getDropDeadTime();
				m_draft = assignment.getDraft();
				m_position_order = 0;
				try
				{
					m_position_order = assignment.getPosition_order();
				}
				catch (Exception e)
				{
					M_log.warn(this + ": setAll(Assignment) get position order " + e.getMessage());
				}
				m_properties = new BaseResourcePropertiesEdit();
				m_properties.addAll(assignment.getProperties());
				m_groups = assignment.getGroups();
				m_access = assignment.getAccess();
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
				return rb.getString("gen.due1");
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
				M_log.warn(this + ":getTimeCreated() no time property defined " + e.getMessage());
			}
			catch (EntityPropertyTypeException e)
			{
				M_log.warn(this + ":getTimeCreated() no time property defined " + e.getMessage());
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
				M_log.warn(this + ":getTimeLastModified() no time property defined " + e.getMessage());
			}
			catch (EntityPropertyTypeException e)
			{
				M_log.warn(this + ":getTimeLastModified() no time property defined " + e.getMessage());
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
					M_log.warn(this + ":getContent() " + e.getMessage());
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
		* @inheritDoc
		*/
		public String getDueTimeString()
		{
			if ( m_dueTime == null )
				return "";
			else
				return m_dueTime.toStringLocalFull();
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
			return new Vector(m_groups);
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
				M_log.warn(this + " setGroupAccess() called with null context: " + getReference());
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), "access:site", getReference());
			}

			// isolate any groups that would be removed or added
			Collection addedGroups = new Vector();
			Collection removedGroups = new Vector();
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
				M_log.warn(this + " clearGroupAccess() called with null context. " + getReference());
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), "access:site", getReference());
			}
			else
			{
				// verify that the user has permission to add in the site context
				if (!allowAddSiteAssignment(m_context))
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

		protected boolean m_groupProject;

		protected boolean m_individuallyGraded;

		protected boolean m_releaseGrades;

		
		
		protected boolean m_allowAttachments;
		
		protected boolean m_allowReviewService;
		
		protected boolean m_allowStudentViewReport;

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
			m_authors = new Vector();
			m_attachments = m_entityManager.newReferenceList();
			m_title = "";
			m_instructions = "";
			m_honorPledge = Assignment.HONOR_PLEDGE_NOT_SET;
			m_typeOfSubmission = Assignment.ASSIGNMENT_SUBMISSION_TYPE_NOT_SET;
			m_typeOfGrade = Assignment.GRADE_TYPE_NOT_SET;
			m_maxGradePoint = 0;
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
			M_log.debug(this + " BaseAssignmentContent : Entering read");

			m_id = el.getAttribute("id");
			m_context = el.getAttribute("context");
			m_title = el.getAttribute("title");
			m_groupProject = getBool(el.getAttribute("groupproject"));
			m_individuallyGraded = getBool(el.getAttribute("indivgraded"));
			m_releaseGrades = getBool(el.getAttribute("releasegrades"));
			m_allowAttachments = getBool(el.getAttribute("allowattach"));
			m_allowReviewService = getBool(el.getAttribute("allowreview"));
			m_allowStudentViewReport = getBool(el.getAttribute("allowstudentview"));
			
			m_timeCreated = getTimeObject(el.getAttribute("datecreated"));
			m_timeLastModified = getTimeObject(el.getAttribute("lastmod"));

			m_instructions = FormattedText.decodeFormattedTextAttribute(el, "instructions");

			try
			{
				m_honorPledge = Integer.parseInt(el.getAttribute("honorpledge"));
			}
			catch (Exception e)
			{
				M_log.warn(this + " BaseAssignmentContent Exception parsing honor pledge int from xml file string : " + e);
			}

			try
			{
				m_typeOfSubmission = Integer.parseInt(el.getAttribute("submissiontype"));
			}
			catch (Exception e)
			{
				M_log.warn(this + " BaseAssignmentContent Exception parsing submission type int from xml file string : " + e);
			}

			try
			{
				m_typeOfGrade = Integer.parseInt(el.getAttribute("typeofgrade"));
			}
			catch (Exception e)
			{
				M_log.warn(this + " BaseAssignmentContent Exception parsing grade type int from xml file string : " + e);
			}

			try
			{
				// %%%zqian
				// read the scaled max grade point first; if there is none, get the old max grade value and multiple by 10
				String maxGradePoint = StringUtil.trimToNull(el.getAttribute("scaled_maxgradepoint"));
				if (maxGradePoint == null)
				{
					maxGradePoint = StringUtil.trimToNull(el.getAttribute("maxgradepoint"));
					if (maxGradePoint != null)
					{
						maxGradePoint = maxGradePoint + "0";
					}
				}
				m_maxGradePoint = Integer.parseInt(maxGradePoint);
			}
			catch (Exception e)
			{
				M_log.warn(this + " BaseAssignmentContent Exception parsing maxgradepoint int from xml file string : " + e);
			}

			// READ THE AUTHORS
			m_authors = new Vector();
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
				M_log.warn(this + " BaseAssignmentContent: Exception reading authors : " + e);
			}

			// READ THE ATTACHMENTS
			m_attachments = m_entityManager.newReferenceList();
			M_log.debug(this + " BaseAssignmentContent: Reading attachments : ");
			intString = el.getAttribute("numberofattachments");
			M_log.debug(this + " BaseAssignmentContent: num attachments : " + intString);
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
						M_log.debug(this + " BaseAssignmentContent: " + attributeString + " : " + tempString);
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(this + " BaseAssignmentContent: Exception reading attachments : " + e);
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
						
							M_log.debug(this + " BaseAssignmentContent(Element): instructions : " + m_instructions);
					}
					if (m_instructions == null)
					{
						m_instructions = "";
					}
				}
			}

			M_log.debug(this + " BaseAssignmentContent(Element): LEAVING STORAGE CONSTRUTOR");

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
							m_allowReviewService = getBool(attributes.getValue("allowreview"));
							m_allowStudentViewReport = getBool(attributes.getValue("allowstudentview"));
							
							m_timeCreated = getTimeObject(attributes.getValue("datecreated"));
							m_timeLastModified = getTimeObject(attributes.getValue("lastmod"));

							m_instructions = FormattedTextDecodeFormattedTextAttribute(attributes, "instructions");

							try
							{
								m_honorPledge = Integer.parseInt(attributes.getValue("honorpledge"));
							}
							catch (Exception e)
							{
								M_log.warn(this + " getContentHandler startElement Exception parsing honor pledge int from xml file string : " + e);
							}

							try
							{
								m_typeOfSubmission = Integer.parseInt(attributes.getValue("submissiontype"));
							}
							catch (Exception e)
							{
								M_log.warn(this + " getContentHandler startElement Exception parsing submission type int from xml file string : " + e);
							}

							try
							{
								m_typeOfGrade = Integer.parseInt(attributes.getValue("typeofgrade"));
							}
							catch (Exception e)
							{
								M_log.warn(this + " getContentHandler startElement Exception parsing grade type int from xml file string : " + e);
							}

							try
							{
								// %%%zqian
								// read the scaled max grade point first; if there is none, get the old max grade value and multiple by 10
								String maxGradePoint = StringUtil.trimToNull(attributes.getValue("scaled_maxgradepoint"));
								if (maxGradePoint == null)
								{
									maxGradePoint = StringUtil.trimToNull(attributes.getValue("maxgradepoint"));
									if (maxGradePoint != null)
									{
										maxGradePoint = maxGradePoint + "0";
									}
								}
								m_maxGradePoint = Integer.parseInt(maxGradePoint);
							}
							catch (Exception e)
							{
								M_log.warn(this + " getContentHandler startElement Exception parsing maxgradepoint int from xml file string : " + e);
							}

							// READ THE AUTHORS
							m_authors = new Vector();
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
								M_log.warn(this + " getContentHandler startElement Exception reading authors : " + e);
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
								M_log.warn(this + " getContentHandler startElement DbCachedContent : Exception reading attachments : " + e);
							}
							
							entity = thisEntity;
						}
						else
						{
							M_log.warn(this + " getContentHandler startElement Unexpected Element " + qName);
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
		
			content.setAttribute("allowreview", getBoolString(m_allowReviewService));
			content.setAttribute("allowstudentview", getBoolString(m_allowStudentViewReport));
			
			content.setAttribute("honorpledge", String.valueOf(m_honorPledge));
			content.setAttribute("submissiontype", String.valueOf(m_typeOfSubmission));
			content.setAttribute("typeofgrade", String.valueOf(m_typeOfGrade));
			content.setAttribute("scaled_maxgradepoint", String.valueOf(m_maxGradePoint));
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
				m_groupProject = content.getGroupProject();
				m_individuallyGraded = content.individuallyGraded();
				m_releaseGrades = content.releaseGrades();
				m_allowAttachments = content.getAllowAttachments();
				//Uct
				m_allowReviewService = content.getAllowReviewService();
				m_allowStudentViewReport = content.getAllowStudentViewReport();
				
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
		 * 
		 * @param gradeType -
		 *        The integer representing the type of grade.
		 * @return Description of the type of grade.
		 */
		public String getTypeOfGradeString(int type)
		{
			String retVal = null;

			switch (type)
			{
				case 1:
					retVal = rb.getString("ungra");
					break;

				case 2:
					retVal = rb.getString("letter");
					break;

				case 3:
					retVal = rb.getString("points");
					break;

				case 4:
					retVal = rb.getString("pass");
					break;

				case 5:
					retVal = rb.getString("check");
					break;

				default:
					retVal = "Unknown Grade Type";
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

		/**
		 * Get the maximum grade for grade type = SCORE_GRADE_TYPE(3) Formated to show one decimal place
		 * 
		 * @return The maximum grade score.
		 */
		public String getMaxGradePointDisplay()
		{
			// formated to show one decimal place, for example, 1000 to 100.0
			String one_decimal_maxGradePoint = m_maxGradePoint / 10 + "." + (m_maxGradePoint % 10);
			return one_decimal_maxGradePoint;
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

		protected List m_submitters;

		protected Time m_timeSubmitted;

		protected Time m_timeReturned;

		protected Time m_timeLastModified;

		protected List m_submittedAttachments;

		protected List m_feedbackAttachments;

		protected String m_submittedText;

		protected String m_feedbackComment;

		protected String m_feedbackText;

		protected String m_grade;

		protected boolean m_submitted;

		protected boolean m_returned;

		protected boolean m_graded;

		protected boolean m_gradeReleased;

		protected boolean m_honorPledgeFlag;

		
		//The score given by the review service
		protected Integer m_reviewScore;
		// The report given by the content review service
		protected String m_reviewReport;
		// The status of the review service
		protected String m_reviewStatus;
		
		protected String m_reviewIconUrl;
		
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
				if (m_reviewScore != null && m_reviewScore > 0) {
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
					int score = contentReviewService.getReviewScore(contentId);
					m_reviewScore = score;
					M_log.debug(this + " getReviewScore CR returned a score of: " + score);
					return score;
						
				} 
				catch (QueueException cie) {
					//should we add the item
					try {
						
							M_log.debug(this + " getReviewScore Item is not in queue we will try add it");
							String contentId = cr.getId();
							String userId = (String)this.getSubmitterIds().get(0);
							try {
								contentReviewService.queueContent(userId, null, getAssignment().getReference(), contentId);
							}
							catch (QueueException qe) {
								M_log.warn(this + " getReviewScore Unable to queue content with content review Service: " + qe.getMessage());
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
					
					if (SecurityService.unlock(UserDirectoryService.getCurrentUser(), "asn.grade", "/site/" + this.m_context))
						return contentReviewService.getReviewReportInstructor(contentId);
					else
						return contentReviewService.getReviewReportStudent(contentId);
					
				} catch (Exception e) {
					//e.printStackTrace();
					M_log.warn(this + ":getReviewReport() " + e.getMessage());
					return "Error";
				}
					
			}
			
		}
		
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
				M_log.warn(this + ":getFirstAcceptableAttachment() " + e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
		
		public String getReviewStatus() {
			return m_reviewStatus;
		}
		
		public String getReviewIconUrl() {
			if (m_reviewIconUrl == null )
				m_reviewIconUrl = contentReviewService.getIconUrlforScore(new Long(this.getReviewScore()));
				
			return m_reviewIconUrl;
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
			
			m_id = id;
			m_assignment = assignId;
			m_properties = new BaseResourcePropertiesEdit();
			addLiveProperties(m_properties);
			m_submitters = new Vector();
			m_feedbackAttachments = m_entityManager.newReferenceList();
			m_submittedAttachments = m_entityManager.newReferenceList();
			m_submitted = false;
			m_returned = false;
			m_graded = false;
			m_gradeReleased = false;
			m_submittedText = "";
			m_feedbackComment = "";
			m_feedbackText = "";
			m_grade = "";
			m_timeLastModified = TimeService.newTime();

			if (submitterId == null)
			{
				String currentUser = SessionManager.getCurrentSessionUserId();
				if (currentUser == null) currentUser = "";
				m_submitters.add(currentUser);
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
			int numAttributes = 0;
			String intString = null;
			String attributeString = null;
			String tempString = null;
			Reference tempReference = null;

			M_log.debug(this + " BaseAssigmentSubmission : ENTERING STORAGE CONSTRUCTOR");

			m_id = el.getAttribute("id");
			m_context = el.getAttribute("context");

			// %%%zqian
			// read the scaled grade point first; if there is none, get the old grade value
			String grade = StringUtil.trimToNull(el.getAttribute("scaled_grade"));
			if (grade == null)
			{
				grade = StringUtil.trimToNull(el.getAttribute("grade"));
				if (grade != null)
				{
					try
					{
						Integer.parseInt(grade);
						// for the grades in points, multiple those by 10
						grade = grade + "0";
					}
					catch (Exception e)
					{
						M_log.warn(this + ":BaseAssignmentSubmission(Element el) " + e.getMessage());
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
			m_gradeReleased = getBool(el.getAttribute("gradereleased"));
			m_honorPledgeFlag = getBool(el.getAttribute("pledgeflag"));

			m_submittedText = FormattedText.decodeFormattedTextAttribute(el, "submittedtext");
			m_feedbackComment = FormattedText.decodeFormattedTextAttribute(el, "feedbackcomment");
			m_feedbackText = FormattedText.decodeFormattedTextAttribute(el, "feedbacktext");

			// READ THE SUBMITTERS
			m_submitters = new Vector();
			M_log.debug(this + " BaseAssignmentSubmission : CONSTRUCTOR : Reading submitters : ");
			intString = el.getAttribute("numberofsubmitters");
			try
			{
				numAttributes = Integer.parseInt(intString);

				for (int x = 0; x < numAttributes; x++)
				{
					attributeString = "submitter" + x;
					tempString = el.getAttribute(attributeString);
					if (tempString != null) m_submitters.add(tempString);
				}
			}
			catch (Exception e)
			{
				M_log.warn(this + " BaseAssignmentSubmission: CONSTRUCTOR : Exception reading submitters : " + e);
			}

			// READ THE FEEDBACK ATTACHMENTS
			m_feedbackAttachments = m_entityManager.newReferenceList();
			intString = el.getAttribute("numberoffeedbackattachments");
			
				M_log.debug(this + " BaseAssignmentSubmission: CONSTRUCTOR : num feedback attachments : " + intString);
			try
			{
				numAttributes = Integer.parseInt(intString);

				for (int x = 0; x < numAttributes; x++)
				{
					attributeString = "feedbackattachment" + x;
					tempString = el.getAttribute(attributeString);
					if (tempString != null)
					{
						tempReference = m_entityManager.newReference(tempString);
						m_feedbackAttachments.add(tempReference);
						
							M_log.debug(this + " BaseAssignmentSubmission: CONSTRUCTOR : " + attributeString + " : "
									+ tempString);
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(this + " BaseAssignmentSubmission: CONSTRUCTOR : Exception reading feedback attachments : " + e);
			}

			// READ THE SUBMITTED ATTACHMENTS
			m_submittedAttachments = m_entityManager.newReferenceList();
			intString = el.getAttribute("numberofsubmittedattachments");
			
				M_log.debug(this + " BaseAssignmentSubmission: CONSTRUCTOR : num submitted attachments : " + intString);
			try
			{
				numAttributes = Integer.parseInt(intString);

				for (int x = 0; x < numAttributes; x++)
				{
					attributeString = "submittedattachment" + x;
					tempString = el.getAttribute(attributeString);
					if (tempString != null)
					{
						tempReference = m_entityManager.newReference(tempString);
						m_submittedAttachments.add(tempReference);
						
							M_log.debug(this + " BaseAssignmentSubmission: CONSTRUCTOR : " + attributeString + " : "
									+ tempString);
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(this + " BaseAssignmentSubmission: CONSTRUCTOR : Exception reading submitted attachments : " + e);
			}

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
						
							M_log.debug(this + " BaseAssignmentSubmission: CONSTRUCTOR : submittedtext : " + m_submittedText);
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
						
							M_log.debug(this + " BaseAssignmentSubmission: CONSTRUCTOR : feedbackcomment : "
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
						
							M_log.debug(this + " BaseAssignmentSubmission: CONSTRUCTOR : FEEDBACK TEXT : " + m_feedbackText);
					}
					if (m_feedbackText == null)
					{
						m_feedbackText = "";
					}
				}
			}

		
			
			try {
				if (el.getAttribute("reviewScore")!=null)
					m_reviewScore = Integer.parseInt(el.getAttribute("reviewScore"));
				else
					m_reviewScore = -1;
			}
			catch (NumberFormatException nfe) {
				m_reviewScore = -1;
				M_log.warn(this + ":BaseAssignmentSubmission(Element) " + nfe.getMessage());
			}
			try {
			// The report given by the content review service
				if (el.getAttribute("reviewReport")!=null)
					m_reviewReport = el.getAttribute("reviewReport");
				else 
					m_reviewReport = "no report available";
				
			// The status of the review service
				if (el.getAttribute("reviewStatus")!=null)
					m_reviewStatus = el.getAttribute("reviewStatus");
				else 
					m_reviewStatus = "";
			}
			catch (Exception e) {
				M_log.error("error constructing Submission: " + e);
			}
			
			//get the review Status from ContentReview rather than using old ones
			if (contentReviewService != null) {
				m_reviewStatus = this.getReviewStatus();
				m_reviewScore  = this.getReviewScore();
			}
			
			
			
			M_log.debug(this + " BaseAssignmentSubmission: LEAVING STORAGE CONSTRUCTOR");

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
						if ("submission".equals(qName) && entity == null)
						{
							try {
								if (attributes.getValue("reviewScore")!=null)
									m_reviewScore = Integer.parseInt(attributes.getValue("reviewScore"));
								else
									m_reviewScore = -1;
							}
							catch (NumberFormatException nfe) {
								m_reviewScore = -1;
								M_log.warn(this + ":AssignmentSubmission:getContentHandler:DefaultEntityHandler " + nfe.getMessage());
							}
							try {
							// The report given by the content review service
								if (attributes.getValue("reviewReport")!=null)
									m_reviewReport = attributes.getValue("reviewReport");
								else 
									m_reviewReport = "no report available";
								
							// The status of the review service
								if (attributes.getValue("reviewStatus")!=null)
									m_reviewStatus = attributes.getValue("reviewStatus");
								else 
									m_reviewStatus = "";
							}
							catch (Exception e) {
								M_log.error("error constructing Submission: " + e);
							}
							
							
							int numAttributes = 0;
							String intString = null;
							String attributeString = null;
							String tempString = null;
							Reference tempReference = null;

							m_id = attributes.getValue("id");
							// M_log.info(this + " BASE SUBMISSION : CONSTRUCTOR : m_id : " + m_id);
							m_context = attributes.getValue("context");
							// M_log.info(this + " BASE SUBMISSION : CONSTRUCTOR : m_context : " + m_context);

							// %%%zqian
							// read the scaled grade point first; if there is none, get the old grade value
							String grade = StringUtil.trimToNull(attributes.getValue("scaled_grade"));
							if (grade == null)
							{
								grade = StringUtil.trimToNull(attributes.getValue("grade"));
								if (grade != null)
								{
									try
									{
										Integer.parseInt(grade);
										// for the grades in points, multiple those by 10
										grade = grade + "0";
									}
									catch (Exception e)
									{
										M_log.warn(this + ":BaseAssignmentSubmission:getContentHanler:DefaultEnityHandler " + e.getMessage());
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
							m_gradeReleased = getBool(attributes.getValue("gradereleased"));
							m_honorPledgeFlag = getBool(attributes.getValue("pledgeflag"));

							m_submittedText = FormattedTextDecodeFormattedTextAttribute(attributes, "submittedtext");
							m_feedbackComment = FormattedTextDecodeFormattedTextAttribute(attributes, "feedbackcomment");
							m_feedbackText = FormattedTextDecodeFormattedTextAttribute(attributes, "feedbacktext");
							 

							// READ THE SUBMITTERS
							m_submitters = new Vector();
							intString = attributes.getValue("numberofsubmitters");
							try
							{
								numAttributes = Integer.parseInt(intString);

								for (int x = 0; x < numAttributes; x++)
								{
									attributeString = "submitter" + x;
									tempString = attributes.getValue(attributeString);
									if (tempString != null) m_submitters.add(tempString);
								}
							}
							catch (Exception e)
							{
								M_log.warn(this + " BaseAssignmentSubmission getContentHandler : Exception reading submitters : " + e);
							}

							// READ THE FEEDBACK ATTACHMENTS
							m_feedbackAttachments = m_entityManager.newReferenceList();
							intString = attributes.getValue("numberoffeedbackattachments");
							try
							{
								numAttributes = Integer.parseInt(intString);

								for (int x = 0; x < numAttributes; x++)
								{
									attributeString = "feedbackattachment" + x;
									tempString = attributes.getValue(attributeString);
									if (tempString != null)
									{
										tempReference = m_entityManager.newReference(tempString);
										m_feedbackAttachments.add(tempReference);
									}
								}
							}
							catch (Exception e)
							{
								M_log.warn(this + " BaseAssignmentSubmission getContentHandler : Exception reading feedback attachments : " + e);
							}

							// READ THE SUBMITTED ATTACHMENTS
							m_submittedAttachments = m_entityManager.newReferenceList();
							intString = attributes.getValue("numberofsubmittedattachments");
							try
							{
								numAttributes = Integer.parseInt(intString);

								for (int x = 0; x < numAttributes; x++)
								{
									attributeString = "submittedattachment" + x;
									tempString = attributes.getValue(attributeString);
									if (tempString != null)
									{
										tempReference = m_entityManager.newReference(tempString);
										m_submittedAttachments.add(tempReference);
									}
								}
							}
							catch (Exception e)
							{
								M_log.warn(this + " BaseAssignmentSubmission getContentHandler: Exception reading submitted attachments : " + e);
							}
							
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
			M_log.debug(this + " BaseAssignmentSubmission : ENTERING TOXML");

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
			submission.setAttribute("reviewScore",Integer.toString(m_reviewScore));
			submission.setAttribute("reviewReport",m_reviewReport == null ? "" : m_reviewReport);
			submission.setAttribute("reviewStatus",m_reviewStatus == null ? "" : m_reviewStatus);
			
			submission.setAttribute("id", m_id == null ? "" : m_id);
			submission.setAttribute("context", m_context == null ? "" : m_context);
			submission.setAttribute("scaled_grade", m_grade == null ? "" : m_grade);
			submission.setAttribute("assignment", m_assignment == null ? "" : m_assignment);

			submission.setAttribute("datesubmitted", getTimeString(m_timeSubmitted));
			submission.setAttribute("datereturned", getTimeString(m_timeReturned));
			submission.setAttribute("lastmod", getTimeString(m_timeLastModified));
			submission.setAttribute("submitted", getBoolString(m_submitted));
			submission.setAttribute("returned", getBoolString(m_returned));
			submission.setAttribute("graded", getBoolString(m_graded));
			submission.setAttribute("gradereleased", getBoolString(m_gradeReleased));
			submission.setAttribute("pledgeflag", getBoolString(m_honorPledgeFlag));

			M_log.debug(this + " BaseAssignmentSubmission: SAVED REGULAR PROPERTIES");

			// SAVE THE SUBMITTERS
			numItemsString = "" + m_submitters.size();
			submission.setAttribute("numberofsubmitters", numItemsString);
			for (int x = 0; x < m_submitters.size(); x++)
			{
				attributeString = "submitter" + x;
				itemString = (String) m_submitters.get(x);
				if (itemString != null) submission.setAttribute(attributeString, itemString);
			}

			M_log.debug(this + " BaseAssignmentSubmission: SAVED SUBMITTERS");

			// SAVE THE FEEDBACK ATTACHMENTS
			numItemsString = "" + m_feedbackAttachments.size();
			submission.setAttribute("numberoffeedbackattachments", numItemsString);
			
			M_log.debug("DB : DbCachedStorage : DbCachedAssignmentSubmission : entering fb attach loop : size : "
						+ numItemsString);
			for (int x = 0; x < m_feedbackAttachments.size(); x++)
			{
				attributeString = "feedbackattachment" + x;
				tempReference = (Reference) m_feedbackAttachments.get(x);
				itemString = tempReference.getReference();
				if (itemString != null) submission.setAttribute(attributeString, itemString);
			}

			M_log.debug(this + " BaseAssignmentSubmission: SAVED FEEDBACK ATTACHMENTS");

			// SAVE THE SUBMITTED ATTACHMENTS
			numItemsString = "" + m_submittedAttachments.size();
			submission.setAttribute("numberofsubmittedattachments", numItemsString);
			for (int x = 0; x < m_submittedAttachments.size(); x++)
			{
				attributeString = "submittedattachment" + x;
				tempReference = (Reference) m_submittedAttachments.get(x);
				itemString = tempReference.getReference();
				if (itemString != null) submission.setAttribute(attributeString, itemString);
			}

			M_log.debug(this + " BaseAssignmentSubmission: SAVED SUBMITTED ATTACHMENTS");

			// SAVE THE PROPERTIES
			m_properties.toXml(doc, stack);
			stack.pop();

			FormattedText.encodeFormattedTextAttribute(submission, "submittedtext", m_submittedText);
			FormattedText.encodeFormattedTextAttribute(submission, "feedbackcomment", m_feedbackComment);
			FormattedText.encodeFormattedTextAttribute(submission, "feedbacktext", m_feedbackText);

			M_log.debug(this + " BaseAssignmentSubmission: LEAVING TOXML");

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
			m_feedbackComment = submission.getFeedbackComment();
			m_feedbackText = submission.getFeedbackText();
			m_returned = submission.getReturned();
			m_graded = submission.getGraded();
			m_gradeReleased = submission.getGradeReleased();
			m_honorPledgeFlag = submission.getHonorPledgeFlag();
			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(submission.getProperties());
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
			Assignment retVal = null;
			if (m_assignment != null)
			{
				retVal = m_assignmentStorage.get(m_assignment);
			}
			
			// track event
			//EventTrackingService.post(EventTrackingService.newEvent(EVENT_ACCESS_ASSIGNMENT, retVal.getReference(), false));

			return retVal;
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
		 * @return True if a final submission, false if still a draft.
		 */
		public boolean getSubmitted()
		{
			return m_submitted;
		}

		/**
		 * Access the list of Users who submitted this response to the Assignment.
		 * 
		 * @return Array of User objects.
		 */
		public User[] getSubmitters()
		{
			List retVal = new Vector();
			for (int x = 0; x < m_submitters.size(); x++)
			{
				String userId = (String) m_submitters.get(x);
				try
				{
					retVal.add(UserDirectoryService.getUser(userId));
				}
				catch (Exception e)
				{
					M_log.warn(this + " BaseAssignmentSubmission getSubmitters" + e.getMessage() + userId);
				}
			}
			
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
		public List getSubmitterIds()
		{
			return m_submitters;
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
			if (!overrideWithGradebookValue)
			{
				// use assignment submission grade
				return m_grade;
			}
			else
			{
				// use grade from associated Gradebook
				Assignment m = getAssignment();
				String gAssignmentName = StringUtil.trimToNull(m.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
				if (gAssignmentName != null)
				{
					enableSecurityAdvisor();
					
					GradebookService g = (GradebookService)  ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
					String gradebookUid = m.getContext();
					if (g.isGradebookDefined(gradebookUid) && g.isAssignmentDefined(gradebookUid, gAssignmentName))
					{
						org.sakaiproject.service.gradebook.shared.Assignment gAssignment = g.getAssignment(gradebookUid, gAssignmentName);
						Map studentMap = g.getViewableStudentsForItemForCurrentUser(gradebookUid, gAssignment.getId());
						String userId = (String) m_submitters.get(0);
						if (studentMap.containsKey(userId))
						{
							// return student score from Gradebook
							try
							{
								String gString = StringUtil.trimToNull(g.getAssignmentScoreString(gradebookUid, gAssignmentName, userId));
								if (gString != null)
								{
									return gString;
								}
							}
							catch (Exception e)
							{
								M_log.warn(this + " BaseAssignmentSubmission getGrade getAssignmentScoreString from GradebookService " + e.getMessage() + " context=" + m_context + " assignment id=" + m_assignment + " userId=" + userId + " gAssignmentName=" + gAssignmentName); 
							}
						}
					}
					
					disableSecurityAdvisors();
				}
			}
			return m_grade;
		}

		/**
		 * Access the grade recieved.
		 * 
		 * @return The Submission's grade..
		 */
		public String getGradeDisplay()
		{
			Assignment m = getAssignment();
			String grade = getGrade();
			if (m.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)
			{
				if (grade != null && grade.length() > 0 && !grade.equals("0"))
				{
					try
					{
						Integer.parseInt(grade);
						// if point grade, display the grade with one decimal place
						return grade.substring(0, grade.length() - 1) + "." + grade.substring(grade.length() - 1);
					}
					catch (Exception e)
					{
						return grade;
					}
				}
				else
				{
					return StringUtil.trimToZero(grade);
				}
			}
			else
			{
				if (grade != null && grade.length() > 0)
				{
					return StringUtil.trimToZero(grade);
				}
				else
				{
					// return "ungraded" in stead
					return rb.getString("gen.ung1");
				}
			}
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
			boolean allowGrade = allowGradeSubmission(getReference());
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
								retVal = rb.getString("listsub.resubmi") + " " + submitTime.toStringLocalFull();
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
							retVal = rb.getString("gen.ung1");
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
					else if (allowGrade)
						// grade saved but not release yet, show this to graders
						retVal = getGradeOrComment();
				}
				else	
				{
					if (allowGrade)
						retVal = rb.getString("gen.ung1");
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
			String numString = StringUtil.trimToNull(m_properties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER));
			return numString != null?Integer.valueOf(numString).intValue():0;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public Time getCloseTime()
		{
			String closeTimeString = StringUtil.trimToNull(m_properties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME));
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
				ContentResource cr = getFirstAcceptableAttachement(attachments);
				Assignment ass = this.getAssignment();
				if (ass != null)
				{
					contentReviewService.queueContent(null, null, ass.getReference(), cr.getId());
				}
				else
				{
					// error, assignment couldn't be found. Log the error
					M_log.debug(this + " BaseAssignmentSubmissionEdit postAttachment: Unable to find assignment associated with submission id= " + this.m_id + " and assignment id=" + this.m_assignment);
				}
			} catch (QueueException qe) {
				M_log.warn(this + " BaseAssignmentSubmissionEdit postAttachment: Unable to add content to Content Review queue: " + qe.getMessage());
			} catch (Exception e) {
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
					M_log.warn(this + ":geFirstAcceptableAttachment " + e.getMessage());
				} catch (IdUnusedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					M_log.warn(this + ":geFirstAcceptableAttachment " + e.getMessage());
				} catch (TypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					M_log.warn(this + ":geFirstAcceptableAttachment " + e.getMessage());
				}

				
			}
			return null;
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
		timeString = StringUtil.trimToNull(timeString);
		if (timeString != null)
		{
			try
			{
				aTime = TimeService.newTimeGmt(timeString);
			}
			catch (Exception e)
			{
				M_log.warn(this + ":geTimeObject " + e.getMessage());
				try
				{
					long longTime = Long.parseLong(timeString);
					aTime = TimeService.newTime(longTime);
				}
				catch (Exception ee)
				{
					M_log.warn(this + " getTimeObject Base Exception creating time object from xml file : " + ee.getMessage() + " timeString=" + timeString);
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

	protected class AssignmentStorageUser implements StorageUser, SAXEntityReader
	{
		private Map<String,Object> m_services;
		
		/**
		 * Construct a new continer given just an id.
		 * 
		 * @param id
		 *        The id for the new object.
		 * @return The new container Resource.
		 */
		public Entity newContainer(String ref)
		{
			return null;
		}

		/**
		 * Construct a new container resource, from an XML element.
		 * 
		 * @param element
		 *        The XML.
		 * @return The new container resource.
		 */
		public Entity newContainer(Element element)
		{
			return null;
		}

		/**
		 * Construct a new container resource, as a copy of another
		 * 
		 * @param other
		 *        The other contianer to copy.
		 * @return The new container resource.
		 */
		public Entity newContainer(Entity other)
		{
			return null;
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
		 * Construct a new continer given just an id.
		 * 
		 * @param id
		 *        The id for the new object.
		 * @return The new containe Resource.
		 */
		public Edit newContainerEdit(String ref)
		{
			return null;
		}

		/**
		 * Construct a new container resource, from an XML element.
		 * 
		 * @param element
		 *        The XML.
		 * @return The new container resource.
		 */
		public Edit newContainerEdit(Element element)
		{
			return null;
		}

		/**
		 * Construct a new container resource, as a copy of another
		 * 
		 * @param other
		 *        The other contianer to copy.
		 * @return The new container resource.
		 */
		public Edit newContainerEdit(Entity other)
		{
			return null;
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

		/**
		 * Check if this resource is in draft mode.
		 * 
		 * @param r
		 *        The resource.
		 * @return true if the resource is in draft mode, false if not.
		 */
		public boolean isDraft(Entity r)
		{
			return false;
		}

		/**
		 * Access the resource owner user id.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource owner user id.
		 */
		public String getOwnerId(Entity r)
		{
			return null;
		}

		/**
		 * Access the resource date.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource date.
		 */
		public Time getDate(Entity r)
		{
			return null;
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
								M_log.warn(this + " AssignmentStorageUser getDefaultHandler startElement Unexpected Element in XML [" + qName + "]");
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

	protected class AssignmentContentStorageUser implements StorageUser, SAXEntityReader
	{
		private Map<String,Object> m_services;
		
		/**
		 * Construct a new continer given just an id.
		 * 
		 * @param id
		 *        The id for the new object.
		 * @return The new container Resource.
		 */
		public Entity newContainer(String ref)
		{
			return null;
		}

		/**
		 * Construct a new container resource, from an XML element.
		 * 
		 * @param element
		 *        The XML.
		 * @return The new container resource.
		 */
		public Entity newContainer(Element element)
		{
			return null;
		}

		/**
		 * Construct a new container resource, as a copy of another
		 * 
		 * @param other
		 *        The other contianer to copy.
		 * @return The new container resource.
		 */
		public Entity newContainer(Entity other)
		{
			return null;
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
		 * Construct a new continer given just an id.
		 * 
		 * @param id
		 *        The id for the new object.
		 * @return The new containe Resource.
		 */
		public Edit newContainerEdit(String ref)
		{
			return null;
		}

		/**
		 * Construct a new container resource, from an XML element.
		 * 
		 * @param element
		 *        The XML.
		 * @return The new container resource.
		 */
		public Edit newContainerEdit(Element element)
		{
			return null;
		}

		/**
		 * Construct a new container resource, as a copy of another
		 * 
		 * @param other
		 *        The other contianer to copy.
		 * @return The new container resource.
		 */
		public Edit newContainerEdit(Entity other)
		{
			return null;
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

		/**
		 * Check if this resource is in draft mode.
		 * 
		 * @param r
		 *        The resource.
		 * @return true if the resource is in draft mode, false if not.
		 */
		public boolean isDraft(Entity r)
		{
			return false;
		}

		/**
		 * Access the resource owner user id.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource owner user id.
		 */
		public String getOwnerId(Entity r)
		{
			return null;
		}

		/**
		 * Access the resource date.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource date.
		 */
		public Time getDate(Entity r)
		{
			return null;
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
								M_log.warn(this + " AssignmentContentStorageUser getDefaultEntityHandler startElement Unexpected Element in XML [" + qName + "]");
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

	protected class AssignmentSubmissionStorageUser implements StorageUser, SAXEntityReader
	{
		private Map<String,Object> m_services;
		
		/**
		 * Construct a new continer given just an id.
		 * 
		 * @param id
		 *        The id for the new object.
		 * @return The new container Resource.
		 */
		public Entity newContainer(String ref)
		{
			return null;
		}

		/**
		 * Construct a new container resource, from an XML element.
		 * 
		 * @param element
		 *        The XML.
		 * @return The new container resource.
		 */
		public Entity newContainer(Element element)
		{
			return null;
		}

		/**
		 * Construct a new container resource, as a copy of another
		 * 
		 * @param other
		 *        The other contianer to copy.
		 * @return The new container resource.
		 */
		public Entity newContainer(Entity other)
		{
			return null;
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
		 * Construct a new continer given just an id.
		 * 
		 * @param id
		 *        The id for the new object.
		 * @return The new containe Resource.
		 */
		public Edit newContainerEdit(String ref)
		{
			return null;
		}

		/**
		 * Construct a new container resource, from an XML element.
		 * 
		 * @param element
		 *        The XML.
		 * @return The new container resource.
		 */
		public Edit newContainerEdit(Element element)
		{
			return null;
		}

		/**
		 * Construct a new container resource, as a copy of another
		 * 
		 * @param other
		 *        The other contianer to copy.
		 * @return The new container resource.
		 */
		public Edit newContainerEdit(Entity other)
		{
			return null;
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
			
			User[] submitters = ((AssignmentSubmission) r).getSubmitters();
			if(submitters != null && submitters.length > 0 && submitters[0] != null) 
			{
 				rv[1] = submitters[0].getId();
			} else {
				M_log.error(new Exception(this + " AssignmentSubmissionStorageUser storageFields Unique constraint is in force -- submitter[0] cannot be null"));
 			}
			
			Time submitTime = ((AssignmentSubmission) r).getTimeSubmitted();
			rv[2] = (submitTime != null)?String.valueOf(submitTime.getTime()):null;
			
			rv[3] = Boolean.valueOf(((AssignmentSubmission) r).getSubmitted()).toString();
			
			rv[4] = Boolean.valueOf(((AssignmentSubmission) r).getGraded()).toString();
			
			return rv;
		}

		/**
		 * Check if this resource is in draft mode.
		 * 
		 * @param r
		 *        The resource.
		 * @return true if the resource is in draft mode, false if not.
		 */
		public boolean isDraft(Entity r)
		{
			return false;
		}

		/**
		 * Access the resource owner user id.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource owner user id.
		 */
		public String getOwnerId(Entity r)
		{
			return null;
		}

		/**
		 * Access the resource date.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource date.
		 */
		public Time getDate(Entity r)
		{
			return null;
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
								M_log.warn(this + " AssignmentSubmissionStorageUser getDefaultHandler startElement: Unexpected Element in XML [" + qName + "]");
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

	/**********************************************************************************************************************************************************************************************************************************************************
	 * CacheRefresher implementations (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentCacheRefresher implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class AssignmentCacheRefresher implements CacheRefresher
	{
		/**
		 * Get a new value for this key whose value has already expired in the cache.
		 * 
		 * @param key
		 *        The key whose value has expired and needs to be refreshed.
		 * @param oldValue
		 *        The old expired value of the key.
		 * @return a new value for use in the cache for this key; if null, the entry will be removed.
		 */
		public Object refresh(Object key, Object oldValue, Event event)
		{

			// key is a reference, but our storage wants an id
			String id = assignmentId((String) key);

			// get whatever we have from storage for the cache for this vale
			Assignment assignment = m_assignmentStorage.get(id);

			M_log.debug(this + " AssignmentCacheRefresher:refresh(): " + key + " : " + id);

			return assignment;

		} // refresh

	}// AssignmentCacheRefresher

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentContentCacheRefresher implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class AssignmentContentCacheRefresher implements CacheRefresher
	{
		/**
		 * Get a new value for this key whose value has already expired in the cache.
		 * 
		 * @param key
		 *        The key whose value has expired and needs to be refreshed.
		 * @param oldValue
		 *        The old expired value of the key.
		 * @return a new value for use in the cache for this key; if null, the entry will be removed.
		 */
		public Object refresh(Object key, Object oldValue, Event event)
		{

			// key is a reference, but our storage wants an id
			String id = contentId((String) key);

			// get whatever we have from storage for the cache for this vale
			AssignmentContent content = m_contentStorage.get(id);

			M_log.debug(this + " AssignmentContentCacheRefresher: refresh(): " + key + " : " + id);

			return content;

		} // refresh

	}// AssignmentContentCacheRefresher

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentSubmissionCacheRefresher implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class AssignmentSubmissionCacheRefresher implements CacheRefresher
	{
		/**
		 * Get a new value for this key whose value has already expired in the cache.
		 * 
		 * @param key
		 *        The key whose value has expired and needs to be refreshed.
		 * @param oldValue
		 *        The old expired value of the key.
		 * @return a new value for use in the cache for this key; if null, the entry will be removed.
		 */
		public Object refresh(Object key, Object oldValue, Event event)
		{

			// key is a reference, but our storage wants an id
			String id = submissionId((String) key);

			// get whatever we have from storage for the cache for this vale
			AssignmentSubmission submission = m_submissionStorage.get(id);

			M_log.debug(this + " AssignmentSubmissionCacheRefresher:refresh(): " + key + " : " + id);

			return submission;

		} // refresh

	}// AssignmentSubmissionCacheRefresher
	
	/**
	 * the AssignmentComparator clas
	 */
	private class AssignmentComparator implements Comparator
	{	
		/**
		 * the criteria
		 */
		String m_criteria = null;

		/**
		 * the criteria
		 */
		String m_asc = null;

		/**
		 * constructor
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. TRUE_STRING if ascending; "false" otherwise.
		 */
		public AssignmentComparator(String criteria, String asc)
		{
			m_criteria = criteria;
			m_asc = asc;
		} // constructor

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

			/** *********** fo sorting assignments ****************** */
			if (m_criteria.equals("duedate"))
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
			else if (m_criteria.equals("sortname"))
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
						M_log.warn(this + " AssignmentComparator.compare " + e.getMessage() + " id=" + userId1);
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
						M_log.warn(this + " AssignmentComparator.compare " + e.getMessage() + " id=" + userId2);
					}
				}

				if (s1 == null)
				{
					result = -1;
				}
				else if (s2 == null)
				{
					result = 1;
				}
				else
				{
					result = s1.compareTo(s2);
				}
			}
			
			// sort ascending or descending
			if (m_asc.equals(Boolean.FALSE.toString()))
			{
				result = -result;
			}
			return result;
		}
	}
	
	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		try
		{
			if(cleanup == true)
			{
				enableSecurityAdvisor();

				String toSiteId = toContext;
				Iterator assignmentsIter = getAssignmentsForContext(toSiteId);
				while (assignmentsIter.hasNext())
				{
					Assignment assignment = (Assignment) assignmentsIter.next();
					String assignmentId = assignment.getId();
					try 
					{
						AssignmentEdit aEdit = editAssignment(assignmentId);
						
						// remove this assignment with all its associated items
						removeAssignmentAndAllReferences(aEdit);
					}
					catch(Exception ee)
					{
						M_log.warn(this + ":transferCopyEntities: remove Assignment and all references for " + assignment.getId() + ee.getMessage());
					}
				}
				
				disableSecurityAdvisors();
			}
			transferCopyEntities(fromContext, toContext, ids);
		}
		catch (Exception e)
		{
			M_log.info(this + "transferCopyEntities: End removing Assignmentt data" + e.getMessage());
		}
	}

	/**
	 * This is to mimic the FormattedText.decodeFormattedTextAttribute but use SAX serialization instead
	 * @return
	 */
	protected String FormattedTextDecodeFormattedTextAttribute(Attributes attributes, String baseAttributeName)
	{
		String ret;

		// first check if an HTML-encoded attribute exists, for example "foo-html", and use it if available
		ret = StringUtil.trimToNull(XmlDecodeAttribute(attributes, baseAttributeName + "-html"));
		if (ret != null) return ret;

		// next try the older kind of formatted text like "foo-formatted", and convert it if found
		ret = StringUtil.trimToNull(XmlDecodeAttribute(attributes, baseAttributeName + "-formatted"));
		ret = FormattedText.convertOldFormattedText(ret);
		if (ret != null) return ret;

		// next try just a plaintext attribute and convert the plaintext to formatted text if found
		// convert from old plaintext instructions to new formatted text instruction
		ret = XmlDecodeAttribute(attributes, baseAttributeName);
		ret = FormattedText.convertPlaintextToFormattedText(ret);
		return ret;
	}
	
	/**
	 * this is to mimic the Xml.decodeAttribute
	 * @param el
	 * @param tag
	 * @return
	 */
	protected String XmlDecodeAttribute(Attributes attributes, String tag)
	{
		String charset = StringUtil.trimToNull(attributes.getValue("charset"));
		if (charset == null) charset = "UTF-8";

		String body = StringUtil.trimToNull(attributes.getValue(tag));
		if (body != null)
		{
			try
			{
				byte[] decoded = CommonsCodecBase64.decodeBase64(body.getBytes("UTF-8"));
				body = new String(decoded, charset);
			}
			catch (Exception e)
			{
				M_log.warn(this + " XmlDecodeAttribute: " + e.getMessage() + " tag=" + tag);
			}
		}

		if (body == null) body = "";

		return body;
	}
	
    /**
     * remove all security advisors
     */
    protected void disableSecurityAdvisors()
    {
    	// remove all security advisors
    	SecurityService.clearAdvisors();
    }

    /**
     * Establish a security advisor to allow the "embedded" azg work to occur
     * with no need for additional security permissions.
     */
    protected void enableSecurityAdvisor()
    {
      // put in a security advisor so we can create citationAdmin site without need
      // of further permissions
      SecurityService.pushAdvisor(new SecurityAdvisor() {
        public SecurityAdvice isAllowed(String userId, String function, String reference)
        {
          return SecurityAdvice.ALLOWED;
        }
      });
    }

} // BaseAssignmentService

