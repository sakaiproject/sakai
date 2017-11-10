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

package org.sakaiproject.db.api;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;

import org.sakaiproject.exception.ServerOverloadException;

/**
 * <p>
 * SqlService provides access to pooled Connections to Sql databases.
 * </p>
 * <p>
 * The Connection objects managed by this service are standard java.sql.Connection objects.
 * </p>
 */
public interface SqlService
{
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Transaction support
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Access an available or newly created Connection from the default pool. Will wait a while until one is available.
	 * 
	 * @return The Connection object.
	 * @throws SQLException
	 *         if a connection cannot be delivered.
	 */
	Connection borrowConnection() throws SQLException;

	/**
	 * Release a database connection.
	 * 
	 * @param conn
	 *        The connetion to release. If null or not one of ours, ignored.
	 */
	void returnConnection(Connection conn);
	
	/**
	 * Run some code in a transaction. The code is callback. Any calls to this service will be done within the transaction if they don't supply their
	 * own connection.<br />
	 * If the transaction fails due to a deadlock, it will be retried a number of times.
	 * 
	 * @param callback
	 *        The code to run.
	 * @param tag
	 *        A string to use in logging failure to identify the transaction.
	 * @return true if all went well. The SqlServiceDeadlockException will be thrown if we end up failing due to a deadlock, and the
	 *         SqlServiceUniqueViolation.
	 */
	boolean transact(Runnable callback, String tag);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Sql operations
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Read a single field from the db, from multiple records, returned as List<String>, one per record.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @return The List of Strings of single fields of the record found, or empty if none found.
	 */
	List<String> dbRead(String sql);

	/**
	 * Process a query, filling in with fields, and return the results as a List, one per record read. If a reader is provided, it will be called for each record to prepare the Object placed into the List. Otherwise, the first field of each record, as a
	 * String, will be placed in the list.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param reader
	 *        The reader object to read each record.
	 * @param <T>
	 *        The type of objects being returned by the SqlReader.
	 * @return The List of things read, one per record.
	 */
	<T> List<T> dbRead(String sql, Object[] fields, SqlReader<T> reader);

	/**
	 * Process a query, filling in with fields, and return the results as a List, one per record read. If a reader is provided, it will be called for each record to prepare the Object placed into the List. Otherwise, the first field of each record, as a
	 * String, will be placed in the list.
	 * 
	 * @param conn
	 *        The db connection object to use.
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param reader
	 *        The reader object to read each record.
	 * @param <T>
	 *        The type of objects being returned by the SqlReader.
	 * @return The List of things read, one per record.
	 */
	<T> List<T> dbRead(Connection conn, String sql, Object[] fields, SqlReader<T> reader);

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
	void dbReadBinary(String sql, Object[] fields, byte[] value);

	/**
	 * Read a single field from the db, from multiple record - concatenating the binary values into value.
	 * 
	 * @param conn
	 *        The optional db connection object to use.
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param value
	 *        The array of bytes to fill with the value read from the db.
	 */
	void dbReadBinary(Connection conn, String sql, Object[] fields, byte[] value);

	/**
	 * Read a single field / record from the db, returning a stream on the result record / field. The stream holds the conection open - so it must be closed or finalized quickly!
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
	InputStream dbReadBinary(String sql, Object[] fields, boolean big) throws ServerOverloadException;

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
	Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn);

	/**
	 * Execute the "insert" sql, returning a possible auto-update field Long value, with an additional stream parameter.
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
	 *        An input stream to add as the last parameter.
	 * @param lastLength
	 *        The number of bytes in the input stream to write.
	 * @return The auto-update value, or null
	 */
	Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn, InputStream last, int lastLength);

	/**
	 * Execute the "write" sql - no response.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @return true if successful, false if not.
	 */
	boolean dbWrite(String sql);

	/**
	 * Execute the "write" sql - no response. a long field is set to "?" - fill it in with var
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param var
	 *        The value to bind to the first parameter in the sql statement.
	 * @return true if successful, false if not.
	 */
	boolean dbWrite(String sql, String var);

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
	boolean dbWriteBinary(String sql, Object[] fields, byte[] var, int offset, int len);

	/**
	 * Execute the "write" sql - no response, using a set of fields from an array.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @return true if successful, false if not.
	 */
	boolean dbWrite(String sql, Object[] fields);

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
	boolean dbWrite(Connection connection, String sql, Object[] fields);

	/**
	 * Execute the "write" sql in a batch - no response, using an array of fields in a List and a given connection.
	 * This is optimized to execute an entire batch of writes in one prepared statement transaction.
	 *
	 * @param connection
	 *        The connection to use.
	 * @param sql
	 *        The sql statement.
	 * @param fieldsList
	 *        The List of array of fields for parameters.
	 * @return true if successful, false if not.
	 */
	boolean dbWriteBatch(Connection connection, String sql, List<Object[]> fieldsList);

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
	boolean dbWriteFailQuiet(Connection connection, String sql, Object[] fields);

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
	boolean dbWrite(String sql, Object[] fields, String lastField);

	/**
	 * Read a single field BLOB from the db from one record, and update it's bytes with content.
	 * 
	 * @param sql
	 *        The sql statement to select the BLOB.
	 * @param content
	 *        The new bytes for the BLOB.
	 */
	void dbReadBlobAndUpdate(String sql, byte[] content);

	/**
	 * Read a single field from the db, from a single record, return the value found, and lock for update.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param field
	 *        A StringBuilder that will be filled with the field.
	 * @return The Connection holding the lock.
	 */
	Connection dbReadLock(String sql, StringBuilder field);

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
	void dbUpdateCommit(String sql, Object[] fields, String var, Connection conn);

	/**
	 * Cancel the update that was locked on this connection.
	 * 
	 * @param conn
	 *        The database connection on which the lock was gained.
	 */
	void dbCancel(Connection conn);

	/**
	 * Access the calendar used in processing Time objects for Sql.
	 * 
	 * @return The calendar used in processing Time objects for Sql.
	 */
	GregorianCalendar getCal();

	/**
	 * @return a string indicating the database vendor - "oracle" or "mysql" or "hsqldb".
	 */
	String getVendor();

	/**
	 * Load and run the named file using the given class loader, as a ddl check / create. The first non-comment ('--') line will be run, and if successfull, all other non-comment lines will be run. SQL statements must be on a single line, and may have ';'
	 * terminators.
	 * 
	 * @param loader
	 *        The ClassLoader used to load the resource.
	 * @param resource
	 *        The path name to the resource - vender string and .sql will be added
	 */
	void ddl(ClassLoader loader, String resource);
	
	/**
	 * Get the next value from this sequence, for those technologies that support sequences. For the others, return null.
	 * 
	 * @param tableName
	 *        The sequence table name
	 * @param conn
	 *        The database connection to use (it will use a new one if null).
	 * @return The Integer value that is the next sequence, or null if sequences are not supported
	 */
	Long getNextSequence(String tableName, Connection conn);
	
	/**
	 * Get the SQL statement constant for a Boolean or Bit field for this value.
	 * 
	 * @param value
	 *        The value.
	 * @return The SQL statement constant for a Boolean or Bit field for this value.
	 */
	String getBooleanConstant(boolean value);

	/**
	 * @param sql
	 * @param reader
	 * @return
	 */
	Connection dbReadLock(String sql, SqlReader reader);

	/**
	 * Execute the "write/update" sql - no response, using a set of fields from an array plus one more as params and connection.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param lastField
	 *        The value to bind to the last parameter in the sql statement.
	 * @param callerConnection
	 *        The connection to use.
	 * @param failQuiet
	 *        If true, don't log errors from statement failure
	 * @return the number of records affected or -1 if something goes wrong if not due to unique constraint 
	 * violation or duplicate key (i.e. the record already exists) OR we are instructed to fail quiet.
	 */
	int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, boolean failQuiet);

	/**
	 * Execute the "write/update" sql - no response, using a set of fields from an array plus one more as params and connection.
	 *
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param lastField
	 *        The value to bind to the last parameter in the sql statement.
	 * @param callerConnection
	 *        The connection to use.
	 * @param failQuiet
	 *        If 1, log some errors (like if records already exists)
	 *        If 2, don't log errors from statement failure
	 * @return the number of records affected or -1 if something goes wrong if not due to unique constraint 
	 * violation or duplicate key (i.e. the record already exists) OR we are instructed to fail quiet.
	 */
	int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, int failQuiet);

}
