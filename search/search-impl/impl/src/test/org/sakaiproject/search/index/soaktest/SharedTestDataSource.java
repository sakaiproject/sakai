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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.index.soaktest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.jdbc.WrappingConnection;
import org.sakaiproject.search.jdbc.WrappingDataSource;
import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * @author ieb
 */
public class SharedTestDataSource
{
	/**
	 * @author ieb
	 *
	 */
	public class ConnectionMeta 
	{

		private StackTraceElement location;
		private long created;
		private Connection connection;

		/**
		 * @param string
		 */
		public ConnectionMeta(Connection c, StackTraceElement location)
		{
			created = System.currentTimeMillis();
			this.location = location;
			this.connection = c;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "["+connection+"] Created By "+location.toString()+" age "+(System.currentTimeMillis()-created);
		}
		/**
		 * @return
		 */
		public boolean isOld()
		{
			return (System.currentTimeMillis()-created) > 10000;
		}

	}

	private static final Log log = LogFactory.getLog(SharedTestDataSource.class);

	private SharedPoolDataSource tds;

	private DataSource wds;

	protected int nopen = 0;

	private int docid = 0;

	private int ldoc = 0;
	
	private Map<Connection, ConnectionMeta> connections = new ConcurrentHashMap<Connection, ConnectionMeta>();


	public SharedTestDataSource(String dblocation, int pool, final boolean poolLogging,
			String driver, String dburl, String user, String password) throws Exception
	{
		log.info("Using Derby DB");
		DriverAdapterCPDS cpds = new DriverAdapterCPDS();
		cpds.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
		cpds.setUrl("jdbc:derby:" + dblocation + ";create=true");
		cpds.setUser("sa");
		cpds.setPassword("manager");

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
		tds.setMaxActive(pool);
		tds.setMaxWait(5);
		tds.setDefaultAutoCommit(false);

		wds = new WrappingDataSource(tds)
		{
			public Connection getConnection() throws SQLException
			{
				final Connection c = tds.getConnection();
				nopen++;
				Exception ex = new Exception();
				StackTraceElement[] ste = ex.getStackTrace();
				log.debug("Stack Trace " + ste[1].toString());
				if (poolLogging) log.info("++++++++++++Opened " + nopen + " from " + ste[1].toString());
				connections.put(c,new ConnectionMeta(c,ste[1]));
				StringBuilder sb = new StringBuilder();
				for( ConnectionMeta cm : connections.values() ) {
					if ( cm.isOld() ) {
						sb.append("\n    ").append(cm);
					}
				}
				if ( sb.length() > 0 ) {
					log.warn("Older Connections found, possible connection leak "+sb.toString()+"\n");
				}
				return new WrappingConnection(c)
				{

					public void close() throws SQLException
					{
						connections.remove(c);
					    c.close();
						nopen--;
						Exception ex = new Exception();
						StackTraceElement[] ste = ex.getStackTrace();
						log.debug("Stack Trace " + ste[1].toString());
						if (poolLogging) log.info("------------Closed " + nopen + " from " + ste[1].toString());
	
					}

				};
			}

		};

		Connection connection = tds.getConnection();
		Statement s = connection.createStatement();
		try
		{
			s
					.execute("create table search_transaction ( txname varchar(64), txid bigint )");
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}
		try
		{
			s.execute("CREATE TABLE searchbuilderitem ( id varchar(64) NOT NULL, "
					+ " version timestamp NOT NULL, " + " name varchar(255) NOT NULL, "
					+ " context varchar(255) NOT NULL, "
					+ " searchaction int default NULL, "
					+ " searchstate int default NULL, " + " itemscope int default NULL, "
					+ " PRIMARY KEY  (id), " + " UNIQUE (name) )");
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}
		try
		{
			s.execute("CREATE TABLE search_journal ( " + " txid bigint NOT NULL, "
					+ " txts bigint NOT NULL, " + " indexwriter varchar(255) NOT NULL, status varchar(32) NOT NULL,  "
					+ " PRIMARY KEY  (txid) )");
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}

		try
		{
			s.execute("CREATE TABLE search_node_status ( " + " jid bigint NOT NULL, "
					+ " jidts bigint NOT NULL, " + " serverid varchar(255) NOT NULL, "
					+ " PRIMARY KEY  (serverid) )");
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}
		s.close();
		connection.commit();
		connection.close();
	}

	/**
	 * @return
	 */
	public DataSource getDataSource()
	{
		return wds;

	}

	/**
	 * @throws Exception
	 */
	public void close() throws Exception
	{
		tds.close();
	}

	public int populateDocuments(long targetItems, String instanceName)
			throws SQLException
	{
		int nitems = 0;
		Connection connection = null;
		PreparedStatement insertPST = null;
		PreparedStatement deletePST = null;
		try
		{
			connection = getDataSource().getConnection();
			insertPST = connection
					.prepareStatement("insert into searchbuilderitem "
							+ "(id,version,name,context,searchaction,searchstate,itemscope) values "
							+ "(?,?,?,?,?,?,?)");
			deletePST = connection
			.prepareStatement("delete from searchbuilderitem where name = ? ");
			for (int i = 0; i < targetItems; i++)
			{
				int state = i % SearchBuilderItem.states.length;
				String name = SearchBuilderItem.states[state];
				int action = i % 3;
				if (state == SearchBuilderItem.STATE_PENDING
						&& action == SearchBuilderItem.ACTION_ADD)
				{
					nitems++;
				}
				String reference = "/" + instanceName + "/" + name
				+ "/at/a/location/" + (ldoc++);
				deletePST.clearParameters();
				deletePST.setString(1,reference);
				deletePST.executeUpdate();
				
				insertPST.clearParameters();
				insertPST.setString(1, String.valueOf(instanceName
						+ System.currentTimeMillis())
						+ String.valueOf(i) + ":" + String.valueOf(docid++));
				insertPST.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				insertPST.setString(3, reference);
				insertPST.setString(4, "/" + name + "/at/a");
				insertPST.setInt(5, action);
				insertPST.setInt(6, state);
				insertPST.setInt(7, SearchBuilderItem.ITEM);
				insertPST.execute();
			}
			connection.commit();
		}
		finally
		{
			try
			{
				insertPST.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
			try {
				deletePST.close();
			} 
			catch (Exception e) {
				log.debug(e);
			}
			try
			{
				connection.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
		}
		return nitems;

	}

	/**
	 * @param i
	 */
	public void resetAfter(int i)
	{
		if ( ldoc > i ) {
			ldoc = 0;
		}
		
	}

}
