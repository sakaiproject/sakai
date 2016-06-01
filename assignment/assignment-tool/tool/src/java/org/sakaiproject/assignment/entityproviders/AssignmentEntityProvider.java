package org.sakaiproject.assignment.entityproviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.Assignment.AssignmentAccess;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.impl.BaseAssignmentService;
import org.sakaiproject.assignment.impl.MySecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;


public class AssignmentEntityProvider extends AbstractEntityProvider implements EntityProvider, 
		CoreEntityProvider, Resolvable, ActionsExecutable, Describeable,
		AutoRegisterEntityProvider, PropertyProvideable, Outputable, Inputable {

	public final static String ENTITY_PREFIX = "assignment";
	private static Logger M_log = LoggerFactory.getLogger(AssignmentEntityProvider.class);

	@AllArgsConstructor
	public class DecoratedAttachment implements Comparable<Object> {

		@Getter
		private String name;
		@Getter
		private String url;

		public int compareTo(Object other) {
			return this.getUrl().compareTo(
					((DecoratedAttachment) other).getUrl());
		}
	}

	@Data
	public class SimpleAssignment {
		/**
		 * the assignment id
		 */
		private String id;

		/**
		 * the AssignmentContent of this Assignment.
		 */
		private AssignmentContent content;

		/**
		 * the reference of the AssignmentContent of this Assignment.
		 */
		private String contentReference;

		/**
		 * the first time at which the assignment can be viewed; may be null.
		 */
		private Time openTime;

		/**
		 * the first time at which the assignment can be viewed; (String)
		 */
		private String openTimeString;

		/**
		 * the time at which the assignment is due; may be null.
		 */
		private Time dueTime;

		/**
		 * the time at which the assignment is due; (String)
		 */
		private String dueTimeString;

		/**
		 * the drop dead time after which responses to this assignment are
		 * considered late; may be null.
		 */
		private Time dropDeadTime;

		/**
		 * the drop dead time after which responses to this assignment are
		 * considered late; (String)
		 */
		private String dropDeadTimeString;

		/**
		 * the close time after which this assignment can no longer be viewed,
		 * and after which submissions will not be accepted. May be null.
		 */
		private Time closeTime;

		/**
		 * the close time after which this assignment can no longer be viewed,
		 * and after which submissions will not be accepted. (String)
		 */
		private String closeTimeString;

		/**
		 * the section info.
		 */
		private String section;

		/**
		 * the context at the time of creation.
		 */
		private String context;

		/**
		 * Get whether this is a draft or final copy.
		 */
		private boolean draft;

		/**
		 * the creator of this object.
		 */
		private String creator;

		/**
		 * the time that this object was created.
		 */
		private Time timeCreated;

		/**
		 * the list of authors.
		 */
		private List authors;
		
		/**
		 * the assignment instructions.
		 */
		private String instructions;

		/**
		 * the time of last modificaiton.
		 */
		private Time timeLastModified;

		/**
		 * the author of last modification
		 */
		private String authorLastModified;

		/**
		 * the title
		 */
		private String title;

		/**
		 * Return string representation of assignment status
		 */
		private String status;

		/**
		 * the position order field for the assignment.
		 */
		private int position_order;

		/**
		 * 
		 * the groups defined for this assignment.
		 */
		private Collection groups;

		/**
		 * the access mode for the assignment - how we compute who has access to
		 * the assignment.
		 */
		private AssignmentAccess access;

		/**
		 * the attachment list
		 */
		private List<DecoratedAttachment> attachments;
		
		/**
		 * Grade scale description.
		 */
		private String gradeScale;
		
		/**
		 * Max points used when grade scale = "Points"
		 */
		private String gradeScaleMaxPoints;
		
		/**
		 * Submission type description (e.g. inline only, inline and attachments)
		 */
		private String submissionType;
		
		/**
		 * Allow re-submission flag
		 */
		private boolean allowResubmission;
		
		/**
		 * Supplement items: model answer text
		 */
		private String modelAnswerText;

		/**
		 * Supplement items: private note text
		 */
		private String privateNoteText;
		
		
		/**
		 * Supplement items: all purpose item text
		 */
		private String allPurposeItemText;
		
		/**
		 * the linked gradebook item id and name
		 */
		private Long gradebookItemId;
		private String gradebookItemName;
		
		public SimpleAssignment() {
		}

		public SimpleAssignment(Assignment a) {
			super();
			if (a == null){
				return;
			}
			this.id = a.getId();
			this.contentReference = a.getContentReference();
			this.openTime = a.getOpenTime();
			this.openTimeString = a.getOpenTimeString();
			this.dueTime = a.getDueTime();
			this.dueTimeString = a.getDueTimeString();
			this.dropDeadTime = a.getDropDeadTime();
			this.dropDeadTimeString = a.getDropDeadTimeString();
			this.closeTime = a.getCloseTime();
			this.closeTimeString = a.getCloseTimeString();
			this.section = a.getSection();
			this.context = a.getContext();
			this.draft = a.getDraft();
			this.creator = a.getCreator();
			this.timeCreated = a.getTimeCreated();
			this.authors = a.getAuthors();
			this.timeLastModified = a.getTimeLastModified();
			this.authorLastModified = a.getAuthorLastModified();
			this.title = a.getTitle();
			this.status = a.getStatus();
			this.position_order = a.getPosition_order();
			this.groups = a.getGroups();
			this.access = a.getAccess();
					
			String gradebookAssignmentProp = a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
			if (gradebookService.isGradebookDefined(a.getContext())) 
			{
				if (gradebookAssignmentProp != null)
				{
					// try to get internal gradebook assignment first
					org.sakaiproject.service.gradebook.shared.Assignment gAssignment = gradebookService.getAssignment(a.getContext(), gradebookAssignmentProp);
					if (gAssignment != null)
					{
						// linked Gradebook item is internal
						this.gradebookItemId = gAssignment.getId();
						this.gradebookItemName = gAssignment.getName();
					}
					else
					{
						// If the linked assignment is not internal to Gradebook, try the external assignment service
						// However, there is no API available in GradebookExternalAssessmentService of getExternalAssignment()
						// We will first check whether the external assignment is defined, and then get it through GradebookService 
						boolean isExternalAssignmentDefined = gradebookExternalService.isExternalAssignmentDefined(a.getContext(), gradebookAssignmentProp);
						if (isExternalAssignmentDefined)
						{
							// since the gradebook item is externally defined, the item is named after the external object's title
							gAssignment = gradebookService.getAssignment(a.getContext(), a.getTitle());
							if (gAssignment != null)
							{
								this.gradebookItemId = gAssignment.getId();
								this.gradebookItemName = gAssignment.getName();
							}
						}
					}
				}
				else 
				{
					M_log.warn("The property \"prop_new_assignment_add_to_gradebook\" is null for the assignment feed");
				}
			}
			if (a.getContent() != null){
				this.instructions = a.getContent().getInstructions();
	
				this.attachments = new ArrayList<DecoratedAttachment>();
				List<Reference> attachment_list = (List<Reference>) a.getContent()
						.getAttachments();
				for (Reference attachment : attachment_list) {
					if (attachment != null && attachment.getProperties() != null) {
						String url = attachment.getUrl();
						String name = attachment.getProperties().getPropertyFormatted(
								attachment.getProperties().getNamePropDisplayName());
						DecoratedAttachment decoratedAttachment = new DecoratedAttachment(
								name, url);
						this.attachments.add(decoratedAttachment);
					}
					else {
						M_log.info("There was an attachment on assignment "+ a.getId() +" that was invalid");
					}
				}
				// Translate grade scale from its numeric value to its description.
				this.gradeScale = a.getContent().getTypeOfGradeString();
				
				// If grade scale is "points" we also capture the maximum points allowed.
				if (a.getContent().getTypeOfGrade() == 3) {
					this.gradeScaleMaxPoints = a.getContent().getMaxGradePointDisplay();
				}
				
				// Use the number of submissions allowed as an indicator that re-submission is permitted.
				if (a.getProperties().getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null && a.getContent().getTypeOfSubmission() != 4){
					this.allowResubmission = true;
				}
				this.submissionType = a.getContent().getTypeOfSubmissionString();
			}
			
			// Supplement Items
			AssignmentModelAnswerItem assignmentModelAnswerItem = assignmentSupplementItemService.getModelAnswer(a.getId());
			if (assignmentModelAnswerItem != null) {
				this.modelAnswerText = assignmentModelAnswerItem.getText();
			}
			AssignmentNoteItem assignmentNoteItem = assignmentSupplementItemService.getNoteItem(a.getId());
			if (assignmentNoteItem != null) {
				this.privateNoteText = assignmentNoteItem.getNote();
			}
			AssignmentAllPurposeItem assignmentAllPurposeItem = assignmentSupplementItemService.getAllPurposeItem(a.getId());
			if (assignmentAllPurposeItem != null) {
				this.allPurposeItemText =  assignmentAllPurposeItem.getText();
			}
		}
	}

	@Setter
	private AssignmentService assignmentService;
	@Setter
	private EntityBroker entityBroker;
	@Setter
	private SecurityService securityService;
	@Setter
	private SessionManager sessionManager;
	@Setter
	private SiteService siteService;
	@Setter
	private AssignmentSupplementItemService assignmentSupplementItemService;
	@Setter
	private GradebookService gradebookService;
	@Setter
	private GradebookExternalAssessmentService gradebookExternalService;
	
	// HTML is deliberately not handled here, so that it will be handled by RedirectingAssignmentEntityServlet
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON, Formats.FORM };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON,
				Formats.FORM };
	}

	public Object getSampleEntity() {
		return new SimpleAssignment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix
	 * ()
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists
	 * (java.lang.String)
	 */
	public boolean entityExists(String id) {
		boolean rv = false;
		// This will look up the ref from the database, so if ref is not null,
		// that means it found one.
		String ref = assignmentService.assignmentReference(id);
		if (ref != null) {
			rv = true;
		}
		return rv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#
	 * getEntity(org.sakaiproject.entitybroker.EntityReference)
	 */
	public Object getEntity(EntityReference ref) {
		if (ref == null || ref.getId() == null) {
			throw new IllegalArgumentException(
					"ref and id must be set for assignments");
		}
		SimpleAssignment assignment;
		try {
			assignment = new SimpleAssignment(
					assignmentService.getAssignment(ref.getId()));
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("No assignment found: " + ref,
					ref.toString(), e);
		} catch (PermissionException e) {
			throw new SecurityException(e);
		}
		return assignment;
	}

	@EntityCustomAction(action = "annc", viewKey = EntityView.VIEW_LIST)
	public Map<String, Object> getAssignDataForAnnouncement(EntityView view,
			Map<String, Object> params) {
		Map<String, Object> assignData = new HashMap<String, Object>();

		String context = view.getPathSegment(2);
		String assignmentId = view.getPathSegment(3);
		if (context == null || assignmentId == null) {
			// format of the view should be in a standard assignment reference
			throw new IllegalArgumentException(
					"Must include context and assignmentId in the path ("
							+ view
							+ "): e.g. /assignment/a/{context}/{assignmentId}");
		}
		try {
			// enable permission to view possible draft assignment
			securityService.pushAdvisor(new MySecurityAdvisor(sessionManager
					.getCurrentSessionUserId(),
					AssignmentService.SECURE_ADD_ASSIGNMENT,
					BaseAssignmentService.getContextReference(context)));

			Assignment a = assignmentService.getAssignment(assignmentService
					.assignmentReference(context, assignmentId));
			assignData.put("assignment", a);
			assignData.put("context", context);
			assignData.put("assignmentId", assignmentId);

			// This is for checking to see if there is a link to announcements
			// in the assignment
			String announcementCheck = a.getProperties().getProperty(
					"CHEF:assignment_opendate_announcement_message_id");

			// the message id passed in through parameters
			String messageId = (String) params.get("messageId");

			// Lots of checks to make absolutely sure this is the assignment we
			// are looking for
			if (announcementCheck != null && !"".equals(announcementCheck)
					&& messageId != null && !"".equals(messageId)
					&& announcementCheck.equals(messageId)) {
				assignData.put("assignmentTitle", a.getTitle());
				String assignmentContext = a.getContext(); // assignment context
				boolean allowReadAssignment = assignmentService
						.allowGetAssignment(assignmentContext); 
				// check for read permission
				if (allowReadAssignment
						&& a.getOpenTime().before(TimeService.newTime())) {
					// this checks if we want to display an assignment link
					try {
						Site site = siteService.getSite(assignmentContext); // site id
						ToolConfiguration fromTool = site
								.getToolForCommonId("sakai.assignment.grades");
						boolean allowAddAssignment = assignmentService
								.allowAddAssignment(assignmentContext); 
						// this checks for the asn.new permission and
						// determines the url we present the user
						boolean allowSubmitAssignment = assignmentService
								.allowAddSubmission(assignmentContext); 
						// this checks for the asn.submit permission and
						// determines the url we present the user

						// Three different urls to be rendered depending on the
						// user's permission
						if (allowAddAssignment) {
							assignData
									.put("assignmentUrl",
											ServerConfigurationService
													.getPortalUrl()
													+ "/directtool/"
													+ fromTool.getId()
													+ "?assignmentId="
													+ a.getReference()
													+ "&panel=Main&sakai_action=doView_assignment");
						} else if (allowSubmitAssignment) {
							assignData
									.put("assignmentUrl",
											ServerConfigurationService
													.getPortalUrl()
													+ "/directtool/"
													+ fromTool.getId()
													+ "?assignmentReference="
													+ a.getReference()
													+ "&panel=Main&sakai_action=doView_submission");
						} else {
							// user can read the assignment, but not submit, so
							// render the appropriate url
							assignData
									.put("assignmentUrl",
											ServerConfigurationService
													.getPortalUrl()
													+ "/directtool/"
													+ fromTool.getId()
													+ "?assignmentId="
													+ a.getReference()
													+ "&panel=Main&sakai_action=doView_assignment_as_student");
						}
					} catch (IdUnusedException e) {
						// No site found
						assignData.remove("assignment");
						assignData.remove("context");
						assignData.remove("assignmentId");
						assignData.remove("assignmentTitle");
						assignData.remove("assignmentUrl");
						throw new IdUnusedException(
								"No site found while creating assignment url");
					}
				}
			}
		} catch (IdUnusedException e) {
			assignData.remove("assignment");
			assignData.remove("context");
			assignData.remove("assignmentId");
			assignData.remove("assignmentTitle");
			assignData.remove("assignmentUrl");
			throw new EntityNotFoundException("No assignment found",
					assignmentId, e);
		} catch (PermissionException e) {
			assignData.remove("assignment");
			assignData.remove("context");
			assignData.remove("assignmentId");
			assignData.remove("assignmentTitle");
			assignData.remove("assignmentUrl");
			throw new SecurityException(e);
		} finally {
			securityService.popAdvisor();
		}
		return assignData;
	}

	@EntityCustomAction(action = "deepLinkWithPermissions", viewKey = EntityView.VIEW_LIST)
	public Map<String, String> getAssignmentDeepLinks(EntityView view,
			Map<String, Object> params) {

		String context = view.getPathSegment(2);
		String assignmentId = view.getPathSegment(3);
		if (context == null || assignmentId == null) {
			// format of the view should be in a standard assignment reference
			throw new IllegalArgumentException(
					"Must include context and assignmentId in the path ("
							+ view
							+ "): e.g. /direct/assignment/deepLinkWithPermissions/{context}/{assignmentId}");
		}

		Map<String, String> assignData = new HashMap<String, String>();

		try {
			Assignment a = assignmentService.getAssignment(assignmentId);
			assignData.put("assignmentId", assignmentId);
			assignData.put("assignmentTitle", a.getTitle());

			boolean allowReadAssignment = params.get("allowReadAssignment") != null ? ((Boolean) params
					.get("allowReadAssignment")).booleanValue() : false;
			boolean allowAddAssignment = params.get("allowAddAssignment") != null ? ((Boolean) params
					.get("allowAddAssignment")).booleanValue() : false;
			boolean allowSubmitAssignment = params.get("allowSubmitAssignment") != null ? ((Boolean) params
					.get("allowSubmitAssignment")).booleanValue() : false;

			assignData.put("assignmentUrl"
                                , assignmentService.getDeepLinkWithPermissions(context, assignmentId
                                            ,allowReadAssignment, allowAddAssignment, allowSubmitAssignment));
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Assignment or site not found", assignmentId, e);
		} catch (PermissionException e) {
			throw new SecurityException(e);
		} catch (Exception e) {
			throw new EntityException(e.getMessage(), assignmentId);
        }
		return assignData;
	}

	@EntityCustomAction(action = "deepLink", viewKey = EntityView.VIEW_LIST)
	public Map<String, String> getAssignmentDeepLink(EntityView view,
			Map<String, Object> params) {

		String context = view.getPathSegment(2);
		String assignmentId = view.getPathSegment(3);
		if (context == null || assignmentId == null) {
			// format of the view should be in a standard assignment reference
			throw new IllegalArgumentException(
					"Must include context and assignmentId in the path ("
							+ view
							+ "): e.g. /direct/assignment/deepLink/{context}/{assignmentId}");
		}

		Map<String, String> assignData = new HashMap<String, String>();

		try {
			Assignment a = assignmentService.getAssignment(assignmentId);
			assignData.put("assignmentId", assignmentId);
			assignData.put("assignmentTitle", a.getTitle());
			assignData.put("assignmentUrl", assignmentService.getDeepLink(context, assignmentId));
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Assignment or site not found", assignmentId, e);
		} catch (PermissionException e) {
			throw new SecurityException(e);
		} catch (Exception e) {
			throw new EntityException(e.getMessage(), assignmentId);
        }
		return assignData;
	}

	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<?> getAssignmentsForSite(EntityView view,
			Map<String, Object> params) {
		List<SimpleAssignment> rv = new ArrayList<SimpleAssignment>();
		String siteId = view.getPathSegment(2);
		
		// check user can access this site
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId, siteId);
		}
		
		String userId = sessionManager.getCurrentSessionUserId();
		for (Iterator aIterator = assignmentService.getAssignmentsForContext(
				siteId, userId); aIterator.hasNext();) {
			Assignment a = (Assignment) aIterator.next();
			rv.add(new SimpleAssignment(a));

		}
		return rv;

	}
	
	/**
	 * my
	 */
	@EntityCustomAction(action = "my", viewKey = EntityView.VIEW_LIST)
	public List<?> getMyAssignmentsForAllSite(EntityView view,
			Map<String, Object> params) {
		List<SimpleAssignment> rv = new ArrayList<SimpleAssignment>();
		String userId = sessionManager.getCurrentSessionUserId();

		// get list of all sites
		List<Site> sites = siteService.getSites(SiteService.SelectionType.ACCESS, null,
				null, null, SiteService.SortType.TITLE_ASC, null);
		// no need to check user can access this site, as the get sites only returned accessible sites
		
		// get all assignments from each site
		for (Site site : sites) {
			String siteId = site.getId();
			
			for (Iterator aIterator = assignmentService.getAssignmentsForContext(
					siteId, userId); aIterator.hasNext();) {
				Assignment a = (Assignment) aIterator.next();
				rv.add(new SimpleAssignment(a));
			}
		}

		return rv;

	}

	
	/**
	 * item/assignmentId
	 */
	@EntityCustomAction(action = "item", viewKey = EntityView.VIEW_LIST)
	public SimpleAssignment getAssignment(EntityView view,
			Map<String, Object> params) {
		String assignmentId = view.getPathSegment(2);

		// check user can access this assignment
		Assignment a;
		try {
			a = assignmentService.getAssignment (assignmentId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid assignment id: " + assignmentId, assignmentId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to assignment: " + assignmentId, assignmentId);
		}
		return new SimpleAssignment(a);
	}

	// PROPERTY STUFF

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider
	 * #findEntityRefs(java.lang.String[], java.lang.String[],
	 * java.lang.String[], boolean)
	 */
	public List<String> findEntityRefs(String[] prefixes, String[] name,
			String[] searchValue, boolean exactMatch) {
		String siteId = null;
		String userId = null;
		List<String> rv = new ArrayList<String>();

		if (ENTITY_PREFIX.equals(prefixes[0])) {

			for (int i = 0; i < name.length; i++) {
				if ("context".equalsIgnoreCase(name[i])
						|| "site".equalsIgnoreCase(name[i]))
					siteId = searchValue[i];
				else if ("user".equalsIgnoreCase(name[i])
						|| "userId".equalsIgnoreCase(name[i]))
					userId = searchValue[i];
			}

			if (siteId != null && userId != null) {
				Iterator assignmentSorter = assignmentService
						.getAssignmentsForContext(siteId, userId);
				// filter to obtain only grade-able assignments
				while (assignmentSorter.hasNext()) {
					Assignment a = (Assignment) assignmentSorter.next();
					if (!a.getDraft()
							&& assignmentService.allowGradeSubmission(a
									.getReference())) {
						rv.add(Entity.SEPARATOR + ENTITY_PREFIX
								+ Entity.SEPARATOR + a.getId());
					}
				}
			}
		}
		return rv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider
	 * #getProperties(java.lang.String)
	 */
	public Map<String, String> getProperties(String reference) {
		Map<String, String> props = new HashMap<String, String>();
		String parsedRef = reference;
		String defaultView = "doView_submission";
		String[] refParts = reference.split(Entity.SEPARATOR);
		String submissionId = "null"; // setting to the string null
		String decWrapper = null;
		String decWrapperTag = "";
		String decSiteId = "";
		String decPageId = "";

		if (refParts.length >= 4) {
			parsedRef = refParts[0] + Entity.SEPARATOR + refParts[1]
					+ Entity.SEPARATOR + refParts[2];
			defaultView = refParts[3];
			if (refParts.length >= 5) {
				submissionId = refParts[4].replaceAll("_", Entity.SEPARATOR);
			}
			if (refParts.length >= 6) {
				decWrapper = refParts[5].replaceAll("_", Entity.SEPARATOR);
				if (decWrapper != null && !"".equals(decWrapper)) {
					String[] splitDec = decWrapper.split(Entity.SEPARATOR);
					if (splitDec.length == 3) {
						decWrapperTag = splitDec[0];
						decSiteId = splitDec[1];
						decPageId = splitDec[2];
					}
				}
			}
		}

		String assignmentId = parsedRef;
		boolean canUserAccessWizardPageAndLinkedArtifcact = false;
		if (!"".equals(decSiteId) && !"".equals(decPageId)
				&& !"null".equals(submissionId)) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("siteId", decSiteId);
			params.put("pageId", decPageId);
			params.put("linkedArtifactId", submissionId);
			ActionReturn ret = entityBroker.executeCustomAction("/matrixcell/"
					+ decPageId, "canUserAccessWizardPageAndLinkedArtifcact",
					params, null);
			if (ret != null && ret.getEntityData() != null) {
				Object returnData = ret.getEntityData().getData();
				canUserAccessWizardPageAndLinkedArtifcact = (Boolean) returnData;
			}
		}

		try {
			Assignment assignment = assignmentService
					.getAssignment(assignmentId);
			props.put("title", assignment.getTitle());
			props.put("author", assignment.getCreator());
			props.put("description", assignment.getContentReference());
			props.put("draft", "" + assignment.getDraft());
			props.put("siteId", assignment.getContext());
			props.put("section", assignment.getSection());
			props.put("status", assignment.getStatus());
			props.put("portalURL", assignment.getUrl());
			if (assignment.getTimeCreated() != null) {
				props.put("created_time", assignment.getTimeCreated()
						.getDisplay());
			}
			if (assignment.getAuthorLastModified() != null) {
				props.put("modified_by", assignment.getAuthorLastModified());
			}
			if (assignment.getTimeLastModified() != null) {
				props.put("modified_time", assignment.getTimeLastModified()
						.getDisplay());
			}
			props.put("due_time", assignment.getDueTimeString());
			props.put("open_time", assignment.getOpenTimeString());
			if (assignment.getDropDeadTime() != null) {
				props.put("retract_time", assignment.getDropDeadTime()
						.getDisplay());
			}

			Site site = siteService.getSite(assignment.getContext());
			String placement = site.getToolForCommonId(
					"sakai.assignment.grades").getId();

			props.put("security.user", sessionManager.getCurrentSessionUserId());
			props.put("security.site.function", SiteService.SITE_VISIT);
			props.put("security.site.ref", site.getReference());
			props.put("security.assignment.function",
					AssignmentService.SECURE_ACCESS_ASSIGNMENT);
			props.put("security.assignment.grade.function",
					AssignmentService.SECURE_GRADE_ASSIGNMENT_SUBMISSION);
			props.put("security.assignment.grade.ref",
					assignment.getReference());

			// OSP specific
			if (("ospMatrix".equals(decWrapperTag) && canUserAccessWizardPageAndLinkedArtifcact)
					|| "null".equals(submissionId)) {

				List<Reference> attachments = new ArrayList<Reference>();

				if (!"null".equals(submissionId)) {
					props.put("security.assignment.ref", submissionId);
					SecurityAdvisor subAdv = new MySecurityAdvisor(
							sessionManager.getCurrentSessionUserId(),
							AssignmentService.SECURE_ACCESS_ASSIGNMENT_SUBMISSION,
							submissionId);
					SecurityAdvisor subAdv2 = new MySecurityAdvisor(
							sessionManager.getCurrentSessionUserId(),
							AssignmentService.SECURE_GRADE_ASSIGNMENT_SUBMISSION,
							assignment.getReference());
					try {
						// enable permission to access submission
						securityService.pushAdvisor(subAdv);
						securityService.pushAdvisor(subAdv2);
						AssignmentSubmission as = assignmentService
								.getSubmission(submissionId);
						attachments.addAll(as.getSubmittedAttachments());
						attachments.addAll(as.getFeedbackAttachments());
					} catch (Exception e) {
						// exception
					} finally {
						// remove security advisor
						securityService.popAdvisor(subAdv2);
						securityService.popAdvisor(subAdv);
					}
				}

				props.put("assignment.content.decoration.wrapper", decWrapper);

				// need the regular assignment attachments too
				attachments.addAll(assignment.getContent().getAttachments());

				StringBuffer refsBuffer = new StringBuffer();
				for (Reference comp : attachments) {
					refsBuffer.append(comp.getReference() + ":::");
				}
				String refs = refsBuffer.toString();

				if (refs.lastIndexOf(":::") > 0) {
					props.put("submissionAttachmentRefs",
							refs.substring(0, refs.lastIndexOf(":::")));
				}

				props.put("url", "/portal/tool/" + placement + "?assignmentId="
						+ assignment.getId() + "&submissionId=" + submissionId
						+ "&assignmentReference=" + assignment.getReference()
						+ "&panel=Main&sakai_action=" + defaultView);
			}
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("No assignment found", reference,
					e);
		} catch (PermissionException e) {
			throw new SecurityException(e);
		}
		return props;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider
	 * #getPropertyValue(java.lang.String, java.lang.String)
	 */
	public String getPropertyValue(String reference, String name) {
		String rv = null;
		// lazy code, if any of the parts of getProperties is found to be slow
		// this should be
		// changed.
		Map<String, String> props = getProperties(reference);
		if (props != null && props.containsKey(name)) {
			rv = props.get(name);
		}
		return rv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider
	 * #setPropertyValue(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void setPropertyValue(String reference, String name, String value) {
		// TODO: add ability to set properties of an assignment
	}

}
