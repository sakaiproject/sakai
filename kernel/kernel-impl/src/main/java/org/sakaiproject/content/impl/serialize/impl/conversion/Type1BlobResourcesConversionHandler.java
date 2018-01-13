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

package org.sakaiproject.content.impl.serialize.impl.conversion;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentResourceSerializer;
import org.sakaiproject.util.conversion.SchemaConversionHandler;

/**
 * @author ieb
 */
@Slf4j
public class Type1BlobResourcesConversionHandler implements SchemaConversionHandler
{
	private Pattern contextPattern = Pattern.compile("\\A/(group/|user/|~)(.+?)/");


	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSource(java.lang.String,
	 *      java.sql.ResultSet)
	 */
	public Object getSource(String id, ResultSet rs) throws SQLException
	{
		ResultSetMetaData metadata = rs.getMetaData();
		String rv = null;
		switch(metadata.getColumnType(1))
		{
		case Types.BLOB:
			Blob blob = rs.getBlob(1);
			if(blob != null)
			{
				rv = new String(blob.getBytes(1L, (int) blob.length()));
			}
			break;
		case Types.CLOB:
			Clob clob = rs.getClob(1);
			if(clob != null)
			{
				rv = clob.getSubString(1L, (int) clob.length());
			}
			break;
		case Types.CHAR:
		case Types.LONGVARCHAR:
		case Types.VARCHAR:
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			byte[] bytes = rs.getBytes(1);
			if(bytes != null)
			{
				rv = new String(bytes);
			}
			break;
		}
		return rv;
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
		SAXSerializableResourceAccess sax2 = new SAXSerializableResourceAccess();
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
			byte[] result = t1b.serialize(sax);
			t1b.parse(sax2, result);
			sax.check(sax2);

			Matcher contextMatcher = contextPattern.matcher(sax.getSerializableId());
			String context = null;
			if (contextMatcher.find())
			{
				String root = contextMatcher.group(1);
				context = contextMatcher.group(2);
				if(! root.equals("group/"))
				{
					context = "~" + context;
				}
			}

			updateRecord.setString(1, context);
			updateRecord.setLong(2, sax.getSerializableContentLength());
			updateRecord.setBytes(3, result);
			updateRecord.setString(4, sax.getSerializableResourceType());
			updateRecord.setString(5, id);
			return true;
		}
		catch (Exception e)
		{
			log.warn("Failed to process record " + id, e);
		}
		return false;

	}

	/**
	 * @see org.sakaiproject.util.conversion.SchemaConversionHandler#validate(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void validate(String id, Object source, Object result) throws Exception
	{
		String xml = (String) source;
		byte[] buffer = (byte[]) result;

		SAXSerializableResourceAccess sourceResource = new SAXSerializableResourceAccess();
		SAXSerializableResourceAccess resultResource = new SAXSerializableResourceAccess();
		sourceResource.parse(xml);

		Type1BaseContentResourceSerializer t1b = new Type1BaseContentResourceSerializer();
		t1b.setTimeService(new ConversionTimeService());
		t1b.parse(resultResource, buffer);

		sourceResource.check(resultResource);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.conversion.SchemaConversionHandler#getValidateSource(java.lang.String, java.sql.ResultSet)
	 */
	public Object getValidateSource(String id, ResultSet rs) throws SQLException
	{
		ResultSetMetaData metadata = rs.getMetaData();
		byte[] rv = null;
		switch(metadata.getColumnType(1))
		{
		case Types.BLOB:
			Blob blob = rs.getBlob(1);
			if(blob != null)
			{
				rv = blob.getBytes(1L, (int) blob.length());
			}
			else
			{
				log.info("getValidateSource(" + id + ") blob is null" );
			}
			break;
		case Types.CLOB:
			Clob clob = rs.getClob(1);
			if(clob != null)
			{
				rv = clob.getSubString(1L, (int) clob.length()).getBytes();
			}
			break;
		case Types.CHAR:
		case Types.LONGVARCHAR:
		case Types.VARCHAR:
			rv = rs.getString(1).getBytes();
			break;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			rv = rs.getBytes(1);
			break;
		}
		return rv;

		//return rs.getBytes(1);
	}
}
