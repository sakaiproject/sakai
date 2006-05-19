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

package org.apache.lucene.store.jdbc.handler;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.store.jdbc.support.JdbcTemplate;

/**
 * Does not delete entries from the database, just marks them for deletion by updating the deleted
 * column to <code>true</code>.
 * <p/>
 * To really delete file entries, use {@link org.apache.lucene.store.jdbc.JdbcDirectory#deleteMarkDeleted()}
 * or {@link org.apache.lucene.store.jdbc.JdbcDirectory#deleteMarkDeleted(long)}.
 *
 * @author kimchy
 */
public class MarkDeleteFileEntryHandler extends AbstractFileEntryHandler {

    public void deleteFile(final String name) throws IOException {
        jdbcTemplate.executeUpdate(table.sqlMarkDeleteByName(), new JdbcTemplate.PrepateStatementAwareCallback() {
            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                ps.setFetchSize(1);
                ps.setBoolean(1, true);
                ps.setString(2, name);
            }
        });
    }

    public List deleteFiles(final List names) throws IOException {
        jdbcTemplate.executeBatch(table.sqlMarkDeleteByName(), new JdbcTemplate.PrepateStatementAwareCallback() {
            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                ps.setFetchSize(1);
                for (Iterator it = names.iterator(); it.hasNext();) {
                    ps.setBoolean(1, true);
                    ps.setString(2, (String) it.next());
                    ps.addBatch();
                }
            }
        });
        return null;
    }
}
