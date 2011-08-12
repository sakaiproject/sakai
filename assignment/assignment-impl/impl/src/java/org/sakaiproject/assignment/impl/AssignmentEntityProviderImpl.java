
package org.sakaiproject.assignment.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentEntityProvider;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.Assignment.AssignmentAccess;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;

public class AssignmentEntityProviderImpl implements AssignmentEntityProvider, CoreEntityProvider,
        Resolvable, ActionsExecutable, Describeable, AutoRegisterEntityProvider, PropertyProvideable, Outputable, Inputable {

	public class SimpleAssignment {
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
		 * the drop dead time after which responses to this assignment are considered late; may be null.
		 */
		private Time dropDeadTime;

		/**
		 * the drop dead time after which responses to this assignment are considered late; (String)
		 */
		private String dropDeadTimeString;

		/**
		 * the close time after which this assignment can no longer be viewed, and after which submissions will not be accepted. May be null.
		 */
		private Time closeTime;

		/**
		 * the close time after which this assignment can no longer be viewed, and after which submissions will not be accepted. (String)
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
		 * the access mode for the assignment - how we compute who has access to the assignment.
		 */
		private AssignmentAccess access;
		
		

		public SimpleAssignment() {
		}

		public SimpleAssignment(Assignment a) {
			super();
			this.content = a.getContent();
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
		}

		public AssignmentContent getContent() {
			return content;
		}

		public void setContent(AssignmentContent content) {
			this.content = content;
		}

		public String getContentReference() {
			return contentReference;
		}

		public void setContentReference(String contentReference) {
			this.contentReference = contentReference;
		}

		public Time getOpenTime() {
			return openTime;
		}

		public void setOpenTime(Time openTime) {
			this.openTime = openTime;
		}

		public String getOpenTimeString() {
			return openTimeString;
		}

		public void setOpenTimeString(String openTimeString) {
			this.openTimeString = openTimeString;
		}

		public Time getDueTime() {
			return dueTime;
		}

		public void setDueTime(Time dueTime) {
			this.dueTime = dueTime;
		}

		public String getDueTimeString() {
			return dueTimeString;
		}

		public void setDueTimeString(String dueTimeString) {
			this.dueTimeString = dueTimeString;
		}

		public Time getDropDeadTime() {
			return dropDeadTime;
		}

		public void setDropDeadTime(Time dropDeadTime) {
			this.dropDeadTime = dropDeadTime;
		}

		public String getDropDeadTimeString() {
			return dropDeadTimeString;
		}

		public void setDropDeadTimeString(String dropDeadTimeString) {
			this.dropDeadTimeString = dropDeadTimeString;
		}

		public Time getCloseTime() {
			return closeTime;
		}

		public void setCloseTime(Time closeTime) {
			this.closeTime = closeTime;
		}

		public String getCloseTimeString() {
			return closeTimeString;
		}

		public void setCloseTimeString(String closeTimeString) {
			this.closeTimeString = closeTimeString;
		}

		public String getSection() {
			return section;
		}

		public void setSection(String section) {
			this.section = section;
		}

		public String getContext() {
			return context;
		}

		public void setContext(String context) {
			this.context = context;
		}

		public boolean isDraft() {
			return draft;
		}

		public void setDraft(boolean draft) {
			this.draft = draft;
		}

		public String getCreator() {
			return creator;
		}

		public void setCreator(String creator) {
			this.creator = creator;
		}

		public Time getTimeCreated() {
			return timeCreated;
		}

		public void setTimeCreated(Time timeCreated) {
			this.timeCreated = timeCreated;
		}

		public List getAuthors() {
			return authors;
		}

		public void setAuthors(List authors) {
			this.authors = authors;
		}

		public Time getTimeLastModified() {
			return timeLastModified;
		}

		public void setTimeLastModified(Time timeLastModified) {
			this.timeLastModified = timeLastModified;
		}

		public String getAuthorLastModified() {
			return authorLastModified;
		}

		public void setAuthorLastModified(String authorLastModified) {
			this.authorLastModified = authorLastModified;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public int getPosition_order() {
			return position_order;
		}

		public void setPosition_order(int position_order) {
			this.position_order = position_order;
		}

		public Collection getGroups() {
			return groups;
		}

		public void setGroups(Collection groups) {
			this.groups = groups;
		}

		public AssignmentAccess getAccess() {
			return access;
		}

		public void setAccess(AssignmentAccess access) {
			this.access = access;
		}
	}
    private AssignmentService assignmentService;
    private EntityBroker entityBroker;
    private SecurityService securityService;
    private SessionManager sessionManager;
    private SiteService siteService;
    
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON, Formats.FORM };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON, Formats.FORM };
	}
	
	public Object getSampleEntity() {
		return new SimpleAssignment();
	}

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
     */
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
     */
    public boolean entityExists(String id) {
        boolean rv = false;
        try {
            Assignment assignment = assignmentService.getAssignment(id);
            if (assignment != null) {
                rv = true;
            }
        } catch (Exception e) {
            rv = false;
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
     */
    public Object getEntity(EntityReference ref) {
        if (ref == null || ref.getId() == null) {
            throw new IllegalArgumentException("ref and id must be set for assignments");
        }
        Assignment assignment;
        try {
            assignment = assignmentService.getAssignment(ref.getId());
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("No assignment found: "+ref, ref.toString(), e);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        }
        return assignment;
    }

    @EntityCustomAction(action="annc", viewKey=EntityView.VIEW_LIST)
    public Map<String, Object> getAssignDataForAnnouncement(EntityView view, Map<String, Object> params) {
        Map<String, Object> assignData = new HashMap<String, Object>();

        String context = view.getPathSegment(2);
        String assignmentId = view.getPathSegment(3);
        if (context == null || assignmentId == null)
        {
            // format of the view should be in a standard assignment reference
            throw new IllegalArgumentException("Must include context and assignmentId in the path ("+view+"): e.g. /assignment/a/{context}/{assignmentId}");
        }
        try
        {
            Assignment a = assignmentService.getAssignment(assignmentId);
            
            assignData.put("assignment", a);
            assignData.put("context", context);
            assignData.put("assignmentId", assignmentId);
            
            // This is for checking to see if there is a link to announcements in the assignment
            String announcementCheck = a.getProperties().getProperty("CHEF:assignment_opendate_announcement_message_id");
            
            // the message id passed in through parameters
            String messageId = (String) params.get("messageId");
            
            // Lots of checks to make absolutely sure this is the assignment we are looking for
            if (announcementCheck != null && ! "".equals(announcementCheck) && messageId!=null && ! "".equals(messageId) && announcementCheck.equals(messageId))
            {
                assignData.put("assignmentTitle", a.getTitle());
                String assignmentContext = a.getContext(); // assignment context
                boolean allowReadAssignment = assignmentService.allowGetAssignment(assignmentContext); // check for read permission
                if (allowReadAssignment && a.getOpenTime().before(TimeService.newTime()))
                {
                    // this checks if we want to display an assignment link
                    try
                    {
                        Site site = siteService.getSite(assignmentContext); // site id
                        ToolConfiguration fromTool = site.getToolForCommonId("sakai.assignment.grades");
                        boolean allowAddAssignment = assignmentService.allowAddAssignment(assignmentContext); // this checks for the asn.new permission and determines the url we present the user
                        boolean allowSubmitAssignment = assignmentService.allowAddSubmission(assignmentContext); // this checks for the asn.submit permission and determines the url we present the user

                        // Three different urls to be rendered depending on the user's permission
                        if (allowAddAssignment)
                        {
                            assignData.put("assignmentUrl", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentId=" + a.getReference() + "&panel=Main&sakai_action=doView_assignment");
                        }
                        else if (allowSubmitAssignment)
                        {
                            assignData.put("assignmentUrl", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentReference=" + a.getReference() + "&panel=Main&sakai_action=doView_submission");
                        }
                        else
                        {
                            // user can read the assignment, but not submit, so render the appropriate url
                            assignData.put("assignmentUrl", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentId=" + a.getReference() + "&panel=Main&sakai_action=doView_assignment_as_student");
                        }
                    }
                    catch (IdUnusedException e)
                    {
                        // No site found
                        assignData.remove("assignment");
                        assignData.remove("context");
                        assignData.remove("assignmentId");
                        assignData.remove("assignmentTitle");
                        assignData.remove("assignmentUrl");
                        throw new IdUnusedException("No site found while creating assignment url");
                    }
                }
            }
        }
        catch (IdUnusedException e)
        {
            assignData.remove("assignment");
            assignData.remove("context");
            assignData.remove("assignmentId");
            assignData.remove("assignmentTitle");
            assignData.remove("assignmentUrl");
            throw new EntityNotFoundException("No assignment found", assignmentId, e);
        }
        catch (PermissionException e)
        {
            assignData.remove("assignment");
            assignData.remove("context");
            assignData.remove("assignmentId");
            assignData.remove("assignmentTitle");
            assignData.remove("assignmentUrl");
            throw new SecurityException(e);
        }
        return assignData;
    }
    
    @EntityCustomAction(action="deepLinks", viewKey=EntityView.VIEW_LIST)
    public Map<String, Object> getAssignmentDeepLinks(EntityView view, Map<String, Object> params) {
        Map<String, Object> assignData = new HashMap<String, Object>();

        if (view.getPathSegment(2) == null || view.getPathSegment(3) == null || view.getPathSegment(4) == null || view.getPathSegment(5) == null )
        {
            // format of the view should be in a standard assignment reference
            throw new IllegalArgumentException("Must include the following parameters in the path ("+view+"): e.g. /assignment/{assignmentId}/{allowReadAssignment}/{allowAddAssignment}/{allowSubmitAssignment}");
        }
        
        // get all params
        String assignmentId = view.getPathSegment(2);
        boolean allowReadAssignment = Boolean.valueOf(view.getPathSegment(3));
        boolean allowAddAssignment = Boolean.valueOf(view.getPathSegment(4));
        boolean allowSubmitAssignment = Boolean.valueOf(view.getPathSegment(5));
        
        try
        {   
            Assignment a = assignmentService.getAssignment(assignmentId);
            assignData.put("assignmentId", assignmentId);
            assignData.put("assignmentTitle", a.getTitle());
            String assignmentContext = a.getContext(); // assignment context
            if (allowReadAssignment && a.getOpenTime().before(TimeService.newTime()))
            {
                // this checks if we want to display an assignment link
                try
                {
                    Site site = siteService.getSite(assignmentContext); // site id
                    ToolConfiguration fromTool = site.getToolForCommonId("sakai.assignment.grades");
                    // Three different urls to be rendered depending on the user's permission
                    if (allowAddAssignment)
                    {
                        assignData.put("assignmentUrl", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentId=" + a.getReference() + "&panel=Main&sakai_action=doView_assignment");
                    }
                    else if (allowSubmitAssignment)
                    {
                        assignData.put("assignmentUrl", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentReference=" + a.getReference() + "&panel=Main&sakai_action=doView_submission");
                    }
                    else
                    {
                        // user can read the assignment, but not submit, so render the appropriate url
                        assignData.put("assignmentUrl", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentId=" + a.getReference() + "&panel=Main&sakai_action=doView_assignment_as_student");
                    }
                }
                catch (IdUnusedException e)
                {
                    // No site found
                    assignData.remove("assignmentTitle");
                    assignData.remove("assignmentUrl");
                    throw new IdUnusedException("No site found while creating assignment url");
                }
            }
        }
        catch (IdUnusedException e)
        {
            assignData.remove("assignmentTitle");
            assignData.remove("assignmentUrl");
            throw new EntityNotFoundException("No assignment found", assignmentId, e);
        }
        catch (PermissionException e)
        {
            assignData.remove("assignmentTitle");
            assignData.remove("assignmentUrl");
            throw new SecurityException(e);
        }
        return assignData;
    }

    // PROPERTY STUFF

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider#findEntityRefs(java.lang.String[], java.lang.String[], java.lang.String[], boolean)
     */
    public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
            boolean exactMatch) {
        String siteId = null;
        String userId = null;
        List<String> rv = new ArrayList<String>();

        if (ENTITY_PREFIX.equals(prefixes[0])) {

            for (int i = 0; i < name.length; i++) {
                if ("context".equalsIgnoreCase(name[i]) || "site".equalsIgnoreCase(name[i]))
                    siteId = searchValue[i];
                else if ("user".equalsIgnoreCase(name[i]) || "userId".equalsIgnoreCase(name[i]))
                    userId = searchValue[i];
            }

            if (siteId != null && userId != null) {
                Iterator assignmentSorter = assignmentService.getAssignmentsForContext(siteId,
                        userId);
                // filter to obtain only grade-able assignments
                while (assignmentSorter.hasNext()) {
                    Assignment a = (Assignment) assignmentSorter.next();
                    if (!a.getDraft() && assignmentService.allowGradeSubmission(a.getReference())) {
                        rv.add(Entity.SEPARATOR + ENTITY_PREFIX + Entity.SEPARATOR + a.getId());
                    }
                }
            }
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider#getProperties(java.lang.String)
     */
    public Map<String, String> getProperties(String reference) {
        Map<String, String> props = new HashMap<String, String>();
        String parsedRef = reference;
        String defaultView = "doView_submission";
        String[] refParts = reference.split(Entity.SEPARATOR);
        String submissionId = "";
        String decWrapper = null;
        String decWrapperTag = "";
        String decSiteId = "";
        String decPageId = "";

        if (refParts.length >= 4) {
            parsedRef = refParts[0] + Entity.SEPARATOR + refParts[1] + Entity.SEPARATOR
                    + refParts[2];
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
        if (!"".equals(decSiteId) && !"".equals(decPageId) && !"".equals(submissionId)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", decSiteId);
            params.put("pageId", decPageId);
            params.put("linkedArtifactId", submissionId);
            ActionReturn ret = entityBroker.executeCustomAction("/matrixcell/" + decPageId,
                    "canUserAccessWizardPageAndLinkedArtifcact", params, null);
            if (ret != null && ret.getEntityData() != null) {
                Object returnData = ret.getEntityData().getData();
                canUserAccessWizardPageAndLinkedArtifcact = (Boolean) returnData;
            }
        }

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            props.put("title", assignment.getTitle());
            props.put("author", assignment.getCreator());
            props.put("description", assignment.getContentReference());
            props.put("draft", "" + assignment.getDraft());
            props.put("siteId", assignment.getContext());
            props.put("section", assignment.getSection());
            props.put("status", assignment.getStatus());
            props.put("portalURL", assignment.getUrl());
            if (assignment.getTimeCreated() != null) {
                props.put("created_time", assignment.getTimeCreated().getDisplay());
            }
            if (assignment.getAuthorLastModified() != null) {
                props.put("modified_by", assignment.getAuthorLastModified());
            }
            if (assignment.getTimeLastModified() != null) {
                props.put("modified_time", assignment.getTimeLastModified().getDisplay());
            }
            props.put("due_time", assignment.getDueTimeString());
            props.put("open_time", assignment.getOpenTimeString());
            if (assignment.getDropDeadTime() != null) {
                props.put("retract_time", assignment.getDropDeadTime().getDisplay());
            }

            Site site = siteService.getSite(assignment.getContext());
            String placement = site.getToolForCommonId("sakai.assignment.grades").getId();

            props.put("security.user", sessionManager.getCurrentSessionUserId());
            props.put("security.site.function", SiteService.SITE_VISIT);
            props.put("security.site.ref", site.getReference());
            props.put("security.assignment.function", AssignmentService.SECURE_ACCESS_ASSIGNMENT);

            // OSP specific
            if (("ospMatrix".equals(decWrapperTag) && canUserAccessWizardPageAndLinkedArtifcact)
                    || "".equals(submissionId)) {

                List<Reference> attachments = new ArrayList<Reference>();

                if (!"".equals(submissionId)) {
                    props.put("security.assignment.ref", submissionId);
                    securityService.pushAdvisor(new MySecurityAdvisor(sessionManager
                            .getCurrentSessionUserId(), AssignmentService.SECURE_ACCESS_ASSIGNMENT,
                            submissionId));
                    AssignmentSubmission as = assignmentService.getSubmission(submissionId);
                    securityService.popAdvisor();
                    attachments.addAll(as.getSubmittedAttachments());
                    attachments.addAll(as.getFeedbackAttachments());
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
                    props.put("submissionAttachmentRefs", refs
                            .substring(0, refs.lastIndexOf(":::")));
                }

                props.put("url", "/portal/tool/" + placement + "?assignmentId="
                        + assignment.getId() + "&submissionId=" + submissionId
                        + "&assignmentReference=" + assignment.getReference()
                        + "&panel=Main&sakai_action=" + defaultView);
            }
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("No assignment found", reference, e);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        }
        return props;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider#getPropertyValue(java.lang.String, java.lang.String)
     */
    public String getPropertyValue(String reference, String name) {
        String rv = null;
        // lazy code, if any of the parts of getProperties is found to be slow this should be
        // changed.
        Map<String, String> props = getProperties(reference);
        if (props != null && props.containsKey(name)) {
            rv = props.get(name);
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider#setPropertyValue(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setPropertyValue(String reference, String name, String value) {
        // TODO: add ability to set properties of an assignment
    }

    /**
     * A simple SecurityAdviser that can be used to override permissions on one reference string for
     * one user for one function.
     */
    static private class MySecurityAdvisor implements SecurityAdvisor {

        protected String m_userId;

        protected String m_function;

        protected List<String> m_references = new ArrayList<String>();

        public MySecurityAdvisor(String userId, String function, String reference) {
            m_userId = userId;
            m_function = function;
            m_references.add(reference);
        }

        public MySecurityAdvisor(String userId, String function, List<String> references) {
            m_userId = userId;
            m_function = function;
            m_references = references;
        }

        public SecurityAdvice isAllowed(String userId, String function, String reference) {
            SecurityAdvice rv = SecurityAdvice.PASS;
            if (m_userId.equals(userId) && m_function.equals(function)
                    && m_references.contains(reference)) {
                rv = SecurityAdvice.ALLOWED;
            }
            return rv;
        }
    }

    // SETTERS

    public void setAssignmentService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setEntityBroker(EntityBroker entityBroker) {
        this.entityBroker = entityBroker;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

}
