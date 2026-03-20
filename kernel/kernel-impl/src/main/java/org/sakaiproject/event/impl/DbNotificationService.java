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
import java.util.stream.Collectors;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.SingleStorageUser;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbNotificationService extends BaseNotificationService {

    @Setter private String tableName = "SAKAI_NOTIFICATION";
    @Setter private SqlService sqlService;

    private boolean locksInDb = true; // If true, we do our locks in the remote database, otherwise we do them here
    private boolean autoDdl = false; // Configuration: to run the ddl on init or not

    public void init() {
        try {
            // if we are auto-creating our schema, check and create
            if (autoDdl) {
                sqlService.ddl(this.getClass().getClassLoader(), "sakai_notification");
            }

            super.init();

            log.info("initialize table: {} locks-in-db: {}", tableName, locksInDb);
        } catch (Exception e) {
            log.warn("initialization failed for table: {}", tableName, e);
        }
    }

    /**
     * Construct a Storage object.
     *
     * @return The new storage object.
     */
    protected Storage newStorage() {
        return new DbStorage(this);
    }

    /**
     * Configuration: set the locks-in-db
     *
     * @param value
     *        The locks-in-db value.
     */
    public void setLocksInDb(String value) {
        locksInDb = Boolean.parseBoolean(value);
    }

    /**
     * Configuration: to run the ddl on init or not.
     *
     * @param value
     *        the auto ddl value.
     */
    public void setAutoDdl(String value) {
        autoDdl = Boolean.parseBoolean(value);
    }

    protected class DbStorage extends BaseDbSingleStorage implements Storage {

        public DbStorage(SingleStorageUser user) {
            super(tableName, "NOTIFICATION_ID", null, locksInDb, "notification", user, sqlService);
        }

        public boolean check(String id) {
            return super.checkResource(id);
        }

        public Notification get(String id) {
            return (Notification) super.getResource(id);
        }

        @SuppressWarnings("unchecked")
        public List<Notification> getAll() {
            return (List<Notification>) super.getAllResources();
        }

        /**
         * Get a Set of all the notifications that are interested in this Event function. Note: instead of this looking, we could have an additional "key" in storage of the event... -ggolen
         *
         * @param function
         *        The Event function
         * @return The Set (Notification) of all the notifications that are interested in this Event function.
         */
        @SuppressWarnings("unchecked")
        public List<Notification> getAll(String function) {
            if (function == null) return new ArrayList<>();

            List<Notification> all = super.getAllResources();
            return all.stream()
                    .filter(n -> n.containsFunction(function))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        public NotificationEdit put(String id) {
            return (NotificationEdit) super.putResource(id, null);
        }

        public NotificationEdit edit(String id) {
            return (NotificationEdit) super.editResource(id);
        }

        public void commit(NotificationEdit edit) {
            super.commitResource(edit);
        }

        public void cancel(NotificationEdit edit) {
            super.cancelResource(edit);
        }

        public void remove(NotificationEdit edit) {
            super.removeResource(edit);
        }
    }
}
