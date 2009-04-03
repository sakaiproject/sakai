/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.news.api;

/**
 * <p>
 * NewsItemEnclosure is the Interface for a Sakai News message enclsoure.
 * </p>
 * <p>
 * The news message enclsure has header fields (type, size) and a url of the actual enclosed file. All fields are read only.
 * </p>
 * 
 * @author Joshua Ryan joshua.ryan@asu.edu  alt^i
 */
public interface NewsItemEnclosure
{
	/**
	 * Access the Url of the enclosed item.
	 * 
	 * @return The url of the enclosure.
	 */
	public String getUrl();

	/**
	 * Access the type of the enclosure.
	 * 
	 * @return The type of the enclosed item.
	 */
	public String getType();

	/**
	 * Access the length in Bytes of the enclosed item.
	 * 
	 * @return The length in Bytes of the enclosed item.
	 */
	public long getLength();

	/**
	 * Set the url of the enclosure.
	 * 
	 * @param url
	 *        The url of the enclosure.
	 */
	public void setUrl(String url);

	/**
	 * Set the type of the enclosure.
	 * 
	 * @param type
	 *        The type of the enclosure.
	 */
	public void setType(String type);

	/**
	 * Set the length in Bytes of the enclosed item.
	 * 
	 * @param length
	 *        The length in Bytes of the enclosed item.
	 */
	public void setLength(long length);

}
