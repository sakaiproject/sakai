package org.sakaiproject.search.index.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.index.SegmentInfo;

public class ClusterSegmentsStorage
{

	private static final String PACKFILE = "packet";

	private static final Log log = LogFactory
			.getLog(ClusterSegmentsStorage.class);

	private String searchIndexDirectory;

	private boolean debug = false;

	private boolean localStructuredStorage = false;

	private JDBCClusterIndexStore clusterIndexStore;

	
	public ClusterSegmentsStorage(String searchIndexDirectory,  JDBCClusterIndexStore clusterIndexStore, boolean localStructuredStorage, boolean debug) {
		this.localStructuredStorage = localStructuredStorage;
		this.clusterIndexStore = clusterIndexStore;
		this.searchIndexDirectory = searchIndexDirectory;
		this.debug = debug;
	}

	/**
	 * unpack a segment from a zip
	 * 
	 * @param addsi
	 * @param packetStream
	 * @param version
	 */
	protected void unpackSegment(SegmentInfo addsi, InputStream packetStream,
			long version) throws IOException
	{
		ZipInputStream zin = new ZipInputStream(packetStream);
		ZipEntry zipEntry = null;
		FileOutputStream fout = null;
		try
		{
			if ( log.isDebugEnabled() ) 
			log.debug("Starting Patch ");
			byte[] buffer = new byte[4096];
			while ((zipEntry = zin.getNextEntry()) != null)
			{

				long ts = zipEntry.getTime();
				// the zip entry needs to be a full path from the
				// searchIndexDirectory... hence this is correct

				File f = new File(searchIndexDirectory, zipEntry.getName());
				if ( log.isDebugEnabled() ) 
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
			if ( log.isDebugEnabled() ) 
			log.debug("Finished Patch");

			try
			{
				addsi.checkSegmentValidity(true);
			}
			catch (Exception ex)
			{
				throw new RuntimeException("Segment " + addsi.getName()
						+ " is corrupted ");
			}

			addsi.setVersion(version);
			addsi.setCreated();
			if ( log.isDebugEnabled() ) 
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
	 * unpack a segment from a zip
	 * 
	 * @param addsi
	 * @param packetStream
	 * @param version
	 */
	protected void unpackPatch(InputStream packetStream) throws IOException
	{
		ZipInputStream zin = new ZipInputStream(packetStream);
		ZipEntry zipEntry = null;
		FileOutputStream fout = null;
		try
		{
			if ( log.isDebugEnabled() ) 
			log.debug("Starting Patch ");
			byte[] buffer = new byte[4096];
			while ((zipEntry = zin.getNextEntry()) != null)
			{

				long ts = zipEntry.getTime();
				// the zip index name is the full path from the
				// searchIndexDirectory
				File f = new File(searchIndexDirectory, zipEntry.getName());
				if ( log.isDebugEnabled() ) 
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
			if ( log.isDebugEnabled() ) 
			log.debug("Finished Patch");

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
	protected File packSegment(SegmentInfo addsi, long newVersion)
			throws IOException
	{

		// just prior to packing a segment we can say its created
		addsi.setCreated();
		
		File tmpFile = new File(searchIndexDirectory, PACKFILE
				+ String.valueOf(System.currentTimeMillis()) + ".zip");
		ZipOutputStream zout = new ZipOutputStream(
				new FileOutputStream(tmpFile));
		addsi.setCheckSum();
		addsi.setTimeStamp( newVersion);

		byte[] buffer = new byte[4096];
		addFile(addsi.getSegmentLocation(), zout, buffer, 0);
		zout.close();
		// touch the version

		try
		{
			if ( log.isDebugEnabled() ) 
			log.debug("Packed " + tmpFile.getName() + "|"
					+ addsi.getCheckSum() + "|" + tmpFile.length()
					+ "|" + addsi);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tmpFile;
	}

	/**
	 * pack a segment into the zip
	 * 
	 * @param addsi
	 * @return
	 * @throws IOException
	 */
	protected File packPatch() throws IOException
	{
		File tmpFile = new File(searchIndexDirectory, PACKFILE
				+ String.valueOf(System.currentTimeMillis()) + ".zip");
		ZipOutputStream zout = new ZipOutputStream(
				new FileOutputStream(tmpFile));
		byte[] buffer = new byte[4096];

		ZipEntry ze = new ZipEntry("lastpatchmarker");
		ze.setTime(System.currentTimeMillis());
		zout.putNextEntry(ze);
		try
		{
			ByteArrayInputStream fin = new ByteArrayInputStream(
					"--PATCH MARKER--".getBytes());
			try
			{
				int len = 0;
				while ((len = fin.read(buffer)) > 0)
				{
					zout.write(buffer, 0, len);
				}
			}
			finally
			{
				fin.close();
			}
		}
		finally
		{
			zout.closeEntry();
		}
		// itertate over all segments present locally

		List l = clusterIndexStore.getLocalSegments();
		for (Iterator li = l.iterator(); li.hasNext();)
		{
			SegmentInfoImpl sgi = (SegmentInfoImpl) li.next();
			if ( sgi.isCreated() ) { // Only add segment locations that are created
				File f = sgi.getSegmentLocation();
				addFile(f, zout, buffer, sgi.getVersion());
			}
		}
		zout.close();
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
	private void addFile(File f, ZipOutputStream zout, byte[] buffer,
			long modtime) throws IOException
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
							addFile(files[i], zout, buffer, modtime);
						}
						else
						{
							if (files[i].lastModified() > modtime)
							{
								dolog("Adding " + files[i].getPath());
								addSingleFile(files[i], zout, buffer);
							}
							else
							{
								dolog("Skipping " + files[i].getPath());
							}
						}
					}
				}
			}
			else
			{
				addSingleFile(f, zout, buffer);
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

	private void addSingleFile(File file, ZipOutputStream zout, byte[] buffer)
			throws IOException
	{
		String path = file.getPath();
		if (path.startsWith(searchIndexDirectory))
		{
			path = path.substring(searchIndexDirectory.length());
		}
		ZipEntry ze = new ZipEntry(path);
		ze.setTime(file.lastModified());
		zout.putNextEntry(ze);
		try
		{
			InputStream fin = new FileInputStream(file);
			try
			{
				int len = 0;
				while ((len = fin.read(buffer)) > 0)
				{
					zout.write(buffer, 0, len);
				}
			}
			finally
			{
				fin.close();
			}
		}
		finally
		{
			zout.closeEntry();
		}

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


}
