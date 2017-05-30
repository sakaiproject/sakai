/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.content.api;

import java.util.List;

/**
* <p>ContentTypeImageService is the Interface for looking up proper image based on the content type.  It also
* associates a display name and a set of file extensions to known types.</p>
*
* @version $Revision$
*/
public interface ContentTypeImageService
{
	/** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = ContentTypeImageService.class.getName();

	/**
	* Get the image file name based on the content type.
	* @param contentType The content type string.
	* @return The image file name based on the content type.
	*/
	@Deprecated
	public String getContentTypeImage(String contentType);
	
	/**
	* Get the font-awesome image class name based on the content type.
	* @param contentType The content type string.
	* @return The font-awesome image class name based on the content type.
	*/
	public String getContentTypeImageClass(String contentType);
	
	/**
	* Get the display name of the content type.
	* @param contentType The content type string.
	* @return The display name of the content type.
	*/
	public String getContentTypeDisplayName(String contentType);
	
	/**
	* Get the file extension value of the content type.
	* @param contentType The content type string.
	* @return The file extension value of the content type.
	*/
	public String getContentTypeExtension(String contentType);

	/**
	* Get the content type string that is used for this file extension.
	* @param extension The file extension (to the right of the dot, not including the dot).
	* @return The content type string that is used for this file extension.
	*/
	public String getContentType(String extension);

	/**
	* Is the type one of the known types used when the file type is unknown?
	* @param contentType The content type string to test.
	* @return true if the type is a type used for unknown file types, false if not.
	*/
	public boolean isUnknownType(String contentType);
	
	/**
	* Access an ordered list of all mimetype categories.
	* @return The list of mimetype categories in alphabetic order.
	*/
	public List<String> getMimeCategories();

	/**
	* Access an ordered list of all mimetype subtypes for a particular category. 
	* @param category The category.
	* @return The list of mimetype subtypes in alphabetic order.
	*/
	public List<?> getMimeSubtypes(String category);

}	// ContentTypeImageService



