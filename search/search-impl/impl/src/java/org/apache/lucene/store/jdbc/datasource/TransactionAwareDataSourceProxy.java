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

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.lucene.store.jdbc.index.FetchPerTransactionJdbcIndexInput;

/**
 * Proxy for a target DataSource, adding awareness of local managed transactions.
 * Similar to a transactional JNDI DataSource as provided by a J2EE server.
 * <p/>
 * Encapsulates both a simple transaction manager based on <code>ThreadLocal</code>
 * and a <code>DataSource</code> that supports it. Should be used when no tranasction
 * managers are used (like JTA or Spring) in order to get simpler support for transactions.
 * <p/>
 * It is by no means aimed at replacing the usage of a propert transaction manager, by is provided
 * for a simple implementation of transactions for {@link org.apache.lucene.store.jdbc.JdbcDirectory}
 * (resulting in better performance), and integration with an existing <code>DataSource</code> code.
 * <p/>
 * Wraps the creates Jdbc <code>Connection</code> with a {@link ConnectionProxy}, which
 * will only close the target connection if it is controlled by it.
 * <p/>
 * The most outer <code>Connection</code> within the context of a thread, is the controlling
 * connection. Each inner <code>Connection</code> that will be retrieved using this data source
 * will return the same connection, and each call to close the connection on inner connection
 * will be disregarded. Commiting a connection should be done only on the outer most connection.
 * <p/>
 * A set of simple utilities are provided in the {@link DataSourceUtils} for simpler management of
 * the <code>DataSource</code>, and special care is taken if the <code>DataSource</code> uses the
 * {@link ConnectionProxy} (such is the case with this data soruce). For example, the
 * {@link DataSourceUtils#commitConnectionIfPossible(java.sql.Connection)} and
 * {@link DataSourceUtils#rollbackConnectionIfPossible(java.sql.Connection)} will only call commit/rollback
 * if the <code>Connection</code> was created by this data source (otherwise, in a managed environment, it
 * will be called on the actual transaction managed, or it will be using AOP).
 * <p/>
 * Note, that all the code that interacts with the database within the Jdbc Store package does not
 * commit / rollbacks the connection. It only executes it's statements, and if something goes wrong
 * throws an exception. The responsiblity for transaction management is with the calling code, and the
 * {@link TransactionAwareDataSourceProxy} is there to help non managed transaction management.
 *
 * @author kimchy
 * @see DataSourceUtils
 * @see org.apache.lucene.store.DirectoryTemplate
 */
public class TransactionAwareDataSourceProxy implements DataSource {

    private static ThreadLocal connectionHolders = new ThreadLocal();

    private DataSource dataSource;

    /**
     * Create the data source with the given data source to wrap.
     */
    public TransactionAwareDataSourceProxy(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns the targe data source.
     */
    public DataSource getTargetDataSource() {
        return dataSource;
    }

    public int getLoginTimeout() throws SQLException {
        return getTargetDataSource().getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        getTargetDataSource().setLoginTimeout(seconds);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return getTargetDataSource().getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        getTargetDataSource().setLogWriter(out);
    }

    /**
     * Not supported.
     */
    public Connection getConnection(String username, String password) throws SQLException {
        Map holders = (Map) connectionHolders.get();
        if (holders == null) {
            holders = new HashMap();
            connectionHolders.set(holders);
        }
        Connection con = (Connection) holders.get(getTargetDataSource());
        if (con == null) {
            con = getTargetDataSource().getConnection(username, password);
            holders.put(getTargetDataSource(), con);
            return getTransactionAwareConnectionProxy(con, getTargetDataSource(), true);
        }
        return getTransactionAwareConnectionProxy(con, getTargetDataSource(), false);
    }

    /**
     * Creates or returns an alreay created connection.
     * <p/>
     * If a connection was already created within the context of the local thread, and the <code>close</code>
     * method was not called yet, the ori connection will be returned (and will be a "not controlled"
     * connection, which means that any call to close will be a no op).
     * <p/>
     * Consider using {@link DataSourceUtils#getConnection(javax.sql.DataSource)} and
     * {@link DataSourceUtils#releaseConnection(java.sql.Connection)} for simpler usage.
     */
    public Connection getConnection() throws SQLException {
        Map holders = (Map) connectionHolders.get();
        if (holders == null) {
            holders = new HashMap();
            connectionHolders.set(holders);
        }
        Connection con = (Connection) holders.get(getTargetDataSource());
        if (con == null) {
            con = getTargetDataSource().getConnection();
            holders.put(getTargetDataSource(), con);
            return getTransactionAwareConnectionProxy(con, getTargetDataSource(), true);
        }
        return getTransactionAwareConnectionProxy(con, getTargetDataSource(), false);
    }

    /**
     * A simple helper that return the Jdbc <code>Connection</code> wrapped in our proxy.
     */
    protected Connection getTransactionAwareConnectionProxy(Connection target, DataSource dataSource, boolean controllsConnection) {
        return (Connection) Proxy.newProxyInstance(
                ConnectionProxy.class.getClassLoader(),
                new Class[]{ConnectionProxy.class},
                new TransactionAwareInvocationHandler(target, dataSource, controllsConnection));
    }


    /**
     * Invocation handler that delegates close calls on JDBC Connections
     * to to being aware of thread-bound transactions.
     */
    private static class TransactionAwareInvocationHandler implements InvocationHandler {

        private final Connection target;

        private final DataSource dataSource;

        private final boolean controlConnection;

        public TransactionAwareInvocationHandler(Connection target, DataSource dataSource, boolean controlConnection) {
            this.target = target;
            this.dataSource = dataSource;
            this.controlConnection = controlConnection;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Invocation on ConnectionProxy interface coming in...

            if (method.getName().equals("getTargetConnection")) {
                // Handle getTargetConnection method: return underlying Connection.
                return this.target;
            } else if (method.getName().equals("controlConnection")) {
                return (controlConnection ? Boolean.TRUE : Boolean.FALSE);
            } else if (method.getName().equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (method.getName().equals("hashCode")) {
                // Use hashCode of Connection proxy.
                return new Integer(hashCode());
            } else if (method.getName().equals("close")) {
                if (controlConnection) {
                    Map holders = (Map) connectionHolders.get();
                    if (holders == null || !holders.containsKey(dataSource)) {
                        throw new IllegalStateException("No value for data source [" + dataSource + "] bound to thread ["
                                + Thread.currentThread().getName() + "]");
                    }
                    Connection transConnection = (Connection) holders.remove(dataSource);
                    if (holders.isEmpty()) {
                        connectionHolders.set(null);
                    }
                    // clear transactional blobs as well
                    FetchPerTransactionJdbcIndexInput.releaseBlobs(transConnection);
                    transConnection.close();
                }
                return null;
            }

            // Invoke method on target Connection.
            try {
                return method.invoke(this.target, args);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }

}
