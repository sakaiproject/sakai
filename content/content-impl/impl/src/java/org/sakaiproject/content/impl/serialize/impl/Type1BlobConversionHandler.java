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
public class Type1BlobConversionHandler implements SchemaConversionHandler
{
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCreateMigrateTable()
	 */
	public String getCreateMigrateTable()
	{
		return "create table content_col_t1register ( id varchar(99), status varchar(99), primary key id )";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getDropMigrateTable()
	 */
	public String getDropMigrateTable()
	{
		return "drop table content_col_t1register";
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCheckMigrateTable()
	 */
	public String getCheckMigrateTable()
	{
		return "select count(*) from content_col_t1register";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getPopulateMigrateTable()
	 */
	public String getPopulateMigrateTable()
	{
		return "insert into content_col_t1register (id,status) select collection_id, 'pending' from content_collection";
	}

	


	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSelectNextBatch()
	 */
	public String getSelectNextBatch()
	{
		return "select id from content_col_t1register where status = 'pending' ";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCompleteNextBatch()
	 */
	public String getCompleteNextBatch()
	{
		return "update content_col_t1register set status = 'done' where id = ? ";
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getMarkNextBatch()
	 */
	public String getMarkNextBatch()
	{
		return "update content_col_t1register set status = 'locked' where id = ? ";
	}





	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSelectRecord()
	 */
	public String getSelectRecord()
	{
		return "select xml from content_collection where collection_id = ?";
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getUpdateRecord()
	 */
	public String getUpdateRecord()
	{
		return "update content_collection set xml = ? where collection_id = ? ";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSource(java.lang.String, java.sql.ResultSet)
	 */
	public Object getSource(String id, ResultSet rs) throws SQLException
	{
		return rs.getString(1);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#convertSource(java.lang.String, java.lang.Object, java.sql.PreparedStatement)
	 */
	public void convertSource(String id, Object source, PreparedStatement updateRecord)
	{
		// TODO Auto-generated method stub

	}

}
