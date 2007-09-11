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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentResourceSerializer;

/**
 * Performs just the file size conversion for quota calculations 
 * @author ieb
 */
public class FileSizeResourcesConversionHandler implements SchemaConversionHandler
{

	private static final Log log = LogFactory
			.getLog(FileSizeResourcesConversionHandler.class);

	private Pattern contextPattern = Pattern.compile("\\A/group/(.+?)/");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCreateMigrateTable()
	 */
	public String getCreateMigrateTable()
	{
		return "create table content_res_fsregister ( id varchar(1024), status varchar(99) )";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getDropMigrateTable()
	 */
	public String getDropMigrateTable()
	{
		return "drop table content_res_fsregister";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCheckMigrateTable()
	 */
	public String getCheckMigrateTable()
	{
		return "select count(*) from content_res_fsregister";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getPopulateMigrateTable()
	 */
	public String getPopulateMigrateTable()
	{
		return "insert into content_res_fsregister (id,status) select RESOURCE_ID, 'pending' from CONTENT_RESOURCE";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSelectNextBatch()
	 */
	public String getSelectNextBatch()
	{
		return "select id from content_res_fsregister where status = 'pending' ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getCompleteNextBatch()
	 */
	public String getCompleteNextBatch()
	{
		return "update content_res_fsregister set status = 'done' where id = ? ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getMarkNextBatch()
	 */
	public String getMarkNextBatch()
	{
		return "update content_res_fsregister set status = 'locked' where id = ? ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSelectRecord()
	 */
	public String getSelectRecord()
	{
		return "select XML from CONTENT_RESOURCE where RESOURCE_ID = ?";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getUpdateRecord()
	 */
	public String getUpdateRecord()
	{
		return "update CONTENT_RESOURCE set CONTEXT = ?, FILE_SIZE = ? where RESOURCE_ID = ? ";
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

		SAXSerializableResourceAccess sax = new SAXSerializableResourceAccess();
		try
		{
			sax.parse(xml);
		}
		catch (Exception e1)
		{
			log.warn("Failed to parse "+id+"["+xml+"]",e1);
			return false;
		}

		Type1BaseContentResourceSerializer t1b = new Type1BaseContentResourceSerializer();
		t1b.setTimeService(new ConversionTimeService());
		try
		{
			Matcher contextMatcher = contextPattern.matcher(sax.getSerializableId());
			String context = null;
			if (contextMatcher.find())
			{
				context = contextMatcher.group(1);
			}

			updateRecord.setString(1, context);
			updateRecord.setLong(2, sax.getSerializableContentLength());
			updateRecord.setString(3, id);
			return true;
		}
		catch (Exception e)
		{
			log.warn("Failed to process record " + id, e);
		}
		return false;

	}
	
	

}
