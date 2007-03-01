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

package org.sakaiproject.search.index.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Hashtable;

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

	private static Hashtable<String, String> checked = new Hashtable<String, String>();

	public String toString()
	{
		return name + ":" + version + ":" + indb + ": State:" + states[getState()]
				+ ":Created:" + name + ": Update"
				+ new Date(version);
	}

	public SegmentInfoImpl(String name, long version, boolean indb,
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
	}

	public SegmentInfoImpl(File file, boolean indb, boolean localStructuredStorage,
			String searchIndexDirectory) throws IOException
	{
		this.searchIndexDirectory = searchIndexDirectory;
		this.localStructuredStorage = localStructuredStorage;
		segmentLocation = file;
		timestampFile = new File(segmentLocation, TIMESTAMP_FILE);
		newFile = new File(segmentLocation, NEW_FILE);
		deletedFile = new File(segmentLocation, DELETED_FILE);
		this.indb = indb;
		this.version = getTimeStamp();
		this.name = file.getName();
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
					newFile.createNewFile();
				}
				catch (IOException e)
				{
					log.error("Failed to create new segment marker at " + newFile);
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
					deletedFile.createNewFile();
				}
				catch (IOException e)
				{
					log.error("Failed to create new segment marker at " + deletedFile);
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

	public String getNewCheckSum() throws NoSuchAlgorithmException, IOException
	{
		File[] files = segmentLocation.listFiles();
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		String ignore = ":" + SegmentInfoImpl.TIMESTAMP_FILE + ":";
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

		log.debug("Checksum " + name + " is " + echecksum);
		return echecksum;
	}

	/**
	 * set the checksum file up
	 * 
	 * @param segmentdir
	 * @throws IOException
	 */
	public void setCheckSum() throws IOException
	{
		String[] fields = getTimeStampFields();
		if (fields == null || fields.length < 2)
		{
			String[] newfields = new String[2];
			try
			{
				newfields[1] = getNewCheckSum();
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
				fields[1] = getNewCheckSum();
			}
			catch (Exception ex)
			{
				log.debug("Failed to get checksum ");
				fields[1] = "none";
			}

		}
		setTimeStampFields(fields);
		// update the cache
		checked.put(name, fields[1]);
	}

	/**
	 * set the timestamp fields
	 * 
	 * @param fields
	 * @throws IOException
	 */
	private void setTimeStampFields(String[] fields) throws IOException
	{
		FileWriter fw = new FileWriter(timestampFile);
		for (int i = 0; i < fields.length; i++)
		{
			fw.write(fields[i]);
			fw.write(":");
		}
		fw.close();
	}

	/**
	 * get the timestamp fields
	 * 
	 * @return
	 * @throws IOException
	 */
	private String[] getTimeStampFields() throws IOException
	{
		if (!timestampFile.exists())
		{
			return null;
		}
		else
		{
			FileReader fr = new FileReader(timestampFile);
			char[] c = new char[4096];
			int len = fr.read(c);
			fr.close();
			String tsContents = new String(c, 0, len);
			return tsContents.split(":");
		}
	}

	/**
	 * 
	 */
	public void setForceValidation()
	{
		checked.remove(name);

	}

	/**
	 * get the checksum using the segment name as a File
	 * 
	 * @param segmentdir
	 * @return
	 * @throws IOException
	 */
	public String getCheckSum() throws IOException
	{

		String[] field = getTimeStampFields();
		if (field != null && field.length >= 2)
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
	 * 
	 * @param l
	 * @throws IOException
	 */
	public void setTimeStamp(long l) throws IOException
	{

		String[] fields = getTimeStampFields();
		if (fields == null || fields.length < 1)
		{
			fields = new String[2];
			try
			{
				fields[1] = getNewCheckSum();
			}
			catch (Exception ex)
			{
				log.debug("Failed to get checksum ");
				fields[1] = "none";
			}
		}
		fields[0] = String.valueOf(l);
		setTimeStampFields(fields);
		timestampFile.setLastModified(l);
	}

	/**
	 * get the timestam associated with the segment dir
	 * 
	 * @return
	 * @throws IOException
	 */
	private long getTimeStamp() throws IOException
	{

		long ts = -1;
		String[] field = getTimeStampFields();
		if ( field != null && field.length >= 1)
		{
			ts = Long.parseLong(field[0]);
		}
		else
		{
			ts = timestampFile.lastModified();
		}
		return ts;
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
	private String getCheckSum(String segmentName) throws IOException
	{
		File segmentdir = SegmentInfo.getSegmentLocation(segmentName,
				localStructuredStorage, searchIndexDirectory);
		return getCheckSum(segmentLocation);
	}
	*/
	public boolean checkSegmentValidity( boolean force ) throws Exception {
		return checkSegmentValidity( force, force);
	}

	public boolean checkSegmentValidity( boolean force, boolean validate)
			throws Exception
	{
		if (!force && !validate)
		{
			return true;
		}
		String liveCheckSum = null;
		boolean cached = false;
		if (force || checked.get(name) == null)
		{

			liveCheckSum = getNewCheckSum();

		}
		else
		{
			cached = true;
			liveCheckSum = (String) checked.get(name);
		}
		String storedCheckSum = getCheckSum();
		if (!"none".equals(storedCheckSum) && !liveCheckSum.equals(storedCheckSum))
		{
			checked.remove(name);
			boolean check = false;
			if (cached)
			{

				log.debug("Performing Retry");
				check = checkSegmentValidity( true,validate);
			}
			else
			{
				log.debug(" No Retry");
			}
			if (!check)
			{
				if (!force)
				{
					log.info("Checksum Failed Live(" + name + ") = "
							+ liveCheckSum);
					log.info("Checksum Failed Stor(" + name + ") = "
							+ storedCheckSum);
				}
			}
			return check;
		}
		else
		{
			checked.put(name, liveCheckSum);
			return true;
		}

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
	public long getTotalSize() {
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.index.SegmentInfo#getSize()
	 */
	public long getSize()
	{
		// TODO Auto-generated method stub
		return segmentSize;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.index.SegmentInfo#loadSize()
	 */
	public void loadSize()
	{
		segmentSize = getTotalSize();
		
	}	
}
