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

package org.apache.lucene.store;

import java.io.IOException;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;

/**
 * A simple directory template for performing operations that result in actions being performed on the
 * directory (like index reader/searcher/writer operation). Helps in having the same code base when working
 * with different implementation of Lucene <code>Directory</code>, or switching between un-managed and managed
 * transactions with {@link JdbcDirectory}.
 * <p/>
 * Will work with any type of <code>Directory</code> implementation. Special actions will be taken if the
 * {@link JdbcDirectory} is used, where {@link DataSourceUtils#commitConnectionIfPossible(java.sql.Connection)} will
 * be called after the callback method.
 *
 * @author kimchy
 */
public class DirectoryTemplate {

    public static interface DirectoryCallback {

        Object doInDirectory(Directory dir) throws IOException;
    }

    public static abstract class DirectoryCallbackWithoutResult implements DirectoryCallback {

        public Object doInDirectory(Directory dir) throws IOException {
            doInDirectoryWithoutResult(dir);
            return null;
        }

        protected abstract void doInDirectoryWithoutResult(Directory dir) throws IOException;
    }

    private DataSource dataSource;

    private Directory dir;

    public DirectoryTemplate(Directory dir) {
        this.dir = dir;
        if (dir instanceof JdbcDirectory) {
            this.dataSource = ((JdbcDirectory) dir).getDataSource();
        }
    }

    public void execute(DirectoryCallback callback) throws IOException {
        if (dataSource == null) {
            callback.doInDirectory(dir);
            return;
        }
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            callback.doInDirectory(dir);
            DataSourceUtils.commitConnectionIfPossible(conn);
        } catch (IOException e) {
            DataSourceUtils.safeRollbackConnectionIfPossible(conn);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(conn);
        }
    }
}
