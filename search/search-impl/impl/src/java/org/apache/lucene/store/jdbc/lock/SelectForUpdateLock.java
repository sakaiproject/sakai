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

package org.apache.lucene.store.jdbc.lock;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcStoreException;
import org.apache.lucene.store.jdbc.support.JdbcTemplate;

/**
 * A lock based on select...for update.
 * <p/>
 * Note, that not all databases support select ... for update, if the database (dialect) does not support it,
 * a exception will be thrown (see {@link org.apache.lucene.store.jdbc.dialect.Dialect#supportsForUpdate()} .
 * <p/>
 * Also note, that when using select for update locking, when the database is created, the commit and write
 * locks will be created and the select for update will be performed on them. If one wishes to switch to
 * {@link PhantomReadLock}, they must be manually deleted.
 * <p/>
 * The lock is released when the transaction is committed, and not when the release method is called.

 * @author kimchy
 */
public class SelectForUpdateLock extends Lock implements JdbcLock {

    private JdbcDirectory jdbcDirectory;

    private String name;

    public void configure(JdbcDirectory jdbcDirectory, String name) throws IOException {
        if (!jdbcDirectory.getDialect().supportsForUpdate()) {
            throw new JdbcStoreException("Database dialect [" + jdbcDirectory.getDialect() + "] does not support select for update");
        }
        this.jdbcDirectory = jdbcDirectory;
        this.name = name;
    }

    public void initializeDatabase(JdbcDirectory jdbcDirectory) throws IOException {
        jdbcDirectory.getJdbcTemplate().executeUpdate(jdbcDirectory.getTable().sqlInsert(),
                new JdbcTemplate.PrepateStatementAwareCallback() {
                    public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                        ps.setFetchSize(1);
                        ps.setString(1, IndexWriter.WRITE_LOCK_NAME);
                        ps.setBlob(2, null);
                        ps.setLong(3, 0);
                        ps.setBoolean(4, false);
                    }
                });
        jdbcDirectory.getJdbcTemplate().executeUpdate(jdbcDirectory.getTable().sqlInsert(),
                new JdbcTemplate.PrepateStatementAwareCallback() {
                    public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                        ps.setFetchSize(1);
                        ps.setString(1, IndexWriter.COMMIT_LOCK_NAME);
                        ps.setBlob(2, null);
                        ps.setLong(3, 0);
                        ps.setBoolean(4, false);
                    }
                });

    }

    public boolean obtain() {
        try {
            return ((Boolean) jdbcDirectory.getJdbcTemplate().executeSelect(jdbcDirectory.getTable().sqlSelectNameForUpdateNoWait(),
                    new JdbcTemplate.ExecuteSelectCallback() {

                public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                    ps.setFetchSize(1);
                    ps.setString(1, name);
                }

                public Object execute(ResultSet rs) throws Exception {
                    if (!rs.next()) {
                        System.err.println("Should not happen, the lock [" + name + "] should already exists");
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                }
            })).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    public void release() {
        // no way to activly release a select for update lock
        // when the transaction commits or rolls back, it will be released
    }

    public boolean isLocked() {
        // no way to know if it is locked or not
        throw new IllegalStateException("SelectForUpdate lock does not support is locked");
    }

    public String toString() {
        return "SelectForUpdateLock[" + name + "/" + jdbcDirectory.getTable() + "]";
    }

}
