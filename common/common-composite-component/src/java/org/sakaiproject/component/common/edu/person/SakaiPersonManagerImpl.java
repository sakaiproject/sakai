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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.sakaiproject.api.common.edu.person.PhotoService;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.common.manager.PersistableHelper;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 */
@Slf4j
@Transactional
public class SakaiPersonManagerImpl implements SakaiPersonManager
{
	private static final String PERCENT_SIGN = "%";
	private static final String SURNAME = "surname";
	private static final String GIVENNAME = "givenName";
	private static final String UID = "uid";
	private static final String TYPE_UUID = "typeUuid";
	private static final String AGENT_UUID = "agentUuid";
	private static final String FERPA_ENABLED = "ferpaEnabled";
	private static final int MAX_QUERY_COLLECTION_SIZE = 1000;

	@Setter private SessionFactory sessionFactory;

	@Setter private PersistableHelper persistableHelper;
    @Setter private SecurityService securityService;
    @Setter private SessionManager sessionManager;
	@Setter private TypeManager typeManager;

	private Type systemMutableType;
	private Type userMutableType;
    private boolean updateUserProfile;

    private static final String[] SYSTEM_MUTABLE_PRIMITIVES = {
            "org.sakaiproject",
            "api.common.edu.person",
            "SakaiPerson.recordType.systemMutable",
            "System Mutable SakaiPerson",
            "System Mutable SakaiPerson"
    };

    private static final String[] USER_MUTABLE_PRIMITIVES = {
            "org.sakaiproject",
            "api.common.edu.person",
			"SakaiPerson.recordType.userMutable",
            "User Mutable SakaiPerson",
            "User Mutable SakaiPerson"
    };

	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private EventTrackingService eventTrackingService;
	@Setter private PhotoService photoService;
	@Setter private IdManager idManager;
	
	public void init() {
		systemMutableType = typeManager.getType(
                SYSTEM_MUTABLE_PRIMITIVES[0],
                SYSTEM_MUTABLE_PRIMITIVES[1],
				SYSTEM_MUTABLE_PRIMITIVES[2]);

		if (systemMutableType == null) {
			systemMutableType = typeManager.createType(
                    SYSTEM_MUTABLE_PRIMITIVES[0],
                    SYSTEM_MUTABLE_PRIMITIVES[1],
					SYSTEM_MUTABLE_PRIMITIVES[2],
                    SYSTEM_MUTABLE_PRIMITIVES[3],
                    SYSTEM_MUTABLE_PRIMITIVES[4]);
		}

		if (systemMutableType == null) throw new IllegalStateException("systemMutableType == null");

		userMutableType = typeManager.getType(
                USER_MUTABLE_PRIMITIVES[0],
                USER_MUTABLE_PRIMITIVES[1],
                USER_MUTABLE_PRIMITIVES[2]);

        if (userMutableType == null) {
			userMutableType = typeManager.createType(
                    USER_MUTABLE_PRIMITIVES[0],
                    USER_MUTABLE_PRIMITIVES[1],
					USER_MUTABLE_PRIMITIVES[2],
                    USER_MUTABLE_PRIMITIVES[3],
                    USER_MUTABLE_PRIMITIVES[4]);
		}

		if (userMutableType == null) throw new IllegalStateException("userMutableType == null");

        updateUserProfile = serverConfigurationService.getBoolean("profile.updateUser", false);
	}

    @Override
    public Optional<SakaiPerson> create(String userId, Type recordType) {
        log.debug("Create a SakaiPerson for userId [{}], type [{}]", userId, recordType);
        if (StringUtils.isBlank(userId) || !isSupportedType(recordType)) {
            log.warn("Invalid userId [{}] or recordType [{}] argument", userId, recordType);
            return Optional.empty();
        }

        SakaiPersonImpl spi;
        try {
            User user = userDirectoryService.getUser(userId);
            spi = new SakaiPersonImpl();
            persistableHelper.createPersistableFields(spi);
            spi.setUuid(idManager.createUuid());
            spi.setAgentUuid(userId);
            spi.setUid(userId);
            spi.setTypeUuid(recordType.getUuid());
            spi.setLocked(false);

            if (updateUserProfile && recordType.equals(userMutableType)) {
                // do not do this for system profiles
                spi.setGivenName(user.getFirstName());
                spi.setSurname(user.getLastName());
                spi.setMail(user.getEmail());
            }

            // the SakaiPerson must not exist in the database yet
            sessionFactory.getCurrentSession().persist(spi);
        } catch (Exception e) {
            log.warn("Could not create SakaiPerson for userId [{}], type [{}]", userId, recordType, e);
            return Optional.empty();
        }

        // post event
        eventTrackingService.post(eventTrackingService.newEvent("profile.new", getReference(spi), true));

        return Optional.of(spi);
    }

    @Override
	public Optional<SakaiPerson> getSakaiPerson(Type recordType) {
        String userId = sessionManager.getCurrentSessionUserId();
        log.debug("Retrieve SakaiPerson for current user: {}, type: {}", userId, recordType);
		return getSakaiPerson(userId, recordType);
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPersonManager#getPrototype()
	 */
	public SakaiPerson getPrototype()
	{
		log.debug("getPrototype()");

		return new SakaiPersonImpl();
	}

    @Override
    public List<SakaiPerson> findSakaiPersonByUid(final String uid) {
        log.debug("Find SakaiPerson with uid: {}", uid);
        if (StringUtils.isBlank(uid)) return List.of();

        CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<InetOrgPersonImpl> query = cb.createQuery(InetOrgPersonImpl.class);
        Root<InetOrgPersonImpl> root = query.from(InetOrgPersonImpl.class);
        query.select(root).where(cb.equal(root.get(UID), uid));
        List<SakaiPerson> results = (List<SakaiPerson>) (List<?>) sessionFactory.getCurrentSession().createQuery(query).getResultList();

        return photoService.overRidesDefault() ? getDiskPhotosForList(results) : results;
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
		if (getSystemMutableType().getUuid().equals(sakaiPerson.getTypeUuid()) && !securityService.isSuperUser())
		{
			throw new IllegalAccessError("System mutable records cannot be updated.");
		}

        // if it is a user mutable record, ensure the user is updating their own record
        // this can be overridden with a security advisor or the admin permission
        if (!securityService.unlock(UserDirectoryService.ADMIN_ID, SakaiPerson.PROFILE_SAVE_PERMISSION, sakaiPerson.getAgentUuid())) {
            if (!StringUtils.equals(sessionManager.getCurrentSessionUserId(), sakaiPerson.getAgentUuid())
                    && !securityService.isSuperUser()) {
                throw new IllegalAccessError("You do not have permissions to update this record!");
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
			sessionFactory.getCurrentSession().update(spi);
			
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

    @Override
    public Optional<SakaiPerson> getSakaiPerson(final String agentUuid, final Type recordType) {
        log.debug("getSakaiPerson(String {}, Type {})", agentUuid, recordType);

        // Input validation
        if (StringUtils.isBlank(agentUuid) || (recordType == null || !isSupportedType(recordType))) {
            log.warn("Invalid agentUuid [{}] or recordType [{}] argument", agentUuid, recordType);
            return Optional.empty();
        }

        CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<PersonImpl> query = cb.createQuery(PersonImpl.class);
        Root<PersonImpl> root = query.from(PersonImpl.class);
        query.select(root).where(cb.equal(root.get(AGENT_UUID), agentUuid), cb.equal(root.get(TYPE_UUID), recordType.getUuid()));
        Optional<SakaiPerson> sakaiPerson = Optional.ofNullable((SakaiPerson) sessionFactory.getCurrentSession().createQuery(query).uniqueResult());

        if (!sakaiPerson.isPresent()) {
            log.debug("No SakaiPerson found for agentUuid {} and type {}", agentUuid, recordType);
            sakaiPerson = create(agentUuid, recordType);
        }

        // If photo service overrides default and this is a system profile, get photo from disk
        if (sakaiPerson.isPresent()
                && photoService.overRidesDefault()
                && systemMutableType.getUuid().equals(sakaiPerson.get().getTypeUuid())) {
            sakaiPerson.get().setJpegPhoto(photoService.getPhotoAsByteArray(sakaiPerson.get().getAgentUuid()));
        }

        return sakaiPerson;
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
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<PersonImpl> query = cb.createQuery(PersonImpl.class);
		Root<PersonImpl> root = query.from(PersonImpl.class);
		query.select(root).where(root.get(AGENT_UUID).in(userIds), cb.equal(root.get(TYPE_UUID), recordType.getUuid()));
		List<SakaiPerson> hb = (List<SakaiPerson>) (List<?>) sessionFactory.getCurrentSession().createQuery(query).getResultList();
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
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SakaiPersonImpl> query = cb.createQuery(SakaiPersonImpl.class);
		Root<SakaiPersonImpl> root = query.from(SakaiPersonImpl.class);
		query.select(root).where(cb.or(
			cb.like(cb.lower(root.get(UID)), cb.lower(cb.literal(match))),
			cb.like(cb.lower(root.get(GIVENNAME)), cb.lower(cb.literal(match))),
			cb.like(cb.lower(root.get(SURNAME)), cb.lower(cb.literal(match)))));
		query.orderBy(cb.asc(root.get(SURNAME)));
		List<SakaiPerson> hb = (List<SakaiPerson>) (List<?>) sessionFactory.getCurrentSession().createQuery(query).getResultList();
		if (photoService.overRidesDefault()) {
			return getDiskPhotosForList(hb);
		} else {
			return hb;
		}
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

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SakaiPersonImpl> query = cb.createQuery(SakaiPersonImpl.class);
		Root<SakaiPersonImpl> root = query.from(SakaiPersonImpl.class);

		List<Predicate> predicates = new ArrayList<>();
		if (StringUtils.isNotBlank(queryByExample.getUid())) {
			predicates.add(cb.equal(root.get(UID), queryByExample.getUid()));
		}
		if (StringUtils.isNotBlank(queryByExample.getGivenName())) {
			predicates.add(cb.equal(root.get(GIVENNAME), queryByExample.getGivenName()));
		}
		if (StringUtils.isNotBlank(queryByExample.getSurname())) {
			predicates.add(cb.equal(root.get(SURNAME), queryByExample.getSurname()));
		}
		if (StringUtils.isNotBlank(queryByExample.getAgentUuid())) {
			predicates.add(cb.equal(root.get(AGENT_UUID), queryByExample.getAgentUuid()));
		}
		if (StringUtils.isNotBlank(queryByExample.getTypeUuid())) {
			predicates.add(cb.equal(root.get(TYPE_UUID), queryByExample.getTypeUuid()));
		}
		if (StringUtils.isNotBlank(queryByExample.getMail())) {
			predicates.add(cb.equal(root.get("mail"), queryByExample.getMail()));
		}

		if (!predicates.isEmpty()) {
			query.where(cb.and(predicates.toArray(new Predicate[0])));
		}

		List<SakaiPerson> hb = (List<SakaiPerson>) (List<?>) session.createQuery(query).getResultList();
		if (photoService.overRidesDefault()) {
			return getDiskPhotosForList(hb);
		} else {
			return hb;
		}
	}

	@Override
	public void delete(final String userId) {
		log.debug("delete SakaiPerson entities matching id [{}]", userId);
		Objects.requireNonNull(userId, "userId cannot be null");

		Session session = sessionFactory.getCurrentSession();
		List<String> deletedRefs = new ArrayList<>();

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SakaiPersonImpl> query = cb.createQuery(SakaiPersonImpl.class);
		Root<SakaiPersonImpl> person = query.from(SakaiPersonImpl.class);
		query.where(cb.equal(person.get("agentUuid"), userId));
		session.createQuery(query).list()
				.forEach(p -> {
					String ref = getReference(p);
					if (securityService.unlock("user.del", ref)) {
						deletedRefs.add(ref);
						session.remove(p);
					}
		});

		for (String ref : deletedRefs) {
			eventTrackingService.post(eventTrackingService.newEvent(PROFILE_DELETE, ref, true));
		}
		log.debug("Deleted {} sakaiPerson entities matching id [{}]", deletedRefs.size(), userId);
	}

    private boolean isSupportedType(Type recordType) {
        log.debug("Checking if <Type> type [{}] is valid", recordType);
        return recordType != null && (userMutableType.equals(recordType) || systemMutableType.equals(recordType));
    }

    private boolean isSupportedType(String typeUuid) {
        log.debug("Checking if <String> type [{}] is valid", typeUuid);
        return typeUuid != null && (userMutableType.getUuid().equals(typeUuid) || systemMutableType.getUuid().equals(typeUuid));
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

		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SakaiPersonImpl> query = cb.createQuery(SakaiPersonImpl.class);
		Root<SakaiPersonImpl> root = query.from(SakaiPersonImpl.class);
		query.select(root).where(root.get(AGENT_UUID).in(agentUuids), cb.isTrue(root.get(FERPA_ENABLED)));
		return sessionFactory.getCurrentSession().createQuery(query).getResultList();
	}

	public List findAllFerpaEnabled()
	{
		log.debug("findAllFerpaEnabled()");

		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SakaiPersonImpl> query = cb.createQuery(SakaiPersonImpl.class);
		Root<SakaiPersonImpl> root = query.from(SakaiPersonImpl.class);
		query.select(root).where(cb.isTrue(root.get(FERPA_ENABLED)));
		return sessionFactory.getCurrentSession().createQuery(query).getResultList();
	}

    private List<SakaiPerson> getDiskPhotosForList(List<SakaiPerson> listIn) {
        return listIn.stream()
                .map(sp -> {
                    if (sp.getAgentUuid() != null &&
                            sp.getTypeUuid().equals(systemMutableType.getUuid())) {
                        sp.setJpegPhoto(photoService.getPhotoAsByteArray(sp.getAgentUuid()));
                    }
                    return sp;
                })
                .collect(Collectors.toList());
	}

}
