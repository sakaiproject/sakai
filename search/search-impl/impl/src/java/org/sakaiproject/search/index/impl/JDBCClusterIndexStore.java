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
import java.io.OutputStream;
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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
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

	private static final String TEMP_INDEX_NAME = "tempindex";

	private static final String INDEX_PATCHNAME = "indexpatch";;

	private boolean autoDdl = false;

	private boolean parallelIndex = false;

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

	private String sharedSegments = null;

	private boolean debug = false;

	/**
	 * locatStructuredStorage causes local segments to be placed into structured
	 * storage on the local disk
	 */
	private boolean localStructuredStorage = false;

	/**
	 * sharedStructuredStorage causes the shared segments to be placed into
	 * shared structured storage in the shared location
	 */
	private boolean sharedStructuredStorage = false;
	
	
	private ClusterSegmentsStorage clusterStorage = null;


	public void init()
	{
		log.info(this + ":init() ");
		clusterStorage = new ClusterSegmentsStorage(searchIndexDirectory, this,
				localStructuredStorage, debug);
		
		// TODO: We should migrate to the correct storage format, on the local 
		// and shared space, by looking at the DB and then checking what is there 
		// followed by a move.
		// Since we are doing a move, it should be ok to have this happend on the fly.
		
		
		
		
		/*
		 * The storage is created by hibernate now try { if (autoDdl) {
		 * SqlService.getInstance().ddl(this.getClass().getClassLoader(),
		 * "search_cluster"); } } catch (Exception ex) { log.error("Failed to
		 * init JDBCClusterIndexStorage", ex); }
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

			List badLocalSegments = getBadLocalSegments();
			// delete any bad local segments before we load so that they get
			// updated
			// from the db
			deleteAll(badLocalSegments);

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
					log.debug("Missing Will update " + db_si);
				}
				else
				{
					log.debug("Present Will Not update " + db_si);
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
						log.debug("Newer will Update " + db_si);
						found = true;
						break;
					}
				}
				if (!found)
				{
					log.debug("Ok will not update " + current_si);
				}
			}

			// which if the currentSegments need updating
			// process the remove list
			// we can only perform a remove, IF there is no other activity.
			// ie only on the first time in any 1 JVM run
			if (okToRemove)
			{
				okToRemove = true;
				// with merge we need to remove local segments
				// that are not present. This may cause problems with
				// an open index, as it will suddenly see segments dissapear
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
						log.debug("Will remove " + local_si);
					}
					else
					{
						log.debug("Ok Will not remove " + local_si);
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
				try {
					updateLocalSegment(connection, addsi);
				} catch ( Exception ex ) {
					// ignore failures to unpack a local segment. It may have been removed by
					// annother node
					log.info("Segment was not unpacked "+ex.getClass().getName()+":"+ex.getMessage());
				}

			}
			// if we made any modifications, we also need to process the patch
			if (updateLocalSegments.size() > 0)
			{
				updateLocalPatch(connection);
			}

			// build the list putting the current segment at the end
			for (Iterator i = dbSegments.iterator(); i.hasNext();)
			{
				SegmentInfo si = (SegmentInfo) i.next();
				File f = getSegmentLocation(si.getName(),localStructuredStorage);
				if ( f.exists() ) {
					// only add those segments that exist after the sync
					segmentList.add(f.getPath());
				}
				log.debug("Segment Present at " + f.getName());
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

	private void deleteAll(List badLocalSegments)
	{
		for (Iterator i = badLocalSegments.iterator(); i.hasNext();)
		{
			File f = (File) i.next();
			deleteAll(f);
		}
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
			List badLocalSegments = getBadLocalSegments();

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
				// dont delete bad segments from the DB
				if (!found)
				{
					for (Iterator j = badLocalSegments.iterator(); j.hasNext();)
					{
						File local_file = (File) j.next();
						if (name.equals(local_file.getName()))
						{
							found = true;
							break;
						}
					}
				}
				if (!found)
				{
					removeDBSegments.add(db_si);
					log.debug("Will remove from the DB " + db_si);
				}
				else
				{
					currentDBSegments.add(db_si);
					log.debug("In the DB will not remove " + db_si);
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
					log.debug(" Will update to the DB " + local_si);
				}
				else
				{
					log.debug(" Will NOT update to the DB " + local_si);

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
						log.debug("Will update modified to the DB " + db_si);
						found = true;
						break;
					}
				}
				if (!found)
				{
					log.debug("Will not update the DB, matches " + local_si);

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
			updateDBPatch(connection);

			for (Iterator i = updateDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo si = (SegmentInfo) i.next();
				File f = getSegmentLocation(si.getName(),localStructuredStorage);
				segmentList.add(f.getPath());
				log.debug("Segments saved " + f.getName());

			}
			connection.commit();
			deleteAll(badLocalSegments);
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
			List badLocalSegments = getBadLocalSegments();

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
				// dont delete bad segments from the DB
				if (!found)
				{
					for (Iterator j = badLocalSegments.iterator(); j.hasNext();)
					{
						File local_file = (File) j.next();
						if (name.equals(local_file.getName()))
						{
							found = true;
							break;
						}
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
				File f = getSegmentLocation(si.getName(),localStructuredStorage);
				segmentList.add(f.getPath());
			}
			connection.commit();
			deleteAll(badLocalSegments);
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

	protected void updateLocalSegment(Connection connection, SegmentInfo addsi)
			throws SQLException, IOException
	{
		if (sharedSegments == null || sharedSegments.length() == 0)
		{
			updateLocalSegmentBLOB(connection, addsi);
		}
		else
		{
			updateLocalSegmentFilesystem(connection, addsi);
		}

	}

	/**
	 * updte a segment from the database
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateLocalSegmentBLOB(Connection connection,
			SegmentInfo addsi) throws SQLException, IOException
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
					checked.remove(addsi.getName()); // force revalidation
					clusterStorage.unpackSegment(addsi, packetStream, version);
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
		File f = getSegmentLocation( rmsi.getName(), localStructuredStorage);
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
					.prepareStatement("select version_, name_ from search_segments where name_ <> ? ");
			segmentAllSelect.clearParameters();
			segmentAllSelect.setString(1, INDEX_PATCHNAME);
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

	protected void updateDBPatch(Connection connection) throws SQLException,
			IOException
	{

		if (sharedSegments == null || sharedSegments.length() == 0)
		{
			updateDBPatchBLOB(connection);
		}
		else
		{
			updateDBPatchFilesystem(connection);
		}
	}

	/**
	 * updat this save this local segment into the db
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateDBPatchBLOB(Connection connection)
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
					.prepareStatement("update search_segments set packet_ = ?, version_ = ?, size_ = ? where name_ = ?");
			segmentInsert = connection
					.prepareStatement("insert into search_segments (packet_, name_, version_, size_ ) values ( ?,?,?,?)");
			packetFile = clusterStorage.packPatch();
			packetStream = new FileInputStream(packetFile);
			segmentUpdate.clearParameters();
			segmentUpdate.setBinaryStream(1, packetStream, (int) packetFile
					.length());
			segmentUpdate.setLong(2, newVersion);
			segmentUpdate.setLong(3, packetFile.length());
			segmentUpdate.setString(4, INDEX_PATCHNAME);
			if (segmentUpdate.executeUpdate() != 1)
			{
				segmentInsert.clearParameters();
				segmentInsert.setBinaryStream(1, packetStream, (int) packetFile
						.length());
				segmentInsert.setString(2, INDEX_PATCHNAME);
				segmentInsert.setLong(3, newVersion);
				segmentInsert.setLong(4, packetFile.length());
				if (segmentInsert.executeUpdate() != 1)
				{
					throw new SQLException(" Failed to insert patch  ");
				}
			}
			log.debug("DB Updated Patch ");
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

	/**
	 * updat this save this local segment into the db
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateDBPatchFilesystem(Connection connection)
			throws SQLException, IOException
	{

		PreparedStatement segmentUpdate = null;
		PreparedStatement segmentInsert = null;
		InputStream packetStream = null;
		OutputStream sharedStream = null;
		File packetFile = null;
		File sharedFinalFile = null;
		File sharedTempFile = null;
		long newVersion = System.currentTimeMillis();
		try
		{
			sharedTempFile = new File(getSharedTempFileName(INDEX_PATCHNAME));
			sharedFinalFile = new File(getSharedFileName(INDEX_PATCHNAME, sharedStructuredStorage));
			packetFile = clusterStorage.packPatch();
			packetStream = new FileInputStream(packetFile);
			sharedStream = new FileOutputStream(sharedTempFile);

			byte[] b = new byte[1024 * 1024];
			int l = 0;
			while ((l = packetStream.read(b)) != -1)
			{
				sharedStream.write(b, 0, l);
			}

			packetStream.close();
			sharedStream.close();

			segmentUpdate = connection
					.prepareStatement("update search_segments set  version_ = ?, size_ = ? where name_ = ? ");
			segmentInsert = connection
					.prepareStatement("insert into search_segments ( name_, version_, size_ ) values ( ?,?,?)");

			segmentUpdate.clearParameters();
			segmentUpdate.setLong(1, newVersion);
			segmentUpdate.setLong(2, packetFile.length());
			segmentUpdate.setString(3, INDEX_PATCHNAME);
			if (segmentUpdate.executeUpdate() != 1)
			{
				segmentInsert.clearParameters();
				segmentInsert.setString(1, INDEX_PATCHNAME);
				segmentInsert.setLong(2, newVersion);
				segmentInsert.setLong(3, packetFile.length());
				if (segmentInsert.executeUpdate() != 1)
				{
					throw new SQLException(" Failed to add patch packet  ");
				}
			}
			sharedTempFile.renameTo(sharedFinalFile);
			log.debug("DB Patch ");

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
				sharedStream.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				sharedTempFile.delete();
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

	protected void updateDBSegment(Connection connection, SegmentInfo addsi)
			throws SQLException, IOException
	{

		if (sharedSegments == null || sharedSegments.length() == 0)
		{
			updateDBSegmentBLOB(connection, addsi);
		}
		else
		{
			updateDBSegmentFilesystem(connection, addsi);
		}
	}

	/**
	 * updat this save this local segment into the db
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateDBSegmentBLOB(Connection connection, SegmentInfo addsi)
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
			packetFile = clusterStorage.packSegment(addsi, newVersion);
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

				String sharedSegment = getSharedFileName(rmsi.getName(), sharedStructuredStorage);
				if (sharedSegment != null)
				{
					File f = new File(sharedSegment);
					if (f.exists())
					{
						f.delete();
					}
				}

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
			f = getSegmentLocation( String.valueOf(System
					.currentTimeMillis()), localStructuredStorage);
			if (!f.exists())
			{
				break;
			}
		}
		f.mkdirs();
		setCheckSum(f);

		return f;
	}

	/**
	 * set the checksum file up
	 * @param segmentdir
	 * @throws IOException
	 */
	void setCheckSum(File segmentdir) throws IOException
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

	/**
	 * get the checksum out of the segment by the segment name
	 * @param segmentName
	 * @return
	 * @throws IOException
	 */
	private String getCheckSum(String segmentName) throws IOException
	{
		File segmentdir = getSegmentLocation( segmentName, localStructuredStorage);
		return getCheckSum(segmentdir);
	}

	/**
	 * get the checksum using the segment name as a File
	 * @param segmentdir
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * set the timestamp in the segment
	 * @param segmentdir
	 * @param l
	 * @throws IOException
	 */
	void setTimeStamp(File segmentdir, long l) throws IOException
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

	/**
	 * get the timestam associated with the segment dir
	 * @param segmentdir
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * get the timestamp fields
	 * @param timestamp
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * set the timestamp fields
	 * @param fields
	 * @param timestamp
	 * @throws IOException
	 */
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
	List getLocalSegments() throws IOException
	{
		List l = new ArrayList();
		File searchDir = new File(searchIndexDirectory);
		return getLocalSegments(searchDir,l);
	}
	
	/**
	 * recurse into a list of segments
	 * @param searchDir
	 * @param l
	 * @return
	 * @throws IOException
	 */
	private List getLocalSegments(File searchDir, List l) throws IOException
	{
	
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
						if (IndexReader.indexExists(files[i]))
						{

							SegmentInfo sgi = new SegmentInfo(files[i]
									.getName(), getTimeStamp(files[i]), false);
							l.add(sgi);
							log.debug("LO Segment " + sgi);
						}
						else
						{
							log
									.warn("Found Orphaned directory with no segment information present "
											+ files[i]);
						}

					} else {
						l = getLocalSegments(files[i],l);
					}
				}
			}
		}
		return l;
	}

	/**
	 * get a list of bad segmetns with brokenindexes
	 * @return
	 * @throws IOException
	 */
	private List getBadLocalSegments() throws IOException
	{
		List l = new ArrayList();
		File searchDir = new File(searchIndexDirectory);
		return getBadLocalSegments(searchDir,l);
	}
	/**
	 * recurse into a list of bad local segments
	 * @param searchDir
	 * @param l
	 * @return
	 * @throws IOException
	 */
	private List getBadLocalSegments(File searchDir, List l) throws IOException
	{
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
						if (!IndexReader.indexExists(files[i]))
						{
							l.add(files[i]);
						}

					} else {
						l = getBadLocalSegments(files[i],l);
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
		log.info("Search Index Location is " + location);

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

		File f = getSegmentLocation(segmentName, localStructuredStorage);
		deleteAll(f);
		SegmentInfo recoverSegInfo = new SegmentInfo(f.getName(), 0, true);
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			updateLocalSegment(connection, recoverSegInfo);
			// we also need to re-apply the patch
			updateLocalPatch(connection);
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

	protected void updateLocalPatch(Connection connection) throws SQLException,
			IOException
	{
		if (sharedSegments == null || sharedSegments.length() == 0)
		{
			updateLocalPatchBLOB(connection);
		}
		else
		{
			updateLocalPatchFilesystem(connection);
		}
	}

	protected void updateLocalPatchFilesystem(Connection connection)
			throws SQLException, IOException
	{
		log.debug("Updating local patch ");
		PreparedStatement segmentSelect = null;
		ResultSet resultSet = null;
		try
		{
			segmentSelect = connection
					.prepareStatement("select version_ from search_segments where name_ = ?");
			segmentSelect.clearParameters();
			segmentSelect.setString(1, INDEX_PATCHNAME);
			resultSet = segmentSelect.executeQuery();
			if (resultSet.next())
			{
				InputStream packetStream = null;
				try
				{
					long version = resultSet.getLong(1);
					File f = new File(getSharedFileName(INDEX_PATCHNAME,sharedStructuredStorage));
					packetStream = new FileInputStream(f);
					clusterStorage.unpackPatch(packetStream);
					log.debug("Updated Patch ");
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
				log.debug("Didnt find patch in database, this is Ok");
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

	private void updateLocalPatchBLOB(Connection connection)
			throws SQLException, IOException
	{
		log.debug("Updating local patch ");
		PreparedStatement segmentSelect = null;
		ResultSet resultSet = null;
		try
		{
			segmentSelect = connection
					.prepareStatement("select version_, packet_ from search_segments where name_ = ?");
			segmentSelect.clearParameters();
			segmentSelect.setString(1, INDEX_PATCHNAME);
			resultSet = segmentSelect.executeQuery();
			if (resultSet.next())
			{
				InputStream packetStream = null;
				try
				{
					long version = resultSet.getLong(1);
					packetStream = resultSet.getBinaryStream(2);
					clusterStorage.unpackPatch(packetStream);
					log.debug("Updated Patch from DB " + version);
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
				log.debug("Didnt find patch in database, this is Ok ");
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
				if (!force)
				{
					log.info("Checksum Failed Live(" + segmentName + ") = "
							+ liveCheckSum);
					log.info("Checksum Failed Stor(" + segmentName + ") = "
							+ storedCheckSum);
				}
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
		File segmentFile = getSegmentLocation(segmentName,localStructuredStorage);
		File[] files = segmentFile.listFiles();
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		String ignore = ":" + TIMESTAMP_FILE + ":";
		byte[] buffer = new byte[4096];
		for (int i = 0; i < files.length; i++)
		{
			// only perform the md5 on the index, not the segments or del tables
			if (files[i].getName().endsWith(".cfs"))
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

	public void removeLocalSegment(File mergeSegment)
	{
		if ((new File(mergeSegment, "segments")).exists())
		{
			deleteAll(mergeSegment);
		}
	}

	/**
	 * updat this save this local segment into the db
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateDBSegmentFilesystem(Connection connection,
			SegmentInfo addsi) throws SQLException, IOException
	{

		PreparedStatement segmentUpdate = null;
		PreparedStatement segmentInsert = null;
		InputStream packetStream = null;
		OutputStream sharedStream = null;
		File packetFile = null;
		File sharedFinalFile = null;
		File sharedTempFile = null;
		long newVersion = System.currentTimeMillis();
		try
		{
			sharedTempFile = new File(getSharedTempFileName(addsi.getName()));
			sharedFinalFile = new File(getSharedFileName(addsi.getName(), sharedStructuredStorage));
			packetFile = clusterStorage.packSegment(addsi, newVersion);
			packetStream = new FileInputStream(packetFile);
			sharedStream = new FileOutputStream(sharedTempFile);

			byte[] b = new byte[1024 * 1024];
			int l = 0;
			while ((l = packetStream.read(b)) != -1)
			{
				sharedStream.write(b, 0, l);
			}

			packetStream.close();
			sharedStream.close();

			segmentUpdate = connection
					.prepareStatement("update search_segments set  version_ = ?, size_ = ? where name_ = ? and version_ = ?");
			segmentInsert = connection
					.prepareStatement("insert into search_segments ( name_, version_, size_ ) values ( ?,?,?)");
			if (addsi.isInDb())
			{
				segmentUpdate.clearParameters();
				segmentUpdate.setLong(1, newVersion);
				segmentUpdate.setLong(2, packetFile.length());
				segmentUpdate.setString(3, addsi.getName());
				segmentUpdate.setLong(4, addsi.getVersion());
				if (segmentUpdate.executeUpdate() != 1)
				{
					throw new SQLException(" ant Find packet to update "
							+ addsi);
				}
			}
			else
			{
				segmentInsert.clearParameters();
				segmentInsert.setString(1, addsi.getName());
				segmentInsert.setLong(2, newVersion);
				segmentInsert.setLong(3, packetFile.length());
				if (segmentInsert.executeUpdate() != 1)
				{
					throw new SQLException(" Failed to insert packet  " + addsi);
				}
			}
			addsi.setVersion(newVersion);
			sharedFinalFile.getParentFile().mkdirs();
			sharedTempFile.renameTo(sharedFinalFile);
			log.info("DB Updated " + addsi);

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
				sharedStream.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				sharedTempFile.delete();
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

	private String getSharedFileName(String name, boolean structured)
	{
		if (sharedSegments != null && sharedSegments.length() > 0)
		{
			if (!sharedSegments.endsWith("/"))
			{
				sharedSegments = sharedSegments + "/";
			}
			if ( structured && !INDEX_PATCHNAME.equals(name) ) {
				String hashName = name.substring(name.length()-4,name.length()-2);
				return sharedSegments + hashName + "/" + name +".zip";
			} else {
				return sharedSegments + name + ".zip";
			}
		}
		return null;
	}

	private String getSharedTempFileName(String name)
	{

		if (sharedSegments != null && sharedSegments.length() > 0)
		{
			if (!sharedSegments.endsWith("/"))
			{
				sharedSegments = sharedSegments + "/";
			}
			return sharedSegments + name + ".zip." + System.currentTimeMillis();
		}
		return null;
	}

	/**
	 * updte a segment from the database
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateLocalSegmentFilesystem(Connection connection,
			SegmentInfo addsi) throws SQLException, IOException
	{
		log.debug("Updating local segment from databse " + addsi);
		PreparedStatement segmentSelect = null;
		ResultSet resultSet = null;
		try
		{
			segmentSelect = connection
					.prepareStatement("select version_ from search_segments where name_ = ?");
			segmentSelect.clearParameters();
			segmentSelect.setString(1, addsi.getName());
			resultSet = segmentSelect.executeQuery();
			if (resultSet.next())
			{
				InputStream packetStream = null;
				try
				{
					long version = resultSet.getLong(1);
					File f = new File(getSharedFileName(addsi.getName(),sharedStructuredStorage));
					packetStream = new FileInputStream(f);
					checked.remove(addsi.getName()); // force revalidation
					clusterStorage.unpackSegment(addsi, packetStream, version);
					log.debug("Updated Local " + addsi);
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

	public String getSharedSegments()
	{
		return sharedSegments;
	}

	public void setSharedSegments(String sharedSegments)
	{
		this.sharedSegments = sharedSegments;
	}

	public void dolog(String message)
	{
		if (debug)
		{
			log.info("JDBCClusterDebug :" + message);
		}
		else if (log.isDebugEnabled())
		{
			log.debug("JDBCClusterDebug :" + message);
		}
	}

	public long getLastUpdate()
	{
		PreparedStatement segmentSelect = null;
		ResultSet resultSet = null;
		Connection connection = null;

		try
		{
			connection = dataSource.getConnection();
			segmentSelect = connection
					.prepareStatement("select version_ from search_segments order by version_ desc");
			segmentSelect.clearParameters();
			resultSet = segmentSelect.executeQuery();
			if (resultSet.next())
			{
				return resultSet.getLong(1);
			}
			else
			{
				return 0;
			}
		}
		catch (Exception ex)
		{
			log.warn(" Cant find last update time " + ex.getClass().getName()
					+ ":" + ex.getMessage());
			return 0;
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
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{

			}
		}
	}

	public List getSegmentInfoList()
	{
		List seginfo = new ArrayList();
		try
		{

			File searchDir = new File(searchIndexDirectory);
			long tsize = getSegmentInfoList(searchDir, seginfo);
			String size = null;
			if (tsize > 1024 * 1024 * 10)
			{
				size = String.valueOf(tsize / (1024 * 1024)) + "MB";
			}
			else if (tsize >= 1024 * 1024)
			{
				size = String.valueOf(tsize / (1024 * 1024)) + "."
						+ String.valueOf(tsize / (102 * 1024) + "MB");
			}
			else
			{
				size = String.valueOf(tsize / (1024)) + "KB";
			}
			seginfo.add(new Object[] { "Total", size, "" });

		}
		catch (Exception ex)
		{
			seginfo.add("Failed to get Segment Info list "
					+ ex.getClass().getName() + " " + ex.getMessage());
		}
		return seginfo;

	}

	public long getSegmentInfoList(File searchDir, List seginfo)
	{

		File[] files = searchDir.listFiles();
		long tsize = 0;
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					File timestamp = new File(files[i], TIMESTAMP_FILE);
					if (timestamp.exists())
					{
						String name = files[i].getName();
						long lsize = getLocalSegmentSize(files[i]);
						tsize += lsize;
						long ts = getLocalSegmentLastModified(files[i]);
						String lastup = (new Date(ts)).toString();

						String size = null;
						if (lsize > 1024 * 1024 * 10)
						{
							size = String.valueOf(lsize / (1024 * 1024)) + "MB";
						}
						else if (lsize >= 1024 * 1024)
						{
							size = String.valueOf(lsize / (1024 * 1024))
									+ "."
									+ String.valueOf(lsize / (102 * 1024)
											+ "MB");
						}
						else
						{
							size = String.valueOf(lsize / (1024)) + "KB";
						}
						seginfo.add(new Object[] { name, size, lastup });
					}
					else
					{
						tsize += getSegmentInfoList(files[i], seginfo);
					}
				}
			}
		}
		return tsize;

	}

	File getSegmentLocation(String name, boolean structured)
	{
		if ( structured ) {
			
			String hashName = name.substring(name.length()-4,name.length()-2);
			File hash = new File(searchIndexDirectory,hashName);
			return new File(hash,name);
		} else {
			return new File(searchIndexDirectory,name);
		}		
	}

	private long getLocalSegmentLastModified(File file)
	{
		long lm = file.lastModified();
		if (file.isDirectory())
		{
			File[] l = file.listFiles();
			for (int i = 0; i < l.length; i++)
			{
				if (l[i].lastModified() > lm)
				{
					lm = l[i].lastModified();
				}
			}

		}
		return lm;
	}

	private long getLocalSegmentSize(File file)
	{
		if (file.isDirectory())
		{
			long lm = 0;
			File[] l = file.listFiles();
			for (int i = 0; i < l.length; i++)
			{
				lm += l[i].length();
			}
			return lm;

		}
		else
		{
			return file.length();
		}
	}

	public void getLock()
	{
		if (parallelIndex)
		{
			throw new RuntimeException("Parallel index is not implemented yet");
		}

	}

	public void releaseLock()
	{
		if (parallelIndex)
		{
			throw new RuntimeException("Parallel index is not implemented yet");
		}
	}

	public boolean isMultipleIndexers()
	{
		return parallelIndex;
	}

	public boolean isParallelIndex()
	{
		return parallelIndex;
	}

	public void setParallelIndex(boolean parallelIndex)
	{
		this.parallelIndex = parallelIndex;
	}

	/**
	 * @return Returns the localStructuredStorage.
	 */
	public boolean isLocalStructuredStorage()
	{
		return localStructuredStorage;
	}

	/**
	 * @param localStructuredStorage The localStructuredStorage to set.
	 */
	public void setLocalStructuredStorage(boolean localStructuredStorage)
	{
		this.localStructuredStorage = localStructuredStorage;
	}

	/**
	 * @return Returns the sharedStructuredStorage.
	 */
	public boolean isSharedStructuredStorage()
	{
		return sharedStructuredStorage;
	}

	/**
	 * @param sharedStructuredStorage The sharedStructuredStorage to set.
	 */
	public void setSharedStructuredStorage(boolean sharedStructuredStorage)
	{
		this.sharedStructuredStorage = sharedStructuredStorage;
	}
}
