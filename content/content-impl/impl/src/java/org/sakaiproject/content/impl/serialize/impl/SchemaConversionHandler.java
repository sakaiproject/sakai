/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.serialize.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ieb
 *
 */
public interface SchemaConversionHandler
{

	/**
	 * An SQL statement to select the next list of items to process, 
	 * It should select these from the Register table, in such a way as to ensure that they
	 * are only selected by the current node. 
	 * it will take the first column returned as the unique id of the item
	 * eg select id from migrate_content_collection where status = 'pending';
	 * @return
	 */
	String getSelectNextBatch();

	/**
	 * SQL to mark the id as being worked on in the register table eg
	 * eg update migrate_content_collection set status = 'locked' where id = ?;
	 * @return
	 */
	String getMarkNextBatch();

	/**
	 * SQL to mark the is as completed in the register table eg
	 * parameter 1 is the ID
	 * update migrate_content_collection set status = 'completed' where id = ?;
	 * @return
	 */
	String getCompleteNextBatch();

	/**
	 * SQL to select the record form the table to be converted, colums are passed for processing to getSource 
	 * select * from content_collection where collection_id = ?;
	 * @return 
	 */
	String getSelectRecord();

	/**
	 * SQL to Update the target record after conversion, the prepared statement is passed to convert source
	 * for polulating eg
	 * update content_collection set xml = ? where collection_id = ?
	 * @return
	 */
	String getUpdateRecord();

	/**
	 * SQL to drop the migration regisgter
	 * eg drop table migrate_content_collection
	 * @return
	 */
	String getDropMigrateTable();

	/**
	 * SQL to check if the migration register exists and has been populated with pendign records
	 * column 1 should be 0 if this is not the case
	 * select count(*) from migrate_content_collection;
	 * @return
	 */
	String getCheckMigrateTable();

	/**
	 * SQL to create the migration table
	 * create table migrate_content_collection ( id varchar(99), status varchar(99), primary key id );
	 * @return
	 */
	String getCreateMigrateTable();

	/**
	 * SQL to populate the migration table with ID's  in the correct state.
	 * insert into migrate_content_collection (id, status) select collection_id, 'pending' from content_collection
	 * @return
	 */
	String getPopulateMigrateTable();

	/**
	 * Retrievs a single source object form a ResultSet, returns null if the source record does not require conversion
	 * the result set was produiced from selectRecord SQL
	 * @param id
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	Object getSource(String id, ResultSet rs) throws SQLException;

	/**
	 * Converts the source object and populates the preapred statement, the prepared statement is from getUpdateRecord.
	 * @param id
	 * @param source
	 * @param updateRecord
	 */
	void convertSource(String id, Object source, PreparedStatement updateRecord);



}
