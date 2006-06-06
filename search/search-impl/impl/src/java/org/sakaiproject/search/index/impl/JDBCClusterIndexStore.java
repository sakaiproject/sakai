/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.search.index.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.search.index.ClusterFilesystem;

/**
 * This is a JDBC implementation of the ClusterFilesystem. It syncronizes the
 * local search index segments with the database, by sipping each segment and
 * pushing it to the database. Each Segment has an extra file that contains an
 * MD5 of the segment and a time stamp of the last update. If any segments are
 * missing in the local segment store (including no segments at all, as with a
 * new cluster node) the missing segments are downloaded from the JDBC store. If
 * any of the segments on the local store are found to be dammaged they are
 * reloaded from the database.
 * 
 * @author ieb
 */
public class JDBCClusterIndexStore implements ClusterFilesystem
{

	private static final Log log = LogFactory
			.getLog(JDBCClusterIndexStore.class);

	private DataSource dataSource = null;

	private String searchIndexDirectory = null;

	private static final String TIMESTAMP_FILE = "_sakaicluster";

	private static final String PACKFILE = "packet";

	private static final String TEMP_INDEX_NAME = "tempindex";

	private boolean autoDdl = false;

	/**
	 * If validate is true, all segments will be checked on initial startup and
	 * upload. This can take a long time. If its false, only when an index is
	 * updated is the MD5 checked. Recomendation is to leave this false.
	 */
	private boolean validate = false;

	private static Hashtable checked = new Hashtable();

	/**
	 * This will be set to after the first update of a JVM run has been
	 * completed, as its possible that IndexReaders may have open references to
	 * the Segments that we try and remove.
	 */
	private static boolean okToRemove = true;

	public void init()
	{
		log.info(this + ":init() ");
		/*
		 The storage is created by hibernate now
		try
		{
			if (autoDdl)
			{
				SqlService.getInstance().ddl(this.getClass().getClassLoader(),
						"search_cluster");
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to init JDBCClusterIndexStorage", ex);
		}
		*/
		log.info(this + ":init() Ok ");

	}

	/**
	 * update the local Segmetns from the DB
	 */
	public List updateSegments()
	{
		Connection connection = null;
		List segmentList = new ArrayList();
		try
		{
			connection = dataSource.getConnection();
			List dbSegments = getDBSegments(connection);
			log.debug("Update: DB Segments = " + dbSegments.size());
			// remove files not in the dbSegmentList
			List localSegments = getLocalSegments();
			log.debug("Update: Local Segments = " + localSegments.size());

			// which of the dbSegments are not present locally

			List updateLocalSegments = new ArrayList();
			for (Iterator i = dbSegments.iterator(); i.hasNext();)
			{
				SegmentInfo db_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = db_si.getName();
				for (Iterator j = localSegments.iterator(); j.hasNext();)
				{
					SegmentInfo local_si = (SegmentInfo) j.next();
					if (name.equals(local_si.getName()))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					updateLocalSegments.add(db_si);
				}
			}

			// which of the dbsegmetnts are newer than local versions
			for (Iterator i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo current_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = current_si.getName();
				long version = current_si.getVersion();
				for (Iterator j = dbSegments.iterator(); j.hasNext();)
				{
					SegmentInfo db_si = (SegmentInfo) j.next();
					if (name.equals(db_si.getName())
							&& db_si.getVersion() > version)
					{
						updateLocalSegments.add(db_si);
						break;
					}
				}
			}

			// which if the currentSegments need updating
			// process the remove list
			// we can only perform a remove, IF there is no other activity.
			// ie only on the first time in any 1 JVM run
			if (okToRemove)
			{
				okToRemove = false;
				List removeLocalSegments = new ArrayList();

				// which segments exist locally but not in the DB, these should
				// be
				// removed
				for (Iterator i = localSegments.iterator(); i.hasNext();)
				{
					SegmentInfo local_si = (SegmentInfo) i.next();
					boolean found = false;
					String name = local_si.getName();
					for (Iterator j = dbSegments.iterator(); j.hasNext();)
					{
						SegmentInfo db_si = (SegmentInfo) j.next();
						if (name.equals(db_si.getName()))
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						removeLocalSegments.add(local_si);
					}
				}

				for (Iterator i = removeLocalSegments.iterator(); i.hasNext();)
				{
					SegmentInfo rmsi = (SegmentInfo) i.next();
					removeLocalSegment(rmsi);
				}
			}

			// process the get list
			for (Iterator i = updateLocalSegments.iterator(); i.hasNext();)
			{
				SegmentInfo addsi = (SegmentInfo) i.next();
				updateLocalSegment(connection, addsi);
			}

			// build the list putting the current segment at the end
			for (Iterator i = dbSegments.iterator(); i.hasNext();)
			{
				SegmentInfo si = (SegmentInfo) i.next();
				File f = new File(searchIndexDirectory, si.getName());
				segmentList.add(f.getPath());
			}

			connection.commit();
		}
		catch (Exception sqle)
		{
			log.error("Failed to update segments ", sqle);
			try
			{
				connection.rollback();
			}
			catch (Exception ex)
			{
			}
		}
		finally
		{
			try
			{
				connection.close();
			}
			catch (Exception e)
			{
			}
		}
		return segmentList;
	}

	public long getTotalSize(File currentSegment)
	{
		long totalSize = 0;
		if (currentSegment.isDirectory())
		{
			File[] files = currentSegment.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].isDirectory())
					{
						totalSize += getTotalSize(files[i]);
					}
					totalSize += files[i].length();
				}
			}
		}
		else
		{
			totalSize += currentSegment.length();
		}
		return totalSize;
	}

	/**
	 * save the local segments to the DB
	 */
	public List saveSegments()
	{
		Connection connection = null;
		List segmentList = new ArrayList();
		try
		{
			connection = dataSource.getConnection();
			List dbSegments = getDBSegments(connection);
			// remove files not in the dbSegmentList
			List localSegments = getLocalSegments();

			// find the dbSegments that are not present locally

			List removeDBSegments = new ArrayList();
			List currentDBSegments = new ArrayList();

			// which segments exist inthe db but not locally
			for (Iterator i = dbSegments.iterator(); i.hasNext();)
			{
				SegmentInfo db_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = db_si.getName();
				for (Iterator j = localSegments.iterator(); j.hasNext();)
				{
					SegmentInfo local_si = (SegmentInfo) j.next();
					if (name.equals(local_si.getName()))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					removeDBSegments.add(db_si);
				}
				else
				{
					currentDBSegments.add(db_si);
				}
			}

			List updateDBSegments = new ArrayList();
			// which of the localSegments are not in the db

			for (Iterator i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = local_si.getName();
				for (Iterator j = dbSegments.iterator(); j.hasNext();)
				{
					SegmentInfo db_si = (SegmentInfo) j.next();
					if (name.equals(db_si.getName()))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					updateDBSegments.add(local_si);
				}
			}

			// which of the localSegments have been modified
			for (Iterator i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = local_si.getName();
				long version = local_si.getVersion();
				for (Iterator j = dbSegments.iterator(); j.hasNext();)
				{
					SegmentInfo db_si = (SegmentInfo) j.next();
					if (name.equals(db_si.getName())
							&& version > db_si.getVersion())
					{
						updateDBSegments.add(db_si);
						break;
					}
				}
			}

			// process the remove list
			for (Iterator i = removeDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo rmsi = (SegmentInfo) i.next();
				removeDBSegment(connection, rmsi);
			}
			// process the get list
			for (Iterator i = updateDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo addsi = (SegmentInfo) i.next();
				updateDBSegment(connection, addsi);
			}
			// build the list putting the current segment at the end

			for (Iterator i = updateDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo si = (SegmentInfo) i.next();
				File f = new File(searchIndexDirectory, si.getName());
				segmentList.add(f.getPath());
			}
			connection.commit();
		}
		catch (Exception ex)
		{
			log.error("Failed to Save Segments back to Central Storage", ex);
			try
			{
				connection.rollback();
			}
			catch (Exception e)
			{
			}
			recoverFromFailure();
		}
		finally
		{
			try
			{
				connection.close();
			}
			catch (Exception e)
			{
			}
		}
		return segmentList;
	}

	public List saveAllSegments()
	{
		Connection connection = null;
		List segmentList = new ArrayList();
		try
		{
			connection = dataSource.getConnection();
			List dbSegments = getDBSegments(connection);
			// remove files not in the dbSegmentList
			List localSegments = getLocalSegments();

			// find the dbSegments that are not present locally

			List updateDBSegments = new ArrayList();
			// which of the localSegments are not in the db

			for (Iterator i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = local_si.getName();
				for (Iterator j = dbSegments.iterator(); j.hasNext();)
				{
					SegmentInfo db_si = (SegmentInfo) j.next();
					if (name.equals(db_si.getName()))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					updateDBSegments.add(local_si);
				}
			}

			// teh db segments
			for (Iterator i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = local_si.getName();
				long version = local_si.getVersion();
				for (Iterator j = dbSegments.iterator(); j.hasNext();)
				{
					SegmentInfo db_si = (SegmentInfo) j.next();
					if (name.equals(db_si.getName()))
					{
						updateDBSegments.add(db_si);
						break;
					}
				}
			}

			// process the get list
			for (Iterator i = updateDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo addsi = (SegmentInfo) i.next();
				updateDBSegment(connection, addsi);
			}
			// build the list putting the current segment at the end

			for (Iterator i = updateDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo si = (SegmentInfo) i.next();
				File f = new File(searchIndexDirectory, si.getName());
				segmentList.add(f.getPath());
			}
			connection.commit();
		}
		catch (Exception ex)
		{
			log.error("Failed to Save Segments back to Central Storage", ex);
			try
			{
				connection.rollback();
			}
			catch (Exception e)
			{
			}
			recoverFromFailure();
		}
		finally
		{
			try
			{
				connection.close();
			}
			catch (Exception e)
			{
			}
		}
		return segmentList;
	}

	/**
	 * updte a segment from the database
	 * 
	 * @param connection
	 * @param addsi
	 */
	private void updateLocalSegment(Connection connection, SegmentInfo addsi)
			throws SQLException, IOException
	{
		log.debug("Updating local segment from databse " + addsi);
		PreparedStatement segmentSelect = null;
		ResultSet resultSet = null;
		try
		{
			segmentSelect = connection
					.prepareStatement("select version_, packet_ from search_segments where name_ = ?");
			segmentSelect.clearParameters();
			segmentSelect.setString(1, addsi.getName());
			resultSet = segmentSelect.executeQuery();
			if (resultSet.next())
			{
				InputStream packetStream = null;
				try
				{
					long version = resultSet.getLong(1);
					packetStream = resultSet.getBinaryStream(2);
					unpackSegment(addsi, packetStream, version);
					log.debug("Updated Packet from DB to versiob " + version);
				}
				finally
				{
					try
					{
						packetStream.close();
					}
					catch (Exception ex)
					{
					}
				}
			}
			else
			{
				log.error("Didnt find segment in database");
			}
		}
		finally
		{
			try
			{
				resultSet.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				segmentSelect.close();
			}
			catch (Exception ex)
			{
			}
		}

	}

	/**
	 * remove a local segment
	 * 
	 * @param rmsi
	 */
	private void removeLocalSegment(SegmentInfo rmsi)
	{
		File f = new File(searchIndexDirectory, rmsi.getName());
		deleteAll(f);
		log.debug("LO Removed " + rmsi);
	}

	/**
	 * get a list of all DB segments ordered by version
	 * 
	 * @param connection
	 * @return
	 */
	private List getDBSegments(Connection connection) throws SQLException
	{
		PreparedStatement segmentAllSelect = null;
		ResultSet resultSet = null;
		List dbsegments = new ArrayList();
		try
		{
			segmentAllSelect = connection
					.prepareStatement("select version_, name_ from search_segments");
			segmentAllSelect.clearParameters();
			resultSet = segmentAllSelect.executeQuery();
			while (resultSet.next())
			{
				final long version = resultSet.getLong(1);
				final String name = resultSet.getString(2);
				SegmentInfo si = new SegmentInfo(name, version, true);
				dbsegments.add(si);
				log.debug("DB Segment " + si);
			}
		}
		finally
		{
			try
			{
				resultSet.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				segmentAllSelect.close();
			}
			catch (Exception ex)
			{
			}
		}
		return dbsegments;
	}

	/**
	 * updat this save this local segment into the db
	 * 
	 * @param connection
	 * @param addsi
	 */
	private void updateDBSegment(Connection connection, SegmentInfo addsi)
			throws SQLException, IOException
	{

		PreparedStatement segmentUpdate = null;
		PreparedStatement segmentInsert = null;
		InputStream packetStream = null;
		File packetFile = null;
		long newVersion = System.currentTimeMillis();
		try
		{
			segmentUpdate = connection
					.prepareStatement("update search_segments set packet_ = ?, version_ = ?, size_ = ? where name_ = ? and version_ = ?");
			segmentInsert = connection
					.prepareStatement("insert into search_segments (packet_, name_, version_, size_ ) values ( ?,?,?,?)");
			packetFile = packSegment(addsi, newVersion);
			packetStream = new FileInputStream(packetFile);
			if (addsi.isInDb())
			{
				segmentUpdate.clearParameters();
				segmentUpdate.setBinaryStream(1, packetStream, (int) packetFile
						.length());
				segmentUpdate.setLong(2, newVersion);
				segmentUpdate.setLong(3, packetFile.length());
				segmentUpdate.setString(4, addsi.getName());
				segmentUpdate.setLong(5, addsi.getVersion());
				if (segmentUpdate.executeUpdate() != 1)
				{
					throw new SQLException(" ant Find packet to update "
							+ addsi);
				}
			}
			else
			{
				segmentInsert.clearParameters();
				segmentInsert.setBinaryStream(1, packetStream, (int) packetFile
						.length());
				segmentInsert.setString(2, addsi.getName());
				segmentInsert.setLong(3, newVersion);
				segmentInsert.setLong(4, packetFile.length());
				if (segmentInsert.executeUpdate() != 1)
				{
					throw new SQLException(" Failed to insert packet  " + addsi);
				}
			}
			addsi.setVersion(newVersion);
			log.debug("DB Updated " + addsi);
			try
			{
				packetStream.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				packetFile.delete();
			}
			catch (Exception ex)
			{
			}

		}
		finally
		{
			try
			{
				packetStream.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				packetFile.delete();
			}
			catch (Exception ex)
			{
			}
			try
			{
				segmentUpdate.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				segmentInsert.close();
			}
			catch (Exception ex)
			{
			}
		}

	}

	private void removeDBSegment(Connection connection, SegmentInfo rmsi)
			throws SQLException
	{
		PreparedStatement segmentDelete = null;
		try
		{
			if (rmsi.isInDb())
			{
				segmentDelete = connection
						.prepareStatement("delete from search_segments where name_ = ? and version_ = ?");
				segmentDelete.clearParameters();
				segmentDelete.setString(1, rmsi.getName());
				segmentDelete.setLong(2, rmsi.getVersion());
				segmentDelete.execute();
				log.debug("DB Removed " + rmsi);
			}
		}
		finally
		{
			try
			{
				segmentDelete.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	/**
	 * create a new local segment and mark its tiestamp
	 */
	public File newSegment() throws IOException
	{
		File f = null;
		for (;;)
		{
			f = new File(searchIndexDirectory, String.valueOf(System
					.currentTimeMillis()));
			if (!f.exists())
			{
				break;
			}
		}
		f.mkdirs();
		setCheckSum(f);

		return f;
	}

	private void setCheckSum(File segmentdir) throws IOException
	{
		File timestamp = new File(segmentdir, TIMESTAMP_FILE);
		String[] fields = getTimeStampFields(timestamp);
		if (fields == null || fields.length < 2)
		{
			String[] newfields = new String[2];
			try
			{
				newfields[1] = getNewCheckSum(segmentdir.getName());
			}
			catch (Exception ex)
			{
				log.debug("Failed to get checksum ");
				newfields[1] = "none";
			}
			if (fields == null || fields.length < 1)
			{
				newfields[0] = String.valueOf(System.currentTimeMillis());
			}
			else
			{
				newfields[0] = fields[0];
			}
			fields = newfields;
		}
		else
		{
			try
			{
				fields[1] = getNewCheckSum(segmentdir.getName());
			}
			catch (Exception ex)
			{
				log.debug("Failed to get checksum ");
				fields[1] = "none";
			}

		}
		setTimeStampFields(fields, timestamp);
		// update the cache
		checked.put(segmentdir.getName(), fields[1]);
	}

	private String getCheckSum(String segmentName) throws IOException
	{
		File segmentdir = new File(searchIndexDirectory, segmentName);
		return getCheckSum(segmentdir);
	}

	private String getCheckSum(File segmentdir) throws IOException
	{

		File timestamp = new File(segmentdir, TIMESTAMP_FILE);

		String[] field = getTimeStampFields(timestamp);
		if (field.length >= 2)
		{
			return field[1];
		}
		else
		{
			return "none";
		}
	}

	private void setTimeStamp(File segmentdir, long l) throws IOException
	{

		File timestamp = new File(segmentdir, TIMESTAMP_FILE);
		String[] fields = getTimeStampFields(timestamp);
		if (fields == null || fields.length < 1)
		{
			fields = new String[2];
			try
			{
				fields[1] = getNewCheckSum(segmentdir.getName());
			}
			catch (Exception ex)
			{
				log.debug("Failed to get checksum ");
				fields[1] = "none";
			}
		}
		fields[0] = String.valueOf(l);
		setTimeStampFields(fields, timestamp);
		timestamp.setLastModified(l);
	}

	private long getTimeStamp(File segmentdir) throws IOException
	{

		File timestamp = new File(segmentdir, TIMESTAMP_FILE);

		long ts = -1;
		String[] field = getTimeStampFields(timestamp);
		if (field.length >= 1)
		{
			ts = Long.parseLong(field[0]);
		}
		else
		{
			ts = timestamp.lastModified();
		}
		return ts;
	}

	private String[] getTimeStampFields(File timestamp) throws IOException
	{
		if (!timestamp.exists())
		{
			return null;
		}
		else
		{
			FileReader fr = new FileReader(timestamp);
			char[] c = new char[4096];
			int len = fr.read(c);
			fr.close();
			String tsContents = new String(c, 0, len);
			return tsContents.split(":");
		}
	}

	private void setTimeStampFields(String[] fields, File timestamp)
			throws IOException
	{
		FileWriter fw = new FileWriter(timestamp);
		for (int i = 0; i < fields.length; i++)
		{
			fw.write(fields[i]);
			fw.write(":");
		}
		fw.close();
	}

	/**
	 * get a list of local segments
	 * 
	 * @return
	 * @throws IOException
	 */
	private List getLocalSegments() throws IOException
	{
		List l = new ArrayList();
		File searchDir = new File(searchIndexDirectory);
		File[] files = searchDir.listFiles();
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					File timestamp = new File(files[i], TIMESTAMP_FILE);
					if (timestamp.exists())
					{

						SegmentInfo sgi = new SegmentInfo(files[i].getName(),
								getTimeStamp(files[i]), false);
						l.add(sgi);
						log.debug("LO Segment " + sgi);
					}
				}
			}
		}
		return l;
	}

	/**
	 * segment info contains information on the segment, name, version, in db
	 * 
	 * @author ieb
	 */
	public class SegmentInfo
	{

		private String name;

		private long version;

		private boolean indb;

		public String toString()
		{
			return name + ":" + version + ":" + indb + ":Created:"
					+ new Date(Long.parseLong(name)) + " Update"
					+ new Date(version);
		}

		public SegmentInfo(String name, long version, boolean indb)
		{
			this.name = name;
			this.version = version;
			this.indb = indb;
		}

		public void setInDb(boolean b)
		{
			indb = b;
		}

		public String getName()
		{
			return name;
		}

		public boolean isInDb()
		{
			return indb;
		}

		public long getVersion()
		{
			return version;
		}

		public void setVersion(long l)
		{
			version = l;

		}
	}

	/**
	 * recover from a failiure
	 */
	private void recoverFromFailure()
	{
		log.error("Recover from Failiure is not implementated at the moment,"
				+ " the local index is corrupt, please delete it and it will "
				+ "reload from the database");

	}

	/**
	 * unpack a segment from a zip
	 * 
	 * @param addsi
	 * @param packetStream
	 * @param version
	 */
	private void unpackSegment(SegmentInfo addsi, InputStream packetStream,
			long version) throws IOException
	{
		ZipInputStream zin = new ZipInputStream(packetStream);
		ZipEntry zipEntry = null;
		FileOutputStream fout = null;
		try
		{
			log.debug("Starting Patch ");
			byte[] buffer = new byte[4096];
			while ((zipEntry = zin.getNextEntry()) != null)
			{

				long ts = zipEntry.getTime();
				File f = new File(searchIndexDirectory, zipEntry.getName());
				log.debug("Patching " + f.getAbsolutePath());
				f.getParentFile().mkdirs();
				fout = new FileOutputStream(f);
				int len;
				while ((len = zin.read(buffer)) > 0)
				{
					fout.write(buffer, 0, len);
				}
				zin.closeEntry();
				fout.close();
				f.setLastModified(ts);
			}
			log.debug("Finished Patch");

			try
			{
				checkSegmentValidity(addsi.getName(), true);
			}
			catch (Exception ex)
			{
				throw new RuntimeException("Segment " + addsi.getName()
						+ " is corrupted ");
			}

			addsi.setVersion(version);
			log.debug("Synced " + addsi);

		}
		finally
		{
			try
			{
				fout.close();
			}
			catch (Exception ex)
			{
			}
		}

	}

	/**
	 * pack a segment into the zip
	 * 
	 * @param addsi
	 * @return
	 * @throws IOException
	 */
	private File packSegment(SegmentInfo addsi, long newVersion)
			throws IOException
	{

		File tmpFile = new File(searchIndexDirectory, PACKFILE
				+ String.valueOf(System.currentTimeMillis()) + ".zip");
		ZipOutputStream zout = new ZipOutputStream(
				new FileOutputStream(tmpFile));
		File segmentFile = new File(searchIndexDirectory, addsi.getName());
		setCheckSum(segmentFile);
		setTimeStamp(segmentFile, newVersion);

		byte[] buffer = new byte[4096];
		if (segmentFile.isDirectory())
		{
			addFile(segmentFile, zout, buffer);
		}
		zout.close();
		// touch the version

		try
		{
			log.debug("Packed " + tmpFile.getName() + "|"
					+ getNewCheckSum(addsi.getName()) + "|" + tmpFile.length()
					+ "|" + addsi);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tmpFile;
	}

	/**
	 * add a file to the zout stream
	 * 
	 * @param f
	 * @param zout
	 * @param buffer
	 * @throws IOException
	 */
	private void addFile(File f, ZipOutputStream zout, byte[] buffer)
			throws IOException
	{
		FileInputStream fin = null;
		try
		{
			if (f.isDirectory())
			{
				File[] files = f.listFiles();
				if (files != null)
				{
					for (int i = 0; i < files.length; i++)
					{
						if (files[i].isDirectory())
						{
							addFile(files[i], zout, buffer);
						}
						else
						{
							String path = files[i].getPath();
							if (path.startsWith(searchIndexDirectory))
							{
								path = path.substring(searchIndexDirectory
										.length());
							}
							ZipEntry ze = new ZipEntry(path);
							log.debug("Adding " + ze.getName());
							ze.setTime(files[i].lastModified());
							zout.putNextEntry(ze);
							fin = new FileInputStream(files[i]);
							int len = 0;
							while ((len = fin.read(buffer)) > 0)
							{
								zout.write(buffer, 0, len);
							}
							fin.close();
							zout.closeEntry();
						}
					}
				}
			}
			else
			{
				ZipEntry ze = new ZipEntry(f.getPath());
				ze.setTime(f.lastModified());
				zout.putNextEntry(ze);
				fin = new FileInputStream(f);
				int len = 0;
				while ((len = fin.read(buffer)) > 0)
				{
					zout.write(buffer, 0, len);
				}
				fin.close();
				zout.closeEntry();
			}
		}
		finally
		{
			try
			{
				fin.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * delete all files under this file and including this file
	 * 
	 * @param f
	 */
	private void deleteAll(File f)
	{
		if (f.isDirectory())
		{
			File[] files = f.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].isDirectory())
					{
						deleteAll(files[i]);
					}
					else
					{
						files[i].delete();
					}
				}
			}
		}
		f.delete();
	}

	/**
	 * @return Returns the dataSource.
	 */
	public DataSource getDataSource()
	{
		return dataSource;
	}

	/**
	 * @param dataSource
	 *        The dataSource to set.
	 */
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public void setLocation(String location)
	{

		searchIndexDirectory = location;
		log.info("Search Index Location is "+location);

	}

	/**
	 * @param autoDdl
	 *        The autoDdl to set.
	 */
	public void setAutoDdl(boolean autoDdl)
	{
		this.autoDdl = autoDdl;
	}

	public void touchSegment(File currentSegment) throws IOException
	{
		setTimeStamp(currentSegment, System.currentTimeMillis());

	}

	/**
	 * create a temporary index for indexing operations
	 */
	public File getTemporarySegment(boolean delete)
	{
		// this index will not have a timestamp, and hence will not be part sync
		// with the db
		File f = new File(searchIndexDirectory, TEMP_INDEX_NAME);
		if (delete && f.exists())
		{
			deleteAll(f);
		}
		f.mkdirs();
		return f;
	}

	public void removeTemporarySegment()
	{
		File f = new File(searchIndexDirectory, TEMP_INDEX_NAME);
		if (f.exists())
		{
			deleteAll(f);
		}
	}

	public void recoverSegment(String segmentName)
	{

		File f = new File(searchIndexDirectory, segmentName);
		deleteAll(f);
		SegmentInfo recoverSegInfo = new SegmentInfo(f.getName(), 0, true);
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			updateLocalSegment(connection, recoverSegInfo);
			connection.commit();
		}
		catch (Exception ex)
		{
			try
			{
				connection.rollback();
			}
			catch (Exception e)
			{
			}
			throw new RuntimeException("Failed to recover dammaged segment ",
					ex);

		}
		finally
		{
			try
			{
				connection.close();
			}
			catch (Exception e)
			{

			}
		}

	}

	public String getSegmentName(String segmentPath)
	{
		File f = new File(segmentPath);
		return f.getName();
	}

	public boolean checkSegmentValidity(String segmentName) throws Exception
	{
		return checkSegmentValidity(segmentName, false);
	}

	public boolean checkSegmentValidity(String segmentName, boolean force)
			throws Exception
	{
		if (!force && !validate)
		{
			return true;
		}
		String liveCheckSum = null;
		boolean cached = false;
		if (force || checked.get(segmentName) == null)
		{

			liveCheckSum = getNewCheckSum(segmentName);

		}
		else
		{
			cached = true;
			liveCheckSum = (String) checked.get(segmentName);
		}
		String storedCheckSum = getCheckSum(segmentName);
		if (!"none".equals(storedCheckSum)
				&& !liveCheckSum.equals(storedCheckSum))
		{
			checked.remove(segmentName);
			boolean check = false;
			if (cached)
			{

				log.debug("Performing Retry");
				check = checkSegmentValidity(segmentName, true);
			}
			else
			{
				log.debug(" No Retry");
			}
			if (!check)
			{
				log.info("Checksum Failed Live(" + segmentName + ") = "
						+ liveCheckSum);
				log.info("Checksum Failed Stor(" + segmentName + ") = "
						+ storedCheckSum);
			}
			return check;
		}
		else
		{
			checked.put(segmentName, liveCheckSum);
			return true;
		}

	}

	public String getNewCheckSum(String segmentName)
			throws NoSuchAlgorithmException, IOException
	{
		File segmentFile = new File(searchIndexDirectory, segmentName);
		File[] files = segmentFile.listFiles();
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		String ignore = ":" + TIMESTAMP_FILE + ":";
		byte[] buffer = new byte[4096];
		for (int i = 0; i < files.length; i++)
		{
			if (TIMESTAMP_FILE.indexOf(files[i].getName()) < 0)
			{
				InputStream fin = new FileInputStream(files[i]);
				int len = 0;
				while ((len = fin.read(buffer)) > 0)
				{
					md5.update(buffer, 0, len);
				}
				fin.close();
			}
		}
		char[] encoding = "0123456789ABCDEF".toCharArray();
		byte[] checksum = md5.digest();
		char[] hexchecksum = new char[checksum.length * 2];
		for (int i = 0; i < checksum.length; i++)
		{
			int lo = checksum[i] & 0x0f;
			int hi = (checksum[i] >> 4) & 0x0f;
			hexchecksum[i * 2] = encoding[lo];
			hexchecksum[i * 2 + 1] = encoding[hi];
		}
		String echecksum = new String(hexchecksum);

		log.debug("Checksum " + segmentName + " is " + echecksum);
		return echecksum;
	}

	/**
	 * @return Returns the validate.
	 */
	public boolean isValidate()
	{
		return validate;
	}

	/**
	 * @param validate
	 *        The validate to set.
	 */
	public void setValidate(boolean validate)
	{
		this.validate = validate;
	}
}
