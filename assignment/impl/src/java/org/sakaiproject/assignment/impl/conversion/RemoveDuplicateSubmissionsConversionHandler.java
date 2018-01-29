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

import org.sakaiproject.util.conversion.SchemaConversionHandler;

/**
 * 
 * 
 *
 */
public class RemoveDuplicateSubmissionsConversionHandler implements
		SchemaConversionHandler 
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
	
	public boolean convertSource(String id, Object source, PreparedStatement updateRecord) throws SQLException 
	{
		updateRecord.setString(1, id);
		// TODO Auto-generated method stub
		return true;
	}

	public Object getSource(String id, ResultSet rs) throws SQLException 
	{
		return rs.next()?rs.getString(1):null;
	}

	public Object getValidateSource(String id, ResultSet rs) throws SQLException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void validate(String id, Object source, Object result) throws Exception 
	{
		// TODO Auto-generated method stub

	}

}
