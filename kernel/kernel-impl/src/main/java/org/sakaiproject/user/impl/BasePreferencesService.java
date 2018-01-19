/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.user.impl;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.*;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.SingleStorageUser;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * BasePreferencesService is a Sakai Preferences implementation.
 * </p>
 */
@Slf4j
public abstract class BasePreferencesService implements PreferencesService, SingleStorageUser
{
	/**
	 * Key used to store the locale preferences
	 */
	private static final String LOCALE_PREFERENCE_KEY = "sakai:resourceloader";
	/** Storage manager for this service. */
	protected Storage m_storage = null;
	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;
	/** the cache for Preference objects **/
	private Cache<String, BasePreferences> m_cache;
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct storage for this service.
	 */
	protected abstract Storage newStorage();

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService().getAccessUrl()) + m_relativeAccessPoint;
	}

	/**
	 * @inheritDoc
	 */
	public String preferencesReference(String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + id;
	}

	/**
	 * Access the preferences id extracted from a preferences reference.
	 * 
	 * @param ref
	 *        The preferences reference string.
	 * @return The the preferences id extracted from a preferences reference.
	 */
	protected String preferencesId(String ref)
	{
		String start = getAccessPoint(true) + Entity.SEPARATOR;
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowd, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!securityService().unlock(lock, resource))
		{
			return false;
		}

		return true;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	protected void unlock(String lock, String resource) throws PermissionException
	{
		if (!unlockCheck(lock, resource))
		{
			throw new PermissionException(sessionManager().getCurrentSessionUserId(), lock, resource);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected abstract FunctionManager functionManager();

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			m_relativeAccessPoint = REFERENCE_ROOT;

			// construct storage and read
			m_storage = newStorage();
			m_storage.open();

			// register as an entity producer
			entityManager().registerEntityProducer(this, REFERENCE_ROOT);

			// register functions
			functionManager().registerFunction(SECURE_ADD_PREFS);
			functionManager().registerFunction(SECURE_EDIT_PREFS);
			functionManager().registerFunction(SECURE_REMOVE_PREFS);

			
			//register a cache
			m_cache = memoryService().getCache(BasePreferencesService.class.getName() +".preferences");
			
			log.info("init()");
		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_storage.close();
		m_storage = null;

		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * PreferencesService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Preferences getPreferences(String id)
	{
		Preferences prefs = findPreferences(id);

		// if not found at all
		if (prefs == null)
		{
			// throwaway empty preferences %%%
			prefs = new BasePreferences(id);
		}

		return prefs;
	}

	/**
	 * @inheritDoc
	 */
	public PreferencesEdit edit(String id) throws PermissionException, InUseException, IdUnusedException
	{
		// security
		unlock(SECURE_EDIT_PREFS, preferencesReference(id));

		// check for existance
		if (!m_storage.check(id))
		{
			//Try to add and return this value
			try {
				return add(id);
			}
			catch (IdUsedException e) {
				//This should never happen
				log.warn("Could not add "+id+" even after checking that it didn't exist in storage. Throwing IdUnusedException but this shouldn't be possible.",e);
				throw new IdUnusedException(id);
			
			}
		}

		// ignore the cache - get the user with a lock from the info store
		PreferencesEdit edit = m_storage.edit(id);
		if (edit == null) throw new InUseException(id);

		((BasePreferences) edit).setEvent(SECURE_EDIT_PREFS);

		return edit;
	}

	/**
	 * @inheritDoc
	 */
	public void commit(PreferencesEdit edit)
	{
		if (edit != null)
		{
			// check for closed edit
			if (!edit.isActiveEdit())
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					log.warn("commit(): closed PreferencesEdit", e);
				}
				return;
			}

			// update the properties
			// addLiveUpdateProperties(user.getPropertiesEdit());
			
			//invalidate the cache
			m_cache.remove(edit.getId());
				
			// complete the edit
			m_storage.commit(edit);
		
			SessionManager sManager = sessionManager();
			Session s = sManager.getCurrentSession();
		
			// track it
			eventTrackingService()
					.post(eventTrackingService().newEvent(((BasePreferences) edit).getEvent(), edit.getReference(), true));

			// close the edit object
			((BasePreferences) edit).closeEdit();
		}
	}

	/**
	 * @inheritDoc
	 */
	public void cancel(PreferencesEdit edit)
	{
		if (edit != null)
		{
			// if this was an add, remove it
			if (SECURE_ADD_PREFS.equals(((BasePreferences) edit).m_event))
			{
				remove(edit);
			}
			else
			{
				// check for closed edit
				if (!edit.isActiveEdit())
				{
					try
					{
						throw new Exception();
					}
					catch (Exception e)
					{
						log.warn("cancel(): closed PreferencesEdit", e);
					}
					return;
				}

				// release the edit lock
				m_storage.cancel(edit);

				// close the edit object
				((BasePreferences) edit).closeEdit();
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(PreferencesEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn("remove(): closed PreferencesEdit", e);
			}
			return;
		}

		// complete the edit
		m_storage.remove(edit);
		
		m_cache.remove(edit.getId());

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_PREFS, edit.getReference(), true));

		// close the edit object
		((BasePreferences) edit).closeEdit();
	}

	/**
	 * Find the preferences object in cache or storage.
	 * 
	 * @param id
	 *        The preferences id.
	 * @return The preferences object found in cache or storage, or null if not found.
	 */
	protected BasePreferences findPreferences(String id)
	{
		if (id == null) {
			return null;
		}

		// Try the cache
		BasePreferences prefs = m_cache.get(id);

		// Failing that, try the storage
		if (prefs == null) {
			prefs = (BasePreferences) m_storage.get(id);
		}

		if (prefs != null) {
			m_cache.put(id, prefs);
		}
		
		return prefs;
	}
	
	
	/**
	 ** Get user's preferred locale (or null if not set)
	 ***/
	public Locale getLocale(String userId)
	{
		Locale loc = null;
		Preferences prefs = getPreferences(userId);
		ResourceProperties locProps = prefs.getProperties(LOCALE_PREFERENCE_KEY);
		String localeString = locProps.getProperty(Preferences.FIELD_LOCALE);
		
		// Parse user locale preference if set
		if (localeString != null)
		{
			String[] locValues = localeString.split("_");
			if (locValues.length > 2)
				loc = new Locale(locValues[0], locValues[1], locValues[2]); // language, country, variant
			else if (locValues.length == 2)
				loc = new Locale(locValues[0], locValues[1]); // language, country
			else if (locValues.length == 1) 
				loc = new Locale(locValues[0]); // just language
		}
		
		return loc;
	}
	
	
	
	
	
	
	/**
	 * @inheritDoc
	 */
	public boolean allowUpdate(String id)
	{
		return unlockCheck(SECURE_EDIT_PREFS, preferencesReference(id));
	}

	/**
	 * @inheritDoc
	 */
	public PreferencesEdit add(String id) throws PermissionException, IdUsedException
	{
		// check security (throws if not permitted)
		unlock(SECURE_ADD_PREFS, preferencesReference(id));

		// reserve a user with this id from the info store - if it's in use, this will return null
		PreferencesEdit edit = m_storage.put(id);
		if (edit == null)
		{
			throw new IdUsedException(id);
		}

		((BasePreferences) edit).setEvent(SECURE_ADD_PREFS);

		return edit;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EntityProducer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public String getLabel()
	{
		return "preferences";
	}

	/**
	 * @inheritDoc
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public HttpAccess getHttpAccess()
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		// for preferences access
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String id = null;

			// we will get null, service, user/preferences Id
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if (parts.length > 2)
			{
				id = parts[2];
			}

			ref.set(APPLICATION_ID, null, id, null, null);

			return true;
		}

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Collection<String> getEntityAuthzGroups(Reference ref, String userId)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		Collection<String> rv = new Vector<String>();

		// for preferences access: no additional role realms
		try
		{
			rv.add(userDirectoryService().userReference(ref.getId()));

			ref.addUserTemplateAuthzGroup(rv, userId);
		}
		catch (NullPointerException e)
		{
			log.warn("getEntityAuthzGroups(): " + e);
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return "";
	}

	/**
	 * @inheritDoc
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		return "";
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BasePreferences(id);
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, Element element)
	{
		return new BasePreferences(element);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, Entity other)
	{
		return new BasePreferences((Preferences) other);
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		BasePreferences e = new BasePreferences(id);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		BasePreferences e = new BasePreferences(element);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		BasePreferences e = new BasePreferences((Preferences) other);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Object[] storageFields(Entity r)
	{
		return null;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if a preferences by this id exists.
		 *
		 * @param id
		 *        The user id.
		 * @return true if a preferences for this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the preferences with this id, or null if not found.
		 *
		 * @param id
		 *        The preferences id.
		 * @return The preferences with this id, or null if not found.
		 */
		public Preferences get(String id);

		/**
		 * Add a new preferences with this id.
		 *
		 * @param id
		 *        The preferences id.
		 * @return The locked Preferences object with this id, or null if the id is in use.
		 */
		public PreferencesEdit put(String id);

		/**
		 * Get a lock on the preferences with this id, or null if a lock cannot be gotten.
		 *
		 * @param id
		 *        The preferences id.
		 * @return The locked Preferences with this id, or null if this records cannot be locked.
		 */
		public PreferencesEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 *
		 * @param user
		 *        The edit to commit.
		 */
		public void commit(PreferencesEdit edit);

		/**
		 * Cancel the changes and release the lock.
		 *
		 * @param user
		 *        The edit to commit.
		 */
		public void cancel(PreferencesEdit edit);

		/**
		 * Remove this edit and release the lock.
		 *
		 * @param user
		 *        The edit to remove.
		 */
		public void remove(PreferencesEdit edit);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Preferences implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BasePreferences implements PreferencesEdit, SessionBindingListener
	{
		/** The user id. */
		protected String m_id = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** The sets of keyed ResourceProperties. */
		protected Map<String, ResourcePropertiesEdit> m_props = null;
		/** The event code for this edit. */
		protected String m_event = null;
		/** Active flag. */
		protected boolean m_active = false;

		/**
		 * Construct.
		 *
		 * @param id
		 *        The user id.
		 */
		public BasePreferences(String id)
		{
			m_id = id;

			// setup for properties
			ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
			m_properties = props;

			m_props = new Hashtable<>();

			// if the id is not null (a new user, rather than a reconstruction)
			// and not the anon (id == "") user,
			// add the automatic (live) properties
			// %%% if ((m_id != null) && (m_id.length() > 0)) addLiveProperties(props);
		}

		/**
		 * Construct from another Preferences object.
		 *
		 * @param user
		 *        The user object to use for values.
		 */
		public BasePreferences(Preferences prefs)
		{
			setAll(prefs);
		}

		/**
		 * Construct from information in XML.
		 *
		 * @param el
		 *        The XML DOM Element definining the user.
		 */
		public BasePreferences(Element el)
		{
			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			m_props = new Hashtable<>();

			m_id = el.getAttribute("id");

			// the children (properties)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
				}

				// look for a set of preferences
				else if (element.getTagName().equals("prefs"))
				{
					String key = element.getAttribute("key");

					// convert old pre Sakai 2.2 keys to new values (copied here to avoid build dependencies - WATCH OUT! -ggolden)
					if (key.startsWith(NotificationService.PREFS_TYPE))
					{
						if (key.endsWith("AnnouncementService"))
						{
							// matches AnnouncementService.APPLICATION_ID
							key = NotificationService.PREFS_TYPE + "sakai:announcement";
						}
						else if (key.endsWith("ContentHostingService"))
						{
							// matches ContentHostingService.APPLICATION_ID
							key = NotificationService.PREFS_TYPE + "sakai:content";
						}
						else if (key.endsWith("MailArchiveService"))
						{
							// matches MailArchiveService.APPLICATION_ID
							key = NotificationService.PREFS_TYPE + "sakai:mailarchive";
						}
						else if (key.endsWith("SyllabusService"))
						{
							// matches SyllabusService.APPLICATION_ID
							key = NotificationService.PREFS_TYPE + "sakai:syllabus";
						}
					}
					else if (key.endsWith("TimeService"))
					{
						// matches TimeService.APPLICATION_ID
						key = "sakai:time";
					}
					else if (key.endsWith("sitenav"))
					{
						// matches Charon portal's value
						key = SITENAV_PREFS_KEY;
					}
					else if (key.endsWith("ResourceLoader"))
					{
						// matches ResourceLoader.APPLICATION_ID
						key = LOCALE_PREFERENCE_KEY;
					}

					BaseResourcePropertiesEdit props = null;

					// the children (properties)
					NodeList kids = element.getChildNodes();
					final int len = kids.getLength();
					for (int i2 = 0; i2 < len; i2++)
					{
						Node kid = kids.item(i2);
						if (kid.getNodeType() != Node.ELEMENT_NODE) continue;
						Element k = (Element) kid;

						// look for properties
						if (k.getTagName().equals("properties"))
						{
							props = new BaseResourcePropertiesEdit(k);
						}
					}

					if (props != null)
					{
						m_props.put(key, props);
					}
				}
			}
		}

		/**
		 * Take all values from this object.
		 *
		 * @param user
		 *        The user object to take values from.
		 */
		protected void setAll(Preferences prefs)
		{
			m_id = prefs.getId();

			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(prefs.getProperties());

			// %%% is this deep enough? -ggolden
			m_props = new Hashtable<>();
			m_props.putAll(((BasePreferences) prefs).m_props);
		}

		/**
		 * @inheritDoc
		 */
		public Element toXml(Document doc, Stack<Element> stack)
		{
			Element prefs = doc.createElement("preferences");

			if (stack.isEmpty())
			{
				doc.appendChild(prefs);
			}
			else
			{
				stack.peek().appendChild(prefs);
			}

			stack.push(prefs);

			prefs.setAttribute("id", getId());

			// properties
			m_properties.toXml(doc, stack);

			// for each keyed property
			for (Iterator it = m_props.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				ResourceProperties properties = (ResourceProperties) entry.getValue();

				// if the props are empty, skip it
				if (properties.getPropertyNames().next() == null) continue;

				Element props = doc.createElement("prefs");
				prefs.appendChild(props);
				props.setAttribute("key", key);
				stack.push(props);
				properties.toXml(doc, stack);
				stack.pop();
			}
			stack.pop();

			return prefs;
		}

		/**
		 * @inheritDoc
		 */
		public String getId()
		{
			if (m_id == null) return "";
			return m_id;
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl()
		{
			return getAccessPoint(false) + m_id;
		}

		/**
		 * @inheritDoc
		 */
		public String getReference()
		{
			return preferencesReference(m_id);
		}

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * @inheritDoc
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;
		}

		/**
		 * @inheritDoc
		 */
		public ResourceProperties getProperties(String key)
		{
			ResourceProperties rv = m_props.get(key);
			if (rv == null)
			{
				// new, throwaway empty one
				rv = new BaseResourceProperties();
			}

			return rv;
		}

		/**
		 * @inheritDoc
		 */
		public Collection<String> getKeys()
		{
			return m_props.keySet();
		}

		/**
		 * @inheritDoc
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Preferences)) return false;
			return ((Preferences) obj).getId().equals(getId());
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * Edit implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * @inheritDoc
		 */
		public int hashCode()
		{
			return getId().hashCode();
		}

		/**
		 * @inheritDoc
		 */
		public int compareTo(Object obj)
		{
			if (!(obj instanceof Preferences)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// sort based on (unique) id
			int compare = getId().compareTo(((Preferences) obj).getId());

			return compare;
		}

		/**
		 * @inheritDoc
		 */
		public ResourcePropertiesEdit getPropertiesEdit(String key)
		{
			synchronized (m_props)
			{
				ResourcePropertiesEdit rv = m_props.get(key);
				if (rv == null)
				{
					// new one saved in the map
					rv = new BaseResourcePropertiesEdit();
					m_props.put(key, rv);
				}

				return rv;
			}
		}

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancel(this);
			}
		}

		/**
		 * Take all values from this object.
		 *
		 * @param user
		 *        The user object to take values from.
		 */
		protected void set(Preferences prefs)
		{
			setAll(prefs);
		}

		/**
		 * Access the event code for this edit.
		 *
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 *
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * @inheritDoc
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;
		}

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;
		}

		/**
		 * @inheritDoc
		 */
		public boolean isActiveEdit()
		{
			return m_active;
		}

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * @inheritDoc
		 */
		public void valueBound(SessionBindingEvent event)
		{
		}

		/**
		 * @inheritDoc
		 */
		public void valueUnbound(SessionBindingEvent event)
		{
			if (log.isDebugEnabled()) log.debug("valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancel(this);
			}
		}
	}

}
