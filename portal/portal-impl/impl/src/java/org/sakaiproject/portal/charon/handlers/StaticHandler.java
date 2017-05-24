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

package org.sakaiproject.portal.charon.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.util.URLUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler to process static content with an internal, in memory cache.
 * Care should be taken not to put large volumes of static content within the 
 * portal space that is handled by this Handler as it will lead to increased
 * memory usage.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
@Slf4j
public abstract class StaticHandler extends BasePortalHandler
{

	public static final int MAX_SIZE_KB = 100;
	private Properties contentTypes = null;

	private static final ThreadLocal<StaticCache[]> staticCacheHolder = new ThreadLocal<>();

	public StaticHandler()
	{
		contentTypes = new Properties();
		InputStream stream = null;
		try
		{
			stream = this.getClass().getResourceAsStream(
			"/org/sakaiproject/portal/charon/staticcontenttypes.config");
			contentTypes.load(stream);
		}
		catch (IOException e)
		{
			throw new RuntimeException(
					"Failed to load Static Content Types (staticcontenttypes.config) ", e);
		}
		finally
		{
			if (stream != null) 
			{
				try {
					stream.close();
				} catch (IOException e) {
					//nothing to be done here
				}
			}
		}

	}

	/**
	 * serve a registered static file
	 * 
	 * @param req
	 * @param res
	 * @param parts
	 * @throws IOException
	 */
	public void doStatic(HttpServletRequest req, HttpServletResponse res, String[] parts)
			throws IOException
	{
		try
		{
			StaticCache[] staticCache = staticCacheHolder.get();
			if (staticCache == null)
			{
				staticCache = new StaticCache[100];
				staticCacheHolder.set(staticCache);
			}
			String path = URLUtils.getSafePathInfo(req);
			if (path.indexOf("..") >= 0)
			{
				res.sendError(404);
				return;
			}
			InputStream inputStream;
			String filename = path.substring(path.lastIndexOf("/"));
			long lastModified = -1;
			long length = -1;
			String realPath = servletContext.getRealPath(path);
			if (realPath == null) {
				// We not uncompressing the webapps.
				URL url = servletContext.getResource(path);
				inputStream = url.openStream();
				if (url != null) {
					try {
						ZipEntry zipEntry = ((JarURLConnection)url.openConnection()).getJarEntry();
						lastModified = zipEntry.getLastModifiedTime().toMillis();
						length = zipEntry.getSize();
					} catch (ClassCastException cce) {
						// Can't get extra data, but should all work.
						log.debug("We don't seem to be a JAR either.", cce);
					}
				} else {
					res.sendError(404);
					return;
				}
			} else {
				File f = new File(realPath);
				inputStream = new FileInputStream(f);
				lastModified = f.lastModified();
				length = f.length();
			}
			if (length >= 0 && length < MAX_SIZE_KB * 1024)
			{
				for (int i = 0; i < staticCache.length; i++)
				{
					StaticCache sc = staticCache[i];
					if (sc != null && path.equals(sc.path))
					{
						// If we don't have a good last modified time it's cached forever
						if (lastModified > sc.lastModified)
						{
							sc.buffer = loadFileBuffer(inputStream, (int)length);
							sc.path = path;
							sc.lastModified = lastModified;
							sc.contenttype = getContentType(filename);
							sc.added = System.currentTimeMillis();
						}
						// send the output
						sendContent(res, sc);
						return;
					}
				}
				// not found in cache, find the oldest or null and evict
				// this is thread Safe, since the cache is per thread.
				StaticCache sc = null;
				for (int i = 1; i < staticCache.length; i++)
				{
					StaticCache current = staticCache[i];
					if (sc == null)
					{
						sc = current;
					}
					if (current == null)
					{
						sc = new StaticCache();
						staticCache[i] = sc;
						break;
					}
					if (sc.added < current.added)
					{
						sc = current;
					}
				}
				sc.buffer = loadFileBuffer(inputStream, (int)length);
				sc.path = path;
				sc.lastModified = lastModified;
				sc.contenttype = getContentType(filename);
				sc.added = System.currentTimeMillis();
				sendContent(res, sc);
				return;

			}
			else
			{
				res.setContentType(getContentType(filename));
				res.addDateHeader("Last-Modified", lastModified);
				res.setContentLength((int) length);
				sendContent(res, inputStream);
				return;
			}

		}
		catch (IOException ex)
		{
			log.info("Failed to send portal content");
			log.debug("Full detail of exception is: {}", ex);
			res.sendError(404, URLUtils.getSafePathInfo(req));
		}

	}

	/**
	 * a simple static cache holder
	 * 
	 * @author ieb
	 */
	protected class StaticCache
	{

		public Object path;

		public long added;

		public byte[] buffer;

		public long lastModified;

		public String contenttype;

	}

	/**
	 * send the static content from the file
	 * 
	 * @param res The ServletResponse to write the content to.
	 * @param inputStream The InputStream to read from
	 * @throws IOException
	 */
	private void sendContent(HttpServletResponse res, InputStream inputStream) throws IOException
	{
		try
		{
			byte[] buffer = new byte[4096];
			int bsize = buffer.length;
			int nr = inputStream.read(buffer, 0, bsize);
			OutputStream out = res.getOutputStream();
			while (nr > 0)
			{
				out.write(buffer, 0, nr);
				nr = inputStream.read(buffer, 0, bsize);
			}

		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch (Exception ex)
			{
			}
		}

	}

	/**
	 * load a file into a byte[]
	 * 
	 * @param inputStream The InputStream to read from.
	 * @param length The length of the input.
	 * @return The loaded bytes
	 * @throws IOException
	 */
	private byte[] loadFileBuffer(InputStream inputStream, int length) throws IOException
	{
		try
		{
			byte[] buffer = new byte[length];
			int pos = 0;
			int remaining = buffer.length;
			int nr = inputStream.read(buffer, pos, remaining);
			while (nr > 0)
			{
				pos = pos + nr;
				remaining = remaining - nr;
				nr = inputStream.read(buffer, pos, remaining);
			}
			return buffer;
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	/**
	 * get the content type of the file
	 * 
	 * @param name The file name.
	 * @return The content type.
	 */
	String getContentType(String name)
	{
		int dot = name.lastIndexOf(".");
		String contentType = null;
		if (dot >= 0 && dot < name.length())
		{
			String ext = name.substring(dot+1);
			contentType = contentTypes.getProperty(ext);
		}
		return (contentType == null)?"application/octet-stream":contentType;
	}

	/**
	 * send the content from the static cache.
	 * 
	 * @param res
	 * @param sc
	 * @throws IOException
	 */
	void sendContent(HttpServletResponse res, StaticCache sc) throws IOException
	{
		if (sc.contenttype != null) {
			res.setContentType(sc.contenttype);
		}
		res.addDateHeader("Last-Modified", sc.lastModified);
		res.setContentLength(sc.buffer.length);
		res.getOutputStream().write(sc.buffer);

	}

}
