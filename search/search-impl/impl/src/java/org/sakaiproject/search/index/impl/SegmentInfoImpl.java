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
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.index.SegmentInfo;

/**
 * @author ieb
 */
/**
 * segment info contains information on the segment, name, version, in db
 * 
 * @author ieb
 */
public class SegmentInfoImpl implements SegmentInfo
{
	private static final Log log = LogFactory.getLog(SegmentInfoImpl.class);

	public static final String TIMESTAMP_FILE = "_sakaicluster";

	// if this file exists in the segment, this is a new segment that has not
	// been completed

	public static final String NEW_FILE = "_newsegment";

	// if this file exists in the segment, the segment has been deleted
	public static final String DELETED_FILE = "_deletedsegment";

	private String name;

	private long version;

	private boolean indb;

	private String[] states = new String[] { "new", "created", "deleted" };

	private boolean localStructuredStorage;

	private String searchIndexDirectory;

	private File segmentLocation;

	private File timestampFile;

	private File newFile;

	private File deletedFile;

	private long segmentSize;

	public static final int STATE_NEW = 0;

	public static final int STATE_CREATED = 1;

	public static final int STATE_DELETED = 2;

	private SegmentState storedSegmentState = null;

	private SegmentState liveSegmentState = null;

	private static ConcurrentHashMap<String, String> lock = new ConcurrentHashMap<String, String>();;

	public String toString()
	{
		return name + ":" + version + ":" + indb + ": State:" + states[getState()]
				+ ":Created:" + name + ": Update" + new Date(version);
	}

	public static SegmentInfo newSharedSegmentInfo(String name, long version,
			boolean localStructuredStorage, String searchIndexDirectory)
	{
		return new SegmentInfoImpl(name, version, true, localStructuredStorage,
				searchIndexDirectory);
	}

	public static SegmentInfo newLocalSegmentInfo(File file,
			boolean localStructuredStorage, String searchIndexDirectory)
	{
		return new SegmentInfoImpl(file, false, localStructuredStorage,
				searchIndexDirectory);
	}

	/**
	 * Create a new copy of the segment refreshing from the local disk.
	 * 
	 * @param recoverSegInfo
	 * @return
	 */
	public static SegmentInfo newLocalSegmentInfo(SegmentInfo recoverSegInfo)
	{
		SegmentInfoImpl si = (SegmentInfoImpl) recoverSegInfo;
		File segmentLocation = getSegmentLocation(si.getName(),
				si.localStructuredStorage, si.searchIndexDirectory);
		return new SegmentInfoImpl(segmentLocation, false, si.localStructuredStorage,
				si.searchIndexDirectory);
	}

	/**
	 * Create Segment Info and set the version number explicity, normally as a
	 * result of the Segment vomming from the DB
	 * 
	 * @param name
	 * @param version
	 * @param indb
	 * @param localStructuredStorage
	 * @param searchIndexDirectory
	 */
	private SegmentInfoImpl(String name, long version, boolean indb,
			boolean localStructuredStorage, String searchIndexDirectory)
	{
		this.searchIndexDirectory = searchIndexDirectory;
		this.localStructuredStorage = localStructuredStorage;
		segmentLocation = getSegmentLocation(name, localStructuredStorage,
				searchIndexDirectory);
		timestampFile = new File(segmentLocation, TIMESTAMP_FILE);
		newFile = new File(segmentLocation, NEW_FILE);
		deletedFile = new File(segmentLocation, DELETED_FILE);
		this.indb = indb;
		this.version = version;
		this.name = name;

		try
		{
			storedSegmentState = new SegmentState(this, timestampFile);
		}
		catch (IOException e)
		{
			log.debug("Cant Load Stored Segment State");
		}
		try
		{
			liveSegmentState = new SegmentState(this, null);
			liveSegmentState.setTimeStamp(version);
		}
		catch (IOException e)
		{
			log.debug("Cant Load Live Segment State");
		}
	}

	/**
	 * Initialize a local segment
	 * 
	 * @param file
	 * @param indb
	 * @param localStructuredStorage
	 * @param searchIndexDirectory
	 */
	private SegmentInfoImpl(File file, boolean indb, boolean localStructuredStorage,
			String searchIndexDirectory)
	{
		this.searchIndexDirectory = searchIndexDirectory;
		this.localStructuredStorage = localStructuredStorage;
		segmentLocation = file;
		timestampFile = new File(segmentLocation, TIMESTAMP_FILE);
		newFile = new File(segmentLocation, NEW_FILE);
		deletedFile = new File(segmentLocation, DELETED_FILE);
		this.indb = indb;
		this.name = file.getName();
		/*
		 * The stored segment state is null, hence we have a potentially broken
		 * segment. We should set the version to -1 and let it update from the
		 * database if it can.
		 */
		this.version = -1;
		if (segmentLocation.isDirectory())
		{
			try
			{
				storedSegmentState = new SegmentState(this, timestampFile);
				this.version = storedSegmentState.getTimeStamp();
			}
			catch (IOException e)
			{
				log.info("Segment (" + name + "): Cant Load Stored Segment State");
			}
			try
			{
				// this might cause a performance problem with a structured
				// index
				// since this is called hierachically and so may generate a
				// checksum
				// of the entire tree. Perhapse we should look for the existance
				// of the lucene segments file before performing this operation
				liveSegmentState = new SegmentState(this, null);
			}
			catch (IOException e)
			{
				log.info("Segment (" + name + "): Cant Load Live Segment State");
			}
		}
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

	public void setState(int state)
	{
		if (state == STATE_NEW)
		{
			if (!newFile.exists())
			{
				try
				{
					newFile.getParentFile().mkdirs();
					newFile.createNewFile();
				}
				catch (IOException e)
				{
					log.error("Segment (" + name
							+ "): Failed to create new segment marker at " + newFile);
				}
			}
			if (deletedFile.exists())
			{
				deletedFile.delete();
			}
		}
		if (state == STATE_CREATED)
		{
			if (deletedFile.exists())
			{
				deletedFile.delete();
			}
			if (newFile.exists())
			{
				newFile.delete();
			}
		}
		if (state == STATE_DELETED)
		{
			if (!deletedFile.exists())
			{
				try
				{
					deletedFile.getParentFile().mkdirs();
					deletedFile.createNewFile();
				}
				catch (IOException e)
				{
					log.error("Segment (" + name
							+ "): Failed to create deleted segment marker at "
							+ deletedFile);
				}
			}
			if (newFile.exists())
			{
				newFile.delete();
			}
		}
	}

	/*
	 * public static int getState(String name, boolean localStructuredStorage,
	 * String searchIndexDirectory) { File f = getSegmentLocation(name,
	 * localStructuredStorage, searchIndexDirectory); return getState(f); }
	 */

	public int getState()
	{
		if (deletedFile.exists())
		{
			return STATE_DELETED;
		}
		else if (newFile.exists())
		{
			return STATE_NEW;
		}
		else
		{
			return STATE_CREATED;
		}

	}

	public File getSegmentLocation()
	{
		return segmentLocation;
	}

	public static File getSegmentLocation(String name, boolean structured,
			String searchIndexDirectory)
	{
		if (structured)
		{

			String hashName = name.substring(name.length() - 4, name.length() - 2);
			File hash = new File(searchIndexDirectory, hashName);
			return new File(hash, name);
		}
		else
		{
			return new File(searchIndexDirectory, name);
		}
	}

	public void setNew()
	{
		setState(STATE_NEW);
	}

	public void setCreated()
	{
		setState(STATE_CREATED);
	}

	public void setDeleted()
	{
		setState(STATE_DELETED);
	}

	public boolean isNew()
	{
		return (getState() == STATE_NEW);
	}

	public boolean isCreated()
	{
		return (getState() == STATE_CREATED);
	}

	public boolean isDeleted()
	{
		return (getState() == STATE_DELETED);
	}

	/**
	 * set the timestamp in the segment
	 * 
	 * @param l
	 * @throws IOException
	 */
	public void setTimeStamp(long l) throws IOException
	{
		liveSegmentState.analyze(this);
		liveSegmentState.setTimeStamp(l);
		liveSegmentState.save(timestampFile);
		timestampFile.setLastModified(l);
	}

	public boolean isClusterSegment()
	{
		return (timestampFile.exists());
	}

	/**
	 * get the checksum out of the segment by the segment name
	 * 
	 * @param segmentName
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	/*
	 * private String getCheckSum(String segmentName) throws IOException { File
	 * segmentdir = SegmentInfo.getSegmentLocation(segmentName,
	 * localStructuredStorage, searchIndexDirectory); return
	 * getCheckSum(segmentLocation); }
	 */
	public boolean checkSegmentValidity(boolean logging, String message)
	{
		/**
		 * Dont check new segments, there will not be any state to check
		 */
		if (isCreated())
		{
			return liveSegmentState.checkValidity(logging, message, storedSegmentState);
		}
		return true;
	}

	public long getLocalSegmentLastModified()
	{
		long lm = segmentLocation.lastModified();
		if (segmentLocation.isDirectory())
		{
			File[] l = segmentLocation.listFiles();
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

	public long getLocalSegmentSize()
	{
		if (segmentLocation.isDirectory())
		{
			long lm = 0;
			File[] l = segmentLocation.listFiles();
			for (int i = 0; i < l.length; i++)
			{
				lm += l[i].length();
			}
			return lm;

		}
		else
		{
			return segmentLocation.length();
		}
	}

	public long getTotalSize()
	{
		return getTotalSize(segmentLocation);
	}

	private long getTotalSize(File currentSegment)
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

	public void touchSegment() throws IOException
	{
		setTimeStamp(System.currentTimeMillis());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.SegmentInfo#getSize()
	 */
	public long getSize()
	{
		return segmentSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.SegmentInfo#loadSize()
	 */
	public void loadSize()
	{
		segmentSize = getTotalSize();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.SegmentInfo#doFinalDelete()
	 */
	public void doFinalDelete()
	{
		if (isDeleted())
		{
			File f = getSegmentLocation();
			deleteAll(f);
			log.info("Segment (" + name + "): Deleted ");
		}
		else
		{
			log.error("Segment (" + name + "): Attempt to delete current Segment Data "
					+ this);
		}

	}

	/**
	 * delete all files under this file and including this file
	 * 
	 * @param f
	 */
	public static void deleteAll(File f)
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
						if (files[i].exists())
						{
							log.warn("Failed to delete  " + files[i].getPath());
						}
					}
				}
			}
		}
		f.delete();
		if (f.exists())
		{
			log.warn("Failed to delete  " + f.getPath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.SegmentInfo#isLocalLock()
	 */
	public boolean isLocalLock()
	{
		String threadName = lock.get(this.name);
		if (threadName == null || threadName.equals(Thread.currentThread().getName()))
		{
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.SegmentInfo#lockLocalSegment()
	 */
	public void lockLocalSegment()
	{
		String threadName = lock.get(this.name);
		if (threadName == null)
		{
			lock.put(this.name, Thread.currentThread().getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.SegmentInfo#unlockLocalSegment()
	 */
	public void unlockLocalSegment()
	{
		if (isLocalLock())
		{
			lock.remove(this.name);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.SegmentInfo#debugSegment(java.lang.String)
	 */
	public void debugSegment(String message)
	{

		liveSegmentState.checkValidity(true, message, storedSegmentState);
		dumpAllFiles(getSegmentLocation(), message);
	}

	/**
	 * @param message
	 */
	private void dumpAllFiles(File f, String message)
	{
		dumpFileInfo(f, message);
		File[] files = f.listFiles();
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					dumpAllFiles(files[i], message);
				}
				else
				{
					dumpFileInfo(files[i], message);

				}
			}
		}
	}

	/**
	 * @param f
	 * @param message
	 */
	private void dumpFileInfo(File f, String message)
	{
		String fileInfo = message + "(" + f.getPath() + "):";
		if (!f.exists())
		{
			log.error(fileInfo + " File No longer exists");

		}
		else
		{
			log.info(fileInfo + " size=[" + f.length() + "] lastModified=["
					+ f.lastModified() + "] read=[" + f.canRead() + "] write=["
					+ f.canWrite() + "] hidden=[" + f.isHidden() + "]");
			try
			{
				FileInputStream fin = new FileInputStream(f);
				fin.read(new byte[4096]);
				log.info(fileInfo + " readOk");
				FileLock fl = fin.getChannel().tryLock();
				fl.release();
				fin.close();
				log.info(fileInfo + " lockOk");
			}
			catch (Exception ex)
			{
				log.warn(fileInfo + " Lock or Read failed: ", ex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.SegmentInfo#compareTo(java.lang.String,
	 *      org.sakaiproject.search.index.SegmentInfo)
	 */
	public void compareTo(String message, SegmentInfo compare)
	{
		SegmentInfoImpl si = (SegmentInfoImpl) compare;
		liveSegmentState.checkValidity(true, message, si.liveSegmentState);
	}

}
