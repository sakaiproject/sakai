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

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.persistence.OptimisticLockException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesConflictException;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.model.PreferencesRecord;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.StorageUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * <p>
 * BasePreferencesService is a Sakai Preferences implementation.
 * </p>
 */
@Slf4j
public class BasePreferencesService implements PreferencesService
{
	/** Key used to store the locale preferences. */
	private static final String LOCALE_PREFERENCE_KEY = "sakai:resourceloader";
	@Setter private SpringCrudRepository<PreferencesRecord, String> repository;
	private TransactionTemplate transactions;

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		transactions = new TransactionTemplate(transactionManager);
		transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;
	/** the cache for Preference objects **/
	private Cache m_cache;

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService.getAccessUrl()) + m_relativeAccessPoint;
	}

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
		if (!securityService.unlock(lock, resource))
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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), lock, resource);
		}
	}

	@Setter protected CacheManager cacheManager;
	@Setter protected ServerConfigurationService serverConfigurationService;
	@Setter protected EntityManager entityManager;
	@Setter protected SecurityService securityService;
	@Setter protected FunctionManager functionManager;
	@Setter protected SessionManager sessionManager;
	@Setter protected EventTrackingService eventTrackingService;
	@Setter protected UserDirectoryService userDirectoryService;

	public void init()
	{
		try
		{
			m_relativeAccessPoint = REFERENCE_ROOT;

			// register as an entity producer
			entityManager.registerEntityProducer(this, REFERENCE_ROOT);

			// register functions
			functionManager.registerFunction(SECURE_ADD_PREFS);
			functionManager.registerFunction(SECURE_EDIT_PREFS);
			functionManager.registerFunction(SECURE_REMOVE_PREFS);

			
			//register a cache
			m_cache = cacheManager.getCache(BasePreferencesService.class.getName() +".preferences");
			
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
		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * PreferencesService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	@Override
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

    @Override
    public Optional<String> getValue(String id, String key, String name) {
        return Optional.ofNullable(getPreferences(id).getProperties(key).getProperty(name));
    }

    @Override
    public boolean applyEditWithAutoCommit(String userId, Consumer<PreferencesEdit> editFunction) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("The user ID must not be empty");
        }
        Objects.requireNonNull(editFunction, "The preferences edit function must not be null");

        PreferencesEdit edit = null;
        try {
            edit = edit(userId);
            editFunction.accept(edit);
            commit(edit);
            return true;
        } catch (PreferencesConflictException e) {
            log.debug("Concurrent preferences update for user {}", userId);
            return false;
        } catch (Exception e) {
            log.warn("Could not update preferences for user {}", userId, e);
            return false;
        } finally {
            if (edit != null && edit.isActiveEdit()) {
                cancel(edit);
            }
        }
    }

    @Override
    public PreferencesEdit edit(String id) throws PermissionException, InUseException, IdUnusedException {
        unlock(SECURE_EDIT_PREFS, preferencesReference(id));
        BasePreferences edit = readPreferences(id);
        if (edit == null) {
            unlock(SECURE_ADD_PREFS, preferencesReference(id));
            edit = new BasePreferences(id);
            edit.setEvent(SECURE_ADD_PREFS);
        } else {
            edit.setEvent(SECURE_EDIT_PREFS);
        }
        edit.activate();
        return edit;
    }

	@Override
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

			try {
				BasePreferences preferences = (BasePreferences) edit;
				PreferencesRecord record = toRecord(preferences);
				PreferencesRecord saved = writePreferences(record, () -> repository.save(record));
				preferences.m_version = saved.getVersion();
			} finally {
				((BasePreferences) edit).closeEdit();
			}
			m_cache.evict(edit.getId());

			// track it
			eventTrackingService.post(eventTrackingService.newEvent(((BasePreferences) edit).getEvent(), edit.getReference(), true));
		}
	}

	@Override
	public void cancel(PreferencesEdit edit)
	{
		if (edit != null)
		{
			((BasePreferences) edit).closeEdit();
		}
	}

	@Override
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

		try {
			BasePreferences preferences = (BasePreferences) edit;
			if (preferences.m_version != null) {
				PreferencesRecord record = toRecord(preferences);
				writePreferences(record, () -> {
					repository.delete(record);
					return null;
				});
			}
		} finally {
			((BasePreferences) edit).closeEdit();
		}

		m_cache.evict(edit.getId());

		// track it
		eventTrackingService.post(eventTrackingService.newEvent(SECURE_REMOVE_PREFS, edit.getReference(), true));
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
		PreferencesSnapshot snapshot = m_cache.get(id, PreferencesSnapshot.class);
		BasePreferences prefs = snapshot != null ? fromSnapshot(snapshot) : null;

		// Failing that, try the storage
		if (prefs == null) {
			try {
				prefs = readPreferences(id);
			} catch (IllegalStateException e) {
				log.warn("Ignoring invalid preferences for user {}: {}", id, e.getMessage());
				return null;
			}
		}

		if (prefs != null) {
			m_cache.put(id, prefs.toSnapshot());
		}

		return prefs;
	}

	/**
	 * Rebuild a (non-editable) BasePreferences from a cached snapshot. BasePreferences itself
	 * can't be cached directly since it's a non-static inner class carrying an implicit
	 * reference to this service.
	 */
	private BasePreferences fromSnapshot(PreferencesSnapshot snapshot)
	{
		BasePreferences prefs = new BasePreferences(snapshot.id);
		prefs.m_properties = snapshot.properties;
		Map<String, ResourcePropertiesEdit> props = new Hashtable<>();
		props.putAll(snapshot.props);
		prefs.m_props = props;
		return prefs;
	}

	/**
	 * Get user's preferred locale (or null if not set)
	 * if userId is blank return Locale.getDefault
	 **/
	public Locale getLocale(String userId)
	{
		Locale loc = null;
		if (StringUtils.isBlank(userId)) {
			return Locale.getDefault();
		}
		Preferences prefs = getPreferences(userId);
		ResourceProperties locProps = prefs.getProperties(LOCALE_PREFERENCE_KEY);
		String localeString = locProps.getProperty(Preferences.FIELD_LOCALE);
		
		// Parse user locale preference if set
		if (localeString != null) {
			String[] locValues = localeString.split("_");
			if (locValues.length > 2) {
				loc = new Locale(locValues[0], locValues[1], locValues[2]); // language, country, variant
			} else if (locValues.length == 2) {
				loc = new Locale(locValues[0], locValues[1]); // language, country
			} else if (locValues.length == 1) {
				loc = new Locale(locValues[0]); // just language
			}
		} else {
			loc = Locale.getDefault();
		}
		
		return loc;
	}

	@Override
	public boolean allowUpdate(String id)
	{
		return unlockCheck(SECURE_EDIT_PREFS, preferencesReference(id));
	}

	@Override
	public PreferencesEdit add(String id) throws PermissionException, IdUsedException
	{
		// check security (throws if not permitted)
		unlock(SECURE_ADD_PREFS, preferencesReference(id));

        if (findRecord(id) != null) {
            throw new IdUsedException(id);
        }
        BasePreferences edit = new BasePreferences(id);
        edit.setEvent(SECURE_ADD_PREFS);
        edit.activate();

		return edit;
	}

	@Override
	public int getSiteTitleDisplayPreference() {

		String currentUserId = sessionManager.getCurrentSessionUserId();

		if (StringUtils.isBlank(currentUserId)) return USE_SITE_TITLE;

		Preferences prefs = getPreferences(currentUserId);

		if (prefs == null) return USE_SITE_TITLE;

		ResourceProperties props = prefs.getProperties(SITENAV_PREFS_KEY);

		if (props == null) return USE_SITE_TITLE;

		return NumberUtils.toInt(props.getProperty(TAB_LABEL_PREF), USE_SITE_TITLE);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EntityProducer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	@Override
	public String getLabel()
	{
		return "preferences";
	}

	@Override
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

	@Override
	public Collection<String> getEntityAuthzGroups(Reference ref, String userId)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		Collection<String> rv = new Vector<String>();

		// for preferences access: no additional role realms
		try
		{
			rv.add(userDirectoryService.userReference(ref.getId()));

			ref.addUserTemplateAuthzGroup(rv, userId);
		}
		catch (NullPointerException e)
		{
			log.warn("getEntityAuthzGroups(): " + e);
		}

		return rv;
	}

    // Finish each transaction before returning an edit or reporting a successful save.
    private PreferencesRecord findRecord(String id) {
        return transactions.execute(status -> repository.findById(id).orElse(null));
    }

    private <T> T writePreferences(PreferencesRecord record, Supplier<T> operation) {
        // Hibernate may assign the initial version before a failed insert is flushed.
        boolean inserting = record.getVersion() == null;
        try {
            return transactions.execute(status -> operation.get());
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            throw new PreferencesConflictException(record.getId(), e);
        } catch (DataIntegrityViolationException e) {
            if (inserting && findRecord(record.getId()) != null) {
                throw new PreferencesConflictException(record.getId(), e);
            }
            throw e;
        }
    }

    private BasePreferences readPreferences(String id) {
        PreferencesRecord record = findRecord(id);
        if (record == null) {
            return null;
        }
        Document document = StorageUtils.readDocumentFromString(record.getXml());
        if (document == null || !"preferences".equals(document.getDocumentElement().getTagName())) {
            throw new IllegalStateException("Invalid preferences document for user " + id);
        }
        BasePreferences preferences = new BasePreferences(document.getDocumentElement());
        if (!id.equals(preferences.getId())) {
            throw new IllegalStateException("Preferences document does not match user " + id);
        }
        preferences.m_version = record.getVersion();
        return preferences;
    }

    private PreferencesRecord toRecord(BasePreferences preferences) {
        Document document = StorageUtils.createDocument();
        preferences.toXml(document, new Stack<>());
        return new PreferencesRecord(preferences.getId(), StorageUtils.writeDocumentToString(document),
                preferences.m_version);
    }

	/**
	 * A cacheable, static (no enclosing-instance reference) snapshot of a BasePreferences'
	 * read-only state, for storing in the distributed cache in place of BasePreferences itself.
	 */
	static class PreferencesSnapshot implements Serializable
	{
		private static final long serialVersionUID = 1L;

		final String id;
		final BaseResourcePropertiesEdit properties;
		final Map<String, BaseResourcePropertiesEdit> props;

		PreferencesSnapshot(String id, BaseResourcePropertiesEdit properties, Map<String, BaseResourcePropertiesEdit> props)
		{
			this.id = id;
			this.properties = properties;
			this.props = props;
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Preferences implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BasePreferences implements PreferencesEdit
	{
		/** The user id. */
		protected String m_id = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** The sets of keyed ResourceProperties. */
		protected Map<String, ResourcePropertiesEdit> m_props = null;
		/** Version of the detached document being edited; null until first persisted. */
		protected Long m_version;
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
		 * @param prefs
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
		 * @param prefs
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
		 * Capture this preferences' state as a cacheable snapshot (see PreferencesSnapshot).
		 */
		PreferencesSnapshot toSnapshot()
		{
			Map<String, BaseResourcePropertiesEdit> propsCopy = new HashMap<>();
			for (Map.Entry<String, ResourcePropertiesEdit> entry : m_props.entrySet())
			{
				propsCopy.put(entry.getKey(), (BaseResourcePropertiesEdit) entry.getValue());
			}
			return new PreferencesSnapshot(m_id, (BaseResourcePropertiesEdit) m_properties, propsCopy);
		}

		@Override
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

		@Override
		public String getId()
		{
			if (m_id == null) return "";
			return m_id;
		}

		@Override
		public String getUrl()
		{
			return getAccessPoint(false) + m_id;
		}

		@Override
		public String getReference()
		{
			return preferencesReference(m_id);
		}

		@Override
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		@Override
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		@Override
		public ResourceProperties getProperties()
		{
			return m_properties;
		}

		@Override
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

		public ResourceProperties getToolNotificationProperties(String commonToolId) {

			if (commonToolId == null) {
				return new BaseResourceProperties();
			}

			String baseKey = NotificationService.PREFS_TYPE + commonToolId.replace(".", ":");
			ResourceProperties props = getProperties(baseKey);
			props.addAll(getProperties(baseKey + NotificationService.NOTI_OVERRIDE_EXTENSION));
			return props;
		}

		@Override
		public Collection<String> getKeys()
		{
			return m_props.keySet();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Preferences)) return false;
			return ((Preferences) obj).getId().equals(getId());
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * Edit implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		@Override
		public int hashCode()
		{
			return getId().hashCode();
		}

		@Override
		public int compareTo(Object obj)
		{
			if (!(obj instanceof Preferences)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// sort based on (unique) id
			int compare = getId().compareTo(((Preferences) obj).getId());

			return compare;
		}

		@Override
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
		 * Take all values from this object.
		 *
		 * @param prefs
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

		@Override
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

		@Override
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

	}
}
