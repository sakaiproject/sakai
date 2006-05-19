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

package org.apache.lucene.store.jdbc.datasource;

import java.sql.Connection;

/**
 * Subinterface of Connection to be implemented by connection proxies. Allows
 * access to the target connection.
 * <p/>
 * Initial version taken from Spring.
 *
 * @author kimchy
 */
public interface ConnectionProxy extends Connection {

    /**
     * Return the target connection of this proxy.
     * <p/>
     * This will typically either be the native JDBC Connection or a wrapper
     * from a connection pool.
     */
    Connection getTargetConnection();

    /**
     * If the given Jdbc Connection actually controls the connection.
     *
     * @see TransactionAwareDataSourceProxy
     * @see DataSourceUtils#releaseConnection(java.sql.Connection)
     */
    boolean controlConnection();
}
