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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public abstract class StaticHandler extends BasePortalHandler
{

	private Properties contentTypes = null;

	private static final ThreadLocal<StaticCache[]> staticCacheHolder = new ThreadLocal<StaticCache[]>();

	private static final Log log = LogFactory.getLog(StaticHandler.class);

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
			String path = req.getPathInfo();
			if (path.indexOf("..") >= 0)
			{
				res.sendError(404);
				return;
			}
			String realPath = servletContext.getRealPath(path);
			File f = new File(realPath);
			if (f.length() < 100 * 1024)
			{
				for (int i = 0; i < staticCache.length; i++)
				{
					StaticCache sc = staticCache[i];
					if (sc != null && path.equals(sc.path))
					{
						if (f.lastModified() > sc.lastModified)
						{
							sc.buffer = loadFileBuffer(f);
							sc.path = path;
							sc.lastModified = f.lastModified();
							sc.contenttype = getContentType(f);
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
				sc.buffer = loadFileBuffer(f);
				sc.path = path;
				sc.lastModified = f.lastModified();
				sc.contenttype = getContentType(f);
				sc.added = System.currentTimeMillis();
				sendContent(res, sc);
				return;

			}
			else
			{
				res.setContentType(getContentType(f));
				res.addDateHeader("Last-Modified", f.lastModified());
				res.setContentLength((int) f.length());
				sendContent(res, f);
				return;

			}

		}
		catch (IOException ex)
		{
			log.info("Failed to send portal content ", ex);
			res.sendError(404, ex.getMessage());
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
	 * @param res
	 * @param f
	 * @throws IOException
	 */
	private void sendContent(HttpServletResponse res, File f) throws IOException
	{
		FileInputStream fin = null;
		try
		{
			fin = new FileInputStream(f);
			res.setContentType(getContentType(f));
			res.addDateHeader("Last-Modified", f.lastModified());
			res.setContentLength((int) f.length());
			byte[] buffer = new byte[4096];
			int pos = 0;
			int bsize = buffer.length;
			int nr = fin.read(buffer, 0, bsize);
			OutputStream out = res.getOutputStream();
			while (nr > 0)
			{
				out.write(buffer, 0, nr);
				nr = fin.read(buffer, 0, bsize);
			}

		}
		finally
		{
			try
			{
				fin.close();
			}
			catch (Exception ex)
			{
			}
		}

	}

	/**
	 * load a file into a byte[]
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private byte[] loadFileBuffer(File f) throws IOException
	{
		FileInputStream fin = null;
		try
		{
			fin = new FileInputStream(f);
			byte[] buffer = new byte[(int) f.length()];
			int pos = 0;
			int remaining = buffer.length;
			int nr = fin.read(buffer, pos, remaining);
			while (nr > 0)
			{
				pos = pos + nr;
				remaining = remaining - nr;
				nr = fin.read(buffer, pos, remaining);
			}
			return buffer;
		}
		finally
		{
			try
			{
				fin.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	/**
	 * get the content type of the file
	 * 
	 * @param f
	 * @return
	 */
	String getContentType(File f)
	{
		String name = f.getName();
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
