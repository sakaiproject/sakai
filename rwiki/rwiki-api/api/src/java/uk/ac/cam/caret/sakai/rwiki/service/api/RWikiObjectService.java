/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.service.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;

import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiPermissions;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.VersionException;

// FIXME: Service

public interface RWikiObjectService extends EntityProducer, EntityTransferrer, ContextObserver
{

	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = RWikiObjectService.class.getName();

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = Entity.SEPARATOR + "wiki";

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_LABEL = "wiki";

	/** Name of the event when creating a resource. */
	public static final String EVENT_RESOURCE_ADD = "wiki.new";

	/** Name of the event when reading a resource. */
	public static final String EVENT_RESOURCE_READ = "wiki.read";

	/** Name of the event when writing a resource. */
	public static final String EVENT_RESOURCE_WRITE = "wiki.revise";

	/** Name of the event when removing a resource. */
	public static final String EVENT_RESOURCE_REMOVE = "wiki.delete";

	public static final String SMALL_CHANGE_IN_THREAD = "wiki.smallchange.request";

	/**
	 * Gets the current object
	 * 
	 * @param name
	 * @param realm
	 * @return
	 * @throws PermissionException
	 */
	RWikiCurrentObject getRWikiObject(String name, String realm)
			throws PermissionException;

	/**
	 * Gets the current object using a named template if it does not exist
	 * 
	 * @param name
	 * @param realm
	 *        the page space the page is in, used to localise and globalise the
	 *        name
	 * @param templateName
	 * @return
	 * @throws PermissionException
	 */
	RWikiCurrentObject getRWikiObject(String name, String realm,
			RWikiObject ignore, String templateName) throws PermissionException;

	/**
	 * Gets the object based on the ID. This
	 * 
	 * @param reference
	 *        the reference object
	 * @return
	 */
	RWikiCurrentObject getRWikiObject(RWikiObject reference);

	/**
	 * Search on current objects
	 * 
	 * @param criteria
	 * @param realm
	 * @return
	 * @throws PermissionException
	 */
	List search(String criteria, String realm) throws PermissionException;

	/**
	 * Update the named page, with permissions
	 * 
	 * @param name
	 * @param realm
	 * @param version
	 * @param content
	 * @param permissions
	 * @throws PermissionException
	 * @throws VersionException
	 */
	void update(String name, String realm, Date version, String content,
			RWikiPermissions permissions) throws PermissionException,
			VersionException;

	/**
	 * Update the name page, no permissions
	 * 
	 * @param name
	 * @param realm
	 * @param version
	 * @param content
	 * @throws PermissionException
	 * @throws VersionException
	 */
	void update(String name, String realm, Date version, String content)
			throws PermissionException, VersionException;

	/**
	 * Update the name page's permissions
	 * 
	 * @param name
	 * @param realm
	 * @param version
	 * @param permissions
	 * @throws PermissionException
	 * @throws VersionException
	 */
	void update(String name, String realm, Date version,
			RWikiPermissions permissions) throws PermissionException,
			VersionException;

	/**
	 * Does the page exist
	 * 
	 * @param name
	 *        A possibly non-globalised page name
	 * @param space
	 *        Default space to globalise to
	 * @return
	 */
	boolean exists(String name, String space);

	// SAK-2519
	/**
	 * A list of pages that have changed since (current versions)
	 * 
	 * @param since
	 * @param realm
	 * @return a list containing RWikiCurrentObjects
	 */
	List findChangedSince(Date since, String realm);

	/**
	 * Finds pages that reference the given page name
	 * 
	 * @param name
	 * @return a non-null list of page names not rwikiObjects
	 */
	List findReferencingPages(String name);

	/**
	 * Revert current revision to a named revision, creates a new revision
	 * 
	 * @param name
	 * @param realm
	 * @param version
	 * @param revision
	 */
	void revert(String name, String realm, Date version, int revision);

	/**
	 * Get a previous version
	 * 
	 * @param referenceObject
	 *        the Rwiki object whore rwikiobjectid field will be used to locate
	 *        the revision
	 * @param revision
	 * @return
	 */
	RWikiHistoryObject getRWikiHistoryObject(RWikiObject refernceObject,
			int revision);

	/**
	 * get a list of all previous versions as RWikiHistoryObjects
	 * 
	 * @param id
	 * @return
	 */
	List findRWikiHistoryObjects(RWikiObject reference);

	/**
	 * Finds the history objects sorted in reverse order
	 * 
	 * @param rwo
	 * @return
	 */
	List findRWikiHistoryObjectsInReverse(RWikiObject rwo);

	/**
	 * get list of subpages of the supplied page. The list will be
	 * alphabetiallcy sorted
	 * 
	 * @param globalParentPageName
	 *        is the page on which we want to find sub pages. THIS IS A GLOBAL
	 *        NAME. DONT CONFUSE WITH A LOCAL NAME
	 * @return a list of pages sorted by name alphabetically.
	 */
	List findRWikiSubPages(String globalParentPageName);

	/**
	 * Updates and creates a new comment on the page
	 * 
	 * @param name
	 * @param realm
	 * @param version
	 * @param content
	 * @throws PermissionException
	 * @throws VersionException
	 */
	void updateNewComment(String name, String realm, Date version,
			String content) throws PermissionException, VersionException;

	/**
	 * Create a list proxy based on the List and Object Proxy
	 * 
	 * @param commentsList
	 * @param lop
	 * @return
	 */
	List createListProxy(List commentsList, ObjectProxy lop);

	/**
	 * Creates a new rwiki Current Object according to the implementation
	 * 
	 * @return
	 */
	RWikiObject createNewRWikiCurrentObject();

	/**
	 * Creates a new RWiki Permissions Bean
	 * 
	 * @return
	 */
	RWikiPermissions createNewRWikiPermissionsImpl();

	/**
	 * fetches the entity based on the RWikiObject
	 * 
	 * @param rwo
	 * @return
	 */
	Entity getEntity(RWikiObject rwo);

	/**
	 * Fetches the Reference Object from the Entity manager based on the
	 * RWikiObject
	 * 
	 * @param rwo
	 * @return
	 */
	Reference getReference(RWikiObject rwo);

	/**
	 * A Map containing EntityHandlers for the Service, Each entity handler
	 * handles a subtype
	 * 
	 * @return
	 */
	Map getHandlers();

	/**
	 * Find all the changes under this point and under since the time specified
	 * 
	 * @param time
	 *        the time after which to consider changes
	 * @param basepath
	 *        the base path
	 * @return a list of RWikiCurrentObjects
	 */
	List findAllChangedSince(Date time, String basepath);

	/**
	 * Check for read permission
	 * 
	 * @param rwo
	 * @return
	 */
	boolean checkRead(RWikiObject rwo);

	/**
	 * check for update permission
	 * 
	 * @param rwo
	 * @return
	 */
	boolean checkUpdate(RWikiObject rwo);

	/**
	 * check for create permission
	 * 
	 * @param rwo
	 * @return
	 */
	boolean checkCreate(RWikiObject rwo);
	
	/**
	 * check for admin permission
	 * 
	 * @param rwo
	 * @return
	 */
	boolean checkAdmin(RWikiObject rwo);

	/**
	 * Find all pages in the database just reture
	 * 
	 * @return
	 */
	List findAllPageNames();

	/**
	 * generates a valid entity reference from the page name
	 * 
	 * @param pageName
	 * @return
	 */
	String createReference(String pageName);

	/**
	 * gets a component page link renderer
	 * 
	 * @param pageSpace
	 * @return
	 */
	PageLinkRenderer getComponentPageLinkRender(String pageSpace, boolean withBreadCrumb);

}
