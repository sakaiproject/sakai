package org.sakaiproject.siteassociation.api;

import java.util.List;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public interface SiteAssocManager {

	/**
	 * 
	 * @param context
	 * @return
	 */
	public Site getSite(String context);
	
	/**
	 * 
	 * @param siteService
	 */
	public void setSiteService(SiteService siteService);
	
	/**
	 * Method to get a list of contexts from which associations have been made
	 * to the given context.
	 * 
	 * @param context
	 *            The context that associations have been made to.
	 * @return A list of contexts.
	 */
	public List<String> getAssociatedTo(String context);
	
	
	/**
	 * Method to get a list of contexts that the given context has been
	 * associated to.
	 * 
	 * @param context
	 *            The context that associations have been made from.
	 * @return A list of contexts.
	 */
	public List<String> getAssociatedFrom(String context);

	
	/**
	 * Method to create a unidirectional association from one context to
	 * another.
	 * 
	 * @param fromContext
	 *            The context from which to make the association.
	 * @param toContext
	 *            The context to make the association to.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action.
	 */
	public void addAssociation(String fromContext, String toContext);
	
	/**
	 * Method to remove the unidirectional association from one context to
	 * another.
	 * 
	 * @param fromContext
	 *            The context the association was made from.
	 * @param toContext
	 *            The context the association was made to.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action.
	 */
	public void removeAssociation(String fromContext, String toContext);
	
}
