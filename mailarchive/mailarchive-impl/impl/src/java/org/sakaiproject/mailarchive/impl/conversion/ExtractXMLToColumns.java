/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.mailarchive.impl.conversion;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;

import org.sakaiproject.util.conversion.SchemaConversionHandler;

/**
 * @author ieb
 */
@Slf4j
public class ExtractXMLToColumns implements SchemaConversionHandler
{
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
		log.debug("convertSource id={} prep={} source={}", id, updateRecord, source);
		String xml = (String) source;

		if (!xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
		{
			log.warn("Improperly formatted XML");
			return false;
		}

		/*
		 * <?xml version="1.0" encoding="UTF-8"?> <message
		 * body="Qm9keSAyMDA4MDEyNzIwMTM0MTkzMw=="
		 * body-html="Qm9keSAyMDA4MDEyNzIwMTM0MTkzMw=="> <header
		 * access="channel" date="20080127201341934" from="admin"
		 * id="d978685c-8730-4975-b3ea-55fdf03e0e5a"
		 * mail-date="20080127201341933" mail-from="from 20080127201341933"
		 * subject="Subject 20080127201341933"/><properties/></message>
		 */
		String body = getXmlAttr(xml, "body");
		String subject = getXmlAttr(xml, "subject");

		byte[] decoded = null;
		try 
		{
			if ( body != null ) 
			{
				decoded = Base64.decodeBase64(body); // UTF-8 by default
				body = org.apache.commons.codec.binary.StringUtils.newStringUtf8(decoded);
			}
		} 
		catch (Exception e) 
		{
			log.warn("Error Base64 Decoding Body and HTML Body");
			return false;
		}

		updateRecord.setString(1, subject);
		updateRecord.setString(2, body);
		updateRecord.setString(3, id);
		return true;
	}

	String getXmlAttr(String xml, String tagName)
	{
		String lookfor = tagName+"=\""; 
		int ipos = xml.indexOf(lookfor);
		if ( ipos < 1 ) return null;
		ipos = ipos + lookfor.length();
		int jpos = xml.indexOf("\"",ipos);
		if ( jpos < 1 || ipos > jpos ) return null;
		return xml.substring(ipos,jpos);
	}

	/**
	 * @see org.sakaiproject.util.conversion.SchemaConversionHandler#validate(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void validate(String id, Object source, Object result) throws Exception
	{
		log.debug("validate id={} source={} result={}", id, source, result);
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
				log.info("getValidateSource(" + id + ") blob ==  null" );
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
	}
}
