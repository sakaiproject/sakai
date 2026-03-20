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

package org.sakaiproject.alias.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.SingleStorageUser;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of the AliasService that provides core functionality for managing aliases
 * in the system. This abstract class handles alias creation, modification, removal, and lookup
 * operations, along with caching, security checks, and entity management.
 *
 * <p>
 * An alias is a mapping from an alternative identifier to a target resource reference within
 * the system. This service manages these mappings and enforces security permissions for alias
 * operations.
 * </p>
 *
 * <p>
 * This class provides a framework for alias persistence through a Storage interface that must
 * be implemented by concrete subclasses. It includes caching support to optimize alias lookups
 * and provides both direct alias management methods and convenience methods for setting and
 * removing aliases.
 * </p>
 *
 * <h3>Key features:</h3>
 * <ul>
 * <li>Alias creation with validation and security checks</li>
 * <li>Target resource reference lookup by alias</li>
 * <li>Multiple aliases per target resource support</li>
 * <li>Configurable caching with automatic cleanup</li>
 * <li>Prohibited alias patterns to prevent reserved names</li>
 * <li>Live property management for tracking creation and modification</li>
 * <li>Security permission checking for all operations</li>
 * <li>Entity provider integration for reference parsing</li>
 * <li>Batch operations and pagination support</li>
 * <li>Search functionality across aliases</li>
 * </ul>
 *
 * <h3>Concrete implementations must provide:</h3>
 * <ul>
 * <li>Storage implementation via newStorage() method</li>
 * <li>Service dependency injection methods for required collaborating services</li>
 * </ul>
 *
 * <p>
 * <strong>Thread Safety:</strong> This class uses caching and synchronization mechanisms to ensure thread-safe
 * operations when managing aliases.
 * </p>
 *
 * <p>
 * <strong>Security:</strong> All modification operations check appropriate permissions before execution.
 * Read operations may have configurable security requirements based on the target resource.
 * </p>
 */
@Slf4j
public abstract class BaseAliasService implements AliasService, SingleStorageUser {

    @Setter protected MemoryService memoryService;
    @Setter protected ServerConfigurationService serverConfigurationService;
    @Setter protected EntityManager entityManager;
    @Setter protected SecurityService securityService;
    @Setter protected SessionManager sessionManager;
    @Setter protected SiteService siteService;
    @Setter protected TimeService timeService;
    @Setter protected FunctionManager functionManager;
    @Setter protected EventTrackingService eventTrackingService;
    @Setter protected UserDirectoryService userDirectoryService;

    protected Storage storage = null; // Storage manager for this service
    protected String relativeAccessPoint = null; // The initial portion of a relative access point URL
    protected int cacheSeconds = 0; // The # seconds to cache gets. 0 disables the cache
    protected int cacheCleanerSeconds = 0;
    private List<String> prohibitedAliases = null;

    protected abstract Storage newStorage();

    /**
     * Access the partial URL that forms the root of resource URLs.
     *
     * @param relative
     *        if true, form within the access path only (i.e. starting with /content)
     * @return the partial URL that forms the root of resource URLs.
     */
    protected String getAccessPoint(boolean relative) {
        return (relative ? "" : serverConfigurationService.getAccessUrl()) + relativeAccessPoint;
    }

    /**
     * Access the internal reference which can be used to access the resource from within the system.
     *
     * @param id
     *        The alias id string.
     * @return The the internal reference which can be used to access the resource from within the system.
     */
    public String aliasReference(String id) {
        return getAccessPoint(true) + Entity.SEPARATOR + id;
    }

    /**
     * Access the alias id extracted from a alias reference.
     *
     * @param ref
     *        The alias reference string.
     * @return The the alias id extracted from a alias reference.
     */
    protected String aliasId(String ref) {
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
     * @return true if allowed, false if not
     */
    protected boolean unlockCheck(String lock, String resource) {
        return securityService.unlock(lock, resource);
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
    protected void unlock(String lock, String resource) throws PermissionException {
        if (!unlockCheck(lock, resource)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), lock, resource);
        }
    }

    /**
     * Check security permission, target modify based.
     *
     * @param target
     *        The target resource reference string.
     * @return true if allowed, false if not
     */
    protected boolean unlockTargetCheck(String target) {
        // check the target for modify access.
        // TODO: this is setup only for sites and mail archive channels, we need an Entity Model based generic "allowModify()" -ggolden.
        Reference ref = entityManager.newReference(target);
        if (ref.getType().equals(SiteService.APPLICATION_ID)) {
            // For all site references (page/site/group check against the actual site. We don't use the context because
            // site references have a context of null.
            return siteService.allowUpdateSite(ref.getContainer());
        }

        // TODO: fake this dependency (MailArchiveService.APPLICATION_ID) to keep the mailarchive dependencies away -ggolden
        else if (ref.getType().equals("sakai:mailarchive")) {
            // base this on site update, too
            log.debug("checing allow update on " + ref.getContext());
            // due to a bug in the mailarchive entity manager the context may be the strign null
            if (ref.getContext() != null && !ref.getContext().equals("null")) {
                log.debug("Checking allow update on " + ref.getContext() + " with lenght: " + ref.getContext().length());
                return siteService.allowUpdateSite(ref.getContext());
            } else {
                boolean ret = siteService.allowAddSite(null);
                log.debug("Cheking site.add permission returning: " + ret);
                return ret;
            }
        }

        // TODO: fake this dependency (CalendarService.APPLICATION_ID) to keep the calendar dependencies away
        else if (ref.getType().equals("sakai:calendar")) {
            // base this on site update, too
            return siteService.allowUpdateSite(ref.getContext());
        }

        // TODO: fake this dependency (AnnouncementService.APPLICATION_ID) to keep the announcement dependencies away
        else if (ref.getType().equals("sakai:announcement")) {
            // base this on site update, too
            return siteService.allowUpdateSite(ref.getContext());
        }

        return false;
    }

    /**
     * Create the live properties for the user.
     */
    protected void addLiveProperties(ResourcePropertiesEdit props) {
        String current = sessionManager.getCurrentSessionUserId();

        props.addProperty(ResourceProperties.PROP_CREATOR, current);
        props.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

        String now = timeService.newTime().toString();
        props.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
        props.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);
    }

    /**
     * Update the live properties for a user for when modified.
     */
    protected void addLiveUpdateProperties(ResourcePropertiesEdit props) {
        String current = sessionManager.getCurrentSessionUserId();

        props.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);
        props.addProperty(ResourceProperties.PROP_MODIFIED_DATE, timeService.newTime().toString());
    }

    /**
     * Set the # minutes to cache a get.
     *
     * @param time
     *        The # minutes to cache a get (as an integer string).
     */
    public void setCacheMinutes(String time) {
        cacheSeconds = Integer.parseInt(time) * 60;
    }

    /**
     * Set the # minutes between cache cleanings.
     *
     * @param time
     *        The # minutes between cache cleanings. (as an integer string).
     */
    public void setCacheCleanerMinutes(String time) {
        cacheCleanerSeconds = Integer.parseInt(time) * 60;
    }

    public void init() {
        relativeAccessPoint = REFERENCE_ROOT;

        // construct storage and read
        storage = newStorage();
        storage.open();

        // register as an entity producer
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);

        // register functions
        functionManager.registerFunction(SECURE_ADD_ALIAS);
        functionManager.registerFunction(SECURE_UPDATE_ALIAS);
        functionManager.registerFunction(SECURE_REMOVE_ALIAS);

        String aliases = serverConfigurationService.getString("mail.prohibitedaliases", "postmaster");
        prohibitedAliases = Arrays.asList(aliases.trim().toLowerCase().split(",\\s*"));
    }

    public void destroy() {
        storage.close();
        storage = null;
        log.info("Alias storage closed");
    }

    @Override
    public boolean allowSetAlias(String alias, String target) {
        if (!securityService.isSuperUser() &&
                prohibitedAliases.contains(alias.toLowerCase()))
            return false;
        return unlockTargetCheck(target);
    }

    @Override
    public void setAlias(String alias, String target) throws IdUsedException, IdInvalidException, PermissionException {
        if (alias != null && alias.length() > 99) { // KNL-454
            log.warn("The length of the alias: \"" + alias + "\" cannot be greater than 99 characters");
            throw new IdInvalidException(alias);
        }
        // check for a valid alias name
        Validator.checkResourceId(alias);

        if ((!securityService.isSuperUser() &&
                prohibitedAliases.contains(alias.toLowerCase())) ||
                !unlockTargetCheck(target)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ALIAS, target);
        }

        // attempt to register this alias with storage - if it's in use, this will return null
        AliasEdit a = storage.put(alias);
        if (a == null) {
            throw new IdUsedException(alias);
        }
        a.setTarget(target);

        // update the properties
        addLiveProperties(a.getPropertiesEdit());

        // complete the edit
        storage.commit(a);

        // track it
        eventTrackingService.post(eventTrackingService.newEvent(SECURE_ADD_ALIAS, aliasReference(alias), true));
    }


    @Override
    public boolean allowRemoveAlias(String alias) {
        return unlockCheck(SECURE_REMOVE_ALIAS, aliasReference(alias));
    }


    @Override
    public void removeAlias(String alias) throws IdUnusedException, PermissionException, InUseException {
        AliasEdit a = edit(alias);
        remove(a);
    }

    @Override
    public boolean allowRemoveTargetAliases(String target) {
        return unlockTargetCheck(target);
    }


    @Override
    public void removeTargetAliases(String target) throws PermissionException {
        if (!unlockTargetCheck(target)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_REMOVE_ALIAS, target);
        }

        List<Alias> all = getAliases(target);
        for (Iterator<Alias> iAll = all.iterator(); iAll.hasNext(); ) {
            Alias alias = (Alias) iAll.next();
            try {
                AliasEdit a = storage.edit(alias.getId());
                if (a != null) {
                    // complete the edit
                    storage.remove(a);

                    // track it
                    eventTrackingService.post(eventTrackingService.newEvent(SECURE_REMOVE_ALIAS, a.getReference(), true));
                }
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public String getTarget(String alias) throws IdUnusedException {
        // check the cache
        String ref = aliasReference(alias);

        BaseAliasEdit a = (BaseAliasEdit) storage.get(alias);
        if (a == null) throw new IdUnusedException(alias);

        return a.getTarget();
    }
    
    @Override
    public List<Alias> getAliases(String target) {
        List<Alias> allForTarget = storage.getAll(target);

        return allForTarget;
    }

    @Override
    public List<Alias> getAliases(String target, int first, int last) {
        List<Alias> allForTarget = storage.getAll(target, first, last);

        return allForTarget;
    }

    @Override
    public List<Alias> getAliases(int first, int last) {
        List<Alias> all = storage.getAll(first, last);

        return all;
    }

    @Override
    public int countAliases() {
        return storage.count();
    }

    @Override
    public List<Alias> searchAliases(String criteria, int first, int last) {
        return storage.search(criteria, first, last);
    }

    @Override
    public int countSearchAliases(String criteria) {
        return storage.countSearch(criteria);
    }

    @Override
    public boolean allowAdd() {
        return unlockCheck(SECURE_ADD_ALIAS, aliasReference(""));
    }

    @Override
    public AliasEdit add(String id) throws IdInvalidException, IdUsedException, PermissionException {
        if (id != null && id.length() > 99) { // KNL-454
            log.warn("The length of the alias: \"" + id + "\" cannot be greater than 99 characters");
            throw new IdInvalidException(id);
        }
        // check for a valid user name
        Validator.checkResourceId(id);

        // check security (throws if not permitted)
        unlock(SECURE_ADD_ALIAS, aliasReference(id));

        // reserve an alias with this id from the info store - if it's in use, this will return null
        AliasEdit a = storage.put(id);
        if (a == null) {
            throw new IdUsedException(id);
        }

        ((BaseAliasEdit) a).setEvent(SECURE_ADD_ALIAS);

        return a;
    }

    @Override
    public boolean allowEdit(String id) {
        return unlockCheck(SECURE_UPDATE_ALIAS, aliasReference(id));
    }


    @Override
    public AliasEdit edit(String id) throws IdUnusedException, PermissionException, InUseException {
        if (id == null) throw new IdUnusedException("null");

        // check security (throws if not permitted)
        unlock(SECURE_UPDATE_ALIAS, aliasReference(id));

        // check for existance
        if (!storage.check(id)) {
            throw new IdUnusedException(id);
        }

        // ignore the cache - get the user with a lock from the info store
        AliasEdit a = storage.edit(id);
        if (a == null) throw new InUseException(id);

        ((BaseAliasEdit) a).setEvent(SECURE_UPDATE_ALIAS);

        return a;
    }

    @Override
    public void commit(AliasEdit edit) {
        // check for closed edit
        if (!edit.isActiveEdit()) {
            try {
                throw new Exception();
            } catch (Exception e) {
                log.warn("commit(): closed AliasEdit", e);
            }
            return;
        }

        // If we're doing an update just change the modification
        if (SECURE_UPDATE_ALIAS.equals(((BaseAliasEdit) edit).getEvent())) {
            addLiveUpdateProperties(edit.getPropertiesEdit());
        } else {
            addLiveProperties(edit.getPropertiesEdit());
        }

        // complete the edit
        storage.commit(edit);

        // track it
        eventTrackingService.post(eventTrackingService.newEvent(((BaseAliasEdit) edit).getEvent(), edit.getReference(), true));

        // close the edit object
        ((BaseAliasEdit) edit).closeEdit();
    }


    @Override
    public void cancel(AliasEdit edit) {
        // check for closed edit
        if (!edit.isActiveEdit()) {
            try {
                throw new Exception();
            } catch (Exception e) {
                log.warn("cancel(): closed AliasEdit", e);
            }
            return;
        }

        // release the edit lock
        storage.cancel(edit);

        // close the edit object
        ((BaseAliasEdit) edit).closeEdit();
    }

    @Override
    public void remove(AliasEdit edit) throws PermissionException {
        // check for closed edit
        if (!edit.isActiveEdit()) {
            try {
                throw new Exception();
            } catch (Exception e) {
                log.warn("remove(): closed AliasEdit", e);
            }
            return;
        }

        // check security (throws if not permitted)
        unlock(SECURE_REMOVE_ALIAS, edit.getReference());

        // complete the edit
        storage.remove(edit);

        // track it
        eventTrackingService.post(eventTrackingService.newEvent(SECURE_REMOVE_ALIAS, edit.getReference(), true));

        // close the edit object
        ((BaseAliasEdit) edit).closeEdit();
    }

    @Override
    public String getLabel() {
        return "alias";
    }

    @Override
    public boolean parseEntityReference(String reference, Reference ref) {
        // for preferences access
        if (reference.startsWith(REFERENCE_ROOT)) {
            String id = null;

            // we will get null, service, userId
            String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

            if (parts.length > 2) {
                id = parts[2];
            }

            ref.set(APPLICATION_ID, null, id, null, null);

            return true;
        }
        return false;
    }

    @Override
    public Entity newResource(Entity container, String id, Object[] others) {
        return new BaseAliasEdit(id);
    }

    @Override
    public Entity newResource(Entity container, Element element) {
        return new BaseAliasEdit(element);
    }

    @Override
    public Entity newResource(Entity container, Entity other) {
        return new BaseAliasEdit((BaseAliasEdit) other);
    }

    @Override
    public Edit newResourceEdit(Entity container, String id, Object[] others) {
        BaseAliasEdit e = new BaseAliasEdit(id);
        e.activate();
        return e;
    }

    @Override
    public Edit newResourceEdit(Entity container, Element element) {
        BaseAliasEdit e = new BaseAliasEdit(element);
        e.activate();
        return e;
    }

    @Override
    public Edit newResourceEdit(Entity container, Entity other) {
        BaseAliasEdit e = new BaseAliasEdit((BaseAliasEdit) other);
        e.activate();
        return e;
    }

    @Override
    public Object[] storageFields(Entity r) {
        return null;
    }

    /**
     * Storage interface for managing alias persistence and retrieval operations.
     *
     * This interface defines the contract for storage implementations that handle the persistence
     * layer for alias management. It provides methods for CRUD operations (Create, Read, Update, Delete)
     * on alias records, along with search, filtering, and pagination capabilities.
     *
     * Implementations of this interface are responsible for:
     * - Managing the lifecycle of alias records in the underlying storage system
     * - Handling concurrent access through locking mechanisms
     * - Supporting efficient querying and retrieval of aliases by various criteria
     * - Managing associated properties for alias records
     *
     * All alias operations use case-insensitive identifiers for lookups and comparisons.
     * The interface supports optimistic locking through edit/commit/cancel patterns to ensure
     * data consistency in concurrent environments.
     *
     * Thread Safety: Implementations must handle concurrent access appropriately, particularly
     * for edit operations that involve locking mechanisms.
     */
    protected interface Storage {
        /**
         * Open.
         */
        void open();

        /**
         * Close.
         */
        void close();

        /**
         * Check if an alias with this id exists.
         *
         * @param id
         *        The alias id (case insensitive).
         * @return true if an alias by this id exists, false if not.
         */
        boolean check(String id);

        /**
         * Get the alias with this id, or null if not found.
         *
         * @param id
         *        The alias id (case insensitive).
         * @return The alias with this id, or null if not found.
         */
        AliasEdit get(String id);

        /**
         * Get all the alias.
         *
         * @return The List (BaseAliasEdit) of all alias.
         */
        List getAll();

        /**
         * Get all the alias in record range.
         *
         * @param first
         *        The first record position to return.
         * @param last
         *        The last record position to return.
         * @return The List (BaseAliasEdit) of all alias.
         */
        List getAll(int first, int last);

        /**
         * Count all the aliases.
         *
         * @return The count of all aliases.
         */
        int count();

        /**
         * Search for aliases with id or target matching criteria, in range.
         *
         * @param criteria
         *        The search criteria.
         * @param first
         *        The first record position to return.
         * @param last
         *        The last record position to return.
         * @return The List (BaseAliasEdit) of all alias.
         */
        List search(String criteria, int first, int last);

        /**
         * Count all the aliases with id or target matching criteria.
         *
         * @param criteria
         *        The search criteria.
         * @return The count of all aliases with id or target matching criteria.
         */
        int countSearch(String criteria);

        /**
         * Get all the alias that point at this target.
         *
         * @return The List (BaseAliasEdit) of all alias that point at this target
         */
        List getAll(String target);

        /**
         * Get all the alias that point at this target, in record range.
         *
         * @param first
         *        The first record position to return.
         * @param last
         *        The last record position to return.
         * @return The List (BaseAliasEdit) of all alias that point at this target, in record range.
         */
        List getAll(String target, int first, int last);

        /**
         * Add a new alias with this id.
         *
         * @param id
         *        The alias id.
         * @return The locked Alias object with this id, or null if the id is in use.
         */
        AliasEdit put(String id);

        /**
         * Get a lock on the alias with this id, or null if a lock cannot be gotten.
         *
         * @param id
         *        The alias id (case insensitive).
         * @return The locked Alias with this id, or null if this records cannot be locked.
         */
        AliasEdit edit(String id);

        /**
         * Commit the changes and release the lock.
         *
         * @param user
         *        The alias to commit.
         */
        void commit(AliasEdit alias);

        /**
         * Cancel the changes and release the lock.
         *
         * @param user
         *        The alias to commit.
         */
        void cancel(AliasEdit alias);

        /**
         * Remove this alias.
         *
         * @param user
         *        The alias to remove.
         */
        void remove(AliasEdit alias);

        /**
         * Read properties from storage into the edit's properties.
         *
         * @param edit
         *        The user to read properties for.
         */
        void readProperties(AliasEdit edit, ResourcePropertiesEdit props);
    }

    /**
     * <p>
     * BaseAlias is an implementation of the CHEF Alias object.
     * </p>
     *
     */
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public class BaseAliasEdit implements AliasEdit, SessionBindingListener {
        @EqualsAndHashCode.Include @Getter protected String id = null;
        @Setter @Getter protected String target = null;
        protected ResourcePropertiesEdit properties = null;
        protected String createdUserId = null;
        protected String lastModifiedUserId = null;
        @Getter protected Time createdTime = null;
        @Getter protected Time lastModifiedTime = null;
        @Getter @Setter protected String event = null;
        protected boolean active = false;

        public BaseAliasEdit(String id) {
            this.id = id;

            // setup for properties
            ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
            properties = props;

            // if not a reconstruction, add properties
            if ((this.id != null) && (!this.id.isEmpty())) addLiveProperties(props);
        }

        public BaseAliasEdit(String id, String target, String createdBy, Time createdOn, String modifiedBy, Time modifiedOn) {
            this.id = id;
            this.target = target;
            createdUserId = createdBy;
            lastModifiedUserId = modifiedBy;
            createdTime = createdOn;
            lastModifiedTime = modifiedOn;

            // setup for properties, but mark them lazy since we have not yet established them from data
            BaseResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
            props.setLazy(true);
            properties = props;
        }

        /**
         * Construct from another Alias object.
         *
         * @param alias
         *        The alias object to use for values.
         */
        public BaseAliasEdit(BaseAliasEdit alias) {
            setAll(alias);
        }

        /**
         * Construct from information in XML.
         *
         * @param el
         *        The XML DOM Element definining the alias.
         */
        public BaseAliasEdit(Element el) {
            // setup for properties
            properties = new BaseResourcePropertiesEdit();

            id = el.getAttribute("id");
            target = el.getAttribute("target");

            createdUserId = StringUtils.trimToNull(el.getAttribute("created-id"));
            lastModifiedUserId = StringUtils.trimToNull(el.getAttribute("modified-id"));

            String time = StringUtils.trimToNull(el.getAttribute("created-time"));
            if (time != null) {
                createdTime = timeService.newTimeGmt(time);
            }

            time = StringUtils.trimToNull(el.getAttribute("modified-time"));
            if (time != null) {
                lastModifiedTime = timeService.newTimeGmt(time);
            }

            // the children (properties)
            NodeList children = el.getChildNodes();
            final int length = children.getLength();
            for (int i = 0; i < length; i++) {
                Node child = children.item(i);
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;
                Element element = (Element) child;

                // look for properties
                if (element.getTagName().equals("properties")) {
                    // re-create properties
                    properties = new BaseResourcePropertiesEdit(element);

                    // pull out some properties into fields to convert old (pre 1.18) versions
                    if (createdUserId == null) {
                        createdUserId = properties.getProperty("CHEF:creator");
                    }
                    if (lastModifiedUserId == null) {
                        lastModifiedUserId = properties.getProperty("CHEF:modifiedby");
                    }
                    if (createdTime == null) {
                        try {
                            createdTime = properties.getTimeProperty("DAV:creationdate");
                        } catch (Exception ignore) {
                        }
                    }
                    if (lastModifiedTime == null) {
                        try {
                            lastModifiedTime = properties.getTimeProperty("DAV:getlastmodified");
                        } catch (Exception ignore) {
                        }
                    }
                    properties.removeProperty("CHEF:creator");
                    properties.removeProperty("CHEF:modifiedby");
                    properties.removeProperty("DAV:creationdate");
                    properties.removeProperty("DAV:getlastmodified");
                }
            }
        }

        @Override
        protected void finalize() {
            // catch the case where an edit was made but never resolved
            if (active) {
                cancel(this);
            }
        }

        /**
         * Take all values from this object.
         *
         * @param alias
         *        The alias object to take values from.
         */
        protected void setAll(BaseAliasEdit alias) {
            id = alias.id;
            target = alias.target;
            createdUserId = ((BaseAliasEdit) alias).createdUserId;
            lastModifiedUserId = ((BaseAliasEdit) alias).lastModifiedUserId;
            if (((BaseAliasEdit) alias).createdTime != null)
                createdTime = (Time) ((BaseAliasEdit) alias).createdTime.clone();
            if (((BaseAliasEdit) alias).lastModifiedTime != null)
                lastModifiedTime = (Time) ((BaseAliasEdit) alias).lastModifiedTime.clone();

            properties = new BaseResourcePropertiesEdit();
            properties.addAll(alias.getProperties());
            ((BaseResourcePropertiesEdit) properties).setLazy(((BaseResourceProperties) alias.getProperties()).isLazy());
        }

        /**
         * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
         *
         * @param doc
         *        The DOM doc to contain the XML (or null for a string return).
         * @param stack
         *        The DOM elements, the top of which is the containing element of the new "resource" element.
         * @return The newly added element.
         */
        public Element toXml(Document doc, Stack stack) {
            Element alias = doc.createElement("alias");

            if (stack.isEmpty()) {
                doc.appendChild(alias);
            } else {
                ((Element) stack.peek()).appendChild(alias);
            }

            stack.push(alias);

            alias.setAttribute("id", id);
            alias.setAttribute("target", target);
            alias.setAttribute("created-id", createdUserId);
            alias.setAttribute("modified-id", lastModifiedUserId);
            alias.setAttribute("created-time", createdTime.toString());
            alias.setAttribute("modified-time", lastModifiedTime.toString());

            getProperties().toXml(doc, stack);

            stack.pop();

            return alias;
        }

        @Override
        public User getCreatedBy() {
            try {
                return userDirectoryService.getUser(createdUserId);
            } catch (Exception e) {
                return userDirectoryService.getAnonymousUser();
            }
        }

        @Override
        public User getModifiedBy() {
            try {
                return userDirectoryService.getUser(lastModifiedUserId);
            } catch (Exception e) {
                return userDirectoryService.getAnonymousUser();
            }
        }

        @Override
        public Time getModifiedTime() {
            return lastModifiedTime;
        }

        @Override
        public Date getDateCreated() {
            return new Date(createdTime.getTime());
        }

        @Override
        public Date getDateModified() {
            return new Date(lastModifiedTime.getTime());
        }

        @Override
        public String getUrl() {
            return getAccessPoint(false) + id;

        }

        @Override
        public String getReference() {
            return aliasReference(id);
        }

        @Override
        public String getReference(String rootProperty) {
            return getReference();
        }

        @Override
        public String getUrl(String rootProperty) {
            return getUrl();
        }

        @Override
        public ResourceProperties getProperties() {
            // if lazy, resolve
            if (((BaseResourceProperties) properties).isLazy()) {
                ((BaseResourcePropertiesEdit) properties).setLazy(false);
                storage.readProperties(this, properties);
            }
            return properties;
        }

        @Override
        public int compareTo(Object obj) {
            if (!(obj instanceof BaseAliasEdit)) throw new ClassCastException();

            // if the object are the same, say so
            if (obj == this) return 0;

            // sort based on (unique) id
            int compare = getId().compareTo(((BaseAliasEdit) obj).getId());

            return compare;

        }

        @Override
        public ResourcePropertiesEdit getPropertiesEdit() {
            // if lazy, resolve
            if (((BaseResourceProperties) properties).isLazy()) {
                ((BaseResourcePropertiesEdit) properties).setLazy(false);
                storage.readProperties(this, properties);
            }
            return properties;
        }

        /**
         * Enable editing.
         */
        protected void activate() {
            active = true;
        }

        @Override
        public boolean isActiveEdit() {
            return active;
        }

        /**
         * Close the edit object - it cannot be used after this.
         */
        protected void closeEdit() {
            active = false;
        }

        @Override
        public String getDescription() {
            try {
                // the rest are references to some resource
                Reference ref = entityManager.newReference(getTarget());
                return ref.getDescription();
            } catch (Exception any) {
                return "unknown";
            }

        }

        public void valueBound(SessionBindingEvent event) {
        }

        public void valueUnbound(SessionBindingEvent event) {
            if (log.isDebugEnabled()) log.debug("valueUnbound()");

            // catch the case where an edit was made but never resolved
            if (active) {
                cancel(this);
            }
        }
    }
}
