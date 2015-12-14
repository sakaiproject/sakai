package org.sakaiproject.gradebookng.rest;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.GbPortalPermission;
import org.sakaiproject.gradebookng.business.model.GbGradeCell;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * This entity provider is to support some of the Javascript front end pieces.
 * It never was built to support third party access, and never will support that
 * use case.
 * 
 * The data you need for Gradebook integrations should already be available in
 * the standard gradebook entityprovider
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
@CommonsLog
public class GradebookNgEntityProvider extends AbstractEntityProvider implements
		EntityProvider, AutoRegisterEntityProvider, ActionsExecutable,
		Outputable, Describeable {

	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON };
	}

	@Override
	public String getEntityPrefix() {
		return "gbng";
	}

	/**
	 * site/assignment-list
	 * @throws IdUnusedException 
	 */
	@EntityCustomAction(action = "assignments", viewKey = EntityView.VIEW_LIST)
	public List<Assignment> getAssignmentList(EntityView view) {

		// get siteId
		String siteId = view.getPathSegment(2);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"Site ID must be set in order to access GBNG data.");
		}
		checkValidSite(siteId);

		// check instructor
		checkInstructor(siteId);
		
		// get assignment list
		List<Assignment> assignments = this.businessService.getGradebookAssignments(siteId);
				
		return assignments;
	}
	
	/**
	 * Update the order of an assignment in the gradebook
	 * This is a per site setting.
	 * 
	 * @param ref
	 * @param params map, must include:
	 * siteId
	 * assignmentId
	 * new order
	 * 
	 * an assignmentorder object will be created and saved as a list in the XML property 'gbng_assignment_order'
	 */
	@EntityCustomAction(action = "assignment-order", viewKey = EntityView.VIEW_NEW)
	public void updateAssignmentOrder(EntityReference ref, Map<String, Object> params) {
		
		// get params
		String siteId = (String) params.get("siteId");
		long assignmentId = NumberUtils.toLong((String) params.get("assignmentId"));
		int order = NumberUtils.toInt((String) params.get("order"));

		// check params supplied are valid 
		if (StringUtils.isBlank(siteId) || assignmentId == 0 || order < 0) {
			throw new IllegalArgumentException(
					"Request data was missing / invalid");
		}
		checkValidSite(siteId);

		// check instructor
		checkInstructor(siteId);
		
		//update the order
		this.businessService.updateAssignmentOrder(siteId, assignmentId, order);
	}
	
	/**
	 * Endpoint for getting the list of cells that have been edited.
	 * TODO enhance to accept a timestamp so we can filter the list
	 * This is designed to be polled on a regular basis so must be lightweight
	 * @param view
	 * @return
	 */
	@EntityCustomAction(action = "isotheruserediting", viewKey = EntityView.VIEW_LIST)
	public List<GbGradeCell> isAnotherUserEditing(EntityView view) {
		
		// get siteId
		String siteId = view.getPathSegment(2);
		
		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"Site ID must be set in order to access GBNG data.");
		}
		checkValidSite(siteId);

		// check instructor
		checkInstructor(siteId);
		
		// get notification list
		// NOTE we assume the gradebook id and siteid are equivalent, which they are
		// unless they have two gradebooks in a site? Is that even possible?
		return this.businessService.getEditingNotifications(siteId);
	}
	
	
	
	@EntityCustomAction(action = "categorized-assignment-order", viewKey = EntityView.VIEW_NEW)
	public void updateCategorizedAssignmentOrder(EntityReference ref, Map<String, Object> params) {

		// get params
		String siteId = (String) params.get("siteId");
		long assignmentId = NumberUtils.toLong((String) params.get("assignmentId"));
		int order = NumberUtils.toInt((String) params.get("order"));

		// check params supplied are valid 
		if (StringUtils.isBlank(siteId) || assignmentId == 0 || order < 0) {
			throw new IllegalArgumentException(
			"Request data was missing / invalid");
		}
		checkValidSite(siteId);

		// check instructor
		checkInstructor(siteId);

		//update the order
		try {
			this.businessService.updateCategorizedAssignmentOrder(siteId, assignmentId, order);
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Helper to check if the user is an instructor. Throws IllegalArgumentException if not.
	 * We don't currently need the value that this produces so we don't return it.
	 * 
	 * @param siteId
	 * @return
	 * @throws IdUnusedException
	 */
	private void checkInstructor(String siteId) {
		
		String currentUserId = this.getCurrentUserId();
		
		if(StringUtils.isBlank(currentUserId)) {
			throw new SecurityException("You must be logged in to access GBNG data");
		}
		
		if(this.businessService.getUserRole(siteId) != GbRole.INSTRUCTOR) {
			throw new SecurityException("You do not have instructor-type permissions in this site.");
		}
	}

	/**
	 * Helper to get current user id
	 * 
	 * @return
	 */
	private String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	/**
	 * Helper to check a site ID is valid. Throws IllegalArgumentException if not.
	 * We don't currently need the site that this produces so we don't return it.
	 * @param siteId
	 */
	@SuppressWarnings("unused")
	private void checkValidSite(String siteId) {
		try {
			Site site = this.siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("Invalid site id");
		}
	}

	@Setter
	private SiteService siteService;

	@Setter
	private SessionManager sessionManager;

	@Setter
	private SecurityService securityService;
	
	@Setter
	private GradebookNgBusinessService businessService;
	
}
