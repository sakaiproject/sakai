/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/deli\
very/UpdateTimerListener.java $
* $Id: UpdateTimerListener.java 13802 2006-08-16 23:13:50Z ktsao@stanford.edu $
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
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
package org.sakaiproject.spring;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;


/**
 * Wraps an existing DataSource for the sole purpose of setting
 * autoCommit=true when the connection is gotten.  This allows
 * the same underlying DataSource to serve for tools that need
 * autoCommit=false, as well as tools that need autoCommit=true.
 */
public class DataSourceWrapperAutoCommit implements DataSource
{
	protected DataSource m_wrapped = null;

	public DataSourceWrapperAutoCommit()
	{
	}

	public void setDataSource(DataSource d)
	{
		m_wrapped = d;
	}

	public DataSource getDataSource()
	{
		return m_wrapped;
	}

	public Connection getConnection() throws SQLException
	{
		Connection c = m_wrapped.getConnection();
		if (c != null) c.setAutoCommit(true);
		return c;
	}

	public Connection getConnection(String username, String password) throws SQLException
	{
		Connection c = m_wrapped.getConnection(username, password);
		if (c != null) c.setAutoCommit(true);
		return c;
	}

	public int getLoginTimeout() throws SQLException
	{
		return m_wrapped.getLoginTimeout();
	}

	public PrintWriter getLogWriter() throws SQLException
	{
		return m_wrapped.getLogWriter();
	}

	public void setLoginTimeout(int timeout) throws SQLException
	{
		m_wrapped.setLoginTimeout(timeout);
	}

	public void setLogWriter(PrintWriter writer) throws SQLException
	{
		m_wrapped.setLogWriter(writer);
	}
}






