/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.journal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.apache.jackrabbit.util.Text;
import org.sakaiproject.component.cover.ComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database-based journal implementation. Stores records inside a database table
 * named <code>JOURNAL</code>, whereas the table <code>GLOBAL_REVISION</code>
 * contains the highest available revision number. These tables are located
 * inside the schema specified in <code>schemaObjectPrefix</code>. <p/> It is
 * configured through the following properties:
 * <ul>
 * <li><code>revision</code>: the filename where the parent cluster node's
 * revision file should be written to; this is a required property with no
 * default value</li>
 * <li><code>driver</code>: the JDBC driver class name to use; this is a
 * required property with no default value</li>
 * <li><code>url</code>: the JDBC connection url; this is a required
 * property with no default value </li>
 * <li><code>schema</code>: the schema to be used; if not specified, this is
 * the second field inside the JDBC connection url, delimeted by colons</li>
 * <li><code>schemaObjectPrefix</code>: the schema object prefix to be used;
 * defaults to an empty string</li>
 * <li><code>user</code>: username to specify when connecting</li>
 * <li><code>password</code>: password to specify when connecting</li>
 * </ul>
 */
public abstract class SakaiDatabaseJournal extends AbstractJournal
{

	/**
	 * Schema object prefix.
	 */
	private static final String SCHEMA_OBJECT_PREFIX_VARIABLE = "${schemaObjectPrefix}";

	/**
	 * Default DDL script name.
	 */
	private static final String DEFAULT_DDL_NAME = "default.ddl";

	/**
	 * Logger.
	 */
	private static Logger log = LoggerFactory.getLogger(SakaiDatabaseJournal.class);

	/**
	 * Schema name, bean property.
	 */
	private String schema;

	/**
	 * Schema object prefix, bean property.
	 */
	protected String schemaObjectPrefix;

	/**
	 * JDBC Connection used.
	 */
	private Connection con;

	/**
	 * Statement returning all revisions within a range.
	 */
	private PreparedStatement selectRevisionsStmt;

	/**
	 * Statement updating the global revision.
	 */
	private PreparedStatement updateGlobalStmt;

	/**
	 * Statement returning the global revision.
	 */
	private PreparedStatement selectGlobalStmt;

	/**
	 * Statement appending a new record.
	 */
	private PreparedStatement insertRevisionStmt;

	/**
	 * Locked revision.
	 */
	private long lockedRevision;

	private NamespaceResolver resolver;

	/**
	 * {@inheritDoc}
	 */
	public void init(String id, NamespaceResolver resolver) throws JournalException
	{

		super.init(id, resolver);

		if (schemaObjectPrefix == null)
		{
			schemaObjectPrefix = "";
		}
		try
		{
			
			con = getConnection();
			con.setAutoCommit(false);

			checkSchema();
			prepareStatements();
		}
		catch (Exception e)
		{
			String msg = "Unable to initialize connection.";
			throw new JournalException(msg, e);
		}
		log.info("DatabaseJournal initialized");
	}

	/**
	 * @return
	 * @throws SQLException 
	 */
	public Connection getConnection() throws SQLException
	{
		DataSource ds = (DataSource) ComponentManager.get(DataSource.class.getName());
		return ds.getConnection();
	}

	/**
	 * Derive a schema from a JDBC connection URL. This simply treats the given
	 * URL as delimeted by colons and takes the 2nd field.
	 * 
	 * @param url
	 *        JDBC connection URL
	 * @return schema
	 * @throws IllegalArgumentException
	 *         if the JDBC connection URL is invalid
	 */
	private String getSchemaFromURL(String url) throws IllegalArgumentException
	{
		int start = url.indexOf(':');
		if (start != -1)
		{
			int end = url.indexOf(':', start + 1);
			if (end != -1)
			{
				return url.substring(start + 1, end);
			}
		}
		throw new IllegalArgumentException(url);
	}

	/**
	 * {@inheritDoc}
	 */
/*
 	protected RecordIterator getRecords(long startRevision) throws JournalException
	{

		try
		{
			selectRevisionsStmt.clearParameters();
			selectRevisionsStmt.clearWarnings();
			selectRevisionsStmt.setLong(1, startRevision);
			selectRevisionsStmt.execute();

			return new DatabaseRecordIterator(selectRevisionsStmt.getResultSet(), resolver);
		}
		catch (SQLException e)
		{
			String msg = "Unable to return record iterater.";
			throw new JournalException(msg, e);
		}
	}
*/
	/**
	 * {@inheritDoc}
	 */
	protected void doLock() throws JournalException
	{
		ResultSet rs = null;
		boolean succeeded = false;

		try
		{
			con.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			String msg = "Unable to set autocommit to false.";
			throw new JournalException(msg, e);
		}

		try
		{
			updateGlobalStmt.clearParameters();
			updateGlobalStmt.clearWarnings();
			updateGlobalStmt.execute();

			selectGlobalStmt.clearParameters();
			selectGlobalStmt.clearWarnings();
			selectGlobalStmt.execute();

			rs = selectGlobalStmt.getResultSet();
			if (!rs.next())
			{
				throw new JournalException("No revision available.");
			}
			lockedRevision = rs.getLong(1);
			succeeded = true;

		}
		catch (SQLException e)
		{
			String msg = "Unable to lock global revision table.";
			throw new JournalException(msg, e);
		}
		finally
		{
			close(rs);
			if (!succeeded)
			{
				rollback(con);

				try
				{
					con.setAutoCommit(true);
				}
				catch (SQLException e)
				{
					String msg = "Unable to set autocommit to true.";
					log.warn(msg, e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doUnlock(boolean successful)
	{
		if (!successful)
		{
			rollback(con);
		}
		try
		{
			con.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			String msg = "Unable to set autocommit to true.";
			log.warn(msg, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected long append(String producerId, InputStream in, int length)
			throws JournalException
	{

		try
		{
			try
			{
				insertRevisionStmt.clearParameters();
				insertRevisionStmt.clearWarnings();
				insertRevisionStmt.setLong(1, lockedRevision);
				insertRevisionStmt.setString(2, getId());
				insertRevisionStmt.setString(3, producerId);
				insertRevisionStmt.setBinaryStream(4, in, length);
				insertRevisionStmt.execute();

				con.commit();
				return lockedRevision;
			}
			finally
			{
				try
				{
					con.setAutoCommit(true);
				}
				catch (SQLException e)
				{
					String msg = "Unable to set autocommit to true.";
					log.warn(msg, e);
				}
			}
		}
		catch (SQLException e)
		{
			String msg = "Unable to append revision " + lockedRevision + ".";
			throw new JournalException(msg, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void close()
	{
		try
		{
			con.close();
		}
		catch (SQLException e)
		{
			String msg = "Error while closing connection: " + e.getMessage();
			log.warn(msg);
		}
	}

	/**
	 * Close some input stream.
	 * 
	 * @param in
	 *        input stream, may be <code>null</code>.
	 */
	private void close(InputStream in)
	{
		try
		{
			if (in != null)
			{
				in.close();
			}
		}
		catch (IOException e)
		{
			String msg = "Error while closing input stream: " + e.getMessage();
			log.warn(msg);
		}
	}

	/**
	 * Close some statement.
	 * 
	 * @param stmt
	 *        statement, may be <code>null</code>.
	 */
	private void close(Statement stmt)
	{
		try
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
		catch (SQLException e)
		{
			String msg = "Error while closing statement: " + e.getMessage();
			log.warn(msg);
		}
	}

	/**
	 * Close some resultset.
	 * 
	 * @param rs
	 *        resultset, may be <code>null</code>.
	 */
	private void close(ResultSet rs)
	{
		try
		{
			if (rs != null)
			{
				rs.close();
			}
		}
		catch (SQLException e)
		{
			String msg = "Error while closing result set: " + e.getMessage();
			log.warn(msg);
		}
	}

	/**
	 * Rollback a connection.
	 * 
	 * @param con
	 *        connection.
	 */
	private void rollback(Connection con)
	{
		try
		{
			con.rollback();
		}
		catch (SQLException e)
		{
			String msg = "Error while rolling back connection: " + e.getMessage();
			log.warn(msg);
		}
	}

	/**
	 * Checks if the required schema objects exist and creates them if they
	 * don't exist yet.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	private void checkSchema() throws Exception
	{
		DatabaseMetaData metaData = con.getMetaData();
		String tableName = schemaObjectPrefix + "JOURNAL";
		if (metaData.storesLowerCaseIdentifiers())
		{
			tableName = tableName.toLowerCase();
		}
		else if (metaData.storesUpperCaseIdentifiers())
		{
			tableName = tableName.toUpperCase();
		}

		ResultSet rs = metaData.getTables(null, null, tableName, null);
		boolean schemaExists;
		try
		{
			schemaExists = rs.next();
		}
		finally
		{
			rs.close();
		}

		if (!schemaExists)
		{
			// read ddl from resources
			InputStream in = SakaiDatabaseJournal.class.getResourceAsStream(schema
					+ ".ddl");
			if (in == null)
			{
				String msg = "No schema-specific DDL found: '" + schema + ".ddl"
						+ "', falling back to '" + DEFAULT_DDL_NAME + "'.";
				log.info(msg);
				in = SakaiDatabaseJournal.class.getResourceAsStream(DEFAULT_DDL_NAME);
				if (in == null)
				{
					msg = "Unable to load '" + DEFAULT_DDL_NAME + "'.";
					throw new JournalException(msg);
				}
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			Statement stmt = con.createStatement();
			try
			{
				String sql = reader.readLine();
				while (sql != null)
				{
					// Skip comments and empty lines
					if (!sql.startsWith("#") && sql.length() > 0)
					{
						// replace prefix variable
						sql = Text.replace(sql, SCHEMA_OBJECT_PREFIX_VARIABLE,
								schemaObjectPrefix);
						// execute sql stmt
						stmt.executeUpdate(sql);
					}
					// read next sql stmt
					sql = reader.readLine();
				}
				// commit the changes
				con.commit();
			}
			finally
			{
                close(stmt);
                try {
                    reader.close();
                } catch (IOException e) {
                    // nothing to do but complain
                    log.warn("Failed to close the input stream reader");
                }
				close(in);
			}
		}
	}

	/**
	 * Builds and prepares the SQL statements.
	 * 
	 * @throws SQLException
	 *         if an error occurs
	 */
	private void prepareStatements() throws SQLException
	{
		selectRevisionsStmt = con
				.prepareStatement("select REVISION_ID, JOURNAL_ID, PRODUCER_ID, REVISION_DATA "
						+ "from "
						+ schemaObjectPrefix
						+ "JOURNAL "
						+ "where REVISION_ID > ?");
		updateGlobalStmt = con.prepareStatement("update " + schemaObjectPrefix
				+ "GLOBAL_REVISION " + "set revision_id = revision_id + 1");
		selectGlobalStmt = con.prepareStatement("select revision_id " + "from "
				+ schemaObjectPrefix + "GLOBAL_REVISION");
		insertRevisionStmt = con.prepareStatement("insert into " + schemaObjectPrefix
				+ "JOURNAL" + "(REVISION_ID, JOURNAL_ID, PRODUCER_ID, REVISION_DATA) "
				+ "values (?,?,?,?)");
	}


	public String getSchema()
	{
		return schema;
	}

	public String getSchemaObjectPrefix()
	{
		return schemaObjectPrefix;
	}


	public void setSchema(String schema)
	{
		this.schema = schema;
	}

	public void setSchemaObjectPrefix(String schemaObjectPrefix)
	{
		this.schemaObjectPrefix = schemaObjectPrefix.toUpperCase();
	}

	protected void append(AppendRecord record, InputStream in, int length)
		throws JournalException {
		throw new UnsupportedOperationException("Not yet implemented.");
	}


}
