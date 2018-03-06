/**
 * Copyright (c) 2003-2013 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.search.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class WrappingDataSource implements DataSource {

	private DataSource wds;
	
	public WrappingDataSource(DataSource ds)
	{
		wds = ds;
	}
	
	public Connection getConnection() throws SQLException
	{
		return wds.getConnection();
	}

	public Connection getConnection(String username, String password)
			throws SQLException
	{
		return wds.getConnection(username, password);
	}

	public PrintWriter getLogWriter() throws SQLException
	{
		return wds.getLogWriter();
	}

	public int getLoginTimeout() throws SQLException
	{
		return wds.getLoginTimeout();
	}

	public void setLogWriter(PrintWriter out) throws SQLException
	{
		wds.setLogWriter(out);
	}

	public void setLoginTimeout(int seconds) throws SQLException
	{
		wds.setLoginTimeout(seconds);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return isWrapperFor(iface);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return unwrap(iface);
	}

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return wds.getParentLogger();
    }
}