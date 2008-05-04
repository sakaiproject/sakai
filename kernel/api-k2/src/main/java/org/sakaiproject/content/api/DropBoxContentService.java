package org.sakaiproject.content.api;

public interface DropBoxContentService {
	/**
	 * Create the current site's dropbox collection and one for each qualified
	 * user that the current user can make.
	 */
	public void createDropboxCollection();

	/**
	 * Create the site's dropbox collection and one for each qualified user that
	 * the current user can make.
	 * 
	 * @param siteId
	 *            the Site id.
	 */
	public void createDropboxCollection(String siteId);

	/**
	 * Access the default dropbox collection id for the current request.<br />
	 * If the current user is a dropbox maintainer for the current site, return
	 * the site's dropbox area.<br />
	 * Otherwis return the current user's collection within the site's dropbox.
	 * 
	 * @return The default dropbox collection id for the current request.
	 */
	public String getDropboxCollection();

	/**
	 * Create an individual dropbox collection for the current user if the
	 * site-level dropbox exists and the current user has EVENT_DROPBOX_OWN
	 * permission for the site.
	 * 
	 * @param siteId
	 *            the Site id.
	 */
	public void createIndividualDropbox(String siteId);

	/**
	 * Access the default dropbox collection id for the current request.<br />
	 * If the current user is a dropbox maintainer for the current site, return
	 * the site's dropbox area.<br />
	 * Otherwis return the current user's collection within the site's dropbox.
	 * 
	 * @param siteId
	 *            The site id.
	 * @return The default dropbox collection id for the site.
	 */
	public String getDropboxCollection(String siteId);

	/**
	 * Determine whether the default dropbox collection id for this user in this
	 * site is the site's entire dropbox collection or just the current user's
	 * collection within the site's dropbox.
	 * 
	 * @return True if user sees all dropboxes in the site, false otherwise.
	 */
	public boolean isDropboxMaintainer();

	/**
	 * Determine whether the default dropbox collection id for this user in some
	 * site is the site's entire dropbox collection or just the current user's
	 * collection within the site's dropbox.
	 * 
	 * @return True if user sees all dropboxes in the site, false otherwise.
	 */
	public boolean isDropboxMaintainer(String siteId);

	/**
	 * Access the default dropbox collection display name for the current
	 * request. If the current user has permission to modify the site's dropbox
	 * collection, this is returned. Otherwise, the current user's collection
	 * within the site's dropbox is returned.
	 * 
	 * @return The default dropbox collection display name for the current
	 *         request.
	 */
	public String getDropboxDisplayName();

	/**
	 * Access the default dropbox collection display name for the site. If the
	 * current user has permission to modify the site's dropbox collection, this
	 * is returned. Otherwise, the current user's collection within the site's
	 * dropbox is returned.
	 * 
	 * @param siteId
	 *            the Site id.
	 * @return The default dropbox collection display name for the site.
	 */
	public String getDropboxDisplayName(String siteId);

	/**
	 * Check whether an id would identify an entity in a dropbox. Does not
	 * determine existence of the entity, just whether its id indicates it is a
	 * dropbox or contained within a dropbox.
	 * 
	 * @return true if the entity is a dropbox or in a dropbox, false otherwise.
	 */
	public boolean isInDropbox(String entityId);
	
	/**
	 * Access the name of the individual dropbox that contains a particular
	 * entity, or null if the entity is not inside an individual dropbox.
	 * 
	 * @param entityId
	 *            The id for an entity
	 * @return
	 */
	public String getIndividualDropboxId(String entityId);


}
