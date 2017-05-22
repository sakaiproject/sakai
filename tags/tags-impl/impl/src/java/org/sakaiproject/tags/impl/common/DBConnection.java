/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 *
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.impl.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A database connection with commit/rollback tracking.
 */
public class DBConnection {

    private final Connection connection;
    private boolean resolved;
    private boolean dirty;

    public DBConnection(Connection connection) {
        this.connection = connection;
        this.dirty = false;
        this.resolved = false;
    }

    public void commit() throws SQLException {
        connection.commit();
        resolved = true;
    }

    public void rollback() throws SQLException {
        connection.rollback();
        resolved = true;
    }

    /**
     * Record the fact that this connection has updated the database (so commit or rollback is required)
     */
    public void markAsDirty() {
        this.dirty = true;
    }

    /**
     * True if the database wasn't updated or a commit or rollback was performed.
     */
    public boolean wasResolved() {
        if (dirty) {
            return resolved;
        } else {
            return true;
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public DBPreparedStatement run(String sql) throws SQLException {
        return new DBPreparedStatement(connection.prepareStatement(sql), this);
    }

}
