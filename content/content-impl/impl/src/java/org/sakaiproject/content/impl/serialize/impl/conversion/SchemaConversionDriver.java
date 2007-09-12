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

package org.sakaiproject.content.impl.serialize.impl.conversion;

import java.util.Properties;

/**
 * @author ieb
 *
 */
public class SchemaConversionDriver
{

	private Properties p;
	private String base;

	public SchemaConversionDriver() {
		
	}
	public void load(Properties p, String base) {
		this.p = p;
		this.base = base;
	}
	
	public String getHandler() {
		return p.getProperty(base);
	}
	public String getHandlerClass() {
		return p.getProperty(base+".handler.class");
	}
	/**
	 * An SQL statement to select the next list of items to process, It should
	 * select these from the Register table, in such a way as to ensure that
	 * they are only selected by the current node. it will take the first column
	 * returned as the unique id of the item eg select id from
	 * migrate_content_collection where status = 'pending';
	 * 
	 * @return
	 */
	public String getSelectNextBatch() {
		return p.getProperty(base+".select.next.batch");
	}

	/**
	 * SQL to mark the id as being worked on in the register table eg eg update
	 * migrate_content_collection set status = 'locked' where id = ?;
	 * 
	 * @return
	 */
	public String getMarkNextBatch() {
		return p.getProperty(base+".mark.next.batch");

	}

	/**
	 * SQL to mark the is as completed in the register table eg parameter 1 is
	 * the ID update migrate_content_collection set status = 'completed' where
	 * id = ?;
	 * 
	 * @return
	 */
	public String getCompleteNextBatch() {
		return p.getProperty(base+".complete.next.batch");
	}

	/**
	 * SQL to select the record form the table to be converted, colums are
	 * passed for processing to getSource select * from content_collection where
	 * collection_id = ?;
	 * 
	 * @return
	 */
	public String getSelectRecord() {
		return p.getProperty(base+".select.record");
	}

	/**
	 * SQL to Update the target record after conversion, the prepared statement
	 * is passed to convert source for polulating eg update content_collection
	 * set xml = ? where collection_id = ?
	 * 
	 * @return
	 */
	public String getUpdateRecord() {
		return p.getProperty(base+".update.record");
	}

	/**
	 * SQL to drop the migration regisgter eg drop table
	 * migrate_content_collection
	 * 
	 * @return
	 */
	public String getDropMigrateTable() {
		return p.getProperty(base+".drop.migrate.table");
		
	}

	/**
	 * SQL to check if the migration register exists and has been populated with
	 * pendign records column 1 should be 0 if this is not the case select
	 * count(*) from migrate_content_collection;
	 * 
	 * @return
	 */
	public String getCheckMigrateTable() {
	return p.getProperty(base+".check.migrate.table");
	}

	/**
	 * SQL to create the migration table create table migrate_content_collection (
	 * id varchar(99), status varchar(99), primary key id );
	 * 
	 * @return
	 */
	public String getCreateMigrateTable() {
		return p.getProperty(base+".create.migrate.table");
	}

	/**
	 * SQL to populate the migration table with ID's in the correct state.
	 * insert into migrate_content_collection (id, status) select collection_id,
	 * 'pending' from content_collection
	 * 
	 * @return
	 */
	public String getPopulateMigrateTable() {
		return p.getProperty(base+".populate.migrate.table");
	}


}
