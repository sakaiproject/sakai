/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * BasicContentTypeImage implements the ContentTypeImageService.
 * </p>
 */
public class BasicContentTypeImageService implements ContentTypeImageService
{
	private ServerConfigurationService serverConfigurationService = null;
	
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BasicContentTypeImageService.class);

	/** Map content type to image file name. */
	//protected Properties m_contentTypeImages = null;

	/** Map content type to display name. */
	//protected Properties m_contentTypeDisplayNames = null;

	/** Map content type to file extension. */
	//protected Properties m_contentTypeExtensions = null;

	/** Map file extension to content type. */
	protected Properties m_contentTypes = null;

	protected SortedMap<String, SortedSet<String>> m_mimetypes = null;

	/** Default file name for unknown types. */
	protected static final String DEFAULT_FILE = "/sakai/generic.gif";

	/** Default content type for unknown extensions. */
	protected static final String UNKNOWN_TYPE = "application/octet-stream";

	/** Another type reported when the file is unknown, Mac IE 5.2. */
	// Note: although this is reported by IE, it's not just binary...
	// protected static final String UNKNOWN_TYPE_II = "application/x-macbinary";
	/** The file name containing the image definitions. */
	protected String m_imageFileName = null;

	/** The file name containing the name definitions. */
	protected String m_nameFileName = null;

	/** The file name containing the extension definitions. */
	protected String m_extensionFileName = null;
	
	/** localized properties **/
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.ContentTypeProperties";
	private static final String DEFAULT_EXTENSIONFILEBUNDLE = "org.sakaiproject.localization.bundle.content_type.content_type_extensions";
	private static final String DEFAULT_IMAGEFILEBUNDLE = "org.sakaiproject.localization.bundle.content_type.content_type_images";
	private static final String DEFAULT_NAMEFILEBUNDLE = "org.sakaiproject.localization.bundle.content_type.content_type_names";
	private static final String RESOURCECLASS = "resource.class.contenttype";
	private static final String EXTENSIONFILEBUNDLE = "resource.bundle.contenttype.extensionfile";
	private static final String IMAGEFILEBUNDLE = "resource.bundle.contenttype.imagefile";
	private static final String NAMEFILEBUNDLE = "resource.bundle.contenttype.namefile";
	
	protected ResourceLoader m_contentTypeExtensions = null;
	protected ResourceLoader m_contentTypeImages = null;
	protected ResourceLoader m_contentTypeDisplayNames = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Inject ServerConfigurationService
	 * @param serverConfigurationService
	 */
	public void setServerConfigurationService(ServerConfigurationService service) {
		serverConfigurationService = service;
	}
	
	/**
	 * Set the file name containing the image definitions.
	 * 
	 * @param name
	 *        the file name.
	 */
	/*
	public void setImageFile(String name)
	{
		m_imageFileName = name;
	}
	*/

	/**
	 * Set the file name containing the name definitions.
	 * 
	 * @param name
	 *        the file name.
	 */
	/*
	public void setNameFile(String name)
	{
		m_nameFileName = name;
	}
	*/

	/**
	 * Set the file name containing the extension definitions.
	 * 
	 * @param name
	 *        the file name.
	 */
	/*
	public void setExtensionFile(String name)
	{
		m_extensionFileName = name;
	}
	*/
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		String resourceClass = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
		String extensionFileBundle = serverConfigurationService.getString(EXTENSIONFILEBUNDLE, DEFAULT_EXTENSIONFILEBUNDLE);
		String imageFileBundle = serverConfigurationService.getString(IMAGEFILEBUNDLE, DEFAULT_IMAGEFILEBUNDLE);
		String nameFileBundle = serverConfigurationService.getString(NAMEFILEBUNDLE, DEFAULT_NAMEFILEBUNDLE);
		
		m_contentTypeExtensions = new Resource().getLoader(resourceClass, extensionFileBundle);
		m_contentTypeImages = new Resource().getLoader(resourceClass, imageFileBundle);
		m_contentTypeDisplayNames = new Resource().getLoader(resourceClass, nameFileBundle);
		
		// read the content type extensions file, using extension as the key
		if (m_contentTypeExtensions != null) {
			m_contentTypes = new Properties();
			m_mimetypes = new TreeMap<String, SortedSet<String>>();
			
			Set<?> set = m_contentTypeExtensions.entrySet();
			Iterator<?> it = set.iterator();
			while (it.hasNext()) {
				Map.Entry<?,?> entry = (Map.Entry<?,?>) it.next();
				
				// MIME type is the string before the equal sign
				String type = entry.getKey().toString();
				//String type = key.substring(0, key.indexOf("="));
				if (!(type.startsWith("#"))) {
					
					// update the list of mime subtypes for the mime category
					int index = type.indexOf("/");
					if (index > 0 && (index + 1) < type.length())
					{
						String category = type.substring(0, index).trim();
						String subtype = type.substring(index + 1).trim();
						SortedSet<String> subtypes = (SortedSet<String>) m_mimetypes.get(category);
						if (subtypes == null)
						{
							subtypes = new TreeSet<String>();
						}
						subtypes.add(subtype);
						m_mimetypes.put(category, subtypes);
					}
					
					// extension string is after the equal sign
					// parse the extension string by space
					String tokens = entry.getValue().toString();
					//String tokens = value.substring(value.indexOf("=") + 1);
					
					StringTokenizer st = new StringTokenizer(tokens, " ", false);
					if (!st.hasMoreTokens()) continue;
	
					while (st.hasMoreTokens())
					{
						String ext = st.nextToken();
						m_contentTypes.put(ext, type);
					}
				}
				
				// Debug
				M_log.debug(entry.getKey() + " : " + entry.getValue());
			}
		}
		
//		try
//		{
//			// M_log.info("init()");
//
//			// ClassLoader cl = this.getClass().getClassLoader();
//
//			// read the images file
//			m_contentTypeImages = new Properties();
//			try
//			{
//				m_contentTypeImages.load(cl.getResourceAsStream(m_imageFileName));
//			}
//			catch (FileNotFoundException e)
//			{
//				M_log.warn("init(): File not found for images file: " + m_imageFileName);
//			}
//			catch (IOException e)
//			{
//				M_log.warn("init(): IOException with images file: " + m_imageFileName);
//			}
//
//			// read the display names file
//			m_contentTypeDisplayNames = new Properties();
//			try
//			{
//				m_contentTypeDisplayNames.load(cl.getResourceAsStream(m_nameFileName));
//			}
//			catch (FileNotFoundException e)
//			{
//				M_log.warn("init(): File not found for names file: " + m_nameFileName);
//			}
//			catch (IOException e)
//			{
//				M_log.warn("init(): IOException with names file: " + m_nameFileName);
//			}
//
//			// read the content type extensions file
//			// use content type as the key
//			
//			m_contentTypeExtensions = new Properties();
//			try
//			{
//				m_contentTypeExtensions.load(cl.getResourceAsStream(m_extensionFileName));
//			}
//			catch (FileNotFoundException e)
//			{
//				M_log.warn("init(): File not found for names file: " + m_extensionFileName);
//			}
//			catch (IOException e)
//			{
//				M_log.warn("init(): IOException with names file: " + m_extensionFileName);
//			}
//			
			
			// read the content type extensions file
			// use extension as the key
			// m_contentTypes = new Properties();
			// m_mimetypes = new TreeMap<String, String>();
			
//			try
//			{
//				// open the file for line reading
//				BufferedReader reader = new BufferedReader(new InputStreamReader(cl.getResourceAsStream(m_extensionFileName)));

//				// read each line
//				String line = null;
//				do
//				{
//					// read the line - null is eof
//					line = reader.readLine();
//					if ((line != null) && (line.length() != 0))
//					{
//						// skip the comment lines
//						if (line.startsWith("#")) continue;

//						// MIME type is the string before the equal sign
//						String type = line.substring(0, line.indexOf("="));

//						// also update the list of mime subtypes for the mime category
//						int index = type.indexOf("/");
//						if (index > 0 && (index + 1) < type.length())
//						{
//							String category = type.substring(0, index).trim();
//							String subtype = type.substring(index + 1).trim();
//							SortedSet subtypes = (SortedSet) m_mimetypes.get(category);
//							if (subtypes == null)
//							{
//								subtypes = new TreeSet();
//							}
//							subtypes.add(subtype);
//							m_mimetypes.put(category, subtypes);
//						}

						// extension string is after the equal sign
						// parse the extension string by space
//						String tokens = line.substring(line.indexOf("=") + 1);
//						StringTokenizer st = new StringTokenizer(tokens, " ", false);

//						if (!st.hasMoreTokens()) continue;

//						while (st.hasMoreTokens())
//						{
//							String ext = st.nextToken();
//							m_contentTypes.put(ext, type);
//						}
//					}
//				}
//				while (line != null);

//				reader.close();
//			}
//			catch (Exception e)
//			{
//				M_log.warn("init(): Exception with ext file: " + m_extensionFileName + " : " + e.toString());
//			}
//		}
//		catch (Throwable t)
//		{
//			M_log.warn("init(): ", t);
//		}
	} // init

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_contentTypeImages.clear();
		m_contentTypeImages = null;

		m_contentTypeDisplayNames.clear();
		m_contentTypeDisplayNames = null;

		m_contentTypeExtensions.clear();
		m_contentTypeExtensions = null;

		m_contentTypes.clear();
		m_contentTypes = null;

		m_mimetypes.clear();
		m_mimetypes = null;

		M_log.info("destroy()");

	} // shutdown

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ContentTypeImageService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Get the image file name based on the content type.
	 * 
	 * @param contentType
	 *        The content type string.
	 * @return The image file name based on the content type.
	 */
	public String getContentTypeImage(String contentType)
	{
		String image = null;
		
		if(contentType != null && m_contentTypeImages.getIsValid( contentType.toLowerCase() ) )
		{
			image = m_contentTypeImages.getString(contentType.toLowerCase());
		}

		// if not there, use the DEFAULT_FILE
		if (image == null ) image = DEFAULT_FILE;

		return image;

	} // getContentTypeImage

	/**
	 * Get the display name of the content type.
	 * 
	 * @param contentType
	 *        The content type string.
	 * @return The display name of the content type.
	 */
	public String getContentTypeDisplayName(String contentType)
	{
		String name = contentType;
		
		if(contentType != null)
		{
			name = m_contentTypeDisplayNames.getString(contentType.toLowerCase());
			// name = m_contentTypeDisplayNames.getProperty(contentType.toLowerCase());
		}
		
		// if not there, use the content type as the name
		if (name == null  || name.indexOf("missing key:") != -1) name = contentType;

		return name;

	} // getContentTypeDisplayName

	/**
	 * Get the file extension value of the content type.
	 * 
	 * @param contentType
	 *        The content type string.
	 * @return The file extension value of the content type.
	 */
	public String getContentTypeExtension(String contentType)
	{
		String extension = m_contentTypeExtensions.getString(contentType.toLowerCase());
		// String extension = m_contentTypeExtensions.getProperty(contentType.toLowerCase());

		// if not there, use empty String
		if (extension == null)
		{
			extension = "";
		}
		else
		{
			if (extension.indexOf(" ") != -1)
			{
				// there might be more than one extension for this MIME type, get one listed first
				extension = extension.substring(0, extension.indexOf(" "));
			}
		}

		return extension;

	} // getContentTypeExtension

	/**
	 * Get the content type string that is used for this file extension.
	 * 
	 * @param extension
	 *        The file extension (to the right of the dot, not including the dot).
	 * @return The content type string that is used for this file extension.
	 */
	public String getContentType(String extension)
	{
		String type = m_contentTypes.getProperty(extension.toLowerCase());

		// if not there, use the UNKNOWN_TYPE
		if (type == null) type = UNKNOWN_TYPE;

		return type;

	} // getContentTypeDisplayName

	/**
	 * Is the type one of the known types used when the file type is unknown?
	 * 
	 * @param contentType
	 *        The content type string to test.
	 * @return true if the type is a type used for unknown file types, false if not.
	 */
	public boolean isUnknownType(String contentType)
	{
		if (contentType.equals(UNKNOWN_TYPE)) return true;
		// if (contentType.equals(UNKNOWN_TYPE_II)) return true;

		return false;

	} // isUnknownType

	/**
	 * Access an ordered list of all mimetype categories.
	 * 
	 * @return The list of mimetype categories in alphabetic order.
	 */
	public List<String> getMimeCategories()
	{
		List<String> rv = new Vector<String>();
		Set<String> categories = m_mimetypes.keySet();
		if (categories != null)
		{
			rv.addAll(categories);
		}
		return rv;
	}

	/**
	 * Access an ordered list of all mimetype subtypes for a particular category.
	 * 
	 * @param category
	 *        The category.
	 * @return The list of mimetype subtypes in alphabetic order.
	 */
	public List<?> getMimeSubtypes(String category)
	{
		List rv = new Vector();
		Set subtypes = (Set) m_mimetypes.get(category);
		if (subtypes != null)
		{
			rv.addAll(subtypes);
		}
		return rv;
	}

} // BasicContentTypeImage