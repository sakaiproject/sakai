
package org.sakaiproject.assignment.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentEntityProvider;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
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
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;

public class AssignmentEntityProviderImpl implements AssignmentEntityProvider, CoreEntityProvider,
        Resolvable, ActionsExecutable, Describeable, AutoRegisterEntityProvider, PropertyProvideable {

    private AssignmentService assignmentService;
    private DeveloperHelperService developerHelperService;
    private EntityBroker entityBroker;
    private SecurityService securityService;
    private SessionManager sessionManager;
    private SiteService siteService;

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
        String messageId = view.getPathSegment(3);
        if (context == null || messageId == null) {
            throw new IllegalArgumentException("Must include context and messageId in the path ("+view+"): e.g. /assignment/annc/{context}/{messageId}");
        }
        assignData.put("context", context);
        assignData.put("messageId", messageId);
        Iterator i = assignmentService.getAssignmentsForContext(context);
        while (i.hasNext()) { // if i is empty, none of this code is run
            Assignment a = (Assignment) i.next();
            
            // This is the only link we have to know if the announcement is associated with an assignment
            String announcementCheck = a.getProperties().getProperty("CHEF:assignment_opendate_announcement_message_id");
            
            // Lots of checks to make absolutely sure this is the assignment we are looking for
            if (announcementCheck != null && ! "".equals(announcementCheck) && announcementCheck.equals(messageId)) {
                String assignmentId = a.getId();
                assignData.put("assignment", a);
                assignData.put("id", assignmentId);
                assignData.put("title", a.getTitle());
                if (assignmentId != null && assignmentId.length() > 0) {
                    String assignmentContext = a.getContext(); // assignment context
                    boolean allowReadAssignment = assignmentService.allowGetAssignment(assignmentContext); // check for read permission
                    if (allowReadAssignment && a.getOpenTime().before(TimeService.newTime())) {
                        // this checks if we want to display an assignment link
                        try {
                            Site site = siteService.getSite(assignmentContext); // site id
                            ToolConfiguration fromTool = site.getToolForCommonId("sakai.assignment.grades");
                            boolean allowAddAssignment = assignmentService.allowAddAssignment(assignmentContext); // this checks for the asn.new permission and determines the url we present the user
                            
                            // Two different urls to be rendered depending on the user's permission
                            if (allowAddAssignment) {
                                assignData.put("url", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentId=" + a.getReference() + "&panel=Main&sakai_action=doView_assignment");
                            } else {
                                assignData.put("url", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentReference=" + a.getReference() + "&panel=Main&sakai_action=doView_submission");
                            }
                        } catch (IdUnusedException e) {
                            // NO site found - skipping this one for now
                            assignData.remove("assignment");
                        }
                    }
                }
                break; // no need to keep iterating if we find the match
            }
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
                    if (assignmentService.allowGradeSubmission(a.getReference())) {
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

                String refs = "";
                for (Reference comp : attachments) {
                    refs += comp.getReference() + ":::";
                }
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
    private class MySecurityAdvisor implements SecurityAdvisor {

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

    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

}
