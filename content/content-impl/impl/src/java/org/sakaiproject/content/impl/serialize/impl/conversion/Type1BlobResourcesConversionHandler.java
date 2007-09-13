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
 * @author ieb
 */
public class Type1BlobResourcesConversionHandler implements SchemaConversionHandler
{

	private static final Log log = LogFactory
			.getLog(Type1BlobResourcesConversionHandler.class);

	private Pattern contextPattern = Pattern.compile("\\A/group/(.+?)/");


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
				context = contextMatcher.group(1);
			}

			updateRecord.setString(1, context);
			updateRecord.setLong(2, sax.getSerializableContentLength());
			updateRecord.setBytes(3, result);
			updateRecord.setString(4, id);
			return true;
		}
		catch (Exception e)
		{
			log.warn("Failed to process record " + id, e);
		}
		return false;

	}
	
	/** 
	 * @see org.sakaiproject.content.impl.serialize.impl.conversion.SchemaConversionHandler#validate(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void validate(String id, Object source, Object result) throws Exception
	{
		String xml = (String) source;
		String type1 = (String) result;

		SAXSerializableResourceAccess sourceResource = new SAXSerializableResourceAccess();
		SAXSerializableResourceAccess resultResource = new SAXSerializableResourceAccess();
		sourceResource.parse(xml);
		resultResource.parse(type1);
		
		sourceResource.check(resultResource);
		
	}


}
