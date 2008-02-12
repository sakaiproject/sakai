/*
 * Copyright 2003-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was basaed on the simular code in the Velocity Tools project.
 */

package org.sakaiproject.portal.charon.velocity;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletContext;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * Resource loader that uses the ServletContext of a webapp to load Velocity
 * templates. (it's much easier to use with servlets than the standard
 * FileResourceLoader, in particular the use of war files is transparent). The
 * default search path is '/' (relative to the webapp root), but you can change
 * this behaviour by specifying one or more paths by mean of as many
 * webapp.resource.loader.path properties as needed in the velocity.properties
 * file. All paths must be relative to the root of the webapp. To enable caching
 * and cache refreshing the webapp.resource.loader.cache and
 * webapp.resource.loader.modificationCheckInterval properties need to be set in
 * the velocity.properties file ... auto-reloading of global macros requires the
 * webapp.resource.loader.cache property to be set to 'false'.
 * 
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @author <a href="mailto:claude@savoirweb.com">Claude Brisson</a>
 * @version $Id: WebappLoader.java 21575 2007-02-15 13:40:21Z
 *          ian@caret.cam.ac.uk $
 */

public class WebappLoader extends ResourceLoader
{

	/** The root paths for templates (relative to webapp's root). */
	protected String[] paths = null;

	protected HashMap templatePaths = null;

	protected ServletContext servletContext = null;

	/**
	 * This is abstract in the base class, so we need it. <br>
	 * NOTE: this expects that the ServletContext has already been placed in the
	 * runtime's application attributes under its full class name (i.e.
	 * "javax.servlet.ServletContext").
	 * 
	 * @param configuration
	 *        the {@link ExtendedProperties} associated with this resource
	 *        loader.
	 */
	@Override
	public void init(ExtendedProperties configuration)
	{
		rsvc.debug("WebappLoader : initialization starting.");

		/* get configured paths */
		paths = configuration.getStringArray("path");
		if (paths == null || paths.length == 0)
		{
			paths = new String[1];
			paths[0] = "/";
		}
		else
		{
			/* make sure the paths end with a '/' */
			for (int i = 0; i < paths.length; i++)
			{
				if (!paths[i].endsWith("/"))
				{
					paths[i] += '/';
				}
				if ( paths[i].startsWith("./") ) {
					paths[i] = paths[i].substring(1);
				}
				rsvc.info("WebappLoader : added template path - '" + paths[i] + "'");
			}
		}

		/* get the ServletContext */
		Object obj = rsvc.getApplicationAttribute(ServletContext.class.getName());
		if (obj instanceof ServletContext)
		{
			servletContext = (ServletContext) obj;
		}
		else
		{
			rsvc.error("WebappLoader : unable to retrieve ServletContext");
		}

		/* init the template paths map */
		templatePaths = new HashMap();

		rsvc.debug("WebappLoader : initialization complete.");
	}

	/**
	 * Get an InputStream so that the Runtime can build a template with it.
	 * 
	 * @param name
	 *        name of template to get
	 * @return InputStream containing the template
	 * @throws ResourceNotFoundException
	 *         if template not found in classpath.
	 */
	@Override
	public synchronized InputStream getResourceStream(String name)
			throws ResourceNotFoundException
	{
		InputStream result = null;

		if (name == null || name.length() == 0)
		{
			throw new ResourceNotFoundException(
					"WebappLoader : No template name provided");
		}

		/*
		 * since the paths always ends in '/', make sure the name never starts
		 * with one
		 */
		while (name.startsWith("/"))
		{
			name = name.substring(1);
		}

		Exception exception = null;
		for (int i = 0; i < paths.length; i++)
		{
			try
			{
				result = servletContext.getResourceAsStream(paths[i] + name);

				/* save the path and exit the loop if we found the template */
				if (result != null)
				{
					templatePaths.put(name, paths[i]);
					break;
				}
			}
			catch (Exception e)
			{
				/* only save the first one for later throwing */
				if (exception == null)
				{
					exception = e;
				}
			}
		}

		/* if we never found the template */
		if (result == null)
		{
			String msg;
			if (exception == null)
			{
				msg = "WebappLoader : Resource '" + name + "' not found.";
			}
			else
			{
				msg = exception.getMessage();
			}
			/* convert to a general Velocity ResourceNotFoundException */
			throw new ResourceNotFoundException(msg);
		}

		return result;
	}

	private File getCachedFile(String rootPath, String fileName)
	{
		// we do this when we cache a resource,
		// so do it again to ensure a match
		while (fileName.startsWith("/"))
		{
			fileName = fileName.substring(1);
		}

		String savedPath = (String) templatePaths.get(fileName);
		return new File(rootPath + savedPath, fileName);
	}

	/**
	 * Checks to see if a resource has been deleted, moved or modified.
	 * 
	 * @param resource
	 *        Resource The resource to check for modification
	 * @return boolean True if the resource has been modified
	 */
	@Override
	public boolean isSourceModified(Resource resource)
	{
		String rootPath = servletContext.getRealPath("/");
		if (rootPath == null)
		{
			// rootPath is null if the servlet container cannot translate the
			// virtual path to a real path for any reason (such as when the
			// content is being made available from a .war archive)
			return false;
		}

		// first, try getting the previously found file
		String fileName = resource.getName();
		File cachedFile = getCachedFile(rootPath, fileName);
		if (!cachedFile.exists())
		{
			/* then the source has been moved and/or deleted */
			return true;
		}

		/*
		 * check to see if the file can now be found elsewhere before it is
		 * found in the previously saved path
		 */
		File currentFile = null;
		for (int i = 0; i < paths.length; i++)
		{
			currentFile = new File(rootPath + paths[i], fileName);
			if (currentFile.canRead())
			{
				/*
				 * stop at the first resource found (just like in
				 * getResourceStream())
				 */
				break;
			}
		}

		/* if the current is the cached and it is readable */
		if (cachedFile.equals(currentFile) && cachedFile.canRead())
		{
			/* then (and only then) do we compare the last modified values */
			return (cachedFile.lastModified() != resource.getLastModified());
		}
		else
		{
			/*
			 * we found a new file for the resource or the resource is no longer
			 * readable.
			 */
			return true;
		}
	}

	/**
	 * Checks to see when a resource was last modified
	 * 
	 * @param resource
	 *        Resource the resource to check
	 * @return long The time when the resource was last modified or 0 if the
	 *         file can't be read
	 */
	@Override
	public long getLastModified(Resource resource)
	{
		String rootPath = servletContext.getRealPath("/");
		if (rootPath == null)
		{
			// rootPath is null if the servlet container cannot translate the
			// virtual path to a real path for any reason (such as when the
			// content is being made available from a .war archive)
			return 0;
		}

		File cachedFile = getCachedFile(rootPath, resource.getName());
		if (cachedFile.canRead())
		{
			return cachedFile.lastModified();
		}
		else
		{
			return 0;
		}
	}
}
