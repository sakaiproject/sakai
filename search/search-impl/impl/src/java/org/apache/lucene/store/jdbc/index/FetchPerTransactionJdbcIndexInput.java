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

package org.apache.lucene.store.jdbc.index;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.JdbcStoreException;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;
import org.apache.lucene.store.jdbc.support.JdbcTable;

/**
 * Caches blobs per transaction. Only supported for dialects that supports blobs per transaction (see
 * {@link org.apache.lucene.store.jdbc.dialect.Dialect#supportTransactionalScopedBlobs()}.
 * <p/>
 * Note, using this index input requires calling the {@link #releaseBlobs(java.sql.Connection)} when the transaction
 * ends. It is automatically taken care of if using {@link org.apache.lucene.store.jdbc.datasource.TransactionAwareDataSourceProxy}.
 * If using JTA for example, a transcation synchronization should be registered with JTA to clear the blobs.
 *
 * @author kimchy
 */
public class FetchPerTransactionJdbcIndexInput extends JdbcBufferedIndexInput {

    private static final Object blobHolderLock = new Object();

    private static final ThreadLocal blobHolder = new ThreadLocal();

    public static void releaseBlobs(Connection connection) {
        synchronized (blobHolderLock) {
            Connection targetConnection = DataSourceUtils.getTargetConnection(connection);
            HashMap holdersPerConn = (HashMap) blobHolder.get();
            if (holdersPerConn == null) {
                return;
            }
            holdersPerConn.remove(targetConnection);
            holdersPerConn.remove(new Integer(System.identityHashCode(targetConnection)));
            if (holdersPerConn.isEmpty()) {
                blobHolder.set(null);
            }
        }
    }

    public static void releaseBlobs(Connection connection, JdbcTable table, String name) {
        synchronized (blobHolderLock) {
            Connection targetConnection = DataSourceUtils.getTargetConnection(connection);
            HashMap holdersPerConn = (HashMap) blobHolder.get();
            if (holdersPerConn == null) {
                return;
            }
            HashMap holdersPerName = (HashMap) holdersPerConn.get(targetConnection);
            if (holdersPerName != null) {
                holdersPerName.remove(name);
            }
            holdersPerName = (HashMap) holdersPerConn.get(new Integer(System.identityHashCode(targetConnection)));
            if (holdersPerName != null) {
                holdersPerName.remove(table.getName() + name);
            }
        }
    }

    private static Blob getBoundBlob(Connection connection, JdbcTable table, String name) {
        synchronized (blobHolderLock) {
            Connection targetConnection = DataSourceUtils.getTargetConnection(connection);
            HashMap holdersPerConn = (HashMap) blobHolder.get();
            if (holdersPerConn == null) {
                return null;
            }
            HashMap holdersPerName = (HashMap) holdersPerConn.get(targetConnection);
            if (holdersPerName == null) {
                holdersPerName = (HashMap) holdersPerConn.get(new Integer(System.identityHashCode(targetConnection)));
                if (holdersPerName == null) {
                    return null;
                }
            }
            Blob blob = (Blob) holdersPerName.get(table.getName() + name);
            if (blob != null) {
                return blob;
            }
            return null;
        }
    }

    private static void bindBlob(Connection connection, JdbcTable table, String name, Blob blob) {
        synchronized (blobHolderLock) {
            Connection targetConnection = DataSourceUtils.getTargetConnection(connection);
            HashMap holdersPerCon = (HashMap) blobHolder.get();
            if (holdersPerCon == null) {
                holdersPerCon = new HashMap();
                blobHolder.set(holdersPerCon);
            }
            HashMap holdersPerName = (HashMap) holdersPerCon.get(targetConnection);
            if (holdersPerName == null) {
                holdersPerName = (HashMap) holdersPerCon.get(new Integer(System.identityHashCode(targetConnection)));
                if (holdersPerName == null) {
                    holdersPerName = new HashMap();
                    holdersPerCon.put(targetConnection, holdersPerName);
                    holdersPerCon.put(new Integer(System.identityHashCode(targetConnection)), holdersPerName);
                }
            }

            holdersPerName.put(table.getName() + name, blob);
        }
    }

    private String name;

    // lazy intialize the length
    private long totalLength = -1;

    private long position = 1;

    private JdbcDirectory jdbcDirectory;

    public void configure(String name, JdbcDirectory jdbcDirectory, JdbcFileEntrySettings settings) throws IOException {
        super.configure(name, jdbcDirectory, settings);
        this.jdbcDirectory = jdbcDirectory;
        this.name = name;
    }


    // Overriding refill here since we can execute a single query to get both the length and the buffer data
    // resulted in not the nicest OO design, where the buffer information is protected in the JdbcBufferedIndexInput class
    // and code duplication between this method and JdbcBufferedIndexInput.
    // Performance is much better this way!
    protected void refill() throws IOException {
        Connection conn = DataSourceUtils.getConnection(jdbcDirectory.getDataSource());
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Blob blob = getBoundBlob(conn, jdbcDirectory.getTable(), name);
            if (blob == null) {
                ps = conn.prepareStatement(jdbcDirectory.getTable().sqlSelectSizeValueByName());
                ps.setFetchSize(1);
                ps.setString(1, name);

                rs = ps.executeQuery();

                // START read blob and update length if required
                if (!rs.next()) {
                    throw new JdbcStoreException("No entry for [" + name + "] table " + jdbcDirectory.getTable());
                }
                synchronized (this) {
                    if (totalLength == -1) {
                        totalLength = rs.getLong(3);
                    }
                }
                // END read blob and update length if required

                blob = rs.getBlob(2);
                bindBlob(conn, jdbcDirectory.getTable(), name, blob);
            } else {
            }
            long start = bufferStart + bufferPosition;
            long end = start + bufferSize;
            if (end > length())                  // don't read past EOF
                end = length();
            bufferLength = (int) (end - start);
            if (bufferLength <= 0)
                throw new IOException("read past EOF");

            if (buffer == null)
                buffer = new byte[bufferSize];          // allocate buffer lazily
            // not doing it anymore for performance reasons (so we don't execute separate
            // query for length)
            //readInternal(buffer, 0, bufferLength);
            readInternal(blob, buffer, 0, bufferLength);

            bufferStart = start;
            bufferPosition = 0;
        } catch (Exception e) {
            throw new JdbcStoreException("Failed to read transactional blob [" + name + "]", e);
        } finally {
            DataSourceUtils.closeResultSet(rs);
            DataSourceUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(conn);
        }
    }

    protected synchronized void readInternal(final byte[] b, final int offset, final int length) throws IOException {
        Connection conn = DataSourceUtils.getConnection(jdbcDirectory.getDataSource());
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Blob blob = getBoundBlob(conn, jdbcDirectory.getTable(), name);
            if (blob == null) {
                ps = conn.prepareStatement(jdbcDirectory.getTable().sqlSelectSizeValueByName());
                ps.setFetchSize(1);
                ps.setString(1, name);

                rs = ps.executeQuery();

                if (!rs.next()) {
                    throw new JdbcStoreException("No entry for [" + name + "] table " + jdbcDirectory.getTable());
                }

                blob = rs.getBlob(2);
                bindBlob(conn, jdbcDirectory.getTable(), name, blob);

                synchronized (this) {
                    if (this.totalLength == -1) {
                        this.totalLength = rs.getLong(3);
                    }
                }
            }
            readInternal(blob, b, offset, length);
        } catch (Exception e) {
            throw new JdbcStoreException("Failed to read transactional blob [" + name + "]", e);
        } finally {
            DataSourceUtils.closeResultSet(rs);
            DataSourceUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(conn);
        }
    }

    /**
     * A helper methods that already reads an open blob
     */
    private synchronized void readInternal(Blob blob, final byte[] b, final int offset, final int length) throws Exception {
        long curPos = getFilePointer();
        if (curPos + 1 != position) {
            position = curPos + 1;
        }
        if (position + length > length() + 1) {
            System.err.println("BAD");
        }
        byte[] bytesRead = blob.getBytes(position, length);
        if (bytesRead.length != length) {
            throw new IOException("read past EOF");
        }
        System.arraycopy(bytesRead, 0, b, offset, length);
        position += bytesRead.length;
    }

    protected void seekInternal(long pos) throws IOException {
        this.position = pos + 1;
    }

    public void close() throws IOException {
        Connection conn = DataSourceUtils.getConnection(jdbcDirectory.getDataSource());
        try {
            releaseBlobs(conn, jdbcDirectory.getTable(), name);
        } finally {
            DataSourceUtils.releaseConnection(conn);
        }
    }

    public synchronized long length() {
        if (totalLength == -1) {
            try {
                this.totalLength = jdbcDirectory.fileLength(name);
            } catch (IOException e) {
                // do nothing here for now, much better for performance
            }
        }
        return totalLength;
    }

}
