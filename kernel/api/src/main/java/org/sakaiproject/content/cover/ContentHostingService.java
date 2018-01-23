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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.cover;

import java.util.Collection;
import java.util.Set;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;

import org.sakaiproject.exception.TypeException;

/**
 * <p>
 * ContentHostingService is a static Cover for the {@link org.sakaiproject.content.api.ContentHostingService ContentHostingService}; see that interface for usage details.
 * </p>
 * @deprecated Use Spring injection or ComponentManager instead of referencing this static cover.
 */
public class ContentHostingService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.content.api.ContentHostingService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.content.api.ContentHostingService) ComponentManager
						.get(org.sakaiproject.content.api.ContentHostingService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.content.api.ContentHostingService) ComponentManager
					.get(org.sakaiproject.content.api.ContentHostingService.class);
		}
	}

	private static org.sakaiproject.content.api.ContentHostingService m_instance = null;

	public static final String CONDITIONAL_ACCESS_LIST = org.sakaiproject.content.api.ContentHostingService.CONDITIONAL_ACCESS_LIST;


	public static java.lang.String APPLICATION_ID = org.sakaiproject.content.api.ContentHostingService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.content.api.ContentHostingService.REFERENCE_ROOT;

	public static java.lang.String EVENT_RESOURCE_ADD = org.sakaiproject.content.api.ContentHostingService.EVENT_RESOURCE_ADD;

	public static java.lang.String EVENT_RESOURCE_READ = org.sakaiproject.content.api.ContentHostingService.EVENT_RESOURCE_READ;

	public static java.lang.String EVENT_RESOURCE_WRITE = org.sakaiproject.content.api.ContentHostingService.EVENT_RESOURCE_WRITE;

	public static java.lang.String EVENT_RESOURCE_REMOVE = org.sakaiproject.content.api.ContentHostingService.EVENT_RESOURCE_REMOVE;

	public static java.lang.String EVENT_RESOURCE_AVAILABLE = org.sakaiproject.content.api.ContentHostingService.EVENT_RESOURCE_AVAILABLE;

	public static java.lang.String AUTH_RESOURCE_ADD = org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_ADD;
	public static java.lang.String AUTH_RESOURCE_READ = org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_READ;
	public static java.lang.String AUTH_RESOURCE_WRITE_ANY = org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_WRITE_ANY;
	public static java.lang.String AUTH_RESOURCE_WRITE_OWN = org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_WRITE_OWN;
	public static java.lang.String AUTH_RESOURCE_REMOVE_ANY = org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_REMOVE_ANY;
	public static java.lang.String AUTH_RESOURCE_REMOVE_OWN = org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_REMOVE_OWN;
	public static java.lang.String AUTH_RESOURCE_ALL_GROUPS = org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_ALL_GROUPS;
	public static java.lang.String AUTH_GROUP_RESOURCE_READ = org.sakaiproject.content.api.ContentHostingService.AUTH_GROUP_RESOURCE_READ;
	public static java.lang.String AUTH_RESOURCE_HIDDEN = org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_HIDDEN;
	public static java.lang.String AUTH_DROPBOX_OWN = org.sakaiproject.content.api.ContentHostingService.AUTH_DROPBOX_OWN;
	public static java.lang.String AUTH_DROPBOX_GROUPS = org.sakaiproject.content.api.ContentHostingService.AUTH_DROPBOX_GROUPS;
	public static java.lang.String AUTH_DROPBOX_MAINTAIN = org.sakaiproject.content.api.ContentHostingService.AUTH_DROPBOX_MAINTAIN;
	
	public static java.lang.String PROP_ALTERNATE_REFERENCE = org.sakaiproject.content.api.ContentHostingService.PROP_ALTERNATE_REFERENCE;

	public static int MAXIMUM_RESOURCE_ID_LENGTH = org.sakaiproject.content.api.ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH;
	
	public static java.lang.String getUrl(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getUrl(param0);
	}

	public static java.lang.String getUrl(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getUrl(param0, param1);
	}

	/**
	 * @param id
	 *        id of the resource to set the UUID for
	 * @param uuid
	 *        the new UUID of the resource
	 * @throws org.sakaiproject.exception.IdInvalidException
	 *         if the given resource already has a UUID set
	 */
	public static void setUuid(java.lang.String id, java.lang.String uuid) throws org.sakaiproject.exception.IdInvalidException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.setUuid(id, uuid);
	}

	public static java.lang.String getReference(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getReference(param0);
	}

	public static org.sakaiproject.content.api.ContentCollection getCollection(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.TypeException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getCollection(param0);
	}

	public static boolean allowAddCollection(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowAddCollection(param0);
	}

	/**
	 * @param param0
	 * @param param1
	 * @return
	 * @throws org.sakaiproject.exception.IdUsedException
	 * @throws org.sakaiproject.exception.IdInvalidException
	 * @throws org.sakaiproject.exception.PermissionException
	 * @throws org.sakaiproject.exception.InconsistentException
	 * @deprecated Suggest use of {@link #addCollection(String)} followed by {@link #getProperties(String)},
	 * 		and {@link #commitCollection(ContentCollectionEdit)}
	 */
	public static org.sakaiproject.content.api.ContentCollection addCollection(java.lang.String param0,
			org.sakaiproject.entity.api.ResourceProperties param1) throws org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InconsistentException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addCollection(param0, param1);
	}

	/**
	 * 
	 * @param param0
	 * @param param1
	 * @param param2
	 * @param param3
	 * @param param4
	 * @param param5
	 * @return
	 * @throws org.sakaiproject.exception.IdUsedException
	 * @throws org.sakaiproject.exception.IdInvalidException
	 * @throws org.sakaiproject.exception.PermissionException
	 * @throws org.sakaiproject.exception.InconsistentException
	 * @deprecated Suggest use of {@link #addCollection(String)} followed by {@link Entity#getProperties()},
	 * 		{@link GroupAwareEdit#setGroupAccess(Collection)}, {@link GroupAwareEdit#setAvailability(boolean, org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time)} 
	 * 		and {@link #commitCollection(ContentCollectionEdit)}
	 */
	public static org.sakaiproject.content.api.ContentCollection addCollection(java.lang.String param0,
			org.sakaiproject.entity.api.ResourceProperties param1, java.util.Collection param2, boolean param3, org.sakaiproject.time.api.Time param4, org.sakaiproject.time.api.Time param5) throws org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InconsistentException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addCollection(param0, param1, param2, param3, param4, param5);
	}

	public static org.sakaiproject.content.api.ContentCollectionEdit addCollection(java.lang.String param0)
			throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.InconsistentException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addCollection(param0);
	}

	public static boolean allowGetCollection(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowGetCollection(param0);
	}

	public static void checkCollection(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.checkCollection(param0);
	}

	public static java.util.List getAllResources(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getAllResources(param0);
	}

	public static java.util.List getAllEntities(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getAllEntities(param0);
	}

	public static boolean allowUpdateCollection(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowUpdateCollection(param0);
	}

	public static org.sakaiproject.content.api.ContentCollectionEdit editCollection(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.TypeException,
			org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.editCollection(param0);
	}

	public static boolean allowRemoveCollection(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveCollection(param0);
	}

	public static void removeCollection(org.sakaiproject.content.api.ContentCollectionEdit param0)
			throws org.sakaiproject.exception.TypeException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InconsistentException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.removeCollection(param0);
	}

	public static void removeCollection(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InUseException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.removeCollection(param0);
	}

	public static void commitCollection(org.sakaiproject.content.api.ContentCollectionEdit param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.commitCollection(param0);
	}

	public static void cancelCollection(org.sakaiproject.content.api.ContentCollectionEdit param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.cancelCollection(param0);
	}

	public static boolean allowAddResource(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowAddResource(param0);
	}

	public static org.sakaiproject.content.api.ContentResource addResource(java.lang.String param0, java.lang.String param1,
			byte[] param2, org.sakaiproject.entity.api.ResourceProperties param3, int param4)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.InconsistentException,
			org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addResource(param0, param1, param2, param3, param4);
	}

	public static org.sakaiproject.content.api.ContentResource addResource(java.lang.String param0, java.lang.String param1,
			int param2, java.lang.String param3, byte[] param4, org.sakaiproject.entity.api.ResourceProperties param5, int param6)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.InconsistentException, org.sakaiproject.exception.OverQuotaException,
			org.sakaiproject.exception.ServerOverloadException, org.sakaiproject.exception.IdUniquenessException,
			org.sakaiproject.exception.IdLengthException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addResource(param0, param1, param2, param3, param4, param5, param6);
	}

	public static org.sakaiproject.content.api.ContentResource addResource(java.lang.String param0, java.lang.String param1,
			byte[] param2, org.sakaiproject.entity.api.ResourceProperties param3, java.util.Collection param4, int param5)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.InconsistentException,
			org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addResource(param0, param1, param2, param3, param4, param5);
	}

	public static org.sakaiproject.content.api.ContentResource addResource(java.lang.String param0, java.lang.String param1,
			int param2, java.lang.String param3, byte[] param4, org.sakaiproject.entity.api.ResourceProperties param5,
			java.util.Collection param6, boolean param7, org.sakaiproject.time.api.Time param8, org.sakaiproject.time.api.Time param9, int param10)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.InconsistentException, org.sakaiproject.exception.OverQuotaException,
			org.sakaiproject.exception.ServerOverloadException, org.sakaiproject.exception.IdUniquenessException,
			org.sakaiproject.exception.IdLengthException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addResource(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
	}

	public static org.sakaiproject.content.api.ContentResourceEdit addResource(java.lang.String param0)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.InconsistentException,
			org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addResource(param0);
	}

	public static boolean allowAddAttachmentResource()
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowAddAttachmentResource();
	}

	public static boolean isAttachmentResource(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isAttachmentResource(param0);
	}

	public static org.sakaiproject.content.api.ContentResource addAttachmentResource(java.lang.String param0,
			java.lang.String param1, byte[] param2, org.sakaiproject.entity.api.ResourceProperties param3)
			throws org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.InconsistentException,
			org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addAttachmentResource(param0, param1, param2, param3);
	}

	public static org.sakaiproject.content.api.ContentResource addAttachmentResource(java.lang.String param0,
			java.lang.String param1, java.lang.String param2, java.lang.String param3, byte[] param4,
			org.sakaiproject.entity.api.ResourceProperties param5) throws org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.InconsistentException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.OverQuotaException,
			org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addAttachmentResource(param0, param1, param2, param3, param4, param5);
	}

	public static org.sakaiproject.content.api.ContentResourceEdit addAttachmentResource(java.lang.String param0)
			throws org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.InconsistentException,
			org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addAttachmentResource(param0);
	}

	public static boolean allowUpdateResource(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowUpdateResource(param0);
	}

	public static org.sakaiproject.content.api.ContentResource updateResource(java.lang.String param0, java.lang.String param1,
			byte[] param2) throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.InUseException,
			org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.updateResource(param0, param1, param2);
	}

	public static org.sakaiproject.content.api.ContentResourceEdit editResource(java.lang.String param0)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.editResource(param0);
	}

	public static boolean allowGetResource(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowGetResource(param0);
	}

	public static boolean allowRemoveResource(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveResource(param0);
	}

	public static void removeResource(org.sakaiproject.content.api.ContentResourceEdit param0)
			throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.removeResource(param0);
	}

	public static void removeResource(java.lang.String param0) throws org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.TypeException,
			org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.removeResource(param0);
	}

	public static boolean allowRename(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowRename(param0, param1);
	}

	public static boolean allowCopy(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowCopy(param0, param1);
	}

	public static void commitResource(org.sakaiproject.content.api.ContentResourceEdit param0)
			throws org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.ServerOverloadException, org.sakaiproject.antivirus.api.VirusFoundException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.commitResource(param0);
	}

	public static void commitResource(org.sakaiproject.content.api.ContentResourceEdit param0, int param1)
			throws org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.ServerOverloadException, org.sakaiproject.antivirus.api.VirusFoundException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.commitResource(param0, param1);
	}

	public static void cancelResource(org.sakaiproject.content.api.ContentResourceEdit param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.cancelResource(param0);
	}

	public static boolean allowGetProperties(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowGetProperties(param0);
	}

	public static boolean allowAddProperty(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowAddProperty(param0);
	}

	public static org.sakaiproject.entity.api.ResourceProperties addProperty(java.lang.String param0, java.lang.String param1,
			java.lang.String param2) throws org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.TypeException,
			org.sakaiproject.exception.InUseException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addProperty(param0, param1, param2);
	}

	public static boolean allowRemoveProperty(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveProperty(param0);
	}

	public static org.sakaiproject.entity.api.ResourceProperties removeProperty(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.InUseException,
			org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.removeProperty(param0, param1);
	}

	public static java.lang.String getContainingCollectionId(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getContainingCollectionId(param0);
	}

	public static boolean isRootCollection(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isRootCollection(param0);
	}

	public static org.sakaiproject.entity.api.ResourcePropertiesEdit newResourceProperties()
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.newResourceProperties();
	}

	public static java.lang.String getSiteCollection(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getSiteCollection(param0);
	}

	public static java.lang.String archiveResources(java.util.List param0, org.w3c.dom.Document param1, java.util.Stack param2,
			java.lang.String param3)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.archiveResources(param0, param1, param2, param3);
	}

	public static org.sakaiproject.content.api.ContentResource getResource(java.lang.String param0)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getResource(param0);
	}

	public static org.sakaiproject.entity.api.ResourceProperties getProperties(java.lang.String param0)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getProperties(param0);
	}

	public static String copy(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.InUseException,
			org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.copy(param0, param1);
	}

	public static String copyIntoFolder(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.IdLengthException, org.sakaiproject.exception.IdUniquenessException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.InUseException,
			org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.ServerOverloadException, org.sakaiproject.exception.InconsistentException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.copyIntoFolder(param0, param1);
	}

	public static void moveIntoFolder(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.InUseException,
			org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.InconsistentException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.moveIntoFolder(param0, param1);
	}

	/**
	 * @deprecated DO NOT USE THIS, it does not work and will ALWAYS throw an UnsupportedOperationException - https://jira.sakaiproject.org/browse/KNL-1078
	 */
	public static void rename(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.TypeException, org.sakaiproject.exception.InUseException,
			org.sakaiproject.exception.OverQuotaException, org.sakaiproject.exception.InconsistentException,
			org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.rename(param0, param1);
	}

	public static void checkResource(java.lang.String param0) throws org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.TypeException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.checkResource(param0);
	}

	public static int getDepth(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return 0;

		return service.getDepth(param0, param1);
	}

	public static java.lang.String merge(java.lang.String param0, org.w3c.dom.Element param1, java.lang.String param2,
			java.lang.String param3, java.util.Map param4, java.util.HashMap param5, java.util.Set param6)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.merge(param0, param1, param2, param3, param4, param5, param6);
	}

	public static java.lang.String getLabel()
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getLabel();
	}

	public static java.lang.String archive(java.lang.String param0, org.w3c.dom.Document param1, java.util.Stack param2,
			java.lang.String param3, java.util.List param4)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.archive(param0, param1, param2, param3, param4);
	}

	public static boolean isPubView(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isPubView(param0);
	}

	public static boolean isInheritingPubView(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isInheritingPubView(param0);
	}

	public static void setPubView(java.lang.String param0, boolean param1)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.setPubView(param0, param1);
	}

	public static String getUuid(String id)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getUuid(id);
	}

	public static String resolveUuid(String uuid)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.resolveUuid(uuid);
	}

	public static java.util.Collection getLocks(String id)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getLocks(id);
	}

	public static void lockObject(String id, String lockId, String subject, boolean system)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.lockObject(id, lockId, subject, system);
	}

	public static void removeLock(String id, String lockId)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.removeLock(id, lockId);
	}

	public static boolean isLocked(String id)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isLocked(id);
	}

	public static boolean containsLockedNode(String id)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.containsLockedNode(id);
	}

	public static void removeAllLocks(String id)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.removeAllLocks(id);
	}

	public static java.util.List findResources(String type, String primaryMimeType, String subMimeType,  Set<String> contextIds)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.findResources(type, primaryMimeType, subMimeType, contextIds);
	}
  
	public static java.util.List findResources(String type, String primaryMimeType, String subMimeType)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.findResources(type, primaryMimeType, subMimeType);
	}
  
	public static java.util.Map getCollectionMap()
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getCollectionMap();
	}

	public static void eliminateDuplicates(java.util.Collection param)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.eliminateDuplicates(param);
	}

	public static void createDropboxCollection()
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.createDropboxCollection();
	}

	public static void createDropboxCollection(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.createDropboxCollection(param0);
	}
	
	public static void createIndividualDropbox(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return;

		service.createIndividualDropbox(param0);
	}

	public static java.lang.String getDropboxCollection()
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getDropboxCollection();
	}

	public static java.lang.String getDropboxCollection(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getDropboxCollection(param0);
	}

	public static boolean isDropboxMaintainer()
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isDropboxMaintainer();
	}

	public static boolean isDropboxMaintainer(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isDropboxMaintainer(param0);
	}

	public static boolean isDropboxGroups(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isDropboxGroups(param0);
	}

	public static java.lang.String getDropboxDisplayName()
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getDropboxDisplayName();
	}

	public static java.lang.String getDropboxDisplayName(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getDropboxDisplayName(param0);
	}
	
	public static boolean isInDropbox(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isInDropbox(param0);
	}

	public static java.util.Comparator newContentHostingComparator(java.lang.String param0, boolean param1)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.newContentHostingComparator(param0, param1);
	}
	
	public static java.util.Collection getGroupsWithReadAccess(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getGroupsWithReadAccess(param0);
	}
	
	// getGroupsWithRemovePermission
	public static java.util.Collection getGroupsWithRemovePermission(java.lang.String param0)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getGroupsWithRemovePermission(param0);
	}

	public static java.util.Collection getGroupsWithAddPermission(java.lang.String param0) 
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getGroupsWithAddPermission(param0);
	}
	
	public static boolean isShortRefs() 
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isShortRefs();
	}

	public static boolean isAvailabilityEnabled() 
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isAvailabilityEnabled();
	}

	public static boolean isSortByPriorityEnabled() 
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isSortByPriorityEnabled();
	}
	
	public static boolean usingResourceTypeRegistry() 
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.usingResourceTypeRegistry();
	}

	/**
	 * @param entityId
	 * @return
	 */
	public static boolean isCollection(java.lang.String entityId)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isCollection(entityId);
	}

	public static org.sakaiproject.content.api.ContentResourceEdit addResource(java.lang.String collectionId, java.lang.String basename, java.lang.String extension, int maximum_tries) 
		throws org.sakaiproject.exception.PermissionException, 
			org.sakaiproject.exception.IdUniquenessException, 
			org.sakaiproject.exception.IdLengthException, 
			org.sakaiproject.exception.IdInvalidException, 
			org.sakaiproject.exception.IdUnusedException, 
			org.sakaiproject.exception.OverQuotaException, 
			org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addResource(collectionId, basename, extension, maximum_tries);
	}

	/**
	 * @param collectionId
	 * @param name
	 * @return
	 * @throws org.sakaiproject.exception.IdLengthException 
	 * @throws org.sakaiproject.exception.IdUnusedException 
	 * @throws org.sakaiproject.exception.TypeException 
	 */
	public static org.sakaiproject.content.api.ContentCollectionEdit addCollection(java.lang.String collectionId, java.lang.String name)
		throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
		org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException, 
		org.sakaiproject.exception.IdLengthException, org.sakaiproject.exception.TypeException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addCollection(collectionId, name);
	}

	/**
	 * @param collectionId
	 * @param name
	 * @param limit
	 * @return
	 * @throws org.sakaiproject.exception.IdLengthException
	 * @throws org.sakaiproject.exception.IdUnusedException
	 * @throws org.sakaiproject.exception.TypeException
	 */
	public static org.sakaiproject.content.api.ContentCollectionEdit addCollection(java.lang.String collectionId, java.lang.String name, int limit)
		throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
		org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException,
		org.sakaiproject.exception.IdLengthException, org.sakaiproject.exception.TypeException, org.sakaiproject.exception.IdUniquenessException
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.addCollection(collectionId, name, limit);
	}

   /**
    * gets the quota for a site collection or for a user's Home collection
    *
    * @param collection the collection on which to test for a quota.  this can be the collection for a site
    * or a user's workspace collection
    * @return the quota in kb
    */
    public static long getQuota(org.sakaiproject.content.api.ContentCollection collection)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return 0;

      return service.getQuota(collection);
	}

    /**
     * Access flag indicating whether ContentHostingHandlers are enabled in this content hosting service.
     * @return true if ContentHostingHandlers are enabled, false otherwise.
     */
    public static boolean isContentHostingHandlersEnabled()
    {
    	org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isContentHostingHandlersEnabled();
    }

    /**
     * Access the name of the individual dropbox that contains a particular entity, or null if the entity is not inside an individual dropbox.
     * @param entityId The id for an entity
     * @return
     */
	public static String getIndividualDropboxId(String entityId) 
	{
    	org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;

		return service.getIndividualDropboxId(entityId);
	}
	
	/**
	 * Retrieve a collection of ContentResource objects pf a particular resource-type.  The collection will 
	 * contain no more than the number of items specified as the pageSize, where pageSize is a non-negative 
	 * number less than or equal to 1028. The resources will be selected in ascending order by resource-id.
	 * If the resources of the specified resource-type in the ContentHostingService in ascending order by 
	 * resource-id are indexed from 0 to M and this method is called with parameters of N for pageSize and 
	 * I for page, the resources returned will be those with indexes (I*N) through ((I+1)*N - 1).  For example,
	 * if pageSize is 1028 and page is 0, the resources would be those with indexes of 0 to 1027.  
	 * This method finds the resources the current user has access to from a "page" of all resources
	 * of the specified type. If that page contains no resources the current user has access to, the 
	 * method returns an empty collection.  If the page does not exist (i.e. there are fewer than 
	 * ((page+1)*page_size) resources of the specified type), the method returns null.    
	 * @param resourceType
	 * @param pageSize
	 * @param page
	 * @return
	 */
	public static Collection<ContentResource> getResourcesOfType(String resourceType, int pageSize, int page)
	{
    	org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;
		
		return service.getResourcesOfType(resourceType, pageSize, page);
	}

	public static void restoreResource(String resourceId) throws PermissionException, IdUsedException, IdUnusedException, 
		IdInvalidException, InconsistentException, OverQuotaException, ServerOverloadException, TypeException, InUseException 
	{
    	org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return ;
		
		service.restoreResource(resourceId);
	}

	public static void removeDeletedResource(String resourceId) throws PermissionException, 
			IdUnusedException, TypeException, InUseException 
	{
    	org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return ;
		
		service.removeDeletedResource(resourceId);
	}

	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#isAvailable(String)
	 */
	public static boolean isAvailable(String entityId)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return false;

		return service.isAvailable(entityId);
	}

	public static Collection<ContentResource> getContextResourcesOfType(String resourceType, Set<String> contextIds)
	{
		org.sakaiproject.content.api.ContentHostingService service = getInstance();
		if (service == null) return null;
		
		return service.getContextResourcesOfType(resourceType, contextIds);
	}

}
