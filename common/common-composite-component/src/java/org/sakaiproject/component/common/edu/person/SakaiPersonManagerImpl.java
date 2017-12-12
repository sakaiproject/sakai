/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.common.edu.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.type.StringType;
import org.sakaiproject.api.common.edu.person.PhotoService;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.common.manager.PersistableHelper;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;


/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 */
@Slf4j
public class SakaiPersonManagerImpl extends HibernateDaoSupport implements SakaiPersonManager
{
	private static final String PERCENT_SIGN = "%";

	private static final String SURNAME = "surname";

	private static final String GIVENNAME = "givenName";

	private static final String UID = "uid";

	private static final String TYPE_UUID = "typeUuid";

	private static final String AGENT_UUID = "agentUuid";
	
	private static final String AGENT_UUID_COLLECTION = "agentUuidCollection";

	private static final String FERPA_ENABLED = "ferpaEnabled";

	private static final String HQL_FIND_SAKAI_PERSON_BY_AGENT_AND_TYPE = "findEduPersonByAgentAndType";

	private static final String HQL_FIND_SAKAI_PERSONS_BY_AGENTS_AND_TYPE = "findEduPersonsByAgentsAndType";

	private static final String HQL_FIND_SAKAI_PERSON_BY_UID = "findSakaiPersonByUid";

	private static final int MAX_QUERY_COLLECTION_SIZE = 1000;
	
	private TypeManager typeManager; // dep inj

	private PersistableHelper persistableHelper; // dep inj

	// SakaiPerson record types
	private Type systemMutableType; // oba constant

	private Type userMutableType; // oba constant

	// hibernate cannot cache BLOB data types - rshastri
	// private boolean cacheFindSakaiPersonString = true;
	// private boolean cacheFindSakaiPersonStringType = true;
	// private boolean cacheFindSakaiPersonSakaiPerson = true;
	// private boolean cacheFindSakaiPersonByUid = true;

	private static final String[] SYSTEM_MUTABLE_PRIMITIVES = { "org.sakaiproject", "api.common.edu.person",
			"SakaiPerson.recordType.systemMutable", "System Mutable SakaiPerson", "System Mutable SakaiPerson", };

	private static final String[] USER_MUTABLE_PRIMITIVES = { "org.sakaiproject", "api.common.edu.person",
			"SakaiPerson.recordType.userMutable", "User Mutable SakaiPerson", "User Mutable SakaiPerson", };

	
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService scs) {
		serverConfigurationService = scs;
	}
	
	private UserDirectoryService userDirectoryService;
	/**
	 * @param userDirectoryService
	 *        The userDirectoryService to set.
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setUserDirectoryService(userDirectoryService {})", userDirectoryService);
		}

		this.userDirectoryService = userDirectoryService;
	}

	
	private EventTrackingService eventTrackingService;
	
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	private PhotoService photoService;
	public void setPhotoService(PhotoService ps) {
		this.photoService = ps;
	}
	
	public void init()
	{
		log.debug("init()");

		log.debug("// init systemMutableType");
		systemMutableType = typeManager.getType(SYSTEM_MUTABLE_PRIMITIVES[0], SYSTEM_MUTABLE_PRIMITIVES[1],
				SYSTEM_MUTABLE_PRIMITIVES[2]);
		if (systemMutableType == null)
		{
			systemMutableType = typeManager.createType(SYSTEM_MUTABLE_PRIMITIVES[0], SYSTEM_MUTABLE_PRIMITIVES[1],
					SYSTEM_MUTABLE_PRIMITIVES[2], SYSTEM_MUTABLE_PRIMITIVES[3], SYSTEM_MUTABLE_PRIMITIVES[4]);
		}
		if (systemMutableType == null) throw new IllegalStateException("systemMutableType == null");

		log.debug("// init userMutableType");
		userMutableType = typeManager.getType(USER_MUTABLE_PRIMITIVES[0], USER_MUTABLE_PRIMITIVES[1], USER_MUTABLE_PRIMITIVES[2]);
		if (userMutableType == null)
		{
			userMutableType = typeManager.createType(USER_MUTABLE_PRIMITIVES[0], USER_MUTABLE_PRIMITIVES[1],
					USER_MUTABLE_PRIMITIVES[2], USER_MUTABLE_PRIMITIVES[3], USER_MUTABLE_PRIMITIVES[4]);
		}
		if (userMutableType == null) throw new IllegalStateException("userMutableType == null");
		
		
		
		log.debug("init() has completed successfully");
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#create(java.lang.String, java.lang.String, org.sakaiproject.api.common.type.Type)
	 */
	public SakaiPerson create(String userId, Type recordType)
	{
		if (log.isDebugEnabled())
		{
			log.debug("create(String {},  Type {})", userId, recordType);
		}
		if (userId == null || userId.length() < 1) throw new IllegalArgumentException("Illegal agentUuid argument passed!");; // a null uid is valid
		if (!isSupportedType(recordType)) throw new IllegalArgumentException("Illegal recordType argument passed!");

		SakaiPersonImpl spi = new SakaiPersonImpl();
		persistableHelper.createPersistableFields(spi);
		spi.setUuid(IdManager.createUuid());
		spi.setAgentUuid(userId);
		spi.setUid(userId);
		spi.setTypeUuid(recordType.getUuid());
		spi.setLocked(Boolean.valueOf(false));
		this.getHibernateTemplate().save(spi);
		
		//log the event
		String ref = getReference(spi);
		eventTrackingService.post(eventTrackingService.newEvent("profile.new", ref, true));
		
		//do not do this for system profiles 
		if (serverConfigurationService.getBoolean("profile.updateUser",false)) {
			try {
				User u = userDirectoryService.getUser(userId);
				spi.setGivenName(u.getFirstName());
				spi.setSurname(u.getLastName());
				spi.setMail(u.getEmail());
			}
			catch (UserNotDefinedException uue) {
				log.error("User {} doesn't exist", userId);
			}
			
		}
		
		log.debug("return spi;");
		return spi;
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#getSakaiPerson(org.sakaiproject.api.common.type.Type)
	 */
	public SakaiPerson getSakaiPerson(Type recordType)
	{
		if (log.isDebugEnabled())
		{
			log.debug("getSakaiPerson(Type {})", recordType);
		}; // no validation required; method is delegated.

		log.debug("return findSakaiPerson(agent.getUuid(), recordType);");
		return getSakaiPerson(SessionManager.getCurrentSessionUserId(), recordType);
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#getPrototype()
	 */
	public SakaiPerson getPrototype()
	{
		log.debug("getPrototype()");

		return new SakaiPersonImpl();
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#findSakaiPersonByUid(java.lang.String)
	 */
	public List findSakaiPersonByUid(final String uid)
	{
		if (log.isDebugEnabled())
		{
			log.debug("findSakaiPersonByUid(String {})", uid);
		}
		if (uid == null || uid.length() < 1) throw new IllegalArgumentException("Illegal uid argument passed!");

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				final Query q = session.getNamedQuery(HQL_FIND_SAKAI_PERSON_BY_UID);
				q.setParameter(UID, uid, StringType.INSTANCE);
				// q.setCacheable(cacheFindSakaiPersonByUid);
				return q.list();
			}
		};

		log.debug("return getHibernateTemplate().executeFind(hcb);");
		List hb = (List) getHibernateTemplate().execute(hcb);
		if (photoService.overRidesDefault()) {
			return this.getDiskPhotosForList(hb);
		} else {
			return hb;
		}
	}

	/**
	 * @see SakaiPersonManager#save(SakaiPerson)
	 */
	public void save(SakaiPerson sakaiPerson)
	{
		if (log.isDebugEnabled())
		{
			log.debug("save(SakaiPerson {})", sakaiPerson);
		}
		if (sakaiPerson == null) throw new IllegalArgumentException("Illegal sakaiPerson argument passed!");
		if (!isSupportedType(sakaiPerson.getTypeUuid()))
			throw new IllegalArgumentException("The sakaiPerson argument contains an invalid Type!");

		// AuthZ
		// Only superusers can update system records
		if (getSystemMutableType().getUuid().equals(sakaiPerson.getTypeUuid()) && !SecurityService.isSuperUser())
		{
			throw new IllegalAccessError("System mutable records cannot be updated.");
		}

		// if it is a user mutable record, ensure the user is updating their own record
		// this can be overriden with a security advisor so the admin user to allow access
		if(!SecurityService.unlock(UserDirectoryService.ADMIN_ID, SakaiPerson.PROFILE_SAVE_PERMISSION, sakaiPerson.getAgentUuid())) {
		
			if (!StringUtils.equals(SessionManager.getCurrentSessionUserId(), sakaiPerson.getAgentUuid()) && !SecurityService.isSuperUser())
			{
				// AuthZ - Ensure the current user is updating their own record
				if (!StringUtils.equals(SessionManager.getCurrentSessionUserId(), sakaiPerson.getAgentUuid())) {
				throw new IllegalAccessError("You do not have permissions to update this record!");
				}
			}
		}

		// store record
		if (!(sakaiPerson instanceof SakaiPersonImpl))
		{
			// TODO support alternate implementations of SakaiPerson
			// copy bean properties into new SakaiPersonImpl with beanutils?
			throw new UnsupportedOperationException("Unknown SakaiPerson implementation found!");
		}
		else
		{
			// update lastModifiedDate
			SakaiPersonImpl spi = (SakaiPersonImpl) sakaiPerson;
			persistableHelper.modifyPersistableFields(spi);
			//if the repository path is set save if there
			if (photoService.overRidesDefault()){
				photoService.savePhoto(spi.getJpegPhoto(), spi.getAgentUuid());
				spi.setJpegPhoto(null);
			} 
			
	
			
			// use update(..) method to ensure someone does not try to insert a
			// prototype.
			getHibernateTemplate().update(spi);
			
			//set the event
			String ref = getReference(spi);
			log.debug("got ref of: {} about to set events", ref);
			
				
			
			eventTrackingService.post(eventTrackingService.newEvent(PROFILE_UPDATE, ref, true));
			
			
			log.debug("User record updated for Id :-{}", spi.getAgentUuid());
			//update the account too -only if not system profile 
			if (serverConfigurationService.getBoolean("profile.updateUser",false) && spi.getTypeUuid().equals(this.userMutableType.getUuid()) )
			{
				try {
					UserEdit userEdit = null;
					userEdit = userDirectoryService.editUser(spi.getAgentUuid());
					userEdit.setFirstName(spi.getGivenName());
					userEdit.setLastName(spi.getSurname());
					userEdit.setEmail(spi.getMail());
					userDirectoryService.commitEdit(userEdit);
					log.debug("Saved user object");
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			
			
		}
	}

	private String getReference(SakaiPerson spi) {
		StringBuilder sb = new StringBuilder(Entity.SEPARATOR);
		sb.append("profile").append(Entity.SEPARATOR).append("type").append(Entity.SEPARATOR)
			.append(spi.getTypeUuid()).append(Entity.SEPARATOR).append("id")
			.append(Entity.SEPARATOR).append(spi.getAgentUuid());
		return sb.toString();
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#findSakaiPerson(java.lang.String, org.sakaiproject.api.common.type.Type)
	 */
	public SakaiPerson getSakaiPerson(final String agentUuid, final Type recordType)
	{
		if (log.isDebugEnabled())
		{
			log.debug("getSakaiPerson(String {}, Type {})", agentUuid, recordType);
		}
		if (agentUuid == null || agentUuid.length() < 1) throw new IllegalArgumentException("Illegal agentUuid argument passed!");
		if (recordType == null || !isSupportedType(recordType))
			throw new IllegalArgumentException("Illegal recordType argument passed!");

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query q = session.getNamedQuery(HQL_FIND_SAKAI_PERSON_BY_AGENT_AND_TYPE);
				q.setParameter(AGENT_UUID, agentUuid, StringType.INSTANCE);
				q.setParameter(TYPE_UUID, recordType.getUuid(), StringType.INSTANCE);
				// q.setCacheable(false);
				return q.uniqueResult();
			}
		};

		log.debug("return (SakaiPerson) getHibernateTemplate().execute(hcb);");
		SakaiPerson sp =  (SakaiPerson) getHibernateTemplate().execute(hcb);
		if (photoService.overRidesDefault() && sp != null && sp.getTypeUuid().equals(this.getSystemMutableType().getUuid())) {
			sp.setJpegPhoto(photoService.getPhotoAsByteArray(sp.getAgentUuid()));
		} 
		
		return sp;
	}

	public Map<String, SakaiPerson> getSakaiPersons(final Set<String> userIds, final Type recordType)
	{
		if (log.isDebugEnabled())
		{
			log.debug("getSakaiPersons(Collection size {}, Type {})", userIds.size(), recordType);
		}
		if (userIds == null || userIds.size() == 0) throw new IllegalArgumentException("Illegal agentUuid argument passed!");
		if (recordType == null || !isSupportedType(recordType)) throw new IllegalArgumentException("Illegal recordType argument passed!");

		int collectionSize = userIds.size();
		
		// Keep an ordered list of userIds
		List<String> userIdList = new ArrayList<String>(userIds);

		// The map we're return
		Map<String, SakaiPerson> map = new HashMap<String, SakaiPerson>(collectionSize);
		
		// Oracle (maybe others, too) can only take up to 1000 parameters total, so chunk the list if necessary
		if(collectionSize > MAX_QUERY_COLLECTION_SIZE)
		{
			int offset = 0;
			List<String> userListChunk = new ArrayList<String>();
			while(offset < collectionSize)
			{
				if(offset > 0 && offset % MAX_QUERY_COLLECTION_SIZE == 0) {
					// Our chunk is full, so process the list, clear it, and continue
					List<SakaiPerson> personListChunk = listSakaiPersons(userListChunk, recordType);
					addSakaiPersonsToMap(personListChunk, map);
					userListChunk.clear();
				}
				userListChunk.add(userIdList.get(offset));
				offset++;
			}
			// We may (and probably do) have remaining users that haven't been queried
			if( ! userListChunk.isEmpty())
			{
				List<SakaiPerson> lastChunk = listSakaiPersons(userListChunk, recordType);
				addSakaiPersonsToMap(lastChunk, map);
			}
		}
		else
		{
			addSakaiPersonsToMap(listSakaiPersons(userIds, recordType), map);
		}
		return map;
	}

	private void addSakaiPersonsToMap(List<SakaiPerson> sakaiPersons, Map<String, SakaiPerson> map) {
		for(Iterator<SakaiPerson> iter = sakaiPersons.iterator(); iter.hasNext();)
		{
			SakaiPerson person = iter.next();
			map.put(person.getAgentUuid(), person);
		}
	}
	
	private List<SakaiPerson> listSakaiPersons(final Collection<String> userIds, final Type recordType)
	{
		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query q = session.getNamedQuery(HQL_FIND_SAKAI_PERSONS_BY_AGENTS_AND_TYPE);
				q.setParameterList(AGENT_UUID_COLLECTION, userIds);
				q.setParameter(TYPE_UUID, recordType.getUuid(), StringType.INSTANCE);
				// q.setCacheable(false);
				return q.list();
			}
		};
		List hb =  (List) getHibernateTemplate().execute(hcb);
		if (photoService.overRidesDefault()) {
			return getDiskPhotosForList(hb);
		} else {
			return hb;
		}
	}

	/**
	 * @see SakaiPersonManager#findSakaiPerson(String)
	 */
	public List findSakaiPerson(final String simpleSearchCriteria)
	{
		if (log.isDebugEnabled())
		{
			log.debug("findSakaiPerson(String {})", simpleSearchCriteria);
		}
		if (simpleSearchCriteria == null || simpleSearchCriteria.length() < 1)
			throw new IllegalArgumentException("Illegal simpleSearchCriteria argument passed!");

		final String match = PERCENT_SIGN + simpleSearchCriteria + PERCENT_SIGN;
		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				final Criteria c = session.createCriteria(SakaiPersonImpl.class);
				c.add(Expression.disjunction().add(Expression.ilike(UID, match)).add(Expression.ilike(GIVENNAME, match)).add(
						Expression.ilike(SURNAME, match)));
				c.addOrder(Order.asc(SURNAME));
				// c.setCacheable(cacheFindSakaiPersonString);
				return c.list();
				
			}
		};

		log.debug("return getHibernateTemplate().executeFind(hcb);");
		List hb = (List) getHibernateTemplate().execute(hcb);
		if (photoService.overRidesDefault()) {
			return getDiskPhotosForList(hb);
		} else {
			return hb;
		}
	}

	/**
	 * @param typeManager
	 *        The typeManager to set.
	 */
	public void setTypeManager(TypeManager typeManager)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setTypeManager(TypeManager {})", typeManager);
		}

		this.typeManager = typeManager;
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#getUserMutableType()
	 */
	public Type getUserMutableType()
	{
		log.debug("getUserMutableType()");

		return userMutableType;
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#getSystemMutableType()
	 */
	public Type getSystemMutableType()
	{
		log.debug("getSystemMutableType()");

		return systemMutableType;
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#findSakaiPerson(org.sakaiproject.api.common.edu.person.SakaiPerson)
	 */
	public List findSakaiPerson(final SakaiPerson queryByExample)
	{
		if (log.isDebugEnabled())
		{
			log.debug("findSakaiPerson(SakaiPerson {})", queryByExample);
		}
		if (queryByExample == null) throw new IllegalArgumentException("Illegal queryByExample argument passed!");

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				Criteria criteria = session.createCriteria(queryByExample.getClass());
				criteria.add(Example.create(queryByExample));
				// criteria.setCacheable(cacheFindSakaiPersonSakaiPerson);
				return criteria.list();
			}
		};

		log.debug("return getHibernateTemplate().executeFind(hcb);");
		List hb = (List) getHibernateTemplate().execute(hcb);
		if (photoService.overRidesDefault()) {
			return getDiskPhotosForList(hb);
		} else {
			return hb;
		}
	}
	
	

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#delete(org.sakaiproject.api.common.edu.person.SakaiPerson)
	 */
	public void delete(final SakaiPerson sakaiPerson)
	{
		if (log.isDebugEnabled())
		{
			log.debug("delete(SakaiPerson {})", sakaiPerson);
		}
		if (sakaiPerson == null) throw new IllegalArgumentException("Illegal sakaiPerson argument passed!");
		
		String ref =  getReference(sakaiPerson);
		
		//only someone with the appropriate permissions can delete
		if(!SecurityService.unlock("user.del", ref)){
			throw new SecurityException("You do not have permission to delete this sakaiPerson.");
		}
		
		
		log.debug("getHibernateTemplate().delete(sakaiPerson);");
		getHibernateTemplate().delete(sakaiPerson);
		eventTrackingService.post(eventTrackingService.newEvent(PROFILE_DELETE, ref, true));
		
	}

	private boolean isSupportedType(Type recordType)
	{
		if (log.isDebugEnabled())
		{
			log.debug("isSupportedType(Type {})", recordType);
		}

		if (recordType == null) return false;
		if (this.getUserMutableType().equals(recordType)) return true;
		if (this.getSystemMutableType().equals(recordType)) return true;
		return false;
	}

	private boolean isSupportedType(String typeUuid)
	{
		if (log.isDebugEnabled())
		{
			log.debug("isSupportedType(String {})", typeUuid);
		}

		if (typeUuid == null) return false;
		if (this.getUserMutableType().getUuid().equals(typeUuid)) return true;
		if (this.getSystemMutableType().getUuid().equals(typeUuid)) return true;
		return false;
	}

	/**
	 * @param cacheFindSakaiPersonStringType
	 *        The cacheFindSakaiPersonStringType to set.
	 */
	// public void setCacheFindSakaiPersonStringType(
	// boolean cacheFindSakaiPersonStringType)
	// {
	// this.cacheFindSakaiPersonStringType = cacheFindSakaiPersonStringType;
	// }
	/**
	 * @param cacheFindSakaiPersonString
	 *        The cacheFindSakaiPersonString to set.
	 */
	// public void setCacheFindSakaiPersonString(boolean cacheFindSakaiPersonString)
	// {
	// this.cacheFindSakaiPersonString = cacheFindSakaiPersonString;
	// }
	/**
	 * @param cacheFindSakaiPersonSakaiPerson
	 *        The cacheFindSakaiPersonSakaiPerson to set.
	 */
	// public void setCacheFindSakaiPersonSakaiPerson(
	// boolean cacheFindSakaiPersonSakaiPerson)
	// {
	// this.cacheFindSakaiPersonSakaiPerson = cacheFindSakaiPersonSakaiPerson;
	// }
	/**
	 * @param cacheFindSakaiPersonByUid
	 *        The cacheFindSakaiPersonByUid to set.
	 */
	// public void setCacheFindSakaiPersonByUid(boolean cacheFindSakaiPersonByUid)
	// {
	// this.cacheFindSakaiPersonByUid = cacheFindSakaiPersonByUid;
	// }
	/**
	 * @param persistableHelper
	 *        The persistableHelper to set.
	 */
	public void setPersistableHelper(PersistableHelper persistableHelper)
	{
		this.persistableHelper = persistableHelper;
	}

	public List isFerpaEnabled(final Collection agentUuids)
	{
		if (log.isDebugEnabled())
		{
			log.debug("isFerpaEnabled(Set {})", agentUuids);
		}
		if (agentUuids == null || agentUuids.isEmpty())
		{
			throw new IllegalArgumentException("Illegal Set agentUuids argument!");
		}

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				final Criteria c = session.createCriteria(SakaiPersonImpl.class);
				c.add(Expression.in(AGENT_UUID, agentUuids));
				c.add(Expression.eq(FERPA_ENABLED, Boolean.TRUE));
				return c.list();
			}
		};
		return (List) getHibernateTemplate().execute(hcb);
	}

	public List findAllFerpaEnabled()
	{
		log.debug("findAllFerpaEnabled()");

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				final Criteria c = session.createCriteria(SakaiPersonImpl.class);
				c.add(Expression.eq(FERPA_ENABLED, Boolean.TRUE));
				return c.list();
			}
		};
		return (List) getHibernateTemplate().execute(hcb);
	}
	

	
	
	
	
	private List getDiskPhotosForList(List listIn) {
		
		List listOut = new ArrayList();
		
		for (int i = 0; i < listIn.size(); i++) {
			SakaiPerson sp = (SakaiPerson)listIn.get(i);
			if (sp.getAgentUuid() != null && sp.getTypeUuid().equals(this.getSystemMutableType().getUuid())) {
				sp.setJpegPhoto(photoService.getPhotoAsByteArray(sp.getAgentUuid()));
			}
			listOut.add(sp);
			
		}
		
		
		return listOut;
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
}
