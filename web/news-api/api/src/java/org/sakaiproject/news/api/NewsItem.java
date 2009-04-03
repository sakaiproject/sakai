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
 * NewsItem is the Interface for a Sakai News message.
 * </p>
 * <p>
 * The news message has header fields (from, date) and a body (text). Each message also has an id, unique within the channel. All fields are read only.
 * </p>
 */
public interface NewsItem
{
	/**
	 * Access the title of the NewsItem.
	 * 
	 * @return The title of the NewsItem.
	 */
	public String getTitle();

	/**
	 * Access the description (or body) of the NewsItem.
	 * 
	 * @return The description of the NewsItem.
	 */
	public String getDescription();

	/**
	 * Access the time when the NewsItem was updated.
	 * 
	 * @return The time when the NewsItem was updated.
	 */
	public String getPubdate();

	/**
	 * Access the URL where the complete story can be found.
	 * 
	 * @return The URL where the complete story can be found.
	 */
	public String getLink();

	/**
	 * Set the title of the NewsItem.
	 * 
	 * @param title
	 *        The title of the NewsItem.
	 */
	public void setTitle(String title);

	/**
	 * Set the description of the NewsItem.
	 * 
	 * @param description
	 *        The description of the NewsItem.
	 */
	public void setDescription(String description);

	/**
	 * Set the time when the NewsItem was updated.
	 * 
	 * @param pubdate
	 *        The time when the NewsItem was updated.
	 */
	public void setPubdate(String pubdate);

	/**
	 * Set the URL where the complete story can be found.
	 * 
	 * @return link The URL where the complete story can be found.
	 */
	public void setLink(String link);
}
