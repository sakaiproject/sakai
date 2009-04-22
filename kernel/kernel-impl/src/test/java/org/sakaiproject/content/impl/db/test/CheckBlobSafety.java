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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.db.test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * @author ieb
 */
public class CheckBlobSafety extends TestCase
{

	private static final Log log = LogFactory.getLog(CheckBlobSafety.class);

	private SharedPoolDataSource tds;

	private String config;

	private Properties p = new Properties();

	private Connection con;

	/**
	 * @param name
	 */
	public CheckBlobSafety(String name)
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

		if (config != null)
		{
			log.info("Using Config " + config);
			File f = new File(config);
			FileInputStream fin = new FileInputStream(config);
			p.load(fin);
			fin.close();
			StringBuilder sb = new StringBuilder();
			Object[] keys = p.keySet().toArray();
			Arrays.sort(keys);
			for (Object k : keys)
			{
				sb.append("\n " + k + ":" + p.get(k));
			}
			log.info("Loaded Properties from " + config + " as " + sb.toString());
		}
		else
		{
			log.info("Using Default Config: testblob.config");
			InputStream is = this.getClass().getResourceAsStream("testblob.config");
			if (is != null) {
	            try {
                    p.load(is);
                    StringBuilder sb = new StringBuilder();
                    Object[] keys = p.keySet().toArray();
                    Arrays.sort(keys);
                    for (Object k : keys)
                    {
                        sb.append("\n " + k + ":" + p.get(k));
                    }
                    log.info("Loaded Default Properties " + config + " as " + sb.toString());
                } finally {
                    is.close();
                }
			}
		}

		cpds.setDriver(p.getProperty("dbDriver"));
		cpds.setUrl(p.getProperty("dbURL"));
		cpds.setUser(p.getProperty("dbUser"));
		cpds.setPassword(p.getProperty("dbPass"));

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
		tds.setMaxActive(10);
		tds.setMaxWait(5);
		tds.setDefaultAutoCommit(false);

		con = tds.getConnection();
		Statement stmt = null;
		try
		{
			stmt = con.createStatement();
			try {
				log.info("Executing " + p.getProperty("drop.statement"));
				stmt.execute(p.getProperty("drop.statement"));
			} catch ( Exception ex ) {
				log.info("Drop failed "+ex.getMessage());
			}
			log.info("Executing " + p.getProperty("create.statement"));
			stmt.execute(p.getProperty("create.statement"));
		}
		finally
		{
			try
			{
				stmt.close();
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
		super.tearDown();
		Statement stmt = null;
		try
		{
			stmt = con.createStatement();
			stmt.execute(p.getProperty("drop.statement"));
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				con.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	public void testBlob() 
	{
		try
		{
			Random r = new Random();
			int blockSize = 4095; // use an odd size to get byte boundaries
			int nblocks = 512;
			int maxSize = blockSize * nblocks;
			byte[] b = new byte[maxSize];
			byte[] bin = new byte[maxSize];
			log.info("Loading Random Data " + maxSize);
			r.nextBytes(b);
			log.info("Loaded Random Data");

			log.info("Got Connection");
			Statement stmt = null;
			PreparedStatement pstout = null;
			PreparedStatement pstin = null;
			InputStream instream = null;
			ResultSet rs = null;
			try
			{
				pstout = con.prepareStatement(p.getProperty("insert.statement"));
				pstin = con.prepareStatement(p.getProperty("select.statement"));
				for ( int i = 1; i < nblocks; i+=5)
				{
					int size = blockSize*i;
					pstout.clearParameters();

					pstout.setBinaryStream(1, new ByteArrayInputStream(b), size);
					pstout.setInt(2, i);
					pstout.executeUpdate();
					log.info("Loaded record  "+i+" of size " + (size) + " bytes");
					con.commit();
					i++;
				}
				for ( int i = 1; i < nblocks; i+=5)
				{
					int size = blockSize*i;
					pstin.clearParameters();
					pstin.setInt(1, i);
					rs = pstin.executeQuery();
					if (rs.next())
					{
						instream = rs.getBinaryStream(1);
						DataInputStream din = new DataInputStream(instream);
						din.readFully(bin, 0, size);
						for (int j = 0; j < size; j++)
						{
							assertEquals("Byte Missmatch record " + i + " offset " + j,
									b[j], bin[j]);
						}
						log.info("Checked Record "+i+" of size "+size+ " bytes");
						din.close();
						instream.close();
						rs.close();
						i++;
					}
					else
					{
						assertEquals("Didnt get any record at " + i, true, false);
					}
					con.commit();
				}
			}
			finally
			{
				try
				{
					instream.close();
				}
				catch (Exception ex)
				{
				}
				try
				{
					rs.close();
				}
				catch (Exception ex)
				{
				}
				try
				{
					stmt.close();
				}
				catch (Exception ex)
				{
				}
			}
		}
		catch (Exception ex)
		{
			log.error("Failed ", ex);
		}

	}

}
