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

package org.sakaiproject.content.impl.serialize.impl.test;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */
public class MySQLByteStorage extends TestCase
{

	private static final Log log = LogFactory.getLog(MySQLByteStorage.class);

	private SharedPoolDataSource tds;

	/**
	 * @param name
	 */
	public MySQLByteStorage(String name)
	{
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{

		super.setUp();
		DriverAdapterCPDS cpds = new DriverAdapterCPDS();

		String config = System.getProperty("migrate.config"); // ,"migrate.properties");
		Properties p = new Properties();
		if (config != null)
		{
			log.info("Using Config " + config);
			File f = new File(config);
			FileInputStream fin = new FileInputStream(config);
			p.load(fin);
			fin.close();
			for (Iterator<Object> i = p.keySet().iterator(); i.hasNext();)
			{
				Object k = i.next();
				log.info("   Test Properties " + k + ":" + p.get(k));
			}
		}

		cpds.setDriver(p.getProperty("dbDriver", "com.mysql.jdbc.Driver"));
		cpds
				.setUrl(p
						.getProperty("dbURL",
								"jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&characterEncoding=UTF-8"));
		cpds.setUser(p.getProperty("dbUser", "sakai22"));
		cpds.setPassword(p.getProperty("dbPass", "sakai22"));

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
		tds.setMaxActive(10);
		tds.setMaxWait(5);
		tds.setDefaultAutoCommit(false);

		Connection connection = null;
		Statement statement = null;
		try
		{
			connection = tds.getConnection();
			statement = connection.createStatement();
			try
			{
				statement.execute("drop table blobtest");
			}
			catch (Exception ex)
			{
			}
			statement
					.execute("create table blobtest ( id int, bval longtext, primary key(id) )");
		}
		finally
		{
			try
			{
				statement.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		tds.close();
		super.tearDown();

	}

	public void testBlobData() throws SQLException
	{
		byte[] b = new byte[102400];
		byte[] b2 = new byte[102400];
		byte[] b3 = new byte[102400];
		char[] cin = new char[102400];
		Random r = new Random();
		r.nextBytes(b);
		
		for (int i = 0; i < b.length; i++)
		{
			cin[i] = (char) (b[i]);
		}
		for (int i = 0; i < b.length; i++)
		{
			b2[i] = (byte) (cin[i]);
		}
		for (int i = 0; i < cin.length; i++)
		{
			if (b[i] != b2[i])
			{
				log.info("Internal Byte First test at " + i + " does not match "
						+ (int) b[i] + ":" + b[i] + " != " + (int) b2[i] + ":" + b2[i]);

				fail("Did not transfer Ok internally");
			}
		}
		log.info("Internal trasfer Ok");

		String bin = new String(cin);
		String bout = null;
		
		char[] cin2 = bin.toCharArray();
		for (int i = 0; i < b.length; i++)
		{
			b2[i] = (byte) (cin2[i]);
		}
		for (int i = 0; i < cin2.length; i++)
		{
			if (b[i] != b2[i])
			{
				log.info("Internal  Char String Byte First test at " + i + " does not match "
						+ (int) b[i] + ":" + b[i] + " != " + (int) b2[i] + ":" + b2[i]);

				fail("Did not transfer Ok internally with char");
			}
		}


		Connection connection = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rs = null;
		try
		{
			connection = tds.getConnection();
			statement = connection
					.prepareStatement("insert into blobtest ( id, bval ) values ( ?, ? )");
			statement.clearParameters();
			statement.setInt(1, 1);
			statement.setString(2, bin);
			statement.executeUpdate();

			statement2 = connection
					.prepareStatement("select bval from blobtest where id =  ? ");
			statement2.clearParameters();
			statement2.setInt(1, 1);
			rs = statement2.executeQuery();
			if (rs.next())
			{
				bout = rs.getString(1);
			}
			char[] cout = bout.toCharArray();
			if (cout.length != cin.length)
			{
				log.info("Dropped " + (cin.length - cout.length));
			}
			for (int i = 0; i < b.length; i++)
			{
				b3[i] = (byte) (cout[i]);
		//		log.info("Byte at "+i+" is "+b3[i]+"  char "+(int)cout[i]);
			}
			for (int i = 0; i < cin.length; i++)
			{
				if (b[i] != b3[i])
				{
					log.info("External Byte at " + i + " does not match " + ":" + b[i] + " != " + b3[i]);
					fail("Did not transfer Ok internally ");
				}
			}

			for (int i = 0; i < cin.length; i++)
			{
				if (cin[i] != cout[i])
				{
					log.info("External Char at " + i + " does not match " + (int) cin[i]
							+ ":" + cin[i] + " != " + (int) cout[i] + ":" + cout[i]);
					log.info("Bytes:" + b[i]);

					fail("DId not serialize Ok");
				}
			}

			assertEquals(bin, bout);

		}
		finally
		{
			try
			{
				rs.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				statement2.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				statement.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{

			}
		}

	}
}
