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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.charon.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */
@Slf4j
public class PortalTestFileUtils
{
	/**
	 * @author ieb
	 */
	public interface RecurseAction
	{

		/**
		 * @param file
		 * @throws IOException
		 */
		void doFile(File file) throws IOException;

		/**
		 * @param f
		 */
		void doBeforeFile(File f);

		/**
		 * @param f
		 */
		void doAfterFile(File f);

	}

	/**
	 * delete all files under this file and including this file
	 * 
	 * @param f
	 * @throws IOException
	 */
	public static void deleteAll(File f) throws IOException
	{
		recurse(f, new RecurseAction()
		{
			public void doFile(File file) throws IOException
			{
				file.delete();
				if (file.exists())
				{
					throw new IOException("Failed to delete  " + file.getPath());
				}
			}

			public void doBeforeFile(File f)
			{
			}

			public void doAfterFile(File f)
			{
			}
		});
	}

	public static void recurse(File f, RecurseAction action) throws IOException
	{
		action.doBeforeFile(f);
		if (f.isDirectory())
		{
			File[] files = f.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].isDirectory())
					{
						recurse(files[i], action);
					}
					else
					{
						action.doFile(files[i]);
					}
				}
			}
		}
		action.doFile(f);
	}

	public static void recurseGrouped(File f, RecurseAction action) throws IOException
	{
		action.doBeforeFile(f);
		if (f.isDirectory())
		{
			File[] files = f.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					if (!files[i].isDirectory())
					{
						action.doFile(files[i]);
					}
				}
				
				action.doAfterFile(f);
				
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].isDirectory())
					{
						recurseGrouped(files[i], action);
					}
				}
			}
		}
		action.doFile(f);
	}

	/**
	 * pack a segment into the zip
	 * 
	 * @param addsi
	 * @return
	 * @throws IOException
	 */
	public static void pack(File source,final String basePath, final String replacePath, OutputStream output) throws IOException
	{
		log.debug("Packing "+source+" repacing "+basePath+" with "+replacePath);
		final ZipOutputStream zout = new ZipOutputStream(output);
		final byte[] buffer = new byte[1024 * 100];
		FileInputStream fin = null;
		try
		{
			recurse(source, new RecurseAction()
			{

				public void doFile(File file) throws IOException
				{
					if (!file.isDirectory())
					{
						log.debug("               Add " + file.getPath());
						addSingleFile(basePath, replacePath, file, zout, buffer);
					}
					else
					{
						log.debug("              Ignore " + file.getPath());
					}
				}

				public void doBeforeFile(File f)
				{
				}

				public void doAfterFile(File f)
				{
				}

			});
		}
		finally
		{
			zout.flush();
			try
			{
				zout.close();
			}
			catch (Exception e)
			{
			}
			try
			{
				fin.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private static void addSingleFile(String basePath, String replacePath, File file, ZipOutputStream zout,
			byte[] buffer) throws IOException
	{
		String path = file.getPath();
		if (path.startsWith(basePath))
		{
			path = replacePath + path.substring(basePath.length());
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

	/**
	 * unpack a segment from a zip
	 * 
	 * @param addsi
	 * @param packetStream
	 * @param version
	 */
	public static void unpack(InputStream source, File destination) throws IOException
	{
		ZipInputStream zin = new ZipInputStream(source);
		ZipEntry zipEntry = null;
		FileOutputStream fout = null;
		try
		{
			byte[] buffer = new byte[4096];
			while ((zipEntry = zin.getNextEntry()) != null)
			{

				long ts = zipEntry.getTime();
				// the zip entry needs to be a full path from the
				// searchIndexDirectory... hence this is correct

				File f = new File(destination, zipEntry.getName());
				log.debug("         Unpack {}" + f.getAbsolutePath());
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
			}
		}
	}

	public static String digest(String token) throws GeneralSecurityException,
			IOException
	{
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		return byteArrayToHexStr(sha1.digest(token.getBytes("UTF-8")));
	}

	private static String byteArrayToHexStr(byte[] data)
	{
		char[] chars = new char[data.length * 2];
		for (int i = 0; i < data.length; i++)
		{
			byte current = data[i];
			int hi = (current & 0xF0) >> 4;
			int lo = current & 0x0F;
			chars[2 * i] = (char) (hi < 10 ? ('0' + hi) : ('A' + hi - 10));
			chars[2 * i + 1] = (char) (lo < 10 ? ('0' + lo) : ('A' + lo - 10));
		}
		return new String(chars);
	}

	/**
	 * @param parentFile
	 * @throws IOException
	 */
	public static void listDirectory(File f) throws IOException
	{
		recurseGrouped(f, new RecurseAction()
		{

			long total = 0;

			public void doFile(File file) throws IOException
			{
				if (!file.isDirectory())
				{
					listFile(file);
					total += file.length();
				}

			}

			private void listFile(File file)
			{
				char[] c = new char[4];
				c[0] = '-';
				c[0] = file.isDirectory() ? 'd' : c[0];
				c[0] = file.isFile() ? 'f' : c[0];
				c[1] = file.isHidden() ? 'h' : '-';
				c[2] = file.canRead() ? 'r' : '-';
				c[3] = file.canWrite() ? 'w' : '-';
				log.info(new String(c) + "  " + getSizeStr(file.length()) + " "
						+ file.getName());

			}

			private String getSizeStr(long size)
			{
				String sizeStr = String.valueOf(size) + "B";
				if (size > (1024L * 1024L * 1024L * 10L))
				{
					sizeStr = String.valueOf(size / (1024L * 1024L * 1024L)) + "G";
				}
				else if (size > (1024L * 1024L * 10L))
				{
					sizeStr = String.valueOf(size / (1024L * 1024L)) + "M";
				}
				else if (size > (1024L * 10L))
				{
					sizeStr = String.valueOf(size / (1024L)) + "K";
				}
				return sizeStr;

			}

			public void doBeforeFile(File f)
			{
				if (f.isDirectory())
				{
					char[] c = new char[4];
					c[0] = '-';
					c[0] = f.isDirectory() ? 'd' : c[0];
					c[0] = f.isFile() ? 'f' : c[0];
					c[1] = f.isHidden() ? 'h' : '-';
					c[2] = f.canRead() ? 'r' : '-';
					c[3] = f.canWrite() ? 'w' : '-';
					log.info(new String(c) + "  " + f.getAbsolutePath());
					log.info("--------------------------------------------------------");
				}
			}

			public void doAfterFile(File f)
			{
				if (f.isDirectory())
				{
					log
							.info("Total :" + getSizeStr(total) + " "
									+ f.getAbsolutePath());
					log.info("--------------------------------------------------------");
					total = 0;
				} else {
					log.info("Not A Directory "+f.getAbsolutePath());
				}

			}

		});
		// TODO Auto-generated method stub

	}

}
