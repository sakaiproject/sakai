/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.db.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.db.api.SqlServiceDeadlockException;
import org.sakaiproject.db.api.SqlServiceUniqueViolationException;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * BasicSqlService implements the SqlService.
 * </p>
 */
@Slf4j
public abstract class BasicSqlService implements SqlService
{
	/** Key name in thread local to find the current transaction connection. */
	protected static final String TRANSACTION_CONNECTION = "sqlService:transaction_connection";

	/** The "shared", "common" database connection pool */
	protected DataSource defaultDataSource;

	/** The "slow" connection pool for file uploads/downloads */
	protected DataSource longDataSource;

	/** Should we do a commit after a single statement read? */
	protected boolean m_commitAfterRead = false;

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/

	/**
	 * @return the UsageSessionService collaborator.
	 */
	protected abstract UsageSessionService usageSessionService();

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected abstract ThreadLocalManager threadLocalManager();

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/

	/**
	 * Configuration: should we do a commit after each single SQL read?
	 * 
	 * @param value
	 *        the setting (true of false) string.
	 */
	public void setCommitAfterRead(String value)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setCommitAfterRead(String " + value + ")");
		}

		m_commitAfterRead = Boolean.valueOf(value).booleanValue();
	}

	/** Database vendor used; possible values are oracle, mysql, hsqldb (default). */
	protected String m_vendor = "hsqldb";

	/**
	 * Configuration: Database vendor used; possible values are oracle, mysql, hsqldb.
	 * 
	 * @param value
	 *        the Database vendor used.
	 */
	public void setVendor(String value)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setVendor(String " + value + ")");
		}

		m_vendor = (value != null) ? value.toLowerCase().trim() : null;
	}

	/**
	 * @inheritDoc
	 */
	public String getVendor()
	{
		return m_vendor;
	}

	/** if true, debug each sql command with timing. */
	protected boolean m_showSql = false;

	/**
	 * Configuration: to show each sql command in the logs or not.
	 * 
	 * @param value
	 *        the showSql setting.
	 */
	public void setShowSql(String value)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setShowSql(String " + value + ")");
		}

		m_showSql = Boolean.valueOf(value).booleanValue();
	}

	/** Configuration: number of on-deadlock retries for save. */
	protected int m_deadlockRetries = 5;

	/**
	 * Configuration: number of on-deadlock retries for save.
	 * 
	 * @param value
	 *        the number of on-deadlock retries for save.
	 */
	public void setDeadlockRetries(String value)
	{
		m_deadlockRetries = Integer.parseInt(value);
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setAutoDdl(String " + value + ")");
		}

		m_autoDdl = Boolean.valueOf(value).booleanValue();
	}

	/** contains a map of the database dependent handlers. */
	protected Map<String, SqlServiceSql> databaseBeans;

	/** The db handler we are using. */
	protected SqlServiceSql sqlServiceSql;

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setSqlServiceSql(String vendor)
	{
		this.sqlServiceSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	/*************************************************************************************************************************************************
	 * Init and Destroy
	 ************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		setSqlServiceSql(getVendor());

		// if we are auto-creating our schema, check and create
		if (m_autoDdl)
		{
			ddl(getClass().getClassLoader(), "sakai_locks");
		}

		log.info("init(): vendor: " + m_vendor + " autoDDL: " + m_autoDdl + " deadlockRetries: " + m_deadlockRetries);
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/*************************************************************************************************************************************************
	 * Work interface methods: org.sakaiproject.sql.SqlService
	 ************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public Connection borrowConnection() throws SQLException
	{
		log.debug("borrowConnection()");

		if (defaultDataSource != null)
		{
			return defaultDataSource.getConnection();
		}
		else
		{
			throw new SQLException("no default pool.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void returnConnection(Connection conn)
	{
		if (log.isDebugEnabled())
		{
			log.debug("returnConnection(Connection " + conn + ")");
		}

		if (conn != null)
		{
			try
			{
				conn.close();
			}
			catch (SQLException e)
			{
				throw new AssertionError(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean transact(Runnable callback, String tag)
	{
		// if we are already in a transaction, stay in it (don't start a new one), and just run the callback (no retries, let the outside transaction
		// code handle that)
		if (threadLocalManager().get(TRANSACTION_CONNECTION) != null)
		{
			callback.run();
			return true;
		}

		// in case of deadlock we might retry
		for (int i = 0; i <= m_deadlockRetries; i++)
		{
			if (i > 0)
			{
				// make a little fuss
				log.warn("transact: deadlock: retrying (" + i + " / " + m_deadlockRetries + "): " + tag);

				// do a little wait, longer for each retry
				// TODO: randomize?
				try
				{
					Thread.sleep(i * 100L);
				}
				catch (Exception ignore)
				{
				}
			}

			Connection connection = null;
			boolean wasCommit = true;
			try
			{
				connection = borrowConnection();
				wasCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);

				// store the connection in the thread
				threadLocalManager().set(TRANSACTION_CONNECTION, connection);

				callback.run();

				connection.commit();

				return true;
			}
			catch (SqlServiceDeadlockException e)
			{
				// rollback
				if (connection != null)
				{
					try
					{
						connection.rollback();
						log.warn("transact: deadlock: rolling back: " + tag);
					}
					catch (Exception ee)
					{
						log.warn("transact: (deadlock: rollback): " + tag + " : " + ee);
					}
				}

				// if this was the last attempt, throw to abort
				if (i == m_deadlockRetries)
				{
					log.warn("transact: deadlock: retry failure: " + tag);
					throw e;
				}
			}
			catch (RuntimeException e)
			{
				// rollback
				if (connection != null)
				{
					try
					{
						connection.rollback();
						log.warn("transact: rolling back: " + tag);
					}
					catch (Exception ee)
					{
						log.warn("transact: (rollback): " + tag + " : " + ee);
					}
				}
				log.warn("transact: failure: " + e);
				throw e;
			}
			catch (SQLException e)
			{
				// rollback
				if (connection != null)
				{
					try
					{
						connection.rollback();
						log.warn("transact: rolling back: " + tag);
					}
					catch (Exception ee)
					{
						log.warn("transact: (rollback): " + tag + " : " + ee);
					}
				}
				log.warn("transact: failure: " + e);
				throw new RuntimeException("SqlService.transact failure", e);
			}

			finally
			{
				if (connection != null)
				{
					// clear the connection from the thread
					threadLocalManager().set(TRANSACTION_CONNECTION, null);

					try
					{
						connection.setAutoCommit(wasCommit);
					}
					catch (Exception e)
					{
						log.warn("transact: (setAutoCommit): " + tag + " : " + e);
					}
					returnConnection(connection);
				}
			}
		}

		return false;
	}

	/** Used to work with dates in GMT in the db. */
	protected final GregorianCalendar m_cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

	/**
	 * {@inheritDoc}
	 */
	public GregorianCalendar getCal()
	{
		//KNL-637 Calendar is not thread safe so we can't use the shared object
		return (GregorianCalendar)m_cal.clone();
	}

	/**
	 * Read a single field from the db, from multiple records, returned as List<String>, one per record.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @return The List of Strings of single fields of the record found, or empty if none found.
	 */
	public List<String> dbRead(String sql)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbRead(String " + sql + ")");
		}

		return dbRead(sql, null, null);
	}

	/**
	 * Process a query, filling in with fields, and return the results as a List, one per record read. If a reader is provided, it will be called for
	 * each record to prepare the Object placed into the List. Otherwise, the first field of each record, as a String, will be placed in the list.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param reader
	 *        The reader object to read each record.
	 * @return The List of things read, one per record.
	 */
	public List dbRead(String sql, Object[] fields, SqlReader reader)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbRead(String " + sql + ", Object[] " + Arrays.toString(fields) + ", SqlReader " + reader + ")");
		}

		return dbRead(null, sql, fields, reader);
	}

	/**
	 * Process a query, filling in with fields, and return the results as a List, one per record read. If a reader is provided, it will be called for
	 * each record to prepare the Object placed into the List. Otherwise, the first field of each record, as a String, will be placed in the list.
	 * 
	 * @param callerConn
	 *        The db connection object to use (if not null).
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param reader
	 *        The reader object to read each record.
	 * @return The List of things read, one per record.
	 */
	public List dbRead(Connection callerConn, String sql, Object[] fields, SqlReader reader)
	{
		// check for a transaction conncetion
		if (callerConn == null)
		{
			callerConn = (Connection) threadLocalManager().get(TRANSACTION_CONNECTION);
		}

		if (log.isDebugEnabled())
		{
			log.debug("dbRead(Connection " + callerConn + ", String " + sql + ", Object[] " + Arrays.toString(fields) + ", SqlReader " + reader + ")");
		}

		// for DEBUG
		long start = 0;
		long connectionTime = 0;
		int lenRead = 0;
		long stmtTime = 0;
		long resultsTime = 0;
		int count = 0;

		if (log.isDebugEnabled())
		{
			String userId = usageSessionService().getSessionId();
			StringBuilder buf = new StringBuilder();
			if (fields != null)
			{
				buf.append(fields[0]);
				for (int i = 1; i < fields.length; i++)
				{
					buf.append(", ");
					buf.append(fields[i]);
				}
			}
			log.debug("Sql.dbRead: " + userId + "\n" + sql + "\n" + buf);
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		ResultSetMetaData meta = null;
		List rv = new Vector();

        try {
            if (m_showSql) {
                start = System.currentTimeMillis();
            }

            // borrow a new connection if we are not provided with one to use
            if (callerConn != null) {
                conn = callerConn;
            } else {
                conn = borrowConnection();
            }
            if (m_showSql) {
                connectionTime = System.currentTimeMillis() - start;
            }
            if (m_showSql) {
                start = System.currentTimeMillis();
            }
            pstmt = conn.prepareStatement(sql);

            // put in all the fields
            prepareStatement(pstmt, fields);

            result = pstmt.executeQuery();

            if (m_showSql) {
                stmtTime = System.currentTimeMillis() - start;
            }
            if (m_showSql) {
                start = System.currentTimeMillis();
            }

            while (result.next()) {
                if (m_showSql) {
                    count++;
                }

                // without a reader, we read the first String from each record
                if (reader == null) {
                    String s;
                    ResultSetMetaData metadataResult = result.getMetaData();
                	
                    if (metadataResult != null && Types.CLOB == metadataResult.getColumnType(1)) {
                        Clob clobResult = result.getClob(1);
                        s = clobResult.getSubString(1, (int) clobResult.length());
                    } else {
                        s = result.getString(1);
                    }
                    if (s != null) {
                        rv.add(s);
                    }
                } else {
                    try {
                        Object obj = reader.readSqlResultRecord(result);
                        if (obj != null) {
                            rv.add(obj);
                        }
                    } catch (SqlReaderFinishedException e) {
                        break;
                    }
                }

            }
        } catch (SQLException e) {
            log.warn("Sql.dbRead: sql: " + sql + debugFields(fields), e);
        } catch (UnsupportedEncodingException e) {
            log.warn("Sql.dbRead: sql: " + sql + debugFields(fields), e);
        } finally {
            if (m_showSql) {
                resultsTime = System.currentTimeMillis() - start;
            }
            if (null != result) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbRead: sql: " + sql + debugFields(fields), e);
                }
            }
            if (null != pstmt) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbRead: sql: " + sql + debugFields(fields), e);
                }
            }

            // return the connection only if we have borrowed a new one for this call
            if (callerConn == null) {
                if (null != conn) {
                    // if we commit on read
                    if (m_commitAfterRead) {
                        try {
                            conn.commit();
                        } catch (SQLException e) {
                            log.warn("Sql.dbRead: sql: " + sql + debugFields(fields), e);
                        }
                    }
                    returnConnection(conn);
                }
            }

        }

		if (m_showSql) debug("Sql.dbRead: time: " + connectionTime + " / " + stmtTime + " / " + resultsTime + " #: " + count, sql, fields);

		return rv;
	}

	/**
	 * Read a single field from the db, from multiple record - concatenating the binary values into value.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param value
	 *        The array of bytes to fill with the value read from the db.
	 */
	public void dbReadBinary(String sql, Object[] fields, byte[] value)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbReadBinary(String " + sql + ", Object[] " + Arrays.toString(fields) + ")");
		}

		dbReadBinary(null, sql, fields, value);
	}

	/**
	 * Read a single field from the db, from multiple record - concatenating the binary values into value.
	 * 
	 * @param callerConn
	 *        The optional db connection object to use.
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param value
	 *        The array of bytes to fill with the value read from the db.
	 */
	public void dbReadBinary(Connection callerConn, String sql, Object[] fields, byte[] value)
	{
		// check for a transaction conncetion
		if (callerConn == null)
		{
			callerConn = (Connection) threadLocalManager().get(TRANSACTION_CONNECTION);
		}

		if (log.isDebugEnabled())
		{
			log.debug("dbReadBinary(Connection " + callerConn + ", String " + sql + ", Object[] " + Arrays.toString(fields) + ")");
		}

		// for DEBUG
		long start = 0;
		long connectionTime = 0;
		int lenRead = 0;

		if (log.isDebugEnabled())
		{
			String userId = usageSessionService().getSessionId();
			log.debug("Sql.dbReadBinary(): " + userId + "\n" + sql);
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		ResultSetMetaData meta = null;

		try
		{
			if (m_showSql) start = System.currentTimeMillis();
			if (callerConn != null)
			{
				conn = callerConn;
			}
			else
			{
				conn = borrowConnection();
			}
			if (m_showSql) connectionTime = System.currentTimeMillis() - start;
			if (m_showSql) start = System.currentTimeMillis();

			pstmt = conn.prepareStatement(sql);

			// put in all the fields
			prepareStatement(pstmt, fields);

			result = pstmt.executeQuery();

			int index = 0;
			while (result.next() && (index < value.length))
			{
				InputStream stream = result.getBinaryStream(1);
				int len = stream.read(value, index, value.length - index);
				stream.close();
				index += len;
				if (m_showSql) lenRead += len;
			}
        } catch (Exception e) {
            log.warn("Sql.dbReadBinary(): " + e);
        } finally {
            if (null != result) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): result close fail: " + e);
                }
            }
            if (null != pstmt) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): pstmt close fail: " + e);
                }
            }
            // return the connection only if we have borrowed a new one for this call
            if (callerConn == null) {
                if (null != conn) {
                    // if we commit on read
                    if (m_commitAfterRead) {
                        try {
                            conn.commit();
                        } catch (SQLException e) {
                            log.warn("Sql.dbReadBinary(): conn commit fail: " + e);
                        }
                    }
                    returnConnection(conn);
                }
            }
        }

		if (m_showSql) {
			debug("sql read binary: len: " + lenRead + "  time: " + connectionTime + " / " + (System.currentTimeMillis() - start), sql, fields);
		}
	}

	/**
	 * Read a single field / record from the db, returning a stream on the result record / field. The stream holds the conection open - so it must be
	 * closed or finalized quickly!
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param big
	 *        If true, the read is expected to be potentially large.
	 * @throws ServerOverloadException
	 *         if the read cannot complete due to lack of a free connection (if wait is false)
	 */
	public InputStream dbReadBinary(String sql, Object[] fields, boolean big) throws ServerOverloadException
	{
		// Note: does not support TRANSACTION_CONNECTION -ggolden

		if (log.isDebugEnabled())
		{
			log.debug("dbReadBinary(String " + sql + ", Object[] " + Arrays.toString(fields) + ", boolean " + big + ")");
		}

		InputStream rv = null;

		// for DEBUG
		long start = 0;
		long connectionTime = 0;
		int lenRead = 0;

        if (log.isDebugEnabled()) {
            String userId = usageSessionService().getSessionId();
            log.debug("Sql.dbReadBinary(): " + userId + "\n" + sql);
        }

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;

        try {
            if (m_showSql) {
                start = System.currentTimeMillis();
            }
            if (!big) {
                conn = borrowConnection();
            } else {
                // get a connection if it's available, else throw
                conn = borrowConnection();
                if (conn == null) {
                    throw new ServerOverloadException(null);
                }
            }
            if (m_showSql) {
                connectionTime = System.currentTimeMillis() - start;
            }
            if (m_showSql) {
                start = System.currentTimeMillis();
            }
            pstmt = conn.prepareStatement(sql);
            // put in all the fields
            prepareStatement(pstmt, fields);
            result = pstmt.executeQuery();

            if (result.next()) {
                InputStream stream = result.getBinaryStream(1);
                rv = new StreamWithConnection(stream, result, pstmt, conn);
            }
        } catch (ServerOverloadException e) {
            throw e;
        } catch (SQLException e) {
            log.warn("Sql.dbReadBinary(): " + e);
        } catch (UnsupportedEncodingException e) {
            log.warn("Sql.dbReadBinary(): " + e);
        } finally {
            // ONLY if we didn't make the rv - else let the rv hold these OPEN!
            if (rv == null) {
                if (null != result) {
                    try {
                        result.close();
                    } catch (SQLException e) {
                        log.warn("Sql.dbReadBinary(): " + e);
                    }
                }
                if (null != pstmt) {
                    try {
                        pstmt.close();
                    } catch (SQLException e) {
                        log.warn("Sql.dbReadBinary(): " + e);
                    }
                }
                if (null != conn) {
                    // if we commit on read
                    if (m_commitAfterRead) {
                        try {
                            conn.commit();
                        } catch (SQLException e) {
                            log.warn("Sql.dbReadBinary(): " + e);
                        }
                    }
                    returnConnection(conn);
                }
            }
            // log.warn("Sql.dbReadBinary(): " + e);
        }

		if (m_showSql) {
			debug("sql read binary: len: " + lenRead + "  time: " + connectionTime + " / " + (System.currentTimeMillis() - start), sql, fields);
		}
		return rv;
	}

	/**
	 * Execute the "write" sql - no response.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @return true if successful, false if not.
	 */
	public boolean dbWrite(String sql)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbWrite(String " + sql + ")");
		}

		return dbWrite(sql, null, null, null, false);

	} // dbWrite

	/**
	 * Execute the "write" sql - no response. a long field is set to "?" - fill it in with var
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param var
	 *        The value to bind to the first parameter in the sql statement.
	 * @return true if successful, false if not.
	 */
	public boolean dbWrite(String sql, String var)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbWrite(String " + sql + ", String " + var + ")");
		}

		return dbWrite(sql, null, var, null, false);
	}

	/**
	 * Execute the "write" sql - no response. a long binary field is set to "?" - fill it in with var
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param var
	 *        The value to bind to the last parameter in the sql statement.
	 * @param offset
	 *        The start within the var to write
	 * @param len
	 *        The number of bytes of var, starting with index, to write
	 * @return true if successful, false if not.
	 */
	public boolean dbWriteBinary(String sql, Object[] fields, byte[] var, int offset, int len)
	{
		// Note: does not support TRANSACTION_CONNECTION -ggolden

		if (log.isDebugEnabled())
		{
			log.debug("dbWriteBinary(String " + sql + ", Object[] " + Arrays.toString(fields) + ", byte[] " + Arrays.toString(var) + ", int " + offset + ", int " + len + ")");
		}

		// for DEBUG
		long start = 0;
		long connectionTime = 0;

		if (log.isDebugEnabled())
		{
			String userId = usageSessionService().getSessionId();
			log.debug("Sql.dbWriteBinary(): " + userId + "\n" + sql + "  size:" + var.length);
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean autoCommit = false;
		boolean resetAutoCommit = false;

		// stream from the var
		InputStream varStream = new ByteArrayInputStream(var, offset, len);

		boolean success = false;

		try
		{
			if (m_showSql) start = System.currentTimeMillis();
			conn = borrowConnection();
			if (m_showSql) connectionTime = System.currentTimeMillis() - start;

			// make sure we do not have auto commit - will change and reset if needed
			autoCommit = conn.getAutoCommit();
			if (autoCommit)
			{
				conn.setAutoCommit(false);
				resetAutoCommit = true;
			}

			if (m_showSql) start = System.currentTimeMillis();
			pstmt = conn.prepareStatement(sql);

			// put in all the fields
			int pos = prepareStatement(pstmt, fields);

			// last, put in the binary
			pstmt.setBinaryStream(pos, varStream, len);

			//int result = 
			pstmt.executeUpdate();

			// commit and indicate success
			conn.commit();
			success = true;
		}
		catch (SQLException e)
		{
			// On mysql unless you allow serverside prepared statements then the maximum size possible is configured
			// by max_allowed_packet. The error codes below are:
			// 1105 max_allowed_packet too small
			// 1118 redo log size not at least 10 times max_allowed_packet
			if ( "mysql".equals(m_vendor) && (e.getErrorCode() == 1105 || e.getErrorCode() == 1118) ) {
				log.warn("SQL '{}' failed, consider useServerPrepStmts=true on JDBC connection.", sql, e);
			}
			// this is likely due to a key constraint problem...
			return false;
		}
		catch (Exception e)
		{
			log.warn("Sql.dbWriteBinary(): " + e);
			return false;
		}
		finally
		{
			//try
			//{
			if (null != pstmt)
			{
				try {
					pstmt.close();
				} catch (SQLException e) {
					log.warn("Sql.dbWriteBinary(): " + e);
				}
			}
			if (null != varStream)
			{
				try {
					varStream.close();
				} catch (IOException e) {
					log.warn("Sql.dbWriteBinary(): " + e);
				}
			}

			if (null != conn)
			{
				// rollback on failure
				if (!success)
				{
					try {
						conn.rollback();
					} catch (SQLException e) {
						log.warn("Sql.dbWriteBinary(): " + e);
					}
				}

				// if we changed the auto commit, reset here
				if (resetAutoCommit)
				{
					try {
						conn.setAutoCommit(autoCommit);
					} catch (SQLException e) {
						log.warn("Sql.dbWriteBinary(): " + e);
					}
				}
				returnConnection(conn);
			}

		}

		if (m_showSql)
			debug("sql write binary: len: " + len + "  time: " + connectionTime + " / " + (System.currentTimeMillis() - start), sql, fields);

		return true;
	}

	/**
	 * Execute the "write" sql - no response, using a set of fields from an array plus one more as params.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @return true if successful, false if not.
	 */
	public boolean dbWrite(String sql, Object[] fields)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbWrite(String " + sql + ", Object[] " + Arrays.toString(fields) + ")");
		}

		return dbWrite(sql, fields, null, null, false);
	}

	/**
	 * Execute the "write" sql - no response, using a set of fields from an array and a given connection.
	 * 
	 * @param connection
	 *        The connection to use.
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @return true if successful, false if not.
	 */
	public boolean dbWrite(Connection connection, String sql, Object[] fields)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbWrite(Connection " + connection + ", String " + sql + ", Object[] " + Arrays.toString(fields) + ")");
		}

		return dbWrite(sql, fields, null, connection, false);
	}

	/**
	 * Execute the "write" sql - no response, using a set of fields from an array and a given connection logging no errors on failure.
	 * 
	 * @param connection
	 *        The connection to use.
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @return true if successful, false if not.
	 */
	public boolean dbWriteFailQuiet(Connection connection, String sql, Object[] fields)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbWriteFailQuiet(Connection " + connection + ", String " + sql + ", Object[] " + Arrays.toString(fields) + ")");
		}

		return dbWrite(sql, fields, null, connection, true);
	}

	/**
	 * Execute the "write" sql - no response, using a set of fields from an array plus one more as params.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param lastField
	 *        The value to bind to the last parameter in the sql statement.
	 * @return true if successful, false if not.
	 */
	public boolean dbWrite(String sql, Object[] fields, String lastField)
	{
		if (log.isDebugEnabled())
		{
			log.debug("dbWrite(String " + sql + ", Object[] " + Arrays.toString(fields) + ", String " + lastField + ")");
		}

		return dbWrite(sql, fields, lastField, null, false);
	}

	/**
	 * @see org.sakaiproject.db.api.SqlService#dbWriteCount(String, Object[], String, Connection, boolean)
	 */
	protected boolean dbWrite(String sql, Object[] fields, String lastField, Connection callerConnection, boolean failQuiet)
	{
 		return ( dbWriteCount(sql, fields, lastField, callerConnection, failQuiet) >= 0 ) ;
	}

	/**
	 * @see org.sakaiproject.db.api.SqlService#dbWriteCount(String, Object[], String, Connection, int)
	 */
	protected boolean dbWrite(String sql, Object[] fields, String lastField, Connection callerConnection, int failQuiet)
	{
 		return ( dbWriteCount(sql, fields, lastField, callerConnection, failQuiet) >= 0 ) ;
	}

	/**
	 * @see org.sakaiproject.db.api.SqlService#dbWriteCount(String, Object[], String, Connection, boolean)
	 */
	public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection,boolean failQuiet) {
		return dbWriteCount(sql,fields,lastField,callerConnection,failQuiet ? 1 : 0);
	}

	/**
	 * @see org.sakaiproject.db.api.SqlService#dbWriteBatch(Connection, String, List<Object[]>)
	 */
	public boolean dbWriteBatch(Connection callerConnection, String sql, List<Object[]> fieldsList)
	{
		boolean success = false;
		PreparedStatement pstmt = null;

		try
		{
			pstmt = callerConnection.prepareStatement(sql);
			for (Object[] fields : fieldsList)
			{
			    prepareStatement(pstmt, fields);
			    pstmt.addBatch();
			}
			pstmt.executeBatch();
			success = true;
		}
		catch (UnsupportedEncodingException e)
		{
			log.warn("Sql.dbWriteBatch()", e);
		}
		catch (SQLException e)
		{
			log.warn("Sql.dbWriteBatch(): error code: " + e.getErrorCode() + " sql: " + sql + " " + e);
		}
		finally
		{
			try
			{
				pstmt.close();
			}
			catch (Exception e)
			{
				log.warn("Sql.dbWriteBatch(): " + e);
				throw new RuntimeException("SqlService.dbWriteBatch failure", e);
			}
		}

		return success;
	}

	/**
	 * @see org.sakaiproject.db.api.SqlService#dbWriteCount(String, Object[], String, Connection, int)
	 */
	public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, int failQuiet)
	{
		int retval = -1;
		// check for a transaction connection
		if (callerConnection == null)
		{
			callerConnection = (Connection) threadLocalManager().get(TRANSACTION_CONNECTION);
		}

		if (log.isDebugEnabled())
		{
			log.debug("dbWrite(String " + sql + ", Object[] " + Arrays.toString(fields) + ", String " + lastField + ", Connection " + callerConnection + ", boolean "
					+ failQuiet + ")");
		}

		// for DEBUG
		long start = 0;
		long connectionTime = 0;

		if (log.isDebugEnabled())
		{
			String userId = usageSessionService().getSessionId();
			StringBuilder buf = new StringBuilder();
			if (fields != null)
			{
				buf.append(fields[0]);
				for (int i = 1; i < fields.length; i++)
				{
					buf.append(", ");
					buf.append(fields[i]);
				}
				if (lastField != null)
				{
					buf.append(", ");
					buf.append(lastField);
				}
			}
			else if (lastField != null)
			{
				buf.append(lastField);
			}
			log.debug("Sql.dbWrite(): " + userId + "\n" + sql + "\n" + buf);
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean autoCommit = false;
		boolean resetAutoCommit = false;

		boolean success = false;

		try
		{
			if (callerConnection != null)
			{
				conn = callerConnection;
			}
			else
			{
				if (m_showSql) start = System.currentTimeMillis();
				conn = borrowConnection();
				if (m_showSql) connectionTime = System.currentTimeMillis() - start;

				// make sure we have do not have auto commit - will change and reset if needed
				autoCommit = conn.getAutoCommit();
				if (autoCommit)
				{
					conn.setAutoCommit(false);
					resetAutoCommit = true;
				}
			}

			if (m_showSql) start = System.currentTimeMillis();
			pstmt = conn.prepareStatement(sql);

			// put in all the fields
			int pos = prepareStatement(pstmt, fields);

			// last, put in the string value
			if (lastField != null)
			{
				sqlServiceSql.setBytes(pstmt, lastField, pos);
				pos++;
			}

			retval = pstmt.executeUpdate();

			// commit unless we are in a transaction (provided with a connection)
			if (callerConnection == null)
			{
				conn.commit();
			}

			// indicate success
			success = true;
		}
		catch (SQLException e)
		{
			// is this due to a key constraint problem?... check each vendor's error codes
			boolean recordAlreadyExists = sqlServiceSql.getRecordAlreadyExists(e);

			if (m_showSql)
			{
				log.warn("Sql.dbWrite(): error code: " + e.getErrorCode() + " sql: " + sql + " binds: " + debugFields(fields) + " " + e);
			}

			// if asked to fail quietly, just return -1 if we find this error.
			if (recordAlreadyExists || failQuiet!=0) {
				//If failQuiet is 1 then print this, otherwise it's in ddl mode so just ignore
				if (failQuiet == 1) {
					log.warn("Sql.dbWrite(): recordAlreadyExists: " +  recordAlreadyExists + ", failQuiet: " + failQuiet + ", : error code: " 
					+ e.getErrorCode() + ", " + "sql: " + sql + ", binds: " + debugFields(fields) + ", error: " + e.toString());
				}
				return -1;
			}

			// perhaps due to a mysql deadlock?
			if (sqlServiceSql.isDeadLockError(e.getErrorCode()))
			{
				// just a little fuss
				log.warn("Sql.dbWrite(): deadlock: error code: " + e.getErrorCode() + " sql: " + sql + " binds: " + debugFields(fields) + " " + e.toString());
				throw new SqlServiceDeadlockException(e);
			}

			else if (recordAlreadyExists)
			{
				// just a little fuss
				log.warn("Sql.dbWrite(): unique violation: error code: " + e.getErrorCode() + " sql: " + sql + " binds: " + debugFields(fields) + " " + e.toString());
				throw new SqlServiceUniqueViolationException(e);
			}
			else
			{
				// something ELSE went wrong, so lest make a fuss
				log.warn("Sql.dbWrite(): error code: " + e.getErrorCode() + " sql: " + sql + " binds: " + debugFields(fields) + " ", e);
				throw new RuntimeException("SqlService.dbWrite failure", e);
			}
		}
		catch (Exception e)
		{
			log.warn("Sql.dbWrite(): " + e);
			throw new RuntimeException("SqlService.dbWrite failure", e);
		}
		finally
		{
			try
			{
				if (null != pstmt) pstmt.close();
				if ((null != conn) && (callerConnection == null))
				{
					// rollback on failure
					if (!success)
					{
						conn.rollback();
					}

					// if we changed the auto commit, reset here
					if (resetAutoCommit)
					{
						conn.setAutoCommit(autoCommit);
					}
					returnConnection(conn);
				}
			}
			catch (Exception e)
			{
				log.warn("Sql.dbWrite(): " + e);
				throw new RuntimeException("SqlService.dbWrite failure", e);
			}
		}

		if (m_showSql)
			debug("Sql.dbWrite(): len: " + ((lastField != null) ? "" + lastField.length() : "null") + "  time: " + connectionTime + " /  "
					+ (System.currentTimeMillis() - start), sql, fields);

		return retval;
	}

	/**
	 * Execute the "insert" sql, returning a possible auto-update field Long value
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param callerConnection
	 *        The connection to use.
	 * @param autoColumn
	 *        The name of the db column that will have auto-update - we will return the value used (leave null to disable this feature).
	 * @return The auto-update value, or null
	 */
	public Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn)
	{
		return dbInsert(callerConnection, sql, fields, autoColumn, null, 0);
	}

	/**
	 * Execute the "insert" sql, returning a possible auto-update field Long value
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param callerConnection
	 *        The connection to use.
	 * @param autoColumn
	 *        The name of the db column that will have auto-update - we will return the value used (leave null to disable this feature).
	 * @param last
	 *        A stream to set as the last field.
	 * @return The auto-update value, or null
	 */
	public Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn, InputStream last, int lastLength)
	{
		// check for a transaction connection
		if (callerConnection == null)
		{
			callerConnection = (Connection) threadLocalManager().get(TRANSACTION_CONNECTION);
		}

		if (log.isDebugEnabled())
		{
			log.debug("dbInsert(String " + sql + ", Object[] " + Arrays.toString(fields) + ", Connection " + callerConnection + ")");
		}

		// for DEBUG
		long start = 0;
		long connectionTime = 0;

		if (log.isDebugEnabled())
		{
			String userId = usageSessionService().getSessionId();
			StringBuilder buf = new StringBuilder();
			if (fields != null)
			{
				buf.append(fields[0]);
				for (int i = 1; i < fields.length; i++)
				{
					buf.append(", ");
					buf.append(fields[i]);
				}
			}
			log.debug("Sql.dbInsert(): " + userId + "\n" + sql + "\n" + buf);
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean autoCommit = false;
		boolean resetAutoCommit = false;

		boolean success = false;
		Long rv = null;

		try
		{
			if (callerConnection != null)
			{
				conn = callerConnection;
			}
			else
			{
				if (m_showSql) start = System.currentTimeMillis();
				conn = borrowConnection();
				if (m_showSql) connectionTime = System.currentTimeMillis() - start;

				// make sure we have do not have auto commit - will change and reset if needed
				autoCommit = conn.getAutoCommit();
				if (autoCommit)
				{
					conn.setAutoCommit(false);
					resetAutoCommit = true;
				}
			}

			if (m_showSql) start = System.currentTimeMillis();

			pstmt = sqlServiceSql.prepareAutoColumn(conn, sql, autoColumn);

			// put in all the fields
			int pos = prepareStatement(pstmt, fields);

			// and the last one
			if (last != null)
			{
				pstmt.setBinaryStream(pos, last, lastLength);
			}

			int result = pstmt.executeUpdate();

			rv = sqlServiceSql.getGeneratedKey(pstmt, sql);

			// commit unless we are in a transaction (provided with a connection)
			if (callerConnection == null)
			{
				conn.commit();
			}

			// indicate success
			success = true;
		}
		catch (SQLException e)
		{
			// is this due to a key constraint problem... check each vendor's error codes
			boolean recordAlreadyExists = sqlServiceSql.getRecordAlreadyExists(e);

			if (m_showSql)
			{
				log.warn("Sql.dbInsert(): error code: " + e.getErrorCode() + " sql: " + sql + " binds: " + debugFields(fields) + " " + e);
			}

			if (recordAlreadyExists) return null;

			// perhaps due to a mysql deadlock?
			if (("mysql".equals(m_vendor)) && (e.getErrorCode() == 1213))
			{
				// just a little fuss
				log.warn("Sql.dbInsert(): deadlock: error code: " + e.getErrorCode() + " sql: " + sql + " binds: " + debugFields(fields) + " "
						+ e.toString());
				throw new SqlServiceDeadlockException(e);
			}

			else if (recordAlreadyExists)
			{
				// just a little fuss
				log.warn("Sql.dbInsert(): unique violation: error code: " + e.getErrorCode() + " sql: " + sql + " binds: " + debugFields(fields)
						+ " " + e.toString());
				throw new SqlServiceUniqueViolationException(e);
			}

			else
			{
				// something ELSE went wrong, so lest make a fuss
				log.warn("Sql.dbInsert(): error code: " + e.getErrorCode() + " sql: " + sql + " binds: " + debugFields(fields) + " ", e);
				throw new RuntimeException("SqlService.dbInsert failure", e);
			}
		}
		catch (Exception e)
		{
			log.warn("Sql.dbInsert(): " + e);
			throw new RuntimeException("SqlService.dbInsert failure", e);
		}
		finally
		{
			try
			{
				if (null != pstmt) pstmt.close();
				if ((null != conn) && (callerConnection == null))
				{
					// rollback on failure
					if (!success)
					{
						conn.rollback();
					}

					// if we changed the auto commit, reset here
					if (resetAutoCommit)
					{
						conn.setAutoCommit(autoCommit);
					}
					returnConnection(conn);
				}
			}
			catch (Exception e)
			{
				log.warn("Sql.dbInsert(): " + e);
				throw new RuntimeException("SqlService.dbInsert failure", e);
			}
		}

		if (m_showSql) debug("Sql.dbWrite(): len: " + "  time: " + connectionTime + " /  " + (System.currentTimeMillis() - start), sql, fields);

		return rv;
	}

	/**
	 * Read a single field BLOB from the db from one record, and update it's bytes with content.
	 * 
	 * @param sql
	 *        The sql statement to select the BLOB.
	 * @param content
	 *        The new bytes for the BLOB.
	 */
	public void dbReadBlobAndUpdate(String sql, byte[] content)
	{
		// Note: does not support TRANSACTION_CONNECTION -ggolden

		if (log.isDebugEnabled())
		{
			log.debug("dbReadBlobAndUpdate(String " + sql + ", byte[] " + Arrays.toString(content) + ")");
		}

		if (!sqlServiceSql.canReadAndUpdateBlob())
		{
			throw new UnsupportedOperationException("BasicSqlService.dbReadBlobAndUpdate() is not supported by the " + getVendor() + " database.");
		}

		// for DEBUG
		long start = 0;
		long connectionTime = 0;
		int lenRead = 0;

		if (log.isDebugEnabled())
		{
			String userId = usageSessionService().getSessionId();
			log.debug("Sql.dbReadBlobAndUpdate(): " + userId + "\n" + sql);
		}

		Connection conn = null;
		Statement stmt = null;
		ResultSet result = null;
		ResultSetMetaData meta = null;
		Object blob = null;
		OutputStream os = null;

		try
		{
			if (m_showSql) start = System.currentTimeMillis();
			conn = borrowConnection();
			if (m_showSql) connectionTime = System.currentTimeMillis() - start;
			if (m_showSql) start = System.currentTimeMillis();
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
			if (result.next())
			{
				blob = result.getBlob(1);
			}
			if (blob != null)
			{
				// %%% not supported? b.truncate(0);
				// int len = b.setBytes(0, content);
				try
				{
					// Use reflection to remove compile time dependency on oracle driver
					Class[] paramsClasses = new Class[0];
					Method getBinaryOutputStreamMethod = blob.getClass().getMethod("getBinaryOutputStream", paramsClasses);
					Object[] params = new Object[0];
					os = (OutputStream) getBinaryOutputStreamMethod.invoke(blob, params);
					os.write(content);

				}
				catch (NoSuchMethodException ex)
				{
					log.warn("Oracle driver error: " + ex);
				}
				catch (IllegalAccessException ex)
				{
					log.warn("Oracle driver error: " + ex);
				}
				catch (InvocationTargetException ex)
				{
					log.warn("Oracle driver error: " + ex);
				} catch (IOException e) {
					log.warn("Oracle driver error: " + e);
				}
			}
		}
		catch (SQLException e)
		{
			log.warn("Sql.dbReadBlobAndUpdate(): " + e);
		}
		finally
		{
			if (null != os) {
				try {
					os.close();
				} catch (IOException e) {
					log.warn("Sql.dbRead(): " + e);
				}
			}
			if (null != result)
			{
				try {
					result.close();
				} catch (SQLException e) {
					log.warn("Sql.dbRead(): " + e);
				}
			}
			if (null != stmt)
			{
				try {
					stmt.close();
				} catch (SQLException e) {
					log.warn("Sql.dbRead(): " + e);
				}
			}
			if (null != conn)
			{
				// if we commit on read
				if (m_commitAfterRead)
				{
					try {
						conn.commit();
					} catch (SQLException e) {
						log.warn("Sql.dbRead(): " + e);
					}
				}

				returnConnection(conn);
			}

		}

		if (m_showSql)
			debug("sql dbReadBlobAndUpdate: len: " + lenRead + "  time: " + connectionTime + " / " + (System.currentTimeMillis() - start), sql, null);
	}

	/**
	 * Read a single field from the db, from a single record, return the value found, and lock for update.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param field
	 *        A StringBuilder that will be filled with the field.
	 * @return The Connection holding the lock.
	 */
	public Connection dbReadLock(String sql, StringBuilder field)
	{
		// Note: does not support TRANSACTION_CONNECTION -ggolden

		if (log.isDebugEnabled())
		{
			log.debug("dbReadLock(String " + sql + ", StringBuilder " + field + ")");
		}

		Connection conn = null;
		Statement stmt = null;
		ResultSet result = null;
		boolean autoCommit = false;
		boolean resetAutoCommit = false;
		boolean closeConn = false;

		try
		{
			// get a new connection
			conn = borrowConnection();

			// adjust to turn off auto commit - we need a transaction
			autoCommit = conn.getAutoCommit();
			if (autoCommit)
			{
				conn.setAutoCommit(false);
				resetAutoCommit = true;
			}

			if (log.isDebugEnabled()) log.debug("Sql.dbReadLock():\n" + sql);

			// create a statement and execute
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);

			// if we have a result record
			if (result.next())
			{
				// get the result and pack into the return buffer
				String rv = result.getString(1);
				if ((field != null) && (rv != null)) field.append(rv);
			}

			// otherwise we fail
			else
			{
				closeConn = true;
			}
		}

		// this is likely the error when the record is otherwise locked - we fail
		catch (SQLException e)
		{
			// Note: ORA-00054 gives an e.getErrorCode() of 54, if anyone cares...
			log.warn("Sql.dbUpdateLock(): " + e.getErrorCode() + " - " + e);
			closeConn = true;
		}
		finally
		{
			// close the result and statement
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					log.warn("Sql.dbReadBinary(): " + e);
				}
			}
			if (null != stmt) {
				try {
					stmt.close();
				} catch (SQLException e) {
					log.warn("Sql.dbReadBinary(): " + e);
				}
			}

			// if we are failing, restore and release the connection
			if ((closeConn) && (conn != null))
			{
				// just in case we got a lock
				try {
					conn.rollback();
				} catch (SQLException e) {
					log.warn("Sql.dbReadBinary(): " + e);
				}
				if (resetAutoCommit)
					try {
						conn.setAutoCommit(autoCommit);
					} catch (SQLException e) {
						log.warn("Sql.dbReadBinary(): " + e);
					}

			}


			if (conn != null)
			{
				returnConnection(conn);
			}
		}

		return conn;
	}


	/**
	 * Read a single field from the db, from a single record, return the value found, and lock for update.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param reader
	 *        A SqlReader that buils the result.
	 * @return The Connection holding the lock.
	 */
	public Connection dbReadLock(String sql, SqlReader reader)
	{
		// Note: does not support TRANSACTION_CONNECTION -ggolden

		if (log.isDebugEnabled())
		{
			log.debug("dbReadLock(String " + sql + ")");
		}

		Connection conn = null;
		Statement stmt = null;
		ResultSet result = null;
		boolean autoCommit = false;
		boolean resetAutoCommit = false;
		boolean closeConn = false;

		try
		{
			// get a new conncetion
			conn = borrowConnection();

			// adjust to turn off auto commit - we need a transaction
			autoCommit = conn.getAutoCommit();
			if (autoCommit)
			{
				conn.setAutoCommit(false);
				resetAutoCommit = true;
			}

			if (log.isDebugEnabled()) log.debug("Sql.dbReadLock():\n" + sql);

			// create a statement and execute
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);

			// if we have a result record
			if (result.next())
			{
				reader.readSqlResultRecord(result);
			}

			// otherwise we fail
			else
			{
				closeConn = true;
			}
		}

		// this is likely the error when the record is otherwise locked - we fail
		catch (SQLException e)
		{
			// Note: ORA-00054 gives an e.getErrorCode() of 54, if anyone cares...
			// log.warn("Sql.dbUpdateLock(): " + e.getErrorCode() + " - " + e);
			closeConn = true;
		}
		catch (SqlReaderFinishedException e) {
			log.warn("Sql.dbReadLock(): " + e);
			closeConn = true;
		}

		finally
		{
			// close the result and statement
			if (null != result) {
				try {
					result.close();
				} catch (SQLException e) {
					log.warn("Sql.dbReadBinary(): " + e);
				}
			}
			if (null != stmt) {
				try {
					stmt.close();
				} catch (SQLException e) {
					log.warn("Sql.dbReadBinary(): " + e);
				}
			}

			// if we are failing, restore and release the connectoin
			if ((closeConn) && (conn != null))
			{
				// just in case we got a lock
				try {
					conn.rollback();
				} catch (SQLException e) {
					log.warn("Sql.dbReadBinary(): " + e);
				}
				if (resetAutoCommit)
					try {
						conn.setAutoCommit(autoCommit);
					} catch (SQLException e) {
						log.warn("Sql.dbReadBinary(): " + e);
					}

			}
			//	log.warn("Sql.dbReadLock(): " + e);

			if (conn != null) 
			{
				returnConnection(conn);
			}
		}

		return conn;
	}

	/**
	 * Commit the update that was locked on this connection.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param var
	 *        The value to bind to the last parameter in the sql statement.
	 * @param conn
	 *        The database connection on which the lock was gained.
	 */
	public void dbUpdateCommit(String sql, Object[] fields, String var, Connection conn)
	{
		// Note: does not support TRANSACTION_CONNECTION -ggolden

		if (log.isDebugEnabled())
		{
			log.debug("dbUpdateCommit(String " + sql + ", Object[] " + Arrays.toString(fields) + ", String " + var + ", Connection " + conn + ")");
		}

		PreparedStatement pstmt = null;

		try
		{
			if (log.isDebugEnabled()) log.debug("Sql.dbUpdateCommit():\n" + sql);

			pstmt = conn.prepareStatement(sql);

			// put in all the fields
			int pos = prepareStatement(pstmt, fields);

			// prepare the update statement and fill with the last variable (if any)
			if (var != null)
			{
				sqlServiceSql.setBytes(pstmt, var, pos);
				pos++;
			}

			// run the SQL statement
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;

			// commit
			conn.commit();
		}
		catch (SQLException e)
		{
			log.warn("Sql.dbUpdateCommit(): " + e);
		} catch (UnsupportedEncodingException e) {
			log.warn("Sql.dbUpdateCommit(): " + e);
		}
		finally
		{
			try
			{
				// close the statemenet and restore / release the connection
				if (null != pstmt) pstmt.close();
				if (null != conn)
				{
					// we don't really know what this should be, but we assume the default is not
					conn.setAutoCommit(false);
					returnConnection(conn);
				}
			}
			catch (Exception e)
			{
				log.warn("Sql.dbUpdateCommit(): " + e);
			}
		}
	}

	/**
	 * Cancel the update that was locked on this connection.
	 * 
	 * @param conn
	 *        The database connection on which the lock was gained.
	 */
	public void dbCancel(Connection conn)
	{
		// Note: does not support TRANSACTION_CONNECTION -ggolden

		if (log.isDebugEnabled())
		{
			log.debug("dbCancel(Connection " + conn + ")");
		}

		try
		{
			// cancel any changes, release any locks
			conn.rollback();

			// we don't really know what this should be, but we assume the default is not
			conn.setAutoCommit(false);
			returnConnection(conn);
		}
		catch (Exception e)
		{
			log.warn("Sql.dbCancel(): " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void ddl(ClassLoader loader, String resource)
	{
		if (log.isDebugEnabled())
		{
			log.debug("ddl(ClassLoader " + loader + ", String " + resource + ")");
		}

		// add the vender string path, and extension
		resource = m_vendor + '/' + resource + ".sql";

		// find the resource from the loader
		InputStream in = loader.getResourceAsStream(resource);
		if (in == null)
		{
			log.warn("Sql.ddl: missing resource: " + resource);
			return;
		}

		try
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			try
			{
				// read the first line, skipping any '--' comment lines
				boolean firstLine = true;
				StringBuilder buf = new StringBuilder();
				for (String line = r.readLine(); line != null; line = r.readLine())
				{
					line = line.trim();
					if (line.startsWith("--")) continue;
					if (line.length() == 0) continue;

					// add the line to the buffer
					buf.append(' ');
					buf.append(line);

					// process if the line ends with a ';'
					boolean process = line.endsWith(";");

					if (!process) continue;

					// remove trailing ';'
					buf.setLength(buf.length() - 1);

					// run the first line as the test - if it fails, we are done
					if (firstLine)
					{
						firstLine = false;
						if (!dbWrite(buf.toString(),null,null,null,2))
						{
							return;
						}
					}

					// run other lines, until done - any one can fail (we will report it)
					else
					{
						dbWrite(null, buf.toString(), null);
					}

					// clear the buffer for next
					buf.setLength(0);
				}
			}
			catch (IOException any)
			{
				log.warn("Sql.ddl: resource: " + resource + " : " + any);
			}
			finally
			{
				try
				{
					r.close();
				}
				catch (IOException any)
				{
					log.warn("Sql.ddl: resource: " + resource + " : " + any);
				}
			}
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException any)
			{
				log.warn("Sql.ddl: resource: " + resource + " : " + any);
			}
		}
	}

	/**
	 * Prepare a prepared statement with fields.
	 * 
	 * @param pstmt
	 *        The prepared statement to fill in.
	 * @param fields
	 *        The Object array of values to fill in.
	 * @return the next pos that was not filled in.
	 * @throws UnsupportedEncodingException
	 */
	protected int prepareStatement(PreparedStatement pstmt, Object[] fields) throws SQLException, UnsupportedEncodingException
	{
		if (log.isDebugEnabled()) {
			log.debug("pstmt = {}, fields = {}", pstmt, Arrays.toString(fields));
		}

		// put in all the fields
		int pos = 1;
		if ((fields != null) && (fields.length > 0)) {
			for (Object field : fields) {
				if (field == null) {
					// Treat a Java null as an SQL null.
					// This makes sure that Oracle vs MySQL use the same value for null.
					sqlServiceSql.setNull(pstmt, pos);
				}
				else if (field instanceof String) {
					String s = (String) field;
					if (s.isEmpty()) {
						// Treat a zero-length Java string as an SQL null
						sqlServiceSql.setNull(pstmt, pos);
					}
					else {
						pstmt.setString(pos, s);
					}
				}
				else if (field instanceof Time) {
					Time t = (Time) field;
					sqlServiceSql.setTimestamp(pstmt, new Timestamp(t.getTime()), m_cal, pos);
				}
				else if (field instanceof Date) {
					Date d = (Date) field;
					sqlServiceSql.setTimestamp(pstmt, new Timestamp(d.getTime()), m_cal, pos);
				}
				else if (field instanceof Long) {
					long l = (Long) field;
					pstmt.setLong(pos, l);
				}
				else if (field instanceof Integer) {
					int n = (Integer) field;
					pstmt.setInt(pos, n);
				}
				else if (field instanceof Float) {
					float f = (Float) field;
					pstmt.setFloat(pos, f);
				}
				else if (field instanceof Boolean) {
					pstmt.setBoolean(pos, (Boolean) field);
				}
				else if (field instanceof byte[]) {
					sqlServiceSql.setBytes(pstmt, (byte[]) field, pos);
				}
				else {
					// %%% support any other types specially?
					String value = field.toString();
					sqlServiceSql.setBytes(pstmt, value, pos);
				}
				pos++;
			}
		}

		return pos;
	}

	/**
	 * Append a message about this SQL statement to the DEBUG string in progress, if any
	 * 
	 * @param str
	 *        The SQL statement.
	 * @param fields
	 *        The bind fields.
	 */
	protected void debug(String str, String sql, Object[] fields)
	{
		// no error will mess us up!
		try
		{
			// StringBuilder buf = (StringBuilder) CurrentService.getInThread("DEBUG");
			// if (buf == null) return;
			StringBuilder buf = new StringBuilder(2048);

			// skip some chatter
			// if (str.indexOf("SAKAI_CLUSTER") != -1) return;
			// if (str.indexOf("dual") != -1) return;

			// buf.append("\n\t");
			buf.append(str);
			buf.append(" binds: ");
			buf.append(debugFields(fields));
			buf.append(" sql: ");
			buf.append(sql);

			log.info(buf.toString());
		}
		catch (Exception ignore)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Ignored Exception: " + ignore.getMessage(), ignore);
			}
		}
	}

	protected String debugFields(Object[] fields)
	{
		StringBuilder buf = new StringBuilder();
		if (fields != null)
		{
			for (int i = 0; i < fields.length; i++)
			{
				if (fields[i] != null)
				{
					buf.append(" ");
					buf.append(fields[i].toString());
				}
				else
				{
					buf.append(" null");
				}
			}
		}
		return buf.toString();
	}

	/**
	 * <p>
	 * StreamWithConnection is a cover over a stream that comes from a statement result in a connection, holding all these until closed.
	 * </p>
	 */
	public class StreamWithConnection extends InputStream
	{
		protected Connection m_conn = null;

		protected PreparedStatement m_pstmt = null;

		protected ResultSet m_result = null;

		protected InputStream m_stream;

        public StreamWithConnection(InputStream stream, ResultSet result, PreparedStatement pstmt,
                Connection conn) {
            if (log.isDebugEnabled()) {
                log.debug("new StreamWithConnection(InputStream " + stream + ", ResultSet "
                        + result + ", PreparedStatement " + pstmt + ", Connection " + conn + ")");
            }

            m_conn = conn;
            m_result = result;
            m_pstmt = pstmt;
            m_stream = stream;
        }

        /* (non-Javadoc)
         * @see java.io.InputStream#close()
         */
        public void close() throws IOException {
            if (log.isDebugEnabled()) {
                log.debug("close()");
            }
            try {
                if (m_stream != null) {
                    m_stream.close();
                }
                m_stream = null;
            } catch (Exception e) {
            }
            try {
                if (null != m_result) {
                    m_result.close();
                }
                m_result = null;
            } catch (Exception e) {
            }
            try {
                if (null != m_pstmt) {
                    m_pstmt.close();
                }
                m_pstmt = null;
            } catch (Exception e) {
            }
            if (null != m_conn) {
                returnConnection(m_conn);
                m_conn = null;
            }
        }

        /* (non-Javadoc)
         * @see java.lang.Object#finalize()
         */
        protected void finalize() {
            if (log.isDebugEnabled()) {
                log.debug("finalize()");
            }
            try {
                close();
            } catch (IOException any) {
                log.error(any.getMessage(), any);
            }
        }

		/* (non-Javadoc)
		 * @see java.io.InputStream#read()
		 */
		public int read() throws IOException
		{
            if (log.isDebugEnabled()) {
                log.debug("read()");
            }
			return m_stream.read();
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#read(byte[])
		 */
		public int read(byte b[]) throws IOException
		{
			if (log.isDebugEnabled()) {
				log.debug("read(byte " + Arrays.toString(b) + ")");
			}

			return m_stream.read(b);
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#read(byte[], int, int)
		 */
		public int read(byte b[], int off, int len) throws IOException
		{
			if (log.isDebugEnabled()) {
				log.debug("read(byte " + Arrays.toString(b) + ", int " + off + ", int " + len + ")");
			}

			return m_stream.read(b, off, len);
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#skip(long)
		 */
		public long skip(long n) throws IOException
		{
			if (log.isDebugEnabled()) {
				log.debug("skip(long " + n + ")");
			}

			return m_stream.skip(n);
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#available()
		 */
		public int available() throws IOException
		{
            if (log.isDebugEnabled()) {
                log.debug("available()");
            }
			return m_stream.available();
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#mark(int)
		 */
		public synchronized void mark(int readlimit)
		{
			if (log.isDebugEnabled()) {
				log.debug("mark(int " + readlimit + ")");
			}

			m_stream.mark(readlimit);
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#reset()
		 */
		public synchronized void reset() throws IOException
		{
            if (log.isDebugEnabled()) {
                log.debug("reset()");
            }
			m_stream.reset();
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#markSupported()
		 */
		public boolean markSupported()
		{
            if (log.isDebugEnabled()) {
                log.debug("markSupported()");
            }
			return m_stream.markSupported();
		}
	}

	/**
	 * @param defaultDataSource
	 *        The defaultDataSource to set.
	 */
	public void setDefaultDataSource(DataSource defaultDataSource)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setDefaultDataSource(DataSource " + defaultDataSource + ")");
		}

		this.defaultDataSource = defaultDataSource;
	}

	/**
	 * @param slowDataSource
	 *        The slowDataSource to set.
	 */
	public void setLongDataSource(DataSource slowDataSource)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setLongDataSource(DataSource " + slowDataSource + ")");
		}

		this.longDataSource = slowDataSource;
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getNextSequence(String tableName, Connection conn)
	{
		String sql = sqlServiceSql.getNextSequenceSql(tableName);

		return (sql == null ? null : new Long((String) (dbRead(conn, sql, null, null).get(0))));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBooleanConstant(boolean value)
	{
		return sqlServiceSql.getBooleanConstant(value);
	}
}
