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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentCollectionSerializer;

/**
 * @author ieb
 */
public class Type1BlobCollectionConversionHandler implements SchemaConversionHandler
{
	private static final Log log = LogFactory.getLog(Type1BlobCollectionConversionHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCreateMigrateTable()
	 */
	public String getCreateMigrateTable()
	{
		return "create table content_col_t1register ( id varchar(1024), status varchar(99) )";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getDropMigrateTable()
	 */
	public String getDropMigrateTable()
	{
		return "drop table content_col_t1register";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCheckMigrateTable()
	 */
	public String getCheckMigrateTable()
	{
		return "select count(*) from content_col_t1register";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getPopulateMigrateTable()
	 */
	public String getPopulateMigrateTable()
	{
		return "insert into content_col_t1register (id,status) select COLLECTION_ID, 'pending' from CONTENT_COLLECTION";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSelectNextBatch()
	 */
	public String getSelectNextBatch()
	{
		return "select id from content_col_t1register where status = 'pending' ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCompleteNextBatch()
	 */
	public String getCompleteNextBatch()
	{
		return "update content_col_t1register set status = 'done' where id = ? ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getMarkNextBatch()
	 */
	public String getMarkNextBatch()
	{
		return "update content_col_t1register set status = 'locked' where id = ? ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSelectRecord()
	 */
	public String getSelectRecord()
	{
		return "select XML from CONTENT_COLLECTION where COLLECTION_ID = ?";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getUpdateRecord()
	 */
	public String getUpdateRecord()
	{
		return "update CONTENT_COLLECTION set XML = ? where COLLECTION_ID = ? ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSource(java.lang.String,
	 *      java.sql.ResultSet)
	 */
	public Object getSource(String id, ResultSet rs) throws SQLException
	{
		return rs.getString(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#convertSource(java.lang.String,
	 *      java.lang.Object, java.sql.PreparedStatement)
	 */
	public boolean convertSource(String id, Object source, PreparedStatement updateRecord)
			throws SQLException
	{

		String xml = (String) source;

		SAXSerializableCollectionAccess sax = new SAXSerializableCollectionAccess();
		SAXSerializableCollectionAccess sax2 = new SAXSerializableCollectionAccess();
		try
		{
			sax.parse(xml);
		}
		catch (Exception e1)
		{
			log.warn("Failed to parse "+id+"["+xml+"]",e1);
			return false;
		}

		Type1BaseContentCollectionSerializer t1b = new Type1BaseContentCollectionSerializer();
		t1b.setTimeService(new ConversionTimeService());
		try
		{
			String result = t1b.serialize(sax);
			t1b.parse(sax2, result);
			sax.check(sax2);
			updateRecord.setString(1, result);
			updateRecord.setString(2, id);
			return true;
		}
		catch (Exception e)
		{
			log.warn("Failed to process record " + id, e);
		}
		return false;

	}

}
