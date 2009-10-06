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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.index.SegmentInfo;

/**
 * @author ieb
 */

public class SegmentState
{

	private static final String VERSION = "1.0";

	private static final Log log = LogFactory.getLog(SegmentState.class);

	private Map<String, FileRecord> fileRecords;

	private long timeStamp = System.currentTimeMillis();

	private String name;

	/**
	 * @param timestampFile
	 * @throws IOException
	 * @throws IOException
	 */
	public SegmentState(SegmentInfo segInfo, File timestampFile) throws IOException
	{
		name = segInfo.getName();
		if (timestampFile == null)
		{
			analyze(segInfo);
		}
		else
		{
			load(timestampFile);
		}
	}

	/**
	 * @param timestampFile
	 * @throws IOException
	 */
	public void save(File checksumFile) throws IOException
	{
		File tmpFile = new File(checksumFile.getAbsolutePath() + ".tmp");
		FileWriter fw = new FileWriter(tmpFile);
		fw.append(VERSION).append("\n");
		fw.append(String.valueOf(timeStamp)).append("\n");
		for (Iterator<FileRecord> i = fileRecords.values().iterator(); i.hasNext();)
		{
			FileRecord fr = i.next();
			fw.append(fr.path).append(";");
			fw.append(fr.checksum).append(";");
			fw.append(String.valueOf(fr.length)).append(";");
			fw.append(String.valueOf(fr.lastMod)).append(";\n");
		}
		fw.close();
		tmpFile.renameTo(checksumFile);
	}

	/**
	 * @param timestampFile
	 * @throws IOException
	 */
	private void load(File checksumFile) throws IOException
	{
		fileRecords = new HashMap<String, FileRecord>();
		BufferedReader fr = new BufferedReader(new FileReader(checksumFile));
		String version = fr.readLine();
		if (VERSION.equals(version))
		{
			String ts = fr.readLine();
			timeStamp = Long.parseLong(ts);
			for (String line = fr.readLine(); line != null; line = fr.readLine())
			{
				String[] elements = line.split(";");
				FileRecord infr = new FileRecord();
				infr.path = elements[0];
				infr.checksum = elements[1];
				infr.length = Long.parseLong(elements[2]);
				infr.lastMod = Long.parseLong(elements[3]);
				fileRecords.put(infr.path, infr);
			}
		}
		else
		{
			log.warn("Segment (" + name + "): Unrecognized version number " + version);
		}
		fr.close();
	}

	/**
	 * @param segInfo
	 */
	public void analyze(SegmentInfo segInfo)
	{
		File[] files = segInfo.getSegmentLocation().listFiles();
		String basePath = segInfo.getSegmentLocation().getAbsolutePath();
		fileRecords = new HashMap<String, FileRecord>();
		MessageDigest md5 = null;
		try
		{
			md5 = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			log.error("Segment (" + name + "): MD5 not available ", e);
		}
		byte[] buffer = new byte[4096];
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				try
				{
					String echecksum = "none";
					if (md5 != null)
					{
						InputStream fin = new FileInputStream(files[i]);
						int len = 0;
						md5.reset();
						while ((len = fin.read(buffer)) > 0)
						{
							md5.update(buffer, 0, len);
						}
						fin.close();
						char[] encoding = "0123456789ABCDEF".toCharArray();
						byte[] checksum = md5.digest();
						char[] hexchecksum = new char[checksum.length * 2];
						for (int j = 0; j < checksum.length; j++)
						{
							int lo = checksum[j] & 0x0f;
							int hi = (checksum[j] >> 4) & 0x0f;
							hexchecksum[j * 2] = encoding[lo];
							hexchecksum[j * 2 + 1] = encoding[hi];
						}
						echecksum = new String(hexchecksum);
					}
					FileRecord fr = new FileRecord();
					fr.checksum = echecksum;
					fr.path = files[i].getAbsolutePath().substring(basePath.length());
					fr.lastMod = files[i].lastModified();
					fr.length = files[i].length();
					fileRecords.put(fr.path, fr);
				}
				catch (Exception ex)
				{
					log.error("Segment (" + name + "): Failed to generate checksum of "
							+ files[i].getAbsolutePath(), ex);
				}
			}
		}

	}

	public class FileRecord
	{

		public long length;

		public long lastMod;

		public String path;

		public String checksum = "none";

		/**
		 * @param sfr
		 * @return
		 */
		public String diff(FileRecord sfr)
		{
			StringBuilder sb = new StringBuilder();
			if (sfr == null)
			{
				return "new file";

			}
			if (!path.equals(sfr.path))
			{
				return "[not the same file]";
			}
			int mod = 0;
			if (!checksum.equals(sfr.checksum))
			{
				sb.append("content changed,");
				mod++;
			}
			if (lastMod > sfr.lastMod)
			{
				sb.append("newer;");
				mod++;
			}
			else if (lastMod < sfr.lastMod)
			{
				sb.append("older;");
				mod++;
			}
			else
			{
				sb.append("same age;");
			}
			if (length > sfr.length)
			{
				sb.append("larger;");
				mod++;
			}
			else if (length < sfr.length)
			{
				sb.append("smaller;");
				mod++;
			}
			else
			{
				sb.append("same size;");
			}
			if (mod != 0)
			{
				return sb.toString();
			}
			return "identical";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return path + ";" + new Date(lastMod) + ";" + length + ";";
		}

	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp()
	{
		return timeStamp;
	}

	/**
	 * @param timeStamp
	 *        the timeStamp to set
	 */
	public void setTimeStamp(long timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	/**
	 * Check the validity of this segment against the stored segment
	 * 
	 * @param message
	 * @param logging
	 * @param storedSegmentState
	 */
	public boolean checkValidity(boolean logging, String message,
			SegmentState storedSegmentState)
	{
		if (storedSegmentState == null)
		{
			if (logging)
			{
				log
						.info("Segment ("
								+ name
								+ "): The segment has no stored state, it may be new or it could be dammaged ");
			}
			return true;
		}
		StringBuilder sb = new StringBuilder();
		if (timeStamp > storedSegmentState.getTimeStamp())
		{
			sb.append(" This Segment has been modified ").append(name).append("\n");
		}
		for (Iterator<FileRecord> i = fileRecords.values().iterator(); i.hasNext();)
		{
			FileRecord fr = i.next();
			FileRecord sfr = storedSegmentState.getFileRecord(fr.path);
			String differences = fr.diff(sfr);

			sb.append("   Checking [").append(fr).append("]==[").append(sfr).append("] ")
					.append(differences).append("\n");
		}
		for (Iterator<FileRecord> i = storedSegmentState.iterator(); i.hasNext();)
		{
			FileRecord sfr = i.next();
			FileRecord fr = fileRecords.get(sfr.path);
			if (fr == null)
			{
				sb.append("   Dropped ").append("").append("\n");
			}
		}
		if (logging)
		{
			log.info("Segment (" + name + "): Checked " + name + "\n" + sb.toString());
		}
		return true;
	}

	/**
	 * @return
	 */
	private Iterator<FileRecord> iterator()
	{
		return fileRecords.values().iterator();
	}

	/**
	 * @param path
	 * @return
	 */
	private FileRecord getFileRecord(String path)
	{
		return fileRecords.get(path);
	}

}
