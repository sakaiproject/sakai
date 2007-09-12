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

package org.sakaiproject.content.impl.serialize.impl.conversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */
public class CheckConnection
{
	private static final Log log = LogFactory.getLog(CheckConnection.class);

	public void check(DataSource tds) throws Exception
	{
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

			testUTF8Transport(connection);

			try
			{
				statement.execute("drop table blobtest");
			}
			catch (Exception ex)
			{
			}

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

	public void testUTF8Transport(Connection connection) throws Exception
	{
		/*
		 * byte[] b = new byte[102400]; byte[] b2 = new byte[102400]; byte[] b3 =
		 * new byte[102400]; char[] cin = new char[102400]; Random r = new
		 * Random(); r.nextBytes(b);
		 */
		byte[] b = new byte[1024];
		byte[] b2 = new byte[1024];
		byte[] b3 = new byte[1024];
		char[] cin = new char[1024];

		{
			int i = 0;
			for (byte bx = Byte.MIN_VALUE; bx <= Byte.MAX_VALUE && i < b.length; bx++)
			{
				b[i++] = bx;
			}
		}
		for (int i = 0; i < b.length; i++)
		{
			cin[i] = (char) (b[i]);
			log.info("Byte "+b[i]+":"+(int)cin[i]);
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

				throw new Exception("Internal Check Failed, byte char conversion failed");
			}
		}

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
				log.info("Internal  Char String Byte First test at " + i
						+ " does not match " + (int) b[i] + ":" + b[i] + " != "
						+ (int) b2[i] + ":" + b2[i]);

				throw new Exception("Internal Check Failed, byte char conversion ");
			}
		}

		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rs = null;
		try
		{
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
				throw new Exception("Truncation Error on transport");
			}
			for (int i = 0; i < b.length; i++)
			{
				b3[i] = (byte) (cout[i]);
				// log.info("Byte at "+i+" is "+b3[i]+" char "+(int)cout[i]);
			}
			for (int i = 0; i < cin.length; i++)
			{
				if (b[i] != b3[i])
				{
					log.info("External Byte at " + i + " does not match " + ":" + b[i]
							+ " != " + b3[i]);
					throw new Exception(
							"UTF-8 Transport and/or database is NOT Ok for UTF8 usage ");
				}
			}

			log.info("DB Connection passes UTF-8 tests");

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
		}

	}

}
