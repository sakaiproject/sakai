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
import org.sakaiproject.util.conversion.SchemaConversionHandler;

/**
 * @author ieb
 */
public class Type1BlobCollectionConversionHandler implements SchemaConversionHandler
{
	private static final Log log = LogFactory.getLog(Type1BlobCollectionConversionHandler.class);

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
			byte[] result = t1b.serialize(sax);
			t1b.parse(sax2, result);
			sax.check(sax2);
			updateRecord.setBytes(1, result);
			updateRecord.setString(2, id);
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

		SAXSerializableCollectionAccess sourceCollection = new SAXSerializableCollectionAccess();
		SAXSerializableCollectionAccess resultCollection = new SAXSerializableCollectionAccess();
		sourceCollection.parse(xml);
		
		Type1BaseContentCollectionSerializer t1b = new Type1BaseContentCollectionSerializer();
		t1b.setTimeService(new ConversionTimeService());
		t1b.parse(resultCollection, buffer);
		
		sourceCollection.check(resultCollection);
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.conversion.SchemaConversionHandler#getValidateSource(java.lang.String, java.sql.ResultSet)
	 */
	public Object getValidateSource(String id, ResultSet rs) throws SQLException
	{
		return rs.getBytes(1);
	}
	
	

}
