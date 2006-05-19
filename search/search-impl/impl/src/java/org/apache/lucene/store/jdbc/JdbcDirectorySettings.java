/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.store.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.store.jdbc.handler.ActualDeleteFileEntryHandler;
import org.apache.lucene.store.jdbc.handler.NoOpFileEntryHandler;
import org.apache.lucene.store.jdbc.index.FetchOnOpenJdbcIndexInput;
import org.apache.lucene.store.jdbc.index.RAMJdbcIndexOutput;
import org.apache.lucene.store.jdbc.lock.PhantomReadLock;

/**
 * General directory level settings.
 * <p />
 * The settings also holds {@link JdbcFileEntrySettings}, that can be registered with
 * the directory settings. Note, that when registering them, they are registered under
 * both the complete name and the 3 charecters name suffix.
 * <p />
 * When creating the settings, it already holds sensible settings, they are:
 * The default {@link JdbcFileEntrySettings} uses the file entry settings defaults.
 * The "deletable", ""deleteable.new", and "deletable.new" uses the {@link NoOpFileEntryHandler}.
 * The "segments" and "segments.new" uses the {@link ActualDeleteFileEntryHandler}, {@link FetchOnOpenJdbcIndexInput},
 * and {@link RAMJdbcIndexOutput}.
 * The file suffix "fnm" uses the {@link FetchOnOpenJdbcIndexInput}, and {@link RAMJdbcIndexOutput}.
 * The file suffix "del" and "tmp" uses the {@link ActualDeleteFileEntryHandler}.
 *
 * @author kimchy
 */
public class JdbcDirectorySettings {

    /**
     * The default file entry settings name that are registered under.
     */
    public static String DEFAULT_FILE_ENTRY = "__default__";

    /**
     * A simple constant having the millisecond value of an hour.
     */
    public static final long HOUR = 60 * 60 * 1000;

    private int nameColumnLength = 50;

    private int valueColumnLengthInK = 500 * 1000;

    private String nameColumnName = "name_";

    private String valueColumnName = "value_";

    private String sizeColumnName = "size_";

    private String lastModifiedColumnName = "lf_";

    private String deletedColumnName = "deleted_";

    private HashMap fileEntrySettings = new HashMap();

    private boolean useCommitLocks = false;

    private long deleteMarkDeletedDelta = HOUR;

    private int queryTimeout = 10;

    private Class lockClass = PhantomReadLock.class;

    /**
     * Creates a new instance of the Jdbc directory settings with it's default values initialized.
     */
    public JdbcDirectorySettings() {
        JdbcFileEntrySettings defaultSettings = new JdbcFileEntrySettings();
        registerFileEntrySettings(DEFAULT_FILE_ENTRY, defaultSettings);

        JdbcFileEntrySettings deletableSettings = new JdbcFileEntrySettings();
        deletableSettings.setClassSetting(JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE, NoOpFileEntryHandler.class);
        registerFileEntrySettings("deletable", deletableSettings);
        registerFileEntrySettings("deleteable.new", deletableSettings);
        // in case lucene fix the spelling mistake
        registerFileEntrySettings("deletable.new", deletableSettings);

        JdbcFileEntrySettings segmentsSettings = new JdbcFileEntrySettings();
        segmentsSettings.setClassSetting(JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE, ActualDeleteFileEntryHandler.class);
        segmentsSettings.setClassSetting(JdbcFileEntrySettings.INDEX_INPUT_TYPE_SETTING, FetchOnOpenJdbcIndexInput.class);
        segmentsSettings.setClassSetting(JdbcFileEntrySettings.INDEX_OUTPUT_TYPE_SETTING, RAMJdbcIndexOutput.class);
        registerFileEntrySettings("segments", segmentsSettings);
        registerFileEntrySettings("segments.new", segmentsSettings);

        JdbcFileEntrySettings dotDelSettings = new JdbcFileEntrySettings();
        dotDelSettings.setClassSetting(JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE, ActualDeleteFileEntryHandler.class);
        registerFileEntrySettings("del", dotDelSettings);

        JdbcFileEntrySettings tmpSettings = new JdbcFileEntrySettings();
        tmpSettings.setClassSetting(JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE, ActualDeleteFileEntryHandler.class);
        registerFileEntrySettings("tmp", dotDelSettings);

        JdbcFileEntrySettings fnmSettings = new JdbcFileEntrySettings();
        fnmSettings.setClassSetting(JdbcFileEntrySettings.INDEX_INPUT_TYPE_SETTING, FetchOnOpenJdbcIndexInput.class);
        fnmSettings.setClassSetting(JdbcFileEntrySettings.INDEX_OUTPUT_TYPE_SETTING, RAMJdbcIndexOutput.class);
        registerFileEntrySettings("fnm", fnmSettings);
    }

    /**
     * Returns the name column length.
     */
    public int getNameColumnLength() {
        return nameColumnLength;
    }

    /**
     * Sets the name column length.
     */
    public void setNameColumnLength(int nameColumnLength) {
        this.nameColumnLength = nameColumnLength;
    }

    /**
     * Returns the value column length (In K).
     */
    public int getValueColumnLengthInK() {
        return valueColumnLengthInK;
    }

    /**
     * Sets the value coumn length (In K).
     */
    public void setValueColumnLengthInK(int valueColumnLengthInK) {
        this.valueColumnLengthInK = valueColumnLengthInK;
    }

    /**
     * Returns the name column name (defaults to name_).
     */
    public String getNameColumnName() {
        return nameColumnName;
    }

    /**
     * Sets the name column name.
     */
    public void setNameColumnName(String nameColumnName) {
        this.nameColumnName = nameColumnName;
    }

    /**
     * Returns the value column name (defaults to value_).
     */
    public String getValueColumnName() {
        return valueColumnName;
    }

    /**
     * Sets the value column name.
     */
    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    /**
     * Returns the size column name (default to size_).
     */
    public String getSizeColumnName() {
        return sizeColumnName;
    }

    /**
     * Sets the size column name.
     */
    public void setSizeColumnName(String sizeColumnName) {
        this.sizeColumnName = sizeColumnName;
    }

    /**
     * Returns the last modified column name (defaults to lf_).
     */
    public String getLastModifiedColumnName() {
        return lastModifiedColumnName;
    }

    /**
     * Sets the last modified column name.
     */
    public void setLastModifiedColumnName(String lastModifiedColumnName) {
        this.lastModifiedColumnName = lastModifiedColumnName;
    }

    /**
     * Returns the deleted column name (defaults to deleted_).
     */
    public String getDeletedColumnName() {
        return deletedColumnName;
    }

    /**
     * Sets the deleted column name.
     */
    public void setDeletedColumnName(String deletedColumnName) {
        this.deletedColumnName = deletedColumnName;
    }

    /**
     * Registers a {@link JdbcFileEntrySettings} against the given name.
     * The name can be the full name of the file, or it's 3 charecters suffix.
     */
    public void registerFileEntrySettings(String name, JdbcFileEntrySettings fileEntrySettings) {
        this.fileEntrySettings.put(name, fileEntrySettings);
    }

    /**
     * Returns the file entries map.
     */
    Map getFileEntrySettings() {
        return this.fileEntrySettings;
    }

    /**
     * Returns the file entries according to the name. If a direct match is found, it's registered
     * {@link JdbcFileEntrySettings} is returned. If one is registered
     * against the last 3 charecters, then it is returned. If none is found, the default file entry
     * handler is returned.
     */
    public JdbcFileEntrySettings getFileEntrySettings(String name) {
        JdbcFileEntrySettings settings = getFileEntrySettingsWithoutDefault(name);
        if (settings != null) {
            return settings;
        }
        return getDefaultFileEntrySettings();
    }

    /**
     * Same as {@link #getFileEntrySettings(String)}, only returns <code>null</code> if no match is found
     * (instead of the default file entry handler settings).
     */
    public JdbcFileEntrySettings getFileEntrySettingsWithoutDefault(String name) {
        JdbcFileEntrySettings settings = (JdbcFileEntrySettings) fileEntrySettings.get(name.substring(name.length() - 3));
        if (settings != null) {
            return settings;
        }
        return (JdbcFileEntrySettings) fileEntrySettings.get(name);
    }

    /**
     * Returns the default file entry handler settings.
     */
    public JdbcFileEntrySettings getDefaultFileEntrySettings() {
        return (JdbcFileEntrySettings) fileEntrySettings.get(DEFAULT_FILE_ENTRY);
    }

    /**
     * Should the system use commit locks. Defaults to <code>false</code>. Only set it to <code>true</code>, if the
     * jdbc driver uses autoCommit (where it is allowed by the database, and the performance
     * implications are taken into account).
     */
    public boolean isUseCommitLocks() {
        return useCommitLocks;
    }

    /**
     * Sets the directory to use commit locks.  Defaults to <code>false</code>. Only set it to <code>true</code>, if the
     * jdbc driver uses autoCommit (where it is allowed by the database, and the performance
     * implications are taken into account).
     */
    public void setUseCommitLocks(boolean useCommitLocks) {
        this.useCommitLocks = useCommitLocks;
    }

    /**
     * Returns the delta (in millis) for the delete mark deleted. File entries marked as being deleted will
     * be deleted from the system (using {@link org.apache.lucene.store.jdbc.JdbcDirectory#deleteMarkDeleted()}
     * if: current_time - deletelMarkDeletedDelta &lt; Time File Entry Marked as Deleted.
     */
    public long getDeleteMarkDeletedDelta() {
        return deleteMarkDeletedDelta;
    }

    /**
     * Sets the delta (in millis) for the delete mark deleted. File entries marked as being deleted will
     * be deleted from the system (using {@link org.apache.lucene.store.jdbc.JdbcDirectory#deleteMarkDeleted()}
     * if: current_time - deletelMarkDeletedDelta &lt; Time File Entry Marked as Deleted.
     */
    public void setDeleteMarkDeletedDelta(long deleteMarkDeletedDelta) {
        this.deleteMarkDeletedDelta = deleteMarkDeletedDelta;
    }

    /**
     * Query timeout applies to Jdbc queries.
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * Query timeout applies to Jdbc queries.
     */
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    /**
     * Returns the lock class that will be used for locking. Defaults to {@link PhantomReadLock}.
     */
    public Class getLockClass() {
        return lockClass;
    }

    /**
     * Sets the lock class that will be used for locking. Defaults to {@link PhantomReadLock}.
     */
    public void setLockClass(Class lockClass) {
        this.lockClass = lockClass;
    }
}
