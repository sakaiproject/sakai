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

package org.sakaiproject.alias.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.BaseDbFlatStorage;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.SingleStorageUser;
import org.sakaiproject.util.StorageUtils;
import org.sakaiproject.util.StringUtil;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * DbAliasService is an extension of the BaseAliasService with a database storage.
 * Fields are fully relational.
 * Full properties are not yet supported - core ones are.
 * Code to find and convert records from before, from the XML-based CHEF_ALIAS table is included.
 * </>
 */
@Slf4j
public class DbAliasService extends BaseAliasService {

    protected String tableName = "SAKAI_ALIAS"; // Table name for aliases
    protected String propTableName = "SAKAI_ALIAS_PROPERTY"; // Table name for properties
    protected String idFieldName = "ALIAS_ID"; // ID field
    protected String[] fieldNames = {"ALIAS_ID", "TARGET", "CREATEDBY", "MODIFIEDBY", "CREATEDON", "MODIFIEDON"}; // All fields
    protected boolean useExternalLocks = true; // If true, we do our locks in the remote database, otherwise we do them here
    protected boolean convertOld = false; // Set if we are to run the from-old conversion
    protected boolean checkOld = false; // check the old table, too
    protected boolean autoDdl = false; // to run the ddl on init or not

    @Setter protected SqlService sqlService;

    /**
     * Configuration: run the from-old conversion.
     *
     * @param value
     *        The conversion desired value.
     */
    public void setConvertOld(String value) {
        convertOld = Boolean.parseBoolean(value);
    }

    /**
     * Configuration: set the external locks value.
     *
     * @param value
     *        The external locks value.
     */
    public void setExternalLocks(String value) {
        useExternalLocks = Boolean.parseBoolean(value);
    }

    /**
     * Configuration: set the locks-in-db
     *
     * @param value
     *        The locks-in-db value.
     */
    public void setCheckOld(String value) {
        checkOld = Boolean.parseBoolean(value);
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

    public void init() {
        try {
            // if we are auto-creating our schema, check and create
            if (autoDdl) {
                sqlService.ddl(this.getClass().getClassLoader(), "sakai_alias");
            }

            super.init();

            log.info("table: {} external locks: {} checkOld: {}", tableName, useExternalLocks, checkOld);

            // do a count which might find no old records so we can ignore old!
            if (checkOld) {
                storage.count();
            }
        } catch (Exception e) {
            log.warn("Initialization of DbAliasService failed", e);
        }
    }

    /**
     * Construct a Storage object.
     *
     * @return The new storage object.
     */
    @Override
    protected Storage newStorage() {
        return new DbStorage(this);
    }

    protected class DbStorage extends BaseDbFlatStorage implements Storage, SqlReader<AliasEdit> {
        /** A prior version's storage model. */
        protected Storage m_oldStorage = null;

        /**
         * Construct.
         *
         * @param user
         *        The StorageUser class to call back for the creation of Resource and Edit objects.
         */
        public DbStorage(SingleStorageUser user) {
            super(tableName, idFieldName, fieldNames, propTableName, useExternalLocks, null, sqlService);
            m_reader = this;
            setCaseInsensitivity(true);

            // setup for old-new straddling
            if (checkOld) {
                m_oldStorage = new DbStorageOld(user);
            }
        }

        @Override
        public boolean check(String id) {
            boolean rv = super.checkResource(id);

            // if not, check old
            if (checkOld && (!rv)) {
                rv = m_oldStorage.check(id);
            }

            return rv;
        }

        @Override
        public AliasEdit get(String id) {
            AliasEdit rv = (AliasEdit) super.getResource(id);

            // if not, check old
            if (checkOld && (rv == null)) {
                rv = m_oldStorage.get(id);
            }
            return rv;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> getAll() {
            // if we have to be concerned with old stuff, we cannot let the db do the range selection
            if (checkOld) {
                Set<Alias> merge = new HashSet<>(super.getAllResources());
                merge.addAll(m_oldStorage.getAll());
                return new ArrayList<>(merge);
            }

            // let the db do range selection
            return (List<Alias>) super.getAllResources();
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> getAll(int first, int last) {
            // if we have to be concerned with old stuff, we cannot let the db do the range selection
            if (checkOld) {
                List<Alias> all = super.getAllResources();

                // add in any additional defined in old
                Set<Alias> merge = new HashSet<>(all);

                // add those in the old not already (id based equals) in all
                List<Alias> more = m_oldStorage.getAll();
                merge.addAll(more);

                all.clear();
                all.addAll(merge);

                Collections.sort(all);

                // subset by position
                if (first < 1) first = 1;
                if (last >= all.size()) last = all.size();

                all = all.subList(first - 1, last);
                return all;
            }

            // let the db do range selection
            return (List<Alias>) super.getAllResources(first, last);
        }

        @Override
        public int count() {
            // if we have to be concerned with old stuff, we cannot let the db do all the counting
            if (checkOld) {
                int count = super.countAllResources();
                count += m_oldStorage.count();

                return count;
            }
            return super.countAllResources();
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> getAll(String target) {
            Object[] fields = new Object[1];
            fields[0] = target;

            // if we have to be concerned with old stuff, we cannot let the db do the range selection
            if (checkOld) {
                Set<Alias> merge = new HashSet<>(super.getSelectedResources("TARGET = ?", fields));
                merge.addAll(m_oldStorage.getAll(target));
                return new ArrayList<>(merge);
            }

            return (List<Alias>) super.getSelectedResources("TARGET = ?", fields);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> getAll(String target, int first, int last) {
            Object[] fields = new Object[1];
            fields[0] = target;

            // if we have to be concerned with old stuff, we cannot let the db do the range selection
            if (checkOld) {
                Set<Alias> mergedSet = new HashSet<>(super.getSelectedResources("TARGET = ?", fields));
                mergedSet.addAll(m_oldStorage.getAll(target));
                List<Alias> all = new ArrayList<>(mergedSet);

                Collections.sort(all);

                first = Math.max(1, first);
                last = Math.min(last, all.size());

                return all.subList(first - 1, last);
            }

            return (List<Alias>) super.getSelectedResources("TARGET = ?", fields, first, last);
        }

        @Override
        public AliasEdit put(String id) {
            // check for already exists (new or old)
            if (check(id)) return null;

            BaseAliasEdit rv = (BaseAliasEdit) super.putResource(id, fields(id, null, false));
            if (rv != null) rv.activate();
            return rv;
        }

        @Override
        public AliasEdit edit(String id) {
            BaseAliasEdit rv = (BaseAliasEdit) super.editResource(id);

            // if not found, try from the old (convert to the new)
            if (checkOld && (rv == null)) {
                // this locks the old table/record
                rv = (BaseAliasEdit) m_oldStorage.edit(id);
                if (rv != null) {
                    // create the record in new, also locking it into an edit
                    rv = (BaseAliasEdit) super.putResource(id, fields(id, rv, false));

                    // delete the old record
                    m_oldStorage.remove(rv);
                }
            }

            if (rv != null) rv.activate();
            return rv;
        }

        @Override
        public void commit(AliasEdit edit) {
            super.commitResource(edit, fields(edit.getId(), edit, true), edit.getProperties());
        }

        @Override
        public void cancel(AliasEdit edit) {
            super.cancelResource(edit);
        }

        @Override
        public void remove(AliasEdit edit) {
            super.removeResource(edit);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> search(String criteria, int first, int last) {
            // if we have to be concerned with old stuff, we cannot let the db do the search
            if (checkOld) {

                return getAll().stream()
                        .filter(a -> StringUtil.containsIgnoreCase(a.getId(), criteria)
                                || StringUtil.containsIgnoreCase(a.getTarget(), criteria))
                        .sorted()
                        .skip(Math.max(0, first - 1))
                        .limit(Math.max(0, last - Math.max(0, first - 1)))
                        .collect(Collectors.toList());
            }

            Object[] fields = new Object[2];
            fields[0] = "%" + StorageUtils.escapeSqlLike(criteria.toUpperCase()) + "%";
            fields[1] = fields[0];

            return (List<Alias>) super.getSelectedResources("UPPER(ALIAS_ID) LIKE ? OR UPPER(TARGET) LIKE ?", fields, first, last);
        }

        @Override
        public int countSearch(String criteria) {
            // if we have to be concerned with old stuff, we cannot let the db do the search and count
            if (checkOld) {
                List<Alias> all = getAll();
                List<Alias> old = new ArrayList<>();

                for (Alias a : all) {
                    if (StringUtil.containsIgnoreCase(a.getId(), criteria)
                            || StringUtil.containsIgnoreCase(a.getTarget(), criteria)) {
                        old.add(a);
                    }
                }
                return old.size();
            }

            Object[] fields = new Object[2];
            fields[0] = "%" + StorageUtils.escapeSqlLike(criteria.toUpperCase()) + "%";
            fields[1] = fields[0];

            return super.countSelectedResources("UPPER(ALIAS_ID) LIKE ? OR UPPER(TARGET) LIKE ?", fields);
        }

        @Override
        public void readProperties(AliasEdit edit, ResourcePropertiesEdit props) {
            super.readProperties(edit, props);
        }

        /**
         * Get the fields for the database from the edit for this id, and the id again at the end if needed
         *
         * @param id
         *        The resource id
         * @param edit
         *        The edit (maybe null in a new)
         * @param idAgain
         *        If true, include the id field again at the end, else don't.
         * @return The fields for the database.
         */
        protected Object[] fields(String id, AliasEdit edit, boolean idAgain) {
            Object[] rv = new Object[idAgain ? 7 : 6];
            rv[0] = caseId(id);
            if (idAgain) {
                rv[6] = rv[0];
            }

            if (edit == null) {
                String current = sessionManager.getCurrentSessionUserId();
                if (current == null) current = "";

                Time now = timeService.newTime();
                rv[1] = "";
                rv[2] = current;
                rv[3] = current;
                rv[4] = now;
                rv[5] = now;
            } else {
                rv[1] = edit.getTarget();
                rv[2] = StringUtils.trimToEmpty(((BaseAliasEdit) edit).createdUserId);
                rv[3] = StringUtils.trimToEmpty(((BaseAliasEdit) edit).lastModifiedUserId);
                rv[4] = edit.getCreatedTime();
                rv[5] = edit.getModifiedTime();
            }

            return rv;
        }

        @Override
        public AliasEdit readSqlResultRecord(ResultSet result) {
            try {
                String id = result.getString(1);
                String target = result.getString(2);
                String createdBy = result.getString(3);
                String modifiedBy = result.getString(4);
                Time createdOn = timeService.newTime(result.getTimestamp(5).getTime());
                Time modifiedOn = timeService.newTime(result.getTimestamp(6).getTime());

                // create the Resource from these fields
                return new BaseAliasEdit(id, target, createdBy, createdOn, modifiedBy, modifiedOn);
            } catch (SQLException ignore) {
                return null;
            }
        }

    }

    /**
     * This is how to access the old chef_alias table (CTools through 2.0.7)
     */
    protected class DbStorageOld extends BaseDbSingleStorage implements Storage {
        /**
         * Construct.
         *
         * @param user
         *        The StorageUser class to call back for the creation of Resource and Edit objects.
         */
        public DbStorageOld(SingleStorageUser user) {
            super("CHEF_ALIAS", "ALIAS_ID", null, false, "alias", user, sqlService);
            setCaseInsensitivity(true);

        }

        @Override
        public boolean check(String id) {
            return super.checkResource(id);
        }

        @Override
        public AliasEdit get(String id) {
            return (AliasEdit) super.getResource(id);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> getAll(int first, int last) {
            return super.getAllResources(first, last);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> getAll() {
            return super.getAllResources();
        }

        @Override
        public int count() {
            int rv = super.countAllResources();

            // if we find no more records in the old table, we can start ignoring it...
            // Note: this means once they go away, they cannot come back (old versions cannot run in the cluster
            // and write to the old cluster table). -ggolden
            if (rv == 0) {
                checkOld = false;
                log.info(" ** starting to ignore old");
            }
            return rv;
        }

        @Override
        public AliasEdit put(String id) {
            return (AliasEdit) super.putResource(id, null);
        }

        @Override
        public AliasEdit edit(String id) {
            return (AliasEdit) super.editResource(id);
        }

        @Override
        public void commit(AliasEdit edit) {
            super.commitResource(edit);
        }

        @Override
        public void cancel(AliasEdit edit) {
            super.cancelResource(edit);
        }

        @Override
        public void remove(AliasEdit edit) {
            super.removeResource(edit);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> getAll(String target) {
            List<Alias> all = super.getAllResources();

            // pick out from all those that are for this target
            return all.stream()
                    .filter(a -> a.getTarget().equals(target))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> getAll(String target, int first, int last) {
            List<Alias> all = super.getAllResources();

            // pick out from all those that are for this target
            List<Alias> found = all.stream()
                    .filter(a -> a.getTarget().equals(target))
                    .sorted()
                    .collect(Collectors.toCollection(ArrayList::new));
            // subset by position
            if (first < 1) first = 1;
            if (last >= found.size()) last = found.size();

            found = found.subList(first - 1, last);

            return found;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Alias> search(String criteria, int first, int last) {
            List<Alias> all = super.getAllResources();
            List<Alias> searchResults = all.stream()
                    .filter(a -> StringUtil.containsIgnoreCase(a.getId(), criteria) || StringUtil.containsIgnoreCase(a.getTarget(), criteria))
                    .sorted()
                    .collect(Collectors.toCollection(ArrayList::new));

            // subset by position
            if (first < 1) first = 1;
            if (last >= searchResults.size()) last = searchResults.size();

            searchResults = searchResults.subList(first - 1, last);

            return searchResults;
        }

        @Override
        @SuppressWarnings("unchecked")
        public int countSearch(String criteria) {
            List<Alias> all = super.getAllResources();
            Number count = all.stream()
                    .filter(a -> StringUtil.containsIgnoreCase(a.getId(), criteria) || StringUtil.containsIgnoreCase(a.getTarget(), criteria))
                    .count();
            return count.intValue();
        }

        @Override
        public void readProperties(AliasEdit edit, ResourcePropertiesEdit props) {
            log.warn("readProperties: should not be called.");
        }
    }
}
