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

package org.sakaiproject.search.index.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
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
import org.sakaiproject.search.api.SearchService;
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

	private static final long MAX_BLOCK_SIZE = 1024 * 1024 * 10; // 10M
																	// blocks

	private boolean autoDdl = false;

	private boolean parallelIndex = false;

	/**
	 * If validate is true, all segments will be checked on initial startup and
	 * upload. This can take a long time. If its false, only when an index is
	 * updated is the MD5 checked. Recomendation is to leave this false.
	 */
	private boolean validate = false;

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

	private SearchService searchService;

	public void init()
	{
		try
		{
			log.info(this + ":init() ");
			clusterStorage = new ClusterSegmentsStorage(searchService,
					searchIndexDirectory, this, localStructuredStorage, debug);

			// We should migrate to the correct storage format, on the
			// local
			// and shared space, by looking at the DB and then checking what is
			// there
			// followed by a move.
			// Since we are doing a move, it should be ok to have this happend
			// on
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
			 * "search_cluster"); } } catch (Exception ex) { log.error("Failed
			 * to init JDBCClusterIndexStorage", ex); }
			 */
			log.info(this + ":init() Ok ");
		}
		catch (Exception ex)
		{
			log.error("Failed to start Cluster Index store", ex);
			System.exit(-1);
		}

	}

	/**
	 * Ther might need to be some locking here. When readers use this, they will
	 * update the local segments with more information, update the local
	 * Segmetns from the DB
	 * 
	 * @param locked
	 *        A locak has been taken on the index
	 */
	public List<SegmentInfo> updateSegments()
	{
		Connection connection = null;
		List<SegmentInfo> segmentList = new ArrayList<SegmentInfo>();
		try
		{
			connection = dataSource.getConnection();
			List dbSegments = getDBSegments(connection);
			if (log.isDebugEnabled())
				log.debug("Update: DB Segments = " + dbSegments.size());
			// remove files not in the dbSegmentList
			List<SegmentInfo> localSegments = getLocalSegments();

			List<SegmentInfo> badLocalSegments = getBadLocalSegments();
			// delete any bad local segments before we load so that they get
			// updated
			// from the db
			deleteAllSegments(badLocalSegments);

			List<SegmentInfo> deletedSegments = getDeletedLocalSegments();
			// delete any segments marked as for deletion, by the last cycle.
			// if this is due to a index reader event, we should not be
			// performing this operation
			// as the current reader will have these files open.
			// we should only delete the old segments update thread
			// If this call forms part of an exception, then we should look at
			// doing a
			// timeout on the delete files.
			deleteAllSegments(deletedSegments);

			if (log.isDebugEnabled())
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
					if (log.isDebugEnabled()) log.debug("Missing Will update " + db_si);
				}
				else
				{
					if (log.isDebugEnabled())
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
						if (log.isDebugEnabled())
							log.debug("Newer will Update " + db_si);
						found = true;
						break;
					}
				}
				if (!found)
				{
					if (log.isDebugEnabled())
						log.debug("Ok will not update " + current_si);
				}
			}

			List<SegmentInfo> removeLocalSegments = new ArrayList<SegmentInfo>();

			// which segments exist locally but not in the DB, these should
			// be
			// removed
			for (Iterator<SegmentInfo> i = localSegments.iterator(); i.hasNext();)
			{

				SegmentInfo local_si = (SegmentInfo) i.next();
				// only check local segments that are not new and not
				if (local_si.isCreated())
				{
					boolean found = false;
					String name = local_si.getName();
					for (Iterator<SegmentInfo> j = dbSegments.iterator(); j.hasNext();)
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
						if (log.isDebugEnabled()) log.debug("Will remove " + local_si);
					}
					else
					{
						if (log.isDebugEnabled())
							log.debug("Ok Will not remove " + local_si);
					}
				}
			}

			// if we could mark the local segment for deletion so that
			// its is only deleted the next time a lock is taken on the index
			for (Iterator<SegmentInfo> i = removeLocalSegments.iterator(); i.hasNext();)
			{
				SegmentInfo rmsi = (SegmentInfo) i.next();
				removeLocalSegment(rmsi);
			}

			// process the get list, first markign the segments as existing, so
			// that other threads
			// dont update them, then perform the update.
			try
			{
				for (Iterator<SegmentInfo> i = updateLocalSegments.iterator(); i.hasNext();)
				{
					SegmentInfo addsi = (SegmentInfo) i.next();
					addsi.lockLocalSegment();
				}
				for (Iterator<SegmentInfo> i = updateLocalSegments.iterator(); i.hasNext();)
				{
					SegmentInfo addsi = (SegmentInfo) i.next();
					try
					{
						// This is thread safe strangely since the source doesnt
						// change
						// hence although it could be wastefull, more than one
						// copy can perform an update at a
						// time.
						if (addsi.isLocalLock())
						{
							updateLocalSegment(connection, addsi);
						}
						else
						{
							log
									.warn("Not Updating Segment, since lock is not on this thread "
											+ addsi.getName());
						}
					}
					catch (Exception ex)
					{
						// ignore failures to unpack a local segment. It may
						// have
						// been removed by
						// annother node
						log.info("Segment was not unpacked " + ex.getClass().getName()
								+ ":" + ex.getMessage());
					}

				}
			}
			finally
			{
				for (Iterator<SegmentInfo> i = updateLocalSegments.iterator(); i.hasNext();)
				{
					SegmentInfo addsi = (SegmentInfo) i.next();
					addsi.unlockLocalSegment();
				}

			}
			// if we made any modifications, we also need to process the patch
			if (updateLocalSegments.size() > 0)
			{
				updateLocalPatch(connection);
			}

			// build the list putting the current segment at the end
			for (Iterator<SegmentInfo> i = dbSegments.iterator(); i.hasNext();)
			{
				SegmentInfo si = (SegmentInfo) i.next();
				File f = si.getSegmentLocation();
				if (f.exists())
				{
					// only add those segments that exist after the sync
					segmentList.add(si);
				}
				if (log.isDebugEnabled()) log.debug("Segment Present at " + f.getName());
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
				log.debug(ex);
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
				log.debug(e);
			}
		}
		return segmentList;
	}

	private void deleteAllSegments(List<SegmentInfo> badLocalSegments)
	{
		for (Iterator<SegmentInfo> i = badLocalSegments.iterator(); i.hasNext();)
		{

			SegmentInfo s = i.next();
			s.doFinalDelete();
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
			for (Iterator<SegmentInfo> i = dbSegments.iterator(); i.hasNext();)
			{
				SegmentInfo db_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = db_si.getName();
				for (Iterator<SegmentInfo> j = localSegments.iterator(); j.hasNext();)
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
					for (Iterator<SegmentInfo> j = badLocalSegments.iterator(); j.hasNext();)
					{
						SegmentInfo local_file = (SegmentInfo) j.next();
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
					if (log.isDebugEnabled())
						log.debug("Will remove from the DB " + db_si);
				}
				else
				{
					currentDBSegments.add(db_si);
					if (log.isDebugEnabled())
						log.debug("In the DB will not remove " + db_si);
				}
			}

			List<SegmentInfo> updateDBSegments = new ArrayList<SegmentInfo>();
			// which of the localSegments are not in the db

			for (Iterator<SegmentInfo> i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = local_si.getName();
				for (Iterator<SegmentInfo> j = dbSegments.iterator(); j.hasNext();)
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
					if (log.isDebugEnabled())
						log.debug(" Will update to the DB " + local_si);
				}
				else
				{
					if (log.isDebugEnabled())
						log.debug(" Will NOT update to the DB " + local_si);

				}
			}

			// which of the localSegments have been modified
			for (Iterator<SegmentInfo> i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();
				boolean found = false;
				String name = local_si.getName();
				long version = local_si.getVersion();
				for (Iterator<SegmentInfo> j = dbSegments.iterator(); j.hasNext();)
				{
					SegmentInfo db_si = (SegmentInfo) j.next();
					if (name.equals(db_si.getName()) && version > db_si.getVersion())
					{
						updateDBSegments.add(db_si);
						if (log.isDebugEnabled())
							log.debug("Will update modified to the DB " + db_si);
						found = true;
						break;
					}
				}
				if (!found)
				{
					if (log.isDebugEnabled())
						log.debug("Will not update the DB, matches " + local_si);

				}
			}

			// process the get list
			for (Iterator<SegmentInfo> i = updateDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo addsi = (SegmentInfo) i.next();
				updateDBSegment(connection, addsi);
			}
			// build the list putting the current segment at the end
			updateDBPatch(connection);

			for (Iterator<SegmentInfo> i = updateDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo si = (SegmentInfo) i.next();
				File f = si.getSegmentLocation();
				segmentList.add(si);
				if (log.isDebugEnabled()) log.debug("Segments saved " + f.getName());

			}

			// process the remove list, the update was Ok so we can remove all
			// the old segments
			for (Iterator<SegmentInfo> i = removeDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo rmsi = (SegmentInfo) i.next();
				removeDBSegment(connection, rmsi);
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
				log.debug(e);
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
				log.debug(e);
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

			for (Iterator<SegmentInfo> i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();

				boolean found = false;
				String name = local_si.getName();
				for (Iterator<SegmentInfo> j = dbSegments.iterator(); j.hasNext();)
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
					for (Iterator<SegmentInfo> j = badLocalSegments.iterator(); j.hasNext();)
					{
						SegmentInfo local_file = (SegmentInfo) j.next();
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
			for (Iterator<SegmentInfo> i = localSegments.iterator(); i.hasNext();)
			{
				SegmentInfo local_si = (SegmentInfo) i.next();
				String name = local_si.getName();
				
				for (Iterator<SegmentInfo> j = dbSegments.iterator(); j.hasNext();)
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
			for (Iterator<SegmentInfo> i = updateDBSegments.iterator(); i.hasNext();)
			{
				SegmentInfo addsi = (SegmentInfo) i.next();
				updateDBSegment(connection, addsi);
			}
			// build the list putting the current segment at the end

			for (Iterator<SegmentInfo> i = updateDBSegments.iterator(); i.hasNext();)
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
				log.debug(e);
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
				log.debug(e);
			}
		}
		return segmentList;
	}

	protected void updateLocalSegment(Connection connection, SegmentInfo addsi)
			throws SQLException, IOException
	{
		if (searchService.hasDiagnostics())
		{
			log.info("\tUpdate Local Segment from Database " + addsi);
		}
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
		if (log.isDebugEnabled())
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
					clusterStorage.unpackSegment(addsi, packetStream, version);
					if (log.isDebugEnabled())
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
						log.debug(ex);
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
				log.debug(ex);
			}
			try
			{
				segmentSelect.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
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
		if (searchService.hasDiagnostics())
		{
			log.info("\tMarked Local Segment for deletion " + rmsi);
		}
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
				SegmentInfo si = SegmentInfoImpl.newSharedSegmentInfo(name, version,
						localStructuredStorage, searchIndexDirectory);
				dbsegments.add(si);
				if (log.isDebugEnabled()) log.debug("DB Segment " + si);
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
				log.debug(ex);
			}
			try
			{
				segmentAllSelect.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}
		return dbsegments;
	}

	protected void updateDBPatch(Connection connection) throws SQLException, IOException
	{

		if (localSegmentsOnly)
		{
			if (log.isDebugEnabled())
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
			if (packetFile.exists())
			{
				packetStream = new FileInputStream(packetFile);
				segmentUpdate.clearParameters();
				segmentUpdate.setBinaryStream(1, packetStream, (int) packetFile.length());
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
				if (log.isDebugEnabled()) log.debug("DB Updated Patch ");
			}
			else
			{
				log.warn(" Packed Patch does not exist " + packetFile.getPath());
			}
		}
		finally
		{
			try
			{
				if (packetStream != null)
				{
					packetStream.close();
				}
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				packetFile.delete();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				segmentUpdate.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				segmentInsert.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
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
		FileChannel packetStream = null;
		FileChannel sharedStream = null;
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
			if (packetFile.exists())
			{
				packetStream = new FileInputStream(packetFile).getChannel();
				sharedTempFile.getParentFile().mkdirs();
				sharedStream = new FileOutputStream(sharedTempFile).getChannel();

				doBlockedStream(packetStream, sharedStream);

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

				long st = System.currentTimeMillis();
				sharedTempFile.renameTo(sharedFinalFile);
				if (searchService.hasDiagnostics())
				{
					log.info("Renamed " + sharedTempFile.getPath() + " to "
							+ sharedFinalFile.getPath() + " in "
							+ (System.currentTimeMillis() - st) + "ms");
				}
			}
			else
			{
				log.warn("Packet file does not exist " + packetFile.getPath());
			}

		}
		finally
		{
			try
			{
				if (packetStream != null) 
				{
					packetStream.close();
				}
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				packetFile.delete();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				if (sharedStream != null)
				{
					sharedStream.close();
				}
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				sharedTempFile.delete();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				segmentUpdate.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				segmentInsert.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}

	}

	protected void updateDBSegment(Connection connection, SegmentInfo addsi)
			throws SQLException, IOException
	{
		if (searchService.hasDiagnostics())
		{
			log.info("\tUpdate Database Segment from Local " + addsi);
		}
		if (localSegmentsOnly)
		{
			if (log.isDebugEnabled())
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
			if (packetFile.exists())
			{
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
						throw new SQLException(" ant Find packet to update " + addsi);
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
				if (log.isDebugEnabled()) log.debug("DB Updated " + addsi);
				try
				{
					packetStream.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
				try
				{
					packetFile.delete();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
			}
			else
			{
				log.warn("Packet file does not exist " + packetFile.getPath());
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
				log.debug(ex);
			}
			try
			{
				packetFile.delete();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				segmentUpdate.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				segmentInsert.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
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

				if (searchService.hasDiagnostics())
				{
					log.info("\tRemoved Segment From Database [" + rmsi + "]");
				}
			}
		}
		finally
		{
			try
			{
				if (segmentDelete != null) {
					segmentDelete.close();
				}
			}
			catch (Exception ex)
			{
				log.debug(ex);
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
			f = SegmentInfoImpl.getSegmentLocation(String.valueOf(System
					.currentTimeMillis()), localStructuredStorage, searchIndexDirectory);
			if (!f.exists())
			{
				break;
			}
		}
		f.mkdirs();

		SegmentInfo si = SegmentInfoImpl.newLocalSegmentInfo(f, localStructuredStorage,
				searchIndexDirectory);
		si.setNew();
		si.setTimeStamp(System.currentTimeMillis());

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

					SegmentInfo sgi = SegmentInfoImpl.newLocalSegmentInfo(files[i],
							localStructuredStorage, searchIndexDirectory);
					if (sgi.isClusterSegment())
					{
						if (IndexReader.indexExists(files[i]))
						{

							if (sgi.isCreated())
							{
								l.add(sgi);
								if (log.isDebugEnabled()) log.debug("LO Segment " + sgi);
							}
							else
							{
								if (log.isDebugEnabled())
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
					SegmentInfo sgi = SegmentInfoImpl.newLocalSegmentInfo(files[i],
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
					SegmentInfo sgi = SegmentInfoImpl.newLocalSegmentInfo(files[i],
							localStructuredStorage, searchIndexDirectory);
					if (sgi.isClusterSegment())
					{
						if (sgi.isDeleted())
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
			SegmentInfoImpl.deleteAll(f);
		}
		f.mkdirs();
		return f;
	}

	public void removeTemporarySegment()
	{
		File f = new File(searchIndexDirectory, TEMP_INDEX_NAME);
		if (f.exists())
		{
			SegmentInfoImpl.deleteAll(f);
		}
	}

	public SegmentInfo saveTemporarySegment() throws IOException
	{
		SegmentInfo segInfo = newSegment();
		File s = new File(searchIndexDirectory, TEMP_INDEX_NAME);
		File d = segInfo.getSegmentLocation();
		copyAll(s, d);
		segInfo.setCreated();
		segInfo.touchSegment();
		return segInfo;
	}

	/**
	 * Copy a file to a directory, if the source is a directory, the copy
	 * recurses into the directory. If the destination does not exists, it is
	 * assumed to be a file.
	 * 
	 * @param s
	 *        the source file
	 * @param d
	 *        the source directory
	 * @throws IOException
	 */
	private void copyAll(File s, File d) throws IOException
	{
		if (s.isDirectory())
		{
			File[] fl = s.listFiles();
			for (int i = 0; i < fl.length; i++)
			{
				if (fl[i].isFile())
				{
					copyFile(fl[i], d);
				}
				else
				{
					File nd = new File(d, fl[i].getName());
					nd.mkdirs();
					copyAll(fl[i], nd);
				}
			}
		}
		else
		{
			copyFile(s, d);
		}
	}

	/**
	 * Copy a file from s to d, s will be a file, d may be a file or directory
	 * 
	 * @param s
	 * @param d
	 * @throws IOException
	 */
	private void copyFile(File s, File d) throws IOException
	{
		if (log.isDebugEnabled())
			log.debug("Copying " + s.getAbsolutePath() + " to " + d.getAbsolutePath());
		if (s.exists() && s.isFile())
		{
			File t = d; // target
			if (d.isDirectory())
			{
				if (!d.exists())
				{
					d.mkdirs();
				}
				t = new File(d, s.getName());
			}
			else
			{
				File p = d.getParentFile();
				if (!p.exists())
				{
					p.mkdirs();
				}
			}
			FileChannel srcChannel = null;
			FileChannel dstChannel = null;
			try
			{
				// use nio
				// Create channel on the source
				srcChannel = new FileInputStream(s).getChannel();

				// Create channel on the destination
				dstChannel = new FileOutputStream(t).getChannel();

				// Copy file contents from source to destination
				doBlockedStream(srcChannel, dstChannel);

				// Close the channels
			}
			finally
			{
				try
				{
					srcChannel.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);

				}
				try
				{
					dstChannel.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);

				}
			}

		}
	}

	public void recoverSegment(SegmentInfo recoverSegInfo)
	{
		SegmentInfo segInfo = SegmentInfoImpl.newLocalSegmentInfo(recoverSegInfo);
		segInfo.debugSegment("Pre Recovery Check :");

		segInfo.compareTo("Comparing Disk Segment to Recovered Segment", recoverSegInfo);

		recoverSegInfo.setDeleted();
		recoverSegInfo.doFinalDelete();
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
				log.debug(ex);
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
				log.debug(e);

			}
		}

		SegmentInfo newSegInfo = SegmentInfoImpl.newLocalSegmentInfo(recoverSegInfo);
		newSegInfo.debugSegment("Recovered Segment");
		newSegInfo.compareTo("Comparing Recoverd Segment to Previous Disk Segment",
				segInfo);

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
		if (log.isDebugEnabled()) log.debug("Updating local patch ");
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
					if (f.exists())
					{
						packetStream = new FileInputStream(f);
						clusterStorage.unpackPatch(packetStream);
						if (log.isDebugEnabled()) log.debug("Updated Patch ");
					}
					else
					{
						log.warn("Shared Segment File does not exist " + f.getPath());
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
						log.debug(ex);
					}
				}
			}
			else
			{
				if (log.isDebugEnabled())
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
				log.debug(ex);
			}
			try
			{
				segmentSelect.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}

	}

	private void updateLocalPatchBLOB(Connection connection) throws SQLException,
			IOException
	{
		if (log.isDebugEnabled()) log.debug("Updating local patch ");
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
					if (log.isDebugEnabled())
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
						log.debug(ex);
					}
				}
			}
			else
			{
				if (log.isDebugEnabled())
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
				log.debug(ex);
			}
			try
			{
				segmentSelect.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}

	}

	public String getSegmentName(String segmentPath)
	{
		File f = new File(segmentPath);
		return f.getName();
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
		FileChannel packetStream = null;
		FileChannel sharedStream = null;
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
			if (packetFile.exists())
			{
				packetStream = new FileInputStream(packetFile).getChannel();
				sharedTempFile.getParentFile().mkdirs();
				sharedStream = new FileOutputStream(sharedTempFile).getChannel();

				// Copy file contents from source to destination
				doBlockedStream(packetStream, sharedStream);

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
				long st = System.currentTimeMillis();
				sharedTempFile.renameTo(sharedFinalFile);
				if (searchService.hasDiagnostics())
				{
					log.info("Renamed " + sharedTempFile.getPath() + " to "
							+ sharedFinalFile.getPath() + " in "
							+ (System.currentTimeMillis() - st) + "ms");
				}

				log.info("DB Updated " + addsi);
			}
			else
			{
				log.warn("Packet file does not exist " + packetFile.getPath());
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
				log.debug(ex);
			}
			try
			{
				packetFile.delete();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				sharedStream.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				sharedTempFile.delete();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				segmentUpdate.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				segmentInsert.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}

	}

	/**
	 * @param packetStream
	 * @param sharedStream
	 * @throws IOException
	 */
	private void doBlockedStream(FileChannel from, FileChannel to) throws IOException
	{
		to.position(0);
		long size = from.size();
		for (long pos = 0; pos < size;)
		{
			long count = size - pos;
			if (count > MAX_BLOCK_SIZE)
			{
				count = MAX_BLOCK_SIZE;

			}

			to.position(pos);
			long cpos = to.position();
			log.debug("NIOTransfering |" + count + "| bytes from |" + pos + "| to |"
					+ cpos + "|");
			long t = to.transferFrom(from, pos, count);
			pos = pos + t;
		}
		log.debug("  Final Size Source        " + from.size());
		log.debug("  Final Size Destination   " + to.size());
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
		if (log.isDebugEnabled())
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
					if (f.exists())
					{
						packetStream = new FileInputStream(f);
						clusterStorage.unpackSegment(addsi, packetStream, version);
						if (log.isDebugEnabled()) log.debug("Updated Local " + addsi);
					}
					else
					{
						log.warn("Shared Segment file is missing " + f.getPath());
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
						log.debug(ex);
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
				log.debug(ex);
			}
			try
			{
				segmentSelect.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
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
				log.debug(ex);
			}
			try
			{
				segmentSelect.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);

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

					SegmentInfo sgi = SegmentInfoImpl.newLocalSegmentInfo(files[i],
							localStructuredStorage, searchIndexDirectory);
					if (sgi != null && sgi.isClusterSegment())
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
							if (log.isDebugEnabled()) log.debug("Debug Failure ", ex);
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
					log.debug(ex);

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
					log.debug(ex);
				}
			}
		}

	}

	private void migrateLocalSegments() throws IOException
	{
		List<SegmentInfo> l = getLocalSegments();
		for (Iterator<SegmentInfo> li = l.iterator(); li.hasNext();)
		{
			SegmentInfo si = li.next();
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
					if (log.isDebugEnabled()) log.debug("Debug Failure ", ex);
				}
			}
		}
	}

	public void getLock() throws IOException
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

	/**
	 * @return the searchService
	 */
	public SearchService getSearchService()
	{
		return searchService;
	}

	/**
	 * @param searchService
	 *        the searchService to set
	 */
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.ClusterFilesystem#centralIndexExists()
	 */
	public boolean centralIndexExists()
	{
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			List l = getDBSegments(connection);
			if (l != null && l.size() > 0)
			{
				return true;
			}
			return false;
		}
		catch (SQLException e)
		{
			return false;
		}
		finally
		{
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}
	}

}
