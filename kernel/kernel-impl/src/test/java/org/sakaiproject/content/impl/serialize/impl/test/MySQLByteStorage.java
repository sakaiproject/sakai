/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.serialize.impl.test;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.util.ByteStorageConversion;

@Slf4j
public class MySQLByteStorage
{
	private SharedPoolDataSource tds;

	@Before
	public void setUp() throws Exception
	{
		DriverAdapterCPDS cpds = new DriverAdapterCPDS();

		String config = System.getProperty("migrate.config"); // ,"migrate.properties");
		Properties p = new Properties();
		if (config != null)
		{
			log.info("Using Config " + config);
			FileInputStream fin = new FileInputStream(config);
			p.load(fin);
			fin.close();
			for (Entry<Object, Object> entry : p.entrySet())
			{
				Object k = entry.getKey();
				log.info("   Test Properties " + k + ":" + entry.getValue());
			}
		}

		cpds.setDriver(p.getProperty("dbDriver", "com.mysql.jdbc.Driver"));
		cpds.setUrl(p.getProperty("dbURL", "jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&characterEncoding=UTF-8"));
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

	@After
	protected void tearDown() throws Exception
	{
		tds.close();
	}

	@Test
	public void testBlobData() throws SQLException
	{
		// run the test 10 times to make really certain there is no problem
		for (int k = 0; k < 10; k++)
		{
			byte[] bin = new byte[102400];
			char[] cin = new char[102400];

			byte[] bout = new byte[102400];
			Random r = new Random();
			r.nextBytes(bin);

			ByteStorageConversion.toChar(bin, 0, cin, 0, cin.length);
			String sin = new String(cin);

			char[] cout = sin.toCharArray();
			ByteStorageConversion.toByte(cout, 0, bout, 0, cout.length);

			for (int i = 0; i < bin.length; i++)
			{
				Assert.assertEquals("Internal Byte conversion failed at " + bin[i] + "=>"
						+ (int) cin[i] + "=>" + bout[i], bin[i], bout[i]);
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
				statement.setInt(1, k);
				statement.setString(2, sin);
				statement.executeUpdate();

				statement2 = connection
						.prepareStatement("select bval from blobtest where id =  ? ");
				statement2.clearParameters();
				statement2.setInt(1, k);
				rs = statement2.executeQuery();
				String sout = null;
				if (rs.next())
				{
					sout = rs.getString(1);
				}

				// ensure no NPE, but maybe this is not ok because cout current value may be invalid
				if (sout != null) {
                    cout = sout.toCharArray();
                }
                ByteStorageConversion.toByte(cout, 0, bout, 0, cout.length);

                if (sout != null) {
                    Assert.assertEquals("Input and Output Lenghts are not the same ", sin.length(),
                            sout.length());
                }

				for (int i = 0; i < bin.length; i++)
				{
					Assert.assertEquals("Database Byte conversion failed at " + bin[i] + "=>"
							+ (int) cin[i] + "=>" + bout[i], bin[i], bout[i]);
				}
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
}
