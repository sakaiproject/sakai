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

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.DirectoryTemplate;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.MultiDeleteDirectory;
import org.apache.lucene.store.jdbc.dialect.Dialect;
import org.apache.lucene.store.jdbc.dialect.DialectResolver;
import org.apache.lucene.store.jdbc.handler.FileEntryHandler;
import org.apache.lucene.store.jdbc.lock.JdbcLock;
import org.apache.lucene.store.jdbc.lock.NoOpLock;
import org.apache.lucene.store.jdbc.support.JdbcTable;
import org.apache.lucene.store.jdbc.support.JdbcTemplate;

/**
 * A Jdbc based implementation of a Lucene <code>Directory</code> allowing the storage of a Lucene index
 * within a database. Uses a jdbc <code>DataSource</code>, {@link Dialect} specific for the database used,
 * and an optional {@link JdbcDirectorySettings} and {@link JdbcTable} for configuration.
 * <p/>
 * The directory works against a single table, where the binary data is stored in <code>Blob</code>. Each "file"
 * has an entry in the database, and different {@link FileEntryHandler} can be defines for different files (or
 * files groups).
 * <p/>
 * Most of the files will not be deleted from the database when the directory delete method is called, but will
 * only be marked to be deleted (see {@link org.apache.lucene.store.jdbc.handler.MarkDeleteFileEntryHandler}. It is
 * done since other readers or searchers might be working with the database, and still use the files. The ability to
 * purge mark deleted files based on a "delta" is acheived using {@link #deleteMarkDeleted()} and
 * {@link #deleteMarkDeleted(long)}. Note, the purging process is not called by the directory code, so it will have
 * to be managed by the application using the jdbc directory.
 * <p/>
 * For transaction management, all the operations performed against the database do not call <code>commit</code> or
 * <code>rollback</code>. They simply open a connection (using
 * {@link org.apache.lucene.store.jdbc.datasource.DataSourceUtils#getConnection(javax.sql.DataSource)} ), and close it
 * using {@link org.apache.lucene.store.jdbc.datasource.DataSourceUtils#releaseConnection(java.sql.Connection)}). This
 * results in the fact that transcation management is simple and wraps the directory operations, allowing it to span
 * as many operations as needed.
 * <p/>
 * For none managed applications (i.e. applications that do not use JTA or Spring transaction manager), the jdbc directory
 * implementation comes with {@link org.apache.lucene.store.jdbc.datasource.TransactionAwareDataSourceProxy} which wraps
 * a <code>DataSource</code> (should be a pooled one, like Jakartat DBCP). Using it with the
 * {@link org.apache.lucene.store.jdbc.datasource.DataSourceUtils}, or the provided {@link DirectoryTemplate} should make
 * integrating or using jdbc directory simple.
 * <p/>
 * Also, for none managed applications, there is an option working with autoCommit=true mode. The system will work much
 * slower, and it is only supported on a portion of the databases, but any existing code that uses Lucene with any
 * other <code>Directory</code> implemenation should work as is. Note, if working in this mode, to set the system to use
 * commit lock ({@link JdbcDirectorySettings#setUseCommitLocks(boolean)} .
 * <p/>
 * If working within managed environments, an external transaction management should be performed (using JTA for example).
 * Simple solutions can be using CMT or Spring Framework abstraction of transaction managers. Currently, the jdbc directory
 * implementation does not implement a transaction management abstraction, since there is a very good solution out there
 * already (Spring and JTA). Note, when using Spring and the <code>DataSourceTransactionManager</code>, to provide the jdbc directory
 * with a Spring's <code>TransactionAwareDataSourceProxy</code>.
 *
 * @author kimchy
 */
public class JdbcDirectory extends Directory implements MultiDeleteDirectory {

    private Dialect dialect;

    private DataSource dataSource;

    private JdbcTable table;

    private JdbcDirectorySettings settings;

    private HashMap fileEntryHandlers = new HashMap();

    private JdbcTemplate jdbcTemplate;


    /**
     * Creates a new jdbc directory.  Creates new {@link JdbcDirectorySettings} using it's default values.
     * Uses {@link DialectResolver} to try and automatically reolve the {@link Dialect}.
     *
     * @param dataSource The data source to use
     * @param tableName  The table name
     * @throws JdbcStoreException
     */
    public JdbcDirectory(DataSource dataSource, String tableName) throws JdbcStoreException {
        Dialect dialect = new DialectResolver().getDialect(dataSource);
        initialize(dataSource, new JdbcTable(new JdbcDirectorySettings(), dialect, tableName));
    }

    /**
     * Creates a new jdbc directory. Creates new {@link JdbcDirectorySettings} using it's default values.
     *
     * @param dataSource The data source to use
     * @param dialect    The dialect
     * @param tableName  The table name
     */
    public JdbcDirectory(DataSource dataSource, Dialect dialect, String tableName) {
        initialize(dataSource, new JdbcTable(new JdbcDirectorySettings(), dialect, tableName));
    }

    /**
     * Creates a new jdbc directory. Uses {@link DialectResolver} to try and automatically reolve the {@link Dialect}.
     *
     * @param dataSource The data source to use
     * @param settings   The settings to configure the directory
     * @param tableName  The table name that will be used
     */
    public JdbcDirectory(DataSource dataSource, JdbcDirectorySettings settings, String tableName)
            throws JdbcStoreException {
        Dialect dialect = new DialectResolver().getDialect(dataSource);
        initialize(dataSource, new JdbcTable(settings, dialect, tableName));
    }

    /**
     * Creates a new jdbc directory.
     *
     * @param dataSource The data source to use
     * @param dialect    The dialect
     * @param settings   The settings to configure the directory
     * @param tableName  The table name that will be used
     */
    public JdbcDirectory(DataSource dataSource, Dialect dialect, JdbcDirectorySettings settings, String tableName) {
        initialize(dataSource, new JdbcTable(settings, dialect, tableName));
    }

    /**
     * Creates a new jdbc directory.
     *
     * @param dataSource The data source to use
     * @param table      The Jdbc table definitions
     */
    public JdbcDirectory(DataSource dataSource, JdbcTable table) {
        initialize(dataSource, table);
    }

    private void initialize(DataSource dataSource, JdbcTable table) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource, table.getSettings());
        this.dialect = table.getDialect();
        this.table = table;
        this.settings = table.getSettings();
        Map fileEntrySettings = settings.getFileEntrySettings();
        // go over all the file entry settings and configure them
        for (Iterator it = fileEntrySettings.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            JdbcFileEntrySettings feSettings = ((JdbcFileEntrySettings) fileEntrySettings.get(name));
            try {
                Class fileEntryHandlerClass = feSettings.getSettingAsClass(JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE, null);
                FileEntryHandler fileEntryHandler = (FileEntryHandler) fileEntryHandlerClass.newInstance();
                fileEntryHandler.configure(this);
                fileEntryHandlers.put(name, fileEntryHandler);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to create FileEntryHandler  [" +
                        feSettings.getSetting(JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE) + "]");
            }
        }
    }

    /**
     * Returns <code>true</code> if the database table exists.
     *
     * @return <code>true</code> if the database table exists, <code>false</code> otherwise
     * @throws IOException
     * @throws UnsupportedOperationException If the database dialect does not support it
     */
    public boolean tableExists() throws IOException, UnsupportedOperationException {
        Boolean tableExists = (Boolean) jdbcTemplate.executeSelect(dialect.sqlTableExists(table.getCatalog(), table.getSchema()),
                new JdbcTemplate.ExecuteSelectCallback() {
                    public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                        ps.setFetchSize(1);
                        ps.setString(1, table.getName().toLowerCase());
                    }

                    public Object execute(ResultSet rs) throws Exception {
                        if (rs.next()) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }
                });
        return tableExists.booleanValue();
    }

    /**
     * Deletes the database table (drops it) from the database.
     *
     * @throws IOException
     */
    public void delete() throws IOException {
        if (!dialect.supportsIfExistsAfterTableName() && !dialect.supportsIfExistsBeforeTableName()) {
            // there are databases where the fact that an exception was thrown, invalidates the connection
            // so if they do not support "if exists" in the drop clause, we will try to check first if the
            // table exists.
            if (dialect.supportsTableExists() && !tableExists()) {
                return;
            }
        }
        jdbcTemplate.executeUpdate(table.sqlDrop());
    }

    /**
     * Creates a new database table. Drops it before hand.
     *
     * @throws IOException
     */
    public void create() throws IOException {
        try {
            delete();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        jdbcTemplate.executeUpdate(table.sqlCreate());
        ((JdbcLock) createLock()).initializeDatabase(this);
    }

    /**
     * Deletes the contents of the database, except for the commit and write lock.
     *
     * @throws IOException
     */
    public void deleteContent() throws IOException {
        jdbcTemplate.executeUpdate(table.sqlDeletaAll());
    }

    /**
     * Delets all the file entries that are marked to be deleted, and they were marked
     * "delta" time ago (base on database time, if possible by dialect). The delta is
     * taken from {@link org.apache.lucene.store.jdbc.JdbcDirectorySettings#getDeleteMarkDeletedDelta()}.
     */
    public void deleteMarkDeleted() throws IOException {
        deleteMarkDeleted(settings.getDeleteMarkDeletedDelta());
    }

    /**
     * Delets all the file entries that are marked to be deleted, and they were marked
     * "delta" time ago (base on database time, if possible by dialect).
     */
    public void deleteMarkDeleted(long delta) throws IOException {
        long currentTime = System.currentTimeMillis();
        if (dialect.supportsCurrentTimestampSelection()) {
            String timestampSelectString = dialect.getCurrentTimestampSelectString();
            if (dialect.isCurrentTimestampSelectStringCallable()) {
                currentTime = ((Long) jdbcTemplate.executeCallable(timestampSelectString,
                        new JdbcTemplate.CallableStatementCallback() {
                            public void fillCallableStatement(CallableStatement cs) throws Exception {
                                cs.registerOutParameter(1, java.sql.Types.TIMESTAMP);
                            }

                            public Object readCallableData(CallableStatement cs) throws Exception {
                                Timestamp timestamp = cs.getTimestamp(1);
                                return new Long(timestamp.getTime());
                            }
                        })).longValue();
            } else {
                currentTime = ((Long) jdbcTemplate.executeSelect(timestampSelectString,
                        new JdbcTemplate.ExecuteSelectCallback() {
                            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                                // nothing to do here
                            }

                            public Object execute(ResultSet rs) throws Exception {
                                rs.next();
                                Timestamp timestamp = rs.getTimestamp(1);
                                return new Long(timestamp.getTime());
                            }
                        })).longValue();
            }
        }
        final long deleteBefore = currentTime - delta;
        jdbcTemplate.executeUpdate(table.sqlDeletaMarkDeleteByDelta(), new JdbcTemplate.PrepateStatementAwareCallback() {
            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                ps.setBoolean(1, true);
                ps.setTimestamp(2, new Timestamp(deleteBefore));
            }
        });
    }

    public String[] list() throws IOException {
        return (String[]) jdbcTemplate.executeSelect(table.sqlSelectNames(), new JdbcTemplate.ExecuteSelectCallback() {
            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                ps.setBoolean(1, false);
            }

            public Object execute(ResultSet rs) throws Exception {
                ArrayList names = new ArrayList();
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
                return (String[]) names.toArray(new String[names.size()]);
            }
        });
    }

    public boolean fileExists(final String name) throws IOException {
        return getFileEntryHandler(name).fileExists(name);
    }

    public long fileModified(final String name) throws IOException {
        return getFileEntryHandler(name).fileModified(name);
    }

    public void touchFile(final String name) throws IOException {
        getFileEntryHandler(name).touchFile(name);
    }

    public void deleteFile(final String name) throws IOException {
        getFileEntryHandler(name).deleteFile(name);
    }

    public List deleteFiles(List names) throws IOException {
        HashMap tempMap = new HashMap();
        for (Iterator it = names.iterator(); it.hasNext();) {
            String name = (String) it.next();
            FileEntryHandler fileEntryHandler = getFileEntryHandler(name);
            ArrayList tempNames = (ArrayList) tempMap.get(fileEntryHandler);
            if (tempNames == null) {
                tempNames = new ArrayList(names.size());
                tempMap.put(fileEntryHandler, tempNames);
            }
            tempNames.add(name);
        }
        ArrayList notDeleted = new ArrayList(names.size() / 2);
        for (Iterator it = tempMap.keySet().iterator(); it.hasNext();) {
            FileEntryHandler fileEntryHandler = (FileEntryHandler) it.next();
            List tempNames = (ArrayList) tempMap.get(fileEntryHandler);
            tempNames = fileEntryHandler.deleteFiles(tempNames);
            if (tempNames != null) {
                notDeleted.addAll(tempNames);
            }
        }
        return notDeleted;
    }

    public void renameFile(final String from, final String to) throws IOException {
        getFileEntryHandler(from).renameFile(from, to);
    }

    public long fileLength(final String name) throws IOException {
        return getFileEntryHandler(name).fileLength(name);
    }

    public IndexInput openInput(String name) throws IOException {
        return getFileEntryHandler(name).openInput(name);
    }

    public IndexOutput createOutput(String name) throws IOException {
        return getFileEntryHandler(name).createOutput(name);
    }

    public Lock makeLock(final String name) {
        final boolean disableLock = name.equals(IndexWriter.COMMIT_LOCK_NAME) && !settings.isUseCommitLocks();
        if (disableLock) {
            return new NoOpLock();
        }
        try {
            Lock lock = createLock();
            ((JdbcLock) lock).configure(this, name);
            return lock;
        } catch (IOException e) {
            // shoule not happen
            return null;
        }
    }

    /**
     * Closes the directory.
     */
    public void close() throws IOException {
        IOException last = null;
        for (Iterator it = fileEntryHandlers.values().iterator(); it.hasNext();) {
            FileEntryHandler fileEntryHandler = (FileEntryHandler) it.next();
            try {
                fileEntryHandler.close();
            } catch (IOException e) {
                last = e;
            }
        }
        if (last != null) {
            throw last;
        }
    }

    protected FileEntryHandler getFileEntryHandler(String name) {
        FileEntryHandler handler = (FileEntryHandler) fileEntryHandlers.get(name.substring(name.length() - 3));
        if (handler != null) {
            return handler;
        }
        handler = (FileEntryHandler) fileEntryHandlers.get(name);
        if (handler != null) {
            return handler;
        }
        return (FileEntryHandler) fileEntryHandlers.get(JdbcDirectorySettings.DEFAULT_FILE_ENTRY);

    }

    protected Lock createLock() throws IOException {
        try {
            return (Lock) settings.getLockClass().newInstance();
        } catch (Exception e) {
            throw new JdbcStoreException("Failed to create lock class [" + settings.getLockClass() + "]");
        }
    }

    public Dialect getDialect() {
        return dialect;
    }

    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    public JdbcTable getTable() {
        return this.table;
    }

    public JdbcDirectorySettings getSettings() {
        return settings;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
