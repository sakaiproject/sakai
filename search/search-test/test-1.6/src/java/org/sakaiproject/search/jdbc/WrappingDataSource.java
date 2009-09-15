/**
 * 
 */
package org.sakaiproject.search.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

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
}