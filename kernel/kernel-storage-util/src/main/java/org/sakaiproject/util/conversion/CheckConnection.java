/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/conversion/CheckConnection.java $
 * $Id: CheckConnection.java 101634 2011-12-12 16:44:33Z aaronz@vt.edu $
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

package org.sakaiproject.util.conversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.util.ByteStorageConversion;

/**
 * @author ieb
 * @deprecated unused as of 12 Dec 2011, planned for removal after 2.9
 */
@Slf4j
public class CheckConnection
{
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
		byte[] bin = new byte[102400];
		char[] cin = new char[102400];
		byte[] bout = new byte[102400];

		{
			int i = 0;
			for (int bx = 0; i < bin.length; bx++)
			{
				bin[i++] = (byte) bx;
			}
		}
		ByteStorageConversion.toChar(bin, 0, cin, 0, cin.length);
		String sin = new String(cin);

		char[] cout = sin.toCharArray();
		ByteStorageConversion.toByte(cout, 0, bout, 0, cout.length);

		for (int i = 0; i < bin.length; i++)
		{
			if (bin[i] != bout[i])
			{
				throw new Exception("Internal Byte conversion failed at " + bin[i] + "=>"
						+ (int) cin[i] + "=>" + bout[i]);
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
			statement.setInt(1, 20);
			statement.setString(2, sin);
			statement.executeUpdate();

			statement2 = connection
					.prepareStatement("select bval from blobtest where id =  ? ");
			statement2.clearParameters();
			statement2.setInt(1, 20);
			rs = statement2.executeQuery();
			String sout = null;
			if (rs.next())
			{
				sout = rs.getString(1);
			}

			if(sout == null)
				throw new IllegalStateException("String sout == null!");
			cout = sout.toCharArray();
			ByteStorageConversion.toByte(cout, 0, bout, 0, cout.length);

			if (sin.length() != sout.length())
			{
				throw new Exception(
						"UTF-8 Data was lost communicating with the database, please "
								+ "check connection string and default table types (Truncation/Expansion)");
			}

			for (int i = 0; i < bin.length; i++)
			{
				if (bin[i] != bout[i])
				{
					throw new Exception(
							"UTF-8 Data was corrupted communicating with the database, "
									+ "please check connectionstring and default table types (Conversion)"
									+ "" + bin[i] + "=>" + (int) cin[i] + "=>" + bout[i]);
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
