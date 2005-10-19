/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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






