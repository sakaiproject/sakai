/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PermissionLevelImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.TopicImpl;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class PermissionLevelManagerImpl implements PermissionLevelManager {

    @Setter private Boolean autoDdl;
    @Setter private IdManager idManager;
    @Setter private MessageForumsTypeManager typeManager;
    @Setter private PlatformTransactionManager transactionManager;
    @Setter private SessionManager sessionManager;
    @Setter private SessionFactory sessionFactory;

    private Map<String, PermissionLevel> defaultPermissionsMap;

    public void init() {
        log.info("Initializing permission level manager");

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    if (autoDdl != null && autoDdl) {
                        // add the default permission level and type data, if necessary
                        loadDefaultTypeAndPermissionLevelData();
                    }
                    initializePermissionLevelData();
                }
            });
        } catch (Exception ex) {
            log.warn("Failed to initialize default permission levels", ex);
        }
    }

    @Override
    public PermissionLevel getPermissionLevelByName(String name) {
        log.debug("Resolving permission level '{}'", name);
        if (StringUtils.isBlank(name)) return null;

        return switch (name) {
            case PERMISSION_LEVEL_NAME_OWNER -> getDefaultOwnerPermissionLevel();
            case PERMISSION_LEVEL_NAME_AUTHOR -> getDefaultAuthorPermissionLevel();
            case PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR -> getDefaultNoneditingAuthorPermissionLevel();
            case PERMISSION_LEVEL_NAME_CONTRIBUTOR -> getDefaultContributorPermissionLevel();
            case PERMISSION_LEVEL_NAME_REVIEWER -> getDefaultReviewerPermissionLevel();
            case PERMISSION_LEVEL_NAME_NONE -> getDefaultNonePermissionLevel();
            default -> null;
        };
    }

    @Override
    public List<String> getOrderedPermissionLevelNames() {
        List<String> levelNames = getDefaultPermissionLevels().stream()
                .map(PermissionLevel::getName)
                .sorted()
                .collect(Collectors.toList());
        log.debug("level names: [{}]", levelNames);
        return levelNames;
    }

    @Override
    public String getPermissionLevelType(PermissionLevel level) {

        log.debug("Resolving permission level type for {}", level);

        if (level == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        PermissionLevel ownerLevel = getDefaultOwnerPermissionLevel();
        if (level.equals(ownerLevel)) {
            return ownerLevel.getTypeUuid();
        }

        PermissionLevel authorLevel = getDefaultAuthorPermissionLevel();
        if (level.equals(authorLevel)) {
            return authorLevel.getTypeUuid();
        }

        PermissionLevel nonEditingAuthorLevel = getDefaultNoneditingAuthorPermissionLevel();
        if (level.equals(nonEditingAuthorLevel)) {
            return nonEditingAuthorLevel.getTypeUuid();
        }

        PermissionLevel reviewerLevel = getDefaultReviewerPermissionLevel();
        if (level.equals(reviewerLevel)) {
            return reviewerLevel.getTypeUuid();
        }

        PermissionLevel contributorLevel = getDefaultContributorPermissionLevel();
        if (level.equals(contributorLevel)) {
            return contributorLevel.getTypeUuid();
        }

        PermissionLevel noneLevel = getDefaultNonePermissionLevel();
        if (level.equals(noneLevel)) {
            return noneLevel.getTypeUuid();
        }

        return null;
    }

    @Override
    public PermissionLevel createPermissionLevel(String name, String typeUuid, Map<String, Boolean> mask) {

        log.debug("Creating permission level '{}' for type '{}'", name, typeUuid);

        if (name == null || typeUuid == null || mask == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        PermissionLevel newPermissionLevel = new PermissionLevelImpl();
        Date now = new Date();
        String currentUser = getCurrentUser();
        newPermissionLevel.setName(name);
        newPermissionLevel.setUuid(idManager.createUuid());
        newPermissionLevel.setCreated(now);
        newPermissionLevel.setCreatedBy(currentUser);
        newPermissionLevel.setModified(now);
        newPermissionLevel.setModifiedBy(currentUser);
        newPermissionLevel.setTypeUuid(typeUuid);

        // set permission properties using reflection
        for (Entry<String, Boolean> entry : mask.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();
            try {
                PropertyUtils.setSimpleProperty(newPermissionLevel, key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return newPermissionLevel;
    }

    @Override
    public DBMembershipItem createDBMembershipItem(String name, String permissionLevelName, Integer type) {

        log.debug("Creating membership item '{}' with type {}", name, type);

        if (name == null || type == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        DBMembershipItem newDBMembershipItem = new DBMembershipItemImpl();
        Date now = new Date();
        String currentUser = getCurrentUser();
        newDBMembershipItem.setName(name);
        newDBMembershipItem.setPermissionLevelName(permissionLevelName);
        newDBMembershipItem.setUuid(idManager.createUuid());
        newDBMembershipItem.setCreated(now);
        newDBMembershipItem.setCreatedBy(currentUser);
        newDBMembershipItem.setModified(now);
        newDBMembershipItem.setModifiedBy(currentUser);
        newDBMembershipItem.setType(type);

        return newDBMembershipItem;
    }

    @Override
    public DBMembershipItem saveDBMembershipItem(DBMembershipItem item) {
        return (DBMembershipItem) sessionFactory.getCurrentSession().merge(item);
    }

    @Override
    public PermissionLevel savePermissionLevel(PermissionLevel level) {
        return (PermissionLevel) sessionFactory.getCurrentSession().merge(level);
    }

    @Override
    public PermissionLevel getDefaultOwnerPermissionLevel() {
        log.debug("Loading owner permission level");

        String typeUuid = typeManager.getOwnerLevelType();

        if (typeUuid == null) {
            throw new IllegalStateException("type cannot be null");
        }
        PermissionLevel level = getDefaultPermissionLevel(typeUuid);

        if (level == null) {

            logMissingPermissionLevelData(PERMISSION_LEVEL_NAME_OWNER);

            // return the default owner permission
            Map<String, Boolean> mask = getDefaultOwnerPermissionsMask();
            level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_OWNER, typeUuid, mask);
        }

        return level;
    }

    @Override
    public PermissionLevel getDefaultAuthorPermissionLevel() {
        log.debug("Loading author permission level");

        String typeUuid = typeManager.getAuthorLevelType();

        if (typeUuid == null) {
            throw new IllegalStateException("type cannot be null");
        }
        PermissionLevel level = getDefaultPermissionLevel(typeUuid);

        if (level == null) {
            logMissingPermissionLevelData(PERMISSION_LEVEL_NAME_AUTHOR);

            // return the default author permission
            Map<String, Boolean> mask = getDefaultAuthorPermissionsMask();
            level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_AUTHOR, typeUuid, mask);
        }

        return level;
    }

    @Override
    public PermissionLevel getDefaultNoneditingAuthorPermissionLevel() {
        log.debug("Loading non-editing author permission level");

        String typeUuid = typeManager.getNoneditingAuthorLevelType();

        if (typeUuid == null) {
            throw new IllegalStateException("type cannot be null");
        }
        PermissionLevel level = getDefaultPermissionLevel(typeUuid);

        if (level == null) {
            logMissingPermissionLevelData(PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR);

            // return the default non-editing author permission
            Map<String, Boolean> mask = getDefaultNonEditingAuthorPermissionsMask();
            level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR, typeUuid, mask);

        }

        return level;
    }

    @Override
    public PermissionLevel getDefaultReviewerPermissionLevel() {
        log.debug("Loading reviewer permission level");

        String typeUuid = typeManager.getReviewerLevelType();

        if (typeUuid == null) {
            throw new IllegalStateException("type cannot be null");
        }
        PermissionLevel level = getDefaultPermissionLevel(typeUuid);

        if (level == null) {
            logMissingPermissionLevelData(PERMISSION_LEVEL_NAME_REVIEWER);

            // return the default reviewer permission
            Map<String, Boolean> mask = getDefaultReviewerPermissionsMask();
            level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_REVIEWER, typeUuid, mask);

        }

        return level;
    }

    @Override
    public PermissionLevel getDefaultContributorPermissionLevel() {
        log.debug("Loading contributor permission level");

        String typeUuid = typeManager.getContributorLevelType();

        if (typeUuid == null) {
            throw new IllegalStateException("type cannot be null");
        }
        PermissionLevel level = getDefaultPermissionLevel(typeUuid);

        if (level == null) {
            logMissingPermissionLevelData(PERMISSION_LEVEL_NAME_CONTRIBUTOR);

            // return the default contributor permission
            Map<String, Boolean> mask = getDefaultContributorPermissionsMask();
            level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR, typeUuid, mask);
        }

        return level;
    }

    @Override
    public PermissionLevel getDefaultNonePermissionLevel() {
        log.debug("Loading none permission level");

        String typeUuid = typeManager.getNoneLevelType();

        if (typeUuid == null) {
            throw new IllegalStateException("type cannot be null");
        }
        PermissionLevel level = getDefaultPermissionLevel(typeUuid);

        if (level == null) {
            logMissingPermissionLevelData(PERMISSION_LEVEL_NAME_NONE);

            // return the default None permission
            Map<String, Boolean> mask = getDefaultNonePermissionsMask();
            level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE, typeUuid, mask);
        }

        return level;
    }

    /**
     * Retrieves the default PermissionLevel for the specified type UUID. This method first checks
     * the defaultPermissionsMap for the PermissionLevel. If not found, it queries the database.
     * Returns a cloned instance when retrieved from cache to prevent
     * unintended modifications to default objects.
     *
     * @param typeUuid the unique identifier of the permission level type
     * @return the PermissionLevel for the given typeUuid, or null if no PermissionLevel is found
     */
    private PermissionLevel getDefaultPermissionLevel(final String typeUuid) {

        if (typeUuid == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("Resolving permission level for type '{}'", typeUuid);

        PermissionLevel level;

        if (defaultPermissionsMap != null && defaultPermissionsMap.containsKey(typeUuid)) {
            // check to see if it is already in the map that was created at startup
            level = defaultPermissionsMap.get(typeUuid).clone();
            log.debug("Using cached permission level for type '{}': {}", typeUuid, level);

        } else {
            // retrieve it from the table
            CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
            CriteriaQuery<PermissionLevelImpl> cq = cb.createQuery(PermissionLevelImpl.class);
            Root<PermissionLevelImpl> root = cq.from(PermissionLevelImpl.class);
            cq.select(root).where(cb.equal(root.get("typeUuid"), typeUuid));
            level = sessionFactory.getCurrentSession().createQuery(cq).uniqueResult();

            log.debug("Loaded permission level from database for type '{}': {}", typeUuid, level);
        }
        return level;
    }

    @Override
    public Boolean getCustomPermissionByName(String customPermName, PermissionLevel permissionLevel) {
        if (customPermName == null)
            throw new IllegalArgumentException("Null permissionLevelName passed");
        if (permissionLevel == null)
            throw new IllegalArgumentException("Null permissionLevel passed");

        return switch (customPermName) {
            case PermissionLevel.NEW_FORUM -> permissionLevel.getNewForum();
            case PermissionLevel.NEW_RESPONSE -> permissionLevel.getNewResponse();
            case PermissionLevel.NEW_RESPONSE_TO_RESPONSE -> permissionLevel.getNewResponseToResponse();
            case PermissionLevel.NEW_TOPIC -> permissionLevel.getNewTopic();
            case PermissionLevel.POST_TO_GRADEBOOK -> permissionLevel.getPostToGradebook();
            case PermissionLevel.DELETE_ANY -> permissionLevel.getDeleteAny();
            case PermissionLevel.DELETE_OWN -> permissionLevel.getDeleteOwn();
            case PermissionLevel.MARK_AS_NOT_READ -> permissionLevel.getMarkAsNotRead();
            case PermissionLevel.MODERATE_POSTINGS -> permissionLevel.getModeratePostings();
            case PermissionLevel.IDENTIFY_ANON_AUTHORS -> permissionLevel.getIdentifyAnonAuthors();
            case PermissionLevel.MOVE_POSTING -> permissionLevel.getMovePosting();
            case PermissionLevel.READ -> permissionLevel.getRead();
            case PermissionLevel.REVISE_ANY -> permissionLevel.getReviseAny();
            case PermissionLevel.REVISE_OWN -> permissionLevel.getReviseOwn();
            case PermissionLevel.CHANGE_SETTINGS -> permissionLevel.getChangeSettings();
            default -> null;
        };
    }

    @Override
    public List<String> getCustomPermissions() {
        return List.of(
                PermissionLevel.NEW_FORUM,
                PermissionLevel.NEW_RESPONSE,
                PermissionLevel.NEW_RESPONSE_TO_RESPONSE,
                PermissionLevel.NEW_TOPIC,
                PermissionLevel.DELETE_ANY,
                PermissionLevel.DELETE_OWN,
                PermissionLevel.MARK_AS_NOT_READ,
                PermissionLevel.MODERATE_POSTINGS,
                PermissionLevel.IDENTIFY_ANON_AUTHORS,
                PermissionLevel.MOVE_POSTING,
                PermissionLevel.POST_TO_GRADEBOOK,
                PermissionLevel.READ,
                PermissionLevel.REVISE_ANY,
                PermissionLevel.REVISE_OWN,
                PermissionLevel.CHANGE_SETTINGS
        );
    }


    private String getCurrentUser() {
        String user = sessionManager.getCurrentSessionUserId();
        return (user == null) ? "test-user" : user;
    }

    @Override
    public List<DBMembershipItem> getAllMembershipItemsForForumsForSite(final Long areaId) {
        log.debug("Loading forum membership items for area {}", areaId);

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<DBMembershipItemImpl> cq = cb.createQuery(DBMembershipItemImpl.class);
        Root<DBMembershipItemImpl> m = cq.from(DBMembershipItemImpl.class);
        Join<Object, Object> f = m.join("forum");
        Join<Object, Object> a = f.join("area");
        m.fetch("permissionLevel", JoinType.LEFT);
        cq.select(m).where(cb.equal(a.get("id"), areaId));
        List<DBMembershipItemImpl> results = sessionFactory.getCurrentSession().createQuery(cq).getResultList();
        return new ArrayList<>(results);
    }

    private List<Long> getAllTopicsForSite(final Long areaId) {
        log.debug("Fetching topic ids for area {}", areaId);
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<TopicImpl> t = cq.from(TopicImpl.class);
        Join<Object, Object> f = t.join("openForum");
        Join<Object, Object> a = f.join("area");
        cq.select(t.get("id")).where(cb.equal(a.get("id"), areaId));
        return sessionFactory.getCurrentSession().createQuery(cq).getResultList();
    }

    @Override
    public List<DBMembershipItem> getAllMembershipItemsForTopicsForSite(final Long areaId) {
        final List<Long> topicIds = getAllTopicsForSite(areaId);
        if (topicIds.isEmpty()) return new ArrayList<>();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<DBMembershipItemImpl> cq = cb.createQuery(DBMembershipItemImpl.class);
        Root<DBMembershipItemImpl> m = cq.from(DBMembershipItemImpl.class);
        Join<Object, Object> t = m.join("topic");
        Predicate topicIdsPredicate = HibernateCriterionUtils.PredicateInSplitter(cb, t.get("id"), topicIds);
        m.fetch("permissionLevel", JoinType.LEFT);
        cq.select(m).where(topicIdsPredicate);
        List<DBMembershipItemImpl> results = sessionFactory.getCurrentSession().createQuery(cq).getResultList();
        return new ArrayList<>(results);
    }

    private void initializePermissionLevelData() {
        log.debug("Initializing default permission level cache");

        defaultPermissionsMap = new HashMap<>();
        defaultPermissionsMap.put(typeManager.getOwnerLevelType(), getDefaultOwnerPermissionLevel());
        defaultPermissionsMap.put(typeManager.getAuthorLevelType(), getDefaultAuthorPermissionLevel());
        defaultPermissionsMap.put(typeManager.getNoneditingAuthorLevelType(), getDefaultNoneditingAuthorPermissionLevel());
        defaultPermissionsMap.put(typeManager.getContributorLevelType(), getDefaultContributorPermissionLevel());
        defaultPermissionsMap.put(typeManager.getReviewerLevelType(), getDefaultReviewerPermissionLevel());
        defaultPermissionsMap.put(typeManager.getNoneLevelType(), getDefaultNonePermissionLevel());
    }

    private void loadDefaultTypeAndPermissionLevelData() {
        try {
            // first, call the methods that will load type data if it is missing
            String ownerType = typeManager.getOwnerLevelType();
            String authorType = typeManager.getAuthorLevelType();
            String contributorType = typeManager.getContributorLevelType();
            String reviewerType = typeManager.getReviewerLevelType();
            String nonEditingAuthorType = typeManager.getNoneditingAuthorLevelType();
            String noneType = typeManager.getNoneLevelType();

            // now let's check to see if we need to add the default permission level data
            if (getDefaultPermissionLevel(ownerType) == null) {
                Map<String, Boolean> mask = getDefaultOwnerPermissionsMask();
                savePermissionLevel(createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_OWNER, ownerType, mask));
            }

            if (getDefaultPermissionLevel(authorType) == null) {
                Map<String, Boolean> mask = getDefaultAuthorPermissionsMask();
                savePermissionLevel(createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_AUTHOR, authorType, mask));
            }

            if (getDefaultPermissionLevel(contributorType) == null) {
                Map<String, Boolean> mask = getDefaultContributorPermissionsMask();
                savePermissionLevel(createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR, contributorType, mask));
            }

            if (getDefaultPermissionLevel(reviewerType) == null) {
                Map<String, Boolean> mask = getDefaultReviewerPermissionsMask();
                savePermissionLevel(createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_REVIEWER, reviewerType, mask));
            }

            if (getDefaultPermissionLevel(nonEditingAuthorType) == null) {
                Map<String, Boolean> mask = getDefaultNonEditingAuthorPermissionsMask();
                savePermissionLevel(createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR, nonEditingAuthorType, mask));
            }

            if (getDefaultPermissionLevel(noneType) == null) {
                Map<String, Boolean> mask = getDefaultNonePermissionsMask();
                savePermissionLevel(createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE, noneType, mask));
            }
        } catch (Exception e) {
            log.warn("Error loading initial default types and/or permissions", e);
        }
    }

    @Override
    public void deleteMembershipItems(Set<DBMembershipItem> membershipSet) {
        if (membershipSet != null && !membershipSet.isEmpty()) {
            Set<PermissionLevel> permissionLevelsToDelete = new HashSet<>();
            Set<DBMembershipItem> membershipItemsToDelete = new HashSet<>();
            Session session = sessionFactory.getCurrentSession();
            for (DBMembershipItem item : membershipSet) {
                if (item != null) {
                    DBMembershipItem managedItem = session.get(DBMembershipItemImpl.class, item.getId());
                    if (managedItem != null) {
                        membershipItemsToDelete.add(managedItem);
                        if (managedItem.getPermissionLevel() != null) {
                            PermissionLevel managedLevel = session.get(PermissionLevelImpl.class, managedItem.getPermissionLevel().getId());
                            if (managedLevel != null) {
                                permissionLevelsToDelete.add(managedLevel);
                            }
                        }
                    }
                }
            }
            membershipItemsToDelete.forEach(session::delete);
            permissionLevelsToDelete.forEach(session::delete);
        }
    }

    private Map<String, Boolean> getDefaultOwnerPermissionsMask() {
        Map<String, Boolean> mask = new HashMap<>();
        mask.put(PermissionLevel.NEW_FORUM, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_TOPIC, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_RESPONSE, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.TRUE);
        mask.put(PermissionLevel.MOVE_POSTING, Boolean.TRUE);
        mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.TRUE);
        mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.TRUE);
        mask.put(PermissionLevel.READ, Boolean.TRUE);
        mask.put(PermissionLevel.MARK_AS_NOT_READ, Boolean.TRUE);
        mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.TRUE);
        mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.TRUE);
        mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_ANY, Boolean.TRUE);
        mask.put(PermissionLevel.REVISE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_ANY, Boolean.TRUE);
        return mask;
    }

    private Map<String, Boolean> getDefaultAuthorPermissionsMask() {
        Map<String, Boolean> mask = new HashMap<>();
        mask.put(PermissionLevel.NEW_FORUM, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_TOPIC, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_RESPONSE, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.TRUE);
        mask.put(PermissionLevel.MOVE_POSTING, Boolean.TRUE);
        mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.TRUE);
        mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.TRUE);
        mask.put(PermissionLevel.READ, Boolean.TRUE);
        mask.put(PermissionLevel.MARK_AS_NOT_READ, Boolean.TRUE);
        mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
        mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_OWN, Boolean.TRUE);
        mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_OWN, Boolean.TRUE);
        mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
        return mask;
    }

    private Map<String, Boolean> getDefaultContributorPermissionsMask() {
        Map<String, Boolean> mask = new HashMap<>();
        mask.put(PermissionLevel.NEW_FORUM, Boolean.FALSE);
        mask.put(PermissionLevel.NEW_TOPIC, Boolean.FALSE);
        mask.put(PermissionLevel.NEW_RESPONSE, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.TRUE);
        mask.put(PermissionLevel.MOVE_POSTING, Boolean.FALSE);
        mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.FALSE);
        mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.FALSE);
        mask.put(PermissionLevel.READ, Boolean.TRUE);
        mask.put(PermissionLevel.MARK_AS_NOT_READ, Boolean.TRUE);
        mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
        mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
        return mask;
    }

    private Map<String, Boolean> getDefaultNonEditingAuthorPermissionsMask() {
        Map<String, Boolean> mask = new HashMap<>();
        mask.put(PermissionLevel.NEW_FORUM, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_TOPIC, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_RESPONSE, Boolean.TRUE);
        mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.TRUE);
        mask.put(PermissionLevel.MOVE_POSTING, Boolean.FALSE);
        mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.TRUE);
        mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.TRUE);
        mask.put(PermissionLevel.READ, Boolean.TRUE);
        mask.put(PermissionLevel.MARK_AS_NOT_READ, Boolean.TRUE);
        mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
        mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_OWN, Boolean.TRUE);
        mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
        return mask;
    }

    private Map<String, Boolean> getDefaultNonePermissionsMask() {
        Map<String, Boolean> mask = new HashMap<>();
        mask.put(PermissionLevel.NEW_FORUM, Boolean.FALSE);
        mask.put(PermissionLevel.NEW_TOPIC, Boolean.FALSE);
        mask.put(PermissionLevel.NEW_RESPONSE, Boolean.FALSE);
        mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.FALSE);
        mask.put(PermissionLevel.MOVE_POSTING, Boolean.FALSE);
        mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.FALSE);
        mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.FALSE);
        mask.put(PermissionLevel.READ, Boolean.FALSE);
        mask.put(PermissionLevel.MARK_AS_NOT_READ, Boolean.FALSE);
        mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
        mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
        return mask;
    }

    private Map<String, Boolean> getDefaultReviewerPermissionsMask() {
        Map<String, Boolean> mask = new HashMap<>();
        mask.put(PermissionLevel.NEW_FORUM, Boolean.FALSE);
        mask.put(PermissionLevel.NEW_TOPIC, Boolean.FALSE);
        mask.put(PermissionLevel.NEW_RESPONSE, Boolean.FALSE);
        mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.FALSE);
        mask.put(PermissionLevel.MOVE_POSTING, Boolean.FALSE);
        mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.FALSE);
        mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.FALSE);
        mask.put(PermissionLevel.READ, Boolean.TRUE);
        mask.put(PermissionLevel.MARK_AS_NOT_READ, Boolean.TRUE);
        mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
        mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_OWN, Boolean.FALSE);
        mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
        return mask;
    }

    private List<PermissionLevel> getDefaultPermissionLevels() {
        // first, check for the levels in the Map. If Map is null,
        // return the default permission level data
        List<PermissionLevel> defaultLevels = new ArrayList<>();
        if (defaultPermissionsMap != null && !defaultPermissionsMap.isEmpty()) {
            defaultLevels.addAll(defaultPermissionsMap.values());
        } else {
            log.debug("Default permission cache is empty; loading built-in levels");
            defaultLevels.add(getDefaultOwnerPermissionLevel());
            defaultLevels.add(getDefaultAuthorPermissionLevel());
            defaultLevels.add(getDefaultContributorPermissionLevel());
            defaultLevels.add(getDefaultNoneditingAuthorPermissionLevel());
            defaultLevels.add(getDefaultNonePermissionLevel());
            defaultLevels.add(getDefaultReviewerPermissionLevel());
        }

        return defaultLevels;
    }

    private void logMissingPermissionLevelData(String levelName) {
        log.info("No stored '{}' permission level found; using built-in defaults.", levelName);
    }

}
