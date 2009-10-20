package org.sakaiproject.search.index.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.index.SegmentInfo;

public class ClusterSegmentsStorage
{

	private static final String PACKFILE = "packet";

	private static final Log log = LogFactory.getLog(ClusterSegmentsStorage.class);

	private String searchIndexDirectory;

	private boolean debug = false;

	private boolean localStructuredStorage = false;

	private JDBCClusterIndexStore clusterIndexStore;

	private SearchService searchService;

	public ClusterSegmentsStorage(SearchService searchService,
			String searchIndexDirectory, JDBCClusterIndexStore clusterIndexStore,
			boolean localStructuredStorage, boolean debug)
	{
		this.localStructuredStorage = localStructuredStorage;
		this.clusterIndexStore = clusterIndexStore;
		this.searchIndexDirectory = searchIndexDirectory;
		this.debug = debug;
		this.searchService = searchService;
	}

	/**
	 * unpack a segment from a zip
	 * 
	 * @param addsi
	 * @param packetStream
	 * @param version
	 */
	protected void unpackSegment(SegmentInfo addsi, InputStream packetStream, long version)
			throws IOException
	{
		log
				.debug("================================Starting Unpack Segment==============================");
		ZipInputStream zin = new ZipInputStream(packetStream);
		ZipEntry zipEntry = null;
		FileOutputStream fout = null;
		try
		{
			File loc = addsi.getSegmentLocation();
			boolean locationExists = false;
			File unpackBase = new File(searchIndexDirectory);
			if (loc.exists())
			{
				locationExists = true;
				unpackBase = new File(searchIndexDirectory, "unpack");
			}
			byte[] buffer = new byte[4096];
			while ((zipEntry = zin.getNextEntry()) != null)
			{

				long ts = zipEntry.getTime();
				// the zip entry needs to be a full path from the
				// searchIndexDirectory... hence this is correct

				File f = new File(unpackBase, zipEntry.getName());
				if (log.isDebugEnabled())
					log.debug("         Unpack " + f.getAbsolutePath());
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

			if (locationExists)
			{
				Map<String, File> moved = new HashMap<String, File>();
				moveAll(new File(unpackBase, loc.getName()), loc, moved);
				deleteAll(unpackBase);
				// unfortunately we have to remove the files befor the reload
				// otherwise the checksums will fail.
				deleteSome(loc, moved);
				// force a reload before we delete the files,
				// since this is in the locked thread, this node will reload
				searchService.reload();

			}

			try
			{
				addsi.checkSegmentValidity(searchService.hasDiagnostics(),
						"Unpack Segment");
			}
			catch (Exception ex)
			{
				try
				{
					addsi.checkSegmentValidity(true, "Unpack Segment Failed");
				}
				catch (Exception e)
				{
					log.debug(e);
				}
				throw new RuntimeException("Segment " + addsi.getName()
						+ " is corrupted ");
			}

			addsi.setVersion(version);
			addsi.setCreated();

		}
		finally
		{
			try
			{
				fout.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}
		log
				.debug("================================Done Unpack Segment==============================");

	}

	/**
	 * @param log2
	 * @param moved
	 */
	private void deleteSome(File f, Map<String, File> moved)
	{
		if (f.isDirectory())
		{
			File[] fs = f.listFiles();
			for (int i = 0; i < fs.length; i++)
			{
				deleteSome(fs[i], moved);
			}
			if (moved.get(f.getPath()) == null)
			{
				f.delete();
				log.debug("          deleted " + f.getPath());
			}
		}
		else
		{
			if (moved.get(f.getPath()) == null)
			{
				f.delete();
				log.debug("          deleted " + f.getPath());
			}
		}

	}

	/**
	 * @param file
	 */
	private void moveAll(File src, File dest, Map<String, File> moved)
	{

		if (src.isDirectory())
		{
			File[] fs = src.listFiles();
			for (int i = 0; i < fs.length; i++)
			{
				moveAll(fs[i], new File(dest, fs[i].getName()), moved);
			}
		}
		else
		{
			if (dest.exists())
			{
				dest.delete();
			}
			else
			{
				File p = dest.getParentFile();
				if (!p.exists())
				{
					p.mkdirs();
				}
			}
			src.renameTo(dest);
			log.debug("          renamed " + src.getPath() + " to " + dest.getPath());
		}
		moved.put(dest.getPath(), dest);
	}

	/**
	 * @param loc
	 */
	private void deleteAll(File f)
	{
		if (f.isDirectory())
		{
			File[] fs = f.listFiles();
			for (int i = 0; i < fs.length; i++)
			{
				deleteAll(fs[i]);
			}
			f.delete();
			log.debug("          deleted " + f.getPath());
		}
		else
		{
			f.delete();
			log.debug("          deleted " + f.getPath());
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
		log
				.debug("================================Start Unpack Patch==============================");
		ZipInputStream zin = new ZipInputStream(packetStream);
		ZipEntry zipEntry = null;
		FileOutputStream fout = null;
		try
		{
			byte[] buffer = new byte[4096];
			while ((zipEntry = zin.getNextEntry()) != null)
			{

				long ts = zipEntry.getTime();
				// the zip index name is the full path from the
				// searchIndexDirectory
				File f = new File(searchIndexDirectory, zipEntry.getName());
				if (log.isDebugEnabled())
					log.debug("                Unpack " + f.getAbsolutePath());
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

		}
		finally
		{
			try
			{
				fout.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}
		log
				.debug("================================Done Unpack Patch==============================");

	}

	/**
	 * pack a segment into the zip
	 * 
	 * @param addsi
	 * @return
	 * @throws IOException
	 */
	protected File packSegment(SegmentInfo addsi, long newVersion) throws IOException
	{

		log
				.debug("================================Start Pack Segment==============================");
		// just prior to packing a segment we can say its created
		addsi.setCreated();

		File tmpFile = new File(searchIndexDirectory, PACKFILE
				+ String.valueOf(System.currentTimeMillis()) + ".zip");
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmpFile));
		addsi.setTimeStamp(newVersion);

		byte[] buffer = new byte[4096];
		addFile(addsi.getSegmentLocation(), zout, buffer, 0);
		zout.close();
		// touch the version

		try
		{
			if (log.isDebugEnabled())
				log.debug("    Packed Name[" + tmpFile.getName() + "]length["
						+ tmpFile.length() + "][" + addsi + "]");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		log
				.debug("================================Done Pack Segment==============================");
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
		log
				.debug("================================Start Pack Patch==============================");

		File tmpFile = new File(searchIndexDirectory, PACKFILE
				+ String.valueOf(System.currentTimeMillis()) + ".zip");
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmpFile));
		byte[] buffer = new byte[4096];

		ZipEntry ze = new ZipEntry("lastpatchmarker");
		ze.setTime(System.currentTimeMillis());
		zout.putNextEntry(ze);
		try
		{
			ByteArrayInputStream fin = new ByteArrayInputStream("--PATCH MARKER--"
					.getBytes());
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

		List<SegmentInfo> l = clusterIndexStore.getLocalSegments();
		for (Iterator<SegmentInfo> li = l.iterator(); li.hasNext();)
		{
			SegmentInfoImpl sgi = (SegmentInfoImpl) li.next();
			if (sgi.isCreated())
			{ // Only add segment locations that are created
				File f = sgi.getSegmentLocation();
				addFile(f, zout, buffer, sgi.getVersion());
			}
		}
		zout.close();
		log
				.debug("================================Done Pack Patch==============================");

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
	private void addFile(File f, ZipOutputStream zout, byte[] buffer, long modtime)
	throws IOException
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
							log.debug("               Add " + files[i].getPath());
							addSingleFile(files[i], zout, buffer);
						}
						else
						{
							log.debug("              Ignore " + files[i].getPath());
						}
					}
				}
			}
		}
		else
		{
			if (f.lastModified() > modtime)
			{
				addSingleFile(f, zout, buffer);
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

}
