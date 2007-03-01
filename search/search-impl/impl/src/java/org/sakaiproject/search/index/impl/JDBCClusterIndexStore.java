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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.sakaiproject.search.index.ClusterFilesystem;
import org.sakaiproject.search.index.SegmentInfo;

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

	private static final Log log = LogFactory.getLog(JDBCClusterIndexStore.class);

	private DataSource dataSource = null;

	private String searchIndexDirectory = null;

	private static final String TEMP_INDEX_NAME = "tempindex";

	private static final String INDEX_PATCHNAME = "indexpatch";

	private boolean autoDdl = false;

	private boolean parallelIndex = false;

	/**
	 * If validate is true, all segments will be checked on initial startup and
	 * upload. This can take a long time. If its false, only when an index is
	 * updated is the MD5 checked. Recomendation is to leave this false.
	 */
	private boolean validate = false;

	/**
	 * This will be set to after the first update of a JVM run has been
	 * completed, as its possible that IndexReaders may have open references to
	 * the Segments that we try and remove. Update: 2007/02/27 Actualy since we
	 * need to merge segments we not need to remove them so this is no longer
	 * the case, okToRemove is always true
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

	private boolean localSegmentsOnly = false;

	public void init()
	{
		log.info(this + ":init() ");
		clusterStorage = new ClusterSegmentsStorage(searchIndexDirectory, this,
				localStructuredStorage, debug);

		// TODO: We should migrate to the correct storage format, on the local
		// and shared space, by looking at the DB and then checking what is
		// there
		// followed by a move.
		// Since we are doing a move, it should be ok to have this happend on
		// the fly.

		try
		{
			migrateLocalSegments();
			migrateSharedSegments();
		}
		catch (IOException ex)
		{
			log
					.error(
							"Failed to migrate search content to new format, the instance should not continue to run ",
							ex);
			System.exit(-1);
		}

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
	public List<SegmentInfo> updateSegments()
	{
		Connection connection = null;
		List<SegmentInfo> segmentList = new ArrayList<SegmentInfo>();
		try
		{
			connection = dataSource.getConnection();
			List dbSegments = getDBSegments(connection);
			log.debug("Update: DB Segments = " + dbSegments.size());
			// remove files not in the dbSegmentList
			List<SegmentInfo> localSegments = getLocalSegments();

			List<SegmentInfo> badLocalSegments = getBadLocalSegments();
			// delete any bad local segments before we load so that they get
			// updated
			// from the db
			deleteAllSegments(badLocalSegments);

			List<SegmentInfo> deletedSegments = getDeletedLocalSegments();
			// delete any segments marked as for deletion
			deleteAllSegments(deletedSegments);

			log.debug("Update: Local Segments = " + localSegments.size());

			// which of the dbSegments are not present locally

			List<SegmentInfo> updateLocalSegments = new ArrayList<SegmentInfo>();
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
					if (name.equals(db_si.getName()) && db_si.getVersion() > version)
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
				// Update 2007/02/27 It does cause problems. We need to make it
				// possible for
				// search to recover, or to delay the removal of segments until
				// the reload is complete.

				List<SegmentInfo> removeLocalSegments = new ArrayList<SegmentInfo>();

				// which segments exist locally but not in the DB, these should
				// be
				// removed
				for (Iterator i = localSegments.iterator(); i.hasNext();)
				{

					SegmentInfo local_si = (SegmentInfo) i.next();
					// only check local segments that are not new and not
					if (local_si.isCreated())
					{
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
				}

				// if we could mark the local segment for deletion so that
				// its is only deleted some time later.
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
				try
				{
					updateLocalSegment(connection, addsi);
				}
				catch (Exception ex)
				{
					// ignore failures to unpack a local segment. It may have
					// been removed by
					// annother node
					log.info("Segment was not unpacked " + ex.getClass().getName() + ":"
							+ ex.getMessage());
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
				File f = si.getSegmentLocation();
				if (f.exists())
				{
					// only add those segments that exist after the sync
					segmentList.add(si);
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

	private void deleteAllSegments(List<SegmentInfo> badLocalSegments)
	{
		for (Iterator<SegmentInfo> i = badLocalSegments.iterator(); i.hasNext();)
		{
			SegmentInfo s = i.next();
			File f = s.getSegmentLocation();
			deleteAll(f);
		}
	}

	private void deleteAll(List<File> badLocalSegments)
	{
		for (Iterator<File> i = badLocalSegments.iterator(); i.hasNext();)
		{
			File f = i.next();
			deleteAll(f);
		}
	}


	/**
	 * save the local segments to the DB
	 */
	public List<SegmentInfo> saveSegments()
	{
		Connection connection = null;
		List<SegmentInfo> segmentList = new ArrayList<SegmentInfo>();
		try
		{
			connection = dataSource.getConnection();
			List<SegmentInfo> dbSegments = getDBSegments(connection);
			// remove files not in the dbSegmentList
			List<SegmentInfo> localSegments = getLocalSegments();
			List<SegmentInfo> badLocalSegments = getBadLocalSegments();

			// find the dbSegments that are not present locally

			List<SegmentInfo> removeDBSegments = new ArrayList<SegmentInfo>();
			List<SegmentInfo> currentDBSegments = new ArrayList<SegmentInfo>();

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

			List<SegmentInfo> updateDBSegments = new ArrayList<SegmentInfo>();
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
					if (name.equals(db_si.getName()) && version > db_si.getVersion())
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
				File f = si.getSegmentLocation();
				segmentList.add(si);
				log.debug("Segments saved " + f.getName());

			}
			connection.commit();
			deleteAllSegments(badLocalSegments);
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

	public List<SegmentInfo> saveAllSegments()
	{
		Connection connection = null;
		List<SegmentInfo> segmentList = new ArrayList<SegmentInfo>();
		try
		{
			connection = dataSource.getConnection();
			List<SegmentInfo> dbSegments = getDBSegments(connection);
			// remove files not in the dbSegmentList
			List<SegmentInfo> localSegments = getLocalSegments();
			List<SegmentInfo> badLocalSegments = getBadLocalSegments();

			// find the dbSegments that are not present locally

			List<SegmentInfo> updateDBSegments = new ArrayList<SegmentInfo>();
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

			// the db segments
			for (Iterator i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();
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
				segmentList.add(si);
			}
			connection.commit();

			deleteAllSegments(badLocalSegments);
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
		if (localSegmentsOnly)
		{
			log.warn("Update Local Segment Requested with inactive Shared Storage "
					+ addsi);
		}
		else
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

	}

	/**
	 * updte a segment from the database
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateLocalSegmentBLOB(Connection connection, SegmentInfo addsi)
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
					addsi.setForceValidation(); // force revalidation
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
	public void removeLocalSegment(SegmentInfo rmsi)
	{

		rmsi.setDeleted();
		log.debug("LO Marked for deletion " + rmsi);
		/*
		 * File f = getSegmentLocation(rmsi.getName(), localStructuredStorage);
		 * deleteAll(f); log.debug("LO Removed " + rmsi);
		 */
	}

	/**
	 * get a list of all DB segments ordered by version
	 * 
	 * @param connection
	 * @return
	 */
	private List<SegmentInfo> getDBSegments(Connection connection) throws SQLException
	{
		PreparedStatement segmentAllSelect = null;
		ResultSet resultSet = null;
		List<SegmentInfo> dbsegments = new ArrayList<SegmentInfo>();
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
				SegmentInfo si = new SegmentInfoImpl(name, version, true,
						localStructuredStorage, searchIndexDirectory);
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

	protected void updateDBPatch(Connection connection) throws SQLException, IOException
	{

		if (localSegmentsOnly)
		{
			log.debug("Update Patch Requested with inactive Shared Storage ");
		}
		else
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
	}

	/**
	 * updat this save this local segment into the db
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateDBPatchBLOB(Connection connection) throws SQLException,
			IOException
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
			segmentUpdate.setBinaryStream(1, packetStream, (int) packetFile.length());
			segmentUpdate.setLong(2, newVersion);
			segmentUpdate.setLong(3, packetFile.length());
			segmentUpdate.setString(4, INDEX_PATCHNAME);
			if (segmentUpdate.executeUpdate() != 1)
			{
				segmentInsert.clearParameters();
				segmentInsert.setBinaryStream(1, packetStream, (int) packetFile.length());
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
	protected void updateDBPatchFilesystem(Connection connection) throws SQLException,
			IOException
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
			sharedFinalFile = new File(getSharedFileName(INDEX_PATCHNAME,
					sharedStructuredStorage));
			packetFile = clusterStorage.packPatch();
			packetStream = new FileInputStream(packetFile);
			sharedTempFile.getParentFile().mkdirs();
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
		if (localSegmentsOnly)
		{
			log.debug("Not Saving Segment to DB as no Shared Storage " + addsi);
		}
		else
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
				segmentUpdate.setBinaryStream(1, packetStream, (int) packetFile.length());
				segmentUpdate.setLong(2, newVersion);
				segmentUpdate.setLong(3, packetFile.length());
				segmentUpdate.setString(4, addsi.getName());
				segmentUpdate.setLong(5, addsi.getVersion());
				if (segmentUpdate.executeUpdate() != 1)
				{
					throw new SQLException(" ant Find packet to update " + addsi);
				}
			}
			else
			{
				segmentInsert.clearParameters();
				segmentInsert.setBinaryStream(1, packetStream, (int) packetFile.length());
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

				String sharedSegment = getSharedFileName(rmsi.getName(),
						sharedStructuredStorage);
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
	public SegmentInfo newSegment() throws IOException
	{
		File f = null;
		for (;;)
		{
			f = SegmentInfoImpl.getSegmentLocation(
					String.valueOf(System.currentTimeMillis()), localStructuredStorage,
					searchIndexDirectory);
			if (!f.exists())
			{
				break;
			}
		}
		f.mkdirs();

		SegmentInfo si = new SegmentInfoImpl(f, false, localStructuredStorage,
				searchIndexDirectory);
		si.setCheckSum();

		return si;
	}

	/**
	 * get a list of local segments
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<SegmentInfo> getLocalSegments() throws IOException
	{
		List<SegmentInfo> l = new ArrayList<SegmentInfo>();
		File searchDir = new File(searchIndexDirectory);
		return getLocalSegments(searchDir, l);
	}

	/**
	 * recurse into a list of segments
	 * 
	 * @param searchDir
	 * @param l
	 * @return
	 * @throws IOException
	 */
	public List<SegmentInfo> getLocalSegments(File searchDir, List<SegmentInfo> l)
			throws IOException
	{

		File[] files = searchDir.listFiles();
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{

					SegmentInfo sgi = new SegmentInfoImpl(files[i], false,
							localStructuredStorage, searchIndexDirectory);
					if (sgi.isClusterSegment())
					{
						if (IndexReader.indexExists(files[i]))
						{

							if (sgi.isCreated())
							{
								l.add(sgi);
								log.debug("LO Segment " + sgi);
							}
							else
							{
								log.debug("LO Segment not created " + sgi.toString());
							}
						}
						else
						{
							log
									.warn("Found Orphaned directory with no segment information present "
											+ files[i]);
						}

					}
					else
					{
						l = getLocalSegments(files[i], l);
					}
				}
			}
		}
		return l;
	}

	/**
	 * get a list of bad segmetns with brokenindexes
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<SegmentInfo> getBadLocalSegments() throws IOException
	{
		List<SegmentInfo> l = new ArrayList<SegmentInfo>();
		File searchDir = new File(searchIndexDirectory);
		return getBadLocalSegments(searchDir, l);
	}

	/**
	 * get a list of segments that are ready for deletion
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<SegmentInfo> getDeletedLocalSegments() throws IOException
	{
		List<SegmentInfo> l = new ArrayList<SegmentInfo>();
		File searchDir = new File(searchIndexDirectory);
		return getDeletedLocalSegments(searchDir, l);
	}

	/**
	 * recurse into a list of bad local segments
	 * 
	 * @param searchDir
	 * @param l
	 * @return
	 * @throws IOException
	 */
	private List<SegmentInfo> getBadLocalSegments(File searchDir, List<SegmentInfo> l)
			throws IOException
	{
		if (searchDir.isDirectory())
		{
			File[] files = searchDir.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					SegmentInfo sgi = new SegmentInfoImpl(files[i], false,
							localStructuredStorage, searchIndexDirectory);
					if (sgi.isClusterSegment())
					{
						if (sgi.isCreated())
						{
							if (!IndexReader.indexExists(files[i]))
							{
								l.add(sgi);
							}

						}
					}
					else
					{
						l = getBadLocalSegments(files[i], l);
					}
				}
			}
		}

		return l;
	}

	/**
	 * Get a list of segments to be deleted
	 * 
	 * @param searchDir
	 * @param l
	 * @return
	 * @throws IOException
	 */
	private List<SegmentInfo> getDeletedLocalSegments(File searchDir, List<SegmentInfo> l)
			throws IOException
	{
		if (searchDir.isDirectory())
		{
			File[] files = searchDir.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					SegmentInfo sgi = new SegmentInfoImpl(files[i], false,
							localStructuredStorage, searchIndexDirectory);
					if (sgi.isClusterSegment())
					{
						if ( sgi.isDeleted() ) 
						{
							l.add(sgi);
						}
					}
					else
					{
						l = getDeletedLocalSegments(files[i], l);
					}
				}
			}
		}
		return l;
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

	public void recoverSegment(SegmentInfo recoverSegInfo)
	{

		deleteAll(recoverSegInfo.getSegmentLocation());
		recoverSegInfo.setNew();
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
			throw new RuntimeException("Failed to recover dammaged segment ", ex);

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
		if (localSegmentsOnly)
		{
			log.warn("Update Patch Requested with inactive Shared Storage ");
		}
		else
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
	}

	protected void updateLocalPatchFilesystem(Connection connection) throws SQLException,
			IOException
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
					File f = new File(getSharedFileName(INDEX_PATCHNAME,
							sharedStructuredStorage));
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

	private void updateLocalPatchBLOB(Connection connection) throws SQLException,
			IOException
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
		File f = SegmentInfoImpl.getSegmentLocation(segmentName, localStructuredStorage, searchIndexDirectory);
		SegmentInfo sgi = new SegmentInfoImpl(f,false,localStructuredStorage,searchIndexDirectory);
		return sgi.checkSegmentValidity(false, validate);
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


	/**
	 * updat this save this local segment into the db
	 * 
	 * @param connection
	 * @param addsi
	 */
	protected void updateDBSegmentFilesystem(Connection connection, SegmentInfo addsi)
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
			sharedTempFile = new File(getSharedTempFileName(addsi.getName()));
			sharedFinalFile = new File(getSharedFileName(addsi.getName(),
					sharedStructuredStorage));
			packetFile = clusterStorage.packSegment(addsi, newVersion);
			packetStream = new FileInputStream(packetFile);
			sharedTempFile.getParentFile().mkdirs();
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
					throw new SQLException(" ant Find packet to update " + addsi);
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
		if (localSegmentsOnly)
		{
			return null;
		}
		if (sharedSegments != null && sharedSegments.length() > 0)
		{
			if (!sharedSegments.endsWith("/"))
			{
				sharedSegments = sharedSegments + "/";
			}
			if (structured && !INDEX_PATCHNAME.equals(name))
			{
				String hashName = name.substring(name.length() - 4, name.length() - 2);
				return sharedSegments + hashName + "/" + name + ".zip";
			}
			else
			{
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
	protected void updateLocalSegmentFilesystem(Connection connection, SegmentInfo addsi)
			throws SQLException, IOException
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
					File f = new File(getSharedFileName(addsi.getName(),
							sharedStructuredStorage));
					packetStream = new FileInputStream(f);
					addsi.setForceValidation(); // force revalidation
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
			log.warn(" Cant find last update time " + ex.getClass().getName() + ":"
					+ ex.getMessage());
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
			seginfo.add("Failed to get Segment Info list " + ex.getClass().getName()
					+ " " + ex.getMessage());
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
					
					SegmentInfo sgi = null;
					try
					{
						sgi = new SegmentInfoImpl(files[i],false,localStructuredStorage, searchIndexDirectory);
					}
					catch (IOException e)
					{
					}
					if (sgi != null || sgi.isClusterSegment())
					{
						String name = files[i].getName();
						long lsize = sgi.getLocalSegmentSize();
						tsize += lsize;
						long ts = sgi.getLocalSegmentLastModified();
						String lastup = (new Date(ts)).toString();

						String size = null;
						if (lsize > 1024 * 1024 * 10)
						{
							size = String.valueOf(lsize / (1024 * 1024)) + "MB";
						}
						else if (lsize >= 1024 * 1024)
						{
							size = String.valueOf(lsize / (1024 * 1024)) + "."
									+ String.valueOf(lsize / (102 * 1024) + "MB");
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

	private void migrateSharedSegments()
	{
		if (localSegmentsOnly)
		{
			return;
		}
		if (sharedSegments != null && sharedSegments.length() > 0)
		{
			Connection connection = null;
			try
			{
				connection = dataSource.getConnection();

				List l = getDBSegments(connection);
				for (Iterator li = l.iterator(); li.hasNext();)
				{
					SegmentInfo si = (SegmentInfo) li.next();
					String shared = getSharedFileName(si.getName(),
							!sharedStructuredStorage);
					File f = new File(shared);
					if (f.exists())
					{
						File fnew = new File(getSharedFileName(si.getName(),
								sharedStructuredStorage));
						fnew.getParentFile().mkdirs();
						log.info("Moving " + f.getPath() + " to " + fnew.getPath());
						try
						{
							f.renameTo(fnew);
						}
						catch (Exception ex)
						{
							log.warn("Failed " + ex.getMessage());
							log.debug("Debug Failure ", ex);
						}
					}
				}
				connection.commit();
			}
			catch (Exception ex)
			{
				try
				{
					connection.rollback();
				}
				catch (Exception ex1)
				{

				}

			}
			finally
			{
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

	private void migrateLocalSegments() throws IOException
	{
		List<SegmentInfo> l = getLocalSegments();
		for (Iterator<SegmentInfo> li = l.iterator(); li.hasNext();)
		{
			SegmentInfo si =  li.next();
			File f = SegmentInfoImpl.getSegmentLocation(si.getName(),
					!localStructuredStorage, searchIndexDirectory);
			if (f.exists())
			{
				File fnew = SegmentInfoImpl.getSegmentLocation(si.getName(),
						localStructuredStorage, searchIndexDirectory);
				fnew.getParentFile().mkdirs();
				log.info("Moving " + f.getPath() + " to " + fnew.getPath());
				try
				{
					f.renameTo(fnew);
				}
				catch (Exception ex)
				{
					log.warn("Failed " + ex.getMessage());
					log.debug("Debug Failure ", ex);
				}
			}
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
	 * @param localStructuredStorage
	 *        The localStructuredStorage to set.
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
	 * @param sharedStructuredStorage
	 *        The sharedStructuredStorage to set.
	 */
	public void setSharedStructuredStorage(boolean sharedStructuredStorage)
	{
		this.sharedStructuredStorage = sharedStructuredStorage;
	}

	/**
	 * @return the localSegmentsOnly
	 */
	public boolean isLocalSegmentsOnly()
	{
		return localSegmentsOnly;
	}

	/**
	 * @param localSegmentsOnly
	 *        the localSegmentsOnly to set
	 */
	public void setLocalSegmentsOnly(boolean localSegmentsOnly)
	{
		this.localSegmentsOnly = localSegmentsOnly;
	}
}
