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

package org.sakaiproject.content.multiplex;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentResourceFilter;
import org.sakaiproject.content.api.providers.SiteContentAdvisor;
import org.sakaiproject.content.api.providers.SiteContentAdvisorProvider;
import org.sakaiproject.content.api.providers.SiteContentAdvisorTypeRegistry;
import org.sakaiproject.content.migration.api.RequestThreadServiceSwitcher;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A content hosting service multiplexer The defaultContentHosting service is
 * used by default, but if you replace this with a pushThreadBoundService() all
 * subsiquent calls to CHS in that thread will be bound to the new CHS, untill
 * the call stack unwinds back to the calling point. you should use the
 * construct
 * 
 * <pre>
 *     
 *     try {
 *        mchs.pushThreadBoundService(myService);
 *        .
 *        .
 *        .
 *      } finally {
 *        mchs.popThreadBoundService();
 *      }
 * </pre>
 * 
 * In addition this class implements the service multiplexer API, which has the
 * setService(Object) method. When this method is called the live service will
 * be changed to the supplied service. The replacement is performed at the start
 * of each request cycle on first use of the API and remains inforce for the
 * remainder of the request cycle.
 * 
 * @author ieb
 */
public class ContentHostingMultiplexService implements ContentHostingService,
		SiteContentAdvisorProvider, SiteContentAdvisorTypeRegistry,
		RequestThreadServiceSwitcher, CacheRefresher
{

	private static final Log log = LogFactory
			.getLog(ContentHostingMultiplexService.class);

	private static final String LIVE_SERVICE = ContentHostingMultiplexService.class
			.getName()
			+ "_liveService";

	private ThreadLocal<Stack<ContentHostingService>> selectedService = new ThreadLocal<Stack<ContentHostingService>>();

	private ContentHostingService defaultContentHostingService = null;

	private Map<String, ContentHostingService> contentHostingServices = null;

	private ThreadLocalManager threadLocalManager;

	/**
	 * The next content hosting service to use on each thread
	 */
	private ContentHostingService nextContentHostingService;

	/**
	 * The key in the content hosting services map
	 */
	private String defaultService;

	private EntityManager entityManager;

	/**
	 * get the current content hosting service
	 * 
	 * @return
	 */
	public ContentHostingService getService()
	{
		ContentHostingService liveService = getLiveService();
		Stack<ContentHostingService> s = selectedService.get();
		if (s == null)
		{
			s = new Stack<ContentHostingService>();
			selectedService.set(s);
		}
		if (s.size() == 0)
		{
			// push the live service onto the stack
			s.push(liveService);
		}
		else
		{
			// push the current service onto the stack.
			s.push(s.peek());
		}
		return s.peek();
	}

	/**
	 * gets the current live service bound to the thread, or binds the next live
	 * service to the thread.
	 * 
	 * @return
	 */
	private ContentHostingService getLiveService()
	{
		ContentHostingService liveService = (ContentHostingService) threadLocalManager
				.get(LIVE_SERVICE);
		if (liveService == null)
		{
			threadLocalManager.set(LIVE_SERVICE, nextContentHostingService);
			liveService = nextContentHostingService;
		}
		return liveService;
	}

	/**
	 * Push a service onto the stack
	 * 
	 * @param service
	 */
	public void pushThreadBoundService(ContentHostingService service)
	{
		Stack<ContentHostingService> s = selectedService.get();
		if (s == null)
		{
			s = new Stack<ContentHostingService>();
			selectedService.set(s);
		}
		s.push(service);
	}

	/**
	 * Pop a service from the stack
	 */
	public void popThreadBoundService()
	{
		Stack<ContentHostingService> s = selectedService.get();
		if (s == null)
		{
			s = new Stack<ContentHostingService>();
			selectedService.set(s);
		}
		if (s.size() > 0)
		{
			s.pop();
		}
		else
		{
			log.warn("Multiplexer Stack is empty, this should not happen ");
		}
	}

	/**
	 * 
	 */
	public ContentHostingMultiplexService()
	{
	}

	/**
	 * Initialise, locating the defailt service, and setting it to be the next
	 * live service.
	 */
	public void init()
	{
		int i = 0;
		defaultContentHostingService = contentHostingServices.get(defaultService);
		nextContentHostingService = defaultContentHostingService;
		log.info("Selected Default Service as " + nextContentHostingService);
		if (nextContentHostingService == null)
		{
			log.fatal("Content Hosting Service not found");
			throw new IllegalStateException("Content Hosting Service of type "
					+ defaultService + " not found");
		}
		entityManager.registerEntityProducer(this,
					ContentHostingService.REFERENCE_ROOT);
		invokeTargets("init", new Object[] {}, new Class[] {});
		
		entityManager.registerEntityProducer(this,
				ContentHostingService.REFERENCE_ROOT);
	}

	public void setContentHostingServices(
			Map<String, ContentHostingService> contentHostingServices)
	{
		this.contentHostingServices = contentHostingServices;
	}

	public Map<String, ContentHostingService> getContentHostingServices()
	{
		return contentHostingServices;
	}

	public void destroy()
	{
		invokeTargets("destroy", new Object[] {}, new Class[] {});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addAttachmentResource(java.lang.String)
	 */
	public ContentResourceEdit addAttachmentResource(String name)
			throws IdInvalidException, InconsistentException, IdUsedException,
			PermissionException, ServerOverloadException
	{
		try
		{
			return getService().addAttachmentResource(name);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addCollection(java.lang.String)
	 */
	public ContentCollectionEdit addCollection(String id) throws IdUsedException,
			IdInvalidException, PermissionException, InconsistentException
	{
		try
		{
			return getService().addCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String)
	 */
	public ContentResourceEdit addResource(String id) throws PermissionException,
			IdUsedException, IdInvalidException, InconsistentException,
			ServerOverloadException
	{
		try
		{
			return getService().addResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowAddAttachmentResource()
	 */
	public boolean allowAddAttachmentResource()
	{
		try
		{
			return getService().allowAddAttachmentResource();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowAddCollection(java.lang.String)
	 */
	public boolean allowAddCollection(String id)
	{
		try
		{
			return getService().allowAddCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowAddProperty(java.lang.String)
	 */
	public boolean allowAddProperty(String id)
	{
		try
		{
			return getService().allowAddProperty(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowAddResource(java.lang.String)
	 */
	public boolean allowAddResource(String id)
	{
		try
		{
			return getService().allowAddResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowCopy(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean allowCopy(String id, String new_id)
	{
		try
		{
			return getService().allowCopy(id, new_id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowGetCollection(java.lang.String)
	 */
	public boolean allowGetCollection(String id)
	{
		try
		{
			return getService().allowGetCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowGetProperties(java.lang.String)
	 */
	public boolean allowGetProperties(String id)
	{
		try
		{
			return getService().allowGetProperties(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowGetResource(java.lang.String)
	 */
	public boolean allowGetResource(String id)
	{
		try
		{
			return getService().allowGetResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowRemoveCollection(java.lang.String)
	 */
	public boolean allowRemoveCollection(String id)
	{
		try
		{
			return getService().allowRemoveCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowRemoveProperty(java.lang.String)
	 */
	public boolean allowRemoveProperty(String id)
	{
		try
		{
			return getService().allowRemoveProperty(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowRemoveResource(java.lang.String)
	 */
	public boolean allowRemoveResource(String id)
	{
		try
		{
			return getService().allowRemoveResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowRename(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean allowRename(String id, String new_id)
	{
		try
		{
			return getService().allowRename(id, new_id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowUpdateCollection(java.lang.String)
	 */
	public boolean allowUpdateCollection(String id)
	{
		try
		{
			return getService().allowUpdateCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#allowUpdateResource(java.lang.String)
	 */
	public boolean allowUpdateResource(String id)
	{
		try
		{
			return getService().allowUpdateResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#archiveResources(java.util.List,
	 *      org.w3c.dom.Document, java.util.Stack, java.lang.String)
	 */
	public String archiveResources(List resources, Document doc, Stack stack,
			String archivePath)
	{
		try
		{
			return getService().archiveResources(resources, doc, stack, archivePath);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#cancelCollection(org.sakaiproject.content.api.ContentCollectionEdit)
	 */
	public void cancelCollection(ContentCollectionEdit edit)
	{
		try
		{
			getService().cancelCollection(edit);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#cancelResource(org.sakaiproject.content.api.ContentResourceEdit)
	 */
	public void cancelResource(ContentResourceEdit edit)
	{
		try
		{
			getService().cancelResource(edit);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#checkCollection(java.lang.String)
	 */
	public void checkCollection(String id) throws IdUnusedException, TypeException,
			PermissionException
	{
		try
		{
			getService().checkCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#checkResource(java.lang.String)
	 */
	public void checkResource(String id) throws PermissionException, IdUnusedException,
			TypeException
	{
		try
		{
			getService().checkResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#commitCollection(org.sakaiproject.content.api.ContentCollectionEdit)
	 */
	public void commitCollection(ContentCollectionEdit edit)
	{
		try
		{
			getService().commitCollection(edit);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#commitResource(org.sakaiproject.content.api.ContentResourceEdit)
	 */
	public void commitResource(ContentResourceEdit edit) throws OverQuotaException,
			ServerOverloadException
	{
		try
		{
			getService().commitResource(edit);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#commitResource(org.sakaiproject.content.api.ContentResourceEdit,
	 *      int)
	 */
	public void commitResource(ContentResourceEdit edit, int priority)
			throws OverQuotaException, ServerOverloadException
	{
		try
		{
			getService().commitResource(edit, priority);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#containsLockedNode(java.lang.String)
	 */
	public boolean containsLockedNode(String id)
	{
		try
		{
			return getService().containsLockedNode(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#copy(java.lang.String,
	 *      java.lang.String)
	 */
	public String copy(String id, String new_id) throws PermissionException,
			IdUnusedException, TypeException, InUseException, OverQuotaException,
			IdUsedException, ServerOverloadException
	{
		try
		{
			return getService().copy(id, new_id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#copyIntoFolder(java.lang.String,
	 *      java.lang.String)
	 */
	public String copyIntoFolder(String id, String folder_id) throws PermissionException,
			IdUnusedException, TypeException, InUseException, OverQuotaException,
			IdUsedException, ServerOverloadException, InconsistentException,
			IdLengthException, IdUniquenessException
	{
		try
		{
			return getService().copyIntoFolder(id, folder_id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#createDropboxCollection()
	 */
	public void createDropboxCollection()
	{
		try
		{
			getService().createDropboxCollection();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#createDropboxCollection(java.lang.String)
	 */
	public void createDropboxCollection(String siteId)
	{
		try
		{
			getService().createDropboxCollection(siteId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#createIndividualDropbox(java.lang.String)
	 */
	public void createIndividualDropbox(String siteId)
	{
		try
		{
			getService().createIndividualDropbox(siteId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#editCollection(java.lang.String)
	 */
	public ContentCollectionEdit editCollection(String id) throws IdUnusedException,
			TypeException, PermissionException, InUseException
	{
		try
		{
			return getService().editCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#editResource(java.lang.String)
	 */
	public ContentResourceEdit editResource(String id) throws PermissionException,
			IdUnusedException, TypeException, InUseException
	{
		try
		{
			return getService().editResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#eliminateDuplicates(java.util.Collection)
	 */
	public void eliminateDuplicates(Collection resourceIds)
	{
		try
		{
			getService().eliminateDuplicates(resourceIds);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#findResources(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public List findResources(String type, String primaryMimeType, String subMimeType)
	{
		try
		{
			return getService().findResources(type, primaryMimeType, subMimeType);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getAllEntities(java.lang.String)
	 */
	public List getAllEntities(String id)
	{
		try
		{
			return getService().getAllEntities(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getAllResources(java.lang.String)
	 */
	public List getAllResources(String id)
	{
		try
		{
			return getService().getAllResources(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getCollection(java.lang.String)
	 */
	public ContentCollection getCollection(String id) throws IdUnusedException,
			TypeException, PermissionException
	{
		try
		{
			return getService().getCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getCollectionMap()
	 */
	public Map getCollectionMap()
	{
		try
		{
			return getService().getCollectionMap();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getCollectionSize(java.lang.String)
	 */
	public int getCollectionSize(String id) throws IdUnusedException, TypeException,
			PermissionException
	{
		try
		{
			return getService().getCollectionSize(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getContainingCollectionId(java.lang.String)
	 */
	public String getContainingCollectionId(String id)
	{
		try
		{
			return getService().getContainingCollectionId(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getDepth(java.lang.String,
	 *      java.lang.String)
	 */
	public int getDepth(String resourceId, String baseCollectionId)
	{
		try
		{
			return getService().getDepth(resourceId, baseCollectionId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getDropboxCollection()
	 */
	public String getDropboxCollection()
	{
		try
		{
			return getService().getDropboxCollection();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getDropboxCollection(java.lang.String)
	 */
	public String getDropboxCollection(String siteId)
	{
		try
		{
			return getService().getDropboxCollection(siteId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getDropboxDisplayName()
	 */
	public String getDropboxDisplayName()
	{
		try
		{
			return getService().getDropboxDisplayName();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getDropboxDisplayName(java.lang.String)
	 */
	public String getDropboxDisplayName(String siteId)
	{
		try
		{
			return getService().getDropboxDisplayName(siteId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getGroupsWithAddPermission(java.lang.String)
	 */
	public Collection getGroupsWithAddPermission(String collectionId)
	{
		try
		{
			return getService().getGroupsWithAddPermission(collectionId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getGroupsWithReadAccess(java.lang.String)
	 */
	public Collection getGroupsWithReadAccess(String collectionId)
	{
		try
		{
			return getService().getGroupsWithReadAccess(collectionId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getGroupsWithRemovePermission(java.lang.String)
	 */
	public Collection getGroupsWithRemovePermission(String collectionId)
	{
		try
		{
			return getService().getGroupsWithRemovePermission(collectionId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getLocks(java.lang.String)
	 */
	public Collection getLocks(String id)
	{
		try
		{
			return getService().getLocks(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getReference(java.lang.String)
	 */
	public String getReference(String id)
	{
		try
		{
			return getService().getReference(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getResource(java.lang.String)
	 */
	public ContentResource getResource(String id) throws PermissionException,
			IdUnusedException, TypeException
	{
		try
		{
			return getService().getResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getSiteCollection(java.lang.String)
	 */
	public String getSiteCollection(String siteId)
	{
		try
		{
			return getService().getSiteCollection(siteId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getUrl(java.lang.String)
	 */
	public String getUrl(String id)
	{
		try
		{
			return getService().getUrl(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getUrl(java.lang.String,
	 *      java.lang.String)
	 */
	public String getUrl(String id, String rootProperty)
	{
		try
		{
			return getService().getUrl(id, rootProperty);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getUuid(java.lang.String)
	 */
	public String getUuid(String id)
	{
		try
		{
			return getService().getUuid(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isAttachmentResource(java.lang.String)
	 */
	public boolean isAttachmentResource(String id)
	{
		try
		{
			return getService().isAttachmentResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isAvailabilityEnabled()
	 */
	public boolean isAvailabilityEnabled()
	{
		try
		{
			return getService().isAvailabilityEnabled();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isAvailable(java.lang.String)
	 */
	public boolean isAvailable(String entityId)
	{
		try
		{
			return getService().isAvailable(entityId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isCollection(java.lang.String)
	 */
	public boolean isCollection(String entityId)
	{
		try
		{
			return getService().isCollection(entityId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isDropboxMaintainer()
	 */
	public boolean isDropboxMaintainer()
	{
		try
		{
			return getService().isDropboxMaintainer();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isDropboxMaintainer(java.lang.String)
	 */
	public boolean isDropboxMaintainer(String siteId)
	{
		try
		{
			return getService().isDropboxMaintainer(siteId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isInDropbox(java.lang.String)
	 */
	public boolean isInDropbox(String entityId)
	{
		try
		{
			return getService().isInDropbox(entityId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isInheritingPubView(java.lang.String)
	 */
	public boolean isInheritingPubView(String id)
	{
		try
		{
			return getService().isInheritingPubView(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isLocked(java.lang.String)
	 */
	public boolean isLocked(String id)
	{
		try
		{
			return getService().isLocked(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isPubView(java.lang.String)
	 */
	public boolean isPubView(String id)
	{
		try
		{
			return getService().isPubView(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isRootCollection(java.lang.String)
	 */
	public boolean isRootCollection(String id)
	{
		try
		{
			return getService().isRootCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isShortRefs()
	 */
	public boolean isShortRefs()
	{
		try
		{
			return getService().isShortRefs();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isSortByPriorityEnabled()
	 */
	public boolean isSortByPriorityEnabled()
	{
		try
		{
			return getService().isSortByPriorityEnabled();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#lockObject(java.lang.String,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	public void lockObject(String id, String lockId, String subject, boolean system)
	{
		try
		{
			getService().lockObject(id, lockId, subject, system);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#moveIntoFolder(java.lang.String,
	 *      java.lang.String)
	 */
	public String moveIntoFolder(String id, String folder_id) throws PermissionException,
			IdUnusedException, TypeException, InUseException, OverQuotaException,
			IdUsedException, InconsistentException, ServerOverloadException
	{
		try
		{
			return getService().moveIntoFolder(id, folder_id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#newContentHostingComparator(java.lang.String,
	 *      boolean)
	 */
	public Comparator newContentHostingComparator(String property, boolean ascending)
	{
		try
		{
			return getService().newContentHostingComparator(property, ascending);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#removeAllLocks(java.lang.String)
	 */
	public void removeAllLocks(String id)
	{
		try
		{
			getService().removeAllLocks(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#removeCollection(java.lang.String)
	 */
	public void removeCollection(String id) throws IdUnusedException, TypeException,
			PermissionException, InUseException, ServerOverloadException
	{
		try
		{
			getService().removeCollection(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#removeCollection(org.sakaiproject.content.api.ContentCollectionEdit)
	 */
	public void removeCollection(ContentCollectionEdit edit) throws TypeException,
			PermissionException, InconsistentException, ServerOverloadException
	{
		try
		{
			getService().removeCollection(edit);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#removeLock(java.lang.String,
	 *      java.lang.String)
	 */
	public void removeLock(String id, String lockId)
	{
		try
		{
			getService().removeLock(id, lockId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#removeResource(java.lang.String)
	 */
	public void removeResource(String id) throws PermissionException, IdUnusedException,
			TypeException, InUseException
	{
		try
		{
			getService().removeResource(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#removeResource(org.sakaiproject.content.api.ContentResourceEdit)
	 */
	public void removeResource(ContentResourceEdit edit) throws PermissionException
	{
		try
		{
			getService().removeResource(edit);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#rename(java.lang.String,
	 *      java.lang.String)
	 */
	public String rename(String id, String new_id) throws PermissionException,
			IdUnusedException, TypeException, InUseException, OverQuotaException,
			InconsistentException, IdUsedException, ServerOverloadException
	{
		try
		{
			return getService().rename(id, new_id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#resolveUuid(java.lang.String)
	 */
	public String resolveUuid(String uuid)
	{
		try
		{
			return getService().resolveUuid(uuid);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#setPubView(java.lang.String,
	 *      boolean)
	 */
	public void setPubView(String id, boolean pubview)
	{
		try
		{
			getService().setPubView(id, pubview);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#setUuid(java.lang.String,
	 *      java.lang.String)
	 */
	public void setUuid(String id, String uuid) throws IdInvalidException
	{
		try
		{
			getService().setUuid(id, uuid);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#updateResource(java.lang.String,
	 *      java.lang.String, byte[])
	 */
	public ContentResource updateResource(String id, String type, byte[] content)
			throws PermissionException, IdUnusedException, TypeException, InUseException,
			OverQuotaException, ServerOverloadException
	{
		try
		{
			return getService().updateResource(id, type, content);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#usingResourceTypeRegistry()
	 */
	public boolean usingResourceTypeRegistry()
	{
		try
		{
			return getService().usingResourceTypeRegistry();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addAttachmentResource(java.lang.String,
	 *      java.lang.String, byte[],
	 *      org.sakaiproject.entity.api.ResourceProperties)
	 */
	public ContentResource addAttachmentResource(String name, String type,
			byte[] content, ResourceProperties properties) throws IdInvalidException,
			InconsistentException, IdUsedException, PermissionException,
			OverQuotaException, ServerOverloadException
	{
		try
		{
			return getService().addAttachmentResource(name, type, content, properties);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addAttachmentResource(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, byte[],
	 *      org.sakaiproject.entity.api.ResourceProperties)
	 */
	public ContentResource addAttachmentResource(String name, String site, String tool,
			String type, byte[] content, ResourceProperties properties)
			throws IdInvalidException, InconsistentException, IdUsedException,
			PermissionException, OverQuotaException, ServerOverloadException
	{
		try
		{
			return getService().addAttachmentResource(name, site, tool, type, content,
					properties);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addCollection(java.lang.String,
	 *      org.sakaiproject.entity.api.ResourceProperties)
	 */
	public ContentCollection addCollection(String id, ResourceProperties properties)
			throws IdUsedException, IdInvalidException, PermissionException,
			InconsistentException
	{
		try
		{
			return getService().addCollection(id, properties);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addCollection(java.lang.String,
	 *      org.sakaiproject.entity.api.ResourceProperties,
	 *      java.util.Collection)
	 */
	public ContentCollection addCollection(String id, ResourceProperties properties,
			Collection groups) throws IdUsedException, IdInvalidException,
			PermissionException, InconsistentException
	{
		try
		{
			return getService().addCollection(id, properties, groups);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addCollection(java.lang.String,
	 *      org.sakaiproject.entity.api.ResourceProperties,
	 *      java.util.Collection, boolean, org.sakaiproject.time.api.Time,
	 *      org.sakaiproject.time.api.Time)
	 */
	public ContentCollection addCollection(String id, ResourceProperties properties,
			Collection groups, boolean hidden, Time releaseDate, Time retractDate)
			throws IdUsedException, IdInvalidException, PermissionException,
			InconsistentException
	{
		try
		{
			return getService().addCollection(id, properties, groups, hidden,
					releaseDate, retractDate);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addProperty(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public ResourceProperties addProperty(String id, String name, String value)
			throws PermissionException, IdUnusedException, TypeException, InUseException,
			ServerOverloadException
	{
		try
		{
			return getService().addProperty(id, name, value);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String,
	 *      java.lang.String, byte[],
	 *      org.sakaiproject.entity.api.ResourceProperties, int)
	 */
	public ContentResource addResource(String id, String type, byte[] content,
			ResourceProperties properties, int priority) throws PermissionException,
			IdUsedException, IdInvalidException, InconsistentException,
			OverQuotaException, ServerOverloadException
	{
		try
		{
			return getService().addResource(id, type, content, properties, priority);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String,
	 *      java.lang.String, int, java.lang.String, byte[],
	 *      org.sakaiproject.entity.api.ResourceProperties, int)
	 */
	public ContentResource addResource(String name, String collectionId, int limit,
			String type, byte[] content, ResourceProperties properties, int priority)
			throws PermissionException, IdUniquenessException, IdLengthException,
			IdInvalidException, InconsistentException, IdLengthException,
			OverQuotaException, ServerOverloadException
	{
		try
		{
			return getService().addResource(name, collectionId, limit, type, content,
					properties, priority);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String,
	 *      java.lang.String, byte[],
	 *      org.sakaiproject.entity.api.ResourceProperties,
	 *      java.util.Collection, int)
	 */
	public ContentResource addResource(String id, String type, byte[] content,
			ResourceProperties properties, Collection groups, int priority)
			throws PermissionException, IdUsedException, IdInvalidException,
			InconsistentException, OverQuotaException, ServerOverloadException
	{
		try
		{
			return getService().addResource(id, type, content, properties, groups,
					priority);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String,
	 *      java.lang.String, int, java.lang.String, byte[],
	 *      org.sakaiproject.entity.api.ResourceProperties,
	 *      java.util.Collection, int)
	 */
	public ContentResource addResource(String name, String collectionId, int limit,
			String type, byte[] content, ResourceProperties properties,
			Collection groups, int priority) throws PermissionException,
			IdUniquenessException, IdLengthException, IdInvalidException,
			InconsistentException, IdLengthException, OverQuotaException,
			ServerOverloadException
	{
		try
		{
			return getService().addResource(name, collectionId, limit, type, content,
					properties, groups, priority);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String,
	 *      java.lang.String, int, java.lang.String, byte[],
	 *      org.sakaiproject.entity.api.ResourceProperties,
	 *      java.util.Collection, boolean, org.sakaiproject.time.api.Time,
	 *      org.sakaiproject.time.api.Time, int)
	 */
	public ContentResource addResource(String name, String collectionId, int limit,
			String type, byte[] content, ResourceProperties properties,
			Collection groups, boolean hidden, Time releaseDate, Time retractDate,
			int priority) throws PermissionException, IdUniquenessException,
			IdLengthException, IdInvalidException, InconsistentException,
			IdLengthException, OverQuotaException, ServerOverloadException
	{
		try
		{
			return getService().addResource(name, collectionId, limit, type, content,
					properties, groups, hidden, releaseDate, retractDate, priority);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getProperties(java.lang.String)
	 */
	public ResourceProperties getProperties(String id) throws PermissionException,
			IdUnusedException
	{
		try
		{
			return getService().getProperties(id);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#newResourceProperties()
	 */
	public ResourcePropertiesEdit newResourceProperties()
	{
		try
		{
			return getService().newResourceProperties();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#removeProperty(java.lang.String,
	 *      java.lang.String)
	 */
	public ResourceProperties removeProperty(String id, String name)
			throws PermissionException, IdUnusedException, TypeException, InUseException,
			ServerOverloadException
	{
		try
		{
			return getService().removeProperty(id, name);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#archive(java.lang.String,
	 *      org.w3c.dom.Document, java.util.Stack, java.lang.String,
	 *      java.util.List)
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath,
			List attachments)
	{
		try
		{
			return getService().archive(siteId, doc, stack, archivePath, attachments);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntity(org.sakaiproject.entity.api.Reference)
	 */
	public Entity getEntity(Reference ref)
	{
		try
		{
			return getService().getEntity(ref);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityAuthzGroups(org.sakaiproject.entity.api.Reference,
	 *      java.lang.String)
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		try
		{
			return getService().getEntityAuthzGroups(ref, userId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityDescription(org.sakaiproject.entity.api.Reference)
	 */
	public String getEntityDescription(Reference ref)
	{
		try
		{
			return getService().getEntityDescription(ref);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		try
		{
			return getService().getEntityResourceProperties(ref);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityUrl(org.sakaiproject.entity.api.Reference)
	 */
	public String getEntityUrl(Reference ref)
	{
		try
		{
			return getService().getEntityUrl(ref);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getHttpAccess()
	 */
	public HttpAccess getHttpAccess()
	{
		try
		{
			return getService().getHttpAccess();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getLabel()
	 */
	public String getLabel()
	{
		try
		{
			return getService().getLabel();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#merge(java.lang.String,
	 *      org.w3c.dom.Element, java.lang.String, java.lang.String,
	 *      java.util.Map, java.util.Map, java.util.Set)
	 */
	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		try
		{
			return getService().merge(siteId, root, archivePath, fromSiteId,
					attachmentNames, userIdTrans, userListAllowImport);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#parseEntityReference(java.lang.String,
	 *      org.sakaiproject.entity.api.Reference)
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		try
		{
			return getService().parseEntityReference(reference, ref);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#willArchiveMerge()
	 */
	public boolean willArchiveMerge()
	{
		try
		{
			return getService().willArchiveMerge();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addCollection(java.lang.String,
	 *      java.lang.String)
	 */
	public ContentCollectionEdit addCollection(String collectionId, String name)
			throws PermissionException, IdUnusedException, IdUsedException,
			IdLengthException, IdInvalidException, TypeException
	{
		try
		{
			return getService().addCollection(collectionId, name);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String,
	 *      java.lang.String, java.lang.String, int)
	 */
	public ContentResourceEdit addResource(String collectionId, String basename,
			String extension, int maximum_tries) throws PermissionException,
			IdUniquenessException, IdLengthException, IdInvalidException,
			IdUnusedException, OverQuotaException, ServerOverloadException
	{
		try
		{
			return getService().addResource(collectionId, basename, extension,
					maximum_tries);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getQuota(org.sakaiproject.content.api.ContentCollection)
	 */
	public long getQuota(ContentCollection collection)
	{
		try
		{
			return getService().getQuota(collection);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getIndividualDropboxId(java.lang.String)
	 */
	public String getIndividualDropboxId(String entityId)
	{
		try
		{
			return getService().getIndividualDropboxId(entityId);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#getResourcesOfType(java.lang.String,
	 *      int, int)
	 */
	public Collection<ContentResource> getResourcesOfType(String resourceType,
			int pageSize, int page)
	{
		try
		{
			return getService().getResourcesOfType(resourceType, pageSize, page);
		}
		finally
		{
			popThreadBoundService();
		}
	}
   
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingService#getContextResourcesOfType(java.lang.String, java.util.Set<java.lang.String>)
	 */
	public Collection<ContentResource> getContextResourcesOfType(String resourceType, Set<String> contextIds) {
		try
		{
			return getService().getContextResourcesOfType(resourceType, contextIds);
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingService#isContentHostingHandlersEnabled()
	 */
	public boolean isContentHostingHandlersEnabled()
	{
		try
		{
			return getService().isContentHostingHandlersEnabled();
		}
		finally
		{
			popThreadBoundService();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setAllowGroupResources(boolean)
	 */
	public void setAllowGroupResources(boolean allowGroupResources)
	{
		invokeTargets("setAllowGroupResources", new Object[] { allowGroupResources },
				new Class[] { boolean.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setAutoDdl(java.lang.String)
	 */
	public void setAutoDdl(String value)
	{
		invokeTargets("setAutoDdl", new Object[] { value }, new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setAvailabilityChecksEnabled(boolean)
	 */
	public void setAvailabilityChecksEnabled(boolean value)
	{
		invokeTargets("setAvailabilityChecksEnabled", new Object[] { value },
				new Class[] { boolean.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setBodyPath(java.lang.String)
	 */
	public void setBodyPath(String value)
	{
		invokeTargets("setBodyPath", new Object[] { value }, new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setBodyVolumes(java.lang.String)
	 */
	public void setBodyVolumes(String value)
	{
		invokeTargets("setBodyVolumes", new Object[] { value },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setCaching(java.lang.String)
	 */
	public void setCaching(String value)
	{
		invokeTargets("setCaching", new Object[] { value }, new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setCollectionTableName(java.lang.String)
	 */
	public void setCollectionTableName(String name)
	{
		invokeTargets("setCollectionTableName", new Object[] { name },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setContentServiceSql(java.lang.String)
	 */
	public void setContentServiceSql(String vendor)
	{
		invokeTargets("setContentServiceSql", new Object[] { vendor },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setConvertToContextQueryForCollectionSize(boolean)
	 */
	public void setConvertToContextQueryForCollectionSize(
			boolean convertToContextQueryForCollectionSize)
	{
		invokeTargets("setConvertToContextQueryForCollectionSize",
				new Object[] { convertToContextQueryForCollectionSize },
				new Class[] { boolean.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setConvertToFile(java.lang.String)
	 */
	public void setConvertToFile(String value)
	{
		invokeTargets("setConvertToFile", new Object[] { value },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setEntityGroupTableName(java.lang.String)
	 */
	public void setEntityGroupTableName(String name)
	{
		invokeTargets("setEntityGroupTableName", new Object[] { name },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setLocksInDb(java.lang.String)
	 */
	public void setLocksInDb(String value)
	{
		invokeTargets("setLocksInDb", new Object[] { value },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setMigrateData(boolean)
	 */
	public void setMigrateData(boolean migrateData)
	{
		invokeTargets("setMigrateData", new Object[] { migrateData },
				new Class[] { boolean.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setPrioritySortEnabled(boolean)
	 */
	public void setPrioritySortEnabled(boolean value)
	{
		invokeTargets("setPrioritySortEnabled", new Object[] { value },
				new Class[] { boolean.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setResourceBodyDeleteTableName(java.lang.String)
	 */
	public void setResourceBodyDeleteTableName(String name)
	{
		invokeTargets("setResourceBodyDeleteTableName", new Object[] { name },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setResourceBodyTableName(java.lang.String)
	 */
	public void setResourceBodyTableName(String name)
	{
		invokeTargets("setResourceBodyTableName", new Object[] { name },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setResourceDeleteTableName(java.lang.String)
	 */
	public void setResourceDeleteTableName(String name)
	{
		invokeTargets("setResourceDeleteTableName", new Object[] { name },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setResourceTableName(java.lang.String)
	 */
	public void setResourceTableName(String name)
	{
		invokeTargets("setResourceTableName", new Object[] { name },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setShortRefs(java.lang.String)
	 */
	public void setShortRefs(String value)
	{
		invokeTargets("setShortRefs", new Object[] { value },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setSiteAlias(java.lang.String)
	 */
	public void setSiteAlias(String value)
	{
		invokeTargets("setSiteAlias", new Object[] { value },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setSiteAttachments(java.lang.String)
	 */
	public void setSiteAttachments(String value)
	{
		invokeTargets("setSiteAttachments", new Object[] { value },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setSiteQuota(java.lang.String)
	 */
	public void setSiteQuota(String quota)
	{
		invokeTargets("setSiteQuota", new Object[] { quota },
				new Class[] { String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setUseContextQueryForCollectionSize(boolean)
	 */
	public void setUseContextQueryForCollectionSize(
			boolean useContextQueryForCollectionSize)
	{
		invokeTargets("setUseContextQueryForCollectionSize",
				new Object[] { useContextQueryForCollectionSize },
				new Class[] { boolean.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#setUseResourceTypeRegistry(boolean)
	 */
	public void setUseResourceTypeRegistry(boolean useRegistry)
	{
		invokeTargets("setUseResourceTypeRegistry", new Object[] { useRegistry },
				new Class[] { boolean.class });
	}

	/**
	 * @param string
	 * @param useRegistry
	 */
	private void invokeTargets(String method, Object[] args, Class[] c)
	{

		for (ContentHostingService chs : contentHostingServices.values())
		{

			try
			{

				chs.getClass().getMethod(method, c).invoke(chs, args);
				if (log.isDebugEnabled() && args.length > 0)
				{
					log.debug(">>>>>>>>>>>>>>Done " + chs + "." + method + "(" + args[0]
							+ ")");
				}
			}
			catch (IllegalArgumentException e)
			{
				log.info("Method " + method
						+ " failed to invoke with an argument exception, ignoring");
			}
			catch (SecurityException e)
			{
				log.info("Method " + method
						+ " failed to invoke with an security exception, ignoring");
			}
			catch (IllegalAccessException e)
			{
				log
						.info("Method "
								+ method
								+ " failed to invoke with an illeagal access exception, ignoring");
			}
			catch (InvocationTargetException e)
			{
				log.info("Method " + method
						+ " failed to invoke with a target exception, ignoring");
			}
			catch (NoSuchMethodException e)
			{
				log.info("Method " + method + " does not exist on " + chs + ", ignoring "
						+ e.getMessage());
			}
		}

	}

	/**
	 * @return the threadLocalManager
	 */
	public ThreadLocalManager getThreadLocalManager()
	{
		return threadLocalManager;
	}

	/**
	 * @param threadLocalManager
	 *        the threadLocalManager to set
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager)
	{
		this.threadLocalManager = threadLocalManager;
	}

	/**
	 * @return the defaultService
	 */
	public String getDefaultService()
	{
		return defaultService;
	}

	/**
	 * @param defaultService
	 *        the defaultService to set
	 */
	public void setDefaultService(String defaultService)
	{
		setNextService(defaultService);
	}

	public String getNextService()
	{
		return defaultService;
	}

	public void setNextService(String nextService)
	{
		if (defaultService != null)
		{
			if (nextService == null)
			{
				nextContentHostingService = defaultContentHostingService;
			}
			else
			{
				if (!defaultService.equals(nextService))
				{
					ContentHostingService chsnew = contentHostingServices
							.get(nextService);
					if (chsnew == null)
					{
						throw new IllegalStateException(
								"Content Hosting Service of type " + nextService
										+ " not found in the multiplexer");
					}
					nextContentHostingService = chsnew;
				}
			}
		}
		defaultService = nextService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.providers.SiteContentAdvisorTypeRegistry#registerSiteContentAdvisorProvidor(org.sakaiproject.content.api.providers.SiteContentAdvisorProvider,
	 *      java.lang.String)
	 */
	public void registerSiteContentAdvisorProvidor(SiteContentAdvisorProvider advisor,
			String type)
	{
		for (ContentHostingService chs : contentHostingServices.values())
		{
			if (chs instanceof SiteContentAdvisorTypeRegistry)
			{
				((SiteContentAdvisorTypeRegistry) chs)
						.registerSiteContentAdvisorProvidor(advisor, type);
			}
		}
		invokeTargets("registerSiteContentAdvisorProvidor",
				new Object[] { advisor, type }, new Class[] {
						SiteContentAdvisorProvider.class, String.class });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.SiteContentAdvisorProvider#getContentAdvisor(org.sakaiproject.site.api.Site)
	 */
	public SiteContentAdvisor getContentAdvisor(Site site)
	{
		try
		{
			ContentHostingService chs = getService();
			if (chs instanceof SiteContentAdvisorProvider)
			{
				return ((SiteContentAdvisorProvider) chs).getContentAdvisor(site);
			}
			return null;
		}
		finally
		{
			popThreadBoundService();
		}

	}
	
	public Object refresh(Object key, Object oldValue, Event event) {
		try
		{
			ContentHostingService chs = getService();
			if (chs instanceof CacheRefresher)
			{
				return ((CacheRefresher) chs).refresh( key,  oldValue,  event);
			}
			return null;
		}
		finally
		{
			popThreadBoundService();
		}
		
	}

	/**
	 * @return the entityManager
	 */
	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	/**
	 * @param entityManager the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

	
}
