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

package org.sakaiproject.event.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationLockedException;
import org.sakaiproject.event.api.NotificationNotDefinedException;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.SingleStorageUser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of the NotificationService interface providing core notification management functionality.
 * <p>
 * This abstract class serves as the foundation for notification services in the system, handling the creation,
 * storage, retrieval, and lifecycle management of notifications. It provides support for both persistent and
 * transient notifications, manages notification references and URLs, and integrates with the event tracking
 * system to observe and respond to application events.
 * </p>
 * <p>
 * The service maintains notifications in a storage backend (implemented by concrete subclasses) and provides
 * caching capabilities through the memory service. It supports filtering and matching of notifications based
 * on resource references and functions, enabling selective notification delivery.
 * </p>
 * <h3>Key responsibilities include:</h3>
 * <ul>
 * <li>Creating and managing notification instances (both persistent and transient)</li>
 * <li>Locking and editing notifications with proper concurrency control</li>
 * <li>Generating notification references and external URLs</li>
 * <li>Filtering notifications by function and resource reference patterns</li>
 * <li>Configuring email reply-ability for notification recipients</li>
 * <li>Integrating with the event system as an observer</li>
 * <li>Managing notification lifecycle through init and destroy operations</li>
 * <li>Providing cache refresh capabilities for distributed environments</li>
 * </ul>
 * <p>
 * Subclasses must implement the newStorage() method to provide the specific storage mechanism
 * for persisting notifications, such as database or file-based storage.
 * </p>
 * <h3>The service supports two types of notifications:</h3>
 * <ul>
 * <li>Persistent notifications: stored in the configured storage backend and cached</li>
 * <li>Transient notifications: temporary notifications not persisted to storage</li>
 * </ul>
 * <p>
 * Thread safety is managed through the storage layer's locking mechanisms for edit operations.
 * </p>
 *
 * @see NotificationService
 * @see Notification
 * @see NotificationEdit
 */
@Slf4j
public abstract class BaseNotificationService implements NotificationService, Observer, SingleStorageUser, CacheRefresher, ApplicationContextAware {
    
    protected Storage storage = null; // Storage manager for this service
    protected String relativeAccessPoint = null; // The initial portion of a relative access point URL
    protected List<Notification> transients = null; // Transient notifications (NotificationEdit)
    protected boolean emailsToReplyable = false; // make the email notifications To: reply-able
    protected boolean emailsFromReplyable = false; // make the email notifications From: reply-able

    @Setter protected ApplicationContext applicationContext;
    @Setter protected EventTrackingService eventTrackingService;
    @Setter protected IdManager idManager;
    @Setter protected MemoryService memoryService;
    @Setter protected ServerConfigurationService serverConfigurationService;

    /**
     * Construct storage for this service.
     */
    protected abstract Storage newStorage();

    /**
     * Does the resource reference match the filter?
     *
     * @param filter
     *        The resource reference filter.
     * @param ref
     *        The resource reference string.
     * @return true if the filter matches the ref, false if not.
     */
    protected boolean match(String filter, String ref) {
        if (filter == null) return true;
        if (filter.isEmpty()) return true;
        return ref.startsWith(filter);
    }

    /**
     * Access the partial URL that forms the root of resource URLs.
     *
     * @param relative
     *        if true, form within the access path only (i.e., starting with /content)
     * @return the partial URL that forms the root of resource URLs.
     */
    protected String getAccessPoint(boolean relative) {
        return (relative ? "" : serverConfigurationService.getAccessUrl()) + relativeAccessPoint;
    }

    /**
     * Access the notification id extracted from a notification reference.
     *
     * @param ref
     *        The notification reference string.
     * @return The notification id extracted from a notification reference.
     */
    protected String notificationId(String ref) {
        String start = getAccessPoint(true) + Entity.SEPARATOR;
        int i = ref.indexOf(start);
        if (i == -1) return ref;
        return ref.substring(i + start.length());
    }

    /**
     * Access the external URL which can be used to access the resource from outside the system.
     *
     * @param id
     *        The notification id.
     * @return The external URL which can be used to access the resource from outside the system.
     */
    protected String notificationUrl(String id) {
        return getAccessPoint(false) + Entity.SEPARATOR + id;
    }

    /**
     * Configuration: set a reply-able status for email notifications in the To:
     *
     * @param value
     *        The setting
     */
    public void setEmailToReplyable(boolean value) {
        log.warn("Use of this setter (emailToReplyable) is deprecated: use notify.email.to.replyable instead");
        emailsToReplyable = value;
    }

    /**
     * Configuration: set a reply-able status for email notifications in the From:
     *
     * @param value
     *        The setting
     */
    public void setEmailFromReplyable(boolean value) {
        log.warn("Use of this setter (emailFromReplyable) is deprecated: use notify.email.from.replyable instead");
        emailsFromReplyable = value;
    }

    public void init() {
        try {
            // prepare for transients
            transients = new ArrayList<>();

            relativeAccessPoint = REFERENCE_ROOT;

            log.info("initialization started");

            // construct storage and read
            storage = newStorage();
            storage.open();

            // start watching the events - only those generated on this server, not those from elsewhere
            eventTrackingService.addLocalObserver(this);

            // set these from real sakai config values
            emailsFromReplyable = serverConfigurationService.getBoolean("notify.email.from.replyable", false);
            emailsToReplyable = serverConfigurationService.getBoolean("notify.email.to.replyable", false);

            log.info("initialization complete");
        } catch (Exception e) {
            log.warn("initialization failure", e);
        }
    }

    public void destroy() {
        eventTrackingService.deleteObserver(this);

        // clean up storage
        storage.close();
        storage = null;

        // clean up transients
        transients.clear();
        transients = null;

        log.info("deinitialization complete");
    }

    @Override
    public NotificationEdit addNotification() {
        // check security (throws if not permitted)
        // unlock(SECURE_ADD_NOTIFICATION, notificationReference(id));

        // get a new unique id
        String id = idManager.createUuid();

        // reserve a notification with this id from the info store - if it's in use, this will return null
        NotificationEdit notification = storage.put(id);
        /*
         * if (notification == null) { throw new IdUsedException(id); }
         */

        ((BaseNotificationEdit) notification).setEvent(SECURE_ADD_NOTIFICATION);

        return notification;
    }

    @Override
    public NotificationEdit addTransientNotification() {
        // the id is not unique and not really used
        String id = "transient";

        // create an object, not through storage
        NotificationEdit notification = new BaseNotificationEdit(id);

        // remember it
        transients.add(notification);

        // no event, no other cluster server knows about it - it's transient and local
        return notification;
    }

    @Override
    public Notification getNotification(String id) throws NotificationNotDefinedException {
        Notification notification = storage.get(id);

        // if not found
        if (notification == null) throw new NotificationNotDefinedException(id);

        return notification;
    }

    @Override
    public String notificationReference(String id) {
        return getAccessPoint(true) + Entity.SEPARATOR + id;
    }

    @Override
    public NotificationEdit editNotification(String id) throws NotificationNotDefinedException, NotificationLockedException {
        // check security (throws if not permitted)
        // unlock(SECURE_UPDATE_NOTIFICATION, notificationReference(id));

        // check for existence
        if (!storage.check(id)) {
            throw new NotificationNotDefinedException(id);
        }

        // ignore the cache - get the notification with a lock from the info store
        NotificationEdit notification = storage.edit(id);
        if (notification == null) throw new NotificationLockedException(id);

        ((BaseNotificationEdit) notification).setEvent(SECURE_UPDATE_NOTIFICATION);

        return notification;
    }

    @Override
    public void commitEdit(NotificationEdit notification) {
        // check for closed edit
        if (!notification.isActiveEdit()) {
            log.warn("Attempting to commit a notification that is closed", new Exception("Stack trace"));
            return;
        }

        // complete the edit
        storage.commit(notification);

        // track it
        eventTrackingService.post(eventTrackingService
                .newEvent(((BaseNotificationEdit) notification).getEvent(), notification.getReference(), true));

        // close the edit object
        ((BaseNotificationEdit) notification).closeEdit();
    }

    @Override
    public void cancelEdit(NotificationEdit notification) {
        // check for closed edit
        if (!notification.isActiveEdit()) {
            log.warn("Attempting to cancel a notification that is closed", new Exception("Stack trace"));
            return;
        }

        // release the edit lock
        storage.cancel(notification);

        // close the edit object
        ((BaseNotificationEdit) notification).closeEdit();
    }

    @Override
    public void removeNotification(NotificationEdit notification) {
        // check for closed edit
        if (!notification.isActiveEdit()) {
            log.warn("Attempting to remove a notification that is closed", new Exception("Stack trace"));
            return;
        }

        // complete the edit
        storage.remove(notification);

        // track it
        eventTrackingService.post(eventTrackingService.newEvent(SECURE_REMOVE_NOTIFICATION, notification.getReference(), true));

        // close the edit object
        ((BaseNotificationEdit) notification).closeEdit();
    }

    public List<Notification> getNotifications(String function) {
        List<Notification> notifications = storage.getAll(function);
        
        // add transients
        transients.stream()
                .filter(n -> n.containsFunction(function))
                .forEach(notifications::add);
        return notifications;
    }

    @Override
    public Notification findNotification(String function, String filter) {
        return getNotifications(function).stream()
                .filter(notification -> notification.getResourceFilter().equals(filter))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Notification> findNotifications(String function, String filter) {
        return getNotifications(function).stream()
                .filter(notification -> notification.getResourceFilter().startsWith(filter))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean isNotificationToReplyable() {
        return this.emailsToReplyable;
    }

    @Override
    public boolean isNotificationFromReplyable() {
        return this.emailsFromReplyable;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof Event event)) return;

        // check the event function against the functions we have notifications watching for
        String function = event.getEvent();

        // for each notification watching for this event
        List<Notification> notifications = getNotifications(function);
        for (Notification notification : notifications) {
            // if the resource matches the notification's resource filter
            if (match(notification.getResourceFilter(), event.getResource())) {
                // cause the notification to run
                notification.notify(event);
            }
        }
    }

    @Override
    public Entity newResource(Entity container, String id, Object[] others) {
        return new BaseNotification(id);
    }

    @Override
    public Entity newResource(Entity container, Element element) {
        return new BaseNotification(element);
    }

    @Override
    public Entity newResource(Entity container, Entity other) {
        return new BaseNotification((Notification) other);
    }

    @Override
    public Edit newResourceEdit(Entity container, String id, Object[] others) {
        BaseNotificationEdit e = new BaseNotificationEdit(id);
        e.activate();
        return e;
    }

    @Override
    public Edit newResourceEdit(Entity container, Element element) {
        BaseNotificationEdit e = new BaseNotificationEdit(element);
        e.activate();
        return e;
    }

    @Override
    public Edit newResourceEdit(Entity container, Entity other) {
        BaseNotificationEdit e = new BaseNotificationEdit((Notification) other);
        e.activate();
        return e;
    }

    @Override
    public Object[] storageFields(Entity r) {
        return null;
    }

    @Override
    public Object refresh(Object key, Object oldValue, Event event) {
        // key is a reference, but our storage wants an id
        String id = notificationId((String) key);

        // get this from storage
        Notification notification = storage.get(id);

        log.debug("key [{}]--[{}]", key, id);

        return notification;
    }


    protected interface Storage {
        /**
         * Open and be ready to read / write.
         */
        void open();

        /**
         * Close.
         */
        void close();

        /**
         * Check if a notification by this id exists.
         *
         * @param id
         *        The notification id.
         * @return true if a notification by this id exists, false if not.
         */
        boolean check(String id);

        /**
         * Add a new notification with this id.
         *
         * @param id
         *        The notification id.
         * @return The locked notification with this id, or null if in use.
         */
        NotificationEdit put(String id);

        /**
         * Get the notification with this id, or null if not found.
         *
         * @param id
         *        The notification id.
         * @return The notification with this id, or null if not found.
         */
        Notification get(String id);

        /**
         * Get a List of all the notifications that are interested in this function.
         *
         * @param function
         *        The Event function
         * @return The List (Notification) of all the notifications that are interested in this function.
         */
        List<Notification> getAll(String function);

        /**
         * Get a List of all notifications.
         *
         * @return The List (Notification) of all notifications.
         */
        List<Notification> getAll();

        /**
         * Get a lock on the notification with this id, or null if a lock cannot be gotten.
         *
         * @param id
         *        The user id.
         * @return The locked Notification with this id, or null if this record cannot be locked.
         */
        NotificationEdit edit(String id);

        /**
         * Commit the changes and release the lock.
         *
         * @param notification
         *        The notification to commit.
         */
        void commit(NotificationEdit notification);

        /**
         * Cancel the changes and release the lock.
         *
         * @param notification
         *        The notification to commit.
         */
        void cancel(NotificationEdit notification);

        /**
         * Remove this notification.
         *
         * @param notification
         *        The notification to remove.
         */
        void remove(NotificationEdit notification);
    }

    public class BaseNotification implements Notification {
        protected List<String> functions = null; // The Event(s) function we are watching for
        protected String filter = null; // The resource reference filter
        protected String id = null; // The resource id
        protected ResourcePropertiesEdit properties = null; // The resource properties
        protected NotificationAction action = null; // The action helper class

        public BaseNotification(String id) {
            // generate a new id
            this.id = id;

            // setup for properties
            properties = new BaseResourcePropertiesEdit();

            // setup for functions
            functions = new ArrayList<>();
        }

        public BaseNotification(Notification other) {
            setAll(other);
        }

        public BaseNotification(Element el) {
            // setup for properties
            properties = new BaseResourcePropertiesEdit();

            // setup for functions
            functions = new ArrayList<>();

            id = el.getAttribute("id");

            // the first function
            String func = StringUtils.trimToNull(el.getAttribute("function"));
            if (func != null) {
                functions.add(func);
            }

            filter = StringUtils.trimToNull(el.getAttribute("filter"));

            // the children (properties, action helper)
            NodeList children = el.getChildNodes();
            final int length = children.getLength();
            for (int i = 0; i < length; i++) {
                Node child = children.item(i);
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;
                Element element = (Element) child;

                // look for properties
                switch (element.getTagName()) {
                    case "properties" -> properties = new BaseResourcePropertiesEdit(element);
                    case "action" -> {
                        String className = StringUtils.trimToNull(element.getAttribute("class"));
                        if (className != null) {
                            try {
                                Class<?> actionClass;
                                try {
                                    actionClass = Class.forName(className);
                                } catch (ClassNotFoundException cnfe) {
                                    Object obj = applicationContext.getBean(className);
                                    actionClass = obj.getClass();
                                }
                                action = (NotificationAction) actionClass.newInstance();
                                action.set(element);
                            } catch (Exception e) {
                                log.warn("creating action helper, {}", e.toString());
                            }
                        }
                    }
                    case "function" -> {
                        func = StringUtils.trimToNull(element.getAttribute("id"));
                        functions.add(func);
                    }
                }
            }
        }

        protected void setAll(Notification other) {
            BaseNotification bOther = (BaseNotification) other;
            id = bOther.id;
            filter = bOther.filter;

            properties = new BaseResourcePropertiesEdit();
            properties.addAll(bOther.properties);

            functions = new ArrayList<>(bOther.functions);

            if (bOther.action != null) {
                action = bOther.action.getClone();
            }
        }

        @Override
        public void notify(Event event) {
            if (action != null) {
                action.notify(this, event);
            }
        }

        @Override
        public String getFunction() {
            return functions.get(0);
        }

        @Override
        public String getResourceFilter() {
            return filter;
        }

        @Override
        public List<String> getFunctions() {
            return new ArrayList<>(functions);
        }

        @Override
        public boolean containsFunction(String function) {
            return functions.contains(function);
        }

        @Override
        public NotificationAction getAction() {
            return action;
        }

        @Override
        public String getUrl() {
            return notificationUrl(id);
        }

        @Override
        public String getReference() {
            return notificationReference(id);
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
        public String getId() {
            return id;
        }

        @Override
        public ResourceProperties getProperties() {
            return properties;
        }

        @Override
        public Element toXml(Document doc, Stack<Element> stack) {
            Element notification = doc.createElement("notification");
            if (stack.isEmpty()) {
                doc.appendChild(notification);
            } else {
                stack.peek().appendChild(notification);
            }

            stack.push(notification);

            notification.setAttribute("id", getId());

            // first function
            if (!functions.isEmpty()) {
                notification.setAttribute("function", functions.get(0));
            }

            if (filter != null) notification.setAttribute("filter", filter);

            // properties
            properties.toXml(doc, stack);

            // action
            if (action != null) {
                Element action = doc.createElement("action");
                notification.appendChild(action);
                action.setAttribute("class", this.action.getClass().getName());
                this.action.toXml(action);
            }

            // more functions
            if (functions.size() > 1) {
                for (int i = 1; i < functions.size(); i++) {
                    String func = functions.get(i);
                    Element funcEl = doc.createElement("function");
                    notification.appendChild(funcEl);
                    funcEl.setAttribute("id", func);
                }
            }
            stack.pop();

            return notification;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof BaseNotification other && this.getId().equals(other.getId()));
        }
    }


    public class BaseNotificationEdit extends BaseNotification implements NotificationEdit, SessionBindingListener {
        protected String m_event = null;
        protected boolean m_active = false;

        public BaseNotificationEdit(String id) {
            super(id);
        }

        /**
         * Construct from an existing definition, in XML.
         *
         * @param el
         *        The message in XML in a DOM element.
         */
        public BaseNotificationEdit(Element el) {
            super(el);
        }

        /**
         * Construct from another Notification.
         *
         * @param other
         *        The other notification to copy values from.
         */
        public BaseNotificationEdit(Notification other) {
            super(other);
        }

        protected void finalize() {
            // catch the case where an edit was made but never resolved
            if (m_active) {
                cancelEdit(this);
            }
        }

        /**
         * Set the Event function, clearing any that have already been set.
         *
         * @param function
         *        The Event function to watch for.
         */
        public void setFunction(String function) {
            functions.clear();
            functions.add(function);
        }

        /**
         * Add another Event function.
         *
         * @param function
         *        Another Event function to watch for.
         */
        public void addFunction(String function) {
            functions.add(function);
        }

        /**
         * Set the resource reference filter.
         *
         * @param filter
         *        The resource reference filter.
         */
        public void setResourceFilter(String filter) {
            this.filter = filter;
        }

        /**
         * Set the action helper that handles the notify() action.
         *
         * @param action
         *        The action helper that handles the notify() action.
         */
        public void setAction(NotificationAction action) {
            this.action = action;
        }

        /**
         * Take all values from this object.
         *
         * @param other
         *        The notification object to take values from.
         */
        protected void set(Notification other) {
            setAll(other);
        }

        /**
         * Access the event code for this edit.
         *
         * @return The event code for this edit.
         */
        protected String getEvent() {
            return m_event;
        }

        /**
         * Set the event code for this edit.
         *
         * @param event
         *        The event code for this edit.
         */
        protected void setEvent(String event) {
            m_event = event;
        }

        /**
         * Access the resource's properties for modification
         *
         * @return The resource's properties.
         */
        public ResourcePropertiesEdit getPropertiesEdit() {
            return properties;
        }

        /**
         * Enable editing.
         */
        protected void activate() {
            m_active = true;
        }

        /**
         * Check to see if the edit is still active or has already been closed.
         *
         * @return true if the edit is active, false if it's been closed.
         */
        public boolean isActiveEdit() {
            return m_active;
        }

        /**
         * Close the edit object - it cannot be used after this.
         */
        protected void closeEdit() {
            m_active = false;
        }

        /**
         * Bind value from event to session
         * @param event
         *        the event that identifies the session
         */
        public void valueBound(SessionBindingEvent event) {
        }

        /**
         * Unbind value from a session
         * @param event
         *        the event that identifies the session
         */
        public void valueUnbound(SessionBindingEvent event) {
            log.debug("unbind value from session, event: {}", event);

            // catch the case where an edit was made but never resolved
            if (m_active) {
                cancelEdit(this);
            }
        }
    }
}
