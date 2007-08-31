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

package org.sakaiproject.search.indexer.impl.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 *
 */
public class TestDataSource
{
	private static final Log log = LogFactory.getLog(TestDataSource.class);
	private SharedPoolDataSource tds;

	public TestDataSource() throws Exception{
		DriverAdapterCPDS cpds = new DriverAdapterCPDS();
		cpds.setDriver("org.hsqldb.jdbcDriver");
		cpds.setUrl("jdbc:hsqldb:mem:aname");
		cpds.setUser("sa");
		cpds.setPassword("");

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
		tds.setMaxActive(30);
		tds.setMaxWait(50);

		Connection connection = tds.getConnection();
		Statement s = connection.createStatement();
		try
		{
			s.execute("DROP TABLE search_transaction" );
		}
		catch (Exception ex)
		{
		}
		try
		{
			s.execute("create table search_transaction ( txname varchar, txid bigint )");
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}
		try
		{
			s.execute("DROP TABLE searchbuilderitem" );
		}
		catch (Exception ex)
		{
		}
		try
		{
			s.execute("CREATE TABLE searchbuilderitem ( id varchar(64) NOT NULL, " +
					" version datetime NOT NULL, " +
					" name varchar(255) NOT NULL, " +
					" context varchar(255) NOT NULL, " +
					" searchaction int default NULL, " +
					" searchstate int default NULL, " +
					" itemscope int default NULL, " +
					" PRIMARY KEY  (id), " +
					" UNIQUE (name) )" );
		}
		catch (Exception ex)
		{
			log.warn("Create Table Said :" + ex.getMessage());
		}

		ResultSet rs = s.executeQuery("select txname, txid from search_transaction");
		log.info("Record ++++++++++");
		while (rs.next())
		{
			log.info("Record " + rs.getString(1) + ":" + rs.getLong(2));
		}
		log.info("Record ----------");
		rs.close();
		connection.commit();
		connection.close();
	}

	/**
	 * @return
	 */
	public DataSource getDataSource()
	{
		return tds;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public void close() throws Exception
	{
		tds.close();
	}
}
