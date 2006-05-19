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

import org.apache.lucene.store.Lock;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.support.JdbcTemplate;

/**
 * A lock based on phantom reads and table level locking. For most database and most transaction
 * isolation levels this lock is suffecient.
 * <p/>
 * The existance of the lock in the database, marks it as being locked.
 * <p/>
 * The benefits of using this lock is the ability to release it.
 *
 * @author kimchy
 */
public class PhantomReadLock extends Lock implements JdbcLock {

    private JdbcDirectory jdbcDirectory;

    private String name;

    public void configure(JdbcDirectory jdbcDirectory, String name) throws IOException {
        this.jdbcDirectory = jdbcDirectory;
        this.name = name;
    }

    public void initializeDatabase(JdbcDirectory jdbcDirectory) {
        // do nothing
    }

    public boolean obtain() {
        try {
            if (jdbcDirectory.getDialect().useExistsBeforeInsertLock()) {
                // there are databases where the fact that an exception was thrown
                // invalidates the connection. So first we check if it exists, and
                // then insert it.
                if (jdbcDirectory.fileExists(name)) {
                    return false;
                }
            }
            jdbcDirectory.getJdbcTemplate().executeUpdate(jdbcDirectory.getTable().sqlInsert(),
                    new JdbcTemplate.PrepateStatementAwareCallback() {
                        public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                            ps.setFetchSize(1);
                            ps.setString(1, name);
                            ps.setBlob(2, null);
                            ps.setLong(3, 0);
                            ps.setBoolean(4, false);
                        }
                    });
        } catch (Exception e) {
        	   e.printStackTrace();
            return false;
        }
        return true;
    }

    public void release() {
        try {
            jdbcDirectory.getJdbcTemplate().executeUpdate(jdbcDirectory.getTable().sqlDeleteByName(),
                    new JdbcTemplate.PrepateStatementAwareCallback() {
                        public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                            ps.setFetchSize(1);
                            ps.setString(1, name);
                        }
                    });
        } catch (Exception e) {
            // do nothing
        }
    }

    public boolean isLocked() {
        try {
            return jdbcDirectory.fileExists(name);
        } catch (Exception e) {
            return false;
        }
    }

    public String toString() {
        return "PhantomReadLock[" + name + "/" + jdbcDirectory.getTable() + "]";
    }
}
