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

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.SingleStorageUser;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * DbPreferencesService is an extension of the BasePreferencesService with database storage.
 * </p>
 */
@Slf4j
public class DbPreferencesService extends BasePreferencesService {
    /** Table name for realms. */
    @Setter protected String tableName = "SAKAI_PREFERENCES";

    /** If true, we do our locks in the remote database, otherwise we do them here. */
    protected boolean m_locksInDb = true;

    protected boolean m_autoDdl = false;

    @Setter protected SqlService sqlService;

    public void setLocksInDb(String value) {
        m_locksInDb = Boolean.parseBoolean(value);
    }

    public void setAutoDdl(String value) {
        m_autoDdl = Boolean.parseBoolean(value);
    }

    public void init() {
        try {
            // if we are auto-creating our schema, check and create
            if (m_autoDdl) {
                sqlService.ddl(this.getClass().getClassLoader(), "sakai_preferences");
            }

            super.init();

            log.info("table: {} locks-in-db: {}", tableName, m_locksInDb);
        } catch (Exception t) {
            log.warn("init(): ", t);
        }
    }

    protected Storage newStorage() {
        return new DbStorage(this);
    }

    protected class DbStorage extends BaseDbSingleStorage implements Storage {
        public DbStorage(SingleStorageUser user) {
            super(tableName, "PREFERENCES_ID", null, m_locksInDb, "preferences", user, sqlService);
        }

        public boolean check(String id) {
            return super.checkResource(id);
        }

        public Preferences get(String id) {
            return (Preferences) super.getResource(id);
        }

        public PreferencesEdit put(String id) {
            return (PreferencesEdit) super.putResource(id, null);
        }

        public PreferencesEdit edit(String id) {
            return (PreferencesEdit) super.editResource(id);
        }

        public void commit(PreferencesEdit edit) {
            super.commitResource(edit);
        }

        public void cancel(PreferencesEdit edit) {
            super.cancelResource(edit);
        }

        public void remove(PreferencesEdit edit) {
            super.removeResource(edit);
        }
    }
}
