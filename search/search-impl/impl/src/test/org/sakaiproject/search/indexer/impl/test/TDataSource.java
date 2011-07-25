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

package org.sakaiproject.search.indexer.impl.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.jdbc.WrappingConnection;
import org.sakaiproject.search.jdbc.WrappingDataSource;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;

/**
 * @author ieb
 */
public class TDataSource
{
	private static final Log log = LogFactory.getLog(TDataSource.class);

	private SharedPoolDataSource tds;

	private DataSource wds;

	protected int nopen = 0;

	private DriverAdapterCPDS cpds;

	public TDataSource(int poolSize, final boolean logging) throws Exception {
		super();
		
		cpds = new DriverAdapterCPDS();
		/*
		 * try { // can we test against mysql
		 * Class.forName("com.mysql.jdbc.Driver");
		 * cpds.setDriver("com.mysql.jdbc.Driver"); cpds
		 * .setUrl("jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&characterEncoding=UTF-8");
		 * cpds.setUser("sakai22"); cpds.setPassword("sakai22"); } catch
		 * (ClassNotFoundException cnfe) { }
		 */
		// we need to use derby db because HSQL only has read_uncommitted
		// transaction
		// isolation

		log.info("Using Derby DB");
		cpds.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
		cpds.setUrl("jdbc:derby:target/testdb;create=true");
		cpds.setUser("sa");
		cpds.setPassword("manager");

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
		tds.setMaxActive(poolSize);
		tds.setMaxWait(5);
		tds.setDefaultAutoCommit(false);

		wds = new WrappingDataSource(tds) {
			
			public Connection getConnection() throws SQLException {
				final Connection c = super.getConnection();
				nopen++;
				if (logging) log.info("+++++++++++Opened " + nopen);
				Exception ex = new Exception();
				StackTraceElement[] ste = ex.getStackTrace();
				log.debug("Stack Trace " + ste[1].toString());
				return new WrappingConnection(c) {

					public void close() throws SQLException
					{
						c.close();
						nopen--;
						if (logging) log.info("--------------Closed " + nopen);
					}
					
				};
			}
			
		};
		

		Connection connection = tds.getConnection();
		Statement s = connection.createStatement();
		try
		{
			s.execute("DROP TABLE search_transaction");
		}
		catch (Exception ex)
		{
			log.warn("Drop Table Said :" + ex.getMessage());
		}
		try
		{
			s
					.execute("create table search_transaction ( txname varchar(36), txid bigint )");
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}
		try
		{
			s.execute("DROP TABLE searchbuilderitem");
		}
		catch (Exception ex)
		{
			log.warn("Drop Table Said :" + ex.getMessage());
		}
		try
		{
			s.execute("CREATE TABLE searchbuilderitem ( \n"
					+ " id varchar(64) NOT NULL, \n" + " version timestamp NOT NULL, \n"
					+ " name varchar(255) NOT NULL, \n"
					+ " context varchar(255) NOT NULL, \n"
					+ " searchaction int default NULL, \n"
					+ " searchstate int default NULL, \n"
					+ " itemscope int default NULL, \n" + " PRIMARY KEY  (id), \n"
					+ " UNIQUE (name) \n" + ")");
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}
		try
		{
			s.execute("DROP TABLE search_journal");
		}
		catch (Exception ex)
		{
			log.warn("Drop Table Said :" + ex.getMessage());
		}
		try
		{
			s
					.execute("CREATE TABLE search_journal ( "
							+ " txid bigint NOT NULL, "
							+ " txts bigint NOT NULL, indexwriter varchar(255) NOT NULL, status varchar(36) NOT NULL, "
							+ " PRIMARY KEY  (txid) )");
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}

		try
		{
			s.execute("DROP TABLE search_node_status");
		}
		catch (Exception ex)
		{
			log.warn("Drop Table Said :" + ex.getMessage());
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

	public List<SearchBuilderItem> populateDocuments(long targetItems, String prefix)
			throws SQLException
	{
		int nitems = 0;

		Connection connection = null;
		PreparedStatement insertPST = null;
		List<SearchBuilderItem> items = new ArrayList<SearchBuilderItem>();
		try
		{
			connection = getDataSource().getConnection();
			insertPST = connection
					.prepareStatement("insert into searchbuilderitem "
							+ "(id,version,name,context,searchaction,searchstate,itemscope) values "
							+ "(?,?,?,?,?,?,?)");
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
				SearchBuilderItemImpl sbi = new SearchBuilderItemImpl();
				sbi.setContext("/" + name + prefix+"/at/a");
				sbi.setName("/" + name + prefix +"/at/a/location/" + i);
				sbi.setVersion(new Date(System.currentTimeMillis()));
				sbi.setId(String.valueOf(System.currentTimeMillis()) + String.valueOf(i));
				sbi.setItemscope(SearchBuilderItem.ITEM);
				sbi.setSearchaction(action);
				sbi.setSearchstate(state);

				insertPST.clearParameters();
				insertPST.setString(1, sbi.getId());
				insertPST.setTimestamp(2, new Timestamp(sbi.getVersion().getTime()));
				insertPST.setString(3, sbi.getName());
				insertPST.setString(4, sbi.getContext());
				insertPST.setInt(5, sbi.getSearchaction());
				insertPST.setInt(6, sbi.getSearchstate());
				insertPST.setInt(7, sbi.getItemscope());
				insertPST.execute();

				items.add(sbi);
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
			try
			{
				connection.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
		}
		return items;

	}

	/**
	 * @param items
	 * @param indexSearcher
	 * @return
	 */
	public int checkIndexContents(List<SearchBuilderItem> items,
			IndexSearcher indexSearcher)
	{
		int errors = 0;
		try
		{
			Map<String, SearchBuilderItem> finalState = new HashMap<String, SearchBuilderItem>();

			for (SearchBuilderItem sbi : items)
			{
				if (sbi.getSearchstate().equals(SearchBuilderItem.STATE_PENDING)) {
					finalState.put(sbi.getId(), sbi);
				}
			}
			for (SearchBuilderItem sbi :finalState.values() )
			{
				TermQuery tq = new TermQuery(new Term(SearchService.FIELD_REFERENCE, sbi
						.getName()));
				Hits h = indexSearcher.search(tq);
				if (sbi.getSearchaction().equals(SearchBuilderItem.ACTION_ADD))
				{
					log.info("====== ADD CHECKING =====");
					if (h.length() != 1)
					{
						log.error("Didnt find " + sbi.getName() + " got " + h.length());
						errors++;
					}
					else
					{
						Document doc = h.doc(0);
						String value = doc.get(SearchService.FIELD_REFERENCE);
						if (!sbi.getName().equals(value))
						{
							log.error("Ids Dont Match ");
							errors++;
						}
						else
						{
							log.debug("Ok " + sbi.getName());
						}
					}
				}
				else
				{
					log.info("====== DELETE CHECKING =====");
					if (h.length() != 0)
					{
						Document doc = h.doc(0);
						String value = doc.get(SearchService.FIELD_REFERENCE);
						log.error("Found " + sbi.getName() + " when should have not  "
								+ value + " "
								+ SearchBuilderItem.actions[sbi.getSearchaction()] + " "
								+ (sbi.isLocked()?"Locked to "+sbi.getLock():SearchBuilderItem.states[sbi.getSearchstate()]) + "");
						errors++;
					}
					else
					{
						log.debug("Ok " + sbi.getName());
					}

				}

			}
			return errors;
		}
		catch (Exception ex)
		{
			log.error("Searchng Exception ", ex);
			return -1;
		}
	}

	/**
	 * @param items
	 * @return
	 * @throws SQLException 
	 */
	public List<SearchBuilderItem> deleteSomeItems(List<SearchBuilderItem> items) throws SQLException
	{
		int nitems = 0;

		Connection connection = null;
		PreparedStatement updatePST = null;
		try
		{
			connection = getDataSource().getConnection();
			updatePST = connection
					.prepareStatement("update searchbuilderitem  set searchaction = ?, searchstate = ? where id = ?");
			int i = 0;
			for (SearchBuilderItem sbi : items )
			{
				if ( sbi.getSearchaction().equals(SearchBuilderItem.ACTION_ADD)) {
					i++;
					if ( i%3 == 0 ) {
						sbi.setSearchaction(SearchBuilderItem.ACTION_DELETE);
						sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
						updatePST.clearParameters();
						updatePST.setInt(1, sbi.getSearchaction());
						updatePST.setInt(2, sbi.getSearchstate());
						updatePST.setString(3, sbi.getId());
						if ( updatePST.executeUpdate() != 1) {
							throw new SQLException("Failed to update for delete "+sbi.getId());
						}
						log.info("Marked "+sbi.getName()+" for deletion ");
					}
				}
			}
			connection.commit();
		}
		finally
		{
			try
			{
				updatePST.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
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
		return items;
	}

}
