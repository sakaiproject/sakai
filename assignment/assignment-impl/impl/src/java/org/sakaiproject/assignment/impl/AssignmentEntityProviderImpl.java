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
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.cover.SessionManager;
import org.theospi.portfolio.matrix.MatrixManager;

public class AssignmentEntityProviderImpl implements AssignmentEntityProvider,
CoreEntityProvider, AutoRegisterEntityProvider, PropertyProvideable {

  private AssignmentService assignmentService;
  private SiteService siteService;
  private MatrixManager matrixManager;
  
  public void setAssignmentService(AssignmentService assignmentService) {
    this.assignmentService = assignmentService;
  }

  public boolean entityExists(String id) {
    boolean rv = false;

    try {
      Assignment assignment = assignmentService.getAssignment(id);
      if (assignment != null) {
        rv = true;
      }
    }
    catch (Exception e) {}
    return rv;
  }

  public String getEntityPrefix() {
    return ENTITY_PREFIX;
  }

  public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue, boolean exactMatch) {
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
        Iterator assignmentSorter = assignmentService.getAssignmentsForContext(siteId, userId);
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
    	parsedRef = refParts[0] + Entity.SEPARATOR + refParts[1] + Entity.SEPARATOR + refParts[2];
    	defaultView = refParts[3];
    	if (refParts.length >= 5) {
    		submissionId = refParts[4].replaceAll("_", Entity.SEPARATOR);
    	}
    	if (refParts.length >= 6) {
    		decWrapper = refParts[5].replaceAll("_", Entity.SEPARATOR);
    		if(decWrapper != null && !"".equals(decWrapper)){
    			String[] splitDec = decWrapper.split(Entity.SEPARATOR);
    			if(splitDec.length == 3){
    				decWrapperTag = splitDec[0];
    				decSiteId = splitDec[1];
    				decPageId = splitDec[2];
    			}
    		}
    	}
    }
    
    String assignmentId = parsedRef;
    
    if(decWrapperTag.equals("ospMatrix") && matrixManager.canUserAccessWizardPageAndLinkedArtifcact(decSiteId, decPageId, submissionId)){

    	try {
    		Assignment assignment = assignmentService.getAssignment(assignmentId);



    		props.put("title", assignment.getTitle());
    		props.put("author", assignment.getCreator());
    		if (assignment.getTimeCreated() != null)
    			props.put("created_time", assignment.getTimeCreated().getDisplay());
    		if (assignment.getAuthorLastModified() != null)
    			props.put("modified_by", assignment.getAuthorLastModified());
    		if (assignment.getTimeLastModified() != null)
    			props.put("modified_time", assignment.getTimeLastModified().getDisplay());

    		Site site = siteService.getSite(assignment.getContext());
    		String placement = site.getToolForCommonId("sakai.assignment.grades").getId();

    		props.put("security.user", SessionManager.getCurrentSessionUserId());
    		props.put("security.site.function", SiteService.SITE_VISIT);
    		props.put("security.site.ref", site.getReference());
    		props.put("security.assignment.function", AssignmentService.SECURE_ACCESS_ASSIGNMENT);

    		List<Reference> attachments = new ArrayList<Reference>();

    		if (!"".equals(submissionId)) {
    			props.put("security.assignment.ref", submissionId);

    			SecurityService.pushAdvisor(new MySecurityAdvisor(SessionManager.getCurrentSessionUserId(), 
    					AssignmentService.SECURE_ACCESS_ASSIGNMENT, submissionId));

    			AssignmentSubmission as = assignmentService.getSubmission(submissionId);

    			SecurityService.popAdvisor();

    			attachments.addAll(as.getSubmittedAttachments());
    			attachments.addAll(as.getFeedbackAttachments());
    		}

    		props.put("assignment.content.decoration.wrapper", decWrapper);




    		//need the regular assignment attachments too
    		attachments.addAll(assignment.getContent().getAttachments());

    		String refs = "";
    		for (Reference comp : attachments) {
    			refs += comp.getReference() + ":::";
    		}
    		if (refs.lastIndexOf(":::") > 0) {
    			props.put("submissionAttachmentRefs", refs.substring(0, refs.lastIndexOf(":::")));
    		}

    		props.put("url", "/portal/tool/" + placement + "?assignmentId=" + assignment.getId() + 
    				"&submissionId=" + submissionId +
    				"&assignmentReference=" + assignment.getReference() + 
    				"&panel=Main&sakai_action=" + defaultView);
    		props.put("status", assignment.getStatus());
    		props.put("due_time", assignment.getDueTimeString());
    		props.put("open_time", assignment.getOpenTimeString());
    		if (assignment.getDropDeadTime() != null)
    			props.put("retract_time", assignment.getDropDeadTime().getDisplay());
    		props.put("description", assignment.getContentReference());
    		props.put("draft", "" + assignment.getDraft());
    		props.put("siteId", assignment.getContext());
    		props.put("section", assignment.getSection());
    	}

    	catch (IdUnusedException e) {
    		e.printStackTrace();
    	}
    	catch (PermissionException e) {
    		e.printStackTrace();
    	}
    }
    return props;
  }

  public String getPropertyValue(String reference, String name) {
    String rv = null;
    //lazy code, if any of the parts of getProperties is found to be slow this should be changed.
    Map<String, String> props = getProperties(reference);
    if (props != null && props.containsKey(name)) {
      rv = props.get(name);
    }
    return rv;
  }

  public void setPropertyValue(String reference, String name, String value) {
    // TODO: add ability to set properties of an assignment
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }
  
  /**
   * A simple SecurityAdviser that can be used to override permissions on one reference string for one user for one function.
   */
  private class MySecurityAdvisor implements SecurityAdvisor
  {
	  protected String m_userId;

	  protected String m_function;

	  protected List<String> m_references = new ArrayList<String>();

	  public MySecurityAdvisor(String userId, String function, String reference)
	  {
		  m_userId = userId;
		  m_function = function;
		  m_references.add(reference);
	  }
	  
	  public MySecurityAdvisor(String userId, String function, List<String> references)
	  {
		  m_userId = userId;
		  m_function = function;
		  m_references = references;
	  }

	  public SecurityAdvice isAllowed(String userId, String function, String reference)
	  {
		  SecurityAdvice rv = SecurityAdvice.PASS;
		  if (m_userId.equals(userId) && m_function.equals(function) && m_references.contains(reference))
		  {
			  rv = SecurityAdvice.ALLOWED;
		  }
		  return rv;
	  }
  }

public MatrixManager getMatrixManager() {
	return matrixManager;
}

public void setMatrixManager(MatrixManager matrixManager) {
	this.matrixManager = matrixManager;
}

}
