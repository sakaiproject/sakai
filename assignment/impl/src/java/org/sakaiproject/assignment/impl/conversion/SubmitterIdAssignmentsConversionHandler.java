/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.assignment.impl.conversion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.util.conversion.SchemaConversionHandler;

/**
 * Performs just the file size conversion for quota calculations
 * 
 * @author ieb
 */
@Slf4j
public class SubmitterIdAssignmentsConversionHandler implements SchemaConversionHandler
{
	// db driver
	private String m_dbDriver = null;
	/**
	 * {@inheritDoc}
	 */
	public String getDbDriver()
	{
		return m_dbDriver;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setDbDriver(String dbDriver)
	{
		m_dbDriver = dbDriver;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.SchemaConversionHandler#getSource(java.lang.String,
	 *      java.sql.ResultSet)
	 */
	public Object getSource(String id, ResultSet rs) throws SQLException
	{
		return rs.next()?rs.getString(1):null;
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

		AssignmentSubmissionAccess sax = new AssignmentSubmissionAccess();
		try
		{
			sax.parse(xml);
		}
		catch (Exception e1)
		{
			log.warn("{}:convertSource Failed to parse {}[{}]{}", this, id, xml, e1);
			return false;
		}

		try
		{
			updateRecord.setString(1, sax.getSubmitterId());
			
			String dateSubmitted = sax.getDatesubmitted();
			if(dateSubmitted == null || dateSubmitted.trim().equals(""))
			{
				updateRecord.setString(2, null);
			}
			else
			{
				updateRecord.setString(2, dateSubmitted);
			}
			updateRecord.setString(3, sax.getSubmitted());
			updateRecord.setString(4, sax.getGraded());
			updateRecord.setString(5, id);
			return true;
		}
		catch (Exception e)
		{
			log.warn("{}:convertSource Failed to process record {} {}", this, id, e);
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.impl.conversion.SchemaConversionHandler#validate(java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void validate(String id, Object source, Object result) throws Exception
	{
		// this conversion did not modify source data.
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.impl.conversion.SchemaConversionHandler#getValidateSource(java.lang.String, java.sql.ResultSet)
	 */
	public Object getValidateSource(String id, ResultSet rs) throws SQLException
	{
		return rs.getString(1);
	}


}
